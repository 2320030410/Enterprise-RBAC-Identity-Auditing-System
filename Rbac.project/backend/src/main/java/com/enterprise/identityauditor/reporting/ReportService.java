package com.enterprise.identityauditor.reporting;

import com.enterprise.identityauditor.audit.Auditable;
import com.enterprise.identityauditor.entity.*;
import com.enterprise.identityauditor.repository.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private RiskAssessmentRepository riskAssessmentRepository;

    @Autowired
    private PrivilegeReviewRepository privilegeReviewRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ==========================================
    // CSV REPORTS
    // ==========================================

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Users CSV Report", resource = "/api/reports/users/csv")
    public byte[] generateUsersCsv() {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"ID", "Username", "Full Name", "Email", "Department", "Job Title", "Enabled", "Assigned Roles", "Assigned Permissions", "Created At", "Last Login"});

            List<User> users = userRepository.findAll();
            for (User u : users) {
                String roles = u.getRoles().stream().map(Role::getName).collect(Collectors.joining("; "));
                String perms = u.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .map(Permission::getName)
                        .distinct()
                        .collect(Collectors.joining("; "));

                writer.writeNext(new String[]{
                        String.valueOf(u.getId()),
                        u.getUsername(),
                        u.getFullName(),
                        u.getEmail(),
                        u.getDepartment() != null ? u.getDepartment() : "N/A",
                        u.getJobTitle() != null ? u.getJobTitle() : "N/A",
                        u.isEnabled() ? "ACTIVE" : "INACTIVE",
                        roles,
                        perms,
                        u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FORMATTER) : "N/A",
                        u.getLastLoginAt() != null ? u.getLastLoginAt().format(DATE_FORMATTER) : "Never"
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating users CSV report: " + e.getMessage(), e);
        }
        return sw.toString().getBytes();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Roles & Permissions CSV Report", resource = "/api/reports/roles/csv")
    public byte[] generateRolesAndPermissionsCsv() {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"Role ID", "Role Name", "Description", "Is System Role", "Permission ID", "Permission Name", "Permission Category", "Is Elevated"});

            List<Role> roles = roleRepository.findAll();
            for (Role role : roles) {
                for (Permission perm : role.getPermissions()) {
                    writer.writeNext(new String[]{
                            String.valueOf(role.getId()),
                            role.getName(),
                            role.getDescription() != null ? role.getDescription() : "",
                            String.valueOf(role.isSystemRole()),
                            String.valueOf(perm.getId()),
                            perm.getName(),
                            perm.getCategory() != null ? perm.getCategory() : "",
                            String.valueOf(perm.isElevated())
                    });
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating roles CSV report: " + e.getMessage(), e);
        }
        return sw.toString().getBytes();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Audit Activity CSV Report", resource = "/api/reports/audit/csv")
    public byte[] generateAuditActivityCsv() {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"ID", "Timestamp", "Username", "Action", "Status", "IP Address", "Resource Accessed", "Description", "Details"});

            List<AuditLog> logs = auditLogRepository.findAll();
            for (AuditLog log : logs) {
                writer.writeNext(new String[]{
                        String.valueOf(log.getId()),
                        log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "",
                        log.getUsername(),
                        log.getAction(),
                        log.getStatus(),
                        log.getIpAddress() != null ? log.getIpAddress() : "",
                        log.getResourceAccessed() != null ? log.getResourceAccessed() : "",
                        log.getDescription() != null ? log.getDescription() : "",
                        log.getDetails() != null ? log.getDetails() : ""
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating audit CSV report: " + e.getMessage(), e);
        }
        return sw.toString().getBytes();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Privilege Creep CSV Report", resource = "/api/reports/privilege-creep/csv")
    public byte[] generatePrivilegeCreepCsv() {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"Review ID", "User ID", "Username", "Flagged Permission", "Associated Role", "Inactivity Days", "Status", "Flagged Reason", "Recommended Action", "Reviewed By", "Notes", "Created At"});

            List<PrivilegeReview> reviews = privilegeReviewRepository.findAll();
            for (PrivilegeReview r : reviews) {
                writer.writeNext(new String[]{
                        String.valueOf(r.getId()),
                        String.valueOf(r.getUserId()),
                        r.getUsername(),
                        r.getPermissionName(),
                        r.getAssociatedRoleName() != null ? r.getAssociatedRoleName() : "N/A",
                        String.valueOf(r.getInactivityDays()),
                        r.getStatus(),
                        r.getFlaggedReason(),
                        r.getRecommendedAction() != null ? r.getRecommendedAction() : "",
                        r.getReviewedBy() != null ? r.getReviewedBy() : "Pending Review",
                        r.getReviewNotes() != null ? r.getReviewNotes() : "",
                        r.getCreatedAt() != null ? r.getCreatedAt().format(DATE_FORMATTER) : ""
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating privilege creep CSV report: " + e.getMessage(), e);
        }
        return sw.toString().getBytes();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Risk Assessment CSV Report", resource = "/api/reports/risk/csv")
    public byte[] generateRiskAssessmentCsv() {
        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{"ID", "User ID", "Username", "Full Name", "Department", "Risk Score", "Classification", "Unused Privileges Count", "Dormant Permissions", "Days Inactive", "Risk Explanation", "Recommended Action", "Assessed At"});

            List<RiskAssessment> list = riskAssessmentRepository.findAllByOrderByRiskScoreDesc();
            for (RiskAssessment ra : list) {
                writer.writeNext(new String[]{
                        String.valueOf(ra.getId()),
                        String.valueOf(ra.getUserId()),
                        ra.getUsername(),
                        ra.getFullName() != null ? ra.getFullName() : "",
                        ra.getDepartment() != null ? ra.getDepartment() : "",
                        String.valueOf(ra.getRiskScore()),
                        ra.getRiskClassification(),
                        String.valueOf(ra.getUnusedElevatedPermissionsCount()),
                        ra.getUnusedPermissions() != null ? ra.getUnusedPermissions() : "None",
                        ra.getDaysSinceLastPrivilegedActivity() != null ? String.valueOf(ra.getDaysSinceLastPrivilegedActivity()) : "N/A",
                        ra.getRiskExplanation() != null ? ra.getRiskExplanation() : "",
                        ra.getRecommendedAction() != null ? ra.getRecommendedAction() : "",
                        ra.getAssessedAt() != null ? ra.getAssessedAt().format(DATE_FORMATTER) : ""
                });
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generating risk CSV report: " + e.getMessage(), e);
        }
        return sw.toString().getBytes();
    }

    // ==========================================
    // PDF REPORTS (OpenPDF)
    // ==========================================

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Executive Security Summary PDF Report", resource = "/api/reports/executive-summary/pdf")
    public byte[] generateExecutiveSummaryPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Header Banner
            addReportHeader(document, "EXECUTIVE SECURITY & IDENTITY GOVERNANCE SUMMARY",
                    "Enterprise RBAC and Privilege Creep Audit Overview");

            List<User> users = userRepository.findAll();
            List<RiskAssessment> assessments = riskAssessmentRepository.findAllByOrderByRiskScoreDesc();

            long total = users.size();
            long high = assessments.stream().filter(a -> "HIGH".equalsIgnoreCase(a.getRiskClassification())).count();
            long critical = assessments.stream().filter(a -> "CRITICAL".equalsIgnoreCase(a.getRiskClassification())).count();
            long medium = assessments.stream().filter(a -> "MEDIUM".equalsIgnoreCase(a.getRiskClassification())).count();
            long low = assessments.stream().filter(a -> "LOW".equalsIgnoreCase(a.getRiskClassification())).count();

            // Executive Metrics Table
            PdfPTable metricTable = new PdfPTable(4);
            metricTable.setWidthPercentage(100);
            metricTable.setSpacingBefore(15);
            metricTable.setSpacingAfter(20);

            addMetricCell(metricTable, "TOTAL IDENTITIES", String.valueOf(total), new Color(13, 110, 253));
            addMetricCell(metricTable, "CRITICAL RISK", String.valueOf(critical), new Color(220, 53, 69));
            addMetricCell(metricTable, "HIGH RISK", String.valueOf(high), new Color(253, 126, 20));
            addMetricCell(metricTable, "MEDIUM / LOW", medium + " / " + low, new Color(25, 135, 84));

            document.add(metricTable);

            // Findings Paragraph
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(15, 23, 42));
            Paragraph secTitle = new Paragraph("Key Identity Risk Findings & Recommendations", sectionFont);
            secTitle.setSpacingAfter(10);
            document.add(secTitle);

            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);
            document.add(new Paragraph("This executive audit identifies accounts exhibiting privilege accumulation, excessive administrative rights, and dormant elevated permissions that breach the 30-day inactivity threshold. Accounts categorized as CRITICAL or HIGH require immediate remediation by revoking dormant permissions.", bodyFont));
            document.add(new Paragraph(" ", bodyFont));

            // High & Critical Risk User Breakdown
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2.5f, 2f, 1.5f, 1.8f, 3.5f});

            addTableHeader(table, new String[]{"Username", "Department", "Privilege Score", "Class", "Dormant Perms", "Recommended Action"});

            for (RiskAssessment ra : assessments) {
                if ("CRITICAL".equalsIgnoreCase(ra.getRiskClassification()) || "HIGH".equalsIgnoreCase(ra.getRiskClassification())) {
                    table.addCell(createCell(ra.getUsername(), false));
                    table.addCell(createCell(ra.getDepartment() != null ? ra.getDepartment() : "General", false));
                    table.addCell(createCell(ra.getRiskScore() + " / 100", false));
                    table.addCell(createRiskBadgeCell(ra.getRiskClassification()));
                    table.addCell(createCell(String.valueOf(ra.getUnusedElevatedPermissionsCount()), false));
                    table.addCell(createCell(ra.getRecommendedAction(), false));
                }
            }

            document.add(table);
            addReportFooter(document);
            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error rendering executive summary PDF: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Over-Privileged Account PDF Report", resource = "/api/reports/over-privileged/pdf")
    public byte[] generateOverPrivilegedReportPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addReportHeader(document, "OVER-PRIVILEGED ACCOUNTS COMPLIANCE REPORT",
                    "Detailed Analysis of Accounts Exceeding Least Privilege Baseline");

            List<RiskAssessment> assessments = riskAssessmentRepository.findAllByOrderByRiskScoreDesc();

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2.5f, 1.8f, 1.8f, 5.5f});
            table.setSpacingBefore(15);

            addTableHeader(table, new String[]{"Identity", "Department", "Score", "Class", "Risk Factor Breakdown"});

            for (RiskAssessment ra : assessments) {
                if (ra.getRiskScore() >= 30) { // Medium, High, Critical
                    table.addCell(createCell(ra.getUsername() + "\n(" + ra.getFullName() + ")", false));
                    table.addCell(createCell(ra.getDepartment(), false));
                    table.addCell(createCell(ra.getRiskScore() + " / 100", false));
                    table.addCell(createRiskBadgeCell(ra.getRiskClassification()));
                    table.addCell(createCell(ra.getRiskExplanation(), false));
                }
            }

            document.add(table);
            addReportFooter(document);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error rendering over-privileged PDF: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Unused Elevated Privileges PDF Report", resource = "/api/reports/unused-privileges/pdf")
    public byte[] generateUnusedPrivilegesReportPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addReportHeader(document, "UNUSED ELEVATED PRIVILEGES & CREEP REPORT",
                    "Flagged Permissions Inactive for >= 30 Consecutive Days");

            List<PrivilegeReview> reviews = privilegeReviewRepository.findAll();

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2.5f, 2.2f, 1.5f, 2f, 3.5f});
            table.setSpacingBefore(15);

            addTableHeader(table, new String[]{"Username", "Permission Name", "Associated Role", "Inactivity", "Status", "Flagged Reason"});

            for (PrivilegeReview r : reviews) {
                table.addCell(createCell(r.getUsername(), false));
                table.addCell(createCell(r.getPermissionName(), false));
                table.addCell(createCell(r.getAssociatedRoleName(), false));
                table.addCell(createCell(r.getInactivityDays() + " days", false));

                // Status cell
                Color statusColor = "APPROVED_REVOKED".equalsIgnoreCase(r.getStatus()) ? new Color(25, 135, 84) :
                        "PENDING".equalsIgnoreCase(r.getStatus()) ? new Color(220, 53, 69) : new Color(108, 117, 125);
                Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, statusColor);
                PdfPCell cell = new PdfPCell(new Phrase(r.getStatus(), statusFont));
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);

                table.addCell(createCell(r.getFlaggedReason(), false));
            }

            document.add(table);
            addReportFooter(document);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error rendering unused privileges PDF: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    @Transactional(readOnly = true)
    @Auditable(action = "REPORT_GENERATE", description = "Generated Audit Compliance PDF Report", resource = "/api/reports/audit-compliance/pdf")
    public byte[] generateAuditComplianceReportPdf() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 40, 40);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            addReportHeader(document, "IDENTITY AUDIT TRAIL COMPLIANCE REPORT",
                    "Cryptographic & Forensic Record of Access Control Events");

            List<AuditLog> logs = auditLogRepository.findTop15ByOrderByTimestampDesc();

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 2f, 2.5f, 1.5f, 4.5f});
            table.setSpacingBefore(15);

            addTableHeader(table, new String[]{"Timestamp", "Actor", "Action", "Status", "Description"});

            for (AuditLog log : logs) {
                table.addCell(createCell(log.getTimestamp() != null ? log.getTimestamp().format(DATE_FORMATTER) : "", false));
                table.addCell(createCell(log.getUsername(), false));
                table.addCell(createCell(log.getAction(), false));

                Color statusColor = "SUCCESS".equalsIgnoreCase(log.getStatus()) ? new Color(25, 135, 84) : new Color(220, 53, 69);
                Font sFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, statusColor);
                PdfPCell cell = new PdfPCell(new Phrase(log.getStatus(), sFont));
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(6);
                table.addCell(cell);

                table.addCell(createCell(log.getDescription(), false));
            }

            document.add(table);
            addReportFooter(document);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error rendering audit compliance PDF: " + e.getMessage(), e);
        }
        return out.toByteArray();
    }

    // Helper methods for PDF formatting
    private void addReportHeader(Document document, String title, String subtitle) throws DocumentException {
        Font brandFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new Color(15, 23, 42));
        Paragraph brand = new Paragraph("ENTERPRISE IDENTITY AUDITOR", brandFont);
        brand.setAlignment(Element.ALIGN_LEFT);
        document.add(brand);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(30, 58, 138));
        Paragraph mainTitle = new Paragraph(title, titleFont);
        mainTitle.setSpacingBefore(5);
        document.add(mainTitle);

        Font subFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.GRAY);
        Paragraph sub = new Paragraph(subtitle + " | Generated: " + LocalDateTime.now().format(DATE_FORMATTER), subFont);
        sub.setSpacingAfter(10);
        document.add(sub);

        // Thin divider line
        Paragraph divider = new Paragraph("________________________________________________________________________________");
        divider.setSpacingAfter(15);
        document.add(divider);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
        Color headerBg = new Color(15, 23, 42);

        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(headerBg);
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String text, boolean bold) {
        Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        if (bold) {
            font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, Color.BLACK);
        }
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private PdfPCell createRiskBadgeCell(String classification) {
        Color badgeColor;
        if ("CRITICAL".equalsIgnoreCase(classification)) {
            badgeColor = new Color(220, 53, 69);
        } else if ("HIGH".equalsIgnoreCase(classification)) {
            badgeColor = new Color(253, 126, 20);
        } else if ("MEDIUM".equalsIgnoreCase(classification)) {
            badgeColor = new Color(255, 193, 7);
        } else {
            badgeColor = new Color(25, 135, 84);
        }

        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, badgeColor);
        PdfPCell cell = new PdfPCell(new Phrase(classification, font));
        cell.setPadding(5);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }

    private void addMetricCell(PdfPTable table, String title, String value, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(248, 249, 250));
        cell.setPadding(10);
        cell.setBorderColor(new Color(222, 226, 230));

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.GRAY);
        Font valFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, color);

        Paragraph pTitle = new Paragraph(title, titleFont);
        Paragraph pVal = new Paragraph(value, valFont);
        cell.addElement(pTitle);
        cell.addElement(pVal);

        table.addCell(cell);
    }

    private void addReportFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph("\nConfidential Document - Generated by Enterprise RBAC & Identity Auditor. Authorized Personnel Only.",
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);
    }
}
