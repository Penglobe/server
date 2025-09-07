package com.penglobe.server.dto.diet;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietRequestDTO {

    @NotNull
    @Positive
    private Long userId;
    private List<FoodItem> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true) // JSON에 더 많은 필드 있어도 무시
    public static class FoodItem {
        private Long id;
        private String name;
        private String brand;
        private Object serving;
        private Object amount;
        private Object nutrition;
    }
}
