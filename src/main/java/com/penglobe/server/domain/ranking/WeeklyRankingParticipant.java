package com.penglobe.server.domain.ranking;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weekly_ranking_participants")
public class WeeklyRankingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDate weekStartDate;

    @Builder
    public WeeklyRankingParticipant(Long userId, LocalDate weekStartDate) {
        this.userId = userId;
        this.weekStartDate = weekStartDate;
    }
}
