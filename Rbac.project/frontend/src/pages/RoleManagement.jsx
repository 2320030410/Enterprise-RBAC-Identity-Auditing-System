import React, { useState, useEffect } from 'react';
import { roleService, permissionService } from '../services/api';
import { KeyRound, Plus, Edit2, Trash2, ShieldAlert, ShieldCheck, Lock } from 'lucide-react';

const RoleManagement = () => {
  const [roles, setRoles] = useState([]);
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);

  const [showModal, setShowModal] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentRoleId, setCurrentRoleId] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    description: '',
    permissionIds: [],
  });

  const [error, setError] = useState('');

  const fetchData = async () => {
    setLoading(true);
    try {
      const [roleRes, permRes] = await Promise.all([
        roleService.getRoles(),
        permissionService.getPermissions(),
      ]);
      setRoles(roleRes.data);
      setPermissions(permRes.data);
    } catch (err) {
      console.error('Failed to load roles and permissions:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const openCreateModal = () => {
    setIsEditing(false);
    setCurrentRoleId(null);
    setFormData({ name: '', description: '', permissionIds: [] });
    setError('');
    setShowModal(true);
  };

  const openEditModal = (role) => {
    setIsEditing(true);
    setCurrentRoleId(role.id);
    setFormData({
      name: role.name.replace('ROLE_', ''),
      description: role.description || '',
      permissionIds: role.permissions?.map((p) => p.id) || [],
    });
    setError('');
    setShowModal(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (isEditing) {
        await roleService.updateRole(currentRoleId, formData);
      } else {
        await roleService.createRole(formData);
      }
      setShowModal(false);
      fetchData();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to save role');
    }
  };

  const handleDelete = async (role) => {
    if (window.confirm(`Are you sure you want to delete role "${role.name}"?`)) {
      try {
        await roleService.deleteRole(role.id);
        fetchData();
      } catch (err) {
        alert(err.response?.data?.message || 'Failed to delete role');
      }
    }
  };

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Role-Based Access Control (RBAC)</h3>
          <p className="text-muted small mb-0">
            Define organizational roles and bind fine-grained permissions adhering to Least Privilege
          </p>
        </div>
        <button onClick={openCreateModal} className="btn btn-primary d-flex align-items-center gap-2 shadow-sm">
          <Plus size={16} /> Create Custom Role
        </button>
      </div>

      <div className="row g-4">
        {loading ? (
          <div className="col-12 text-center py-5">
            <div className="spinner-border text-primary" role="status"></div>
          </div>
        ) : (
          roles.map((role) => (
            <div key={role.id} className="col-12 col-lg-6 col-xl-4">
              <div className="card shadow-sm border-0 h-100 p-4">
                <div className="d-flex justify-content-between align-items-start mb-3">
                  <div className="d-flex align-items-center gap-2">
                    <div className="icon-box bg-primary bg-opacity-10 text-primary">
                      <KeyRound size={22} />
                    </div>
                    <div>
                      <h5 className="fw-bold mb-0 text-dark">{role.name.replace('ROLE_', '')}</h5>
                      <span className="font-monospace text-muted" style={{ fontSize: '0.75rem' }}>{role.name}</span>
                    </div>
                  </div>
                  {role.systemRole ? (
                    <span className="badge bg-secondary bg-opacity-15 text-dark border d-flex align-items-center gap-1">
                      <Lock size={12} /> System Role
                    </span>
                  ) : (
                    <span className="badge bg-info bg-opacity-15 text-info border">Custom Role</span>
                  )}
                </div>

                <p className="small text-muted mb-3" style={{ minHeight: '38px' }}>
                  {role.description || 'No formal description provided.'}
                </p>

                <div className="border-top pt-3 mb-3">
                  <div className="d-flex justify-content-between align-items-center mb-2">
                    <span className="small fw-semibold text-secondary">
                      Entitlements Granted ({role.permissions?.length || 0})
                    </span>
                  </div>
                  <div className="d-flex flex-wrap gap-1" style={{ maxHeight: '110px', overflowY: 'auto' }}>
                    {role.permissions?.map((p) => (
                      <span
                        key={p.id}
                        className={`badge font-monospace ${
                          p.elevated
                            ? 'bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25'
                            : 'bg-light text-dark border'
                        }`}
                        style={{ fontSize: '0.7rem' }}
                        title={p.elevated ? 'Elevated privilege' : 'Standard permission'}
                      >
                        {p.name}
                      </span>
                    ))}
                  </div>
                </div>

                <div className="mt-auto pt-3 border-top d-flex justify-content-end gap-2">
                  <button
                    onClick={() => openEditModal(role)}
                    className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1"
                  >
                    <Edit2 size={13} /> Edit Permissions
                  </button>
                  {!role.systemRole && (
                    <button
                      onClick={() => handleDelete(role)}
                      className="btn btn-outline-danger btn-sm d-flex align-items-center gap-1"
                    >
                      <Trash2 size={13} /> Delete
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Role Edit/Create Modal */}
      {showModal && (
        <div className="modal show d-block bg-dark bg-opacity-50" tabIndex="-1">
          <div className="modal-dialog modal-dialog-centered modal-lg">
            <div className="modal-content border-0 shadow-lg rounded-4">
              <form onSubmit={handleSubmit}>
                <div className="modal-header border-bottom py-3">
                  <h5 className="modal-title fw-bold text-dark">
                    {isEditing ? `Edit Role: ${formData.name}` : 'Define New Security Role'}
                  </h5>
                  <button type="button" className="btn-close" onClick={() => setShowModal(false)}></button>
                </div>
                <div className="modal-body p-4">
                  {error && <div className="alert alert-danger py-2 small mb-3">{error}</div>}

                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Role Name *</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="e.g. DEVOPS_ENGINEER"
                      value={formData.name}
                      onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                      required
                      disabled={isEditing && roles.find((r) => r.id === currentRoleId)?.systemRole}
                    />
                    <small className="text-muted" style={{ fontSize: '0.75rem' }}>
                      Stored with prefix <code>ROLE_</code> in Spring Security authorities.
                    </small>
                  </div>

                  <div className="mb-3">
                    <label className="form-label small fw-semibold">Description</label>
                    <input
                      type="text"
                      className="form-control"
                      placeholder="Brief role objective"
                      value={formData.description}
                      onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                    />
                  </div>

                  <div className="mb-2">
                    <label className="form-label small fw-semibold">Bind Permissions (Fine-Grained Privileges)</label>
                    <div className="p-3 bg-light rounded-3 border" style={{ maxHeight: '280px', overflowY: 'auto' }}>
                      <div className="row g-2">
                        {permissions.map((perm) => (
                          <div key={perm.id} className="col-12 col-md-6">
                            <div className="form-check">
                              <input
                                className="form-check-input"
                                type="checkbox"
                                id={`perm-${perm.id}`}
                                checked={formData.permissionIds.includes(perm.id)}
                                onChange={(e) => {
                                  if (e.target.checked) {
                                    setFormData({
                                      ...formData,
                                      permissionIds: [...formData.permissionIds, perm.id],
                                    });
                                  } else {
                                    setFormData({
                                      ...formData,
                                      permissionIds: formData.permissionIds.filter((id) => id !== perm.id),
                                    });
                                  }
                                }}
                              />
                              <label className="form-check-label small" htmlFor={`perm-${perm.id}`}>
                                <span className="font-monospace fw-semibold">{perm.name}</span>
                                {perm.elevated && (
                                  <span className="badge bg-danger bg-opacity-15 text-danger ms-1.5" style={{ fontSize: '0.65rem' }}>
                                    Elevated
                                  </span>
                                )}
                              </label>
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  </div>
                </div>
                <div className="modal-footer border-top py-2.5">
                  <button type="button" className="btn btn-light" onClick={() => setShowModal(false)}>Cancel</button>
                  <button type="submit" className="btn btn-primary fw-semibold">Save Role</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default RoleManagement;
