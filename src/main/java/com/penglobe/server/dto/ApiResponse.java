package com.penglobe.server.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "공통 API 응답 형식")
public class ApiResponse<T> {

    @Schema(description = "HTTP 상태 코드", example = "200")
    private int status;

    @Schema(description = "응답 메시지", example = "요청 성공")
    private String message;

    @Schema(description = "응답 데이터 (제네릭 타입)")
    private T data;

    // ✅ 성공 응답 팩토리 메서드
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "요청 성공", data);
    }

    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    // ✅ 실패 응답 팩토리 메서드
    public static <T> ApiResponse<T> fail(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}
