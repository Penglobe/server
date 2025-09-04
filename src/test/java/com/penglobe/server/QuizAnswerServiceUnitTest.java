package com.penglobe.server;

import com.penglobe.server.domain.quiz.QuizQuestions;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.PointsLedgerRepository;
import com.penglobe.server.repository.QuizQuestionRepository;
import com.penglobe.server.repository.UserCountersRepository;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.service.QuizQuestionService;
import org.apache.catalina.startup.UserConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class QuizAnswerServiceUnitTest {

    private QuizQuestionRepository quizRepository;
    private PointsLedgerRepository pointsLedgerRepository;
    private UserRepository userRepository;
    private QuizQuestionService quizService;
    private UserCountersRepository userCountersRepository;

    @BeforeEach
    void setUp() {
        quizRepository = mock(QuizQuestionRepository.class);
        pointsLedgerRepository = mock(PointsLedgerRepository.class);
        userRepository = mock(UserRepository.class);
        userCountersRepository = mock(UserCountersRepository.class);

        quizService = new QuizQuestionService(
                quizRepository,
                pointsLedgerRepository,
                userRepository

        );
    }

    @Test
    void testSubmitAnswer_WithSysOut() {
        Long userId = 1L;
        Long quizId = 1L;
        Boolean userAnswer = true;

        // Mock 유저
        User mockUser = new User();
        mockUser.setTotalPoint(10);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        System.out.println("===정답===");
        System.out.println("현재 보유 포인트: " + mockUser.getTotalPoint());

        // Mock 퀴즈
        QuizQuestions quiz = new QuizQuestions();
        quiz.setQuizId(3L);
        quiz.setQuestion("서울은 대한민국의 수도이다");
        quiz.setIsAnswerTrue(false);
        when(quizRepository.findMaxId()).thenReturn(10L);
        when(quizRepository.findRandomQuiz(anyLong())).thenReturn(Optional.of(quiz));

        // Service 호출
        //int points = quizService.submitAnswer(userAnswer);

        // 콘솔 출력
        System.out.println("사용자 정답유무: " + quiz.getIsAnswerTrue());
        //System.out.println("적립 포인트: " + points);
        System.out.println("사용자 총 포인트: " + mockUser.getTotalPoint());

    }
}
