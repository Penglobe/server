package com.penglobe.server.dto;

import com.penglobe.server.domain.transport.TransportActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "이동 활동 DTO")
public class TransportActivityDto {

    @Schema(description = "활동 ID", example = "10")
    private Long id;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이동 수단 (WALK/BIKE/TRANSIT)", example = "WALK")
    private String mode;

    @Schema(description = "이동 시작 시각 (ISO 8601)", example = "2025-08-25T08:00:00")
    private LocalDateTime startTime;

    @Schema(description = "이동 종료 시각 (ISO 8601)", example = "2025-08-25T08:30:00")
    private LocalDateTime endTime;

    @Schema(description = "이동 거리 (미터)", example = "1200")
    private int distanceM;

    @Schema(description = "절감된 CO2 배출량 (그램)", example = "168")
    private int co2g;

    // Factory Method: Entity -> DTO
    public static TransportActivityDto fromEntity(TransportActivity entity) {
        return TransportActivityDto.builder()
                .id(entity.getId())
                .userId(entity.getUser().getId())
                .mode(entity.getMode().name())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .distanceM(entity.getDistanceM())
                .co2g(entity.getCo2g())
                .build();
    }
}
