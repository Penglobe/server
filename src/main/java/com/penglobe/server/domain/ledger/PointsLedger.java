package com.penglobe.server.domain.ledger;
import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "points_ledger",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "reason", "ref_table", "ref_id"}
                )
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PointsLedger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate eventDate;

    @Column(nullable = false)
    private Integer changeAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerReason reason;

    @Column(name = "ref_table", length = 50)
    private String refTable;

    @Column(name = "ref_id")
    private Long refId;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Lob
    private String metadataJson;
}
