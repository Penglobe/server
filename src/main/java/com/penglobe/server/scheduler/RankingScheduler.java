package com.penglobe.server.scheduler;

import com.penglobe.server.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private final RankingService rankingService;

    /**
     * 5분마다 실시간 랭킹을 갱신합니다.
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void scheduleLiveRankingUpdate() {
        rankingService.updateLiveWeeklyRanking();
    }

    /**
     * 매주 월요일 0시 5분에 주간 랭킹을 마감합니다.
     */
    @Scheduled(cron = "0 5 0 * * MON")
    public void scheduleWeeklyFinalization() {
        rankingService.finalizeWeeklyRanking();
    }

    /**
     * 매일 새벽 2시에 전체 랭킹을 갱신합니다.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduleAllRankingUpdate() {
        rankingService.updateAllRanking();
    }

    /**
     * 매일 새벽 3시에 지역별 랭킹 점수를 갱신합니다.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void scheduleRegionRankingUpdate() {
        rankingService.updateRegionRankings();
    }
}
