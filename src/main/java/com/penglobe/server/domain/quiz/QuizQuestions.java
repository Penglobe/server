package com.penglobe.server.domain.quiz;


import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="quiz")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "quiz_id", nullable = false, updatable = false)
    private Long quizId;

    @Column(name = "question", nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "is_answer_true", nullable = false)
    private Boolean  isAnswerTrue;

}