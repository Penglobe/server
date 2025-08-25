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
    private LocalDate eventDate;
    private Integer changeAmount;
    private LedgerReason reason;
    private Integer balanceAfter;
}
