import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/api';
import { Shield, User, Mail, Lock, Building, Briefcase, ArrowLeft, CheckCircle } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    department: 'Engineering',
    jobTitle: 'Software Engineer',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await authService.register(formData);
      setSuccess(true);
      setTimeout(() => {
        navigate('/login');
      }, 2000);
    } catch (err) {
      console.error(err);
      setError(err.response?.data?.message || 'Registration failed. Please check inputs.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="d-flex align-items-center justify-content-center min-vh-100 bg-light py-5">
      <div className="container">
        <div className="row justify-content-center">
          <div className="col-12 col-md-10 col-lg-8 col-xl-6">
            <div className="card shadow-lg border-0 rounded-4 overflow-hidden">
              <div className="card-body p-4 p-md-5">
                <div className="d-flex align-items-center justify-content-between mb-4">
                  <div className="d-flex align-items-center gap-2">
                    <Shield size={28} className="text-primary" />
                    <h4 className="fw-bold mb-0 text-dark">Identity Onboarding</h4>
                  </div>
                  <Link to="/login" className="btn btn-sm btn-outline-secondary d-flex align-items-center gap-1">
                    <ArrowLeft size={14} /> Back to Sign In
                  </Link>
                </div>

                {success && (
                  <div className="alert alert-success d-flex align-items-center gap-2 py-2.5 small" role="alert">
                    <CheckCircle size={18} className="flex-shrink-0" />
                    <div>Identity provisioned successfully! Redirecting to login...</div>
                  </div>
                )}

                {error && (
                  <div className="alert alert-danger py-2.5 small" role="alert">
                    {error}
                  </div>
                )}

                <form onSubmit={handleRegister}>
                  <div className="row g-3">
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold text-secondary">Username</label>
                      <div className="input-group">
                        <span className="input-group-text bg-light"><User size={16} /></span>
                        <input
                          type="text"
                          name="username"
                          className="form-control bg-light"
                          placeholder="j.smith"
                          value={formData.username}
                          onChange={handleChange}
                          required
                        />
                      </div>
                    </div>

                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold text-secondary">Work Email</label>
                      <div className="input-group">
                        <span className="input-group-text bg-light"><Mail size={16} /></span>
                        <input
                          type="email"
                          name="email"
                          className="form-control bg-light"
                          placeholder="j.smith@enterprise.local"
                          value={formData.email}
                          onChange={handleChange}
                          required
                        />
                      </div>
                    </div>

                    <div className="col-12">
                      <label className="form-label small fw-semibold text-secondary">Full Legal Name</label>
                      <input
                        type="text"
                        name="fullName"
                        className="form-control bg-light"
                        placeholder="John Smith"
                        value={formData.fullName}
                        onChange={handleChange}
                        required
                      />
                    </div>

                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold text-secondary">Department</label>
                      <div className="input-group">
                        <span className="input-group-text bg-light"><Building size={16} /></span>
                        <select
                          name="department"
                          className="form-select bg-light"
                          value={formData.department}
                          onChange={handleChange}
                        >
                          <option value="Engineering">Engineering</option>
                          <option value="Identity Management">Identity Management</option>
                          <option value="Security Architecture">Security Architecture</option>
                          <option value="Quality Assurance">Quality Assurance</option>
                          <option value="Human Resources">Human Resources</option>
                          <option value="Finance">Finance</option>
                          <option value="Internal Audit">Internal Audit</option>
                        </select>
                      </div>
                    </div>

                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold text-secondary">Job Title</label>
                      <div className="input-group">
                        <span className="input-group-text bg-light"><Briefcase size={16} /></span>
                        <input
                          type="text"
                          name="jobTitle"
                          className="form-control bg-light"
                          placeholder="e.g. Associate Analyst"
                          value={formData.jobTitle}
                          onChange={handleChange}
                          required
                        />
                      </div>
                    </div>

                    <div className="col-12">
                      <label className="form-label small fw-semibold text-secondary">Secure Password</label>
                      <div className="input-group">
                        <span className="input-group-text bg-light"><Lock size={16} /></span>
                        <input
                          type="password"
                          name="password"
                          className="form-control bg-light"
                          placeholder="Min 6 characters"
                          value={formData.password}
                          onChange={handleChange}
                          required
                          minLength={6}
                        />
                      </div>
                      <small className="text-muted" style={{ fontSize: '0.75rem' }}>
                        Password will be hashed with BCrypt. Default assigned role will be <code>ROLE_EMPLOYEE</code>.
                      </small>
                    </div>

                    <div className="col-12 mt-4">
                      <button
                        type="submit"
                        disabled={loading || success}
                        className="btn btn-primary w-100 py-2.5 fw-semibold shadow-sm"
                      >
                        {loading ? 'Provisioning Identity...' : 'Complete Self-Registration'}
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
