package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.dto.RiskAssessmentDto;
import com.enterprise.identityauditor.entity.RiskAssessment;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.exception.ResourceNotFoundException;
import com.enterprise.identityauditor.repository.RiskAssessmentRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import com.enterprise.identityauditor.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER')")
    public ResponseEntity<List<RiskAssessmentDto>> getRiskAssessments(
            @RequestParam(required = false) String classification,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String search) {
        List<RiskAssessment> list = riskAssessmentRepository.filterAssessments(classification, department, search);
        List<RiskAssessmentDto> dtos = list.stream().map(privilegeCreepService::mapToDto).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or @userSecurity.isCurrentUser(authentication, #userId)")
    public ResponseEntity<RiskAssessmentDto> getUserRisk(@PathVariable Long userId) {
        RiskAssessment assessment = riskAssessmentRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                    return privilegeCreepService.analyzeUserPrivilegeCreep(user, "ON_DEMAND_USER_QUERY");
                });
        return ResponseEntity.ok(privilegeCreepService.mapToDto(assessment));
    }

    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('SYSTEM_ADMIN')")
    public ResponseEntity<List<RiskAssessmentDto>> runManualScan() {
        String actor = "MANUAL_SCAN";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            actor = userDetails.getUsername();
        }
        List<RiskAssessmentDto> results = privilegeCreepService.runFullScan(actor);
        return ResponseEntity.ok(results);
    }
}
