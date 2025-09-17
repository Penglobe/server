package com.penglobe.server.controller;

import com.penglobe.server.dto.ranking.WeeklyRankingResponseDTO;
import com.penglobe.server.dto.RegionDTO;
import java.util.List;
import com.penglobe.server.service.RankingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/rankings")
@RequiredArgsConstructor
@Tag(name = "랭킹 컨트롤러", description = "주간, 전체, 지역별 랭킹 및 현재 로그인한 사용자의 랭킹을 출력합니다.")
public class RankingController {

    private final RankingService rankingService;

    @Operation(
            summary = "주간 랭킹",
            description = "현재 진행중인 주간 랭킹과 현재 로그인한 사용자의 랭킹 출력"
    )
    @GetMapping("/weekly")
    public ResponseEntity<WeeklyRankingResponseDTO> getWeeklyRanking(Authentication authentication) {
        Long currentUserId = requireUserId(authentication);

        rankingService.updateLiveWeeklyRanking(); // 실시간 주간 랭킹 업데이트
        WeeklyRankingResponseDTO response = rankingService.getWeeklyRanking(currentUserId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "지역별 탄소 절감량 랭킹 조회")
    @GetMapping("/regions")
    public ResponseEntity<List<RegionDTO>> getRegionRanking() {
        List<RegionDTO> rankings = rankingService.getRegionRankings();
        return ResponseEntity.ok(rankings);
    }

    @Operation(
            summary = "전체 랭킹",
            description = "전체 랭킹과 현재 로그인한 사용자의 랭킹 출력"
    )
    @GetMapping("/global")
    public ResponseEntity<WeeklyRankingResponseDTO> getAllRanking(Authentication authentication) {
        Long currentUserId = requireUserId(authentication);

        rankingService.updateAllRanking(); // 실시간 전체 랭킹 업데이트
        WeeklyRankingResponseDTO response = rankingService.getAllRanking(currentUserId);
        return ResponseEntity.ok(response);
    }

     // @Operation(
    //         summary = "랭킹 데이터 실시간 업데이트",
    //         description = "주간, 전체 랭킹 데이터를 실시간으로 업데이트합니다."
    // )
    // @GetMapping("/update-all")
    // public ResponseEntity<String> updateAllRankingsManually() {
    //     rankingService.updateLiveWeeklyRanking();
    //     rankingService.updateAllRanking();
    //     return ResponseEntity.ok("All rankings updated successfully!");
    // }

    private Long requireUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증이 필요합니다.");
        }
        Object p = authentication.getPrincipal();
        if (p instanceof Long l) return l;
        if (p instanceof String s) {
            try {
                return Long.parseLong(s);
            } catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "잘못된 인증 주체 형식");
            }
        }
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증 주체가 유효하지 않습니다.");
    }
}

