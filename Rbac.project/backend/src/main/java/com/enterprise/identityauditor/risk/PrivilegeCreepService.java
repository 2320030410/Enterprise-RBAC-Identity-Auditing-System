package com.enterprise.identityauditor.risk;

import com.enterprise.identityauditor.dto.PrivilegeReviewDto;
import com.enterprise.identityauditor.dto.RiskAssessmentDto;
import com.enterprise.identityauditor.entity.*;
import com.enterprise.identityauditor.repository.AuditLogRepository;
import com.enterprise.identityauditor.repository.PrivilegeReviewRepository;
import com.enterprise.identityauditor.repository.RiskAssessmentRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PrivilegeCreepService {

    private static final Logger logger = LoggerFactory.getLogger(PrivilegeCreepService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private PrivilegeReviewRepository privilegeReviewRepository;

    @Value("${identity.auditor.privilege-creep.inactivity-threshold-days:30}")
    private int inactivityThresholdDays;

    // Mapping between elevated permissions and corresponding audit actions
    private static final Map<String, List<String>> PERMISSION_AUDIT_ACTION_MAP = Map.of(
            "SYSTEM_ADMIN", List.of("SYSTEM_ADMIN", "SYSTEM_CONFIG", "RISK_SCAN"),
            "USER_DELETE", List.of("USER_DELETE"),
            "USER_CREATE", List.of("USER_CREATE"),
            "USER_UPDATE", List.of("USER_UPDATE"),
            "ROLE_ASSIGN", List.of("ROLE_ASSIGN"),
            "ROLE_REVOKE", List.of("ROLE_REVOKE"),
            "PERMISSION_MANAGE", List.of("PERMISSION_MANAGE", "ROLE_CREATE", "ROLE_UPDATE", "ROLE_DELETE"),
            "REPORT_GENERATE", List.of("REPORT_GENERATE"),
            "AUDIT_VIEW", List.of("AUDIT_VIEW")
    );

    @Scheduled(cron = "${identity.auditor.privilege-creep.cron:0 0 0 * * ?}")
    public void runScheduledPrivilegeScan() {
        logger.info("Starting scheduled privilege creep detection and risk scan...");
        runFullScan("SYSTEM_SCHEDULER");
    }

    @Transactional
    public List<RiskAssessmentDto> runFullScan(String triggeredBy) {
        logger.info("Executing comprehensive privilege creep analysis triggered by: {}", triggeredBy);
        List<User> users = userRepository.findAll();
        List<RiskAssessment> assessments = new ArrayList<>();

        for (User user : users) {
            RiskAssessment assessment = analyzeUserPrivilegeCreep(user, triggeredBy);
            assessments.add(assessment);
        }

        return assessments.stream()
                .sorted(Comparator.comparingInt(RiskAssessment::getRiskScore).reversed())
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public RiskAssessment analyzeUserPrivilegeCreep(User user, String triggeredBy) {
        // 1. Gather all unique permissions and identify elevated ones
        Set<Permission> allPermissions = new HashSet<>();
        Map<String, String> permissionToRoleMap = new HashMap<>();

        for (Role role : user.getRoles()) {
            for (Permission p : role.getPermissions()) {
                allPermissions.add(p);
                permissionToRoleMap.putIfAbsent(p.getName(), role.getName());
            }
        }

        List<Permission> elevatedPermissions = allPermissions.stream()
                .filter(Permission::isElevated)
                .collect(Collectors.toList());

        List<String> unusedElevatedPermDetails = new ArrayList<>();
        int unusedCount = 0;
        LocalDateTime mostRecentPrivilegedActivity = null;

        // 2. Audit history analysis for each elevated permission
        for (Permission perm : elevatedPermissions) {
            List<String> correspondingActions = PERMISSION_AUDIT_ACTION_MAP.getOrDefault(perm.getName(), List.of(perm.getName()));
            AuditLog latestLog = null;

            for (String action : correspondingActions) {
                Optional<AuditLog> logOpt = auditLogRepository.findTop1ByUserIdAndActionAndStatusOrderByTimestampDesc(
                        user.getId(), action, "SUCCESS");
                if (logOpt.isPresent()) {
                    if (latestLog == null || logOpt.get().getTimestamp().isAfter(latestLog.getTimestamp())) {
                        latestLog = logOpt.get();
                    }
                }
            }

            int daysInactive;
            boolean neverUsed = false;

            if (latestLog != null) {
                daysInactive = (int) ChronoUnit.DAYS.between(latestLog.getTimestamp(), LocalDateTime.now());
                if (mostRecentPrivilegedActivity == null || latestLog.getTimestamp().isAfter(mostRecentPrivilegedActivity)) {
                    mostRecentPrivilegedActivity = latestLog.getTimestamp();
                }
            } else {
                // Never used! Calculate days since account creation
                LocalDateTime baselineDate = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now().minusDays(45);
                daysInactive = (int) ChronoUnit.DAYS.between(baselineDate, LocalDateTime.now());
                neverUsed = true;
            }

            // Flag if inactivity exceeds threshold (30 days)
            if (daysInactive >= inactivityThresholdDays) {
                unusedCount++;
                String statusNote = neverUsed ?
                        String.format("%s (Never used since assignment %d days ago)", perm.getName(), daysInactive) :
                        String.format("%s (Inactive for %d days)", perm.getName(), daysInactive);
                unusedElevatedPermDetails.add(statusNote);

                // Create or update PrivilegeReview item
                createOrUpdatePrivilegeReview(user, perm.getName(),
                        permissionToRoleMap.get(perm.getName()), daysInactive, neverUsed);
            }
        }

        // 3. Calculate Overall Days Since Last Privileged Activity
        Integer overallDaysInactive = null;
        if (mostRecentPrivilegedActivity != null) {
            overallDaysInactive = (int) ChronoUnit.DAYS.between(mostRecentPrivilegedActivity, LocalDateTime.now());
        } else if (!elevatedPermissions.isEmpty()) {
            LocalDateTime baseline = user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now().minusDays(45);
            overallDaysInactive = (int) ChronoUnit.DAYS.between(baseline, LocalDateTime.now());
        }

        // 4. Calculate Risk Score (0 - 100)
        RiskScoreCalculation scoreCalc = calculateRiskScore(user, elevatedPermissions.size(), unusedCount, overallDaysInactive);

        // 5. Save or update RiskAssessment record
        RiskAssessment assessment = riskAssessmentRepository.findByUserId(user.getId())
                .orElse(new RiskAssessment());

        assessment.setUserId(user.getId());
        assessment.setUsername(user.getUsername());
        assessment.setFullName(user.getFullName());
        assessment.setDepartment(user.getDepartment());
        assessment.setRiskScore(scoreCalc.score);
        assessment.setRiskClassification(scoreCalc.classification);
        assessment.setUnusedElevatedPermissionsCount(unusedCount);
        assessment.setUnusedPermissions(String.join(", ", unusedElevatedPermDetails));
        assessment.setLastPrivilegedActivity(mostRecentPrivilegedActivity);
        assessment.setDaysSinceLastPrivilegedActivity(overallDaysInactive);
        assessment.setRiskExplanation(scoreCalc.explanation);
        assessment.setRecommendedAction(scoreCalc.recommendation);
        assessment.setAssessedAt(LocalDateTime.now());

        return riskAssessmentRepository.save(assessment);
    }

    private void createOrUpdatePrivilegeReview(User user, String permissionName, String roleName, int daysInactive, boolean neverUsed) {
        boolean alreadyPending = privilegeReviewRepository.existsByUserIdAndPermissionNameAndStatus(
                user.getId(), permissionName, "PENDING");

        if (!alreadyPending) {
            PrivilegeReview review = new PrivilegeReview();
            review.setUserId(user.getId());
            review.setUsername(user.getUsername());
            review.setPermissionName(permissionName);
            review.setAssociatedRoleName(roleName != null ? roleName : "CUSTOM");
            review.setInactivityDays(daysInactive);
            review.setStatus("PENDING");
            review.setFlaggedReason(neverUsed ?
                    "Permission has NEVER been exercised since account creation (" + daysInactive + " days)" :
                    "Elevated permission has not been utilized for " + daysInactive + " consecutive days");
            review.setRecommendedAction("Revoke elevated permission or unassign role '" +
                    roleName + "' to enforce Principle of Least Privilege (PoLP)");

            privilegeReviewRepository.save(review);
        }
    }

    private RiskScoreCalculation calculateRiskScore(User user, int elevatedPermCount, int unusedElevatedCount, Integer daysInactive) {
        int score = 0;
        List<String> factorList = new ArrayList<>();

        // Factor 1: Role privilege level
        boolean isSuperAdmin = user.getRoles().stream().anyMatch(r -> r.getName().contains("SUPER_ADMIN"));
        boolean isAdmin = user.getRoles().stream().anyMatch(r -> r.getName().contains("ADMIN"));
        boolean isManager = user.getRoles().stream().anyMatch(r -> r.getName().contains("MANAGER"));
        boolean isAuditor = user.getRoles().stream().anyMatch(r -> r.getName().contains("AUDITOR"));

        if (isSuperAdmin) {
            score += 25;
            factorList.add("+25 pts: Holds Super Admin system authority");
        } else if (isAdmin) {
            score += 20;
            factorList.add("+20 pts: Holds Administrative authority");
        } else if (isManager || isAuditor) {
            score += 10;
            factorList.add("+10 pts: Holds elevated operational role");
        }

        // Factor 2: Elevated permissions volume
        if (elevatedPermCount > 0) {
            int elevatedWeight = Math.min(20, elevatedPermCount * 4);
            score += elevatedWeight;
            factorList.add(String.format("+%d pts: Assigned %d elevated permissions", elevatedWeight, elevatedPermCount));
        }

        // Factor 3: Inactivity duration of privileged operations
        if (daysInactive != null) {
            if (daysInactive >= 90) {
                score += 30;
                factorList.add("+30 pts: Extended privileged inactivity (>= 90 days)");
            } else if (daysInactive >= 60) {
                score += 20;
                factorList.add("+20 pts: Moderate privileged inactivity (>= 60 days)");
            } else if (daysInactive >= 30) {
                score += 15;
                factorList.add("+15 pts: Privileged inactivity threshold breached (>= 30 days)");
            }
        }

        // Factor 4: Number of unused elevated permissions (Direct privilege creep)
        if (unusedElevatedCount > 0) {
            int creepWeight = Math.min(35, unusedElevatedCount * 12);
            score += creepWeight;
            factorList.add(String.format("+%d pts: Detected %d dormant elevated privileges (Privilege Creep)", creepWeight, unusedElevatedCount));
        }

        // Factor 5: Disabled account anomaly
        if (!user.isEnabled() && elevatedPermCount > 0) {
            score += 15;
            factorList.add("+15 pts: Inactive account retains privileged access");
        }

        // Clamp to 0 - 100
        int finalScore = Math.min(100, Math.max(0, score));

        // Risk Classification
        String classification;
        if (finalScore >= 80) {
            classification = "CRITICAL";
        } else if (finalScore >= 60) {
            classification = "HIGH";
        } else if (finalScore >= 30) {
            classification = "MEDIUM";
        } else {
            classification = "LOW";
        }

        String explanation = String.join(" | ", factorList);
        if (factorList.isEmpty()) {
            explanation = "Normal low-privilege employee profile with no dormant elevated permissions.";
        }

        String recommendation;
        if ("CRITICAL".equals(classification) || "HIGH".equals(classification)) {
            recommendation = "Immediate Action Required: Revoke dormant administrative privileges via Privilege Review panel. Align account to Principle of Least Privilege.";
        } else if ("MEDIUM".equals(classification)) {
            recommendation = "Review scheduled: Audit dormant permissions and verify business necessity with department manager.";
        } else {
            recommendation = "Compliant: User permissions match operational activity. Maintain routine monitoring.";
        }

        return new RiskScoreCalculation(finalScore, classification, explanation, recommendation);
    }

    public RiskAssessmentDto mapToDto(RiskAssessment entity) {
        RiskAssessmentDto dto = new RiskAssessmentDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setUsername(entity.getUsername());
        dto.setFullName(entity.getFullName());
        dto.setDepartment(entity.getDepartment());
        dto.setRiskScore(entity.getRiskScore());
        dto.setRiskClassification(entity.getRiskClassification());
        dto.setUnusedElevatedPermissionsCount(entity.getUnusedElevatedPermissionsCount());
        dto.setUnusedPermissions(entity.getUnusedPermissions());
        dto.setLastPrivilegedActivity(entity.getLastPrivilegedActivity());
        dto.setDaysSinceLastPrivilegedActivity(entity.getDaysSinceLastPrivilegedActivity());
        dto.setRiskExplanation(entity.getRiskExplanation());
        dto.setRecommendedAction(entity.getRecommendedAction());
        dto.setReviewed(entity.isReviewed());
        dto.setReviewedBy(entity.getReviewedBy());
        dto.setReviewedAt(entity.getReviewedAt());
        dto.setAssessedAt(entity.getAssessedAt());
        return dto;
    }

    private static class RiskScoreCalculation {
        final int score;
        final String classification;
        final String explanation;
        final String recommendation;

        RiskScoreCalculation(int score, String classification, String explanation, String recommendation) {
            this.score = score;
            this.classification = classification;
            this.explanation = explanation;
            this.recommendation = recommendation;
        }
    }
}
