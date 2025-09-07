package com.penglobe.server.controller;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.BalanceDTO;
import com.penglobe.server.dto.PointDTO;
import com.penglobe.server.dto.PointLedgerResponse;
import com.penglobe.server.service.PointLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/points")
@RequiredArgsConstructor
@Tag(name="PointLedger", description = "포인트 조회 API")
public class PointLedgerController {
    private final PointLedgerService pointLedgerService;

    //거래 내역 조회
    @Operation(
            summary = "거래 내역 조회", description = "기간별 사용자 포인트 거래 내역 조회입니다. (기간을 설정하지 않으면 기본적으로 이번 달 내역을 조회합니다.)"
    )
    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<PointLedgerResponse>> getMyPointLedger(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        List<PointDTO> points = pointLedgerService.getUserPointList(userId);
        PointLedgerResponse response = new PointLedgerResponse(points, points.size());
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    //잔액 조회
    @Operation(
            summary = "잔액조회", description = "남은 잔액을 조회합니다."
    )
    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<BalanceDTO>> getMyBalance(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        BalanceDTO balance = pointLedgerService.getCurrentBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}
