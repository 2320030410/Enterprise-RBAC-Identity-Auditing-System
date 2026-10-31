package com.enterprise.identityauditor.dto;

import java.util.List;
import java.util.Map;

public class DashboardStatsDto {
    private long totalUsers;
    private long activeUsers;
    private long totalRoles;
    private long totalPermissions;
    private long privilegedAccounts;
    private long unusedElevatedPermissions;
    private long highRiskUsers;
    private long criticalRiskUsers;
    private long pendingPrivilegeReviews;

    // Charts & breakdowns
    private Map<String, Long> usersByRole;
    private Map<String, Long> riskDistribution;
    private Map<String, Long> privilegedVsRegular;
    private Map<String, Long> unusedPermissionsSummary;

    private List<AuditLogDto> recentAuditLogs;
    private List<RiskAssessmentDto> highRiskAlerts;

    public DashboardStatsDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(long activeUsers) {
        this.activeUsers = activeUsers;
    }

    public long getTotalRoles() {
        return totalRoles;
    }

    public void setTotalRoles(long totalRoles) {
        this.totalRoles = totalRoles;
    }

    public long getTotalPermissions() {
        return totalPermissions;
    }

    public void setTotalPermissions(long totalPermissions) {
        this.totalPermissions = totalPermissions;
    }

    public long getPrivilegedAccounts() {
        return privilegedAccounts;
    }

    public void setPrivilegedAccounts(long privilegedAccounts) {
        this.privilegedAccounts = privilegedAccounts;
    }

    public long getUnusedElevatedPermissions() {
        return unusedElevatedPermissions;
    }

    public void setUnusedElevatedPermissions(long unusedElevatedPermissions) {
        this.unusedElevatedPermissions = unusedElevatedPermissions;
    }

    public long getHighRiskUsers() {
        return highRiskUsers;
    }

    public void setHighRiskUsers(long highRiskUsers) {
        this.highRiskUsers = highRiskUsers;
    }

    public long getCriticalRiskUsers() {
        return criticalRiskUsers;
    }

    public void setCriticalRiskUsers(long criticalRiskUsers) {
        this.criticalRiskUsers = criticalRiskUsers;
    }

    public long getPendingPrivilegeReviews() {
        return pendingPrivilegeReviews;
    }

    public void setPendingPrivilegeReviews(long pendingPrivilegeReviews) {
        this.pendingPrivilegeReviews = pendingPrivilegeReviews;
    }

    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }

    public Map<String, Long> getRiskDistribution() {
        return riskDistribution;
    }

    public void setRiskDistribution(Map<String, Long> riskDistribution) {
        this.riskDistribution = riskDistribution;
    }

    public Map<String, Long> getPrivilegedVsRegular() {
        return privilegedVsRegular;
    }

    public void setPrivilegedVsRegular(Map<String, Long> privilegedVsRegular) {
        this.privilegedVsRegular = privilegedVsRegular;
    }

    public Map<String, Long> getUnusedPermissionsSummary() {
        return unusedPermissionsSummary;
    }

    public void setUnusedPermissionsSummary(Map<String, Long> unusedPermissionsSummary) {
        this.unusedPermissionsSummary = unusedPermissionsSummary;
    }

    public List<AuditLogDto> getRecentAuditLogs() {
        return recentAuditLogs;
    }

    public void setRecentAuditLogs(List<AuditLogDto> recentAuditLogs) {
        this.recentAuditLogs = recentAuditLogs;
    }

    public List<RiskAssessmentDto> getHighRiskAlerts() {
        return highRiskAlerts;
    }

    public void setHighRiskAlerts(List<RiskAssessmentDto> highRiskAlerts) {
        this.highRiskAlerts = highRiskAlerts;
    }
}
