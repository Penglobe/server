package com.penglobe.server.controller;

import com.penglobe.server.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/kakao")
@Tag(name = "카카오 API 프록시", description = "카카오 지도/주소 검색을 위한 프록시 API")
public class KakaoProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    @Operation(
            summary = "주소 검색",
            description = "카카오 로컬 API를 통해 주소를 검색합니다."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<String>> search(
            @Parameter(description = "검색할 주소/장소명 (예: 서울역)", required = true)
            @RequestParam String query
    ) {
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + query;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return ResponseEntity.ok(ApiResponse.success(response.getBody()));
    }

}
