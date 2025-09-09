// src/main/java/com/penglobe/server/service/OrderService.java
package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.shop.Orders;
import com.penglobe.server.domain.shop.Products;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.OrderDTO;
import com.penglobe.server.repository.OrdersRepository;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.ProductsRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrdersRepository ordersRepository;
    private final ProductsRepository productsRepository;
    private final UserRepository userRepository;
    private final PointsLedgerRepository pointsLedgerRepository;

    /** 구매 즉시 주문 생성 (포인트 차감 포함) */
    @Transactional
    public OrderDTO placeOrder(Long userId, OrderDTO req) {
        Products product = productsRepository.findById(req.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));

        int qty = req.getQty();
        int need = product.getPrice() * qty;

        // 포인트 원자 차감 (0이면 부족)
        int updated = userRepository.deductPoints(userId, need);
        if (updated == 0) throw new IllegalStateException("포인트가 부족합니다.");

        User userRef = userRepository.getReferenceById(userId);

        Orders order = Orders.builder()
                .user(userRef)
                .product(product)
                .qty(qty)
                .totalPoints(need)
                .build(); // status는 @PrePersist 등에서 기본값 설정됨

        pointsLedgerRepository.save(
                PointsLedger.builder()
                        .user(userRef)
                        .changeAmount(-need)
                        .reason(LedgerReason.SHOP_PURCHASE)
                        .build()
        );

        Orders saved = ordersRepository.save(order);
        return toDTO(saved);
    }

    /** 내 주문 목록 (최신순) */
    public List<OrderDTO> myOrders(Long userId) {
        return ordersRepository.findByUser_UserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toDTO).toList();
    }

    /** 주문 상세 */
    public OrderDTO get(Long orderId) {
        Orders o = ordersRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문이 없습니다."));
        return toDTO(o);
    }

    private OrderDTO toDTO(Orders o) {
        return OrderDTO.builder()
                .orderId(o.getOrderId())
                .productId(null) // WRITE_ONLY라 응답에서 제외되지만 명시적으로 null
                .qty(o.getQty())
                .totalPoints(o.getTotalPoints())
                .productName(o.getProduct().getName())
                .status(o.getStatus() == null ? null : o.getStatus().name()) // ← Enum → String
                .createdAt(o.getCreatedAt())
                .build();
    }
}
