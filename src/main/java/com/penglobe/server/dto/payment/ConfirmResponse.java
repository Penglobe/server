package com.penglobe.server.dto.payment;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConfirmResponse {
    private boolean ok;
    private String message;
    private Integer newBalance; // null 가능(실패 시)
}
