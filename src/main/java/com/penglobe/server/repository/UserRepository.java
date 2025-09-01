package com.penglobe.server.repository;

import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.RankedUserDTO; // Added import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByKakaoId(Long kakaoId);
    boolean existsByEmail(String email);
    boolean existsByKakaoId(Long kakaoId);

    @Query("""
    select new com.penglobe.server.dto.RankedUserDTO(
        u.userId, u.nickname, 0, CAST(SUM(uc.totalDistanceCo2Kg) + SUM(uc.totalDietCo2Kg) AS BigDecimal), false
    )
    from User u
    join UserCounters uc ON uc.userId = u.userId
    group by u.userId, u.nickname
    order by CAST(SUM(uc.totalDistanceCo2Kg) + SUM(uc.totalDietCo2Kg) AS BigDecimal) DESC
    """)
    List<RankedUserDTO> findAllUsersWithTotalSavingsForRanking();
}

