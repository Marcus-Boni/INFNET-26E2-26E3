package com.infnet.tp2.controller;

import com.infnet.tp2.domain.model.AuditLog;
import com.infnet.tp2.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllHistory());
    }

    @GetMapping("/entity/{entityName}")
    public ResponseEntity<List<AuditLog>> getLogsByEntityName(@PathVariable String entityName) {
        return ResponseEntity.ok(auditLogService.getHistoryForEntityName(entityName));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<List<AuditLog>> getLogsByEntity(@PathVariable String entityName, @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getHistoryForEntity(entityName, entityId));
    }
}
