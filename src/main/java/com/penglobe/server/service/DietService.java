package com.penglobe.server.service;

import com.penglobe.server.domain.diet.DietRecord;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.diet.DietDTO;
import com.penglobe.server.repository.DietRecordRepository;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DietService {

    private final DietRecordRepository dietRecordRepository;
    private final UserRepository userRepository;
    private final UserCountersRepository userCountersRepository;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int MAX_RECORDS_PER_DAY = 3;

    // 소수 자리/반올림 규칙 통일
    private static final int SCALE = 1;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    private static LocalDateTime startOfDayKST(LocalDate date) {
        return date.atStartOfDay();
    }
    private static LocalDateTime endOfDayKST(LocalDate date) {
        return date.plusDays(1).atStartOfDay();
    }
    private static BigDecimal fix(BigDecimal v) {
        return v.setScale(SCALE, RM);
    }
    private static BigDecimal nz(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static DietDTO toDTO(DietRecord d, BigDecimal totalDietCo2Kg, int todayCountAfter) {
        return DietDTO.builder()
                .dietId(d.getDietId())
                .userId(d.getUser().getUserId())
                .co2Kg(d.getCo2Kg())
                .totalDietCo2Kg(totalDietCo2Kg)
                .todayCountAfter(todayCountAfter)   // ← 필드명 일치
                .build();
    }

    @Transactional
    public DietDTO createDietRecordWithDailyLimit(Long userId, BigDecimal co2Kg) {

        if (co2Kg == null || co2Kg.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("co2Kg는 0 이상이어야 합니다.");
        }

        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = startOfDayKST(today);
        LocalDateTime end = endOfDayKST(today);

        long todayCount = dietRecordRepository
                .countByUser_UserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, start, end);

        if (todayCount >= MAX_RECORDS_PER_DAY) {
            throw new IllegalStateException("식단 기록은 하루 최대 " + MAX_RECORDS_PER_DAY + "회까지 가능합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));

        BigDecimal fixed = fix(co2Kg); // 소수 1자리 반올림

        // 기록 저장
        DietRecord saved = dietRecordRepository.save(
                DietRecord.builder()
                        .user(user)
                        .co2Kg(fixed)
                        .build()
        );

        // 누적 업데이트 (없으면 생성)
        UserCounters counters = userCountersRepository.findById(userId)
                .orElse(UserCounters.builder().user(user).userId(userId).build());

        BigDecimal current = nz(counters.getTotalDietCo2Kg());
        counters.setTotalDietCo2Kg( fix(current.add(fixed)) );
        userCountersRepository.save(counters);

        int todayCountAfter = Math.toIntExact(todayCount + 1);
        return toDTO(saved, counters.getTotalDietCo2Kg(), todayCountAfter);

    }

    public BigDecimal getTodaySum(Long userId) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = startOfDayKST(today);
        LocalDateTime end = endOfDayKST(today);
        return fix(nz(dietRecordRepository.sumCo2KgByUserAndPeriod(userId, start, end)));
    }

    public BigDecimal getSumByPeriod(Long userId, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        if (startInclusive == null || endExclusive == null || !startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("start < end 조건을 만족하는 기간을 입력하세요.");
        }
        return fix(nz(dietRecordRepository.sumCo2KgByUserAndPeriod(userId, startInclusive, endExclusive)));
    }

    public BigDecimal getDailySumByDate(Long userId, LocalDate dateKST) {
        LocalDateTime start = startOfDayKST(dateKST);
        LocalDateTime end   = endOfDayKST(dateKST);
        return fix(nz(dietRecordRepository.sumCo2KgByUserAndPeriod(userId, start, end)));
    }
}

