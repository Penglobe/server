package com.penglobe.server.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class RegionDTO {

    private Integer regionId; // 지역 아이디
    private String regionName; // 지역 이름
    private Long totalSaving; // 총 절감량



}
