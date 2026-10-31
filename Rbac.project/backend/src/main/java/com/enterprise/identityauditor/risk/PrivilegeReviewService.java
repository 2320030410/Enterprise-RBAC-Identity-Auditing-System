package com.enterprise.identityauditor.risk;

import com.enterprise.identityauditor.audit.AuditLogService;
import com.enterprise.identityauditor.dto.PrivilegeReviewDto;
import com.enterprise.identityauditor.entity.PrivilegeReview;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.exception.BadRequestException;
import com.enterprise.identityauditor.exception.ResourceNotFoundException;
import com.enterprise.identityauditor.repository.PrivilegeReviewRepository;
import com.enterprise.identityauditor.repository.RoleRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrivilegeReviewService {

    @Autowired
    private PrivilegeReviewRepository privilegeReviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<PrivilegeReviewDto> getAllReviews(String status) {
        List<PrivilegeReview> reviews;
        if (status != null && !status.isEmpty()) {
            reviews = privilegeReviewRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            reviews = privilegeReviewRepository.findAllByOrderByCreatedAtDesc();
        }
        return reviews.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrivilegeReviewDto> getUserReviews(Long userId) {
        return privilegeReviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public PrivilegeReviewDto approveRevocation(Long reviewId, String adminUsername, String notes) {
        PrivilegeReview review = privilegeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege review not found with ID: " + reviewId));

        if (!"PENDING".equalsIgnoreCase(review.getStatus())) {
            throw new BadRequestException("Review has already been processed with status: " + review.getStatus());
        }

        User user = userRepository.findById(review.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + review.getUserId()));

        // Remove the associated role or permission from user to mitigate privilege creep
        String roleName = review.getAssociatedRoleName();
        if (roleName != null && !roleName.isEmpty()) {
            boolean removed = user.getRoles().removeIf(r -> r.getName().equalsIgnoreCase(roleName));
            if (removed) {
                userRepository.save(user);
            }
        }

        // Update review record
        review.setStatus("APPROVED_REVOKED");
        review.setReviewedBy(adminUsername);
        review.setReviewNotes(notes != null && !notes.isEmpty() ? notes : "Approved privilege revocation due to prolonged inactivity.");
        review.setUpdatedAt(LocalDateTime.now());
        PrivilegeReview savedReview = privilegeReviewRepository.save(review);

        // Re-assess user risk immediately after revocation
        privilegeCreepService.analyzeUserPrivilegeCreep(user, "PRIVILEGE_REVOCATION_APPROVAL");

        // Record audit log
        auditLogService.recordLog(
                user.getId(),
                adminUsername,
                "PRIVILEGE_REVOKE_APPROVED",
                "Approved revocation of dormant privilege '" + review.getPermissionName() + "' (Role: " + roleName + ") for user " + user.getUsername(),
                "/api/privilege-reviews/" + reviewId + "/approve",
                "SUCCESS",
                "127.0.0.1",
                "Notes: " + review.getReviewNotes()
        );

        return mapToDto(savedReview);
    }

    @Transactional
    public PrivilegeReviewDto rejectRevocation(Long reviewId, String adminUsername, String notes) {
        PrivilegeReview review = privilegeReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Privilege review not found with ID: " + reviewId));

        if (!"PENDING".equalsIgnoreCase(review.getStatus())) {
            throw new BadRequestException("Review has already been processed with status: " + review.getStatus());
        }

        review.setStatus("REJECTED_RETAINED");
        review.setReviewedBy(adminUsername);
        review.setReviewNotes(notes != null && !notes.isEmpty() ? notes : "Privilege retained by administrator discretion with business justification.");
        review.setUpdatedAt(LocalDateTime.now());
        PrivilegeReview savedReview = privilegeReviewRepository.save(review);

        // Record audit log
        auditLogService.recordLog(
                review.getUserId(),
                adminUsername,
                "PRIVILEGE_REVOKE_REJECTED",
                "Rejected revocation of privilege '" + review.getPermissionName() + "' for user " + review.getUsername() + " (Privilege retained)",
                "/api/privilege-reviews/" + reviewId + "/reject",
                "SUCCESS",
                "127.0.0.1",
                "Justification: " + review.getReviewNotes()
        );

        return mapToDto(savedReview);
    }

    public PrivilegeReviewDto mapToDto(PrivilegeReview review) {
        PrivilegeReviewDto dto = new PrivilegeReviewDto();
        dto.setId(review.getId());
        dto.setUserId(review.getUserId());
        dto.setUsername(review.getUsername());
        dto.setPermissionName(review.getPermissionName());
        dto.setAssociatedRoleName(review.getAssociatedRoleName());
        dto.setInactivityDays(review.getInactivityDays());
        dto.setFlaggedReason(review.getFlaggedReason());
        dto.setRecommendedAction(review.getRecommendedAction());
        dto.setStatus(review.getStatus());
        dto.setReviewedBy(review.getReviewedBy());
        dto.setReviewNotes(review.getReviewNotes());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }
}
