package com.penglobe.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {

    // 응답 전용
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long productId;

    @NotBlank
    @Size(max = 100)
    private String name;

    private String description;

    @NotNull
    @Min(0)
    private Integer price;

    private String img;

    // 응답 전용 (BaseEntity의 createdAt 노출하고 싶을 때)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
}
