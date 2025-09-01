package com.penglobe.server.repository;

import com.penglobe.server.domain.ranking.WeeklyRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeeklyRankingRepository extends JpaRepository<WeeklyRanking, Integer> {

    List<WeeklyRanking> findTop10ByOrderByRankingAsc();

    Optional<WeeklyRanking> findByUserId(Long userId);
}
