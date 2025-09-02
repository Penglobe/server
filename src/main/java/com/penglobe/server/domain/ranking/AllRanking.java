package com.penglobe.server.domain.ranking;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "all_ranking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllRanking extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long allRankingId;

    @Column(name = "user_id", unique = true, nullable = false)
    private Long userId;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "score", nullable = false, precision = 10, scale = 2)
    private BigDecimal score = BigDecimal.ZERO;

    @Column(name = "ranking")
    private Integer ranking;
}
