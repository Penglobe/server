package com.penglobe.server.repository;

import com.penglobe.server.domain.shop.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
    // 내 주문 목록 (최신순) — BaseEntity에 createdAt이 있다 가정
    List<Orders> findByUser_UserIdOrderByCreatedAtDesc(Long userId);
}
