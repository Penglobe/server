package com.penglobe.server.service;

import com.penglobe.server.dto.RegionDTO;
import com.penglobe.server.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public List<RegionDTO> getRegionRankings() {
        // 1. 레포지토리에서 절감량 기준으로 정렬된 지역 목록을 조회합니다.
        List<RegionDTO> regionRankings = regionRepository.getRegionRankings();

        // 2. 조회된 데이터를 바탕으로 랭킹을 계산하고 DTO에 순위를 설정합니다.
        int rank = 1;
        for (int i = 0; i < regionRankings.size(); i++) {
            // 동점자 처리: 이전 지역과 총 절감량이 다를 경우에만 순위를 현재 인덱스 + 1로 업데이트합니다.
            if (i > 0 && regionRankings.get(i).getTotalSaving().compareTo(regionRankings.get(i - 1).getTotalSaving()) != 0) {
                rank = i + 1;
            }
            regionRankings.get(i).setRank(rank);
        }

        return regionRankings;
    }



}
