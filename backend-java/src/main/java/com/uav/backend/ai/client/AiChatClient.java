package com.uav.backend.ai.client;

import com.uav.backend.ai.dto.AiChatRequest;
import com.uav.backend.ai.dto.AiChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.OutputStream;

@Component
public class AiChatClient {

    private final RestClient restClient;

    public AiChatClient(
            RestClient.Builder builder,
            @Value("${app.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public String chat(String sessionId, String message) {
        return chat(sessionId, message, null);
    }

    public String chat(
            String sessionId,
            String message,
            String knowledgeQuery) {
        AiChatResponse response = restClient.post()
                .uri("/api/chat")
                .body(new AiChatRequest(sessionId, message, knowledgeQuery))
                .retrieve()
                .body(AiChatResponse.class);

        if (response == null || response.answer() == null) {
            throw new IllegalStateException("AI 服务返回了空结果");
        }

        return response.answer();
    }

    public void stream(
            String sessionId,
            String message,
            String knowledgeQuery,
            OutputStream outputStream) {
        restClient.post()
                .uri("/api/chat/stream")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .body(new AiChatRequest(sessionId, message, knowledgeQuery))
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException(
                                "AI 流式服务请求失败: HTTP "
                                        + response.getStatusCode().value()
                        );
                    }
                    try (var inputStream = response.getBody()) {
                        inputStream.transferTo(outputStream);
                        outputStream.flush();
                    }
                    return null;
                });
    }
}
