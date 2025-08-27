package com.penglobe.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RankedUserDTO {
    private Long userId;
    private String nickname;
    private int rank; // 이번 주 실시간 순위 (1~10)
    private double totalCo2Saved; // 이번 주 절감량
    private boolean isCurrentUser; // 현재 API를 요청한 유저인지 여부
}
