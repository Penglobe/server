package com.penglobe.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("\uD83D\uDC27 Penglobe API")
                        .description("📘 Penglobe API 명세서\n\n" +
                                "🔑 인증이 필요한 API는 상단의 **Authorize 버튼**(🔒 아이콘)을 클릭하세요.\n\n" +
                                "👉 토큰 입력 형식: \n" +
                                "`Bearer {JWT 토큰}`\n\n" +
                                "예시:\n" +
                                "`Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`\n\n" +
                                "토큰을 입력하면, 이후 실행하는 모든 요청에 자동으로 `Authorization` 헤더가 추가됩니다.")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("여기에 JWT 토큰을 입력하세요. (형식: `Bearer <토큰>`)"))  // ✅ SecurityScheme 설명 추가
                );
    }
}
