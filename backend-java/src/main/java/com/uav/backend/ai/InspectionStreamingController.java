package com.uav.backend.ai;

import com.uav.backend.ai.client.AiChatClient;
import com.uav.backend.ai.dto.InspectionAnalysisRequest;
import com.uav.backend.ai.service.InspectionAnalysisPromptService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@RestController
@RequestMapping("/inspection-workflows")
public class InspectionStreamingController {

    private final AiChatClient aiChatClient;
    private final InspectionAnalysisPromptService promptService;

    public InspectionStreamingController(
            AiChatClient aiChatClient,
            InspectionAnalysisPromptService promptService) {
        this.aiChatClient = aiChatClient;
        this.promptService = promptService;
    }

    @PostMapping(
            value = "/{taskCode}/analysis/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<StreamingResponseBody> stream(
            @PathVariable String taskCode,
            @Valid @RequestBody InspectionAnalysisRequest request) {
        String sessionId = request.sessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = "inspection-stream-" + UUID.randomUUID();
        }

        String prompt = promptService.buildPrompt(
                taskCode,
                request.question()
        );
        String currentSessionId = sessionId;
        StreamingResponseBody body = outputStream -> aiChatClient.stream(
                currentSessionId,
                prompt,
                request.question(),
                outputStream
        );

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .cacheControl(CacheControl.noCache())
                .header("X-Accel-Buffering", "no")
                .body(body);
    }
}
