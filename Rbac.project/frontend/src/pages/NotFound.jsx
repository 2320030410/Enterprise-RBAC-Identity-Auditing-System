import React from 'react';
import { Link } from 'react-router-dom';
import { FileQuestion, ArrowLeft } from 'lucide-react';

const NotFound = () => {
  return (
    <div className="d-flex align-items-center justify-content-center min-vh-100 bg-light p-4">
      <div className="card shadow-lg border-0 rounded-4 text-center p-5" style={{ maxWidth: '480px' }}>
        <div className="d-inline-flex p-3 rounded-circle bg-secondary bg-opacity-10 text-secondary mx-auto mb-3">
          <FileQuestion size={48} />
        </div>
        <h2 className="fw-bold text-dark mb-2">404 - Not Found</h2>
        <p className="text-muted small mb-4">
          The requested identity endpoint or dashboard view does not exist on this server.
        </p>
        <div>
          <Link to="/" className="btn btn-primary d-inline-flex align-items-center gap-2 shadow-sm">
            <ArrowLeft size={16} /> Return to Home
          </Link>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
