import React from 'react';

const StatCard = ({ title, value, icon: Icon, color = 'primary', subtitle, actionButton }) => {
  const colorMap = {
    primary: { bg: '#e0f2fe', text: '#0284c7' },
    danger: { bg: '#fee2e2', text: '#ef4444' },
    warning: { bg: '#fef3c7', text: '#f59e0b' },
    success: { bg: '#dcfce7', text: '#10b981' },
    purple: { bg: '#f3e8ff', text: '#9333ea' },
    navy: { bg: '#1e293b', text: '#f8fafc' },
  };

  const theme = colorMap[color] || colorMap.primary;

  return (
    <div className="card stat-card h-100 shadow-sm border-0">
      <div className="d-flex justify-content-between align-items-start">
        <div>
          <span className="text-muted small fw-semibold text-uppercase tracking-wider">{title}</span>
          <h2 className="mt-2 mb-1 fw-bold text-dark">{value}</h2>
          {subtitle && <p className="mb-0 small text-muted">{subtitle}</p>}
        </div>
        <div
          className="icon-box"
          style={{ backgroundColor: theme.bg, color: theme.text }}
        >
          {Icon && <Icon size={24} />}
        </div>
      </div>
      {actionButton && <div className="mt-3 pt-2 border-top">{actionButton}</div>}
    </div>
  );
};

export default StatCard;
