package com.aiplatform.controller;

import com.aiplatform.model.UsageEvent;
import com.aiplatform.service.AuditLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final AuditLogService auditLog;

    public AuditController(AuditLogService auditLog) {
        this.auditLog = auditLog;
    }

    @GetMapping
    public List<UsageEvent> latest(@RequestParam String userId, @RequestParam(defaultValue = "20") int limit) {
        return auditLog.latestFor(userId, Math.min(Math.max(limit, 1), 100));
    }
}
