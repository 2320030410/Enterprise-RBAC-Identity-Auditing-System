import React, { useState, useEffect } from 'react';
import { privilegeReviewService } from '../services/api';
import {
  UserCheck,
  ShieldAlert,
  CheckCircle,
  XCircle,
  Clock,
  Filter,
  RefreshCw,
  AlertTriangle,
  UserX,
} from 'lucide-react';

const PrivilegeReview = () => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusFilter, setStatusFilter] = useState('PENDING');

  // Action modal state
  const [activeReview, setActiveReview] = useState(null);
  const [actionType, setActionType] = useState('APPROVE'); // APPROVE or REJECT
  const [notes, setNotes] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const fetchReviews = async () => {
    setLoading(true);
    try {
      const res = await privilegeReviewService.getReviews(statusFilter);
      setReviews(res.data);
    } catch (err) {
      console.error('Failed to load privilege reviews:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchReviews();
  }, [statusFilter]);

  const openActionModal = (review, type) => {
    setActiveReview(review);
    setActionType(type);
    setNotes(
      type === 'APPROVE'
        ? `Revoking ${review.permissionName} due to ${review.inactivityDays} days of dormancy.`
        : 'Business justification: required for emergency operational continuity.'
    );
  };

  const handleActionSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      if (actionType === 'APPROVE') {
        await privilegeReviewService.approveReview(activeReview.id, notes);
      } else {
        await privilegeReviewService.rejectReview(activeReview.id, notes);
      }
      setActiveReview(null);
      fetchReviews();
    } catch (err) {
      alert(err.response?.data?.message || 'Action failed');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Privilege Creep Review & Remediation</h3>
          <p className="text-muted small mb-0">
            Enforce Principle of Least Privilege by reviewing and revoking dormant administrative permissions
          </p>
        </div>
        <button onClick={fetchReviews} className="btn btn-outline-primary d-flex align-items-center gap-2 shadow-sm">
          <RefreshCw size={15} /> Refresh Reviews
        </button>
      </div>

      {/* Filter Tabs */}
      <div className="card shadow-sm border-0 mb-4 p-2 bg-light">
        <div className="d-flex gap-2">
          <button
            onClick={() => setStatusFilter('PENDING')}
            className={`btn btn-sm ${statusFilter === 'PENDING' ? 'btn-primary' : 'btn-outline-secondary'}`}
          >
            Pending Review ({reviews.filter((r) => r.status === 'PENDING').length || (statusFilter === 'PENDING' ? reviews.length : 0)})
          </button>
          <button
            onClick={() => setStatusFilter('APPROVED_REVOKED')}
            className={`btn btn-sm ${statusFilter === 'APPROVED_REVOKED' ? 'btn-success text-white' : 'btn-outline-secondary'}`}
          >
            Approved & Revoked
          </button>
          <button
            onClick={() => setStatusFilter('REJECTED_RETAINED')}
            className={`btn btn-sm ${statusFilter === 'REJECTED_RETAINED' ? 'btn-secondary text-white' : 'btn-outline-secondary'}`}
          >
            Retained with Justification
          </button>
          <button
            onClick={() => setStatusFilter('')}
            className={`btn btn-sm ${statusFilter === '' ? 'btn-dark' : 'btn-outline-secondary'}`}
          >
            All Reviews
          </button>
        </div>
      </div>

      {/* Reviews Table */}
      <div className="card shadow-sm border-0 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Identity</th>
                <th>Dormant Permission</th>
                <th>Associated Role</th>
                <th>Inactivity Duration</th>
                <th>Flagged Audit Rationale</th>
                <th>Review Status</th>
                <th className="text-end">Administrative Governance</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Fetching privilege review queue...
                  </td>
                </tr>
              ) : reviews.length > 0 ? (
                reviews.map((r) => (
                  <tr key={r.id}>
                    <td>
                      <div className="fw-semibold text-dark">@{r.username}</div>
                      <div className="text-muted small">User ID: #{r.userId}</div>
                    </td>
                    <td>
                      <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 font-monospace px-2.5 py-1.5">
                        {r.permissionName}
                      </span>
                    </td>
                    <td>
                      <span className="badge bg-light text-secondary border font-monospace">
                        {r.associatedRoleName || 'CUSTOM'}
                      </span>
                    </td>
                    <td>
                      <span className="d-flex align-items-center gap-1 text-danger fw-semibold small">
                        <Clock size={14} /> {r.inactivityDays} days
                      </span>
                    </td>
                    <td className="small text-secondary" style={{ maxWidth: '280px' }}>
                      {r.flaggedReason}
                    </td>
                    <td>
                      {r.status === 'PENDING' ? (
                        <span className="badge bg-warning bg-opacity-20 text-dark border border-warning">
                          PENDING ACTION
                        </span>
                      ) : r.status === 'APPROVED_REVOKED' ? (
                        <span className="badge bg-success bg-opacity-15 text-success border border-success d-inline-flex align-items-center gap-1">
                          <CheckCircle size={12} /> REVOKED
                        </span>
                      ) : (
                        <span className="badge bg-secondary bg-opacity-25 text-secondary border d-inline-flex align-items-center gap-1">
                          <CheckCircle size={12} /> RETAINED
                        </span>
                      )}
                    </td>
                    <td className="text-end">
                      {r.status === 'PENDING' ? (
                        <div className="btn-group btn-group-sm">
                          <button
                            onClick={() => openActionModal(r, 'APPROVE')}
                            className="btn btn-danger d-inline-flex align-items-center gap-1"
                            title="Approve removal of unused privilege"
                          >
                            <UserX size={13} /> Revoke
                          </button>
                          <button
                            onClick={() => openActionModal(r, 'REJECT')}
                            className="btn btn-outline-secondary d-inline-flex align-items-center gap-1"
                            title="Retain privilege with business justification"
                          >
                            Retain
                          </button>
                        </div>
                      ) : (
                        <span className="small text-muted" title={r.reviewNotes}>
                          By @{r.reviewedBy || 'Admin'}
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" className="text-center py-5 text-muted">
                    No privilege review tickets match current status filter.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Review Action Modal */}
      {activeReview && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <form onSubmit={handleActionSubmit}>
                <div className="modal-header border-bottom py-3">
                  <h5 className="modal-title fw-bold text-dark">
                    {actionType === 'APPROVE' ? 'Confirm Privilege Revocation' : 'Retain Privilege with Justification'}
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setActiveReview(null)}></button>
                </div>
                <div className="modal-body p-4">
                  <div className="p-3 bg-light rounded-3 mb-3 border small">
                    <div><strong>Target Identity:</strong> @{activeReview.username}</div>
                    <div><strong>Dormant Privilege:</strong> <code>{activeReview.permissionName}</code></div>
                    <div><strong>Associated Role:</strong> {activeReview.associatedRoleName}</div>
                    <div><strong>Inactivity Duration:</strong> {activeReview.inactivityDays} days without operational activity</div>
                  </div>

                  <div className="mb-3">
                    <label className="form-label small fw-semibold text-secondary">
                      {actionType === 'APPROVE' ? 'Audit Revocation Note:' : 'Required Business Justification:'}
                    </label>
                    <textarea
                      className="form-control"
                      rows="3"
                      value={notes}
                      onChange={(e) => setNotes(e.target.value)}
                      required
                    ></textarea>
                    <small className="text-muted">
                      {actionType === 'APPROVE'
                        ? 'Revoking this privilege will unassign the role from the user, immediately lowering their risk score.'
                        : 'Retaining this dormant permission will maintain elevated risk on the identity.'}
                    </small>
                  </div>
                </div>
                <div className="modal-footer border-top py-2.5">
                  <button type="button" className="btn btn-light" onClick={() => setActiveReview(null)}>Cancel</button>
                  <button
                    type="submit"
                    disabled={submitting}
                    className={`btn fw-semibold ${actionType === 'APPROVE' ? 'btn-danger' : 'btn-secondary'}`}
                  >
                    {submitting ? 'Executing...' : actionType === 'APPROVE' ? 'Revoke Privilege Now' : 'Save Justification & Retain'}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PrivilegeReview;
