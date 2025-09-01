package com.penglobe.server.repository;

import com.penglobe.server.domain.ranking.WeeklyRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WeeklyRankingRepository extends JpaRepository<WeeklyRanking, Integer> {
}
