# Enterprise Role-Based Access Control (RBAC) & Identity Auditor

## 1. Project Overview

The **Enterprise Role-Based Access Control (RBAC) & Identity Auditor** is a security-focused application designed to manage user access, enforce permissions, monitor security activities, and identify potentially risky access assignments in enterprise environments.

In organizations, employees have different responsibilities and require different levels of access to applications and sensitive information. Traditional Role-Based Access Control (RBAC) provides structured authorization by assigning permissions to roles and roles to users. However, when employees change their responsibilities, outdated permissions may remain active, leading to a security issue known as **Privilege Creep**.

Our project addresses this problem by integrating RBAC enforcement with identity auditing, security-event logging, contextual risk assessment, and an administrator review workflow.

The system helps administrators understand who has access to what, monitor security activities, and identify potentially excessive or suspicious permissions.

## 2. Problem Statement

Traditional RBAC systems control access based on predefined roles and permissions. However, they do not automatically determine whether every assigned permission is still necessary.

Some common problems include:

- Old permissions remaining active after employee role changes.
- Users accumulating unnecessary permissions over time.
- Unused or excessive access assignments.
- Difficulty identifying potentially risky user activities.
- Manual access reviews being time-consuming and inconsistent.
- Lack of a clear account-level security risk summary.

Our project aims to integrate access control, identity auditing, privilege monitoring, and contextual risk classification into a unified system.

## 3. Objectives

The main objectives of this project are:

1. Implement secure user authentication using BCrypt password hashing and JWT-based authentication.
2. Manage users, roles, permissions, and their mappings.
3. Enforce permission-based access decisions using ALLOW and DENY mechanisms.
4. Maintain security-event logs for auditing and traceability.
5. Identify potentially excessive, unused, elevated, or risky permissions.
6. Classify security risks into Low, Medium, High, and Critical categories.
7. Provide administrators with a centralized view of user identities, permissions, audit records, and risk information.
8. Support repeatable testing of authentication, authorization, auditing, and risk assessment.

## 4. Key Features

### 4.1 User Authentication
- Secure user registration and login.
- BCrypt password hashing.
- JWT-based authentication for API requests.

### 4.2 Role Management
- Create and manage user roles.
- Assign roles to users.
- Maintain user-role relationships.

### 4.3 Permission Management
- Create and manage permissions.
- Map permissions to roles.
- Control access to protected operations.

### 4.4 Role-Based Access Control
- Verify user permissions before allowing protected operations.
- Return ALLOW or DENY decisions.
- Prevent unauthorized access to restricted resources.

### 4.5 Security Event Auditing
- Record relevant security events.
- Maintain audit information for traceability.
- Support administrative security reviews.

### 4.6 Privilege Creep Monitoring
- Identify potentially excessive permissions.
- Review unused permission assignments.
- Highlight elevated or potentially risky access.

### 4.7 Contextual Risk Assessment
The system evaluates different contextual factors, including:

- Elevated privileges.
- Excessive or unused permissions.
- Unfamiliar IP addresses or devices.
- Unusual login times.
- Repeated failed login attempts.
- Suspicious activities.
- Repeated unauthorized access attempts.

### 4.8 Risk Classification

Based on the project's documented risk-scoring thresholds, risks are categorized as follows:

| Risk Category | Risk Score |
|---|---|
| Low | 0–20 |
| Medium | 21–40 |
| High | 41–60 |
| Critical | 61 and above |

The system provides reasons for risk classifications to help administrators prioritize investigation.

*Note: Risk flags indicate potential concerns and do not independently prove malicious activity.*

## 5. System Architecture

The project follows a frontend-backend architecture.

### Architecture Workflow

```text
             USER / ADMIN
                  |
                  v
             React.js
              Interface
                  |
                  v
          Spring Boot REST API
                  |
                  v
          Spring Security + JWT
                  |
                  v
          RBAC Authorization
                  |
            +-----+-----+
            |           |
            v           v
          ALLOW       DENY
            |
            v
       Audit Event Logging
            |
            v
      Contextual Risk Assessment
            |
            v
       Risk Classification
            |
            v
     Administrator Dashboard
            |
            v
      Review and Remediation
```

