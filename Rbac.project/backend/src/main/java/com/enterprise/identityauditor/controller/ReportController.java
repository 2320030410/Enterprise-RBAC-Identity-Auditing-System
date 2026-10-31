package com.enterprise.identityauditor.controller;

import com.enterprise.identityauditor.reporting.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    // ==========================================
    // CSV DOWNLOAD ENDPOINTS
    // ==========================================

    @GetMapping("/users/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadUsersCsv() {
        byte[] csv = reportService.generateUsersCsv();
        return buildFileResponse(csv, "users-report-" + LocalDate.now() + ".csv", "text/csv");
    }

    @GetMapping("/roles/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadRolesCsv() {
        byte[] csv = reportService.generateRolesAndPermissionsCsv();
        return buildFileResponse(csv, "roles-permissions-report-" + LocalDate.now() + ".csv", "text/csv");
    }

    @GetMapping("/audit/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadAuditCsv() {
        byte[] csv = reportService.generateAuditActivityCsv();
        return buildFileResponse(csv, "audit-activity-report-" + LocalDate.now() + ".csv", "text/csv");
    }

    @GetMapping("/privilege-creep/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadPrivilegeCreepCsv() {
        byte[] csv = reportService.generatePrivilegeCreepCsv();
        return buildFileResponse(csv, "privilege-creep-report-" + LocalDate.now() + ".csv", "text/csv");
    }

    @GetMapping("/risk/csv")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadRiskCsv() {
        byte[] csv = reportService.generateRiskAssessmentCsv();
        return buildFileResponse(csv, "risk-assessment-report-" + LocalDate.now() + ".csv", "text/csv");
    }

    // ==========================================
    // PDF DOWNLOAD ENDPOINTS
    // ==========================================

    @GetMapping("/executive-summary/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadExecutiveSummaryPdf() {
        byte[] pdf = reportService.generateExecutiveSummaryPdf();
        return buildFileResponse(pdf, "executive-summary-" + LocalDate.now() + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/over-privileged/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadOverPrivilegedPdf() {
        byte[] pdf = reportService.generateOverPrivilegedReportPdf();
        return buildFileResponse(pdf, "over-privileged-accounts-" + LocalDate.now() + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/unused-privileges/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadUnusedPrivilegesPdf() {
        byte[] pdf = reportService.generateUnusedPrivilegesReportPdf();
        return buildFileResponse(pdf, "unused-elevated-privileges-" + LocalDate.now() + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    @GetMapping("/audit-compliance/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR') or hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> downloadAuditCompliancePdf() {
        byte[] pdf = reportService.generateAuditComplianceReportPdf();
        return buildFileResponse(pdf, "audit-compliance-report-" + LocalDate.now() + ".pdf", MediaType.APPLICATION_PDF_VALUE);
    }

    private ResponseEntity<byte[]> buildFileResponse(byte[] content, String filename, String contentType) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .contentLength(content.length)
                .body(content);
    }
}
