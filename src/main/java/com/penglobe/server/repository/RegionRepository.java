package com.penglobe.server.repository;

import com.penglobe.server.domain.region.Regions;
import com.penglobe.server.dto.RegionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Regions, Integer> {
    @Query("""
    select new com.penglobe.server.dto.RegionDTO(
    r.id, r.name, CAST(CAST(SUM(uc.totalDistanceCo2Kg) + SUM(uc.totalDietCo2Kg) AS java.math.BigDecimal) AS java.math.BigDecimal)
    )
    from Regions r
    join User u ON u.homeRegionId = r.id
    join UserCounters uc ON uc.userId = u.id
    group by r.id, r.name
    order by CAST(CAST(SUM(uc.totalDistanceCo2Kg) + SUM(uc.totalDietCo2Kg) AS java.math.BigDecimal) AS java.math.BigDecimal) DESC
""")
    List<RegionDTO> getRegionRankings();
}
