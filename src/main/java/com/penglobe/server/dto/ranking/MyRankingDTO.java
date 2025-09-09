package com.penglobe.server.dto.ranking;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MyRankingDTO {
    private final Long userId; // Added userId field
    private final Integer rank;
    private final BigDecimal score;
    private final Integer lastWeekRank;
    private final String profile; // Added profile field

    public MyRankingDTO(Long userId, Integer rank, BigDecimal score, Integer lastWeekRank, String profile) { // Updated constructor
        this.userId = userId; // Assign userId
        this.rank = rank;
        this.score = score;
        this.lastWeekRank = lastWeekRank;
        this.profile = profile;
    }
}
