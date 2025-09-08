// dto/user/UserProfileDTO.java
package com.penglobe.server.dto;

import lombok.*;
import java.math.BigDecimal;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String nickname;
    private Integer regionId;

    // 누적값
    private BigDecimal totalDistanceCo2Kg;
    private BigDecimal totalDietCo2Kg;
    private BigDecimal totalSurveyCo2Kg;

    // 합계
    private BigDecimal totalScore;
}
