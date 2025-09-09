package com.penglobe.server.service;

import com.penglobe.server.domain.attendance.AttendanceType;
import com.penglobe.server.domain.survey.*;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.survey.*;
import com.penglobe.server.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional

public class SurveyService {

    private final SurveyOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;
    private final SurveyItemRepository surveyItemRepository;
    private final UserCountersRepository userCountersRepository;
    private final AttendanceLogService attendanceLogService;
    private final UserRepository userRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;
    private final SurveyLLMService surveyLLMService;

    //설문 보여주기
    public List<SurveyItemDTO> getTodaySurvey() {
        List<SurveyItem> items = surveyItemRepository.findAll();
        List<SurveyItemDTO> result = new ArrayList<>();

        for (SurveyItem item : items) {
            List<SurveyOption> options = optionRepository.findBySurveyItem_SurveyItemId(item.getSurveyItemId());

            List<SurveyItemDTO.OptionDTO> dto = options.stream()
                    .map(o -> new SurveyItemDTO.OptionDTO(o.getValue()))
                    .toList();

            result.add(new SurveyItemDTO(item.getSurveyItemId(), item.getQuestion(), dto));
        }

        return result;
    }

    public SurveyResultDTO submitSurvey(SurveySubmitRequestDTO dto) {
        // 오늘 날짜 구간
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(LocalTime.MAX);


        // 1️⃣ 오늘 제출 여부 확인
        List<SurveyResponse> todayResponses = responseRepository
                .findByUserIdAndCreatedAtBetween(dto.getUserId(), startOfDay, endOfDay);

        // ✅ 이미 제출한 경우 → 저장하지 않고 기존 결과 반환
        if (!todayResponses.isEmpty()) {
            SurveyResponse latest = todayResponses.get(todayResponses.size() - 1);
            List<TopCo2DTO> top3 = new ArrayList<>();
            if (latest.getTop1() != null) top3.add(new TopCo2DTO(latest.getTop1()));
            if (latest.getTop2() != null) top3.add(new TopCo2DTO(latest.getTop2()));
            if (latest.getTop3() != null) top3.add(new TopCo2DTO(latest.getTop3()));

            double todayAverage = getTodayAverageCo2();

            System.out.println("@@@@@@@" + latest.getFeedback());

            return new SurveyResultDTO(
                    latest.getTotalCo2kg(),
                    dto.getUserId(),
                    top3,
                    true, // submitted = true
                    latest.getCreatedAt(),
                    todayAverage, latest.getFeedback()
            );
        }

        // 2️⃣ 첫 제출일 때 SurveyResponse 생성 + totalCo2 계산
        SurveyResponse response = new SurveyResponse();
        response.setUserId(dto.getUserId());

        List<TopCo2DTO> co2List = new ArrayList<>();
        double totalCo2 = 0;

        for (SurveyAnswerDTO a : dto.getAnswer()) {
            SurveyOption option = optionRepository
                    .findBySurveyItem_SurveyItemIdAndValue(a.getItemId(), a.getSelectValue())
                    .orElseThrow(() -> new RuntimeException(
                            "옵션을 찾을 수 없습니다. itemId=" + a.getItemId() + ", value=" + a.getSelectValue()
                    ));

            SurveyAnswer answer = new SurveyAnswer();
            answer.setSurveyResponse(response);
            answer.setSurveyItemId(option.getSurveyItem());
            answer.setCo2kg(option.getCo2kg());
            answer.setSelectValues(a.getSelectValue());
            answer.setUserId(dto.getUserId());

            response.getAnswers().add(answer);

            // 상대점수 계산
            double maxCo2 = optionRepository.findMaxCo2ByItemId(option.getSurveyItem().getSurveyItemId());
            double relativeScore = maxCo2 == 0 ? 0 : option.getCo2kg() / maxCo2;

            co2List.add(new TopCo2DTO(relativeScore, option.getCo2kg(), option.getSurveyItem().getCode()));

            totalCo2 += option.getCo2kg();
            totalCo2 = Math.round(totalCo2 * 100.0) / 100.0;
        }

        // 3️⃣ UserCounters 업데이트
        response.setTotalCo2kg(totalCo2);
        responseRepository.save(response);

        // ✅ UserCounters 업데이트
        UserCounters userCounters = userCountersRepository.findByUserId(dto.getUserId())
                .orElseGet(() -> {
                    UserCounters c = new UserCounters();
                    c.setUserId(dto.getUserId());
                    c.setTotalSurveyCo2Kg(BigDecimal.ZERO);
                    return c;
                });

        BigDecimal newTotal = userCounters.getTotalSurveyCo2Kg().add(BigDecimal.valueOf(totalCo2));
        userCounters.setTotalSurveyCo2Kg(newTotal);
        double useraverageCo2kg = updateDailyStatistics(totalCo2); // 오늘 전체 평균 계산

        userCountersRepository.save(userCounters);

        double todayAverage = updateDailyStatistics(totalCo2);

        // 4️⃣ Top3 선정
        List<TopCo2DTO> top3 = co2List.stream()
                .sorted((o1, o2) -> Double.compare(o2.getRelativeScore(), o1.getRelativeScore()))
                .limit(3)
                .collect(Collectors.toList());

        response.setTop1(top3.size() > 0 ? top3.get(0).getCode() : null);
        response.setTop2(top3.size() > 1 ? top3.get(1).getCode() : null);
        response.setTop3(top3.size() > 2 ? top3.get(2).getCode() : null);
        
        // User 객체 가져오기
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
        if (newSurvey) {
            log.info("오늘 첫 출석 인정 ✅");

        } else {
            log.info("이미 오늘 제출함 → 무시");
        }

        System.out.println("####################3 " + surveyResult);
        return new SurveyResultDTO(totalCo2, dto.getUserId(), top3, false, response.getCreatedAt(), todayAverage, feedback);
    }


    // DailyStatistics 누적 및 평균 계산
    public double updateDailyStatistics(double totalCo2) {
        LocalDate today = LocalDate.now();
        int dayOfWeek = today.getDayOfWeek().getValue();

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

        avgDaily.setStatisticsTotalCo2kg(avgDaily.getStatisticsTotalCo2kg() + totalCo2);
        avgDaily.setUserCount(avgDaily.getUserCount() + 1);
        dailyStatisticsRepository.save(avgDaily);

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

    //사용자 totalco2
    public double[] getUserWeeklyCo2(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        double[] weekCo2 = new double[7]; // 0=월, 6=일
        Arrays.fill(weekCo2, 0);

        List<StatisticsDTO> stats = dailyStatisticsRepository.findWeeklyAvgByUserId(userId, startOfWeek, endOfWeek);
        for (StatisticsDTO d : stats) {
            int index = (d.getDayOfWeek() + 5) % 7;
            weekCo2[index] = d.getTotalCo2kg();
        }
        return weekCo2;
    }

    //사용자 전체 평균
    public double[] getTotalWeeklyCo2() {
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        double[] weekCo2 = new double[7];
        Arrays.fill(weekCo2, 0);

        List<StatisticsDTO> stats = dailyStatisticsRepository.findWeeklyAvgAllUsers(startOfWeek, endOfWeek);
        for (StatisticsDTO d : stats) {
            int index = (d.getDayOfWeek());
            double roundedCo2 = Math.round(d.getTotalCo2kg() * 100.0) / 100.0;
            weekCo2[index] = roundedCo2;

        }
        return weekCo2;
    }


}