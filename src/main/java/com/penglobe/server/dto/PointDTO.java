package com.penglobe.server.dto;

import com.penglobe.server.domain.ledger.LedgerReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PointDTO {
    private LocalDate eventDate; //날짜
    private Integer changeAmount; //포인트
    private LedgerReason reason; //포인트 지급/차감 사유
    private Integer balanceAfter; //남은 금액
}


