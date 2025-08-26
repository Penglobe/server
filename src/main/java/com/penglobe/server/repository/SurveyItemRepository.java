package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.SurveyItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyItemRepository extends JpaRepository<SurveyItem, Long> {}