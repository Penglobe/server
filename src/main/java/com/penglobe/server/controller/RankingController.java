package com.penglobe.server.controller;

import com.penglobe.server.dto.ranking.WeeklyRankingResponseDTO;
import com.penglobe.server.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Tag(name = "주간 랭킹 컨트롤러", description = "현재 진행중인 주간 랭킹과 현재 로그인한 사용자의 랭킹을 출력합니다.")
public class RankingController {

    private final RankingService rankingService;

    @Operation(
            summary = "주간 랭킹",
            description = "현재 진행중인 주간 랭킹과 현재 로그인한 사용자의 랭킹 출력"
    )
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyRankingResponseDTO> getWeeklyRanking() {
        // TODO: 아래는 임시 ID입니다. 실제 사용자 ID 필요
        Long currentUserId = 1L; // 현재 로그인한 사용자의 ID (임시)

        WeeklyRankingResponseDTO response = rankingService.getWeeklyRanking(currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "전체 랭킹",
            description = "전체 랭킹과 현재 로그인한 사용자의 랭킹 출력"
    )
    @GetMapping("/global")
    public ResponseEntity<WeeklyRankingResponseDTO> getAllRanking() {
        // TODO: 아래는 임시 ID입니다. 실제 사용자 ID 필요
        Long currentUserId = 1L; // 현재 로그인한 사용자의 ID (임시)

        WeeklyRankingResponseDTO response = rankingService.getAllRanking(currentUserId);
        return ResponseEntity.ok(response);
    }
}
