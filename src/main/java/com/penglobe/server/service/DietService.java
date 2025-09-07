package com.penglobe.server.service;

import com.penglobe.server.domain.diet.DietRecord;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.DietDTO;
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
@Transactional(readOnly = true)  // 읽기 전용
public class DietService {

    private final DietRecordRepository dietRecordRepository;  // 식단
    private final UserRepository userRepository;  // 사용자
    private final UserCountersRepository userCountersRepository;  // 누적

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 하루 최대 3개까지 가능
    private static final int MAX_RECORDS_PER_DAY = 3;

    private static LocalDateTime startOfDayKST(LocalDate date) {
        return date.atStartOfDay();  // 오늘 00:00
    }
    private static LocalDateTime endOfDayKST(LocalDate date) {
        return date.plusDays(1).atStartOfDay(); // 다음날 00:00 (exclusive) → [오늘 00:00, 내일 00:00) = 오늘 하루 전 범위
    }
    private static DietDTO toDTO(DietRecord d) {
        return DietDTO.builder()
                .dietId(d.getDietId())
                .userId(d.getUser().getUserId())
                .co2Kg(d.getCo2Kg())
                .build();
    }

    // 생성 (하루 3회 제한 + 누적 업데이트)
    @Transactional
    public DietDTO createDietRecordWithDailyLimit(Long userId, BigDecimal co2Kg) {

        // co2 : null 또는 음수 불가
        if (co2Kg == null || co2Kg.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("co2Kg는 0 이상이어야 합니다.");
        }

        // 오늘 날짜 범위
        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = startOfDayKST(today);
        LocalDateTime end = endOfDayKST(today);

        // 오늘 기록 개수 확인 ( 하루 3개 제한 체크 )
        long todayCount = dietRecordRepository
                .countByUser_UserIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(userId, start, end);
        if (todayCount >= MAX_RECORDS_PER_DAY) {
            throw new IllegalStateException("식단 기록은 하루 최대 " + MAX_RECORDS_PER_DAY + "회까지 가능합니다.");
        }

        // 사용자 존재 여부 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. id=" + userId));

        // 소수 첫째 자리까지 반올림해서 저장
        BigDecimal fixed = co2Kg.setScale(1, RoundingMode.HALF_UP);

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

        BigDecimal current = counters.getTotalDietCo2Kg();
        if (current == null) current = BigDecimal.ZERO;

        counters.setTotalDietCo2Kg(
                current.add(fixed).setScale(1, RoundingMode.HALF_UP)
        );

        userCountersRepository.save(counters);

        return toDTO(saved);
    }

    // 오늘 합계
    public BigDecimal getTodaySum(Long userId) {
        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = startOfDayKST(today);
        LocalDateTime end = endOfDayKST(today);
        return dietRecordRepository
                .sumCo2KgByUserAndPeriod(userId, start, end)
                .setScale(1, RoundingMode.HALF_UP);
    }

    // 기간 합계
    public BigDecimal getSumByPeriod(Long userId, LocalDateTime startInclusive, LocalDateTime endExclusive) {
        if (startInclusive == null || endExclusive == null || !startInclusive.isBefore(endExclusive)) {
            throw new IllegalArgumentException("start < end 조건을 만족하는 기간을 입력하세요.");
        }
        return dietRecordRepository
                .sumCo2KgByUserAndPeriod(userId, startInclusive, endExclusive)
                .setScale(1, RoundingMode.HALF_UP);
    }

    // 특정 날짜 합계
    public BigDecimal getDailySumByDate(Long userId, LocalDate dateKST) {
        LocalDateTime start = startOfDayKST(dateKST);
        LocalDateTime end   = endOfDayKST(dateKST);

        return dietRecordRepository
                .sumCo2KgByUserAndPeriod(userId, start, end)
                .setScale(1, RoundingMode.HALF_UP);
    }

}
