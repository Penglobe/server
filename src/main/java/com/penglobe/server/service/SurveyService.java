package com.penglobe.server.service;

import com.penglobe.server.domain.survey.SurveyAnswer;
import com.penglobe.server.domain.survey.SurveyItem;
import com.penglobe.server.domain.survey.SurveyOption;
import com.penglobe.server.domain.survey.SurveyResponse;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.survey.*;
import com.penglobe.server.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional

public class SurveyService {

    private final SurveyOptionRepository optionRepository;
    private final SurveyResponseRepository responseRepository;
    private final SurveyAnswerRepository answerRepository;
    private final SurveyItemRepository surveyItemRepository;
    private final UserCountersRepository userCountersRepository;

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
        //중복 제출 체크
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime startOfDay = today.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = today.toLocalDate().atTime(LocalTime.MAX);

        boolean alreadyExists = responseRepository.existsByUserIdAndCreatedAtBetween(
                dto.getUserId(), startOfDay, endOfDay
        );

//        if (alreadyExists) {
//            System.out.println("S################################이미 제출됨");
//            return null;
//        }

        //새로운 surveyResponse entity 생성 -> 설문 제출 기록용
        SurveyResponse response = new SurveyResponse();
        response.setUserId(dto.getUserId());

        //항목별 상새 점수 계산용
        List<TopCo2DTO> co2List = new ArrayList<>();
        double totalCo2 = 0;

        for (SurveyAnswerDTO a : dto.getAnswer()) {
            SurveyOption option = (SurveyOption) optionRepository.findBySurveyItem_SurveyItemIdAndValue(a.getItemId(), a.getSelectValue())
                    .orElseThrow(() -> new RuntimeException( "옵션을 찾을 수 없습니다. itemId=" + a.getItemId() + ", value=" + a.getSelectValue()));

            SurveyAnswer answer = new SurveyAnswer();
            answer.setSurveyResponse(response);
            answer.setSurveyItemId(option.getSurveyItem());
            answer.setCo2kg(option.getCo2kg());
            answer.setSelectValues(a.getSelectValue());
            answer.setUserId(dto.getUserId());

            //answer를 response에 추가 => 그래서 totalCo2
            response.getAnswers().add(answer);

            // 항목별 최대 CO₂ 조회 (상대점수 계산)
            double maxCo2 = optionRepository.findMaxCo2ByItemId(option.getSurveyItem().getSurveyItemId());
            double relativeScore = maxCo2 == 0 ? 0 : option.getCo2kg() / maxCo2;

            //topco2dto에 상대점수, 총 co2, code 저장
            co2List.add(new TopCo2DTO(relativeScore, option.getCo2kg(), option.getSurveyItem().getCode()));

            totalCo2 += option.getCo2kg();
            totalCo2 = Math.round(totalCo2*100.0) / 100.0;
        }

        response.setTotalCo2kg(totalCo2);
        responseRepository.save(response);

        //user_counters에 저장
        //누적 설문조사 co2
        UserCounters userCounters = userCountersRepository.findByUserId(dto.getUserId())
                .orElseGet(() -> {
                    UserCounters c = new UserCounters();
                    c.setUserId(dto.getUserId());
                    c.setTotalSurveyCo2Kg(BigDecimal.valueOf(response.getTotalCo2kg()));
                    return c;
                });
        BigDecimal newTotal = userCounters.getTotalSurveyCo2Kg().add(BigDecimal.valueOf(totalCo2));
        userCounters.setTotalSurveyCo2Kg(newTotal);
        userCountersRepository.save(userCounters);

    //상대점수 기준 Top3 선택
        List<TopCo2DTO> top3 = co2List.stream()
                .sorted((o1, o2) -> Double.compare(o2.getRelativeScore(), o1.getRelativeScore()))
                .limit(3)
                .collect(Collectors.toList());

        //response에 top1, 2, 3 저장
        response.setTop1(top3.size() > 0 ? top3.get(0).getCode() : null);
        response.setTop2(top3.size() > 1 ? top3.get(1).getCode() : null);
        response.setTop3(top3.size() > 2 ? top3.get(2).getCode() : null);

        return new SurveyResultDTO(totalCo2, top3);
    }
}