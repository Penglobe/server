package com.penglobe.server.service;

import com.penglobe.server.domain.survey.SurveyAnswer;
import com.penglobe.server.domain.survey.SurveyItem;
import com.penglobe.server.domain.survey.SurveyOption;
import com.penglobe.server.domain.survey.SurveyResponse;
import com.penglobe.server.dto.survey.*;
import com.penglobe.server.repository.SurveyAnswerRepository;
import com.penglobe.server.repository.SurveyItemRepository;
import com.penglobe.server.repository.SurveyOptionRepository;
import com.penglobe.server.repository.SurveyResponseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
        SurveyResponse response = new SurveyResponse();
        response.setUserId(dto.getUserId());

        double totalCo2 = 0;
        List<TopCo2DTO> co2List = new ArrayList<>();

        System.out.println("@@@@@@@@@서비스@@@@@@@@@@" + dto.getUserId() + dto.getAnswer()) ;

        for (SurveyAnswerDTO a : dto.getAnswer()) {
            SurveyOption option = (SurveyOption) optionRepository.findBySurveyItem_SurveyItemIdAndValue(a.getItemId(), a.getSelectValue())
                    .orElseThrow(() -> new RuntimeException( "옵션을 찾을 수 없습니다. itemId=" + a.getItemId() + ", value=" + a.getSelectValue()));

            SurveyAnswer answer = new SurveyAnswer();
            answer.setSurveyResponse(response);
            answer.setSurveyItemId(option.getSurveyItem());
            answer.setCo2kg(option.getCo2kg());
            answer.setSelectValues(a.getSelectValue());
            System.out.println("@@@@@@@@@@@@" + option.getSurveyItem() + option.getCo2kg());

            //answer를 response에 추가 => 그래서 totalCo2
            response.getAnswers().add(answer);

            // 항목별 최대 CO₂ 조회 (상대점수 계산)
            double maxCo2 = optionRepository.findMaxCo2ByItemId(option.getSurveyItem().getSurveyItemId());
            double relativeScore = maxCo2 == 0 ? 0 : option.getCo2kg() / maxCo2;

            co2List.add(new TopCo2DTO(relativeScore, option.getCo2kg(), option.getSurveyItem().getCode()));
            totalCo2 += option.getCo2kg();
        }

        response.setTotalCo2kg(totalCo2);
        response.setSurveyDate(LocalDate.now());
        responseRepository.save(response);

    //상대점수 기준 Top3 선택
        List<TopCo2DTO> top3 = co2List.stream()
                .sorted((o1, o2) -> Double.compare(o2.getRelativeScore(), o1.getRelativeScore()))
                .limit(3)
                .collect(Collectors.toList());

        return new SurveyResultDTO(totalCo2, top3);

    }
}