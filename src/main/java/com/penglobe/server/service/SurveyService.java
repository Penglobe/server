package com.penglobe.server.service;

import com.penglobe.server.domain.attendance.AttendanceType;
import com.penglobe.server.domain.survey.*;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.survey.*;
import com.penglobe.server.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional

public class SurveyService {

    private final SurveyOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyItemRepository surveyItemRepository;
    private final UserCountersRepository userCountersRepository;
    private final AttendanceLogService attendanceLogService;
    private final UserRepository userRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final SurveyLLMService surveyLLMService;

    //설문 보여주기
    public List<SurveyItemDTO> getTodaySurvey() {
        //모든 항목 불러오기
        List<SurveyItem> items = surveyItemRepository.findAll();
        List<SurveyItemDTO> result = new ArrayList<>();

        //문항별 옵션 불러오기
        for (SurveyItem item : items) {
            List<SurveyOption> options = optionRepository.findBySurveyItem_SurveyItemId(item.getSurveyItemId());

            //옵션 DTO 변환
            List<SurveyItemDTO.OptionDTO> dto = options.stream()
                    .map(o -> new SurveyItemDTO.OptionDTO(o.getValue()))
                    .toList();
            //질문 dto 변환
            result.add(new SurveyItemDTO(item.getSurveyItemId(), item.getQuestion(), dto));
        }
        return result;
    }

    //설문제출
    public SurveyResultDTO submitSurvey(SurveySubmitRequestDTO dto) {
        // 오늘 날짜 구간
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(LocalTime.MAX);

        //오늘 제출 여부 확인
        List<SurveyResponse> todayResponses = responseRepository
                .findByUserIdAndCreatedAtBetween(dto.getUserId(), startOfDay, endOfDay);

        // 이미 제출 → DB에 저장된 결과 반환
        if (!todayResponses.isEmpty()) {
            SurveyResponse latest = todayResponses.get(todayResponses.size() - 1);
            // DB에 저장된 top3 불러오기
            List<TopCo2DTO> top3 = new ArrayList<>();
            if (latest.getTop1() != null) top3.add(new TopCo2DTO(latest.getTop1()));
            if (latest.getTop2() != null) top3.add(new TopCo2DTO(latest.getTop2()));
            if (latest.getTop3() != null) top3.add(new TopCo2DTO(latest.getTop3()));

            double todayAverage = getTodayAverageCo2();

            return new SurveyResultDTO(
                    latest.getTotalCo2kg(),
                    dto.getUserId(),
                    top3,
                    true, //이미 제출함
                    latest.getCreatedAt(),
                    todayAverage, latest.getFeedback()
            );
        }

        //첫 제출일 때 SurveyResponse 객체 생성
        SurveyResponse response = new SurveyResponse();
        response.setUserId(dto.getUserId());

        List<TopCo2DTO> co2List = new ArrayList<>();
        double totalCo2 = 0;

        //답변 하나씩 처리
        for (SurveyAnswerDTO a : dto.getAnswer()) {
            //선택한 옵션 찾기
            SurveyOption option = optionRepository
                    .findBySurveyItem_SurveyItemIdAndValue(a.getItemId(), a.getSelectValue())
                    .orElseThrow(() -> new RuntimeException(
                            "옵션을 찾을 수 없습니다. itemId=" + a.getItemId() + ", value=" + a.getSelectValue()
                    ));

            //surveyAnswer 생성
            SurveyAnswer answer = new SurveyAnswer();
            answer.setSurveyResponse(response);
            answer.setSurveyItemId(option.getSurveyItem());
            answer.setCo2kg(option.getCo2kg());
            answer.setSelectValues(a.getSelectValue());
            answer.setUserId(dto.getUserId());

            response.getAnswers().add(answer);

            // 상대점수 계산 (최대 배출량 대비 비율)
            double maxCo2 = optionRepository.findMaxCo2ByItemId(option.getSurveyItem().getSurveyItemId());
            double relativeScore = maxCo2 == 0 ? 0 : option.getCo2kg() / maxCo2;

            co2List.add(new TopCo2DTO(relativeScore, option.getCo2kg(), option.getSurveyItem().getCode()));

            //총 배출량 누적
            totalCo2 += option.getCo2kg();
            totalCo2 = Math.round(totalCo2 * 100.0) / 100.0;
        }

        //UserCounters 업데이트
        response.setTotalCo2kg(totalCo2);
        responseRepository.save(response);

        //사용자 누적 배출량 갱신
        UserCounters userCounters = userCountersRepository.findByUserId(dto.getUserId())
                .orElseGet(() -> {
                    UserCounters c = new UserCounters();
                    c.setUserId(dto.getUserId());
                    c.setTotalSurveyCo2Kg(BigDecimal.ZERO);
                    return c;
                });

        BigDecimal newTotal = userCounters.getTotalSurveyCo2Kg().add(BigDecimal.valueOf(totalCo2));
        userCounters.setTotalSurveyCo2Kg(newTotal);
        userCountersRepository.save(userCounters);

        //오늘 전체 평균 갱신
        double todayAverage = updateDailyStatistics(totalCo2);

        //top3 선정 (상대점수 높은 순)
        List<TopCo2DTO> top3 = co2List.stream()
                .sorted((o1, o2) -> Double.compare(o2.getRelativeScore(), o1.getRelativeScore()))
                .limit(3)
                .collect(Collectors.toList());

        response.setTop1(top3.size() > 0 ? top3.get(0).getCode() : null);
        response.setTop2(top3.size() > 1 ? top3.get(1).getCode() : null);
        response.setTop3(top3.size() > 2 ? top3.get(2).getCode() : null);
        
        //사용자 객체 확인 (출석 체크용)
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. userId=" + dto.getUserId()));

        SurveyResultDTO surveyResult = new SurveyResultDTO(totalCo2, dto.getUserId(), top3, false, response.getCreatedAt(), todayAverage, null);

        // LLM 피드백 생성 및 저장
        String feedback;
        try {
            surveyResult = surveyLLMService.generateFeedback(surveyResult, top3);
            feedback = surveyResult.getFeedback();
            response.setFeedback(feedback);
            responseRepository.save(response); // DB 반영
        } catch (Exception e) {
            feedback = "피드백 생성 실패";
            surveyResult.setFeedback(feedback);
        }

        // 출석 로그 시도 (하루 1회만 인정)
        boolean newSurvey = attendanceLogService.markAttendance(user, AttendanceType.SURVEY);
        return new SurveyResultDTO(totalCo2, dto.getUserId(), top3, false, response.getCreatedAt(), todayAverage, feedback);
    }

