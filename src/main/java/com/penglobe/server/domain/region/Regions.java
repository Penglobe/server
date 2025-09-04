package com.penglobe.server.domain.region;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "regions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Regions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "region_id")
    private Integer regionId; //지역 아이디

    @Column(nullable = false)
    private String name; //지역 명

    @Builder.Default
    @Column(name = "total_co2kg", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCo2kg = BigDecimal.ZERO; //지역별 총 탄소 절감량

}
