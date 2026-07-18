package com.uav.backend.ai.client;

import com.uav.backend.ai.dto.KnowledgeDeleteResponse;
import com.uav.backend.ai.dto.KnowledgeDocumentResponse;
import com.uav.backend.ai.dto.KnowledgeSearchRequest;
import com.uav.backend.ai.dto.KnowledgeSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class AiKnowledgeClient {

    private final RestClient restClient;

    public AiKnowledgeClient(
            RestClient.Builder builder,
            @Value("${app.ai.base-url}") String baseUrl) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public List<KnowledgeDocumentResponse> listDocuments() {
        List<KnowledgeDocumentResponse> response = restClient.get()
                .uri("/api/knowledge/documents")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return response == null ? List.of() : response;
    }

    public KnowledgeDocumentResponse upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("请选择需要上传的文档");
        }

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        try {
            body.part(
                            "file",
                            new NamedByteArrayResource(
                                    file.getBytes(),
                                    file.getOriginalFilename()
                            )
                    )
                    .contentType(resolveContentType(file));
        } catch (IOException ex) {
            throw new IllegalArgumentException("无法读取上传的文档", ex);
        }

        KnowledgeDocumentResponse response = restClient.post()
                .uri("/api/knowledge/documents")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body.build())
                .retrieve()
                .body(KnowledgeDocumentResponse.class);
        if (response == null) {
            throw new IllegalStateException("AI 知识库服务返回了空结果");
        }
        return response;
    }

    public List<KnowledgeSearchResult> search(
            KnowledgeSearchRequest request) {
        List<KnowledgeSearchResult> response = restClient.post()
                .uri("/api/knowledge/search")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
                });
        return response == null ? List.of() : response;
    }

    public KnowledgeDeleteResponse delete(String documentId) {
        KnowledgeDeleteResponse response = restClient.delete()
                .uri("/api/knowledge/documents/{documentId}", documentId)
                .retrieve()
                .body(KnowledgeDeleteResponse.class);
        if (response == null) {
            throw new IllegalStateException("AI 知识库服务返回了空结果");
        }
        return response;
    }

    private MediaType resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType == null
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(contentType);
    }

    private static final class NamedByteArrayResource
            extends ByteArrayResource {

        private final String filename;

        private NamedByteArrayResource(byte[] bytes, String filename) {
            super(bytes);
            this.filename = filename == null ? "unnamed.txt" : filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }
}
