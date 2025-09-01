package com.penglobe.server.domain.ledger;
import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Collection;
@Entity
@Table(name = "point_ledger",
        indexes = {
                @Index(name = "idx_point_ledger_user_id", columnList = "user_id"),
                @Index(name = "idx_point_ledger_reason", columnList = "reason")
        })
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsLedger extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id")
    private Long pointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // FK → users.id

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;  // +적립 / -차감

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 30)
    private LedgerReason reason;
}