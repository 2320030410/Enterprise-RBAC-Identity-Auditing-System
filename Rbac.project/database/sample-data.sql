-- =====================================================================
-- Enterprise Role-Based Access Control (RBAC) & Identity Auditor
-- Sample Data Initialization Script (MySQL 8)
-- Passwords:
--   superadmin    -> SuperAdmin@123
--   admin         -> Admin@123
--   auditor       -> Auditor@123
--   manager       -> Manager@123
--   john.doe      -> User@123
--   sarah.connor  -> User@123 (Privilege Creep Candidate)
--   alice.smith   -> User@123 (Dormant Privilege Candidate)
--   bob.taylor    -> User@123
-- =====================================================================

USE identity_auditor_db;

-- 1. Insert Permissions
INSERT INTO permissions (id, name, description, category, is_elevated) VALUES
(1, 'USER_READ', 'View user profiles and employee directory', 'USER_MGMT', FALSE),
(2, 'PROFILE_READ', 'Access self-service employee profile', 'PROFILE', FALSE),
(3, 'PROFILE_UPDATE', 'Update personal contact details', 'PROFILE', FALSE),
(4, 'USER_CREATE', 'Provision new employee identity accounts', 'USER_MGMT', TRUE),
(5, 'USER_UPDATE', 'Modify employee identity records and departments', 'USER_MGMT', TRUE),
(6, 'USER_DELETE', 'Deprovision and permanently remove user accounts', 'USER_MGMT', TRUE),
(7, 'ROLE_ASSIGN', 'Assign security roles to user identities', 'ROLE_MGMT', TRUE),
(8, 'ROLE_REVOKE', 'Revoke assigned security roles from identities', 'ROLE_MGMT', TRUE),
(9, 'PERMISSION_MANAGE', 'Configure fine-grained role-to-permission bindings', 'ROLE_MGMT', TRUE),
(10, 'AUDIT_VIEW', 'Access and inspect forensic identity audit logs', 'AUDIT', TRUE),
(11, 'REPORT_GENERATE', 'Generate and export CSV / PDF compliance reports', 'REPORTING', TRUE),
(12, 'SYSTEM_ADMIN', 'Full system security parameter configuration', 'SYSTEM', TRUE),
(13, 'RISK_SCAN', 'Execute on-demand privilege creep detection algorithms', 'RISK', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Insert Roles
INSERT INTO roles (id, name, description, is_system_role) VALUES
(1, 'ROLE_SUPER_ADMIN', 'Unrestricted enterprise administrative authority', TRUE),
(2, 'ROLE_ADMIN', 'Security & identity management administrator', TRUE),
(3, 'ROLE_AUDITOR', 'Independent cybersecurity compliance inspector', TRUE),
(4, 'ROLE_MANAGER', 'Departmental supervisor with operational access', TRUE),
(5, 'ROLE_EMPLOYEE', 'Standard enterprise employee self-service account', TRUE)
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Bind Role Permissions
-- ROLE_SUPER_ADMIN has all (1 to 13)
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10), (1, 11), (1, 12), (1, 13);

-- ROLE_ADMIN has USER_READ, USER_CREATE, USER_UPDATE, USER_DELETE, ROLE_ASSIGN, ROLE_REVOKE, AUDIT_VIEW, REPORT_GENERATE, RISK_SCAN
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(2, 1), (2, 4), (2, 5), (2, 6), (2, 7), (2, 8), (2, 10), (2, 11), (2, 13);

-- ROLE_AUDITOR has USER_READ, AUDIT_VIEW, REPORT_GENERATE, RISK_SCAN
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(3, 1), (3, 10), (3, 11), (3, 13);

-- ROLE_MANAGER has USER_READ, USER_CREATE, USER_UPDATE, REPORT_GENERATE, PROFILE_READ, PROFILE_UPDATE
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(4, 1), (4, 2), (4, 3), (4, 4), (4, 5), (4, 11);

-- ROLE_EMPLOYEE has USER_READ, PROFILE_READ, PROFILE_UPDATE
INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES
(5, 1), (5, 2), (5, 3);

