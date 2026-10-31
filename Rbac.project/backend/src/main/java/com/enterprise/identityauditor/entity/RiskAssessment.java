package com.enterprise.identityauditor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "risk_assessments", indexes = {
        @Index(name = "idx_risk_user_id", columnList = "user_id"),
        @Index(name = "idx_risk_classification", columnList = "risk_classification"),
        @Index(name = "idx_risk_score", columnList = "risk_score")
})
public class RiskAssessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(length = 100)
    private String department;

    @Column(name = "risk_score", nullable = false)
    private int riskScore; // 0 to 100

    @Column(name = "risk_classification", nullable = false, length = 20)
    private String riskClassification; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "unused_elevated_permissions_count", nullable = false)
    private int unusedElevatedPermissionsCount = 0;

    @Column(name = "unused_permissions", columnDefinition = "TEXT")
    private String unusedPermissions;

    @Column(name = "last_privileged_activity")
    private LocalDateTime lastPrivilegedActivity;

    @Column(name = "days_since_last_privileged_activity")
    private Integer daysSinceLastPrivilegedActivity;

    @Column(name = "risk_explanation", columnDefinition = "TEXT")
    private String riskExplanation;

    @Column(name = "recommended_action", columnDefinition = "TEXT")
    private String recommendedAction;

    @Column(nullable = false)
    private boolean reviewed = false;

    @Column(name = "reviewed_by", length = 50)
    private String reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "assessed_at", nullable = false)
    private LocalDateTime assessedAt;

    public RiskAssessment() {
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
