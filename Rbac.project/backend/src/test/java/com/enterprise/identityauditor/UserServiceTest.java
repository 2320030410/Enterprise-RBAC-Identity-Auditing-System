package com.enterprise.identityauditor;

import com.enterprise.identityauditor.dto.UserCreateRequest;
import com.enterprise.identityauditor.dto.UserDto;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.repository.RoleRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import com.enterprise.identityauditor.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PrivilegeCreepService privilegeCreepService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create new user, hash password, and trigger initial risk analysis")
    void testCreateUserSuccess() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("clark.kent");
        request.setEmail("clark@dailyplanet.com");
        request.setPassword("Secret@123");
        request.setFullName("Clark Kent");
        request.setDepartment("Journalism");
        request.setJobTitle("Reporter");
        request.setEnabled(true);
        request.setRoleIds(Set.of(1L));

        Role role = new Role("ROLE_EMPLOYEE", "Employee", true);
        role.setId(1L);

        when(userRepository.existsByUsername("clark.kent")).thenReturn(false);
        when(userRepository.existsByEmail("clark@dailyplanet.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret@123")).thenReturn("hashedPassword123");
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(99L);
            return u;
        });

        UserDto created = userService.createUser(request);

        assertNotNull(created);
        assertEquals("clark.kent", created.getUsername());
        verify(passwordEncoder, times(1)).encode("Secret@123");
        verify(privilegeCreepService, times(1)).analyzeUserPrivilegeCreep(any(User.class), eq("USER_CREATION"));
    }

    @Test
    @DisplayName("Should toggle user enabled status")
    void testToggleUserStatus() {
        User user = new User("clark.kent", "clark@dailyplanet.com", "hash", "Clark Kent", "News", "Reporter");
        user.setId(99L);
        user.setEnabled(true);

        when(userRepository.findById(99L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto toggled = userService.toggleUserStatus(99L);
        assertFalse(toggled.isEnabled(), "User should now be disabled");
    }
}