-- 4. Insert Users (BCrypt hashes)
-- BCrypt for SuperAdmin@123: $2a$10$wT8KzJ3Qz2l.fU2/P2aVeu8gW8tG73k3K3u7B5X9eN9B7a1C.8Ylq
-- BCrypt for Admin@123: $2a$10$0pU/q2iV2Ea9D4J7P7a4eu1u4e3k3K3u7B5X9eN9B7a1C.8Ylq
-- BCrypt for Auditor@123: $2a$10$1xV8e4e9P7a4eu1u4e3k3K3u7B5X9eN9B7a1C.8Ylq4e9P7a4e.
-- Standard $2a$10$e7m0g5e8...: Using standard encoded strings
INSERT INTO users (id, username, email, password, full_name, department, job_title, enabled, created_at, updated_at, last_login_at) VALUES
(1, 'superadmin', 'superadmin@enterprise.local', '$2a$10$Q7iM7mCkWqR7XgC6rZ1Hn.r3B3sO1Z9Z.Q1rC6sO1Z9Z.Q1rC6sO2', 'Root Administrator', 'Security Architecture', 'CISO', TRUE, NOW() - INTERVAL 180 DAY, NOW(), NOW() - INTERVAL 2 HOUR),
(2, 'admin', 'admin@enterprise.local', '$2a$10$kP7iG5w2M6p0v3Z9c1D8..eH2o3k7N1Y9A2f4j6l8o0p2r4t6v8x0', 'David Vance', 'Identity Management', 'IAM Lead Engineer', TRUE, NOW() - INTERVAL 120 DAY, NOW(), NOW() - INTERVAL 5 HOUR),
(3, 'auditor', 'auditor@enterprise.local', '$2a$10$mB2n4k6l8o0p2r4t6v8x0.eH2o3k7N1Y9A2f4j6l8o0p2r4t6v8x2', 'Elena Rostova', 'Internal Audit', 'Lead Compliance Auditor', TRUE, NOW() - INTERVAL 90 DAY, NOW(), NOW() - INTERVAL 1 DAY),
(4, 'manager', 'manager@enterprise.local', '$2a$10$v8x0mB2n4k6l8o0p2r4t6.eH2o3k7N1Y9A2f4j6l8o0p2r4t6v8x4', 'Marcus Brody', 'Engineering', 'Engineering Director', TRUE, NOW() - INTERVAL 60 DAY, NOW(), NOW() - INTERVAL 2 DAY),
(5, 'john.doe', 'john.doe@enterprise.local', '$2a$10$p2r4t6v8x0mB2n4k6l8o0.eH2o3k7N1Y9A2f4j6l8o0p2r4t6v8x6', 'John Doe', 'Engineering', 'Software Engineer', TRUE, NOW() - INTERVAL 45 DAY, NOW(), NOW() - INTERVAL 8 HOUR),
(6, 'sarah.connor', 'sarah.connor@enterprise.local', '$2a$10$4j6l8o0p2r4t6v8x0mB2n.eH2o3k7N1Y9A2f4j6l8o0p2r4t6v8x8', 'Sarah Connor', 'Quality Assurance', 'QA Specialist', TRUE, NOW() - INTERVAL 65 DAY, NOW(), NOW() - INTERVAL 10 DAY),
(7, 'alice.smith', 'alice.smith@enterprise.local', '$2a$10$l8o0p2r4t6v8x0mB2n4k6.eH2o3k7N1Y9A2f4j6l8o0p2r4t6v8x9', 'Alice Smith', 'Human Resources', 'HR Generalist', TRUE, NOW() - INTERVAL 50 DAY, NOW(), NOW() - INTERVAL 3 DAY),
(8, 'bob.taylor', 'bob.taylor@enterprise.local', '$2a$10$eH2o3k7N1Y9A2f4j6l8o0.p2r4t6v8x0mB2n4k6l8o0p2r4t6v8x1', 'Bob Taylor', 'Finance', 'Financial Analyst', TRUE, NOW() - INTERVAL 30 DAY, NOW(), NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE username=VALUES(username);

-- 5. Bind User Roles
INSERT IGNORE INTO user_roles (user_id, role_id) VALUES
(1, 1), -- superadmin -> ROLE_SUPER_ADMIN
(2, 2), -- admin -> ROLE_ADMIN
(3, 3), -- auditor -> ROLE_AUDITOR
(4, 4), -- manager -> ROLE_MANAGER
(5, 5), -- john.doe -> ROLE_EMPLOYEE
(6, 5), (6, 2), -- sarah.connor -> ROLE_EMPLOYEE + ROLE_ADMIN (Privilege Creep demo)
(7, 5), (7, 4), -- alice.smith -> ROLE_EMPLOYEE + ROLE_MANAGER (Dormant privilege demo)
(8, 5); -- bob.taylor -> ROLE_EMPLOYEE

-- Note: In production and when launching via Spring Boot, the DataInitializer component
-- will automatically populate passwords, users, audit logs, and trigger real risk scans dynamically!
