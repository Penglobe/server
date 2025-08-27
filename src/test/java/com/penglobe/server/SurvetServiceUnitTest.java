package com.penglobe.server;

import com.penglobe.server.domain.survey.SurveyItem;
import com.penglobe.server.domain.survey.SurveyOption;
import com.penglobe.server.dto.survey.SurveyAnswerDTO;
import com.penglobe.server.dto.survey.SurveyResultDTO;
import com.penglobe.server.dto.survey.SurveySubmitRequestDTO;
import com.penglobe.server.repository.SurveyAnswerRepository;
import com.penglobe.server.repository.SurveyOptionRepository;
import com.penglobe.server.repository.SurveyResponseRepository;
import com.penglobe.server.service.SurveyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class SurveyServiceUnitTest {

    @Mock
    private SurveyOptionRepository optionRepository;

    @Mock
    private SurveyAnswerRepository answerRepository;

    @Mock
    private SurveyResponseRepository responseRepository;

    @InjectMocks
    private SurveyService surveyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // save() 호출 시 입력 객체 그대로 반환
        when(answerRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(responseRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void submitSurvey_ShouldCalculateTotalCo2AndTop3() {

        SurveyAnswerDTO answer1 = new SurveyAnswerDTO(); answer1.setItemId(1L); answer1.setSelectValue(2);
        SurveyAnswerDTO answer2 = new SurveyAnswerDTO(); answer2.setItemId(2L); answer2.setSelectValue(1);
        SurveyAnswerDTO answer3 = new SurveyAnswerDTO(); answer3.setItemId(3L); answer3.setSelectValue(3);

        SurveySubmitRequestDTO submitRequest = new SurveySubmitRequestDTO();
        submitRequest.setUserId(1L);
        submitRequest.setAnswer(Arrays.asList(answer1, answer2, answer3));

        SurveyItem item1 = new SurveyItem(); item1.setItemId(1L); item1.setCode("재활용");
        SurveyItem item2 = new SurveyItem(); item2.setItemId(2L); item2.setCode("대중교통");
        SurveyItem item3 = new SurveyItem(); item3.setItemId(3L); item3.setCode("식습관");

        SurveyOption opt1 = new SurveyOption(); opt1.setSurveyItem(item1); opt1.setValue(2); opt1.setCo2(1.5);
        SurveyOption opt2 = new SurveyOption(); opt2.setSurveyItem(item2); opt2.setValue(1); opt2.setCo2(2.0);
        SurveyOption opt3 = new SurveyOption(); opt3.setSurveyItem(item3); opt3.setValue(3); opt3.setCo2(3.0);

        when(optionRepository.findBySurveyItem_ItemIdAndValue(1L, 2)).thenReturn(Optional.of(opt1));
        when(optionRepository.findBySurveyItem_ItemIdAndValue(2L, 1)).thenReturn(Optional.of(opt2));
        when(optionRepository.findBySurveyItem_ItemIdAndValue(3L, 3)).thenReturn(Optional.of(opt3));


        // maxCo2 조회 Mock (상대점수 계산용)
        when(optionRepository.findMaxCo2ByItemId(1L)).thenReturn(2.0);
        when(optionRepository.findMaxCo2ByItemId(2L)).thenReturn(2.0);
        when(optionRepository.findMaxCo2ByItemId(3L)).thenReturn(3.0);

        SurveyResultDTO result = surveyService.submitSurvey(submitRequest);

        assertThat(result.getTotalCo2()).isEqualTo(6.5);
        assertThat(result.getTop3()).hasSizeLessThanOrEqualTo(3);

        System.out.println("총 CO2: " + result.getTotalCo2());
        result.getTop3().forEach(top ->
                System.out.println(top.getCode() + " - CO2: " + top.getCo2() + " / 상대점수: " + top.getRelativeScore())
        );
    }
}
