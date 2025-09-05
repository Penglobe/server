package com.penglobe.server.repository;

import com.penglobe.server.domain.ranking.WeeklyRankingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyRankingParticipantRepository extends JpaRepository<WeeklyRankingParticipant, Long> {

    List<WeeklyRankingParticipant> findAll();

    void deleteAllInBatch();
}
