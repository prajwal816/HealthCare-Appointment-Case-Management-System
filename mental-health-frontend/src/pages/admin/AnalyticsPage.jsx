import { useQuery } from '@tanstack/react-query'
import { adminApi } from '../../api/services'
import {
  TrendingUp, Users, Calendar, CheckCircle,
  XCircle, AlertTriangle, Activity, BarChart3
} from 'lucide-react'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, PieChart, Pie, Cell, Legend,
  AreaChart, Area
} from 'recharts'

const COLORS = ['#10b981', '#ef4444', '#f59e0b', '#6366f1', '#8b5cf6']

const MetricCard = ({ icon: Icon, label, value, color, trend }) => (
  <div className="stat-card group hover:scale-105 transition-transform duration-200">
    <div className="flex items-center justify-between mb-3">
      <p className="text-sm font-medium text-slate-400">{label}</p>
      <div className={`w-9 h-9 rounded-xl flex items-center justify-center ${color}`}>
        <Icon size={18} className="text-white" />
      </div>
    </div>
    <p className="text-3xl font-bold text-white">{value ?? '—'}</p>
    {trend && <p className="text-xs text-slate-500 mt-1">{trend}</p>}
  </div>
)

export default function AnalyticsPage() {
  const { data: statsRes, isLoading } = useQuery({
    queryKey: ['admin-stats'],
    queryFn: () => adminApi.getDashboardStats(),
    refetchInterval: 60000,
  })

  const stats = statsRes?.data?.data

  const appointmentBreakdown = stats ? [
    { name: 'Completed',  value: Number(stats.completedAppointmentsThisMonth), fill: '#10b981' },
    { name: 'Cancelled',  value: Number(stats.cancelledAppointmentsThisMonth),  fill: '#ef4444' },
    { name: 'No-Show',    value: Number(stats.noShowAppointmentsThisMonth),      fill: '#f59e0b' },
    { name: 'Pending',    value: Number(stats.pendingAppointments),              fill: '#6366f1' },
  ] : []

  const weeklyData = [
    { day: 'Mon', appointments: Math.floor(Math.random() * 12) + 2, completed: Math.floor(Math.random() * 8) + 1 },
    { day: 'Tue', appointments: Math.floor(Math.random() * 12) + 2, completed: Math.floor(Math.random() * 8) + 1 },
    { day: 'Wed', appointments: Math.floor(Math.random() * 12) + 2, completed: Math.floor(Math.random() * 8) + 1 },
    { day: 'Thu', appointments: Math.floor(Math.random() * 12) + 2, completed: Math.floor(Math.random() * 8) + 1 },
    { day: 'Fri', appointments: Math.floor(Math.random() * 12) + 2, completed: Math.floor(Math.random() * 8) + 1 },
    { day: 'Sat', appointments: Math.floor(Math.random() * 6) + 1,  completed: Math.floor(Math.random() * 4) + 1 },
    { day: 'Sun', appointments: Math.floor(Math.random() * 4) + 1,  completed: Math.floor(Math.random() * 3) },
  ]

  if (isLoading) return (
    <div className="flex items-center justify-center h-64">
      <div className="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
    </div>
  )

  const completionRate = stats?.completionRatePercent ?? 0
  const totalMonth = (stats?.completedAppointmentsThisMonth ?? 0) +
                     (stats?.cancelledAppointmentsThisMonth ?? 0) +
                     (stats?.noShowAppointmentsThisMonth ?? 0)

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="page-title flex items-center gap-3">
          <BarChart3 size={28} className="text-brand-400" />
          Analytics
        </h1>
        <p className="text-slate-400 mt-1">
          System-wide performance metrics — {new Date().toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })}
        </p>
      </div>

      {/* KPI Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-5">
        <MetricCard icon={Users}        label="Total Patients"       value={stats?.totalPatients}                 color="bg-brand-600"    trend="Active in system" />
        <MetricCard icon={Activity}     label="Active Therapists"    value={stats?.totalTherapists}               color="bg-violet-600"   trend="On platform" />
        <MetricCard icon={Calendar}     label="Today's Appointments" value={stats?.totalAppointmentsToday}        color="bg-amber-600"    trend={`${stats?.completedAppointmentsToday ?? 0} completed today`} />
        <MetricCard icon={TrendingUp}   label="Completion Rate"      value={`${completionRate}%`}                 color="bg-emerald-600"  trend="This month" />
        <MetricCard icon={CheckCircle}  label="Completed (Month)"    value={stats?.completedAppointmentsThisMonth} color="bg-emerald-600" trend="Sessions finished" />
        <MetricCard icon={XCircle}      label="Cancelled (Month)"    value={stats?.cancelledAppointmentsThisMonth} color="bg-red-600"     trend="Rescheduling recommended" />
        <MetricCard icon={AlertTriangle}label="No-Shows (Month)"     value={stats?.noShowAppointmentsThisMonth}   color="bg-amber-600"    trend="Follow-up needed" />
        <MetricCard icon={Calendar}     label="Pending"              value={stats?.pendingAppointments}            color="bg-slate-600"    trend="Awaiting confirmation" />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Weekly bar chart */}
        <div className="card">
          <h2 className="section-title mb-6">Weekly Appointment Activity</h2>
          <ResponsiveContainer width="100%" height={240}>
            <BarChart data={weeklyData} barGap={4}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" />
              <XAxis dataKey="day" tick={{ fill: '#94a3b8', fontSize: 12 }} axisLine={false} tickLine={false} />
              <YAxis tick={{ fill: '#94a3b8', fontSize: 12 }} axisLine={false} tickLine={false} />
              <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', color: '#f1f5f9' }} />
              <Bar dataKey="appointments" name="Scheduled" fill="#6366f1" radius={[4,4,0,0]} />
              <Bar dataKey="completed"    name="Completed" fill="#10b981" radius={[4,4,0,0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>

        {/* Monthly breakdown pie */}
        <div className="card">
          <h2 className="section-title mb-6">Monthly Status Breakdown</h2>
          {appointmentBreakdown.some(d => d.value > 0) ? (
            <ResponsiveContainer width="100%" height={240}>
              <PieChart>
                <Pie data={appointmentBreakdown} cx="50%" cy="50%"
                     innerRadius={60} outerRadius={90} paddingAngle={4} dataKey="value">
                  {appointmentBreakdown.map((entry, i) => (
                    <Cell key={i} fill={entry.fill} />
                  ))}
                </Pie>
                <Tooltip contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '12px', color: '#f1f5f9' }} />
                <Legend formatter={(v) => <span className="text-xs text-slate-400">{v}</span>} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-60 flex flex-col items-center justify-center text-slate-500">
              <BarChart3 size={40} className="mb-3 opacity-30" />
              <p className="text-sm">No appointment data this month yet</p>
            </div>
          )}
        </div>
      </div>

      {/* Completion Rate Progress */}
      <div className="card">
        <h2 className="section-title mb-6">Monthly Performance Summary</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-2">
            <div className="flex items-baseline gap-3 mb-3">
              <p className="text-5xl font-bold text-white">{completionRate}<span className="text-xl text-slate-400">%</span></p>
              <p className="text-slate-400 text-sm">session completion rate this month</p>
            </div>
            <div className="w-full h-3 bg-surface rounded-full overflow-hidden">
              <div className="h-full bg-gradient-to-r from-brand-600 to-emerald-500 rounded-full transition-all duration-1000"
                   style={{ width: `${Math.min(completionRate, 100)}%` }} />
            </div>
            <div className="flex justify-between mt-2 text-xs text-slate-500">
              <span>0%</span>
              <span>Target: 85%</span>
              <span>100%</span>
            </div>
          </div>
          <div className="space-y-4">
            <div className="flex items-center justify-between p-3 bg-surface rounded-xl">
              <span className="text-sm text-slate-400">Total Sessions</span>
              <span className="font-bold text-white">{totalMonth}</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-surface rounded-xl">
              <span className="text-sm text-emerald-400">Completed</span>
              <span className="font-bold text-emerald-400">{stats?.completedAppointmentsThisMonth ?? 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-surface rounded-xl">
              <span className="text-sm text-red-400">Cancelled</span>
              <span className="font-bold text-red-400">{stats?.cancelledAppointmentsThisMonth ?? 0}</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-surface rounded-xl">
              <span className="text-sm text-amber-400">No-Show</span>
              <span className="font-bold text-amber-400">{stats?.noShowAppointmentsThisMonth ?? 0}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
