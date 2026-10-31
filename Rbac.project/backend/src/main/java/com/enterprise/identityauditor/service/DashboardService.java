package com.enterprise.identityauditor.service;

import com.enterprise.identityauditor.audit.AuditLogService;
import com.enterprise.identityauditor.dto.AuditLogDto;
import com.enterprise.identityauditor.dto.DashboardStatsDto;
import com.enterprise.identityauditor.dto.RiskAssessmentDto;
import com.enterprise.identityauditor.entity.RiskAssessment;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.repository.*;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private PrivilegeReviewRepository privilegeReviewRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @Transactional(readOnly = true)
    public DashboardStatsDto getDashboardStats() {
        DashboardStatsDto stats = new DashboardStatsDto();

        List<User> allUsers = userRepository.findAll();
        List<Role> allRoles = roleRepository.findAll();
        List<RiskAssessment> allAssessments = riskAssessmentRepository.findAllByOrderByRiskScoreDesc();

        long totalUsers = allUsers.size();
        long activeUsers = allUsers.stream().filter(User::isEnabled).count();
        long totalRoles = allRoles.size();
        long totalPermissions = permissionRepository.count();

        // Privileged users (users who hold any elevated permissions or admin/manager roles)
        long privilegedUsers = allUsers.stream().filter(u ->
                u.getRoles().stream().anyMatch(r ->
                        r.getName().contains("ADMIN") || r.getName().contains("MANAGER") || r.getName().contains("AUDITOR") ||
                        r.getPermissions().stream().anyMatch(com.enterprise.identityauditor.entity.Permission::isElevated)
                )
        ).count();

        long highRiskCount = allAssessments.stream().filter(a -> "HIGH".equalsIgnoreCase(a.getRiskClassification())).count();
        long criticalRiskCount = allAssessments.stream().filter(a -> "CRITICAL".equalsIgnoreCase(a.getRiskClassification())).count();

        long unusedElevatedCount = allAssessments.stream()
                .mapToInt(RiskAssessment::getUnusedElevatedPermissionsCount)
                .sum();

        long pendingReviews = privilegeReviewRepository.countByStatus("PENDING");

        stats.setTotalUsers(totalUsers);
        stats.setActiveUsers(activeUsers);
        stats.setTotalRoles(totalRoles);
        stats.setTotalPermissions(totalPermissions);
        stats.setPrivilegedAccounts(privilegedUsers);
        stats.setUnusedElevatedPermissions(unusedElevatedCount);
        stats.setHighRiskUsers(highRiskCount);
        stats.setCriticalRiskUsers(criticalRiskCount);
        stats.setPendingPrivilegeReviews(pendingReviews);

        // Chart 1: Users by Role
        Map<String, Long> usersByRole = new LinkedHashMap<>();
        for (Role role : allRoles) {
            long count = allUsers.stream().filter(u -> u.getRoles().contains(role)).count();
            usersByRole.put(role.getName().replace("ROLE_", ""), count);
        }
        stats.setUsersByRole(usersByRole);

        // Chart 2: Risk Classification Distribution
        Map<String, Long> riskDist = new LinkedHashMap<>();
        riskDist.put("LOW", allAssessments.stream().filter(a -> "LOW".equalsIgnoreCase(a.getRiskClassification())).count());
        riskDist.put("MEDIUM", allAssessments.stream().filter(a -> "MEDIUM".equalsIgnoreCase(a.getRiskClassification())).count());
        riskDist.put("HIGH", highRiskCount);
        riskDist.put("CRITICAL", criticalRiskCount);
        stats.setRiskDistribution(riskDist);

        // Chart 3: Privileged vs Regular Users
        Map<String, Long> privVsReg = new LinkedHashMap<>();
        privVsReg.put("Privileged", privilegedUsers);
        privVsReg.put("Standard Employee", Math.max(0, totalUsers - privilegedUsers));
        stats.setPrivilegedVsRegular(privVsReg);

        // Chart 4: Unused Permissions breakdown
        Map<String, Long> unusedSummary = new HashMap<>();
        for (RiskAssessment assessment : allAssessments) {
            if (assessment.getUnusedPermissions() != null && !assessment.getUnusedPermissions().isEmpty()) {
                String[] items = assessment.getUnusedPermissions().split(", ");
                for (String item : items) {
                    String cleanName = item.split(" ")[0].trim();
                    if (!cleanName.isEmpty()) {
                        unusedSummary.put(cleanName, unusedSummary.getOrDefault(cleanName, 0L) + 1L);
                    }
                }
            }
        }
        stats.setUnusedPermissionsSummary(unusedSummary);

        // Recent Audit Activity
        List<AuditLogDto> recentLogs = auditLogService.getRecentLogs();
        stats.setRecentAuditLogs(recentLogs);

        // High & Critical Risk Alerts
        List<RiskAssessmentDto> alerts = allAssessments.stream()
                .filter(a -> "HIGH".equalsIgnoreCase(a.getRiskClassification()) || "CRITICAL".equalsIgnoreCase(a.getRiskClassification()))
                .map(privilegeCreepService::mapToDto)
                .collect(Collectors.toList());
        stats.setHighRiskAlerts(alerts);

        return stats;
    }
}
