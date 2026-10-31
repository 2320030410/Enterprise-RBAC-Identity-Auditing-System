package com.enterprise.identityauditor.service;

import com.enterprise.identityauditor.audit.Auditable;
import com.enterprise.identityauditor.dto.PermissionDto;
import com.enterprise.identityauditor.dto.RoleDto;
import com.enterprise.identityauditor.dto.RoleRequest;
import com.enterprise.identityauditor.entity.Permission;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.exception.BadRequestException;
import com.enterprise.identityauditor.exception.ResourceNotFoundException;
import com.enterprise.identityauditor.repository.PermissionRepository;
import com.enterprise.identityauditor.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));
        return mapToDto(role);
    }

    @Transactional
    @Auditable(action = "ROLE_CREATE", description = "Created new security role", resource = "/api/roles")
    public RoleDto createRole(RoleRequest request) {
        String roleName = request.getName().trim().toUpperCase();
        if (!roleName.startsWith("ROLE_")) {
            roleName = "ROLE_" + roleName;
        }

        if (roleRepository.existsByName(roleName)) {
            throw new BadRequestException("Role name already exists: " + roleName);
        }

        Role role = new Role(roleName, request.getDescription(), false);

        if (request.getPermissionIds() != null && !request.getPermissionIds().isEmpty()) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(perms);
        }

        Role saved = roleRepository.save(role);
        return mapToDto(saved);
    }

    @Transactional
    @Auditable(action = "ROLE_UPDATE", description = "Updated role and permissions", resource = "/api/roles/{id}")
    public RoleDto updateRole(Long id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        if (role.isSystemRole()) {
            // Cannot rename system role, but can adjust permissions
            role.setDescription(request.getDescription());
        } else {
            String roleName = request.getName().trim().toUpperCase();
            if (!roleName.startsWith("ROLE_")) {
                roleName = "ROLE_" + roleName;
            }
            if (!role.getName().equalsIgnoreCase(roleName) && roleRepository.existsByName(roleName)) {
                throw new BadRequestException("Role name already exists: " + roleName);
            }
            role.setName(roleName);
            role.setDescription(request.getDescription());
        }

        if (request.getPermissionIds() != null) {
            Set<Permission> perms = new HashSet<>(permissionRepository.findAllById(request.getPermissionIds()));
            role.setPermissions(perms);
        }

        Role saved = roleRepository.save(role);
        return mapToDto(saved);
    }

    @Transactional
    @Auditable(action = "ROLE_DELETE", description = "Deleted security role", resource = "/api/roles/{id}")
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + id));

        if (role.isSystemRole()) {
            throw new BadRequestException("System roles cannot be deleted");
        }

        roleRepository.delete(role);
    }

    @Transactional
    @Auditable(action = "PERMISSION_ASSIGN_ROLE", description = "Updated permissions for role", resource = "/api/roles/{id}/permissions")
    public RoleDto assignPermissionsToRole(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        Set<Permission> permissions = new HashSet<>(permissionRepository.findAllById(permissionIds));
        role.setPermissions(permissions);

        Role saved = roleRepository.save(role);
        return mapToDto(saved);
    }

    public RoleDto mapToDto(Role role) {
        List<PermissionDto> permissionDtos = role.getPermissions().stream()
                .map(p -> new PermissionDto(p.getId(), p.getName(), p.getDescription(), p.getCategory(), p.isElevated()))
                .collect(Collectors.toList());

        return new RoleDto(role.getId(), role.getName(), role.getDescription(), role.isSystemRole(), permissionDtos);
    }
}
