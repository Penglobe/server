package com.penglobe.server.dto.ranking;

import com.penglobe.server.domain.ranking.WeeklyRanking;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class RankingInfoDTO {
    private final Integer rank;
    private final String nickname;
    private final BigDecimal score;

    public RankingInfoDTO(WeeklyRanking weeklyRanking) {
        this.rank = weeklyRanking.getRanking();
        this.nickname = weeklyRanking.getNickname();
        this.score = weeklyRanking.getScore();
    }
}
