import React from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import {
  Shield,
  LayoutDashboard,
  Users,
  KeyRound,
  ShieldCheck,
  FileText,
  AlertTriangle,
  UserCheck,
  DownloadCloud,
  User,
  LogOut,
  Sliders,
} from 'lucide-react';

const Sidebar = () => {
  const { user, logout, isPrivileged } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="brand">
        <Shield size={26} className="text-info" />
        <div className="d-flex flex-column">
          <span className="lh-1" style={{ letterSpacing: '0.02em' }}>IdentityAuditor</span>
          <span style={{ fontSize: '0.65rem', color: '#38bdf8', fontWeight: 600 }}>ENTERPRISE IGA</span>
        </div>
      </div>

      <div className="px-3 py-3 border-bottom border-secondary border-opacity-25">
        <div className="d-flex align-items-center gap-2">
          <div
            className="rounded-circle bg-primary bg-opacity-25 text-info d-flex align-items-center justify-content-center fw-bold"
            style={{ width: '36px', height: '36px', fontSize: '0.9rem' }}
          >
            {user?.fullName?.charAt(0) || user?.username?.charAt(0) || 'U'}
          </div>
          <div className="overflow-hidden">
            <div className="text-white text-truncate fw-semibold small">{user?.fullName || user?.username}</div>
            <div className="badge bg-secondary bg-opacity-50 text-light px-2 py-0.5" style={{ fontSize: '0.68rem' }}>
              {user?.roles?.[0]?.replace('ROLE_', '') || 'USER'}
            </div>
          </div>
        </div>
      </div>

      <nav className="mt-3">
        {isPrivileged() ? (
          <>
            <div className="sidebar-category">Governance & Analytics</div>
            <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <LayoutDashboard size={18} />
              <span>SOC Dashboard</span>
            </NavLink>
            <NavLink to="/risk-analysis" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <AlertTriangle size={18} />
              <span>Risk Analysis</span>
            </NavLink>
            <NavLink to="/privilege-review" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <UserCheck size={18} />
              <span>Privilege Reviews</span>
            </NavLink>

            <div className="sidebar-category">Access Control</div>
            <NavLink to="/users" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <Users size={18} />
              <span>User Directory</span>
            </NavLink>
            <NavLink to="/roles" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <KeyRound size={18} />
              <span>Role Management</span>
            </NavLink>
            <NavLink to="/permissions" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <ShieldCheck size={18} />
              <span>Permission Matrix</span>
            </NavLink>

            <div className="sidebar-category">Forensics & Reports</div>
            <NavLink to="/audit-logs" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <FileText size={18} />
              <span>Audit Logs</span>
            </NavLink>
            <NavLink to="/reports" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <DownloadCloud size={18} />
              <span>Compliance Reports</span>
            </NavLink>
          </>
        ) : (
          <>
            <div className="sidebar-category">Employee Portal</div>
            <NavLink to="/employee-dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <LayoutDashboard size={18} />
              <span>My Workspace</span>
            </NavLink>
            <NavLink to="/audit-logs" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
              <FileText size={18} />
              <span>My Activity Log</span>
            </NavLink>
          </>
        )}

        <div className="sidebar-category">Account</div>
        <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
          <User size={18} />
          <span>My Profile</span>
        </NavLink>
      </nav>

      <div className="p-3 mt-auto border-top border-secondary border-opacity-25" style={{ position: 'sticky', bottom: 0, backgroundColor: 'var(--primary-navy)' }}>
        <button
          onClick={handleLogout}
          className="btn btn-outline-danger w-100 btn-sm d-flex align-items-center justify-content-center gap-2"
        >
          <LogOut size={16} />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  );
};

export default Sidebar;
