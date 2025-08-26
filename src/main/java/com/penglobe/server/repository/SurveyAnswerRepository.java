package com.penglobe.server.repository;

import com.penglobe.server.domain.survey.SurveyAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

//항목별 사용자의 선택 저장, top3 계산용
public interface SurveyAnswerRepository extends JpaRepository<SurveyAnswer, Long> {}