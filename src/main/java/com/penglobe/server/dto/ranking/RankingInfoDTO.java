package com.penglobe.server.dto.ranking;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RankingInfoDTO {
    private final Integer rank;
    private final String nickname;
    private final BigDecimal score;

    public RankingInfoDTO(Integer rank, String nickname, BigDecimal score) {
        this.rank = rank;
        this.nickname = nickname;
        this.score = score;
    }
}
