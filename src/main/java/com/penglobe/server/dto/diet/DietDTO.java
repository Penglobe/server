package com.penglobe.server.dto.diet;

import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietDTO {
    // 저장 DTO
    private Long dietId;      // 기록 ID
    private Long userId;      // 사용자 ID
    private BigDecimal co2Kg; // 절감 배출량
}
