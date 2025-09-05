package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopCo2DTO {
    private Double relativeScore; //top3 선정 용 상대점수
    private Double co2kg;
    private String code;

    public TopCo2DTO(String code) {
        this.code = code;
        this.co2kg = co2kg;
        this.relativeScore = relativeScore;
    }

}
