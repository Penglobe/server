package com.penglobe.server;

import com.penglobe.server.domain.quiz.QuizQuestions;
import com.penglobe.server.service.QuizQuestionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class QuizServiceTest {
    @Autowired
    private QuizQuestionService quizService;

    @Test
    void testRandomQuizFromDatabase() {
        for (int i = 0; i < 1; i++) {
            QuizQuestions quiz = quizService.getDailyQuiz();
            System.out.println("===오늘 퀴즈===");
            System.out.println("랜덤 퀴즈 ID: " + quiz.getQuizId());
            System.out.println("퀴즈 질문: " + quiz.getQuestion());
            System.out.println("정답 여부: " + quiz.getIsAnswerTrue());
        }
    }
}
