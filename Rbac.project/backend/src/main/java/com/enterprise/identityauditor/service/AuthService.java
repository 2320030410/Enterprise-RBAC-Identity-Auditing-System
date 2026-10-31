package com.enterprise.identityauditor.service;

import com.enterprise.identityauditor.audit.AuditLogService;
import com.enterprise.identityauditor.dto.AuthRequest;
import com.enterprise.identityauditor.dto.AuthResponse;
import com.enterprise.identityauditor.dto.RegisterRequest;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.exception.BadRequestException;
import com.enterprise.identityauditor.repository.RoleRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import com.enterprise.identityauditor.security.CustomUserDetails;
import com.enterprise.identityauditor.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @Transactional
    public AuthResponse authenticateUser(AuthRequest loginRequest, String ipAddress) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            User user = userRepository.findById(userDetails.getId()).orElse(null);
            if (user != null) {
                user.setLastLoginAt(LocalDateTime.now());
                userRepository.save(user);
            }

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            List<String> permissions = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(auth -> !auth.startsWith("ROLE_"))
                    .collect(Collectors.toList());

            // Log successful login
            auditLogService.recordLog(
                    userDetails.getId(),
                    userDetails.getUsername(),
                    "USER_LOGIN",
                    "User logged in successfully",
                    "/api/auth/login",
                    "SUCCESS",
                    ipAddress,
                    "Authentication successful"
            );

            return new AuthResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    user != null ? user.getFullName() : userDetails.getUsername(),
                    user != null ? user.getDepartment() : "General",
                    roles,
                    permissions
            );
        } catch (BadCredentialsException ex) {
            // Log failed login attempt
            auditLogService.recordLog(
                    null,
                    loginRequest.getUsername(),
                    "USER_LOGIN_FAILED",
                    "Failed login attempt: invalid credentials for username " + loginRequest.getUsername(),
                    "/api/auth/login",
                    "FAILURE",
                    ipAddress,
                    "Bad credentials provided"
            );
            throw ex;
        }
    }

    @Transactional
    public void registerUser(RegisterRequest registerRequest, String ipAddress) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already registered!");
        }

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFullName(),
                registerRequest.getDepartment() != null ? registerRequest.getDepartment() : "Engineering",
                registerRequest.getJobTitle() != null ? registerRequest.getJobTitle() : "Associate"
        );

        Role userRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role ROLE_EMPLOYEE not found."));
        user.setRoles(new HashSet<>(Set.of(userRole)));

        User saved = userRepository.save(user);

        // Run initial risk calculation
        privilegeCreepService.analyzeUserPrivilegeCreep(saved, "USER_REGISTRATION");

        auditLogService.recordLog(
                saved.getId(),
                saved.getUsername(),
                "USER_REGISTER",
                "New user registered: " + saved.getUsername(),
                "/api/auth/register",
                "SUCCESS",
                ipAddress,
                "Self-registration completed"
        );
    }
}
