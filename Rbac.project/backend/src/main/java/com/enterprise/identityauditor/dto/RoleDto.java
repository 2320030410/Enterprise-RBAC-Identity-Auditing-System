package com.enterprise.identityauditor.dto;

import java.util.List;

public class RoleDto {
    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
    private List<PermissionDto> permissions;

    public RoleDto() {
    }

    public RoleDto(Long id, String name, String description, boolean systemRole, List<PermissionDto> permissions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.systemRole = systemRole;
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSystemRole() {
        return systemRole;
    }

    public void setSystemRole(boolean systemRole) {
        this.systemRole = systemRole;
    }

    public List<PermissionDto> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<PermissionDto> permissions) {
        this.permissions = permissions;
    }
}
