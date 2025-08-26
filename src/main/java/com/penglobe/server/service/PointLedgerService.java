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
    PointsLedgerRepository pointsLedgerRepository;

    @Autowired
    UserRepository userRepository;

    public List<PointDTO> getUserPointList(Long userId, LocalDate from, LocalDate to) {
        //사용자 id 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));

        //날짜 범위가 null이면 이번 달로 설정
        if (from == null || to == null) {
            LocalDate now = LocalDate.now();
            from = now.withMonth(1);
            to = now;
        }

        //리스트
        List<PointsLedger> pointlist = pointsLedgerRepository.findByUserAndEventDateBetweenOrderByEventDateDesc(user, from, to);

        return pointlist.stream().map(l -> new PointDTO(
                l.getEventDate(),
                l.getChangeAmount(),
                l.getReason(), l
                .getBalanceAfter()
                ))
                .collect(Collectors.toList());
    }

    //잔액조회
    public BalanceDTO getCurrentBalance(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));

        PointsLedger latest = pointsLedgerRepository.findTopByUserOrderByEventDateDesc(user);
        Integer balance = latest != null ? latest.getBalanceAfter() : 0;

        return new BalanceDTO(balance);
    }

}
