package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.audit.AuditLogService;
import com.enterprise.identityauditor.dto.AuditLogDto;
import com.enterprise.identityauditor.dto.DashboardStatsDto;
import com.enterprise.identityauditor.dto.RiskAssessmentDto;
import com.enterprise.identityauditor.dto.UserDto;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import com.enterprise.identityauditor.security.CustomUserDetails;
import com.enterprise.identityauditor.service.DashboardService;
import com.enterprise.identityauditor.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER')")
    public ResponseEntity<DashboardStatsDto> getDashboardStats() {
        return ResponseEntity.ok(dashboardService.getDashboardStats());
    }

    @GetMapping("/employee")
    public ResponseEntity<Map<String, Object>> getEmployeeDashboard() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return ResponseEntity.status(401).build();
        }

        UserDto user = userService.getUserById(userDetails.getId());
        List<AuditLogDto> personalLogs = auditLogService.getUserLogs(userDetails.getId());

        // Get personal risk assessment
        RiskAssessmentDto risk = null;
        try {
            User userEntity = userService.getUserEntityById(userDetails.getId());
            var entity = privilegeCreepService.analyzeUserPrivilegeCreep(userEntity, "EMPLOYEE_DASHBOARD_VIEW");
            risk = privilegeCreepService.mapToDto(entity);
        } catch (Exception ignored) {
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("assignedRoles", user.getRoles());
        data.put("assignedPermissions", user.getPermissions());
        data.put("personalActivity", personalLogs);
        data.put("securityStatus", risk != null ? risk.getRiskClassification() : "LOW");
        data.put("riskScore", risk != null ? risk.getRiskScore() : 10);
        data.put("lastLogin", user.getLastLoginAt());

        return ResponseEntity.ok(data);
    }
}
