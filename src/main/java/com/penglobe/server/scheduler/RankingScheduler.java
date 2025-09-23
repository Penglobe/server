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
        System.out.println("서버 기동 완료. 초기화 작업 실행...");

        // 🚫 주간 참여자 선정, 주간 랭킹 초기화는 하지 않음
        // rankingService.selectWeeklyParticipants();
        // rankingService.updateLiveWeeklyRanking();

        // ✅ 전체/지역 랭킹은 재시작해도 갱신해주는 게 안전
        rankingService.updateAllRanking();
        rankingService.updateRegionRankings();

        initialized = true;
        System.out.println("랭킹 갱신 완료");
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
