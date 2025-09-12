package com.penglobe.server.dto.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ConfirmRequest {

    // 프론트에서 imp_uid, merchant_uid로 보내므로 매핑
    @JsonProperty("imp_uid")
    @NotBlank
    private String impUid;

    @JsonProperty("merchant_uid")
    @NotBlank
    private String merchantUid;

    // true/false가 확실히 오면 primitive로 바꿔도 됨
    private Boolean success;
}
