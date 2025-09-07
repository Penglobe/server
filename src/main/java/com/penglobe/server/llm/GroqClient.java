// src/main/java/com/penglobe/server/llm/GroqClient.java
package com.penglobe.server.llm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GroqClient {

    private final WebClient groqWebClient;

    @Value("${groq.model}")
    private String model;

    public String chat(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "max_tokens", 512,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a carbon emission calculator. Respond ONLY with JSON."),
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            Map<?, ?> response = groqWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            var choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Groq: empty choices");
            }

            var msg = (Map<String, Object>) choices.get(0).get("message");
            String content = (String) msg.get("content");
            if (content == null) {
                throw new RuntimeException("Groq: empty content");
            }
            return content;

        } catch (WebClientResponseException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Groq call failed: " + e.getMessage(), e);
        }
    }
}
