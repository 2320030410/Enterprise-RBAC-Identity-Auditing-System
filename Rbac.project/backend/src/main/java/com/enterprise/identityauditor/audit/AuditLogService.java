package com.enterprise.identityauditor.audit;

import com.enterprise.identityauditor.dto.AuditLogDto;
import com.enterprise.identityauditor.entity.AuditLog;
import com.enterprise.identityauditor.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AuditLog recordLog(Long userId, String username, String action, String description,
                             String resourceAccessed, String status, String ipAddress, String details) {
        // Enforce safety: truncate and ensure no secrets
        String safeDetails = sanitize(details);
        String safeDescription = sanitize(description);

        AuditLog log = new AuditLog(
                userId,
                username != null ? username : "ANONYMOUS",
                action,
                safeDescription,
                resourceAccessed,
                status,
                ipAddress,
                LocalDateTime.now(),
                safeDetails
        );

        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> searchLogs(String username, String action, String status,
                                        LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.searchAuditLogs(username, action, status, startDate, endDate)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getRecentLogs() {
        return auditLogRepository.findTop15ByOrderByTimestampDesc()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogDto> getUserLogs(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctActions() {
        return auditLogRepository.findDistinctActions();
    }

    public AuditLogDto mapToDto(AuditLog log) {
        AuditLogDto dto = new AuditLogDto();
        dto.setId(log.getId());
        dto.setUserId(log.getUserId());
        dto.setUsername(log.getUsername());
        dto.setAction(log.getAction());
        dto.setDescription(log.getDescription());
        dto.setResourceAccessed(log.getResourceAccessed());
        dto.setStatus(log.getStatus());
        dto.setIpAddress(log.getIpAddress());
        dto.setTimestamp(log.getTimestamp());
        dto.setDetails(log.getDetails());
        return dto;
    }

    private String sanitize(String input) {
        if (input == null) return null;
        // Never store passwords or tokens in logs
        return input.replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"[PROTECTED]\"")
                    .replaceAll("(?i)\"token\"\\s*:\\s*\"[^\"]*\"", "\"token\":\"[PROTECTED]\"")
                    .replaceAll("(?i)Bearer\\s+[A-Za-z0-9-_.]+", "Bearer [PROTECTED]");
    }
}
