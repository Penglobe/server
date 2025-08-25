package com.penglobe.server.domain.user;

import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.UserType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type = UserType.USER;

    @Column(unique = true, length = 190)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Column(name = "home_region_id")
    private Integer homeRegionId;

    @Column(name = "total_point", nullable = false)
    private Integer totalPoint = 0; // 보유 포인트 캐시

    @Column(name = "profile_id")
    private Integer profileId;

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Column(name = "is_profile_complete", nullable = false)
    private Boolean isProfileComplete = false;
}
