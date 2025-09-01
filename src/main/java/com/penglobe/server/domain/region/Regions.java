package com.penglobe.server.domain.region;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "regions")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Regions extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer region_id; //지역 아이디

    @Column(nullable = false)
    private String name; //지역 명


}
