package com.penglobe.server.controller;

import com.penglobe.server.domain.transport.TransportActivity;
import com.penglobe.server.domain.transport.TransportMode;
import com.penglobe.server.domain.transport.UserPlaceBookmark;
import com.penglobe.server.domain.user.User;
import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.BookmarkDto;
import com.penglobe.server.dto.TransportActivityDto;
import com.penglobe.server.repository.UserRepository;
import com.penglobe.server.service.BookmarkService;
import com.penglobe.server.service.TransportActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@RestController
@RequestMapping("/transport")
@RequiredArgsConstructor
@Tag(name = "TransportActivity", description = "도보/자전거/대중교통 이동 기록 API")
public class TransportActivityController {

    private final TransportActivityService activityService;
    private final BookmarkService bookmarkService;
    private final UserRepository userRepository;

    @Operation(summary = "이동 시작", description = "사용자가 이동을 시작합니다.")
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<TransportActivityDto>> start(
            @RequestParam Long userId,
            @RequestParam TransportMode mode
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        TransportActivity activity = activityService.startActivity(user, mode);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "이동을 시작했습니다.", TransportActivityDto.fromEntity(activity)));
    }

    @Operation(summary = "이동 종료", description = "사용자가 이동을 종료합니다.")
    @PostMapping("/{transportId}/stop")
    public ResponseEntity<ApiResponse<TransportActivityDto>> stop(
            @PathVariable Long transportId,
            @RequestParam int distanceM,
            @RequestBody(required = false) String pathGeojson
    ) {
        TransportActivityDto dto = activityService.stopActivity(transportId, distanceM, pathGeojson);
        return ResponseEntity.ok(ApiResponse.success(200, "이동을 종료했습니다.", dto));
    }


    @Operation(summary = "북마크 등록")
    @PostMapping("/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkDto>> createBookmark(
            @RequestParam Long userId,
            @RequestBody BookmarkDto dto
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        UserPlaceBookmark b = bookmarkService.create(user, dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(201, "북마크 등록 완료", BookmarkDto.fromEntity(b)));
    }

    @Operation(summary = "내 북마크 목록 조회")
    @GetMapping("/bookmarks")
    public ResponseEntity<ApiResponse<List<BookmarkDto>>> listBookmarks(
            @RequestParam Long userId
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        List<BookmarkDto> list = bookmarkService.findByUser(user)
                .stream().map(BookmarkDto::fromEntity).toList();
        return ResponseEntity.ok(ApiResponse.success(200, "북마크 조회 성공", list));
    }

    @Operation(summary = "북마크 수정")
    @PutMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkDto>> updateBookmark(
            @PathVariable Long bookmarkId,
            @RequestBody BookmarkDto dto
    ) {
        UserPlaceBookmark b = bookmarkService.update(bookmarkId, dto);
        return ResponseEntity.ok(ApiResponse.success(200, "북마크 수정 성공", BookmarkDto.fromEntity(b)));
    }

    @Operation(summary = "북마크 삭제")
    @DeleteMapping("/bookmarks/{bookmarkId}")
    public ResponseEntity<ApiResponse<Void>> deleteBookmark(
            @PathVariable Long bookmarkId
    ) {
        bookmarkService.delete(bookmarkId);
        return ResponseEntity.ok(ApiResponse.success(200, "북마크 삭제 성공", null));
    }
}
