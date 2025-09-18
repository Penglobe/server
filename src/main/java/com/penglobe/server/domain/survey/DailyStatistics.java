package com.penglobe.server.domain.survey;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name="survey_statistic", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"date", "userId"})
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DailyStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statistics_id;
    private LocalDate date; //해당 일자
    private int dayOfWeek; //월~일

    @Column(nullable = true)
    private Long userId;

    private double statisticsTotalCo2kg;
    private int yearWeek;


    @Column(nullable = true)
    private Integer userCount;
}
