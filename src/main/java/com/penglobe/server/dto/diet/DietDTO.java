package com.penglobe.server.dto.diet;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietDTO {
    private Long dietId;              // 기록 ID
    private Long userId;              // 사용자 ID
    private BigDecimal co2Kg;         // 절감 배출량
    private BigDecimal totalDietCo2Kg; // 누적 식단 절감량
    private Integer todayCountAfter;   // 오늘 기록 개수
}
