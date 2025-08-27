package com.penglobe.server.domain.survey;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="survey_answer")
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
//사용자가 설문에서 각 항목별 선택한 답변 -> 기반으로 총 탄소배출량 구함, top3 데이터가 됨
public class SurveyAnswer extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_response_id", nullable = false)
    private SurveyResponse surveyResponse;

    //항목id
    @Column(nullable = false)
    private Long itemId;

    //사용자가 선택한 값
    @Column(nullable = false)
    private Integer selectValues;

    //선택값 기반 co2
    @Column(nullable = false)
    private Double co2;

}
