package com.uav.backend.ai.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uav.backend.ai.dto.AiChatRequest;
import com.uav.backend.ai.dto.AiChatResponse;
import com.uav.backend.ai.dto.AiStreamResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class AiChatClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public AiChatClient(
            RestClient.Builder builder,
            ObjectMapper objectMapper,
            @Value("${app.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
    }

    public String chat(String sessionId, String message) {
        return chat(sessionId, message, null);
    }

    public String chat(
            String sessionId,
            String message,
            String knowledgeQuery) {
        return chatResponse(sessionId, message, knowledgeQuery).answer();
    }

    public AiChatResponse chatResponse(
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

        return response;
    }

    public void stream(
            String sessionId,
            String message,
            String knowledgeQuery,
            OutputStream outputStream) {
        stream(
                sessionId,
                message,
                knowledgeQuery,
                outputStream,
                ignored -> {
                }
        );
    }

    public void stream(
            String sessionId,
            String message,
            String knowledgeQuery,
            OutputStream outputStream,
            Consumer<AiStreamResult> beforeDone) {
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
                    AiSseEventAccumulator accumulator =
                            new AiSseEventAccumulator(objectMapper);
                    boolean terminalEvent = false;
                    try (var reader = new BufferedReader(
                            new InputStreamReader(
                                    response.getBody(),
                                    StandardCharsets.UTF_8
                            )
                    )) {
                        List<String> eventLines = new ArrayList<>();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                if (!eventLines.isEmpty()) {
                                    terminalEvent = forwardEvent(
                                            eventLines,
                                            accumulator,
                                            outputStream,
                                            beforeDone
                                    );
                                    eventLines.clear();
                                    if (terminalEvent) {
                                        break;
                                    }
                                }
                            } else {
                                eventLines.add(line);
                            }
                        }
                        if (!terminalEvent && !eventLines.isEmpty()) {
                            terminalEvent = forwardEvent(
                                    eventLines,
                                    accumulator,
                                    outputStream,
                                    beforeDone
                            );
                        }
                    }
                    if (!terminalEvent) {
                        throw new IllegalStateException(
                                "AI 流式连接未返回结束事件"
                        );
                    }
                    return null;
                });
    }

    private boolean forwardEvent(
            List<String> eventLines,
            AiSseEventAccumulator accumulator,
            OutputStream outputStream,
            Consumer<AiStreamResult> beforeDone) throws IOException {
        AiSseEventAccumulator.ParsedEvent event =
                accumulator.accept(eventLines);

        if (event.isDone()) {
            beforeDone.accept(event.completedResult());
        }

        String eventBlock = String.join("\n", eventLines) + "\n\n";
        outputStream.write(
                eventBlock.getBytes(StandardCharsets.UTF_8)
        );
        outputStream.flush();

        return event.isDone() || event.isError();
    }
}
