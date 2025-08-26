package com.penglobe.server.domain.mission;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mission_definitions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 사람이 읽기 쉬운 코드 (예: WALK, DIET, ATTEND) */
    private String code;

    /** 미션 종류 (걷기/식단=kg, 출석=days) */
    @Enumerated(EnumType.STRING)
    private MissionMetric metric;

    /** 첫 목표 시작값 (예: 걷기 10, 식단 30, 출석 10) */
    private Long startTarget;

    /** 목표 증가 단위 (예: 걷기 10, 식단 30, 출석 10) */
    private Long step;

    /** 보상 포인트(각 목표치 달성 시 지급) */
    private Integer rewardPoints;

    /** 제목/설명(표시용) */
    private String title;
    @Lob
    private String description;

}
