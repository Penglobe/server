package com.penglobe.server.controller;

import com.penglobe.server.domain.quiz.QuizQuestions;
import com.penglobe.server.service.QuizQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/quiz")
@RequiredArgsConstructor
@Tag(name="Quiz", description = "퀴즈 출력/정답")
public class QuizController {
    private final QuizQuestionService quizQuestionService;

    @GetMapping("/today")
    @Operation(summary = "오늘의 퀴즈 조회", description = "날짜 기반 랜덤으로 오늘의 퀴즈 1개를 조회합니다.")
    public QuizQuestions getTodayQuestions() {
        return quizQuestionService.getDailyQuiz();
    }

    @PostMapping("/submit")
    @Operation(summary = "퀴즈 답 제출 및 포인트 적립", description = "사용자가 퀴즈 답을 제출하면 포인트가 적립됩니다. 정답 10포인트, 오답 1포인트")
    public Map<String, Object> submitAnswer(@Parameter(description = "사용자 ID", required = true)
                                                @RequestParam("userId") Long userId,
                                            @Parameter(description = "사용자 답변 (O=true, X=false)", required = true)
                                                @RequestParam("answer") Boolean answer) {
        int points = quizQuestionService.submitAnswer(userId, answer);
        return Map.of("points", points);
    }
}
