package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.BalanceDTO;
import com.penglobe.server.dto.PointDTO;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointLedgerService {

    @Autowired
    private PointsLedgerRepository pointsLedgerRepository;

    @Autowired
    private UserRepository userRepository;

    public List<PointDTO> getUserPointList(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));

        List<PointsLedger> pointlist =
                pointsLedgerRepository.findByUserOrderByCreatedAtDesc(user);



        return pointlist.stream()
                .map(l -> new PointDTO(
                        l.getCreatedAt(),
                        l.getChangeAmount(),
                        l.getReason(),
                        l.getUser().getTotalPoint()
                ))
                .collect(Collectors.toList());
    }

    // 잔액 조회
    public BalanceDTO getCurrentBalance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));

        PointsLedger latest = pointsLedgerRepository.findTopByUserOrderByCreatedAtDesc(user);
        Integer balance = latest != null ? latest.getUser().getTotalPoint() : 0;

        return new BalanceDTO(balance);
    }
}
