package com.penglobe.server.domain.user;
import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "user_counters")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserCounters extends BaseEntity {

    /** users.id와 동일 (공유 PK) */
    @Id
    private Long userId;

    /** 공유 PK 매핑: user_counters.user_id = users.id */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id") // FK = PK (이건 꼭 필요)
    private User user;

    /** 누적 도보 절감량 (kg) */
    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDistanceCo2Kg = BigDecimal.ZERO;

    /** 누적 식단 절감량 (kg) */
    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalDietCo2Kg = BigDecimal.ZERO;

    /** 누적 설문 절감량 (kg) */
    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalSurveyCo2Kg = BigDecimal.ZERO;

    /** 누적 출석 일수 */
    @Builder.Default
    private Integer attendanceTotalDays = 0;

    /** 연속 출석 일수 */
    @Builder.Default
    private Integer attendanceStreakDays = 0;

    /** 마지막 출석 날짜 */
    private LocalDate lastAttendanceDate;

}
