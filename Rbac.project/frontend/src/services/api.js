import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor to inject JWT Bearer token into outgoing requests
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor to handle unauthorized 401 responses
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      const isAuthPath = window.location.pathname.startsWith('/login') || window.location.pathname.startsWith('/register');
      if (!isAuthPath) {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// API Service Functions
export const authService = {
  login: (credentials) => api.post('/auth/login', credentials),
  register: (userData) => api.post('/auth/register', userData),
  getCurrentUser: () => api.get('/auth/me'),
};

export const userService = {
  getUsers: (params) => api.get('/users', { params }),
  getUserById: (id) => api.get(`/users/${id}`),
  createUser: (userData) => api.post('/users', userData),
  updateUser: (id, userData) => api.put(`/users/${id}`, userData),
  deleteUser: (id) => api.delete(`/users/${id}`),
  toggleStatus: (id) => api.patch(`/users/${id}/toggle-status`),
  assignRole: (userId, roleId) => api.post(`/users/${userId}/roles/${roleId}`),
  removeRole: (userId, roleId) => api.delete(`/users/${userId}/roles/${roleId}`),
  getDepartments: () => api.get('/users/departments'),
};

export const roleService = {
  getRoles: () => api.get('/roles'),
  getRoleById: (id) => api.get(`/roles/${id}`),
  createRole: (roleData) => api.post('/roles', roleData),
  updateRole: (id, roleData) => api.put(`/roles/${id}`, roleData),
  deleteRole: (id) => api.delete(`/roles/${id}`),
  assignPermissions: (id, permissionIds) => api.put(`/roles/${id}/permissions`, permissionIds),
};

export const permissionService = {
  getPermissions: () => api.get('/permissions'),
  getElevatedPermissions: () => api.get('/permissions/elevated'),
};

export const auditService = {
  getAuditLogs: (params) => api.get('/audit-logs', { params }),
  getDistinctActions: () => api.get('/audit-logs/actions'),
  getMyHistory: () => api.get('/audit-logs/my-history'),
};

export const riskService = {
  getRiskAssessments: (params) => api.get('/risk', { params }),
  getUserRisk: (userId) => api.get(`/risk/${userId}`),
  runScan: () => api.post('/risk/scan'),
};

export const privilegeReviewService = {
  getReviews: (status) => api.get('/privilege-reviews', { params: { status } }),
  getUserReviews: (userId) => api.get(`/privilege-reviews/user/${userId}`),
  approveReview: (id, reviewNotes) => api.post(`/privilege-reviews/${id}/approve`, { reviewNotes }),
  rejectReview: (id, reviewNotes) => api.post(`/privilege-reviews/${id}/reject`, { reviewNotes }),
};

export const dashboardService = {
  getStats: () => api.get('/dashboard/stats'),
  getEmployeeDashboard: () => api.get('/dashboard/employee'),
};

// Report download helper function
export const reportService = {
  downloadReport: async (url, filename) => {
    const response = await api.get(url, { responseType: 'blob' });
    const blob = new Blob([response.data], {
      type: response.headers['content-type'] || 'application/octet-stream',
    });
    const downloadUrl = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.download = filename;
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(downloadUrl);
  },
};

export default api;
