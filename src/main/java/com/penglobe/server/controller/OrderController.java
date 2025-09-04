package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.OrderDTO;
import com.penglobe.server.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop/orders")
@Tag(name = "Shop - Orders", description = "굿즈샵 주문 API")
public class OrderController {

    private final OrderService orderService;

    @Operation(
            summary = "주문 생성(구매하기)",
            description = "사용자가 '구매하기'를 누르면 즉시 주문이 생성되고 포인트가 차감됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<OrderDTO>> place(
            @AuthenticationPrincipal(expression = "userId") Long userId,
            @RequestBody @Valid OrderDTO body // productId, qty만 전송됨
    ) {
        OrderDTO res = orderService.placeOrder(userId, body);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/shop/orders/" + res.getOrderId()))
                .body(ApiResponse.success(201, "주문 생성 완료", res));
    }

    @Operation(summary = "내 주문 목록", description = "로그인 사용자의 주문 내역을 최신순으로 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> myOrders(
            @AuthenticationPrincipal(expression = "userId") Long userId) {
        List<OrderDTO> res = orderService.myOrders(userId);
        return ResponseEntity.ok(ApiResponse.success(200, "주문 목록 조회 성공", res));
    }

    @Operation(summary = "주문 상세", description = "주문 ID로 상세 정보를 조회합니다. (상세 화면이 없으면 생략 가능)")
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDTO>> get(@PathVariable Long orderId) {
        OrderDTO res = orderService.get(orderId);
        return ResponseEntity.ok(ApiResponse.success(200, "주문 상세 조회 성공", res));
    }
}