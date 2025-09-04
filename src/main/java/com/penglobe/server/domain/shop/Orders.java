package com.penglobe.server.domain.shop;

import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;        // <- 네 User 엔티티 경로로 변경
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "orders", // ✅ 명시 (order 예약어 회피)
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_product", columnList = "product_id")
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Orders extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", columnDefinition = "BIGINT UNSIGNED")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Products product;

    @Column(name = "qty", nullable = false)
    private Integer qty;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20) // ✅ 포터블 (선택)
    @Builder.Default
    private OrderStatus status = OrderStatus.ORDERED;

    @PrePersist
    void onCreate() {
        if (status == null) status = OrderStatus.ORDERED;
        // (선택) 안전망: totalPoints 미지정 시 계산
        if (totalPoints == null && product != null && qty != null) {
            totalPoints = product.getPrice() * qty;
        }
    }
}
