package com.penglobe.server.domain.payment;

import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;


import java.time.LocalDateTime;


@Entity @Table(name = "payment_intent", indexes = {
        @Index(name="uk_payment_intent_merchant_uid", columnList = "merchant_uid", unique = true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@DynamicUpdate
public class PaymentIntent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(name = "merchant_uid", nullable = false, length = 64)
    private String merchantUid;


    @Column(nullable = false)
    private Integer amount; // KRW


    @Column(nullable = false, length = 100)
    private String name; // 결제명 (예: "얼음 10,000 충전")


    @Column(name = "callback_url", length = 300)
    private String callbackUrl; // 앱 딥링크


    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;


    @Column(name = "imp_uid", length = 64)
    private String impUid; // 포트원 거래번호


    @Column(name = "pg_provider", length = 40)
    private String pgProvider;


    private LocalDateTime paidAt;


    @Column(length = 200)
    private String failReason;


    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


    @PrePersist void prePersist(){
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    @PreUpdate void preUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
