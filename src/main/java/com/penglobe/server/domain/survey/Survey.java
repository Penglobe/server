package com.penglobe.server.domain.survey;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "survey")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "survey_date", nullable = false)
    private LocalDate surveyDate;

    @Column(name = "q1_answer", nullable = false, length = 200)
    private String q1Answer;

    @Column(name = "q2_answer", nullable = false, length = 200)
    private String q2Answer;

    @Column(name = "q3_answer", nullable = false, length = 200)
    private String q3Answer;

    @Column(name = "q4_answer", nullable = false, length = 200)
    private String q4Answer;

    @Column(name = "q5_answer", nullable = false, length = 200)
    private String q5Answer;
}
