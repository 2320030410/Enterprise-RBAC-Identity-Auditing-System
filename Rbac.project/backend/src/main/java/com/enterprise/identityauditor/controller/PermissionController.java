package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.dto.PermissionDto;
import com.enterprise.identityauditor.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
public class PermissionController {

    @Autowired
    private PermissionService permissionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER') or hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<List<PermissionDto>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.getAllPermissions());
    }

    @GetMapping("/elevated")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER')")
    public ResponseEntity<List<PermissionDto>> getElevatedPermissions() {
        return ResponseEntity.ok(permissionService.getElevatedPermissions());
    }
}
