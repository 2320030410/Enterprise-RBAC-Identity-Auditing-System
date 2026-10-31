package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.dto.PrivilegeReviewActionRequest;
import com.enterprise.identityauditor.dto.PrivilegeReviewDto;
import com.enterprise.identityauditor.risk.PrivilegeReviewService;
import com.enterprise.identityauditor.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/privilege-reviews")
public class PrivilegeReviewController {

    @Autowired
    private PrivilegeReviewService privilegeReviewService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER')")
    public ResponseEntity<List<PrivilegeReviewDto>> getAllReviews(@RequestParam(required = false) String status) {
        return ResponseEntity.ok(privilegeReviewService.getAllReviews(status));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or @userSecurity.isCurrentUser(authentication, #userId)")
    public ResponseEntity<List<PrivilegeReviewDto>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(privilegeReviewService.getUserReviews(userId));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_REVOKE')")
    public ResponseEntity<PrivilegeReviewDto> approveRevocation(
            @PathVariable Long id,
            @RequestBody(required = false) PrivilegeReviewActionRequest request) {
        String adminUsername = getCurrentUsername();
        String notes = request != null ? request.getReviewNotes() : "";
        PrivilegeReviewDto result = privilegeReviewService.approveRevocation(id, adminUsername, notes);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('ROLE_REVOKE')")
    public ResponseEntity<PrivilegeReviewDto> rejectRevocation(
            @PathVariable Long id,
            @RequestBody(required = false) PrivilegeReviewActionRequest request) {
        String adminUsername = getCurrentUsername();
        String notes = request != null ? request.getReviewNotes() : "";
        PrivilegeReviewDto result = privilegeReviewService.rejectRevocation(id, adminUsername, notes);
        return ResponseEntity.ok(result);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }
        return "ADMIN";
    }
}
