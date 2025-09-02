package com.penglobe.server.domain.survey;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "survey_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//설문조사 내용
public class SurveyItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long  surveyItemId;

    //항목 코드
    //분리배출, 일회용품, 종이 타월사용, 음식물쓰레기, 에너지절약, 새로운시도
    @Column(nullable = false, length = 100)
    private String code;

    //질문 내용
    @Column(nullable = false, length = 200)
    private String question;
}
