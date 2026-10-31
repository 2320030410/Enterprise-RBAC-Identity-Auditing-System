package com.enterprise.identityauditor.service;

import com.enterprise.identityauditor.audit.Auditable;
import com.enterprise.identityauditor.dto.RoleDto;
import com.enterprise.identityauditor.dto.UserCreateRequest;
import com.enterprise.identityauditor.dto.UserDto;
import com.enterprise.identityauditor.dto.UserUpdateRequest;
import com.enterprise.identityauditor.entity.Permission;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.exception.BadRequestException;
import com.enterprise.identityauditor.exception.ResourceNotFoundException;
import com.enterprise.identityauditor.repository.RoleRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserDto> searchUsers(String keyword, String department, Boolean enabled) {
        return userRepository.searchUsers(keyword, department, enabled).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = getUserEntityById(id);
        return mapToDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return mapToDto(user);
    }

    @Transactional
    @Auditable(action = "USER_CREATE", description = "Created new system user", resource = "/api/users")
    public UserDto createUser(UserCreateRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use: " + request.getEmail());
        }

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getFullName(),
                request.getDepartment(),
                request.getJobTitle()
        );
        user.setEnabled(request.isEnabled());

        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
                roles.add(role);
            }
        } else {
            // Default role is ROLE_EMPLOYEE
            Role defaultRole = roleRepository.findByName("ROLE_EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role ROLE_EMPLOYEE not found"));
            roles.add(defaultRole);
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Run initial risk calculation for newly created user
        privilegeCreepService.analyzeUserPrivilegeCreep(savedUser, "USER_CREATION");

        return mapToDto(savedUser);
    }

    @Transactional
    @Auditable(action = "USER_UPDATE", description = "Updated user profile details", resource = "/api/users/{id}")
    public UserDto updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Check email collision
        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already in use: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setDepartment(request.getDepartment());
        user.setJobTitle(request.getJobTitle());
        user.setEnabled(request.isEnabled());

        if (request.getNewPassword() != null && !request.getNewPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (request.getRoleIds() != null) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : request.getRoleIds()) {
                Role role = roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User saved = userRepository.save(user);
        privilegeCreepService.analyzeUserPrivilegeCreep(saved, "USER_UPDATE");

        return mapToDto(saved);
    }

    @Transactional
    @Auditable(action = "USER_DELETE", description = "Deleted system user account", resource = "/api/users/{id}")
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        // Prevent deleting root superadmin
        if ("superadmin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Cannot delete default root superadmin account");
        }

        userRepository.delete(user);
    }

    @Transactional
    @Auditable(action = "USER_STATUS_TOGGLE", description = "Toggled user active/inactive status", resource = "/api/users/{id}/toggle-status")
    public UserDto toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if ("superadmin".equalsIgnoreCase(user.getUsername())) {
            throw new BadRequestException("Cannot deactivate default root superadmin account");
        }

        user.setEnabled(!user.isEnabled());
        User saved = userRepository.save(user);
        privilegeCreepService.analyzeUserPrivilegeCreep(saved, "STATUS_TOGGLE");

        return mapToDto(saved);
    }

    @Transactional
    @Auditable(action = "ROLE_ASSIGN", description = "Assigned role to user", resource = "/api/users/{userId}/roles/{roleId}")
    public UserDto assignRoleToUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        user.getRoles().add(role);
        User saved = userRepository.save(user);
        privilegeCreepService.analyzeUserPrivilegeCreep(saved, "ROLE_ASSIGNED");

        return mapToDto(saved);
    }

    @Transactional
    @Auditable(action = "ROLE_REVOKE", description = "Revoked role from user", resource = "/api/users/{userId}/roles/{roleId}")
    public UserDto removeRoleFromUser(Long userId, Long roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));

        user.getRoles().remove(role);
        User saved = userRepository.save(user);
        privilegeCreepService.analyzeUserPrivilegeCreep(saved, "ROLE_REVOKED");

        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctDepartments() {
        return userRepository.findDistinctDepartments();
    }

    public UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setDepartment(user.getDepartment());
        dto.setJobTitle(user.getJobTitle());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setLastLoginAt(user.getLastLoginAt());

        List<RoleDto> roleDtos = user.getRoles().stream()
                .map(r -> new RoleDto(r.getId(), r.getName(), r.getDescription(), r.isSystemRole(), null))
                .collect(Collectors.toList());
        dto.setRoles(roleDtos);

        Set<String> perms = new HashSet<>();
        for (Role r : user.getRoles()) {
            for (Permission p : r.getPermissions()) {
                perms.add(p.getName());
            }
        }
        dto.setPermissions(new ArrayList<>(perms));

        return dto;
    }
}
