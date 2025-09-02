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
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("설문응답 시 총 co2와 top3 계산")
    void submitSurvey_WithRealOptions_ShouldCalculateCorrectly() {
        // --- 사용자 응답 ---
        SurveyAnswerDTO a1 = new SurveyAnswerDTO(); a1.setItemId(1L); a1.setSelectValue(1); // 분리배출
        SurveyAnswerDTO a2 = new SurveyAnswerDTO(); a2.setItemId(2L); a2.setSelectValue(2); // 일회용품
        SurveyAnswerDTO a3 = new SurveyAnswerDTO(); a3.setItemId(3L); a3.setSelectValue(3); // 종이타월
        SurveyAnswerDTO a4 = new SurveyAnswerDTO(); a4.setItemId(4L); a4.setSelectValue(2); // 음식물쓰레기
        SurveyAnswerDTO a5 = new SurveyAnswerDTO(); a5.setItemId(5L); a5.setSelectValue(1); // 전자기기

        SurveySubmitRequestDTO request = new SurveySubmitRequestDTO();
        request.setUserId(1L);
        request.setAnswer(Arrays.asList(a1, a2, a3, a4, a5));

        // --- SurveyItem + Option 정의 ---
        SurveyItem i1 = new SurveyItem(); i1.setSurveyItemId(1L); i1.setCode("분리배출");
        SurveyItem i2 = new SurveyItem(); i2.setSurveyItemId(2L); i2.setCode("일회용품");
        SurveyItem i3 = new SurveyItem(); i3.setSurveyItemId(3L); i3.setCode("종이타월");
        SurveyItem i4 = new SurveyItem(); i4.setSurveyItemId(4L); i4.setCode("음식물쓰레기");
        SurveyItem i5 = new SurveyItem(); i5.setSurveyItemId(5L); i5.setCode("전자기기");

        // 선택된 옵션 Mock
        SurveyOption o1 = new SurveyOption(); o1.setSurveyItem(i1); o1.setValue(1); o1.setCo2kg(0.05);
        SurveyOption o2 = new SurveyOption(); o2.setSurveyItem(i2); o2.setValue(2); o2.setCo2kg(0.12);
        SurveyOption o3 = new SurveyOption(); o3.setSurveyItem(i3); o3.setValue(3); o3.setCo2kg(0.0);
        SurveyOption o4 = new SurveyOption(); o4.setSurveyItem(i4); o4.setValue(2); o4.setCo2kg(0.02);
        SurveyOption o5 = new SurveyOption(); o5.setSurveyItem(i5); o5.setValue(1); o5.setCo2kg(0.05);

        when(optionRepository.findBySurveyItem_SurveyItemIdAndValue(1L, 1)).thenReturn(Optional.of(o1));
        when(optionRepository.findBySurveyItem_SurveyItemIdAndValue(2L, 2)).thenReturn(Optional.of(o2));
        when(optionRepository.findBySurveyItem_SurveyItemIdAndValue(3L, 3)).thenReturn(Optional.of(o3));
        when(optionRepository.findBySurveyItem_SurveyItemIdAndValue(4L, 2)).thenReturn(Optional.of(o4));
        when(optionRepository.findBySurveyItem_SurveyItemIdAndValue(5L, 1)).thenReturn(Optional.of(o5));

        // maxCo2 Mock (상대점수 계산용)
        when(optionRepository.findMaxCo2ByItemId(1L)).thenReturn(0.05);
        when(optionRepository.findMaxCo2ByItemId(2L)).thenReturn(0.24);
        when(optionRepository.findMaxCo2ByItemId(3L)).thenReturn(0.15);
        when(optionRepository.findMaxCo2ByItemId(4L)).thenReturn(0.02);
        when(optionRepository.findMaxCo2ByItemId(5L)).thenReturn(0.05);

        // --- 실행 ---
        SurveyResultDTO result = surveyService.submitSurvey(request);

        // --- 검증 ---

        System.out.println("총 CO2: " + result.getTotalCo2());
        result.getTop3().forEach(top ->
                System.out.println(top.getCode() + " - CO2: " + top.getCo2kg() + " / 상대점수: " + top.getRelativeScore())
        );
    }
}
