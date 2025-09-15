package com.penglobe.server.service;

import com.penglobe.server.domain.shop.Products;
import com.penglobe.server.dto.ProductDTO;
import com.penglobe.server.repository.OrdersRepository;
import com.penglobe.server.repository.ProductsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductsRepository productsRepository;
    private final OrdersRepository ordersRepository;

    /** 목록 (최신 ID 순) */
    public List<ProductDTO> list() {
        return productsRepository.findAll(Sort.by(Sort.Direction.DESC, "productId"))
                .stream().map(this::toDTO).toList();
    }

    /** 상세 */
    public ProductDTO get(Long id) {
        Products p = productsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        return toDTO(p);
    }

    /** 생성: 컨트롤러에서 업로드된 이미지 경로(req.img)를 넣어줌 */
    @Transactional
    public ProductDTO create(ProductDTO req) {
        validateRequiredForCreate(req);

        Products p = new Products();
        applyCreate(p, req);
        Products saved = productsRepository.save(p);
        return toDTO(saved);
    }

    //기부하기
    @Transactional
    public ProductDTO createDonation(ProductDTO req) {
        validateRequiredForCreateForDonation(req);

        Products p = new Products();
        applyCreate(p, req);
        Products saved = productsRepository.save(p);
        return toDTO(saved);
    }

    /** 수정: req.img == null 이면 기존 이미지 유지, 값이 있으면 교체 */
    @Transactional
    public void update(Long id, ProductDTO req) {
        Products p = productsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        applyUpdate(p, req); // Dirty Checking
        productsRepository.save(p);
    }

    /** 삭제 (이미지 파일 정리는 컨트롤러/UploadService에서 수행) */
    @Transactional
    public void delete(Long id) {
        if (ordersRepository.existsByProduct_ProductId(id)) {
            throw new IllegalStateException("내역이 있어 삭제할 수 없습니다.");
        }
        productsRepository.deleteById(id);
    }

    /** 컨트롤러에서 기존 이미지 경로 조회용 (교체/삭제 시 사용) */
    public String findImgPath(Long id) {
        Products p = productsRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상품이 없습니다."));
        return p.getImg();
    }

    // ----------------------
    // 내부 헬퍼
    // ----------------------

    private void applyCreate(Products p, ProductDTO req) {
        p.setName(req.getName());
        p.setDescription(req.getDescription());
        p.setPrice(req.getPrice());
        p.setImg(req.getImg()); // 업로드된 경로(또는 null)
    }

    private void applyUpdate(Products p, ProductDTO req) {
        if (req.getName() != null)        p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        //if (req.getPrice() != null)       p.setPrice(req.getPrice());
        // 새 이미지가 전달된 경우에만 교체 (null이면 기존 유지)
        if (req.getImg() != null && !req.getImg().isBlank()) {
            p.setImg(req.getImg());
        }
    }

    private void validateRequiredForCreate(ProductDTO req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (req.getPrice() == null) {
            throw new IllegalArgumentException("가격은 필수입니다.");
        }
    }

    private void validateRequiredForCreateForDonation(ProductDTO req) {
        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("기부명은 필수입니다.");
        }

    }

    private ProductDTO toDTO(Products p) {
        return ProductDTO.builder()
                .productId(p.getProductId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .img(p.getImg())
                .createdAt(p.getCreatedAt())
                // .updatedAt(p.getUpdatedAt())  // 필요하면 DTO에 필드 추가 후 주석 해제
                .build();
    }
}
