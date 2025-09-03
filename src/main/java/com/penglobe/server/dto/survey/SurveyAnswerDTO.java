package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
//항목별 id이랑 사용자가 선택한 값 받음
public class SurveyAnswerDTO {
    private Long itemId;
    private String selectValue; //선택 항목번호
}
