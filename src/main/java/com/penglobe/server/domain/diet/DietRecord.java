package com.penglobe.server.domain.diet;
import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "diet_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DietRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dietId;

    // 사용자 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 업로드한 이미지 URL
    @Column(name = "image_url", nullable = false, length = 255)
    private String imageUrl;

    // 분석 JSON 요약
    @Lob
    @Column(name = "analysis_json")
    private String analysisJson;

    // 절감 배출량 (kg)
    @Builder.Default
    @Column(name = "co2_kg", nullable = false)
    private Integer co2Kg = 0;
}