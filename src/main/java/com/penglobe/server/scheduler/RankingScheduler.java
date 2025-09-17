package com.penglobe.server.scheduler;

import com.penglobe.server.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;
    private boolean initialized = false;

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        System.out.println("Performing initial data setup on startup...");
        rankingService.selectWeeklyParticipants(); // Select participants first
        rankingService.updateLiveWeeklyRanking();
        rankingService.updateAllRanking();
        rankingService.updateRegionRankings();
        System.out.println("Initial data setup complete.");
        initialized = true; // Set to true after initialization
    }

    /**
     * 매주 월요일 0시 5분에 주간 랭킹 참여자를 선정합니다.
     */
    @Scheduled(cron = "0 5 0 * * MON")
    public void scheduleWeeklyParticipantSelection() {
        System.out.println("Selecting weekly ranking participants...");
        rankingService.selectWeeklyParticipants();
        System.out.println("Weekly ranking participant selection complete.");
    }

    /**
     * 매주 월요일 0시 1분에 주간 랭킹을 마감합니다.
     */
    @Scheduled(cron = "0 1 0 * * MON")
    public void scheduleWeeklyFinalization() {
        if (!initialized) return; // Add this line
        rankingService.finalizeWeeklyRanking();
    }

    /**
     * 매일 자정에 지역별 랭킹 점수를 갱신합니다.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleRegionRankingUpdate() {
        rankingService.updateRegionRankings();
    }
}
