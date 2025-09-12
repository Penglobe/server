package com.penglobe.server.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateIntentRequest {
    @NotNull @Positive
    private Integer amount;

    @NotBlank
    private String name;

    @NotBlank
    private String callback; // 딥링크/콜백 URL
}