package com.penglobe.server.domain.survey;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="survey_response")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
//최종 탄소배출량
public class SurveyResponse extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long surveyResponseId;

    @Column(nullable = false)
    private Long userId;

    //최종 탄소배출량
    @Column(nullable = false)
    private Double totalCo2kg;

    @OneToMany(mappedBy = "surveyResponse", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SurveyAnswer> answers = new ArrayList<>();

//    @Column(columnDefinition = "TEXT")
//    private String feedback; // LLM 피드백 저장용

    @Column
    private String top1;

    @Column
    private String top2;

    @Column
    private String top3;
}
