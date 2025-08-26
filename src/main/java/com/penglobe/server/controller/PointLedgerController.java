package com.penglobe.server.controller;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.dto.BalanceDTO;
import com.penglobe.server.dto.PointDTO;
import com.penglobe.server.dto.PointLedgerResponse;
import com.penglobe.server.service.PointLedgerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/ledger/{userId}")
    public ResponseEntity<PointLedgerResponse> getPointLedger(@PathVariable Long userId,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to ) {
        List<PointDTO> points = pointLedgerService.getUserPointList(userId, from, to);
        PointLedgerResponse response = new PointLedgerResponse(points, points.size());
        return ResponseEntity.ok(response);
    }

    //잔액 조회
    @Operation(
            summary = "잔액조회", description = "남은 잔액을 조회합니다."
    )
    @GetMapping("/balance/{userId}")
    public ResponseEntity<BalanceDTO> getBalance(@PathVariable Long userId) {
        BalanceDTO balance = pointLedgerService.getCurrentBalance(userId);
        return ResponseEntity.ok(balance);
    }
}
