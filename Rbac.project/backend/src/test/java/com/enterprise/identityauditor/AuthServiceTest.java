package com.enterprise.identityauditor;

import com.enterprise.identityauditor.audit.AuditLogService;
import com.enterprise.identityauditor.dto.AuthRequest;
import com.enterprise.identityauditor.dto.AuthResponse;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.security.CustomUserDetails;
import com.enterprise.identityauditor.security.JwtUtils;
import com.enterprise.identityauditor.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should successfully authenticate valid user and issue JWT Bearer token")
    void testSuccessfulAuthentication() {
        AuthRequest request = new AuthRequest("admin", "Admin@123");

        CustomUserDetails userDetails = new CustomUserDetails(
                2L, "admin", "admin@enterprise.local", "encodedPass", true,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("USER_READ"))
        );

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("mock.jwt.token");

        User user = new User("admin", "admin@enterprise.local", "encodedPass", "David Vance", "Security", "Lead");
        user.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        AuthResponse response = authService.authenticateUser(request, "127.0.0.1");

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertTrue(response.getRoles().contains("ROLE_ADMIN"));
        verify(auditLogService, times(1)).recordLog(eq(2L), eq("admin"), eq("USER_LOGIN"), anyString(), anyString(), eq("SUCCESS"), anyString(), anyString());
    }

    @Test
    @DisplayName("Should reject invalid credentials and record failure audit log")
    void testFailedAuthentication() {
        AuthRequest request = new AuthRequest("admin", "WrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.authenticateUser(request, "127.0.0.1"));
        verify(auditLogService, times(1)).recordLog(isNull(), eq("admin"), eq("USER_LOGIN_FAILED"), anyString(), anyString(), eq("FAILURE"), anyString(), anyString());
    }
}
