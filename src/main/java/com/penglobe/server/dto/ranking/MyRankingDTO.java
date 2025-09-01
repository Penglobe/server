package com.penglobe.server.dto.ranking;

import com.penglobe.server.domain.ranking.WeeklyRanking;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class MyRankingDTO {
    private final Integer rank;
    private final BigDecimal score;

    public MyRankingDTO(WeeklyRanking weeklyRanking) {
        this.rank = weeklyRanking.getRanking();
        this.score = weeklyRanking.getScore();
    }
}
