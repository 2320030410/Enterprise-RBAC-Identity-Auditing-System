import React, { useState, useEffect } from 'react';
import { riskService, userService } from '../services/api';
import RiskBadge from '../components/RiskBadge';
import {
  AlertTriangle,
  RefreshCw,
  Search,
  Filter,
  ShieldCheck,
  ShieldAlert,
  Info,
  Clock,
  ArrowRight,
  CheckCircle,
} from 'lucide-react';
import { Link } from 'react-router-dom';

const RiskAnalysis = () => {
  const [assessments, setAssessments] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);

  // Filters
  const [classificationFilter, setClassificationFilter] = useState('');
  const [departmentFilter, setDepartmentFilter] = useState('');
  const [search, setSearch] = useState('');

  // Explanation Modal state
  const [selectedAssessment, setSelectedAssessment] = useState(null);

  const fetchAssessments = async () => {
    setLoading(true);
    try {
      const params = {};
      if (classificationFilter) params.classification = classificationFilter;
      if (departmentFilter) params.department = departmentFilter;
      if (search) params.search = search;

      const res = await riskService.getRiskAssessments(params);
      setAssessments(res.data);
    } catch (err) {
      console.error('Failed to load risk assessments:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchDepts = async () => {
    try {
      const res = await userService.getDepartments();
      setDepartments(res.data);
    } catch (err) {
      console.error('Failed to load departments:', err);
    }
  };

  useEffect(() => {
    fetchDepts();
  }, []);

  useEffect(() => {
    fetchAssessments();
  }, [classificationFilter, departmentFilter, search]);

  const handleRunScan = async () => {
    setScanning(true);
    try {
      await riskService.runScan();
      await fetchAssessments();
    } catch (err) {
      console.error('Scan execution failed:', err);
    } finally {
      setScanning(false);
    }
  };

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Identity Risk & Privilege Creep Analysis</h3>
          <p className="text-muted small mb-0">
            Algorithmic scoring (0–100) evaluating elevated entitlement volume, 30-day inactivity, and dormant privileges
          </p>
        </div>
        <button
          onClick={handleRunScan}
          disabled={scanning}
          className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
        >
          <RefreshCw size={16} className={scanning ? 'spin-icon' : ''} />
          <span>{scanning ? 'Recalculating Identity Risks...' : 'Run Privilege Creep Scan'}</span>
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="card shadow-sm border-0 mb-4 p-3">
        <div className="row g-3">
          <div className="col-12 col-md-5">
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0 text-muted"><Search size={16} /></span>
              <input
                type="text"
                className="form-control bg-light border-start-0"
                placeholder="Search by username or full name..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>
          <div className="col-12 col-md-4">
            <select
              className="form-select bg-light"
              value={classificationFilter}
              onChange={(e) => setClassificationFilter(e.target.value)}
            >
              <option value="">All Risk Tiers</option>
              <option value="CRITICAL">CRITICAL (80–100)</option>
              <option value="HIGH">HIGH (60–79)</option>
              <option value="MEDIUM">MEDIUM (30–59)</option>
              <option value="LOW">LOW (0–29)</option>
            </select>
          </div>
          <div className="col-12 col-md-3">
            <select
              className="form-select bg-light"
              value={departmentFilter}
              onChange={(e) => setDepartmentFilter(e.target.value)}
            >
              <option value="">All Departments</option>
              {departments.map((dept) => (
                <option key={dept} value={dept}>{dept}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Risk Assessments Table */}
      <div className="card shadow-sm border-0 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Identity</th>
                <th>Department</th>
                <th>Risk Score</th>
                <th>Classification</th>
                <th>Dormant Privileges</th>
                <th>Days Inactive</th>
                <th>Recommended Action</th>
                <th className="text-end">Remediation</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="8" className="text-center py-5 text-muted">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Computing identity risk vectors...
                  </td>
                </tr>
              ) : assessments.length > 0 ? (
                assessments.map((a) => (
                  <tr key={a.id}>
                    <td>
                      <div className="fw-semibold text-dark">{a.fullName || a.username}</div>
                      <div className="text-muted small">@{a.username}</div>
                    </td>
                    <td>
                      <span className="small text-secondary">{a.department || 'N/A'}</span>
                    </td>
                    <td>
                      <div className="d-flex align-items-center gap-2">
                        <span className="fw-bold font-monospace fs-6">{a.riskScore}</span>
                        <div className="progress flex-grow-1" style={{ height: '6px', minWidth: '50px' }}>
                          <div
                            className={`progress-bar ${
                              a.riskScore >= 80 ? 'bg-danger' : a.riskScore >= 60 ? 'bg-warning' : a.riskScore >= 30 ? 'bg-info' : 'bg-success'
                            }`}
                            style={{ width: `${a.riskScore}%` }}
                          ></div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <RiskBadge classification={a.riskClassification} />
                    </td>
                    <td>
                      {a.unusedElevatedPermissionsCount > 0 ? (
                        <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 d-inline-flex align-items-center gap-1">
                          <AlertTriangle size={12} /> {a.unusedElevatedPermissionsCount} dormant
                        </span>
                      ) : (
                        <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25">
                          0 dormant
                        </span>
                      )}
                    </td>
                    <td className="small text-muted font-monospace">
                      {a.daysSinceLastPrivilegedActivity !== null && a.daysSinceLastPrivilegedActivity !== undefined
                        ? `${a.daysSinceLastPrivilegedActivity} days`
                        : 'Active / None'}
                    </td>
                    <td>
                      <button
                        onClick={() => setSelectedAssessment(a)}
                        className="btn btn-link p-0 text-start text-decoration-none small text-truncate d-block"
                        style={{ maxWidth: '240px' }}
                      >
                        <Info size={14} className="text-primary me-1" />
                        <span className="text-dark">{a.recommendedAction}</span>
                      </button>
                    </td>
                    <td className="text-end">
                      {a.unusedElevatedPermissionsCount > 0 ? (
                        <Link
                          to="/privilege-review"
                          className="btn btn-sm btn-danger d-inline-flex align-items-center gap-1 shadow-sm"
                        >
                          Revoke <ArrowRight size={13} />
                        </Link>
                      ) : (
                        <button
                          onClick={() => setSelectedAssessment(a)}
                          className="btn btn-sm btn-outline-secondary"
                        >
                          Details
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="8" className="text-center py-5 text-muted">
                    No risk assessment entries found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Detailed Risk Explanation Modal */}
      {selectedAssessment && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <div className="modal-header border-bottom py-3">
                <div className="d-flex align-items-center gap-2">
                  <AlertTriangle size={20} className="text-warning" />
                  <h5 className="modal-title fw-bold text-dark">
                    Identity Risk Breakdown: @{selectedAssessment.username}
                  </h5>
                </div>
                <button type="button" className="btn-close" onClick={() => setSelectedAssessment(null)}></button>
              </div>
              <div className="modal-body p-4">
                <div className="d-flex justify-content-between align-items-center p-3 bg-light rounded-3 mb-3 border">
                  <div>
                    <span className="text-muted small">Assigned Risk Tier:</span>
                    <div className="mt-1">
                      <RiskBadge classification={selectedAssessment.riskClassification} score={selectedAssessment.riskScore} />
                    </div>
                  </div>
                  <div className="text-end">
                    <span className="text-muted small">Dormant Elevated Permissions:</span>
                    <div className="fw-bold fs-5 text-danger">{selectedAssessment.unusedElevatedPermissionsCount} detected</div>
                  </div>
                </div>

                <div className="mb-3">
                  <h6 className="fw-bold text-dark mb-1">Transparent Factor Scoring Breakdown:</h6>
                  <p className="text-muted small mb-2">
                    How the mathematical score ({selectedAssessment.riskScore}/100) was calculated by the algorithm:
                  </p>
                  <div className="bg-light p-3 rounded-3 border small font-monospace">
                    {selectedAssessment.riskExplanation}
                  </div>
                </div>

                {selectedAssessment.unusedPermissions && (
                  <div className="mb-3">
                    <h6 className="fw-bold text-dark mb-1">Specific Dormant Privileges Detected:</h6>
                    <div className="p-3 bg-danger bg-opacity-10 border border-danger border-opacity-25 rounded-3 text-danger small font-monospace">
                      {selectedAssessment.unusedPermissions}
                    </div>
                  </div>
                )}

                <div className="mb-2">
                  <h6 className="fw-bold text-dark mb-1">Recommended Least-Privilege Remediation:</h6>
                  <p className="text-secondary small mb-0">{selectedAssessment.recommendedAction}</p>
                </div>
              </div>
              <div className="modal-footer border-top py-2.5">
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedAssessment(null)}>
                  Close
                </button>
                {selectedAssessment.unusedElevatedPermissionsCount > 0 && (
                  <Link to="/privilege-review" className="btn btn-danger">
                    Proceed to Privilege Review Workflow
                  </Link>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RiskAnalysis;
