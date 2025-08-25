package com.penglobe.server.domain.transport;
import com.penglobe.server.domain.BaseEntity;
import com.penglobe.server.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "transport_activities")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class TransportActivity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportMode mode;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(nullable = false)
    private Integer distanceM = 0;

    @Column(nullable = false)
    private Integer co2g = 0;

    @Column(nullable = false)
    private LocalDate takenOn;

    @Lob
    private String pathGeojson;
}
