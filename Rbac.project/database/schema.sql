-- =====================================================================
-- Enterprise Role-Based Access Control (RBAC) & Identity Auditor
-- MySQL 8 Normalized Relational Schema
-- =====================================================================

CREATE DATABASE IF NOT EXISTS identity_auditor_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE identity_auditor_db;

-- -----------------------------------------------------
-- Table: permissions
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    category VARCHAR(50),
    is_elevated BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_permission_category (category),
    INDEX idx_permission_elevated (is_elevated)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: roles
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    is_system_role BOOLEAN NOT NULL DEFAULT FALSE,
    INDEX idx_role_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: users
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    job_title VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    last_login_at DATETIME,
    INDEX idx_user_department (department),
    INDEX idx_user_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: role_permissions (Many-to-Many)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE,
    CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permissions (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: user_roles (Many-to-Many)
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: audit_logs
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    description VARCHAR(500) NOT NULL,
    resource_accessed VARCHAR(255),
    status VARCHAR(30) NOT NULL,
    ip_address VARCHAR(50),
    timestamp DATETIME NOT NULL,
    details TEXT,
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_username (username),
    INDEX idx_audit_action (action),
    INDEX idx_audit_timestamp (timestamp),
    INDEX idx_audit_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: risk_assessments
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS risk_assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    username VARCHAR(50) NOT NULL,
    full_name VARCHAR(100),
    department VARCHAR(100),
    risk_score INT NOT NULL,
    risk_classification VARCHAR(20) NOT NULL,
    unused_elevated_permissions_count INT NOT NULL DEFAULT 0,
    unused_permissions TEXT,
    last_privileged_activity DATETIME,
    days_since_last_privileged_activity INT,
    risk_explanation TEXT,
    recommended_action TEXT,
    reviewed BOOLEAN NOT NULL DEFAULT FALSE,
    reviewed_by VARCHAR(50),
    reviewed_at DATETIME,
    assessed_at DATETIME NOT NULL,
    INDEX idx_risk_user_id (user_id),
    INDEX idx_risk_classification (risk_classification),
    INDEX idx_risk_score (risk_score)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- -----------------------------------------------------
-- Table: privilege_reviews
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS privilege_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    username VARCHAR(50) NOT NULL,
    permission_name VARCHAR(100) NOT NULL,
    associated_role_name VARCHAR(50),
    inactivity_days INT NOT NULL,
    flagged_reason VARCHAR(500) NOT NULL,
    recommended_action VARCHAR(500),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    reviewed_by VARCHAR(50),
    review_notes VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_review_user_id (user_id),
    INDEX idx_review_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
