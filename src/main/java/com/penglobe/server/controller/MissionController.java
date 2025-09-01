package com.penglobe.server.controller;

import com.penglobe.server.domain.mission.MissionMetric;
import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.MissionSlotDTO;
import com.penglobe.server.service.MissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.penglobe.server.domain.mission.MissionMetric.*;


@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
@Tag(name = "Missions", description = "미션(환경걸음/식단/출석) 4칸 창 조회 및 수령 API")
public class MissionController {
    private final MissionService missionService;

    @GetMapping("/windows")
    @Operation(
            summary = "미션 3개(각 4칸) 한 번에 조회",
            description = "사용자의 진행도/수령 이력 기준으로 환경걸음·식단·출석 각각 4칸의 목표치와 상태(locked/claimable/claimed)를 반환합니다. "
                    + "윈도우의 앵커는 '마지막 수령 타겟'(없으면 startTarget)입니다."
    )
    public ResponseEntity<ApiResponse<Map<MissionMetric, List<MissionSlotDTO>>>> windows(
            @Parameter(description = "사용자 ID", required = true, example = "123")
            @RequestHeader("X-User-Id") Long userId) {

        // 보기 좋은 고정 순서로 정렬해서 내려주고 싶으면 아래 사용
        Map<MissionMetric, List<MissionSlotDTO>> ordered = new LinkedHashMap<>();
        ordered.put(WALK_CO2_KG, missionService.getWindow(userId, WALK_CO2_KG));
        ordered.put(DIET_CO2_KG, missionService.getWindow(userId, DIET_CO2_KG));
        ordered.put(ATTEND_MONTH_DAYS, missionService.getWindow(userId, ATTEND_MONTH_DAYS));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "미션 창 조회 성공", ordered));
    }

    @PostMapping("/claim")
    @Operation(
            summary = "미션 수령",
            description = "진행도가 목표치 이상이고 아직 미수령이면 수령 처리합니다. "
                    + "(중복/동시성은 UNIQUE(user, metric, target)로 멱등 보장)"
    )
    public ResponseEntity<ApiResponse<Void>> claim(
            @Parameter(description = "미션 종류", required = true, example = "WALK_CO2_KG")
            @RequestParam MissionMetric metric,
            @Parameter(description = "수령할 목표치(kg/days)", required = true, example = "40")
            @RequestParam long target,
            @Parameter(description = "사용자 ID", required = true, example = "123")
            @RequestHeader("X-User-Id") Long userId) {

        missionService.claim(userId, metric, target);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(HttpStatus.OK.value(), "미션 수령 성공", null));
    }
}
