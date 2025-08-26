package com.penglobe.server.repository;

import com.penglobe.server.domain.region.Regions;
import com.penglobe.server.dto.RegionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Regions, Integer> {
    @Query("""
    select new com.penglobe.server.dto.RegionDTO(
    r.id, r.name, SUM(uc.totalDistanceCo2Kg + uc.totalDietCo2Kg)
    )
    from Regions r
    join User u ON u.homeRegionId = r.id
    join UserCounters uc ON uc.userId = u.id
    group by r.id, r.name
    order by SUM(uc.totalDistanceCo2Kg + uc.totalDietCo2Kg) DESC
""")
    List<RegionDTO> getRegionRankings();
}
