package com.penglobe.server.controller;

import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.TransportActivityDto;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.service.TransportActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transport")
@RequiredArgsConstructor
@Tag(name = "TransportActivity", description = "도보/자전거/대중교통 이동 기록 API")
public class TransportActivityController {

    private final TransportActivityService activityService;
    private final UserRepository userRepository;

    @Operation(
            summary = "이동 시작",
            description = "사용자가 이동을 시작합니다. mode(WALK/BIKE/TRANSIT)와 userId가 필요합니다."
    )
    @PostMapping("/start")
    public ApiResponse<TransportActivityDto> start(
            @Parameter(description = "사용자 ID", required = true, example = "1")
            @RequestParam Long userId,
            @Parameter(description = "이동 수단", required = true, example = "WALK")
            @RequestParam TransportMode mode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        TransportActivity activity = activityService.startActivity(user, mode);
        return ApiResponse.ok(TransportActivityDto.fromEntity(activity));
    }

    @Operation(
            summary = "이동 종료",
            description = "사용자가 이동을 종료합니다. 이동 거리(m)와 선택적으로 pathGeojson을 전달할 수 있습니다."
    )
    @PostMapping("/{id}/stop")
    public ApiResponse<TransportActivityDto> stop(
            @Parameter(description = "활동 ID", required = true, example = "10")
            @PathVariable Long id,
            @Parameter(description = "이동 거리(m)", required = true, example = "1200")
            @RequestParam int distanceM,
            @Parameter(description = "경로 GeoJSON", required = false, example = "{\"type\":\"LineString\",...}")
            @RequestBody(required = false) String pathGeojson) {

        TransportActivity activity = activityService.stopActivity(id, distanceM, pathGeojson);
        return ApiResponse.ok(TransportActivityDto.fromEntity(activity));
    }
}
