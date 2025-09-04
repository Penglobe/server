package com.penglobe.server.domain.user;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Column(length = 512, nullable = false)
    private String token;

    private Long expiryTime; // 만료시간 (epoch millis)
}
