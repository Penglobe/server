package com.penglobe.server.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @Builder
public class UserTotalScoreDTO {
    private Long userId;
    private String nickname;
    private BigDecimal totalScore;

    public UserTotalScoreDTO(Long userId, String nickname, BigDecimal totalScore) {
        this.userId = userId;
        this.nickname = nickname;
        this.totalScore = totalScore;
    }

}
