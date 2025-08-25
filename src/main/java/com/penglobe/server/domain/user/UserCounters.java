package com.penglobe.server.domain.user;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "user_counters")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserCounters {

    @Id
    private Long userId; // users.id와 동일, PK + FK

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private Long totalDistanceM = 0L;
    private Long totalDietAmount = 0L;
    private Integer attendanceTotalDays = 0;
    private Integer attendanceStreakDays = 0;
    private LocalDate lastAttendanceDate;

}
