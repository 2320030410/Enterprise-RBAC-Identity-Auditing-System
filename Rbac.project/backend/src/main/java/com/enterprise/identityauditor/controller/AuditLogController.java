package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.audit.AuditLogService;
import com.enterprise.identityauditor.dto.AuditLogDto;
import com.enterprise.identityauditor.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('AUDIT_VIEW')")
    public ResponseEntity<List<AuditLogDto>> getAuditLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(auditLogService.searchLogs(username, action, status, startDate, endDate));
    }

    @GetMapping("/actions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('AUDIT_VIEW')")
    public ResponseEntity<List<String>> getDistinctActions() {
        return ResponseEntity.ok(auditLogService.getDistinctActions());
    }

    @GetMapping("/my-history")
    public ResponseEntity<List<AuditLogDto>> getMyAuditHistory() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return ResponseEntity.ok(auditLogService.getUserLogs(userDetails.getId()));
        }
        return ResponseEntity.ok(List.of());
    }
}
