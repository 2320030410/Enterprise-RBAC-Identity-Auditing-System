import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { riskService } from '../services/api';
import { Shield, RefreshCw, LogOut, User, Bell, CheckCircle } from 'lucide-react';

const Navbar = ({ onScanComplete }) => {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const [scanning, setScanning] = useState(false);
  const [scanMessage, setScanMessage] = useState(null);

  const handleScan = async () => {
    setScanning(true);
    setScanMessage(null);
    try {
      await riskService.runScan();
      setScanMessage('Scan complete! Risk scores updated.');
      if (onScanComplete) {
        onScanComplete();
      }
      setTimeout(() => setScanMessage(null), 4000);
    } catch (err) {
      console.error('Scan failed:', err);
    } finally {
      setScanning(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="top-navbar d-flex align-items-center justify-content-between">
      <div className="d-flex align-items-center gap-3">
        <div className="d-flex align-items-center gap-2">
          <span className="badge bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25 px-2.5 py-1.5 fw-semibold d-flex align-items-center gap-1">
            <Shield size={14} /> Production IAM Realm
          </span>
          {scanMessage && (
            <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 px-2.5 py-1.5 fw-semibold d-flex align-items-center gap-1 animate__animated animate__fadeIn">
              <CheckCircle size={14} /> {scanMessage}
            </span>
          )}
        </div>
      </div>

      <div className="d-flex align-items-center gap-3">
        {isAdmin() && (
          <button
            onClick={handleScan}
            disabled={scanning}
            className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1.5 shadow-sm fw-semibold"
          >
            <RefreshCw size={14} className={scanning ? 'spin-icon' : ''} />
            <span>{scanning ? 'Scanning Identities...' : 'Run Privilege Creep Scan'}</span>
          </button>
        )}

        <div className="dropdown">
          <button
            className="btn btn-light btn-sm d-flex align-items-center gap-2 border"
            type="button"
            data-bs-toggle="dropdown"
            aria-expanded="false"
            onClick={handleLogout}
            title="Sign out of session"
          >
            <div
              className="rounded-circle bg-dark text-white d-flex align-items-center justify-content-center fw-bold"
              style={{ width: '28px', height: '28px', fontSize: '0.8rem' }}
            >
              {user?.username?.charAt(0).toUpperCase() || 'U'}
            </div>
            <span className="fw-semibold small d-none d-md-inline">{user?.username}</span>
            <LogOut size={14} className="text-danger ms-1" />
          </button>
        </div>
      </div>
    </header>
  );
};

export default Navbar;
