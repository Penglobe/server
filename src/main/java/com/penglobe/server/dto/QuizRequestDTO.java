package com.penglobe.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class QuizRequestDTO {
    private Long userId;
    private Long quizId;
    private Boolean answer;

}
