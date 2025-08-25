package com.penglobe.server.domain.point;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="point",  uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_user_reason_ref",
                columnNames = {"user_id", "reason", "ref_table", "ref_id"}
        )})
public class PointLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    //기준 발행일
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    //적립, 차감
    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private Reason reason;

    //근거 테이블명
    @Column(name = "ref_table", length = 50)
    private String refTable;

    //근거 테이블 id
    @Column(name = "ref_id")
    private Long refId;

    //거래 후 잔액
    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

}
