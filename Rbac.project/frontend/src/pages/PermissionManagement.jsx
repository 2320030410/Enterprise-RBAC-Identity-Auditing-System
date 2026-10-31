import React, { useState, useEffect } from 'react';
import { permissionService } from '../services/api';
import { ShieldCheck, ShieldAlert, KeyRound, Search, Filter } from 'lucide-react';

const PermissionManagement = () => {
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [categoryFilter, setCategoryFilter] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    const fetchPerms = async () => {
      try {
        const res = await permissionService.getPermissions();
        setPermissions(res.data);
      } catch (err) {
        console.error('Failed to load permissions:', err);
      } finally {
        setLoading(false);
      }
    };
    fetchPerms();
  }, []);

  const categories = [...new Set(permissions.map((p) => p.category).filter(Boolean))];

  const filteredPermissions = permissions.filter((p) => {
    const matchCat = !categoryFilter || p.category === categoryFilter;
    const matchSearch =
      !search ||
      p.name.toLowerCase().includes(search.toLowerCase()) ||
      (p.description && p.description.toLowerCase().includes(search.toLowerCase()));
    return matchCat && matchSearch;
  });

  return (
    <div className="container-fluid p-0">
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Granular Permission Catalog</h3>
          <p className="text-muted small mb-0">
            Fine-grained system authorizations and privileges evaluated by Spring Security method guards
          </p>
        </div>
        <div className="d-flex gap-2">
          <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 px-3 py-2 d-flex align-items-center gap-1.5 fw-semibold">
            <ShieldAlert size={15} /> Monitored for Privilege Creep
          </span>
        </div>
      </div>

      {/* Filter and Search */}
      <div className="card shadow-sm border-0 mb-4 p-3">
        <div className="row g-3">
          <div className="col-12 col-md-6">
            <div className="input-group">
              <span className="input-group-text bg-light border-end-0 text-muted"><Search size={16} /></span>
              <input
                type="text"
                className="form-control bg-light border-start-0"
                placeholder="Search permissions by identifier or description..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>
          </div>
          <div className="col-12 col-md-6">
            <select
              className="form-select bg-light"
              value={categoryFilter}
              onChange={(e) => setCategoryFilter(e.target.value)}
            >
              <option value="">All Categories</option>
              {categories.map((c) => (
                <option key={c} value={c}>{c}</option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Permissions Table */}
      <div className="card shadow-sm border-0 overflow-hidden">
        <div className="table-responsive">
          <table className="table table-hover align-middle mb-0">
            <thead>
              <tr>
                <th>Permission Name</th>
                <th>Category</th>
                <th>Privilege Level</th>
                <th>Description</th>
                <th>Spring Method Guard</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan="5" className="text-center py-5 text-muted">
                    <div className="spinner-border spinner-border-sm text-primary me-2"></div>
                    Loading permission entitlements...
                  </td>
                </tr>
              ) : filteredPermissions.length > 0 ? (
                filteredPermissions.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <div className="d-flex align-items-center gap-2">
                        <KeyRound size={16} className={p.elevated ? 'text-danger' : 'text-primary'} />
                        <span className="font-monospace fw-bold text-dark">{p.name}</span>
                      </div>
                    </td>
                    <td>
                      <span className="badge bg-light text-secondary border font-monospace">
                        {p.category || 'GENERAL'}
                      </span>
                    </td>
                    <td>
                      {p.elevated ? (
                        <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25 d-inline-flex align-items-center gap-1">
                          <ShieldAlert size={12} /> ELEVATED PRIVILEGE
                        </span>
                      ) : (
                        <span className="badge bg-success bg-opacity-10 text-success border border-success border-opacity-25 d-inline-flex align-items-center gap-1">
                          <ShieldCheck size={12} /> Standard
                        </span>
                      )}
                    </td>
                    <td className="text-secondary small">{p.description}</td>
                    <td>
                      <code className="text-primary small" style={{ backgroundColor: '#f1f5f9', padding: '2px 6px', borderRadius: '4px' }}>
                        @PreAuthorize("hasAuthority('{p.name}')")
                      </code>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="text-center py-5 text-muted">
                    No permissions match current search criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default PermissionManagement;