    //오늘 설문 제출 여부 확인
    public boolean hasSubmittedToday(Long userId) {
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(LocalTime.MAX);

        return responseRepository.existsByUserIdAndCreatedAtBetween(userId, startOfDay, endOfDay);
    }

    // DailyStatistics 누적 및 평균 계산
    public double updateDailyStatistics(double totalCo2) {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue() -1;

        //오늘 날짜 없으면 새로 생성
        DailyStatistics avgDaily = dailyStatisticsRepository.findByUserIdIsNullAndDate(today)
                .orElseGet(() -> {
                    DailyStatistics d = new DailyStatistics();
                    d.setUserId(null);
                    d.setDate(today);
                    d.setDayOfWeek(dayOfWeek);
                    d.setStatisticsTotalCo2kg(0);
                    d.setUserCount(0);
                    return d;
                });

        //전체 누적값 업데이트
        avgDaily.setStatisticsTotalCo2kg(avgDaily.getStatisticsTotalCo2kg() + totalCo2);
        avgDaily.setUserCount(avgDaily.getUserCount() + 1);
        dailyStatisticsRepository.save(avgDaily);

        //평균값 계산
        double averageCo2 = avgDaily.getUserCount() > 0 ?
                Math.round(avgDaily.getStatisticsTotalCo2kg() / avgDaily.getUserCount() * 100.0) / 100.0
                : 0;
        return averageCo2;
    }

    // DailyStatistics에서 오늘 평균 조회
    private double getTodayAverageCo2() {
        LocalDate today = LocalDate.now();
        DailyStatistics avgDaily = dailyStatisticsRepository.findByUserIdIsNullAndDate(today)
                .orElse(null);

        if (avgDaily == null || avgDaily.getUserCount() == 0) return 0;

        return Math.round(avgDaily.getStatisticsTotalCo2kg() / avgDaily.getUserCount() * 100.0) / 100.0;
    }

    //사용자 주간 배출량
    public double[] getUserWeeklyCo2(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        double[] weekCo2 = new double[7]; // 0=월, 6=일
        Arrays.fill(weekCo2, 0);

        //사용자별 주간 통계 조회
        List<StatisticsDTO> stats = dailyStatisticsRepository.findWeeklyAvgByUserId(userId, startOfWeek, endOfWeek);
        for (StatisticsDTO d : stats) {
            int index = (d.getDayOfWeek() + 5) % 7;
            weekCo2[index] = d.getTotalCo2kg();
        }
        return weekCo2;
    }

    //사용자 전체 주간 평균
    public double[] getTotalWeeklyCo2() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        double[] weekCo2 = new double[7];
        Arrays.fill(weekCo2, 0);

        //전체 사용자 주간 통계 조회
        List<StatisticsDTO> stats = dailyStatisticsRepository.findWeeklyAvgAllUsers(startOfWeek, endOfWeek);
        for (StatisticsDTO d : stats) {
            int index = (d.getDayOfWeek());
            double roundedCo2 = Math.round(d.getTotalCo2kg() * 100.0) / 100.0;
            weekCo2[index] = roundedCo2;
        }
        return weekCo2;
    }
}