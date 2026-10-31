import React from 'react';
import { AlertTriangle, ShieldCheck, AlertCircle, ShieldAlert } from 'lucide-react';

const RiskBadge = ({ classification, score }) => {
  const norm = (classification || '').toUpperCase();

  let badgeClass = 'badge-low';
  let Icon = ShieldCheck;

  if (norm === 'CRITICAL') {
    badgeClass = 'badge-critical';
    Icon = ShieldAlert;
  } else if (norm === 'HIGH') {
    badgeClass = 'badge-high';
    Icon = AlertTriangle;
  } else if (norm === 'MEDIUM') {
    badgeClass = 'badge-medium';
    Icon = AlertCircle;
  }

  return (
    <span className={`badge ${badgeClass} d-inline-flex align-items-center gap-1 px-2.5 py-1.5 rounded-pill`}>
      <Icon size={14} />
      <span>{norm || 'LOW'}</span>
      {score !== undefined && score !== null && (
        <span className="ms-1 opacity-75">({score})</span>
      )}
    </span>
  );
};

export default RiskBadge;
