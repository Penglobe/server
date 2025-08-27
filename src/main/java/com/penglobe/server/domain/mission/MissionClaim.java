package com.penglobe.server.domain.mission;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mission_claims",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_claim_user_metric_target",
                columnNames = {"user_id","metric","target"} // ← DB 컬럼명
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MissionClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MissionMetric metric;

    /** 수령한 목표값(kg/days) */
    @Column(nullable = false)
    private Long target;

    @Column(nullable = false)
    private LocalDateTime claimedAt;

    @PrePersist
    void prePersist() {
        if (claimedAt == null) claimedAt = LocalDateTime.now();
    }
}

