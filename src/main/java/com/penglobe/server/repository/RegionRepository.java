package com.penglobe.server.repository;

import com.penglobe.server.domain.region.Regions;
import com.penglobe.server.dto.RegionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Regions, Integer> {
    @Query("""
    select new com.penglobe.server.dto.RegionDTO(
    r.regionId, r.name, CAST(SUM(uc.totalDistanceCo2Kg) + SUM(uc.totalDietCo2Kg) AS BigDecimal)
    )
    from Regions r
    join User u ON u.regionId = r.regionId
    join UserCounters uc ON uc.userId = u.userId
    group by r.regionId, r.name
    order by CAST(SUM(uc.totalDistanceCo2Kg) + SUM(uc.totalDietCo2Kg) AS BigDecimal) DESC
""")
    List<RegionDTO> getRegionRankings();
}
