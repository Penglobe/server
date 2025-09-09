package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class StatisticsDTO {
    private Integer dayOfWeek;
    private Double totalCo2kg;

    public StatisticsDTO(Integer dayOfWeek, Double totalCo2kg) {
        this.dayOfWeek = dayOfWeek;
        this.totalCo2kg = totalCo2kg;
    }

    public Integer getDayOfWeek() {
        return dayOfWeek;
    }

    public Double getTotalCo2kg() {
        return totalCo2kg;
    }
}