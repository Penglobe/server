// src/main/java/com/penglobe/server/service/UserService.java
package com.penglobe.server.service;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.UserProfileDTO;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserCountersRepository userCountersRepository;

    public UserProfileDTO getMe(Long userId) {
        if (userId == null) throw new IllegalArgumentException("userId is null");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자: " + userId));

        // 없으면 0으로 채워진 카운터 생성 느낌으로 빌더(조회 전용)
        UserCounters c = userCountersRepository.findById(userId)
                .orElse(UserCounters.builder().user(user).userId(userId).build());

        BigDecimal dist = nz(c.getTotalDistanceCo2Kg());
        BigDecimal diet = nz(c.getTotalDietCo2Kg());
        BigDecimal surv = nz(c.getTotalSurveyCo2Kg());
        BigDecimal total = dist.add(diet).add(surv);

        return UserProfileDTO.builder()
                .id(user.getUserId())
                .nickname(user.getNickname())
                .regionId(user.getRegionId())
                .totalDistanceCo2Kg(dist)
                .totalDietCo2Kg(diet)
                .totalSurveyCo2Kg(surv)
                .totalScore(total)
                .build();
    }

    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }
}
