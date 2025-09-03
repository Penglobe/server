package com.penglobe.server.repository;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.domain.user.UserCounters;
import org.springframework.data.jpa.repository.JpaRepository;
import com.penglobe.server.dto.UserTotalScoreDTO;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserCountersRepository extends JpaRepository<UserCounters, Long> {
    @Query("SELECT uc.user FROM UserCounters uc WHERE uc.lastAttendanceDate >= :sevenDaysAgo")
    List<User> findUsersActiveSince(@Param("sevenDaysAgo") LocalDate sevenDaysAgo);

    @Query("SELECT new com.penglobe.server.dto.UserTotalScoreDTO(uc.userId, uc.user.nickname, " +
            "CAST((uc.totalDistanceCo2Kg + uc.totalDietCo2Kg + uc.totalSurveyCo2Kg) AS bigdecimal)) " +
            "FROM UserCounters uc " +
            "ORDER BY (uc.totalDistanceCo2Kg + uc.totalDietCo2Kg + uc.totalSurveyCo2Kg) DESC")
    List<UserTotalScoreDTO> findUserTotalScores();

    @Query("SELECT SUM(COALESCE(uc.totalDistanceCo2Kg, 0) + COALESCE(uc.totalDietCo2Kg, 0) + COALESCE(uc.totalSurveyCo2Kg, 0)) " +
           "FROM UserCounters uc WHERE uc.user.regionId = :regionId")
    Optional<BigDecimal> sumTotalCo2ByRegionId(@Param("regionId") Integer regionId);
    Optional<UserCounters> findByUserId(Long userId);


}
