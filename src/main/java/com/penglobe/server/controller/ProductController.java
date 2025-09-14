// src/main/java/com/penglobe/server/controller/ProductController.java
package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import com.penglobe.server.dto.ProductDTO;
import com.penglobe.server.service.ProductService;
import com.penglobe.server.service.UploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/shop/products")
@Tag(name = "Shop - Products", description = "굿즈샵 상품 API (멀티파트 전용)")
public class ProductController {

    private final ProductService productService;
    private final UploadService uploadService;

    @Operation(summary = "상품 목록")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<List<ProductDTO>>> list() {
        List<ProductDTO> res = productService.list();
        return ResponseEntity.ok(ApiResponse.success(200, "상품 목록 조회 성공", res));
    }

    @Operation(summary = "상품 상세")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> get(@PathVariable Long id) {
        ProductDTO res = productService.get(id);
        return ResponseEntity.ok(ApiResponse.success(200, "상품 상세 조회 성공", res));
    }

    @Operation(summary = "상품 생성 - multipart", description = "이미지 파일 업로드로 신규 상품 생성")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> create(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") Integer price,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String imgPath = (image != null && !image.isEmpty())
                    ? uploadService.saveImage(image)
                    : null;

            ProductDTO req = new ProductDTO();
            req.setName(name);
            req.setDescription(description);
            req.setPrice(price);
            req.setImg(imgPath);

            ProductDTO res = productService.create(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(URI.create("/shop/products/" + res.getProductId()))
                    .body(ApiResponse.success(201, "상품 생성 완료", res));
        } catch (Exception e) {
            // 디버깅 편의: 원인 그대로 내려주기
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(400, e.getMessage()));
        }
    }
    @Operation(summary = "기부 생성 - multipart", description = "이미지 파일 업로드로 기부 등록")
    @PostMapping(value = "/donation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProductDTO>> createDonation(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String imgPath = (image != null && !image.isEmpty())
                    ? uploadService.saveImage(image)
                    : null;

            ProductDTO req = new ProductDTO();
            req.setName("[기부] " + name);
            req.setDescription(description);
            req.setPrice(req.getPrice() != null ? req.getPrice() : 0); // null이면 0으로 처리
            req.setImg(imgPath);

            ProductDTO res = productService.createDonation(req);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .location(URI.create("/shop/products/" + res.getProductId()))
                    .body(ApiResponse.success(201, "상품 생성 완료", res));
        } catch (Exception e) {
            // 디버깅 편의: 원인 그대로 내려주기
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(400, e.getMessage()));
        }
    }

    @Operation(summary = "상품 수정 - multipart", description = "부분 수정 + 이미지 교체 가능")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> update(
            @PathVariable Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "price", required = false) Integer price,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        try {
            String newImg = null;
            if (image != null && !image.isEmpty()) {
                String oldImg = productService.findImgPath(id);
                newImg = uploadService.saveImage(image);
                uploadService.deleteIfExists(oldImg);
            }

            ProductDTO req = new ProductDTO();
            req.setName(name);
            req.setDescription(description);
            req.setPrice(price);
            req.setImg(newImg); // null이면 기존 유지

            productService.update(id, req);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(204, "상품 수정 완료", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(400, e.getMessage()));
        }
    }

    @Operation(summary = "상품 삭제")
    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            String oldImg = productService.findImgPath(id);
            productService.delete(id);
            uploadService.deleteIfExists(oldImg);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(ApiResponse.success(204, "상품 삭제 완료", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail(400, e.getMessage()));
        }
    }

    // 🔧 멀티파트 바인딩/용량 오류를 400으로 내려서 원인 보이게
    @ExceptionHandler({ MultipartException.class, IllegalArgumentException.class, IllegalStateException.class })
    public ResponseEntity<ApiResponse<Void>> handleMultipartErrors(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(400, e.getMessage()));
    }
}
