package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.DailyCarbonReductionDTO;
import com.penglobe.server.dto.MyPageDTO;
import com.penglobe.server.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @GetMapping
    public ResponseEntity<ApiResponse<MyPageDTO>> getMyPageInfo(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());

        MyPageDTO myPageInfo = myPageService.getMyPageInfo(userId);
        return ResponseEntity.ok(ApiResponse.success(myPageInfo));
    }

    @GetMapping("/daily/{date}")
    public ResponseEntity<ApiResponse<DailyCarbonReductionDTO>> getDailyCarbonReduction(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long userId = Long.parseLong(userDetails.getUsername()); // Placeholder: Adjust based on actual UserDetails implementation

        DailyCarbonReductionDTO dailyReduction = myPageService.getDailyCarbonReduction(userId, date);
        return ResponseEntity.ok(ApiResponse.success(dailyReduction));
    }
}
