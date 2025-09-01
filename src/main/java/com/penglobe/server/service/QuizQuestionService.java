package com.penglobe.server.service;

import com.penglobe.server.domain.quiz.QuizQuestions;
import com.penglobe.server.repository.QuizQuestionRepository;
import com.penglobe.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QuizQuestionService {
    final QuizQuestionRepository quizQuestionRepository;
    final UserRepository userRepository;




}
