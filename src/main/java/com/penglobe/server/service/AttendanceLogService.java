package com.penglobe.server.service;

import com.penglobe.server.domain.attendance.AttendanceLog;
import com.penglobe.server.domain.attendance.AttendanceType;
import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.repository.AttendanceLogRepository;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final UserCountersRepository userCountersRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final UserRepository userRepository;

    /**
     * 출석 등록 시도
     * @return true = 오늘 첫 출석, false = 이미 출석한 경우
     */
    @Transactional
    public boolean markAttendance(User user, AttendanceType type) {
        LocalDate today = LocalDate.now();

        // 하루 1회만 인정
        if (attendanceLogRepository.existsByUserAndDate(user, today)) {
            return false; // 이미 출석 기록 있음 → 무시
        }

        // 새로운 출석 로그 저장
        AttendanceLog log = AttendanceLog.builder()
                .user(user)
                .date(today)
                .attendanceType(type)
                .build();
        attendanceLogRepository.save(log);

        // UserCounters 조회
        UserCounters counters = userCountersRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("UserCounters 없음"));

        // streak 계산
        int streakDays = 1;
        if (counters.getLastAttendanceDate() != null &&
                counters.getLastAttendanceDate().plusDays(1).equals(today)) {
            streakDays = counters.getAttendanceStreakDays() + 1;
        }

        // monthDays 계산
        int monthDays;
        if (counters.getLastAttendanceDate() != null &&
                counters.getLastAttendanceDate().getMonth().equals(today.getMonth()) &&
                counters.getLastAttendanceDate().getYear() == today.getYear()) {
            monthDays = counters.getAttendanceMonthDays() + 1;
        } else {
            monthDays = 1; // 새 달이면 초기화
        }

        // 벌크 업데이트 실행
        userCountersRepository.updateAttendanceStats(user, today, streakDays, monthDays);

        return true; // 오늘 첫 출석 인정
    }

    /**
     * 홈 진입 시 모달 노출 여부
     * - 오늘 출석 로그가 있고 shownAt이 비어 있으면 true
     */
    @Transactional(readOnly = true)
    public boolean shouldShowPopup(Long userId) {
        return attendanceLogRepository
                .findByUser_UserIdAndDate(userId, LocalDate.now())
                .filter(a -> a.getShownAt() == null)
                .isPresent();
    }

    /**
     * 출석 보상 지급(하루 1회, 멱등)
     * - points_ledger에 ATTENDANCE 적립
     * - attendance_logs.shown_at = today
     * @return 지급된 포인트(이미 지급된 경우 0 반환)
     */
    @Transactional
    public int claimAttendanceReward(Long userId) {
        LocalDate today = LocalDate.now();

        AttendanceLog log = attendanceLogRepository
                .findByUser_UserIdAndDate(userId, today)
                .orElseThrow(() -> new IllegalStateException("오늘 출석 로그가 없습니다."));

        if (log.getShownAt() != null) {
            throw new IllegalStateException("이미 오늘 출석 보상을 처리했습니다.");
        }

        long already = pointsLedgerRepository.countTodayAttendance(userId);
        if (already > 0) {
            // 이미 지급된 상태: 모달만 닫힘 처리
            log.setShownAt(today);
            return 0;
        }

        // 가중치 랜덤 (10/20/30/40/50, 유저+날짜 시드 → 같은 날 동일)
        int reward = rollWeightedReward(userId, today);

        User user = userRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("User Not Fount"));

        int updatedBalance = user.getTotalPoint() + reward;
        user.setTotalPoint(updatedBalance);
        userRepository.save(user);

        PointsLedger ledger = PointsLedger.builder()
                .user(userRepository.getReferenceById(userId))
                .changeAmount(reward)
                .reason(LedgerReason.ATTENDANCE)
                .balanceAfter(updatedBalance)
                .build();
        pointsLedgerRepository.save(ledger);

        log.setShownAt(today);
        return reward;
    }

    @Transactional(readOnly = true)
    public int previewAttendanceReward(Long userId) {
        LocalDate today = LocalDate.now();
        // 오늘 출석 로그가 있고 아직 모달 안봤을 때만 미리보기 허용(선택)
        attendanceLogRepository.findByUser_UserIdAndDate(userId, today)
                .filter(a -> a.getShownAt() == null)
                .orElseThrow(() -> new IllegalStateException("오늘 출석 대상이 아닙니다."));

        return rollWeightedReward(userId, today); // 지급 X, 계산만
    }

    /** 10/20/30/40/50에 서로 다른 확률 부여 (합 100) */
    private int rollWeightedReward(Long userId, LocalDate date) {
        long seed = Objects.hash(userId, date);
        Random r = new Random(seed);

        int[] rewards = {10, 20, 30, 40, 50, 100};
        int[] weights = {39, 30, 15, 10, 5, 1}; // 40%, 30%, 15%, 10%, 5%

        int rnd = r.nextInt(100); // 0~99
        int cumulative = 0;
        for (int i = 0; i < rewards.length; i++) {
            cumulative += weights[i];
            if (rnd < cumulative) return rewards[i];
        }
        return rewards[rewards.length - 1];
    }
}
