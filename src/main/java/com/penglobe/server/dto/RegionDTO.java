package com.penglobe.server.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegionDTO {

    private int rank; // 지역 순위
    private Integer regionId; // 지역 아이디
    private String regionName; // 지역 이름
    private BigDecimal totalCo2; // 총 절감량

    // JPQL 조회를 위한 생성자
    public RegionDTO(Integer regionId, String regionName, BigDecimal totalCo2) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.totalCo2 = totalCo2;
        if (this.totalCo2 == null) {
            this.totalCo2 = BigDecimal.ZERO;
        }
    }

}
