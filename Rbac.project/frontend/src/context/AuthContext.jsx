import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../services/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem('user');
    return savedUser ? JSON.parse(savedUser) : null;
  });
  const [token, setToken] = useState(() => localStorage.getItem('token') || null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const checkAuth = async () => {
      const savedToken = localStorage.getItem('token');
      if (savedToken) {
        try {
          const res = await authService.getCurrentUser();
          setUser((prev) => ({
            ...prev,
            ...res.data,
            roles: res.data.roles?.map((r) => r.name) || prev?.roles || [],
            permissions: res.data.permissions || prev?.permissions || [],
          }));
        } catch (err) {
          console.error('Session validation error:', err);
          logout();
        }
      }
      setLoading(false);
    };

    checkAuth();
  }, []);

  const login = async (username, password) => {
    const res = await authService.login({ username, password });
    const authData = res.data;
    setToken(authData.token);
    setUser(authData);
    localStorage.setItem('token', authData.token);
    localStorage.setItem('user', JSON.stringify(authData));
    return authData;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  };

  const hasRole = (role) => {
    if (!user || !user.roles) return false;
    const cleanRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
    return user.roles.includes(cleanRole);
  };

  const hasAnyRole = (...roles) => {
    if (!user || !user.roles) return false;
    return roles.some((role) => {
      const cleanRole = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
      return user.roles.includes(cleanRole);
    });
  };

  const hasPermission = (permission) => {
    if (!user || !user.permissions) return false;
    return user.permissions.includes(permission);
  };

  const isAdmin = () => {
    return hasAnyRole('ADMIN', 'SUPER_ADMIN');
  };

  const isPrivileged = () => {
    return hasAnyRole('ADMIN', 'SUPER_ADMIN', 'AUDITOR', 'MANAGER');
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        loading,
        login,
        logout,
        hasRole,
        hasAnyRole,
        hasPermission,
        isAdmin,
        isPrivileged,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
