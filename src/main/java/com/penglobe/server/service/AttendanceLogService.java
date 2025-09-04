package com.penglobe.server.service;

import com.penglobe.server.domain.attendance.AttendanceLog;
import com.penglobe.server.domain.attendance.AttendanceType;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.repository.AttendanceLogRepository;
import com.penglobe.server.repository.UserCountersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AttendanceLogService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final UserCountersRepository userCountersRepository;

    @Transactional
    public AttendanceLog markAttendance(User user, AttendanceType type) {
        LocalDate today = LocalDate.now();

        // 하루 1회만 인정
        if (attendanceLogRepository.existsByUserAndDate(user, today)) {
            throw new IllegalStateException("오늘 이미 출석이 등록되었습니다.");
        }

        // 출석 로그 저장
        AttendanceLog log = AttendanceLog.builder()
                .user(user)
                .date(today)
                .attendanceType(type)
                .shownAt(today)
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
            monthDays = 1; // 새 달이면 리셋
        }

        // 벌크 업데이트 실행
        userCountersRepository.updateAttendanceStats(user, today, streakDays, monthDays);

        return log;
    }

}
