package com.penglobe.server.repository;

import com.penglobe.server.domain.attendance.AttendanceLog;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    Optional<AttendanceLog> findByUser_UserIdAndDate(Long userId, LocalDate date);

    boolean existsByUserAndDate(User user, LocalDate date);
}
