package com.enterprise.identityauditor.config;

import com.enterprise.identityauditor.entity.AuditLog;
import com.enterprise.identityauditor.entity.Permission;
import com.enterprise.identityauditor.entity.Role;
import com.enterprise.identityauditor.entity.User;
import com.enterprise.identityauditor.repository.AuditLogRepository;
import com.enterprise.identityauditor.repository.PermissionRepository;
import com.enterprise.identityauditor.repository.RoleRepository;
import com.enterprise.identityauditor.repository.UserRepository;
import com.enterprise.identityauditor.risk.PrivilegeCreepService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PrivilegeCreepService privilegeCreepService;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (roleRepository.count() > 0) {
            logger.info("Database already initialized. Ensuring privilege creep scan is synchronized...");
            privilegeCreepService.runFullScan("SYSTEM_BOOT_SYNC");
            return;
        }

        logger.info("Initializing Enterprise RBAC and Identity Auditor database with baseline data...");

        // 1. Create Permissions
        Map<String, Permission> permMap = new HashMap<>();

        // Standard Permissions
        permMap.put("USER_READ", createPermission("USER_READ", "View user details and profiles", "USER_MGMT", false));
        permMap.put("PROFILE_READ", createPermission("PROFILE_READ", "View own employee profile", "PROFILE", false));
        permMap.put("PROFILE_UPDATE", createPermission("PROFILE_UPDATE", "Update personal profile information", "PROFILE", false));

        // Elevated Permissions (isElevated = true)
        permMap.put("USER_CREATE", createPermission("USER_CREATE", "Provision new users in the organization", "USER_MGMT", true));
        permMap.put("USER_UPDATE", createPermission("USER_UPDATE", "Modify user properties and departments", "USER_MGMT", true));
        permMap.put("USER_DELETE", createPermission("USER_DELETE", "Deprovision/delete user accounts", "USER_MGMT", true));
        permMap.put("ROLE_ASSIGN", createPermission("ROLE_ASSIGN", "Assign roles to identities", "ROLE_MGMT", true));
        permMap.put("ROLE_REVOKE", createPermission("ROLE_REVOKE", "Revoke assigned roles from identities", "ROLE_MGMT", true));
        permMap.put("PERMISSION_MANAGE", createPermission("PERMISSION_MANAGE", "Configure role permissions", "ROLE_MGMT", true));
        permMap.put("AUDIT_VIEW", createPermission("AUDIT_VIEW", "Access system identity audit logs", "AUDIT", true));
        permMap.put("REPORT_GENERATE", createPermission("REPORT_GENERATE", "Export compliance and risk reports", "REPORTING", true));
        permMap.put("SYSTEM_ADMIN", createPermission("SYSTEM_ADMIN", "Full system configuration and control", "SYSTEM", true));
        permMap.put("RISK_SCAN", createPermission("RISK_SCAN", "Initiate on-demand privilege creep scans", "RISK", true));

        permissionRepository.saveAll(permMap.values());

        // 2. Create Roles
        Role superAdminRole = new Role("ROLE_SUPER_ADMIN", "Unrestricted system-wide governance authority", true);
        superAdminRole.setPermissions(new HashSet<>(permMap.values()));

        Role adminRole = new Role("ROLE_ADMIN", "Identity & access administration authority", true);
        adminRole.setPermissions(new HashSet<>(Arrays.asList(
                permMap.get("USER_READ"), permMap.get("USER_CREATE"), permMap.get("USER_UPDATE"), permMap.get("USER_DELETE"),
                permMap.get("ROLE_ASSIGN"), permMap.get("ROLE_REVOKE"), permMap.get("AUDIT_VIEW"),
                permMap.get("REPORT_GENERATE"), permMap.get("RISK_SCAN")
        )));

        Role auditorRole = new Role("ROLE_AUDITOR", "Security compliance and audit inspection", true);
        auditorRole.setPermissions(new HashSet<>(Arrays.asList(
                permMap.get("USER_READ"), permMap.get("AUDIT_VIEW"), permMap.get("REPORT_GENERATE"), permMap.get("RISK_SCAN")
        )));

        Role managerRole = new Role("ROLE_MANAGER", "Departmental supervisory and read management", true);
        managerRole.setPermissions(new HashSet<>(Arrays.asList(
                permMap.get("USER_READ"), permMap.get("USER_CREATE"), permMap.get("USER_UPDATE"),
                permMap.get("REPORT_GENERATE"), permMap.get("PROFILE_READ"), permMap.get("PROFILE_UPDATE")
        )));

        Role employeeRole = new Role("ROLE_EMPLOYEE", "Standard employee user with self-service access", true);
        employeeRole.setPermissions(new HashSet<>(Arrays.asList(
                permMap.get("USER_READ"), permMap.get("PROFILE_READ"), permMap.get("PROFILE_UPDATE")
        )));

        roleRepository.saveAll(Arrays.asList(superAdminRole, adminRole, auditorRole, managerRole, employeeRole));

        // 3. Create Users with BCrypt encrypted passwords
        // Password for superadmin: SuperAdmin@123
        User superAdmin = new User("superadmin", "superadmin@enterprise.local",
                passwordEncoder.encode("SuperAdmin@123"), "Root Administrator", "Security Architecture", "Chief Information Security Officer");
        superAdmin.setRoles(new HashSet<>(Set.of(superAdminRole)));
        superAdmin.setCreatedAt(LocalDateTime.now().minusDays(180));
        superAdmin.setLastLoginAt(LocalDateTime.now().minusHours(2));
        userRepository.save(superAdmin);

        // Password for admin: Admin@123
        User admin = new User("admin", "admin@enterprise.local",
                passwordEncoder.encode("Admin@123"), "David Vance", "Identity Management", "IAM Lead Engineer");
        admin.setRoles(new HashSet<>(Set.of(adminRole)));
        admin.setCreatedAt(LocalDateTime.now().minusDays(120));
        admin.setLastLoginAt(LocalDateTime.now().minusHours(5));
        userRepository.save(admin);

        // Password for auditor: Auditor@123
        User auditor = new User("auditor", "auditor@enterprise.local",
                passwordEncoder.encode("Auditor@123"), "Elena Rostova", "Internal Audit", "Lead Compliance Auditor");
        auditor.setRoles(new HashSet<>(Set.of(auditorRole)));
        auditor.setCreatedAt(LocalDateTime.now().minusDays(90));
        auditor.setLastLoginAt(LocalDateTime.now().minusDays(1));
        userRepository.save(auditor);

        // Password for manager: Manager@123
        User manager = new User("manager", "manager@enterprise.local",
                passwordEncoder.encode("Manager@123"), "Marcus Brody", "Engineering", "Engineering Director");
        manager.setRoles(new HashSet<>(Set.of(managerRole)));
        manager.setCreatedAt(LocalDateTime.now().minusDays(60));
        manager.setLastLoginAt(LocalDateTime.now().minusDays(2));
        userRepository.save(manager);

        // Standard compliant employee: john.doe / User@123
        User john = new User("john.doe", "john.doe@enterprise.local",
                passwordEncoder.encode("User@123"), "John Doe", "Engineering", "Software Engineer");
        john.setRoles(new HashSet<>(Set.of(employeeRole)));
        john.setCreatedAt(LocalDateTime.now().minusDays(45));
        john.setLastLoginAt(LocalDateTime.now().minusHours(8));
        userRepository.save(john);

        // PRIVILEGE CREEP DEMO USER 1: sarah.connor / User@123
        // Has ADMIN role assigned 65 days ago, but last performed admin action 52 days ago (>30 days inactive!)
        User sarah = new User("sarah.connor", "sarah.connor@enterprise.local",
                passwordEncoder.encode("User@123"), "Sarah Connor", "Quality Assurance", "QA Specialist");
        sarah.setRoles(new HashSet<>(Arrays.asList(employeeRole, adminRole)));
        sarah.setCreatedAt(LocalDateTime.now().minusDays(65));
        sarah.setLastLoginAt(LocalDateTime.now().minusDays(10));
        userRepository.save(sarah);

        // PRIVILEGE CREEP DEMO USER 2: alice.smith / User@123
        // Assigned MANAGER role 50 days ago, has NEVER performed manager/creation actions!
        User alice = new User("alice.smith", "alice.smith@enterprise.local",
                passwordEncoder.encode("User@123"), "Alice Smith", "Human Resources", "HR Generalist");
        alice.setRoles(new HashSet<>(Arrays.asList(employeeRole, managerRole)));
        alice.setCreatedAt(LocalDateTime.now().minusDays(50));
        alice.setLastLoginAt(LocalDateTime.now().minusDays(3));
        userRepository.save(alice);

        // Another compliant employee: bob.taylor / User@123
        User bob = new User("bob.taylor", "bob.taylor@enterprise.local",
                passwordEncoder.encode("User@123"), "Bob Taylor", "Finance", "Financial Analyst");
        bob.setRoles(new HashSet<>(Set.of(employeeRole)));
        bob.setCreatedAt(LocalDateTime.now().minusDays(30));
        bob.setLastLoginAt(LocalDateTime.now().minusDays(1));
        userRepository.save(bob);

        // 4. Seed Historical Audit Logs to realistically demonstrate Privilege Creep
        createHistoricalAuditLogs(superAdmin, admin, auditor, sarah, alice);

        // 5. Run Initial Privilege Creep Detection & Risk Assessment
        logger.info("Running initial Privilege Creep analysis and generating risk scores...");
        privilegeCreepService.runFullScan("INITIAL_BOOTSTRAP");

        logger.info("Enterprise Identity Auditor successfully initialized!");
    }

    private Permission createPermission(String name, String description, String category, boolean elevated) {
        return new Permission(name, description, category, elevated);
    }

    private void createHistoricalAuditLogs(User superAdmin, User admin, User auditor, User sarah, User alice) {
        List<AuditLog> logs = new ArrayList<>();

        // Admin recent active logs (today and past few days)
        logs.add(new AuditLog(admin.getId(), admin.getUsername(), "USER_LOGIN", "User logged in successfully", "/api/auth/login", "SUCCESS", "192.168.1.10", LocalDateTime.now().minusHours(5), "Web session"));
        logs.add(new AuditLog(admin.getId(), admin.getUsername(), "USER_CREATE", "Created new employee record", "/api/users", "SUCCESS", "192.168.1.10", LocalDateTime.now().minusDays(1), "Created bob.taylor"));
        logs.add(new AuditLog(admin.getId(), admin.getUsername(), "ROLE_ASSIGN", "Assigned ROLE_EMPLOYEE to user", "/api/users/8/roles/5", "SUCCESS", "192.168.1.10", LocalDateTime.now().minusDays(1), "Role granted"));
        logs.add(new AuditLog(admin.getId(), admin.getUsername(), "REPORT_GENERATE", "Exported executive audit report", "/api/reports/executive-summary/pdf", "SUCCESS", "192.168.1.10", LocalDateTime.now().minusDays(2), "Compliance export"));

        // SuperAdmin active logs
        logs.add(new AuditLog(superAdmin.getId(), superAdmin.getUsername(), "SYSTEM_ADMIN", "Configured enterprise security policies", "/api/system/config", "SUCCESS", "192.168.1.1", LocalDateTime.now().minusHours(2), "Global policy sync"));
        logs.add(new AuditLog(superAdmin.getId(), superAdmin.getUsername(), "RISK_SCAN", "Initiated full organization identity scan", "/api/risk/scan", "SUCCESS", "192.168.1.1", LocalDateTime.now().minusDays(3), "Manual scan"));

        // Auditor logs
        logs.add(new AuditLog(auditor.getId(), auditor.getUsername(), "AUDIT_VIEW", "Inspected privileged audit events", "/api/audit-logs", "SUCCESS", "192.168.1.25", LocalDateTime.now().minusDays(1), "Log review"));

        // SARAH CONNOR: Last used USER_DELETE and ROLE_REVOKE 52 days ago! (> 30 days threshold!)
        logs.add(new AuditLog(sarah.getId(), sarah.getUsername(), "USER_LOGIN", "User logged in", "/api/auth/login", "SUCCESS", "192.168.1.42", LocalDateTime.now().minusDays(10), "Web login"));
        logs.add(new AuditLog(sarah.getId(), sarah.getUsername(), "USER_DELETE", "Deleted legacy test user account", "/api/users/99", "SUCCESS", "192.168.1.42", LocalDateTime.now().minusDays(52), "Action executed 52 days ago"));
        logs.add(new AuditLog(sarah.getId(), sarah.getUsername(), "ROLE_REVOKE", "Revoked temporary role from contractor", "/api/users/99/roles/2", "SUCCESS", "192.168.1.42", LocalDateTime.now().minusDays(54), "Action executed 54 days ago"));

        // ALICE SMITH: Logged in 3 days ago, but NEVER performed elevated actions (USER_CREATE / REPORT_GENERATE)
        logs.add(new AuditLog(alice.getId(), alice.getUsername(), "USER_LOGIN", "User logged in", "/api/auth/login", "SUCCESS", "192.168.1.55", LocalDateTime.now().minusDays(3), "Employee login"));

        // Unauthorized access attempt demonstration
        logs.add(new AuditLog(sarah.getId(), sarah.getUsername(), "SYSTEM_ADMIN", "Attempted unauthorized system policy modification [UNAUTHORIZED ACCESS ATTEMPT]", "/api/system/config", "UNAUTHORIZED", "192.168.1.42", LocalDateTime.now().minusDays(4), "Access denied by security filter"));

        auditLogRepository.saveAll(logs);
    }
}
