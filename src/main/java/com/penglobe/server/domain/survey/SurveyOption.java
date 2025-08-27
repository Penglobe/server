package com.penglobe.server.domain.survey;

import com.penglobe.server.domain.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.cache.spi.support.AbstractReadWriteAccess;

@Entity
@Table(name = "survey_option")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
//설문조사 내용 중 항목 관련
public class SurveyOption extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private SurveyItem surveyItem;

    //선택지 값
    //1, 2, 3
    @Column(nullable = false)
    private Integer value;

    //선택지 설명
    //보류
    @Column(nullable = false, length = 100)
    private String description;

    //선택지에 해당하는 co2
    @Column(nullable = false)
    private Double co2;
}
