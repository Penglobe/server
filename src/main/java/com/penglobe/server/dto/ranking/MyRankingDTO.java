package com.penglobe.server.dto.ranking;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MyRankingDTO {
    private final Integer rank;
    private final BigDecimal score;

    public MyRankingDTO(Integer rank, BigDecimal score) {
        this.rank = rank;
        this.score = score;
    }
}
