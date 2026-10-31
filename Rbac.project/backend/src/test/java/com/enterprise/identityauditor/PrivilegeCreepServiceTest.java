package com.enterprise.identityauditor;

import com.enterprise.identityauditor.entity.AuditLog;
import com.enterprise.identityauditor.entity.Permission;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.repository.AuditLogRepository;
import com.enterprise.identityauditor.repository.PrivilegeReviewRepository;
import com.enterprise.identityauditor.repository.RiskAssessmentRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrivilegeCreepServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private PrivilegeReviewRepository privilegeReviewRepository;

    @InjectMocks
    private PrivilegeCreepService privilegeCreepService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(privilegeCreepService, "inactivityThresholdDays", 30);
    }

    @Test
    @DisplayName("Should flag elevated permission unused for more than 30 days and calculate elevated risk")
    void testPrivilegeCreepDetectionWith30DayInactivity() {
        // Arrange
        User user = new User("sarah.connor", "sarah@test.com", "pass", "Sarah Connor", "QA", "Specialist");
        user.setId(6L);
        user.setCreatedAt(LocalDateTime.now().minusDays(60));

        Permission deletePerm = new Permission("USER_DELETE", "Delete users", "USER_MGMT", true);
        Role adminRole = new Role("ROLE_ADMIN", "Administrator", true);
        adminRole.setPermissions(new HashSet<>(Set.of(deletePerm)));
        user.setRoles(new HashSet<>(Set.of(adminRole)));

        // Simulate last usage was 45 days ago (> 30 days)
        AuditLog pastLog = new AuditLog(6L, "sarah.connor", "USER_DELETE", "Deleted user",
                "/api/users/99", "SUCCESS", "127.0.0.1", LocalDateTime.now().minusDays(45), "Test");

        when(auditLogRepository.findTop1ByUserIdAndActionAndStatusOrderByTimestampDesc(eq(6L), eq("USER_DELETE"), eq("SUCCESS")))
                .thenReturn(Optional.of(pastLog));

        when(riskAssessmentRepository.findByUserId(6L)).thenReturn(Optional.empty());
        when(riskAssessmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var assessment = privilegeCreepService.analyzeUserPrivilegeCreep(user, "UNIT_TEST");

        // Assert
        assertNotNull(assessment);
        assertEquals("sarah.connor", assessment.getUsername());
        assertTrue(assessment.getUnusedElevatedPermissionsCount() >= 1, "Should count at least 1 unused elevated permission");
        assertTrue(assessment.getRiskScore() >= 30, "Risk score should be elevated due to privilege creep");
        assertTrue(assessment.getRiskExplanation().contains("pts"), "Explanation should contain transparent points breakdown");

        // Verify that a privilege review was saved
        verify(privilegeReviewRepository, atLeastOnce()).save(any());
    }

    @Test
    @DisplayName("Should detect never-used dormant elevated permissions assigned > 30 days ago")
    void testNeverUsedElevatedPermissionCreep() {
        // Arrange: User created 50 days ago, assigned elevated permission, never used
        User user = new User("alice.smith", "alice@test.com", "pass", "Alice Smith", "HR", "Generalist");
        user.setId(7L);
        user.setCreatedAt(LocalDateTime.now().minusDays(50));

        Permission createPerm = new Permission("USER_CREATE", "Create users", "USER_MGMT", true);
        Role managerRole = new Role("ROLE_MANAGER", "Manager", true);
        managerRole.setPermissions(new HashSet<>(Set.of(createPerm)));
        user.setRoles(new HashSet<>(Set.of(managerRole)));

        // No audit logs found
        when(auditLogRepository.findTop1ByUserIdAndActionAndStatusOrderByTimestampDesc(anyLong(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        when(riskAssessmentRepository.findByUserId(7L)).thenReturn(Optional.empty());
        when(riskAssessmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var assessment = privilegeCreepService.analyzeUserPrivilegeCreep(user, "UNIT_TEST");

        // Assert
        assertNotNull(assessment);
        assertTrue(assessment.getUnusedPermissions().contains("Never used"), "Should distinguish never-used permissions");
        assertEquals(1, assessment.getUnusedElevatedPermissionsCount());
    }

    @Test
    @DisplayName("Standard employee with no elevated permissions should have low risk score")
    void testStandardEmployeeLowRisk() {
        // Arrange
        User user = new User("john.doe", "john@test.com", "pass", "John Doe", "Dev", "Developer");
        user.setId(5L);
        user.setCreatedAt(LocalDateTime.now().minusDays(30));

        Permission readPerm = new Permission("USER_READ", "Read users", "USER_MGMT", false); // Not elevated
        Role employeeRole = new Role("ROLE_EMPLOYEE", "Employee", true);
        employeeRole.setPermissions(new HashSet<>(Set.of(readPerm)));
        user.setRoles(new HashSet<>(Set.of(employeeRole)));

        when(riskAssessmentRepository.findByUserId(5L)).thenReturn(Optional.empty());
        when(riskAssessmentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var assessment = privilegeCreepService.analyzeUserPrivilegeCreep(user, "UNIT_TEST");

        // Assert
        assertNotNull(assessment);
        assertEquals(0, assessment.getUnusedElevatedPermissionsCount());
        assertEquals("LOW", assessment.getRiskClassification());
        assertTrue(assessment.getRiskScore() < 30);
    }
}
