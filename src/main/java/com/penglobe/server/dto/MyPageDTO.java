package com.penglobe.server.dto;

import lombok.*;

import java.math.BigDecimal;

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
    private Integer regionId;
    private String regionName;
    private BigDecimal totalScore;

    public MyPageDTO(Long userId, String nickname, int totalScore) {
        this(userId, nickname, BigDecimal.valueOf(totalScore));
    }


    public MyPageDTO(Long userId, String nickname, BigDecimal totalScore) {
        this.userId = userId;
        this.nickname = nickname;
        this.totalScore = totalScore;
    }
}
