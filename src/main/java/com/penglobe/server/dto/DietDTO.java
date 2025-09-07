package com.penglobe.server.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietDTO {
    private Long dietId;      // 기록 ID
    private Long userId;      // 사용자 ID
    private BigDecimal co2Kg; // 절감 배출량
}

