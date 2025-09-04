package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.ProductDTO;
import com.penglobe.server.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop/products")
@Tag(name = "Shop - Products", description = "굿즈샵 상품 API")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "상품 목록", description = "굿즈샵에 노출되는 상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> list() {
        List<ProductDTO> res = productService.list();
        return ResponseEntity.ok(ApiResponse.success(200, "상품 목록 조회 성공", res));
    }

    @Operation(summary = "상품 상세", description = "상품 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> get(@PathVariable Long id) {
        ProductDTO res = productService.get(id);
        return ResponseEntity.ok(ApiResponse.success(200, "상품 상세 조회 성공", res));
    }

    @Operation(summary = "상품 생성(관리자)", description = "신규 상품을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> create(@RequestBody @Valid ProductDTO req) {
        ProductDTO res = productService.create(req);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .location(URI.create("/shop/products/" + res.getProductId()))
                .body(ApiResponse.success(201, "상품 생성 완료", res));
    }

    @Operation(summary = "상품 수정(관리자)", description = "상품 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> update(@PathVariable Long id,
                                                    @RequestBody @Valid ProductDTO req) {
        productService.update(id, req);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(204, "상품 수정 완료", null));
    }

    @Operation(summary = "상품 삭제(관리자)", description = "상품을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success(204, "상품 삭제 완료", null));
    }
}
