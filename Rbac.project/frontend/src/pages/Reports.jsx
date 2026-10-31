import React, { useState } from 'react';
import { reportService } from '../services/api';
import {
  DownloadCloud,
  FileSpreadsheet,
  FileText,
  ShieldCheck,
  AlertTriangle,
  Users,
  KeyRound,
  CheckCircle,
} from 'lucide-react';

const Reports = () => {
  const [downloading, setDownloading] = useState(null);

  const handleDownload = async (url, filename, key) => {
    setDownloading(key);
    try {
      await reportService.downloadReport(url, filename);
    } catch (err) {
      console.error('Failed to download report:', err);
      alert('Report download failed. Check network connection.');
    } finally {
      setDownloading(null);
    }
  };

  const csvReports = [
    {
      key: 'users_csv',
      title: 'User Access Directory Report',
      description: 'Comprehensive roster of all enterprise identities, assigned roles, effective permissions, and login telemetry.',
      url: '/reports/users/csv',
      filename: `user-access-report-${new Date().toISOString().split('T')[0]}.csv`,
      icon: Users,
    },
    {
      key: 'roles_csv',
      title: 'Roles & Entitlements Matrix',
      description: 'Role-to-permission mapping catalogue, highlighting system roles and elevated privilege boundaries.',
      url: '/reports/roles/csv',
      filename: `roles-permissions-report-${new Date().toISOString().split('T')[0]}.csv`,
      icon: KeyRound,
    },
    {
      key: 'audit_csv',
      title: 'Cryptographic Audit Activity Trail',
      description: 'Chronological export of authentication logs, user provisioning, role assignments, and authorization denials.',
      url: '/reports/audit/csv',
      filename: `audit-trail-report-${new Date().toISOString().split('T')[0]}.csv`,
      icon: FileSpreadsheet,
    },
    {
      key: 'creep_csv',
      title: 'Privilege Creep Review Records',
      description: 'Audit report of all flagged dormant permissions inactive for 30+ days, with reviewer notes and status.',
      url: '/reports/privilege-creep/csv',
      filename: `privilege-creep-report-${new Date().toISOString().split('T')[0]}.csv`,
      icon: AlertTriangle,
    },
    {
      key: 'risk_csv',
      title: 'Risk Assessment & Scoring Data',
      description: 'Tabular dataset of computed risk scores (0–100), classifications, factor breakdowns, and least privilege advice.',
      url: '/reports/risk/csv',
      filename: `risk-assessment-report-${new Date().toISOString().split('T')[0]}.csv`,
      icon: ShieldCheck,
    },
  ];

  const pdfReports = [
    {
      key: 'exec_pdf',
      title: 'Executive Security & Identity Governance Summary',
      description: 'Executive briefing document featuring identity hygiene metrics, risk tier distribution, and governance recommendations.',
      url: '/reports/executive-summary/pdf',
      filename: `executive-summary-${new Date().toISOString().split('T')[0]}.pdf`,
      badge: 'CISO / Board',
    },
    {
      key: 'over_pdf',
      title: 'Over-Privileged Accounts Compliance Report',
      description: 'Detailed analysis of accounts exceeding least privilege baselines with multi-factor risk justification.',
      url: '/reports/over-privileged/pdf',
      filename: `over-privileged-accounts-${new Date().toISOString().split('T')[0]}.pdf`,
      badge: 'Compliance Audit',
    },
    {
      key: 'unused_pdf',
      title: 'Unused Elevated Privileges & Creep Report',
      description: 'Technical listing of specific permissions exceeding the 30-day inactivity threshold across the organization.',
      url: '/reports/unused-privileges/pdf',
      filename: `unused-elevated-privileges-${new Date().toISOString().split('T')[0]}.pdf`,
      badge: 'Identity Security',
    },
    {
      key: 'audit_pdf',
      title: 'Identity Audit Trail Compliance Certification',
      description: 'Formal audit document verifying immutable capture of administrative actions and access control events.',
      url: '/reports/audit-compliance/pdf',
      filename: `audit-compliance-certification-${new Date().toISOString().split('T')[0]}.pdf`,
      badge: 'Forensic Record',
    },
  ];

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Identity Compliance & Security Reports</h3>
          <p className="text-muted small mb-0">
            Export actual database records and governance metrics as production-grade CSV datasets or formal PDF briefings
          </p>
        </div>
      </div>

      {/* PDF Reports Section */}
      <h5 className="fw-bold text-dark mb-3 d-flex align-items-center gap-2">
        <FileText size={20} className="text-danger" /> Formal Executive PDF Reports (OpenPDF)
      </h5>
      <div className="row g-4 mb-5">
        {pdfReports.map((report) => {
          const isCurrent = downloading === report.key;
          return (
            <div key={report.key} className="col-12 col-md-6">
              <div className="card shadow-sm border-0 h-100 p-4">
                <div className="d-flex justify-content-between align-items-start mb-2">
                  <div className="d-flex align-items-center gap-2">
                    <div className="icon-box bg-danger bg-opacity-10 text-danger">
                      <FileText size={22} />
                    </div>
                    <div>
                      <h6 className="fw-bold mb-0 text-dark">{report.title}</h6>
                      <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 small" style={{ fontSize: '0.68rem' }}>
                        {report.badge}
                      </span>
                    </div>
                  </div>
                </div>

                <p className="text-muted small my-3 flex-grow-1">
                  {report.description}
                </p>

                <div className="pt-3 border-top d-flex justify-content-between align-items-center">
                  <span className="small text-muted font-monospace">PDF Document</span>
                  <button
                    onClick={() => handleDownload(report.url, report.filename, report.key)}
                    disabled={isCurrent}
                    className="btn btn-outline-danger btn-sm d-flex align-items-center gap-1.5 shadow-sm fw-semibold"
                  >
                    <DownloadCloud size={15} />
                    <span>{isCurrent ? 'Generating PDF...' : 'Download PDF'}</span>
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* CSV Reports Section */}
      <h5 className="fw-bold text-dark mb-3 d-flex align-items-center gap-2">
        <FileSpreadsheet size={20} className="text-success" /> Tabular CSV Data Reports (OpenCSV)
      </h5>
      <div className="row g-4">
        {csvReports.map((report) => {
          const Icon = report.icon;
          const isCurrent = downloading === report.key;
          return (
            <div key={report.key} className="col-12 col-md-6 col-xl-4">
              <div className="card shadow-sm border-0 h-100 p-4">
                <div className="d-flex align-items-center gap-2 mb-2">
                  <div className="icon-box bg-success bg-opacity-10 text-success">
                    <Icon size={20} />
                  </div>
                  <h6 className="fw-bold mb-0 text-dark">{report.title}</h6>
                </div>

                <p className="text-muted small my-3 flex-grow-1">
                  {report.description}
                </p>

                <div className="pt-3 border-top d-flex justify-content-between align-items-center">
                  <span className="small text-muted font-monospace">CSV Spreadsheet</span>
                  <button
                    onClick={() => handleDownload(report.url, report.filename, report.key)}
                    disabled={isCurrent}
                    className="btn btn-outline-success btn-sm d-flex align-items-center gap-1.5 shadow-sm fw-semibold"
                  >
                    <DownloadCloud size={15} />
                    <span>{isCurrent ? 'Exporting CSV...' : 'Download CSV'}</span>
                  </button>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default Reports;
