package com.penglobe.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class PersonalRankingResponseDTO {
    private Integer previousWeekRank; // 나의 지난 주 최종 순위
    private List<RankedUserDTO> currentRanking; // 이번 주 그룹의 실시간 랭킹 목록
}
