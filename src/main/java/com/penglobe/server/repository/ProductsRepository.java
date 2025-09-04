package com.penglobe.server.repository;

import com.penglobe.server.domain.shop.Products;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductsRepository extends JpaRepository<Products, Long> {
}
