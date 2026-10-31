import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { userService } from '../services/api';
import { User, Mail, Shield, Building, Briefcase, Lock, CheckCircle, AlertCircle } from 'lucide-react';

const UserProfile = () => {
  const { user: authUser } = useAuth();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);

  const [passwordForm, setPasswordForm] = useState({
    newPassword: '',
    confirmPassword: '',
  });

  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        if (authUser?.id) {
          const res = await userService.getUserById(authUser.id);
          setProfile(res.data);
        }
      } catch (err) {
        console.error('Failed to load profile:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [authUser]);

  const handlePasswordUpdate = async (e) => {
    e.preventDefault();
    setMessage('');
    setError('');

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError('New passwords do not match');
      return;
    }

    if (passwordForm.newPassword.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    try {
      await userService.updateUser(authUser.id, {
        email: profile.email,
        fullName: profile.fullName,
        department: profile.department,
        jobTitle: profile.jobTitle,
        enabled: profile.enabled,
        newPassword: passwordForm.newPassword,
      });

      setMessage('Password updated successfully with BCrypt hashing!');
      setPasswordForm({ newPassword: '', confirmPassword: '' });
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to update password');
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <div className="spinner-border text-primary" role="status"></div>
      </div>
    );
  }

  return (
    <div className="container-fluid p-0">
      <div className="mb-4">
        <h3 className="fw-bold text-dark mb-1">Identity Profile & Credentials</h3>
        <p className="text-muted small mb-0">
          Manage your enterprise authentication credentials, contact profile, and inspected entitlements
        </p>
      </div>

      <div className="row g-4">
        {/* Profile Card */}
        <div className="col-12 col-lg-6">
          <div className="card shadow-sm border-0 h-100 p-4">
            <div className="d-flex align-items-center gap-3 mb-4">
              <div
                className="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center fw-bold"
                style={{ width: '64px', height: '64px', fontSize: '1.6rem' }}
              >
                {profile?.fullName?.charAt(0) || profile?.username?.charAt(0)}
              </div>
              <div>
                <h4 className="fw-bold text-dark mb-0">{profile?.fullName}</h4>
                <span className="text-muted">@{profile?.username}</span>
                <div className="mt-1">
                  <span className="badge bg-success bg-opacity-15 text-success">
                    {profile?.enabled ? 'ACTIVE IDENTITY' : 'DISABLED'}
                  </span>
                </div>
              </div>
            </div>

            <div className="list-group list-group-flush mb-4">
              <div className="list-group-item px-0 py-2.5 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-2"><Mail size={16} /> Work Email:</span>
                <span className="fw-semibold text-dark">{profile?.email}</span>
              </div>
              <div className="list-group-item px-0 py-2.5 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-2"><Building size={16} /> Department:</span>
                <span className="fw-semibold text-dark">{profile?.department || 'General'}</span>
              </div>
              <div className="list-group-item px-0 py-2.5 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-2"><Briefcase size={16} /> Job Title:</span>
                <span className="fw-semibold text-dark">{profile?.jobTitle || 'Associate'}</span>
              </div>
              <div className="list-group-item px-0 py-2.5 d-flex justify-content-between">
                <span className="text-muted d-flex align-items-center gap-2"><Shield size={16} /> Roles:</span>
                <div className="d-flex flex-wrap gap-1 justify-content-end">
                  {profile?.roles?.map((r) => (
                    <span key={r.id || r.name} className="badge bg-primary bg-opacity-10 text-primary border">
                      {r.name.replace('ROLE_', '')}
                    </span>
                  ))}
                </div>
              </div>
            </div>

            <div className="border-top pt-3">
              <span className="small fw-semibold text-secondary d-block mb-2">Granted Permissions:</span>
              <div className="d-flex flex-wrap gap-1" style={{ maxHeight: '140px', overflowY: 'auto' }}>
                {profile?.permissions?.map((p, i) => (
                  <span key={i} className="badge bg-light text-dark border font-monospace" style={{ fontSize: '0.72rem' }}>
                    {p}
                  </span>
                ))}
              </div>
            </div>
          </div>
        </div>

        {/* Password Update Card */}
        <div className="col-12 col-lg-6">
          <div className="card shadow-sm border-0 h-100 p-4">
            <div className="d-flex align-items-center gap-2 mb-3">
              <Lock size={20} className="text-primary" />
              <h5 className="fw-bold mb-0 text-dark">Rotate Identity Password</h5>
            </div>
            <p className="text-muted small mb-4">
              Update your account password. New credentials will be encoded with BCrypt hashing before database persistence.
            </p>

            {message && (
              <div className="alert alert-success d-flex align-items-center gap-2 py-2 small mb-3">
                <CheckCircle size={16} /> {message}
              </div>
            )}
            {error && (
              <div className="alert alert-danger d-flex align-items-center gap-2 py-2 small mb-3">
                <AlertCircle size={16} /> {error}
              </div>
            )}

            <form onSubmit={handlePasswordUpdate}>
              <div className="mb-3">
                <label className="form-label small fw-semibold text-secondary">New Password</label>
                <input
                  type="password"
                  className="form-control"
                  placeholder="Enter new password (min 6 characters)"
                  value={passwordForm.newPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                  required
                  minLength={6}
                />
              </div>

              <div className="mb-4">
                <label className="form-label small fw-semibold text-secondary">Confirm New Password</label>
                <input
                  type="password"
                  className="form-control"
                  placeholder="Re-enter new password"
                  value={passwordForm.confirmPassword}
                  onChange={(e) => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                  required
                />
              </div>

              <button type="submit" className="btn btn-primary fw-semibold shadow-sm">
                Update Account Password
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UserProfile;
