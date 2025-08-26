package com.penglobe.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//json으로 반환할 때 사용하는 dto
public class PointLedgerResponse {
    private List<PointDTO> points;
    private int count;
}
