package com.penglobe.server.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class KakaoMapController {

    @Value("${kakao.js.key}")
    private String kakaoApiKey;

    @GetMapping("/map")
    public String map(@RequestParam(required = false) Double startLat,
                      @RequestParam(required = false) Double startLng,
                      @RequestParam(required = false) Double endLat,
                      @RequestParam(required = false) Double endLng,
                      @RequestParam(required = false) Double currentLat,
                      @RequestParam(required = false) Double currentLng,
                      Model model) {
        model.addAttribute("kakaoApiKey", kakaoApiKey);
        model.addAttribute("startLat", startLat);
        model.addAttribute("startLng", startLng);
        model.addAttribute("endLat", endLat);
        model.addAttribute("endLng", endLng);
        model.addAttribute("currentLat", currentLat);
        model.addAttribute("currentLng", currentLng);

        // ✅ SVG는 HTML 내부에서 직접 정의하므로 전달 불필요
        return "map"; // templates/map.html
    }
}
