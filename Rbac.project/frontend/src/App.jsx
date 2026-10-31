import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import DashboardLayout from './layouts/DashboardLayout';
import ProtectedRoute from './components/ProtectedRoute';

// Pages
import Login from './pages/Login';
import Register from './pages/Register';
import AdminDashboard from './pages/AdminDashboard';
import EmployeeDashboard from './pages/EmployeeDashboard';
import UserManagement from './pages/UserManagement';
import RoleManagement from './pages/RoleManagement';
import PermissionManagement from './pages/PermissionManagement';
import AuditLogs from './pages/AuditLogs';
import RiskAnalysis from './pages/RiskAnalysis';
import PrivilegeReview from './pages/PrivilegeReview';
import Reports from './pages/Reports';
import UserProfile from './pages/UserProfile';
import AccessDenied from './pages/AccessDenied';
import NotFound from './pages/NotFound';

// Root Dispatcher to automatically send user to their respective dashboard
const HomeRedirect = () => {
  const { user, isPrivileged, loading } = useAuth();

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center min-vh-100">
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/login" replace />;
  }

  return isPrivileged() ? <Navigate to="/dashboard" replace /> : <Navigate to="/employee-dashboard" replace />;
};

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Authentication Pages */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/access-denied" element={<AccessDenied />} />

          {/* Protected Application Routes inside DashboardLayout */}
          <Route
            element={
              <ProtectedRoute>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<HomeRedirect />} />

            {/* Admin SOC Dashboard */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER']}>
                  <AdminDashboard />
                </ProtectedRoute>
              }
            />

            {/* Employee Self-Service Dashboard */}
            <Route path="/employee-dashboard" element={<EmployeeDashboard />} />

            {/* User Directory & Lifecycle Management */}
            <Route
              path="/users"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER']}>
                  <UserManagement />
                </ProtectedRoute>
              }
            />

            {/* RBAC Role Configuration */}
            <Route
              path="/roles"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN']}>
                  <RoleManagement />
                </ProtectedRoute>
              }
            />

            {/* Granular Permission Catalog */}
            <Route
              path="/permissions"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER']}>
                  <PermissionManagement />
                </ProtectedRoute>
              }
            />

            {/* Forensic Audit Trail */}
            <Route path="/audit-logs" element={<AuditLogs />} />

            {/* Identity Risk & Privilege Creep Analysis */}
            <Route
              path="/risk-analysis"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER']}>
                  <RiskAnalysis />
                </ProtectedRoute>
              }
            />

            {/* Privilege Review & Remediation Workflow */}
            <Route
              path="/privilege-review"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER']}>
                  <PrivilegeReview />
                </ProtectedRoute>
              }
            />

            {/* Compliance & Governance Reports */}
            <Route
              path="/reports"
              element={
                <ProtectedRoute requiredRoles={['ADMIN', 'SUPER_ADMIN', 'AUDITOR']}>
                  <Reports />
                </ProtectedRoute>
              }
            />

            {/* Personal Account Profile */}
            <Route path="/profile" element={<UserProfile />} />
          </Route>

          {/* 404 Catch-All */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
