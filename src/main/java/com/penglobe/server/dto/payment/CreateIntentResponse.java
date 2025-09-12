package com.penglobe.server.dto.payment;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class CreateIntentResponse {
    private String merchantUid;
    private String payUrl;
}