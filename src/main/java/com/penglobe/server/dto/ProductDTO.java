package com.penglobe.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {

    // 응답 전용
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long productId;

    // create에선 필수지만(update는 선택): 서비스 레벨에서 검증
    @Size(max = 100)
    private String name;

    private String description;

    // create에선 필수지만(update는 선택): 서비스 레벨에서 검증
    @Min(0)
    private Integer price;

    // 업로드 저장 후의 공개 경로(/uploads/..)
    private String img;

    // 응답 전용
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;

    // 응답 전용
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime updatedAt;
}
