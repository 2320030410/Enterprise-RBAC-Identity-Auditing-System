package com.enterprise.identityauditor.service;

import com.enterprise.identityauditor.dto.PermissionDto;
import com.enterprise.identityauditor.entity.Permission;
import com.enterprise.identityauditor.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PermissionDto> getElevatedPermissions() {
        return permissionRepository.findByIsElevatedTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public PermissionDto mapToDto(Permission p) {
        return new PermissionDto(p.getId(), p.getName(), p.getDescription(), p.getCategory(), p.isElevated());
    }
}
