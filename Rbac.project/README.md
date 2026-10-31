# Enterprise Role-Based Access Control (RBAC) & Identity Auditor

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.2.4-green.svg)](https://spring.io/projects/spring-security)
[![React](https://img.shields.io/badge/React-18.3.1-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-5.2-purple.svg)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-orange.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-Academic%20B.Tech%20CSE-navy.svg)](#)

An enterprise-grade **Identity Governance and Administration (IGA)** platform designed to detect **privilege creep**, enforce the **Principle of Least Privilege (PoLP)**, calculate **quantitative cybersecurity risk scores (0–100)**, maintain an **immutable forensic audit trail via Spring AOP**, and generate **downloadable CSV and PDF compliance reports**.

Developed as a flagship **Final-Year B.Tech Computer Science and Engineering (Cybersecurity Specialization) Project**.

---

## 1. Project Overview & Problem Statement

In modern enterprise architectures, **privilege creep** represents one of the most critical insider threat vectors. Privilege creep occurs when employees transition between departments or roles and accumulate elevated administrative entitlements that are never subsequently revoked. 

### Core Capabilities:
1. **Dynamic Privilege Creep Detection Engine**: Scans all assigned identity entitlements, analyzes historical audit logs, and flags elevated permissions inactive for **$\ge$ 30 consecutive days**.
2. **Dormancy vs. Inactivity Differentiation**: Uniquely distinguishes between an elevated privilege that was used in the past but has grown dormant vs. a permission that has **never been exercised** since account provisioning.
3. **Transparent Algorithmic Risk Scoring (0–100)**: Evaluates role tier, elevated permission count, inactivity duration, and account status to classify risk into **LOW**, **MEDIUM**, **HIGH**, and **CRITICAL** with mathematical explainability.
4. **Privilege Review & Remediation Workflow**: Provides administrators with a formal queue to review flagged permissions, approve immediate revocation (automatically lowering user risk), or record business justifications for temporary retention.
5. **Spring AOP Forensic Auditing Engine**: Intercepts authentication attempts, administrative operations, and unauthorized access attempts without polluting business logic.
6. **Multi-Tier Compliance Reporting**: Generates on-the-fly downloadable **CSV datasets (OpenCSV)** and **formal executive PDF briefings (OpenPDF)**.

---

## 2. Architecture & Technology Stack

```
   ┌─────────────────────────────────────────────────────────────┐
   │             React 18 Single Page Application                │
   │  Bootstrap 5 · Chart.js · Lucide Icons · Axios Client       │
   └──────────────────────────────┬──────────────────────────────┘
                                  │ HTTP / REST (JWT Bearer)
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │             Spring Boot 3 Enterprise Backend                │
   │                                                             │
   │  ┌────────────────────┐   ┌──────────────────────────────┐  │
   │  │   Spring Security  │   │     Spring AOP Auditing      │  │
   │  │   JWT Auth & RBAC  │   │  @Auditable Method Intercept │  │
   │  └────────────────────┘   └──────────────────────────────┘  │
   │  ┌────────────────────┐   ┌──────────────────────────────┐  │
   │  │  Privilege Creep   │   │     Reporting Module         │  │
   │  │  Detection Engine  │   │    OpenPDF & OpenCSV Engine  │  │
   │  └────────────────────┘   └──────────────────────────────┘  │
   │  ┌───────────────────────────────────────────────────────┐  │
   │  │            Spring Data JPA & Hibernate                │  │
   │  └───────────────────────────────────────────────────────┘  │
   └──────────────────────────────┬──────────────────────────────┘
                                  │ JDBC
                                  ▼
   ┌─────────────────────────────────────────────────────────────┐
   │                  Relational Storage Layer                   │
   │  MySQL 8 (Production) / In-Memory H2 (Dev Compatibility)    │
   └─────────────────────────────────────────────────────────────┘
```

### Technology Matrix:
| Component | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | React 18, Vite 5, Bootstrap 5 | Modern, responsive cybersecurity SOC dashboard interface |
| **Data Visualization** | Chart.js 4, react-chartjs-2 | Interactive Doughnut, Bar, and Pie charts for risk telemetry |
| **Backend Core** | Java 17 / 23, Spring Boot 3.2.5 | Enterprise microservices framework and MVC business logic |
| **Security & Auth** | Spring Security 6, JJWT 0.12.5 | Stateless JWT authentication, BCrypt password hashing |
| **Aspect-Oriented Audit** | Spring AOP, AspectJ | Non-invasive forensic event logging and security interception |
| **Data Access** | Spring Data JPA, Hibernate ORM | Normalized relational schema with foreign key integrity |
| **PDF Reporting** | OpenPDF 2.0.3 | CISO-grade cryptographic compliance briefings with tables |
| **CSV Reporting** | OpenCSV 5.9 | Tabular data exports for identity audits and SIEM ingestion |
| **Database** | MySQL 8.0 / H2 Engine | Normalized relational tables with cascading constraints |

---

## 3. Pre-Seeded Sample Login Credentials

The application initializes with realistic test accounts configured with encrypted passwords:

| Username | Plaintext Password | Assigned Security Roles | Purpose in Demonstration |
| :--- | :--- | :--- | :--- |
| **`superadmin`** | `SuperAdmin@123` | `ROLE_SUPER_ADMIN` | Unrestricted global access, system settings, all reports |
| **`admin`** | `Admin@123` | `ROLE_ADMIN` | Identity management, role assignment, risk remediation |
| **`auditor`** | `Auditor@123` | `ROLE_AUDITOR` | Compliance inspector, forensic logs, report downloads |
| **`manager`** | `Manager@123` | `ROLE_MANAGER` | Departmental supervisor, employee read access |
| **`sarah.connor`** | `User@123` | `ROLE_EMPLOYEE`, `ROLE_ADMIN` | **Privilege Creep Candidate**: Holds `USER_DELETE` & `ROLE_REVOKE` unused for > 50 days! |
| **`alice.smith`** | `User@123` | `ROLE_EMPLOYEE`, `ROLE_MANAGER` | **Dormant Privilege Candidate**: Holds manager rights never exercised since assignment |
| **`john.doe`** | `User@123` | `ROLE_EMPLOYEE` | Standard compliant employee (Self-Service workspace) |
| **`bob.taylor`** | `User@123` | `ROLE_EMPLOYEE` | Standard compliant employee (Finance department) |

> [!TIP]
> On the **Login Page**, click the **One-Click Demo Account Buttons** to instantly populate credentials for any role during viva presentations!

---

## 4. Mathematical Risk Scoring Algorithm

The risk assessment engine calculates a quantitative score between **$0$ and $100$**:

$$\text{Risk Score} = \min\left(100, \sum \text{Risk Factors}\right)$$

### Factor Breakdown:
1. **Role Privilege Tier**:
   - `ROLE_SUPER_ADMIN`: $+25\text{ pts}$
   - `ROLE_ADMIN`: $+20\text{ pts}$
   - `ROLE_MANAGER` / `ROLE_AUDITOR`: $+10\text{ pts}$
2. **Elevated Entitlement Volume**:
   - $+4\text{ pts}$ per assigned elevated permission (capped at $+20\text{ pts}$)
3. **Privileged Inactivity Duration**:
   - $\ge 90\text{ days}$ inactive: $+30\text{ pts}$
   - $\ge 60\text{ days}$ inactive: $+20\text{ pts}$
   - $\ge 30\text{ days}$ inactive (Threshold Breach): $+15\text{ pts}$
4. **Dormant Elevated Entitlements (Privilege Creep)**:
   - $+12\text{ pts}$ per dormant elevated privilege (capped at $+35\text{ pts}$)
5. **Account Status Anomaly**:
   - Disabled account retaining privileged rights: $+15\text{ pts}$

### Risk Classification Thresholds:
- **`LOW`** ($0 - 29$): Compliant employee identity adhering to least privilege.
- **`MEDIUM`** ($30 - 59$): Operational identity; periodic audit review recommended.
- **`HIGH`** ($60 - 79$): Excessive permissions or dormant elevated privileges detected.
- **`CRITICAL`** ($80 - 100$): Severe privilege accumulation or long-term dormant admin access requiring immediate revocation.

---

## 5. Normalized Database Schema

The relational database adheres to Third Normal Form (3NF):

```sql
users (id, username, email, password, full_name, department, job_title, enabled, created_at, updated_at, last_login_at)
roles (id, name, description, is_system_role)
permissions (id, name, description, category, is_elevated)
user_roles (user_id, role_id, assigned_at)
role_permissions (role_id, permission_id)
audit_logs (id, user_id, username, action, description, resource_accessed, status, ip_address, timestamp, details)
risk_assessments (id, user_id, username, full_name, department, risk_score, risk_classification, unused_elevated_permissions_count, unused_permissions, last_privileged_activity, days_since_last_privileged_activity, risk_explanation, recommended_action, reviewed, reviewed_by, reviewed_at, assessed_at)
privilege_reviews (id, user_id, username, permission_name, associated_role_name, inactivity_days, flagged_reason, recommended_action, status, reviewed_by, review_notes, created_at, updated_at)
```

Pre-written SQL scripts:
- `database/schema.sql` (MySQL 8 Table definitions, indexes, foreign keys)
- `database/sample-data.sql` (Seed data with BCrypt hashes and historical telemetry)

---

## 6. Local Setup and Execution Instructions

### Prerequisites:
- **Java 17 or higher** (Java 23 verified compatible)
- **Node.js 18+ and npm**
- **MySQL 8.0** (Optional: the backend includes an automatic zero-friction in-memory dev profile that works out of the box without installing MySQL!)

### Step 1: Start the Spring Boot Backend

Open PowerShell / Terminal in the project root:

```powershell
cd backend

# Option A: Zero-friction Dev Mode (Built-in H2 with MySQL compatibility, zero external setup needed)
mvn spring-boot:run

# Option B: Run against external MySQL 8 Server (ensure schema.sql is executed in MySQL first)
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

> **Backend Port**: `http://localhost:8081` (or `http://localhost:8080`)  
> **API Base**: `http://localhost:8081/api`  
> **H2 In-Memory Console**: `http://localhost:8081/h2-console` (JDBC URL: `jdbc:h2:mem:identity_auditor_db`)

### Step 2: Start the React Frontend

In a separate terminal window:

```powershell
cd frontend

# Install dependencies (if not already installed)
npm install

# Start Vite Development Server
npm run dev
```

> **Frontend URL**: `http://localhost:5173`

---

## 7. REST API Documentation

### Authentication (`/api/auth`)
- `POST /api/auth/login` - Authenticate with username & password, returns JWT token & granted entitlements.
- `POST /api/auth/register` - Self-service identity onboarding.
- `GET /api/auth/me` - Retrieve current session identity details.

### User Directory (`/api/users`)
- `GET /api/users` - Search and filter users by department and enabled status.
- `GET /api/users/{id}` - Retrieve user details and role bindings.
- `POST /api/users` - Provision new employee account (`@PreAuthorize("hasRole('ADMIN')")`).
- `PUT /api/users/{id}` - Modify user details and role assignments.
- `DELETE /api/users/{id}` - Deprovision user account.
- `PATCH /api/users/{id}/toggle-status` - Activate / deactivate account.

### Role & Entitlement Management (`/api/roles`, `/api/permissions`)
- `GET /api/roles` - Retrieve all roles and their mapped permissions.
- `POST /api/roles` - Create custom security role.
- `PUT /api/roles/{id}/permissions` - Update role permission matrix.
- `GET /api/permissions` - List all fine-grained permissions.

### Risk & Privilege Creep (`/api/risk`, `/api/privilege-reviews`)
- `GET /api/risk` - Retrieve all computed risk assessments with factor explanations.
- `GET /api/risk/{userId}` - Retrieve specific user risk score.
- `POST /api/risk/scan` - Trigger on-demand algorithmic privilege creep detection scan.
- `GET /api/privilege-reviews` - Retrieve flagged dormant privilege queue.
- `POST /api/privilege-reviews/{id}/approve` - Approve revocation of dormant privilege (unassigns role and recomputes risk).
- `POST /api/privilege-reviews/{id}/reject` - Retain privilege with audit justification.

### Forensics & Reporting (`/api/audit-logs`, `/api/reports`)
- `GET /api/audit-logs` - Query forensic logs with username, action, status, and date filters.
- `GET /api/reports/users/csv` - Download User Access Directory CSV.
- `GET /api/reports/audit/csv` - Download Audit Activity CSV.
- `GET /api/reports/privilege-creep/csv` - Download Privilege Creep Review CSV.
- `GET /api/reports/executive-summary/pdf` - Download Executive Security Summary PDF.
- `GET /api/reports/over-privileged/pdf` - Download Over-Privileged Accounts PDF.
- `GET /api/reports/unused-privileges/pdf` - Download Unused Privileges Report PDF.

---

## 8. Academic Viva Defense Guide (Q&A)

### Q1: What is Privilege Creep and why is it dangerous?
> **Answer**: Privilege creep occurs when users accumulate permissions over time as they change projects or departments, without old rights being revoked. It violates the Principle of Least Privilege, broadens the attack surface for credential theft, and elevates insider threat risks.

### Q2: How does your algorithm detect privilege creep?
> **Answer**: The algorithm queries the user's assigned permissions and identifies elevated privileges (`isElevated = true`). It then inspects the `audit_logs` table via Spring Data JPA to find the timestamp of the last successful privileged operation for each privilege. If the inactivity duration exceeds 30 days (or if the privilege has never been used since account creation), the permission is flagged as dormant and a `PrivilegeReview` ticket is automatically generated.

### Q3: How is Spring AOP used for auditing?
> **Answer**: We defined a custom annotation `@Auditable(action = "...", description = "...")`. Using an `@Around` advice aspect in `AuditAspect.java`, execution of service methods is intercepted. The aspect extracts the authenticated principal from `SecurityContextHolder`, client IP from `RequestContextHolder`, parameters, execution time, and records an immutable log in MySQL marked `SUCCESS`, `FAILURE`, or `UNAUTHORIZED`.

### Q4: How is authorization enforced on the backend?
> **Answer**: Authorization is enforced using method-level security with Spring Security's `@EnableMethodSecurity(prePostEnabled = true)`. Methods are protected with annotations such as `@PreAuthorize("hasRole('ADMIN') or hasAuthority('USER_CREATE')")`. Even if a user bypasses frontend controls, the backend returns HTTP 403 Forbidden.
