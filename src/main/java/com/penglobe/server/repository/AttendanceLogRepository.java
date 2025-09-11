package com.penglobe.server.repository;

import com.penglobe.server.domain.attendance.AttendanceLog;
import com.penglobe.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceLogRepository extends JpaRepository<AttendanceLog, Long> {

    Optional<AttendanceLog> findByUser_UserIdAndDate(Long userId, LocalDate date);

    boolean existsByUserAndDate(User user, LocalDate date);

    void deleteByUser(User user);

    @Query("SELECT a.date FROM AttendanceLog a WHERE a.user.userId = :userId")
    List<LocalDate> findDatesByUserId(@Param("userId") Long userId);
}

