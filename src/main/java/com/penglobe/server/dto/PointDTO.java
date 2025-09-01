package com.penglobe.server.dto;

import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.ledger.LedgerReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PointDTO extends BaseEntity {
    private LocalDateTime eventDate; // PointsLedger의 BaseEntity.createdAt
    private Integer changeAmount;    // +적립 / -차감
    private LedgerReason reason;      // 사유
    private Integer balanceAfter;    // 잔액
}


