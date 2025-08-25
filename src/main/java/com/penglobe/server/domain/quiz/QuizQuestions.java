package com.penglobe.server.domain.quiz;


import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name="quiz")
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //문제
    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    //정답 O=1, X=0
    @Column(name = "is_answer_true", nullable = false)
    private Boolean isAnswerTrue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
}
