package com.penglobe.server.repository;

import com.penglobe.server.domain.mission.MissionDefinition;
import com.penglobe.server.domain.mission.MissionMetric;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionDefinitionRepository extends JpaRepository<MissionDefinition, Long> {
    MissionDefinition findByMetric(MissionMetric metric);
}
