import { useQuery } from '@tanstack/react-query'
import { adminApi } from '../../api/services'
import {
  Users, Calendar, CheckCircle, XCircle, TrendingUp,
  Clock, AlertTriangle, Activity, UserCheck
} from 'lucide-react'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend
} from 'recharts'

const StatCard = ({ icon: Icon, label, value, color, sub }) => (
  <div className="stat-card">
    <div className="flex items-center justify-between">
      <p className="text-sm font-medium text-slate-400">{label}</p>
      <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${color}`}>
        <Icon size={18} className="text-white" />
      </div>
    </div>
    <p className="text-3xl font-bold text-white">{value ?? '—'}</p>
    {sub && <p className="text-xs text-slate-500">{sub}</p>}
  </div>
)

const PIE_COLORS = ['#10b981', '#ef4444', '#f59e0b', '#6366f1']

export default function AdminDashboard() {
  const { data: statsRes, isLoading } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: () => adminApi.getDashboardStats(),
    refetchInterval: 120000,
  })

  const stats = statsRes?.data?.data

  const pieData = stats ? [
    { name: 'Completed',  value: Number(stats.completedAppointmentsThisMonth) },
    { name: 'Cancelled',  value: Number(stats.cancelledAppointmentsThisMonth) },
    { name: 'No-Show',    value: Number(stats.noShowAppointmentsThisMonth) },
    { name: 'Pending',    value: Number(stats.pendingAppointments) },
  ] : []

  if (isLoading) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="page-title">Admin Dashboard</h1>
        <p className="text-slate-400 mt-1">System-wide overview — {new Date().toLocaleDateString('en-IN', { weekday:'long', year:'numeric', month:'long', day:'numeric' })}</p>
      </div>

      {/* Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <StatCard icon={Users}      label="Total Patients"         value={stats?.totalPatients}            color="bg-brand-600"    sub="Active records" />
        <StatCard icon={UserCheck}  label="Active Cases"           value={stats?.activePatients}           color="bg-emerald-600"  sub="Currently in treatment" />
        <StatCard icon={Activity}   label="Therapists"             value={stats?.totalTherapists}          color="bg-violet-600"   sub="On the system" />
        <StatCard icon={Calendar}   label="Appointments Today"     value={stats?.totalAppointmentsToday}   color="bg-amber-600"    sub={`${stats?.completedAppointmentsToday ?? 0} completed`} />
        <StatCard icon={CheckCircle} label="Completed This Month"  value={stats?.completedAppointmentsThisMonth} color="bg-emerald-600" sub={`${stats?.completionRatePercent ?? 0}% completion rate`} />
        <StatCard icon={XCircle}    label="Cancelled This Month"   value={stats?.cancelledAppointmentsThisMonth} color="bg-red-600"     sub="Rescheduling recommended" />
        <StatCard icon={AlertTriangle} label="No-Shows This Month" value={stats?.noShowAppointmentsThisMonth}    color="bg-amber-600"   sub="Follow-up required" />
        <StatCard icon={Clock}      label="Pending Appointments"   value={stats?.pendingAppointments}      color="bg-slate-600"    sub="Awaiting confirmation" />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-5">
        {/* Appointment Breakdown Pie */}
        <div className="card">
          <h2 className="section-title mb-6">Monthly Breakdown</h2>
          {pieData.some(d => d.value > 0) ? (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={55} outerRadius={85}
                     paddingAngle={4} dataKey="value">
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', color: '#f1f5f9' }} />
                <Legend formatter={(v) => <span className="text-xs text-slate-400">{v}</span>} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-56 flex items-center justify-center text-slate-500 text-sm">No data yet</div>
          )}
        </div>

        {/* Completion Rate Card */}
        <div className="card lg:col-span-2 flex flex-col justify-between">
          <h2 className="section-title mb-4">Completion Rate</h2>
          <div className="flex items-end gap-6">
            <div>
              <p className="text-6xl font-bold text-white">{stats?.completionRatePercent ?? 0}<span className="text-2xl text-slate-400">%</span></p>
              <p className="text-slate-400 text-sm mt-2">This month's session completion rate</p>
            </div>
            <div className="flex-1 h-3 bg-surface rounded-full overflow-hidden">
              <div className="h-full bg-gradient-to-r from-brand-600 to-emerald-500 rounded-full transition-all duration-1000"
                   style={{ width: `${stats?.completionRatePercent ?? 0}%` }} />
            </div>
          </div>
          <div className="grid grid-cols-3 gap-4 mt-6 pt-6 border-t border-surface-border">
            <div className="text-center">
              <p className="text-2xl font-bold text-emerald-400">{stats?.completedAppointmentsThisMonth ?? 0}</p>
              <p className="text-xs text-slate-500 mt-1">Completed</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-red-400">{stats?.cancelledAppointmentsThisMonth ?? 0}</p>
              <p className="text-xs text-slate-500 mt-1">Cancelled</p>
            </div>
            <div className="text-center">
              <p className="text-2xl font-bold text-amber-400">{stats?.noShowAppointmentsThisMonth ?? 0}</p>
              <p className="text-xs text-slate-500 mt-1">No-Show</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
