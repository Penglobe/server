package com.penglobe.server.dto;

import lombok.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class MyPageDTO {
    private Long userId;
    private String nickname;
    private Integer totalPoint;
    private Integer attendanceTotalDays;
    private Integer longestAttendanceStreak;
    private Integer attendanceStreakDays;
    private Integer regionId;
    @JsonProperty("regionName")
    private String regionName;
    private BigDecimal totalScore;
    @JsonProperty("profile")
    private String profile; // Added profile field

    // Explicit getters for Jackson serialization
    @JsonProperty("profile")
    public String getProfile() {
        return profile;
    }

    @JsonProperty("regionName")
    public String getRegionName() {
        return regionName;
    }

    public MyPageDTO(Long userId, String nickname, int totalScore) {
        this(userId, nickname, BigDecimal.valueOf(totalScore));
    }


    public MyPageDTO(Long userId, String nickname, BigDecimal totalScore) {
        this.userId = userId;
        this.nickname = nickname;
        this.totalScore = totalScore;
    }
}
