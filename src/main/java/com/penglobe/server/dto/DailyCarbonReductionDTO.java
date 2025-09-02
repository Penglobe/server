package com.penglobe.server.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCarbonReductionDTO {
    private BigDecimal transportCo2Kg;
    private BigDecimal dietCo2Kg;
    private BigDecimal totalCo2Kg;
}
