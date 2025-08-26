package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.SurveyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyResponseRepository extends JpaRepository<SurveyResponse, Long> {
    //사용자의 설문 응답 저장
    //총 co2, 제출일자, 설문 저장
    SurveyResponse findTopByUserIdOrderBySurveyDateDesc(Long userId);
}