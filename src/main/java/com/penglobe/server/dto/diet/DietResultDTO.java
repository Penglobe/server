package com.penglobe.server.dto.diet;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class DietResultDTO {
    private BigDecimal totalCo2Kg;
    private List<ItemResult> items;
    private List<String> unknown;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemResult {
        private String name;
        private BigDecimal co2Kg;
    }
}