package com.penglobe.server.repository;

import com.penglobe.server.domain.ranking.AllRanking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AllRankingRepository extends JpaRepository<AllRanking, Long> {

    List<AllRanking> findTop10ByOrderByRankingAsc();

    Optional<AllRanking> findByUserId(Long userId);
}
