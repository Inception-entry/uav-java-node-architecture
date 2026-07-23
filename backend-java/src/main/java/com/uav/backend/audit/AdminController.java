package com.uav.backend.audit;

import com.uav.backend.audit.dto.AdminOverviewResponse;
import com.uav.backend.audit.dto.AuditLogPageResponse;
import com.uav.backend.audit.service.AuditLogService;
import com.uav.backend.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AuditLogService auditLogService;

    public AdminController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/overview")
    public ApiResponse<AdminOverviewResponse> overview() {
        return ApiResponse.ok(auditLogService.overview());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<AuditLogPageResponse> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false) String username) {
        return ApiResponse.ok(auditLogService.search(
                page,
                size,
                action,
                outcome,
                username
        ));
    }
}
