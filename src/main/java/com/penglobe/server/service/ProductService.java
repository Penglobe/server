package com.penglobe.server.service;

import com.penglobe.server.domain.shop.Products;
import com.penglobe.server.dto.ProductDTO;
import com.penglobe.server.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductsRepository productsRepository;

    public List<ProductDTO> list() {
        return productsRepository.findAll()
                .stream().map(this::toDTO).toList();
    }

    public ProductDTO get(Long id) {
        Products p = productsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        return toDTO(p);
    }

    @Transactional
    public ProductDTO create(ProductDTO req) {
        Products p = new Products();
        apply(p, req);
        Products saved = productsRepository.save(p);
        return toDTO(saved);
    }

    @Transactional
    public void update(Long id, ProductDTO req) {
        Products p = productsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        apply(p, req); // Dirty Checking
    }

    @Transactional
    public void delete(Long id) {
        productsRepository.deleteById(id);
    }

    private void apply(Products p, ProductDTO req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setImg(req.getImg());
    }

    private ProductDTO toDTO(Products p) {
        return ProductDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .img(p.getImg())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
