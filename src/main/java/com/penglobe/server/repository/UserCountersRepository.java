package com.penglobe.server.repository;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import com.penglobe.server.dto.MyPageDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserCountersRepository extends JpaRepository<UserCounters, Long> {

    @Query("SELECT uc.user FROM UserCounters uc WHERE uc.lastAttendanceDate >= :sevenDaysAgo")
    List<User> findUsersActiveSince(@Param("sevenDaysAgo") LocalDate sevenDaysAgo);

    @Query("SELECT new com.penglobe.server.dto.MyPageDTO(uc.userId, uc.user.nickname, " +
            "CAST((uc.totalDistanceCo2Kg + uc.totalDietCo2Kg + uc.totalSurveyCo2Kg) AS java.math.BigDecimal)) " +
            "FROM UserCounters uc " +
            "ORDER BY (uc.totalDistanceCo2Kg + uc.totalDietCo2Kg + uc.totalSurveyCo2Kg) DESC")
    List<MyPageDTO> findUserTotalScores();

    @Query("SELECT SUM(COALESCE(uc.totalDistanceCo2Kg, 0) + COALESCE(uc.totalDietCo2Kg, 0) + COALESCE(uc.totalSurveyCo2Kg, 0)) " +
            "FROM UserCounters uc WHERE uc.user.regionId = :regionId")
    Optional<BigDecimal> sumTotalCo2ByRegionId(@Param("regionId") Integer regionId);

    Optional<UserCounters> findByUserId(Long userId);

    Optional<UserCounters> findByUser(User user);

    // ================== 🔽 최적화된 벌크 업데이트 ================== //

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserCounters uc " +
            "SET uc.totalDistanceCo2Kg = uc.totalDistanceCo2Kg + :co2Kg " +
            "WHERE uc.user = :user")
    int addDistanceCo2(@Param("user") User user, @Param("co2Kg") BigDecimal co2Kg);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserCounters uc " +
            "SET uc.totalDietCo2Kg = uc.totalDietCo2Kg + :co2Kg " +
            "WHERE uc.user = :user")
    int addDietCo2(@Param("user") User user, @Param("co2Kg") BigDecimal co2Kg);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserCounters uc " +
            "SET uc.totalSurveyCo2Kg = uc.totalSurveyCo2Kg + :co2Kg " +
            "WHERE uc.user = :user")
    int addSurveyCo2(@Param("user") User user, @Param("co2Kg") BigDecimal co2Kg);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserCounters uc " +
            "SET uc.attendanceTotalDays = uc.attendanceTotalDays + 1, " +
            "    uc.attendanceMonthDays = :monthDays, " +
            "    uc.attendanceStreakDays = :streakDays, " +
            "    uc.longestAttendanceStreak = GREATEST(uc.longestAttendanceStreak, :streakDays), " +
            "    uc.lastAttendanceDate = :today " +
            "WHERE uc.user = :user")
    int updateAttendanceStats(@Param("user") User user,
                              @Param("today") LocalDate today,
                              @Param("streakDays") int streakDays,
                              @Param("monthDays") int monthDays);


}
