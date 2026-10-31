import React, { useState, useEffect } from 'react';
import { dashboardService, riskService } from '../services/api';
import { Link } from 'react-router-dom';
import StatCard from '../components/StatCard';
import RiskBadge from '../components/RiskBadge';
import {
  Users,
  Shield,
  KeyRound,
  ShieldAlert,
  AlertTriangle,
  UserCheck,
  RefreshCw,
  ArrowRight,
  ShieldOff,
  Activity,
  FileCheck2,
} from 'lucide-react';
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
} from 'chart.js';
import { Doughnut, Bar, Pie } from 'react-chartjs-2';

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement, Title);

const AdminDashboard = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [scanning, setScanning] = useState(false);

  const fetchStats = async () => {
    try {
      const res = await dashboardService.getStats();
      setStats(res.data);
    } catch (err) {
      console.error('Failed to load dashboard metrics:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStats();
  }, []);

  const handleRunScan = async () => {
    setScanning(true);
    try {
      await riskService.runScan();
      await fetchStats();
    } catch (err) {
      console.error('Scan execution error:', err);
    } finally {
      setScanning(false);
    }
  };

  if (loading) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: '60vh' }}>
        <div className="spinner-border text-primary" role="status">
          <span className="visually-hidden">Loading Cyber SOC Dashboard...</span>
        </div>
      </div>
    );
  }

  // Chart 1: Role Distribution
  const roleChartData = {
    labels: Object.keys(stats?.usersByRole || {}),
    datasets: [
      {
        data: Object.values(stats?.usersByRole || {}),
        backgroundColor: ['#0284c7', '#3b82f6', '#10b981', '#f59e0b', '#64748b'],
        borderWidth: 1,
      },
    ],
  };

  // Chart 2: Risk Classification Distribution
  const riskChartData = {
    labels: ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'],
    datasets: [
      {
        label: 'Identities by Risk Tier',
        data: [
          stats?.riskDistribution?.LOW || 0,
          stats?.riskDistribution?.MEDIUM || 0,
          stats?.riskDistribution?.HIGH || 0,
          stats?.riskDistribution?.CRITICAL || 0,
        ],
        backgroundColor: ['#10b981', '#eab308', '#f97316', '#ef4444'],
        borderRadius: 6,
      },
    ],
  };

  // Chart 3: Privileged vs Regular
  const privVsRegData = {
    labels: Object.keys(stats?.privilegedVsRegular || {}),
    datasets: [
      {
        data: Object.values(stats?.privilegedVsRegular || {}),
        backgroundColor: ['#f97316', '#0284c7'],
        borderWidth: 1,
      },
    ],
  };

  // Chart 4: Dormant Elevated Permissions
  const unusedChartData = {
    labels: Object.keys(stats?.unusedPermissionsSummary || {}),
    datasets: [
      {
        label: 'Dormant Assignments (>30 Days)',
        data: Object.values(stats?.unusedPermissionsSummary || {}),
        backgroundColor: '#ef4444',
        borderRadius: 4,
      },
    ],
  };

  return (
    <div className="container-fluid p-0">
      {/* Page Title & Scan Action */}
      <div className="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
        <div>
          <h3 className="fw-bold text-dark mb-1">Identity Security Operations Center</h3>
          <p className="text-muted small mb-0">
            Real-time RBAC governance, privilege creep identification, and identity risk metrics
          </p>
        </div>
        <div className="d-flex gap-2">
          <button
            onClick={handleRunScan}
            disabled={scanning}
            className="btn btn-primary d-flex align-items-center gap-2 shadow-sm"
          >
            <RefreshCw size={16} className={scanning ? 'spin-icon' : ''} />
            <span>{scanning ? 'Analyzing Access Patterns...' : 'Execute Privilege Creep Scan'}</span>
          </button>
          <Link to="/reports" className="btn btn-outline-secondary d-flex align-items-center gap-2">
            <FileCheck2 size={16} /> Export Reports
          </Link>
        </div>
      </div>

      {/* 8 Primary Security Metric Cards */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Total Users"
            value={stats?.totalUsers || 0}
            subtitle={`${stats?.activeUsers || 0} active accounts`}
            icon={Users}
            color="primary"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Privileged Accounts"
            value={stats?.privilegedAccounts || 0}
            subtitle="Holds elevated roles/permissions"
            icon={Shield}
            color="purple"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Unused Elevated Privileges"
            value={stats?.unusedElevatedPermissions || 0}
            subtitle="Inactive for >= 30 days"
            icon={ShieldOff}
            color="danger"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Critical & High Risk"
            value={(stats?.criticalRiskUsers || 0) + (stats?.highRiskUsers || 0)}
            subtitle={`${stats?.criticalRiskUsers || 0} Critical, ${stats?.highRiskUsers || 0} High`}
            icon={ShieldAlert}
            color="danger"
          />
        </div>
      </div>

      {/* Second Row of Cards */}
      <div className="row g-3 mb-4">
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Active Security Roles"
            value={stats?.totalRoles || 0}
            subtitle="Defined authorization tiers"
            icon={KeyRound}
            color="navy"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Fine-Grained Permissions"
            value={stats?.totalPermissions || 0}
            subtitle="Granular privilege entitlements"
            icon={KeyRound}
            color="primary"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Pending Privilege Reviews"
            value={stats?.pendingPrivilegeReviews || 0}
            subtitle="Awaiting admin revocation"
            icon={UserCheck}
            color="warning"
          />
        </div>
        <div className="col-12 col-sm-6 col-xl-3">
          <StatCard
            title="Identity Hygiene Ratio"
            value={`${Math.round(
              ((stats?.totalUsers - (stats?.criticalRiskUsers + stats?.highRiskUsers)) /
                (stats?.totalUsers || 1)) *
                100
            )}%`}
            subtitle="Compliant low/medium accounts"
            icon={Activity}
            color="success"
          />
        </div>
      </div>

      {/* Interactive Charts Row */}
      <div className="row g-4 mb-4">
        <div className="col-12 col-lg-6">
          <div className="card shadow-sm border-0 h-100 p-4">
            <h5 className="fw-bold mb-3 text-dark">Identity Risk Classification Distribution</h5>
            <div style={{ height: '240px' }} className="d-flex align-items-center justify-content-center">
              <Bar
                data={riskChartData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: { legend: { display: false } },
                }}
              />
            </div>
          </div>
        </div>

        <div className="col-12 col-lg-6">
          <div className="card shadow-sm border-0 h-100 p-4">
            <h5 className="fw-bold mb-3 text-dark">Privilege Creep: Dormant Elevated Permissions</h5>
            <div style={{ height: '240px' }} className="d-flex align-items-center justify-content-center">
              {Object.keys(stats?.unusedPermissionsSummary || {}).length > 0 ? (
                <Bar
                  data={unusedChartData}
                  options={{
                    indexAxis: 'y',
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                  }}
                />
              ) : (
                <div className="text-muted small">No dormant elevated permissions currently detected.</div>
              )}
            </div>
          </div>
        </div>

        <div className="col-12 col-md-6 col-lg-6">
          <div className="card shadow-sm border-0 h-100 p-4">
            <h5 className="fw-bold mb-3 text-dark">User Distribution by Security Role</h5>
            <div style={{ height: '220px' }} className="d-flex align-items-center justify-content-center">
              <Doughnut
                data={roleChartData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: { legend: { position: 'bottom' } },
                }}
              />
            </div>
          </div>
        </div>

        <div className="col-12 col-md-6 col-lg-6">
          <div className="card shadow-sm border-0 h-100 p-4">
            <h5 className="fw-bold mb-3 text-dark">Privileged Accounts vs. Standard Employees</h5>
            <div style={{ height: '220px' }} className="d-flex align-items-center justify-content-center">
              <Pie
                data={privVsRegData}
                options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: { legend: { position: 'bottom' } },
                }}
              />
            </div>
          </div>
        </div>
      </div>

      {/* High-Risk Alerts & Forensic Logs Row */}
      <div className="row g-4">
        {/* High Risk Alert Panel */}
        <div className="col-12 col-xl-6">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
              <div className="d-flex align-items-center gap-2">
                <AlertTriangle size={18} className="text-danger" />
                <h5 className="fw-bold mb-0 text-dark">High & Critical Risk Identities</h5>
              </div>
              <Link to="/risk-analysis" className="btn btn-sm btn-outline-primary d-flex align-items-center gap-1">
                View All <ArrowRight size={14} />
              </Link>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Identity</th>
                      <th>Department</th>
                      <th>Risk Classification</th>
                      <th>Dormant Privileges</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats?.highRiskAlerts && stats.highRiskAlerts.length > 0 ? (
                      stats.highRiskAlerts.slice(0, 5).map((alert) => (
                        <tr key={alert.id}>
                          <td>
                            <div className="fw-semibold text-dark">{alert.username}</div>
                            <div className="text-muted" style={{ fontSize: '0.75rem' }}>{alert.fullName}</div>
                          </td>
                          <td>{alert.department || 'N/A'}</td>
                          <td>
                            <RiskBadge classification={alert.riskClassification} score={alert.riskScore} />
                          </td>
                          <td>
                            <span className="badge bg-danger bg-opacity-10 text-danger border border-danger border-opacity-25">
                              {alert.unusedElevatedPermissionsCount} unused
                            </span>
                          </td>
                          <td>
                            <Link to="/privilege-review" className="btn btn-sm btn-outline-danger py-1 px-2.5">
                              Review
                            </Link>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="5" className="text-center py-4 text-muted small">
                          No high or critical risk accounts detected. The organization is compliant!
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        {/* Recent Forensic Audit Activity */}
        <div className="col-12 col-xl-6">
          <div className="card shadow-sm border-0 h-100">
            <div className="card-header bg-white py-3 border-bottom d-flex justify-content-between align-items-center">
              <div className="d-flex align-items-center gap-2">
                <Activity size={18} className="text-primary" />
                <h5 className="fw-bold mb-0 text-dark">Recent Forensic Audit Trail</h5>
              </div>
              <Link to="/audit-logs" className="btn btn-sm btn-outline-primary d-flex align-items-center gap-1">
                All Logs <ArrowRight size={14} />
              </Link>
            </div>
            <div className="card-body p-0">
              <div className="table-responsive">
                <table className="table table-hover align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Actor</th>
                      <th>Action</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats?.recentAuditLogs && stats.recentAuditLogs.length > 0 ? (
                      stats.recentAuditLogs.slice(0, 5).map((log) => (
                        <tr key={log.id}>
                          <td className="text-muted small">
                            {new Date(log.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}
                          </td>
                          <td className="fw-semibold text-dark">{log.username}</td>
                          <td>
                            <span className="badge bg-light text-dark border font-monospace" style={{ fontSize: '0.72rem' }}>
                              {log.action}
                            </span>
                          </td>
                          <td>
                            <span
                              className={`badge ${
                                log.status === 'SUCCESS'
                                  ? 'bg-success bg-opacity-10 text-success'
                                  : 'bg-danger bg-opacity-10 text-danger'
                              }`}
                            >
                              {log.status}
                            </span>
                          </td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td colSpan="4" className="text-center py-4 text-muted small">
                          No audit activity recorded yet.
                        </td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminDashboard;
