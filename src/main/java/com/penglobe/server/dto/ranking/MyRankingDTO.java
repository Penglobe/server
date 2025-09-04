package com.penglobe.server.dto.ranking;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MyRankingDTO {
    private final Integer rank;
    private final BigDecimal score;
    private final Integer lastWeekRank;

    public MyRankingDTO(Integer rank, BigDecimal score, Integer lastWeekRank) {
        this.rank = rank;
        this.score = score;
        this.lastWeekRank = lastWeekRank;
    }
}
