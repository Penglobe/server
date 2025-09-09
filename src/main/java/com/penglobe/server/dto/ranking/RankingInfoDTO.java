package com.penglobe.server.dto.ranking;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RankingInfoDTO {
    private final Long userId; // Added userId field
    private final Integer rank;
    private final String nickname;
    private final BigDecimal score;
    private final String profile; // Added profile field

    public RankingInfoDTO(Long userId, Integer rank, String nickname, BigDecimal score, String profile) { // Updated constructor
        this.userId = userId; // Assign userId
        this.rank = rank;
        this.nickname = nickname;
        this.score = score;
        this.profile = profile;
    }
}
