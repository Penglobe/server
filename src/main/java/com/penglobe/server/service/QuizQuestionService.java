package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.quiz.QuizQuestions;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.QuizRequestDTO;
import com.penglobe.server.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {
    final QuizQuestionRepository quizQuestionRepository;
    final PointsLedgerRepository pointsLedgerRepository;
    private final UserRepository userRepository;

    // 오늘의 퀴즈 가져오기 (랜덤)
    public QuizQuestions getDailyQuiz() {

        Long minId = quizQuestionRepository.findMinId();
        Long maxId = quizQuestionRepository.findMaxId();

        long randomQuizId = new Random().nextLong(minId, maxId + 1);

        return quizQuestionRepository.findRandomQuiz(randomQuizId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));
    }

    // 답 제출 + 포인트 적립
    public int submitAnswer(Boolean userAnswer, QuizRequestDTO request) {

        //중복 제출 확인 및 메시지
        long submitted = pointsLedgerRepository.countTodayQuizSubmit(request.getUserId());
        if(submitted >= 1) {
            throw new IllegalStateException("오늘 퀴즈는 이미 제출했습니다. \n 포인트는 지급되지 않습니다.");
        }

        long quizId = request.getQuizId();

        Boolean correctAnswer = quizQuestionRepository.findByQuizId(quizId)
                .map(QuizQuestions::getIsAnswerTrue)
                .orElseThrow(() -> new RuntimeException("퀴즈없음"));

        // 사용자 조회
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("userId" + user.getUserId());

        // 포인트 지급
        int points = (correctAnswer != null && correctAnswer.equals(userAnswer)) ? 10 : 1;
        int updatedBalance = user.getTotalPoint() + points;
        user.setTotalPoint(updatedBalance);

        // PointsLedger 생성 후 저장
        PointsLedger ledger = PointsLedger.builder()
                .user(user)
                .changeAmount(points)
                .reason(LedgerReason.QUIZ)
                .balanceAfter(updatedBalance)
                .build();

        pointsLedgerRepository.save(ledger);
        userRepository.save(user);

        return points;
    }
}