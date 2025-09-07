package com.penglobe.server.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // application.properties 또는 application-*.properties 에서 불러옴
    @Value("${swagger.server.url}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("🐧 Penglobe API")
                        .description("📘 Penglobe API 명세서\n\n" +
                                "🔑 인증이 필요한 API는 **Authorize 버튼**(🔒 아이콘)을 클릭하세요.\n\n" +
                                "👉 토큰 입력 형식: \n" +
                                "`Bearer {JWT 토큰}`\n\n" +
                                "예시:\n" +
                                "`Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...`\n\n" +
                                "토큰을 입력하면, 이후 실행하는 모든 요청에 자동으로 `Authorization` 헤더가 추가됩니다.")
                        .version("v1"))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)   // 🔑 환경별로 값이 달라짐
                                .description("API Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("여기에 JWT 토큰을 입력하세요. (형식: `Bearer <토큰>`)")));
    }
}
