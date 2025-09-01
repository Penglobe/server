package com.penglobe.server.domain.mission;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mission_claims",
        uniqueConstraints = {@UniqueConstraint(
                name = "uk_claim_user_metric_target",
                columnNames = {"user_id", "metric", "target"}),
                @UniqueConstraint(
                        name = "uk_claim_user_metric_period",
                        columnNames = {"user_id", "metric", "period_month"})
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MissionClaim extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionClaimsId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private MissionMetric metric;

    /** 수령한 목표값(kg/days) */
    @Column(nullable = false)
    private Long target;

    @Column(name = "period_month", length = 7) // "YYYY-MM"
    private String periodMonth;                // 월간 미션용(출석)
}

