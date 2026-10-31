import React, { useState, useEffect } from 'react';
import { dashboardService } from '../services/api';
import RiskBadge from '../components/RiskBadge';
import {
  User,
  Shield,
  KeyRound,
  ShieldCheck,
  Clock,
  Building,
  Briefcase,
  Activity,
  CheckCircle2,
} from 'lucide-react';

const EmployeeDashboard = () => {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEmployeeData = async () => {
      try {
        const res = await dashboardService.getEmployeeDashboard();
        setData(res.data);
      } catch (err) {
        console.error('Failed to load employee dashboard:', err);
      } finally {
        setLoading(false);
      }
    };

    fetchEmployeeData();
  }, []);

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading Employee Workspace...</span>
        </div>
      </div>
    );
  }

  const user = data?.user;

  return (
    <div className="container-fluid p-0">
      {/* Welcome Banner */}
      <div className="card shadow-sm border-0 mb-4 bg-primary text-white overflow-hidden" style={{ borderRadius: '16px' }}>
        <div className="card-body p-4 p-md-5 position-relative">
          <div className="d-flex align-items-center gap-3 mb-2">
            <span className="badge bg-white text-primary px-3 py-1 fw-bold rounded-pill">
              Employee Self-Service Portal
            </span>
            <span className="badge bg-success bg-opacity-75 text-white px-2.5 py-1 rounded-pill d-flex align-items-center gap-1">
              <CheckCircle2 size={13} /> Account Verified
            </span>
          </div>
          <h2 className="fw-bold mb-1">Welcome back, {user?.fullName || user?.username}!</h2>
          <p className="text-white-50 mb-0">
            Inspect your assigned identity roles, principle-of-least-privilege entitlements, and security activity trail.
          </p>
        </div>
      </div>

      {/* Top 3 Identity Cards */}
      <div className="row g-4 mb-4">
        {/* Profile Card */}
        <div className="col-12 col-md-4">
          <div className="card shadow-sm border-0 h-100 p-4">
            <div className="d-flex align-items-center gap-3 mb-3">
              <div
                className="rounded-circle bg-primary bg-opacity-10 text-primary d-flex align-items-center justify-content-center fw-bold"
                style={{ width: '50px', height: '50px', fontSize: '1.2rem' }}
              >
                {user?.fullName?.charAt(0) || user?.username?.charAt(0)}
              </div>
              <div>
                <h5 className="fw-bold text-dark mb-0">{user?.fullName}</h5>
                <span className="text-muted small">@{user?.username}</span>
              </div>
            </div>

            <div className="list-group list-group-flush small">
              <div className="list-group-item px-0 py-2 border-0 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-1.5"><Building size={14} /> Department:</span>
                <span className="fw-semibold text-dark">{user?.department || 'General'}</span>
              </div>
              <div className="list-group-item px-0 py-2 border-0 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-1.5"><Briefcase size={14} /> Job Title:</span>
                <span className="fw-semibold text-dark">{user?.jobTitle || 'Associate'}</span>
              </div>
              <div className="list-group-item px-0 py-2 border-0 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-1.5"><Clock size={14} /> Last Login:</span>
                <span className="fw-semibold text-dark">
                  {user?.lastLoginAt ? new Date(user.lastLoginAt).toLocaleString() : 'First Session'}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Security Posture Status */}
        <div className="col-12 col-md-4">
          <div className="card shadow-sm border-0 h-100 p-4">
            <span className="text-muted small fw-semibold text-uppercase tracking-wider">Security Posture Status</span>
            <div className="mt-3 mb-2">
              <RiskBadge classification={data?.securityStatus} score={data?.riskScore} />
            </div>
            <p className="small text-muted mb-3">
              Risk score: <strong>{data?.riskScore || 0} / 100</strong>. Your account adheres to corporate Least Privilege guidelines.
            </p>
            <div className="progress" style={{ height: '8px' }}>
              <div
                className={`progress-bar ${
                  data?.riskScore > 60 ? 'bg-danger' : data?.riskScore > 30 ? 'bg-warning' : 'bg-success'
                }`}
                role="progressbar"
                style={{ width: `${Math.min(100, Math.max(10, data?.riskScore || 10))}%` }}
              ></div>
            </div>
          </div>
        </div>

        {/* Active Roles Summary */}
        <div className="col-12 col-md-4">
          <div className="card shadow-sm border-0 h-100 p-4">
            <span className="text-muted small fw-semibold text-uppercase tracking-wider">Assigned Security Roles</span>
            <div className="d-flex flex-wrap gap-2 mt-3">
              {data?.assignedRoles && data.assignedRoles.length > 0 ? (
                data.assignedRoles.map((role) => (
                  <span
                    key={role.id || role.name}
                    className="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 px-3 py-2 rounded-3 fw-semibold d-flex align-items-center gap-1"
                  >
                    <KeyRound size={14} /> {role.name?.replace('ROLE_', '') || role}
                  </span>
                ))
              ) : (
                <span className="badge bg-secondary">No roles assigned</span>
              )}
            </div>
            <p className="small text-muted mt-3 mb-0">
              Roles are managed centrally by your IT & Cybersecurity Administrator.
            </p>
          </div>
        </div>
      </div>

      {/* Permissions & Activity Trail Row */}
      <div className="row g-4">
        {/* Fine-Grained Permissions List */}
        <div className="col-12 col-lg-5">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-header bg-white py-3 border-bottom d-flex align-items-center gap-2">
              <ShieldCheck size={18} className="text-success" />
              <h5 className="fw-bold mb-0 text-dark">Effective Entitlements (Permissions)</h5>
            </div>
            <div className="card-body">
              <p className="text-muted small mb-3">
                Below are the granular privileges aggregated from your assigned corporate roles:
              </p>
              <div className="d-flex flex-wrap gap-2">
                {data?.assignedPermissions && data.assignedPermissions.length > 0 ? (
                  data.assignedPermissions.map((perm, idx) => (
                    <span
                      key={idx}
                      className="badge bg-light text-dark border px-3 py-2 rounded-2 font-monospace"
                      style={{ fontSize: '0.8rem' }}
                    >
                      {perm}
                    </span>
                  ))
                ) : (
                  <span className="text-muted small">No specific permissions granted.</span>
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Personal Forensic Activity Trail */}
        <div className="col-12 col-lg-7">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-header bg-white py-3 border-bottom d-flex align-items-center gap-2">
              <Activity size={18} className="text-primary" />
              <h5 className="fw-bold mb-0 text-dark">Personal Forensic Activity Trail</h5>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Action</th>
                      <th>Description</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data?.personalActivity && data.personalActivity.length > 0 ? (
                      data.personalActivity.slice(0, 8).map((log) => (
                        <tr key={log.id}>
                          <td className="text-muted small">
                            {new Date(log.timestamp).toLocaleString()}
                          </td>
                          <td>
                            <span className="badge bg-light text-dark border font-monospace" style={{ fontSize: '0.72rem' }}>
                              {log.action}
                            </span>
                          </td>
                          <td className="small text-truncate" style={{ maxWidth: '240px' }}>
                            {log.description}
                          </td>
                          <td>
                            <span
                              className={`badge ${
                                log.status === 'SUCCESS'
                                  ? 'bg-success bg-opacity-10 text-success'
                                  : 'bg-danger bg-opacity-10 text-danger'
                              }`}
                            >
                              {log.status}
                            </span>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="4" className="text-center py-4 text-muted small">
                          No recent personal activity logged.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default EmployeeDashboard;
