package com.penglobe.server.domain.user;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "profile", nullable = false)
    private String profile; // 기본값 제거 → PrePersist에서 보장

    @Column(name = "kakao_id")
    private Long kakaoId;

    @Builder.Default
    @Column(name = "is_profile_complete", nullable = false)
    private Boolean isProfileComplete = false;

    @Column(name = "last_week_rank")
    private Integer lastWeekRank; // 지난 주 최종 랭킹

    /**
     * DB에 INSERT 되기 전에 null 값들을 기본값으로 채움
     */
    @PrePersist
    public void prePersist() {
        if (this.type == null) {
            this.type = UserType.USER;
        }
        if (this.totalPoint == null) {
            this.totalPoint = 0;
        }
        if (this.profile == null) {
            this.profile = "ToryFace";
        }
        if (this.isProfileComplete == null) {
            this.isProfileComplete = false;
        }
    }
}
