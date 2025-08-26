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
public class SurveyItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long itemId; //항목id

    @Column(nullable = false, length = 100)
    private String code; //항목 코드

    @Column(nullable = false, length = 200)
    private String question; //내용
}
