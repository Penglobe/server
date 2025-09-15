package com.penglobe.server.dto.diet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
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

    @NotNull
    @JsonAlias({"mealType","eat_mode","eatMode"})
    private EatMode eatMode;

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

    public enum EatMode {
        HOME, DELIVERY, TAKEOUT, RESTAURANT;
        @JsonCreator
        public static EatMode from(String v) {
            return v == null ? null : EatMode.valueOf(v.trim().toUpperCase());
        }
    }
}
