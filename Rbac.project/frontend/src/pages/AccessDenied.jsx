import React from 'react';
import { Link } from 'react-router-dom';
import { ShieldAlert, ArrowLeft } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

const AccessDenied = () => {
  const { isPrivileged } = useAuth();

  return (
    <div className="d-flex align-items-center justify-content-center min-vh-100 bg-light p-4">
      <div className="card shadow-lg border-0 rounded-4 text-center p-5" style={{ maxWidth: '500px' }}>
        <div className="d-inline-flex p-3 rounded-circle bg-danger bg-opacity-10 text-danger mx-auto mb-3">
          <ShieldAlert size={48} />
        </div>
        <h2 className="fw-bold text-dark mb-2">403 - Access Denied</h2>
        <p className="text-muted small mb-4">
          Security Exception: Your current identity role does not possess the requisite authorization entitlement to view this administrative resource.
        </p>
        <div>
          <Link
            to={isPrivileged() ? '/dashboard' : '/employee-dashboard'}
            className="btn btn-primary d-inline-flex align-items-center gap-2 shadow-sm"
          >
            <ArrowLeft size={16} /> Return to Authorized Workspace
          </Link>
        </div>
      </div>
    </div>
  );
};

export default AccessDenied;
