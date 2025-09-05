package com.penglobe.server.domain.user;

import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.UserType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType type = UserType.USER;

    @Column(unique = true, length = 190)
    private String email;

    @Column(length = 255)
    private String passwordHash;

    @Column(length = 50)
    private String nickname;

    @Column(name = "region_id")
    private Integer regionId;

    @Builder.Default
    @Column(name = "total_point", nullable = false)
    private Integer totalPoint = 0; // 보유 포인트 캐시

    @Column(name = "profile", length = 64, nullable = false)
    @Builder.Default
    private String profile = "ToryFace";

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Builder.Default
    @Column(name = "is_profile_complete", nullable = false)
    private Boolean isProfileComplete = false;

    @Column(name = "last_week_rank")
    private Integer lastWeekRank; // 지난 주 최종 랭킹
}
