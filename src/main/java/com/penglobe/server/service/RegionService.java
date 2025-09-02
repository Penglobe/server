package com.penglobe.server.service;

import com.penglobe.server.dto.RegionDTO;
import com.penglobe.server.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionService {

    private final RegionRepository regionRepository;

    /**
     * CO2 총 절감량 기준 지역별 랭킹을 조회합니다.
     * <p>
     * 1. Repository를 통해 각 지역의 CO2 절감량 합계를 내림차순으로 정렬하여 조회합니다.
     * 2. 조회된 리스트를 순회하며 순위를 부여합니다.
     * 3. 동점자 발생 시 같은 순위를 부여하고, 다음 순위는 동점자 수만큼 건너뛰어 계산합니다. (예: 1, 2, 2, 4)
     *
     * @return 순위가 포함된 RegionDTO 리스트
     */
    @Transactional(readOnly = true)
    public List<RegionDTO> getRegionRankings() {
        List<RegionDTO> regionRankings = regionRepository.getRegionRankings();
        int rank = 0;
        BigDecimal lastScore = new BigDecimal(-1); // 이전 점수를 저장할 변수

        for (int i = 0; i < regionRankings.size(); i++) {
            RegionDTO current = regionRankings.get(i);
            // 이전 점수와 다를 경우에만 순위를 i+1로 갱신
            if (current.getTotalCo2().compareTo(lastScore) != 0) {
                rank = i + 1;
            }
            current.setRank(rank);
            lastScore = current.getTotalCo2();
        }
        return regionRankings;
    }



}
