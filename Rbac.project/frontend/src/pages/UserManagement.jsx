import React, { useState, useEffect } from 'react';
import { userService, roleService } from '../services/api';
import {
  Users,
  UserPlus,
  Search,
  Filter,
  Edit2,
  Trash2,
  KeyRound,
  ShieldAlert,
  Check,
  X,
  Eye,
  AlertCircle,
} from 'lucide-react';

const UserManagement = () => {
  const [users, setUsers] = useState([]);
  const [roles, setRoles] = useState([]);
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [search, setSearch] = useState('');
  const [selectedDept, setSelectedDept] = useState('');
  const [selectedEnabled, setSelectedEnabled] = useState('');

  // Modals state
  const [showAddModal, setShowAddModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);

  // Form states
  const [addForm, setAddForm] = useState({
    username: '',
    email: '',
    password: '',
    fullName: '',
    department: 'Engineering',
    jobTitle: 'Engineer',
    enabled: true,
    roleIds: [],
  });

  const [editForm, setEditForm] = useState({
    email: '',
    fullName: '',
    department: '',
    jobTitle: '',
    enabled: true,
    newPassword: '',
    roleIds: [],
  });

  const [actionError, setActionError] = useState('');

  const fetchUsers = async () => {
    setLoading(true);
    try {
      const params = {};
      if (search) params.search = search;
      if (selectedDept) params.department = selectedDept;
      if (selectedEnabled !== '') params.enabled = selectedEnabled === 'true';

      const res = await userService.getUsers(params);
      setUsers(res.data);
    } catch (err) {
      console.error('Failed to fetch users:', err);
    } finally {
      setLoading(false);
    }
  };

  const fetchMeta = async () => {
    try {
      const [roleRes, deptRes] = await Promise.all([
        roleService.getRoles(),
        userService.getDepartments(),
      ]);
      setRoles(roleRes.data);
      setDepartments(deptRes.data);
    } catch (err) {
      console.error('Failed to fetch metadata:', err);
    }
  };

  useEffect(() => {
    fetchMeta();
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [search, selectedDept, selectedEnabled]);

  const handleAddSubmit = async (e) => {
    e.preventDefault();
    setActionError('');
    try {
      await userService.createUser(addForm);
      setShowAddModal(false);
      setAddForm({
        username: '',
        email: '',
        password: '',
        fullName: '',
        department: 'Engineering',
        jobTitle: 'Engineer',
        enabled: true,
        roleIds: [],
      });
      fetchUsers();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to create user');
    }
  };

  const openEditModal = (user) => {
    setSelectedUser(user);
    setEditForm({
      email: user.email,
      fullName: user.fullName,
      department: user.department || '',
      jobTitle: user.jobTitle || '',
      enabled: user.enabled,
      newPassword: '',
      roleIds: user.roles.map((r) => r.id),
    });
    setActionError('');
    setShowEditModal(true);
  };

  const handleEditSubmit = async (e) => {
    e.preventDefault();
    setActionError('');
    try {
      await userService.updateUser(selectedUser.id, editForm);
      setShowEditModal(false);
      fetchUsers();
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to update user');
    }
  };

  const handleDelete = async (user) => {
    if (window.confirm(`Are you sure you want to permanently delete user "${user.username}"?`)) {
      try {
        await userService.deleteUser(user.id);
        fetchUsers();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete user');
      }
    }
  };

  const handleToggleStatus = async (user) => {
    try {
      await userService.toggleStatus(user.id);
      fetchUsers();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to toggle status');
    }
  };

  const openViewModal = (user) => {
    setSelectedUser(user);
    setShowViewModal(true);
  };

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">User Directory & Entitlements</h3>
          <p className="text-muted small mb-0">
            Provision, manage, and govern identity access lifecycles across the enterprise
          </p>
        </div>
        <button
          onClick={() => {
            setShowAddModal(true);
            setActionError('');
          }}
          className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
        >
          <UserPlus size={16} /> Add New Employee
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="card shadow-sm border-0 mb-4 p-3">
        <div className="row g-3">
          <div className="col-12 col-md-5">
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0 text-muted"><Search size={16} /></span>
              <input
                type="text"
                className="form-control bg-light border-start-0"
                placeholder="Search by username, full name, or email..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>
          <div className="col-12 col-md-4">
            <select
              className="form-select bg-light"
              value={selectedDept}
              onChange={(e) => setSelectedDept(e.target.value)}
            >
              <option value="">All Departments</option>
              {departments.map((dept) => (
                <option key={dept} value={dept}>{dept}</option>
              ))}
            </select>
          </div>
          <div className="col-12 col-md-3">
            <select
              className="form-select bg-light"
              value={selectedEnabled}
              onChange={(e) => setSelectedEnabled(e.target.value)}
            >
              <option value="">All Account Statuses</option>
              <option value="true">Active Only</option>
              <option value="false">Inactive / Disabled</option>
            </select>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="card shadow-sm border-0 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Identity</th>
                <th>Department & Title</th>
                <th>Assigned Roles</th>
                <th>Status</th>
                <th>Created</th>
                <th className="text-end">Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="6" className="text-center py-5 text-muted">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Loading directory identities...
                  </td>
                </tr>
              ) : users.length > 0 ? (
                users.map((u) => (
                  <tr key={u.id}>
                    <td>
                      <div className="d-flex align-items-center gap-2.5">
                        <div
                          className="rounded-circle bg-primary bg-opacity-10 text-primary d-flex align-items-center justify-content-center fw-bold"
                          style={{ width: '36px', height: '36px' }}
                        >
                          {u.fullName?.charAt(0) || u.username?.charAt(0)}
                        </div>
                        <div>
                          <div className="fw-semibold text-dark">{u.fullName}</div>
                          <div className="text-muted small">@{u.username} &bull; {u.email}</div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <div className="text-dark small fw-medium">{u.department || 'N/A'}</div>
                      <div className="text-muted small">{u.jobTitle || 'N/A'}</div>
                    </td>
                    <td>
                      <div className="d-flex flex-wrap gap-1">
                        {u.roles?.map((r) => (
                          <span
                            key={r.id || r.name}
                            className={`badge px-2 py-1 ${
                              r.name.includes('ADMIN')
                                ? 'bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25'
                                : r.name.includes('MANAGER')
                                ? 'bg-warning bg-opacity-10 text-warning border border-warning border-opacity-25'
                                : 'bg-primary bg-opacity-10 text-primary border border-primary border-opacity-25'
                            }`}
                            style={{ fontSize: '0.72rem' }}
                          >
                            {r.name.replace('ROLE_', '')}
                          </span>
                        ))}
                      </div>
                    </td>
                    <td>
                      <button
                        onClick={() => handleToggleStatus(u)}
                        className={`badge border-0 px-2.5 py-1.5 rounded-pill small fw-semibold cursor-pointer ${
                          u.enabled
                            ? 'bg-success bg-opacity-10 text-success'
                            : 'bg-secondary bg-opacity-25 text-secondary'
                        }`}
                        title="Click to toggle account status"
                      >
                        {u.enabled ? 'ACTIVE' : 'DISABLED'}
                      </button>
                    </td>
                    <td className="text-muted small">
                      {u.createdAt ? new Date(u.createdAt).toLocaleDateString() : 'N/A'}
                    </td>
                    <td className="text-end">
                      <div className="btn-group btn-group-sm">
                        <button
                          onClick={() => openViewModal(u)}
                          className="btn btn-outline-secondary"
                          title="View effective entitlements"
                        >
                          <Eye size={14} />
                        </button>
                        <button
                          onClick={() => openEditModal(u)}
                          className="btn btn-outline-primary"
                          title="Edit Identity"
                        >
                          <Edit2 size={14} />
                        </button>
                        <button
                          onClick={() => handleDelete(u)}
                          className="btn btn-outline-danger"
                          title="Delete Identity"
                          disabled={u.username === 'superadmin'}
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="text-center py-5 text-muted">
                    No matching users found in directory.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add User Modal */}
      {showAddModal && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <form onSubmit={handleAddSubmit}>
                <div className="modal-header border-bottom py-3">
                  <h5 className="modal-title fw-bold text-dark">Provision New Employee Identity</h5>
                  <button type="button" className="btn-close" onClick={() => setShowAddModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  {actionError && (
                    <div className="alert alert-danger py-2 small mb-3">{actionError}</div>
                  )}
                  <div className="row g-3">
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Username *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={addForm.username}
                        onChange={(e) => setAddForm({ ...addForm, username: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Email *</label>
                      <input
                        type="email"
                        className="form-control"
                        value={addForm.email}
                        onChange={(e) => setAddForm({ ...addForm, email: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Full Legal Name *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={addForm.fullName}
                        onChange={(e) => setAddForm({ ...addForm, fullName: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Temporary Password *</label>
                      <input
                        type="password"
                        className="form-control"
                        value={addForm.password}
                        onChange={(e) => setAddForm({ ...addForm, password: e.target.value })}
                        required
                        minLength={6}
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Department</label>
                      <input
                        type="text"
                        className="form-control"
                        value={addForm.department}
                        onChange={(e) => setAddForm({ ...addForm, department: e.target.value })}
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Job Title</label>
                      <input
                        type="text"
                        className="form-control"
                        value={addForm.jobTitle}
                        onChange={(e) => setAddForm({ ...addForm, jobTitle: e.target.value })}
                      />
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Assign Security Roles *</label>
                      <div className="d-flex flex-wrap gap-2 p-3 bg-light rounded-3 border">
                        {roles.map((role) => (
                          <div key={role.id} className="form-check me-3">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              id={`role-${role.id}`}
                              checked={addForm.roleIds.includes(role.id)}
                              onChange={(e) => {
                                if (e.target.checked) {
                                  setAddForm({ ...addForm, roleIds: [...addForm.roleIds, role.id] });
                                } else {
                                  setAddForm({
                                    ...addForm,
                                    roleIds: addForm.roleIds.filter((id) => id !== role.id),
                                  });
                                }
                              }}
                            />
                            <label className="form-check-label small" htmlFor={`role-${role.id}`}>
                              {role.name.replace('ROLE_', '')}
                            </label>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-top py-2.5">
                  <button type="button" className="btn btn-light" onClick={() => setShowAddModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary fw-semibold">Save & Provision</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* Edit User Modal */}
      {showEditModal && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <form onSubmit={handleEditSubmit}>
                <div className="modal-header border-bottom py-3">
                  <h5 className="modal-title fw-bold text-dark">Modify User: {selectedUser?.username}</h5>
                  <button type="button" className="btn-close" onClick={() => setShowEditModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  {actionError && (
                    <div className="alert alert-danger py-2 small mb-3">{actionError}</div>
                  )}
                  <div className="row g-3">
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Email *</label>
                      <input
                        type="email"
                        className="form-control"
                        value={editForm.email}
                        onChange={(e) => setEditForm({ ...editForm, email: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Full Name *</label>
                      <input
                        type="text"
                        className="form-control"
                        value={editForm.fullName}
                        onChange={(e) => setEditForm({ ...editForm, fullName: e.target.value })}
                        required
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Department</label>
                      <input
                        type="text"
                        className="form-control"
                        value={editForm.department}
                        onChange={(e) => setEditForm({ ...editForm, department: e.target.value })}
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Job Title</label>
                      <input
                        type="text"
                        className="form-control"
                        value={editForm.jobTitle}
                        onChange={(e) => setEditForm({ ...editForm, jobTitle: e.target.value })}
                      />
                    </div>
                    <div className="col-12 col-md-6">
                      <label className="form-label small fw-semibold">Reset Password (Optional)</label>
                      <input
                        type="password"
                        className="form-control"
                        placeholder="Leave blank to keep existing"
                        value={editForm.newPassword}
                        onChange={(e) => setEditForm({ ...editForm, newPassword: e.target.value })}
                      />
                    </div>
                    <div className="col-12 col-md-6 d-flex align-items-center">
                      <div className="form-check mt-3">
                        <input
                          type="checkbox"
                          className="form-check-input"
                          id="editEnabledCheck"
                          checked={editForm.enabled}
                          onChange={(e) => setEditForm({ ...editForm, enabled: e.target.checked })}
                        />
                        <label className="form-check-label small fw-semibold" htmlFor="editEnabledCheck">
                          Account Enabled
                        </label>
                      </div>
                    </div>
                    <div className="col-12">
                      <label className="form-label small fw-semibold">Role Assignments</label>
                      <div className="d-flex flex-wrap gap-2 p-3 bg-light rounded-3 border">
                        {roles.map((role) => (
                          <div key={role.id} className="form-check me-3">
                            <input
                              className="form-check-input"
                              type="checkbox"
                              id={`edit-role-${role.id}`}
                              checked={editForm.roleIds.includes(role.id)}
                              onChange={(e) => {
                                if (e.target.checked) {
                                  setEditForm({ ...editForm, roleIds: [...editForm.roleIds, role.id] });
                                } else {
                                  setEditForm({
                                    ...editForm,
                                    roleIds: editForm.roleIds.filter((id) => id !== role.id),
                                  });
                                }
                              }}
                            />
                            <label className="form-check-label small" htmlFor={`edit-role-${role.id}`}>
                              {role.name.replace('ROLE_', '')}
                            </label>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-top py-2.5">
                  <button type="button" className="btn btn-light" onClick={() => setShowEditModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary fw-semibold">Update User</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}

      {/* View User Modal */}
      {showViewModal && selectedUser && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <div className="modal-header border-bottom py-3">
                <h5 className="modal-title fw-bold text-dark">Identity Details: {selectedUser.fullName}</h5>
                <button type="button" className="btn-close" onClick={() => setShowViewModal(false)}></button>
              </div>
              <div className="modal-body p-4">
                <div className="mb-3">
                  <span className="text-muted small">Username:</span>
                  <div className="fw-semibold">@{selectedUser.username}</div>
                </div>
                <div className="mb-3">
                  <span className="text-muted small">Roles Held:</span>
                  <div className="d-flex flex-wrap gap-1 mt-1">
                    {selectedUser.roles?.map((r) => (
                      <span key={r.id || r.name} className="badge bg-primary bg-opacity-10 text-primary border">
                        {r.name}
                      </span>
                    ))}
                  </div>
                </div>
                <div>
                  <span className="text-muted small">Effective Entitlements (Aggregated Permissions):</span>
                  <div className="d-flex flex-wrap gap-1 mt-1">
                    {selectedUser.permissions?.map((p, i) => (
                      <span key={i} className="badge bg-light text-dark border font-monospace" style={{ fontSize: '0.75rem' }}>
                        {p}
                      </span>
                    ))}
                  </div>
                </div>
              </div>
              <div className="modal-footer border-top py-2.5">
                <button type="button" className="btn btn-secondary" onClick={() => setShowViewModal(false)}>Close</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
