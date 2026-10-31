import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Shield, Lock, User, AlertCircle, ArrowRight, CheckCircle2 } from 'lucide-react';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const { login } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const authData = await login(username, password);
      const isPrivileged = authData.roles.some((r) =>
        ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN', 'ROLE_AUDITOR', 'ROLE_MANAGER'].includes(r)
      );

      if (isPrivileged) {
        navigate('/dashboard');
      } else {
        navigate('/employee-dashboard');
      }
    } catch (err) {
      console.error(err);
      setError(
        err.response?.data?.message || 'Invalid credentials or connection error. Please verify username and password.'
      );
    } finally {
      setLoading(false);
    }
  };

  const setDemoCredentials = (u, p) => {
    setUsername(u);
    setPassword(p);
    setError('');
  };

  return (
    <div className="d-flex align-items-center justify-content-center min-vh-100 bg-light py-5">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-12 col-md-10 col-lg-8 col-xl-6">
            <div className="text-center mb-4">
              <div
                className="d-inline-flex p-3 rounded-circle shadow-sm mb-3"
                style={{ backgroundColor: 'var(--primary-navy)' }}
              >
                <Shield size={38} className="text-info" />
              </div>
              <h2 className="fw-bold text-dark tracking-tight">Enterprise Identity Auditor</h2>
              <p className="text-muted small">
                Cybersecurity Identity Governance, Privilege Creep Detection & RBAC Management
              </p>
            </div>

            <div className="card shadow-lg border-0 rounded-4 overflow-hidden">
              <div className="card-body p-4 p-md-5">
                <div className="d-flex align-items-center justify-content-between mb-4">
                  <h4 className="fw-bold mb-0 text-dark">System Authentication</h4>
                  <span className="badge bg-primary bg-opacity-10 text-primary px-3 py-1.5 rounded-pill small fw-semibold">
                    JWT + BCrypt
                  </span>
                </div>

                {error && (
                  <div className="alert alert-danger d-flex align-items-center gap-2 py-2.5 small" role="alert">
                    <AlertCircle size={18} className="flex-shrink-0" />
                    <div>{error}</div>
                  </div>
                )}

                <form onSubmit={handleLogin}>
                  <div className="mb-3">
                    <label className="form-label small fw-semibold text-secondary">Username</label>
                    <div className="input-group">
                      <span className="input-group-text bg-light border-end-0 text-muted">
                        <User size={18} />
                      </span>
                      <input
                        type="text"
                        className="form-control border-start-0 ps-0 bg-light"
                        placeholder="Enter system username"
                        value={username}
                        onChange={(e) => setUsername(e.target.value)}
                        required
                        autoFocus
                      />
                    </div>
                  </div>

                  <div className="mb-4">
                    <label className="form-label small fw-semibold text-secondary">Password</label>
                    <div className="input-group">
                      <span className="input-group-text bg-light border-end-0 text-muted">
                        <Lock size={18} />
                      </span>
                      <input
                        type="password"
                        className="form-control border-start-0 ps-0 bg-light"
                        placeholder="Enter password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                      />
                    </div>
                  </div>

                  <button
                    type="submit"
                    disabled={loading}
                    className="btn btn-primary w-100 py-2.5 fw-semibold d-flex align-items-center justify-content-center gap-2 shadow-sm"
                  >
                    {loading ? (
                      <>
                        <span className="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span>
                        <span>Verifying Credentials...</span>
                      </>
                    ) : (
                      <>
                        <span>Authenticate to Dashboard</span>
                        <ArrowRight size={18} />
                      </>
                    )}
                  </button>
                </form>

                <div className="mt-4 pt-3 border-top">
                  <div className="d-flex align-items-center justify-content-between mb-2">
                    <span className="small fw-semibold text-muted text-uppercase" style={{ fontSize: '0.72rem' }}>
                      One-Click Demo Account Presets:
                    </span>
                  </div>
                  <div className="d-flex flex-wrap gap-2">
                    <button
                      type="button"
                      onClick={() => setDemoCredentials('admin', 'Admin@123')}
                      className="btn btn-sm btn-outline-primary"
                    >
                      Admin
                    </button>
                    <button
                      type="button"
                      onClick={() => setDemoCredentials('superadmin', 'SuperAdmin@123')}
                      className="btn btn-sm btn-outline-dark"
                    >
                      Super Admin
                    </button>
                    <button
                      type="button"
                      onClick={() => setDemoCredentials('sarah.connor', 'User@123')}
                      className="btn btn-sm btn-outline-danger"
                      title="Privilege Creep Candidate (>30 days dormant admin access)"
                    >
                      Privilege Creep (Sarah)
                    </button>
                    <button
                      type="button"
                      onClick={() => setDemoCredentials('auditor', 'Auditor@123')}
                      className="btn btn-sm btn-outline-success"
                    >
                      Auditor
                    </button>
                    <button
                      type="button"
                      onClick={() => setDemoCredentials('manager', 'Manager@123')}
                      className="btn btn-sm btn-outline-warning"
                    >
                      Manager
                    </button>
                    <button
                      type="button"
                      onClick={() => setDemoCredentials('john.doe', 'User@123')}
                      className="btn btn-sm btn-outline-secondary"
                    >
                      Employee (John)
                    </button>
                  </div>
                </div>

                <div className="text-center mt-4">
                  <span className="text-muted small">New employee in organization? </span>
                  <Link to="/register" className="small fw-semibold text-decoration-none text-primary">
                    Self-Register Here
                  </Link>
                </div>
              </div>
            </div>

            <div className="text-center mt-4 text-muted small">
              Enterprise Role-Based Access Control & Identity Governance Platform &bull; B.Tech Final Year CSE
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
