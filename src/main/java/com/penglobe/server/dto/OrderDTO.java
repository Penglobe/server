// src/main/java/com/penglobe/server/dto/OrderDTO.java
package com.penglobe.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDTO {
    // ===== 요청 필드 =====
    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long productId;

    @NotNull @Min(1)
    private Integer qty;

    // ===== 응답 전용 필드 =====
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long orderId;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String productName;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer totalPoints;

    // Orders.status가 Enum이면 문자열로 내려줌
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
}
