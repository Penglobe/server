package com.penglobe.server.dto;

import com.penglobe.server.domain.transport.TransportActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Schema(description = "이동 활동 DTO")
public class TransportActivityDto {

    @Schema(description = "활동 ID", example = "10")
    private Long transportId;

    @Schema(description = "사용자 ID", example = "1")
    private Long userId;

    @Schema(description = "이동 수단 (WALK/BIKE/TRANSIT)", example = "WALK")
    private String mode;

    @Schema(description = "이동 시작 시각 (ISO 8601)", example = "2025-08-25T08:00:00")
    private LocalDateTime startTime;

    @Schema(description = "이동 종료 시각 (ISO 8601)", example = "2025-08-25T08:30:00")
    private LocalDateTime endTime;

    @Schema(description = "총 이동 시간 (분)", example = "30")
    private int durationM;

    @Schema(description = "이동 거리 (미터)", example = "1200")
    private int distanceM;

    @Schema(description = "절감된 CO2 배출량 (kg)", example = "1.68")
    private BigDecimal co2Kg;

    @Schema(description = "지급된 포인트", example = "120")
    private int points;

    // Factory Method: Entity -> DTO
    public static TransportActivityDto fromEntity(TransportActivity entity) {
        return TransportActivityDto.builder()
                .transportId(entity.getTransportId())
                .userId(entity.getUser().getUserId())
                .mode(entity.getMode().name())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .distanceM(entity.getDistanceM())
                .co2Kg(entity.getCo2Kg())
                .build();
    }

    public static TransportActivityDto fromEntity(TransportActivity entity, int durationM, int points) {
        return TransportActivityDto.builder()
                .transportId(entity.getTransportId())
                .userId(entity.getUser().getUserId())
                .mode(entity.getMode().name())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .durationM(durationM)
                .distanceM(entity.getDistanceM())
                .co2Kg(entity.getCo2Kg())
                .points(points)
                .build();
    }
}
