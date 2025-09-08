package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyResultDTO {
    private Double totalCo2; // 최종 배출량
    private Long userId;
    private List<TopCo2DTO> top3; // co2 배출량이 높은 상위 3개 항목의 정보
    private boolean submitted; // 오늘 이미 제출했는지 여부
    private LocalDateTime surveyDate; // 제출 시각

}
