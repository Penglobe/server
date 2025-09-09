package com.penglobe.server.dto.ranking;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RankingInfoDTO {
    private final Integer rank;
    private final String nickname;
    private final BigDecimal score;
    private final String profile; // Added profile field

    public RankingInfoDTO(Integer rank, String nickname, BigDecimal score, String profile) {
        this.rank = rank;
        this.nickname = nickname;
        this.score = score;
        this.profile = profile;
    }
}
