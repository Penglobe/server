package com.penglobe.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal; // Added import

@Getter
@Setter
@AllArgsConstructor
public class RankedUserDTO {
    private Long userId;
    private String nickname;
    private Integer rank; // 이번 주 실시간 순위 (1~10)
    private BigDecimal totalCo2Saved; // 이번 주 절감량
    private Boolean isCurrentUser; // 현재 API를 요청한 유저인지 여부
}
