import React, { useState, useEffect } from 'react';
import { auditService } from '../services/api';
import { useAuth } from '../context/AuthContext';
import {
  FileText,
  Search,
  Filter,
  RefreshCw,
  Eye,
  ShieldAlert,
  CheckCircle,
  XCircle,
  Calendar,
} from 'lucide-react';

const AuditLogs = () => {
  const { isPrivileged } = useAuth();
  const [logs, setLogs] = useState([]);
  const [actions, setActions] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [username, setUsername] = useState('');
  const [action, setAction] = useState('');
  const [status, setStatus] = useState('');
  const [selectedLog, setSelectedLog] = useState(null);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      if (isPrivileged()) {
        const params = {};
        if (username) params.username = username;
        if (action) params.action = action;
        if (status) params.status = status;

        const res = await auditService.getAuditLogs(params);
        setLogs(res.data);
      } else {
        const res = await auditService.getMyHistory();
        setLogs(res.data);
      }
    } catch (err) {
      console.error('Failed to load audit logs:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchActions = async () => {
    if (isPrivileged()) {
      try {
        const res = await auditService.getDistinctActions();
        setActions(res.data);
      } catch (err) {
        console.error('Failed to load actions:', err);
      }
    }
  };

  useEffect(() => {
    fetchActions();
  }, []);

  useEffect(() => {
    fetchLogs();
  }, [username, action, status]);

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Forensic Identity Audit Trail</h3>
          <p className="text-muted small mb-0">
            Immutable log of all authentication, administrative, privilege changes, and access control events
          </p>
        </div>
        <button
          onClick={fetchLogs}
          disabled={loading}
          className="btn btn-outline-primary d-flex align-items-center gap-2 shadow-sm"
        >
          <RefreshCw size={15} className={loading ? 'spin-icon' : ''} /> Refresh Trail
        </button>
      </div>

      {/* Filter Bar (Only shown for privileged users) */}
      {isPrivileged() && (
        <div className="card shadow-sm border-0 mb-4 p-3">
          <div className="row g-3">
            <div className="col-12 col-md-4">
              <div className="input-group">
                <span className="input-group-text bg-light border-end-0 text-muted"><Search size={16} /></span>
                <input
                  type="text"
                  className="form-control bg-light border-start-0"
                  placeholder="Filter by actor username..."
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>
            </div>
            <div className="col-12 col-md-4">
              <select
                className="form-select bg-light"
                value={action}
                onChange={(e) => setAction(e.target.value)}
              >
                <option value="">All Forensic Actions</option>
                {actions.map((act) => (
                  <option key={act} value={act}>{act}</option>
                ))}
              </select>
            </div>
            <div className="col-12 col-md-4">
              <select
                className="form-select bg-light"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="">All Result Statuses</option>
                <option value="SUCCESS">SUCCESS</option>
                <option value="FAILURE">FAILURE</option>
                <option value="UNAUTHORIZED">UNAUTHORIZED ATTEMPT</option>
              </select>
            </div>
          </div>
        </div>
      )}

      {/* Audit Logs Table */}
      <div className="card shadow-sm border-0 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Actor (Username)</th>
                <th>Action</th>
                <th>Resource Target</th>
                <th>Status</th>
                <th>IP Origin</th>
                <th className="text-end">Forensic Detail</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Retrieving cryptographic audit events...
                  </td>
                </tr>
              ) : logs.length > 0 ? (
                logs.map((log) => (
                  <tr key={log.id}>
                    <td className="text-muted small">
                      {new Date(log.timestamp).toLocaleString()}
                    </td>
                    <td>
                      <span className="fw-semibold text-dark">@{log.username}</span>
                    </td>
                    <td>
                      <span className="badge bg-light text-dark border font-monospace" style={{ fontSize: '0.72rem' }}>
                        {log.action}
                      </span>
                    </td>
                    <td className="small font-monospace text-secondary text-truncate" style={{ maxWidth: '180px' }}>
                      {log.resourceAccessed || 'N/A'}
                    </td>
                    <td>
                      {log.status === 'SUCCESS' ? (
                        <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 d-inline-flex align-items-center gap-1">
                          <CheckCircle size={12} /> SUCCESS
                        </span>
                      ) : log.status === 'UNAUTHORIZED' ? (
                        <span className="badge bg-danger text-white border border-danger d-inline-flex align-items-center gap-1">
                          <ShieldAlert size={12} /> UNAUTHORIZED
                        </span>
                      ) : (
                        <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 d-inline-flex align-items-center gap-1">
                          <XCircle size={12} /> FAILURE
                        </span>
                      )}
                    </td>
                    <td className="small text-muted font-monospace">{log.ipAddress || '127.0.0.1'}</td>
                    <td className="text-end">
                      <button
                        onClick={() => setSelectedLog(log)}
                        className="btn btn-sm btn-outline-secondary py-1 px-2.5"
                      >
                        <Eye size={13} className="me-1" /> Inspect
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">
                    No audit records found matching current query parameters.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Forensic Inspection Modal */}
      {selectedLog && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <div className="modal-header border-bottom py-3">
                <div className="d-flex align-items-center gap-2">
                  <FileText size={20} className="text-primary" />
                  <h5 className="modal-title fw-bold text-dark">Audit Event Payload #{selectedLog.id}</h5>
                </div>
                <button type="button" className="btn-close" onClick={() => setSelectedLog(null)}></button>
              </div>
              <div className="modal-body p-4">
                <div className="row g-3 mb-3">
                  <div className="col-6">
                    <span className="text-muted small">Event Timestamp:</span>
                    <div className="fw-semibold">{new Date(selectedLog.timestamp).toString()}</div>
                  </div>
                  <div className="col-6">
                    <span className="text-muted small">Origin IP Address:</span>
                    <div className="fw-semibold font-monospace">{selectedLog.ipAddress}</div>
                  </div>
                  <div className="col-6">
                    <span className="text-muted small">Authenticated Actor:</span>
                    <div className="fw-semibold">@{selectedLog.username} (ID: {selectedLog.userId || 'N/A'})</div>
                  </div>
                  <div className="col-6">
                    <span className="text-muted small">Result Status:</span>
                    <div>
                      <span className={`badge ${selectedLog.status === 'SUCCESS' ? 'bg-success' : 'bg-danger'}`}>
                        {selectedLog.status}
                      </span>
                    </div>
                  </div>
                  <div className="col-12">
                    <span className="text-muted small">Resource URI Target:</span>
                    <div className="font-monospace text-primary bg-light p-2 rounded border small">
                      {selectedLog.resourceAccessed || 'N/A'}
                    </div>
                  </div>
                  <div className="col-12">
                    <span className="text-muted small">Action Summary:</span>
                    <div className="fw-semibold text-dark">{selectedLog.description}</div>
                  </div>
                  <div className="col-12">
                    <span className="text-muted small">Sanitized Event Details:</span>
                    <pre className="bg-dark text-light p-3 rounded-3 small mb-0 font-monospace overflow-auto" style={{ maxHeight: '200px' }}>
                      {selectedLog.details || 'No additional payload metadata attached.'}
                    </pre>
                  </div>
                </div>
              </div>
              <div className="modal-footer border-top py-2.5">
                <button type="button" className="btn btn-secondary" onClick={() => setSelectedLog(null)}>
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AuditLogs;
