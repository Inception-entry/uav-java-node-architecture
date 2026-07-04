package com.uav.backend.ai.client;

import com.uav.backend.ai.dto.AiChatRequest;
import com.uav.backend.ai.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AiChatClient {

    private final RestClient restClient;

    public AiChatClient(
            RestClient.Builder builder,
            @Value("${app.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String chat(String message) {
        AiChatResponse response = restClient.post()
                .uri("/api/chat")
                .body(new AiChatRequest(message))
                .retrieve()
                .body(AiChatResponse.class);

        if (response == null || response.answer() == null) {
            throw new IllegalStateException("AI 服务返回了空结果");
        }

        return response.answer();
    }
}
