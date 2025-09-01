package com.penglobe.server.dto;

import com.penglobe.server.domain.transport.UserPlaceBookmark;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BookmarkDto {
    private Long bookmarkId;
    private String bookmarkLabel;
    private String address;
    private Integer regionId;
    private BigDecimal lat;
    private BigDecimal lng;

    public static BookmarkDto fromEntity(UserPlaceBookmark b) {
        return BookmarkDto.builder()
                .bookmarkId(b.getBookmarkId())
                .bookmarkLabel(b.getBookmarkLabel())
                .address(b.getAddress())
                .regionId(b.getRegionId())
                .lat(b.getLat())
                .lng(b.getLng())
                .build();
    }
}
