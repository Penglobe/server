package com.penglobe.server.domain.transport;


import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_place_bookmarks")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class UserPlaceBookmark extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookmarkId;

    // 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String bookmarkLabel; // 예: 집, 회사, 학교

    @Column(length = 255)
    private String address;

    @Column(name = "region_id")
    private Integer regionId;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lat;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal lng;

}
