package com.penglobe.server.dto.ranking;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WeeklyRankingResponseDTO {
    private final List<RankingInfoDTO> top10;
    private final MyRankingDTO myRank;
}
