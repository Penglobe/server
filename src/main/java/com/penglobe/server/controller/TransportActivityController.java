package com.penglobe.server.controller;
import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.service.TransportActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transport")
@RequiredArgsConstructor
public class TransportActivityController {

    private final TransportActivityService activityService;
    private final UserRepository userRepository;

    // 이동 시작
    @PostMapping("/start")
    public TransportActivity start(@RequestParam Long userId, @RequestParam TransportMode mode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return activityService.startActivity(user, mode);
    }

    // 이동 종료
    @PostMapping("/{id}/stop")
    public TransportActivity stop(@PathVariable Long id,
                                  @RequestParam int distanceM,
                                  @RequestBody(required = false) String pathGeojson) {
        return activityService.stopActivity(id, distanceM, pathGeojson);
    }
}
