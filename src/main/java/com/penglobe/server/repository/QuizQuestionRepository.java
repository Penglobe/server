package com.penglobe.server.repository;

import com.penglobe.server.domain.quiz.QuizQuestions;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestions, Long> {


}
