package com.enterprise.identityauditor.dto;

import java.time.LocalDateTime;

public class RiskAssessmentDto {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String department;
    private int riskScore;
    private String riskClassification;
    private int unusedElevatedPermissionsCount;
    private String unusedPermissions;
    private LocalDateTime lastPrivilegedActivity;
    private Integer daysSinceLastPrivilegedActivity;
    private String riskExplanation;
    private String recommendedAction;
    private boolean reviewed;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private LocalDateTime assessedAt;

    public RiskAssessmentDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(int riskScore) {
        this.riskScore = riskScore;
    }

    public String getRiskClassification() {
        return riskClassification;
    }

    public void setRiskClassification(String riskClassification) {
        this.riskClassification = riskClassification;
    }

    public int getUnusedElevatedPermissionsCount() {
        return unusedElevatedPermissionsCount;
    }

    public void setUnusedElevatedPermissionsCount(int unusedElevatedPermissionsCount) {
        this.unusedElevatedPermissionsCount = unusedElevatedPermissionsCount;
    }

    public String getUnusedPermissions() {
        return unusedPermissions;
    }

    public void setUnusedPermissions(String unusedPermissions) {
        this.unusedPermissions = unusedPermissions;
    }

    public LocalDateTime getLastPrivilegedActivity() {
        return lastPrivilegedActivity;
    }

    public void setLastPrivilegedActivity(LocalDateTime lastPrivilegedActivity) {
        this.lastPrivilegedActivity = lastPrivilegedActivity;
    }

    public Integer getDaysSinceLastPrivilegedActivity() {
        return daysSinceLastPrivilegedActivity;
    }

    public void setDaysSinceLastPrivilegedActivity(Integer daysSinceLastPrivilegedActivity) {
        this.daysSinceLastPrivilegedActivity = daysSinceLastPrivilegedActivity;
    }

    public String getRiskExplanation() {
        return riskExplanation;
    }

    public void setRiskExplanation(String riskExplanation) {
        this.riskExplanation = riskExplanation;
    }

    public String getRecommendedAction() {
        return recommendedAction;
    }

    public void setRecommendedAction(String recommendedAction) {
        this.recommendedAction = recommendedAction;
    }

    public boolean isReviewed() {
        return reviewed;
    }

    public void setReviewed(boolean reviewed) {
        this.reviewed = reviewed;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getAssessedAt() {
        return assessedAt;
    }

    public void setAssessedAt(LocalDateTime assessedAt) {
        this.assessedAt = assessedAt;
    }
}
