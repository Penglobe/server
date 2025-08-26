package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.yaml.snakeyaml.DumperOptions;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopCo2DTO {
    private Double co2;
    private String code;
}
