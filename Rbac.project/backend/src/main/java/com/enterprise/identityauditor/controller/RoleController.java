package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.dto.ApiResponse;
import com.enterprise.identityauditor.dto.RoleDto;
import com.enterprise.identityauditor.dto.RoleRequest;
import com.enterprise.identityauditor.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER') or hasAuthority('ROLE_ASSIGN')")
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR')")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        return ResponseEntity.ok(roleService.getRoleById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleRequest request) {
        RoleDto created = roleService.createRole(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(roleService.updateRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<ApiResponse> deleteRole(@PathVariable Long id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new ApiResponse(true, "Role deleted successfully"));
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN') or hasAuthority('PERMISSION_MANAGE')")
    public ResponseEntity<RoleDto> assignPermissionsToRole(@PathVariable Long id, @RequestBody Set<Long> permissionIds) {
        return ResponseEntity.ok(roleService.assignPermissionsToRole(id, permissionIds));
    }
}
