package com.penglobe.server.domain.mission;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "mission_definitions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mission_def_metric",
                columnNames = {"metric"} // metric은 하나만 존재
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MissionDefinition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long missionId;

    /** 미션 종류 (걷기/식단=kg, 출석=days) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 32)
    private MissionMetric metric;

    /** 첫 목표 시작값 (예: 걷기 10, 식단 30, 출석 10) */
    @Column(nullable = false)
    private Long startTarget;

    /** 목표 증가 단위 (예: 걷기 10, 식단 30, 출석 10) */
    @Column(nullable = false)
    private Long step;

    /** 보상 포인트(각 목표치 달성 시 지급) */
    @Column(nullable = false)
    private Integer rewardPoints;

}

