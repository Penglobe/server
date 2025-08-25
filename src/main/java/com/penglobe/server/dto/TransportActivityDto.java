package com.penglobe.server.dto;
import com.penglobe.server.domain.transport.TransportActivity;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TransportActivityDto {

    private Long id;
    private Long userId;
    private String mode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int distanceM;
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