### Data Layer

- **Database:** MySQL
- **ORM:** Spring Data JPA / Hibernate
- **Auditing:** Spring AOP
- **API Testing:** Postman
- **API Documentation:** Swagger / OpenAPI

## 6. Technology Stack

| Component | Technology |
|---|---|
| Frontend | React.js |
| Backend | Java |
| Backend Framework | Spring Boot |
| Security Framework | Spring Security |
| Authentication | JWT |
| Password Security | BCrypt |
| Database | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Auditing | Spring AOP |
| API Testing | Postman |
| API Documentation | Swagger / OpenAPI |

## 7. Working Methodology

The proposed system follows these steps:

**Step 1: User Authentication**

Users log in using their credentials. Passwords are secured using BCrypt hashing, and JWT is used for authenticated API requests.

**Step 2: Role and Permission Resolution**

The system retrieves the roles assigned to the user and identifies the permissions associated with those roles.

**Step 3: Access Authorization**

When a user requests a protected operation, the system checks the assigned permissions and generates an ALLOW or DENY decision.

**Step 4: Security Event Logging**

Relevant security activities and access events are recorded for auditing and future investigation.

**Step 5: Risk Assessment**

The system evaluates contextual factors, including unusual login patterns, elevated privileges, excessive permissions, and repeated failed attempts.

**Step 6: Risk Classification**

Based on the calculated risk score, the system classifies the risk as Low, Medium, High, or Critical.

**Step 7: Administrator Review**

Administrators can review identity information, permissions, audit events, and risk classifications to take appropriate follow-up actions.

## 8. Implementation and Testing

The project covers the following implementation areas:

- User registration and login.
- BCrypt password hashing and JWT authentication.
- User and account administration.
- Role creation and assignment.
- Permission registry and mappings.
- Permission-based ALLOW / DENY authorization.
- Security-event logging.
- Privilege-creep monitoring.
- Contextual risk assessment.
- Administrative audit and risk review.

### Verification Scenarios

The following test scenarios are defined for system verification:

1. Valid and invalid user login.
2. User role assignment.
3. Permission creation and modification.
4. Unauthorized access attempts.
5. ALLOW / DENY authorization decisions.
6. Security-event logging.
7. Risk assessment and classification.

## 9. Applications

The proposed system can be used in:

- Enterprise management portals.
- Human Resource (HR) management systems.
- Financial management applications.
- Internal organizational applications.
- Systems handling sensitive business information.
- Applications requiring controlled user access and security auditing.

## 10. Future Enhancements

The project can be extended with the following improvements:

- Automated least-privilege recommendations.
- Advanced anomaly detection.
- Improved contextual risk assessment.
- Continuous monitoring of user access activities.
- Evaluation with larger user and permission datasets.
- Reduction of false-positive risk alerts.
- Performance and scalability evaluation.
- Enhanced administrator reporting and audit dashboards.

## 11. Conclusion

The Enterprise Role-Based Access Control (RBAC) & Identity Auditor combines traditional role-based authorization with identity auditing, privilege monitoring, and contextual risk assessment.

By integrating these capabilities into a unified workflow, the system aims to improve visibility into user access, identify potentially unnecessary permissions, and help administrators prioritize security reviews.

The project provides a foundation for building more transparent, manageable, and secure enterprise access management systems.

## 12. Project Team

**Team 16 – Engineering Capstone Project**

| Roll Number | Team Member |
|---|---|
| 2320030047 | P. Varun Reddy |
| 2320030410 | K. Chandana |
| 2320030446 | Pardhava Nandan |

**Project Guide:** Dr. K. Swanthana

**Department:** Computer Science and Engineering

**Institution:** Koneru Lakshmaiah Education Foundation

---

*Academic Project – Engineering Capstone Project I / Industrial Internship Phase I, 2026–2027.*
