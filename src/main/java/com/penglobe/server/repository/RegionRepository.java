package com.penglobe.server.repository;

import com.penglobe.server.domain.region.Regions;
import com.penglobe.server.dto.RegionDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RegionRepository extends JpaRepository<Regions, Integer> {
    @Query("SELECT new com.penglobe.server.dto.RegionDTO(r.regionId, r.name, r.totalCo2kg) FROM Regions r ORDER BY r.totalCo2kg DESC")
    List<RegionDTO> getRegionRankings();
}
