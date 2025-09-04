package com.penglobe.server.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyPageDTO {
    private Long userId;
    private String nickname;
    private Integer totalPoint;
    private Integer attendanceTotalDays;
    private Integer longestAttendanceStreak;
    private Integer attendanceStreakDays;
}
