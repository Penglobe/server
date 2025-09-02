package com.penglobe.server.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SurveyItemDTO {
    private Long itemId;
    private String code;
    private List<OptionDTO> options;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OptionDTO {
        private Integer value;
    }
}