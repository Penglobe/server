package com.penglobe.server.repository;

import com.penglobe.server.domain.quiz.QuizQuestions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestions, Long> {
    //랜덤 1개 조회
    @Query(value = "SELECT * FROM quiz WHERE quiz_id >= :minId ORDER BY quiz_id LIMIT 1", nativeQuery = true)
    Optional<QuizQuestions> findRandomQuiz(@Param("minId") Long minId);

    //퀴즈ID 가져옴
    @Query("SELECT q FROM QuizQuestions q WHERE q.quizId = :quizId")
    Optional<QuizQuestions> findByQuizId(@Param("quizId") Long quizId);


    @Query("SELECT MIN(q.quizId) FROM QuizQuestions q")
    Long findMinId();

    @Query("SELECT MAX(q.quizId) FROM QuizQuestions q")
    Long findMaxId();
}