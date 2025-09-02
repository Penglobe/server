package com.penglobe.server.service;

import com.penglobe.server.domain.ledger.LedgerReason;
import com.penglobe.server.domain.ledger.PointsLedger;
import com.penglobe.server.domain.quiz.QuizQuestions;
import com.penglobe.server.domain.user.User;
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


    //랜덤으로 퀴즈 1개 선택

    // 오늘의 퀴즈 가져오기 (랜덤)
    public QuizQuestions getDailyQuiz() {
        Long minId = quizQuestionRepository.findMinId();
        Long maxId = quizQuestionRepository.findMaxId();

        long randomId = new Random().nextLong(minId, maxId + 1);

        return quizQuestionRepository.findRandomQuiz(randomId)
                .orElseThrow(() -> new RuntimeException("퀴즈를 찾을 수 없습니다."));
    }

    // 답 제출 + 포인트 적립
    @Transactional
    public int submitAnswer(Long userId, Boolean userAnswer) {
        QuizQuestions quiz = getDailyQuiz();

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        int points = quiz.getIsAnswerTrue().equals(userAnswer) ? 1 : 10;

        user.setTotalPoint(user.getTotalPoint() + points);

        // PointsLedger 생성 후 저장
        PointsLedger ledger = new PointsLedger(null, user, points, LedgerReason.QUIZ);
        pointsLedgerRepository.save(ledger);
        userRepository.save(user);

        return points;
    }




}