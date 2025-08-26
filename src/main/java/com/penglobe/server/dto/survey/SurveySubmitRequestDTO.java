package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveySubmitRequestDTO {
   private Long userId;
   private List<SurveyAnswerDTO> answer; // 사용자가 작성한 항목별 값(탄소배출량)
}
