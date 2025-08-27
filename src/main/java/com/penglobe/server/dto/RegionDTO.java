package com.penglobe.server.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegionDTO {

    private int rank; // 지역 순위
    private Integer regionId; // 지역 아이디
    private String regionName; // 지역 이름
    private BigDecimal totalSaving; // 총 절감량

    // JPQL 조회를 위한 생성자 (임시: int 타입 확인용)
    public RegionDTO(Integer regionId, String regionName, int totalSaving) {
        this.regionId = regionId;
        this.regionName = regionName;
        this.totalSaving = BigDecimal.valueOf(totalSaving); // Convert int to BigDecimal
        // No null check needed for primitive int
    }

}
