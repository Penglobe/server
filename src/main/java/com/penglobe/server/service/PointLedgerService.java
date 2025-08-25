package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.PointDTO;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointLedgerService {

    @Autowired
    PointsLedgerRepository pointsLedgerRepository;

    @Autowired
    UserRepository userRepository;

    public List<PointDTO> getUserPointList(Long userId) {
        //사용자 id 찾기
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("존재하지 않은 사용자입니다."));

        //리스트
        List<PointsLedger> pointlist = pointsLedgerRepository.findByUserOrderByEventDateDesc(user);

        return pointlist.stream().map(l -> new PointDTO(
                l.getEventDate(), l.getChangeAmount(), l.getReason(), l.getBalanceAfter())).collect(Collectors.toList());
    }

}
