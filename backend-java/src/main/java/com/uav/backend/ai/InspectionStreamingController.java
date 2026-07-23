package com.uav.backend.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uav.backend.ai.client.AiChatClient;
import com.uav.backend.ai.domain.AnalysisChannel;
import com.uav.backend.ai.dto.InspectionAnalysisRequest;
import com.uav.backend.ai.service.InspectionAnalysisPromptService;
import com.uav.backend.ai.service.InspectionAnalysisRecordService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inspection-workflows")
public class InspectionStreamingController {

    private static final Logger log = LoggerFactory.getLogger(
            InspectionStreamingController.class
    );

    private final AiChatClient aiChatClient;
    private final InspectionAnalysisPromptService promptService;
    private final InspectionAnalysisRecordService recordService;
    private final ObjectMapper objectMapper;

    public InspectionStreamingController(
            AiChatClient aiChatClient,
            InspectionAnalysisPromptService promptService,
            InspectionAnalysisRecordService recordService,
            ObjectMapper objectMapper) {
        this.aiChatClient = aiChatClient;
        this.promptService = promptService;
        this.recordService = recordService;
        this.objectMapper = objectMapper;
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
        String analysisId = "inspection-stream-" + UUID.randomUUID();
        StreamingResponseBody body = outputStream -> {
            try {
                aiChatClient.stream(
                        currentSessionId,
                        prompt,
                        request.question(),
                        outputStream,
                        result -> recordService.saveCompleted(
                                analysisId,
                                taskCode,
                                currentSessionId,
                                AnalysisChannel.STREAM,
                                request.question(),
                                result.answer(),
                                result.model(),
                                result.sources()
                        )
                );
            } catch (Exception exception) {
                log.error(
                        "AI streaming analysis failed"
                                + " analysisId={} taskCode={} sessionId={}",
                        analysisId,
                        taskCode,
                        currentSessionId,
                        exception
                );
                writeErrorEvent(outputStream);
            }
        };

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_EVENT_STREAM)
                .cacheControl(CacheControl.noCache())
                .header("X-Accel-Buffering", "no")
                .body(body);
    }

    private void writeErrorEvent(java.io.OutputStream outputStream) {
        try {
            String data = objectMapper.writeValueAsString(Map.of(
                    "message",
                    "AI 分析未完成或结果保存失败，请重试"
            ));
            outputStream.write(
                    ("event: error\ndata: " + data + "\n\n")
                            .getBytes(StandardCharsets.UTF_8)
            );
            outputStream.flush();
        } catch (IOException exception) {
            log.debug(
                    "Unable to write SSE error event because client closed",
                    exception
            );
        }
    }
}
