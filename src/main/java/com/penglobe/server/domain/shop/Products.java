package com.penglobe.server.domain.shop;

import com.penglobe.server.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products") // ✅ 명시
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Products extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", columnDefinition = "BIGINT UNSIGNED")
    private Long productId;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "img", columnDefinition = "TEXT")
    private String img;
}

