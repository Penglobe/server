package com.penglobe.server.repository;

import com.penglobe.server.domain.ledger.PointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
}
