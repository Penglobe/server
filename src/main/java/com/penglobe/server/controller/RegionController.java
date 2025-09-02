package com.penglobe.server.controller;

import com.penglobe.server.dto.RegionDTO;
import com.penglobe.server.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Tag(name = "지역별 랭킹 컨트롤러")
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "지역별 탄소 절감량 랭킹 조회")
    @GetMapping("/ranking")
    public ResponseEntity<List<RegionDTO>> getRegionRanking() {
        List<RegionDTO> rankings = regionService.getRegionRankings();
        return ResponseEntity.ok(rankings);
    }
}
