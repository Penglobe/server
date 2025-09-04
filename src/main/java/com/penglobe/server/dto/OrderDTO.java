package com.penglobe.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.penglobe.server.domain.shop.OrderStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDTO {

    // 응답 전용
    @JsonProperty(access = Access.READ_ONLY)
    private Long orderId;

    // 요청 전용
    @NotNull
    @JsonProperty(access = Access.WRITE_ONLY)
    private Long productId;

    // 요청 전용
    @NotNull @Min(1)
    @JsonProperty(access = Access.WRITE_ONLY)
    private Integer qty;

    // 응답 전용
    @JsonProperty(access = Access.READ_ONLY)
    private Integer totalPoints;

    // 응답 전용
    @JsonProperty(access = Access.READ_ONLY)
    private String productName;

    // 응답 전용
    @JsonProperty(access = Access.READ_ONLY)
    private OrderStatus status;

    // 응답 전용
    @JsonProperty(access = Access.READ_ONLY)
    private LocalDateTime createdAt;
}
