package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.dto.ApiResponse;
import com.enterprise.identityauditor.dto.AuthRequest;
import com.enterprise.identityauditor.dto.AuthResponse;
import com.enterprise.identityauditor.dto.RegisterRequest;
import com.enterprise.identityauditor.dto.UserDto;
import com.enterprise.identityauditor.security.CustomUserDetails;
import com.enterprise.identityauditor.service.AuthService;
import com.enterprise.identityauditor.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody AuthRequest loginRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        AuthResponse response = authService.authenticateUser(loginRequest, ipAddress);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        authService.registerUser(registerRequest, ipAddress);
        return ResponseEntity.ok(new ApiResponse(true, "User registered successfully! You may now sign in."));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            UserDto userDto = userService.getUserById(userDetails.getId());
            return ResponseEntity.ok(userDto);
        }
        return ResponseEntity.notFound().build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isEmpty() && !"unknown".equalsIgnoreCase(xf)) {
            return xf.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
