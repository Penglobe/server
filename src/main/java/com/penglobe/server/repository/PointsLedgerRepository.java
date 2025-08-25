package com.penglobe.server.repository;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
    //사용자 id로 포인트 사용내역 조회
    List<PointsLedger> pointListById(User user);
}
