package com.penglobe.server.dto;

import com.penglobe.server.domain.transport.UserPlaceBookmark;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookmarkDto {
    private Long id;
    private String label;
    private String address;
    private Integer regionId;
    private BigDecimal lat;
    private BigDecimal lng;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookmarkDto fromEntity(UserPlaceBookmark b) {
        return BookmarkDto.builder()
                .id(b.getId())
                .label(b.getLabel())
                .address(b.getAddress())
                .regionId(b.getRegionId())
                .lat(b.getLat())
                .lng(b.getLng())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
