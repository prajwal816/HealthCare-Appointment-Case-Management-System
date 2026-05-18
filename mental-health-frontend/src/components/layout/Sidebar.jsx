import { NavLink, useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../store/authStore'
import { authApi } from '../../api/services'
import toast from 'react-hot-toast'
import {
  LayoutDashboard, Users, Calendar, FileText,
  Settings, LogOut, Activity, Brain, Shield
} from 'lucide-react'
import clsx from 'clsx'

const navItems = {
  ADMIN: [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/patients',  icon: Users,           label: 'Patients' },
    { to: '/appointments', icon: Calendar,     label: 'Appointments' },
    { to: '/analytics', icon: Activity,        label: 'Analytics' },
    { to: '/settings',  icon: Settings,        label: 'Settings' },
  ],
  PSYCHIATRIST: [
    { to: '/dashboard',    icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/appointments', icon: Calendar,        label: 'My Schedule' },
    { to: '/patients',     icon: Users,           label: 'My Patients' },
    { to: '/session-notes',icon: FileText,        label: 'Session Notes' },
  ],
  PSYCHOLOGIST: [
    { to: '/dashboard',    icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/appointments', icon: Calendar,        label: 'My Schedule' },
    { to: '/patients',     icon: Users,           label: 'My Patients' },
    { to: '/session-notes',icon: FileText,        label: 'Session Notes' },
  ],
  RECEPTIONIST: [
    { to: '/dashboard',    icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/patients',     icon: Users,           label: 'Patients' },
    { to: '/appointments', icon: Calendar,        label: 'Appointments' },
  ],
  PATIENT: [
    { to: '/dashboard',    icon: LayoutDashboard, label: 'My Dashboard' },
    { to: '/appointments', icon: Calendar,        label: 'My Appointments' },
  ],
}

export default function Sidebar() {
  const { user, logout } = useAuthStore()
  const navigate = useNavigate()
  const items = navItems[user?.role] ?? []

  const handleLogout = async () => {
    try {
      await authApi.logout()
    } catch (_) {}
    logout()
    toast.success('Logged out successfully')
    navigate('/login')
  }

  return (
    <aside className="fixed left-0 top-0 h-full w-64 bg-surface-card border-r border-surface-border flex flex-col z-30">
      {/* Logo */}
      <div className="px-6 py-5 border-b border-surface-border">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-brand-600 flex items-center justify-center">
            <Brain size={20} className="text-white" />
          </div>
          <div>
            <p className="font-bold text-white text-sm leading-tight">CIMHANS</p>
            <p className="text-xs text-slate-500">Mental Health System</p>
          </div>
        </div>
      </div>

      {/* Role Badge */}
      <div className="px-4 py-3 border-b border-surface-border">
        <span className="badge-info flex items-center gap-1.5 w-fit">
          <Shield size={11} />
          {user?.role?.replace('_', ' ')}
        </span>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
        {items.map(({ to, icon: Icon, label }) => (
          <NavLink key={to} to={to} end={to === '/dashboard'}>
            {({ isActive }) => (
              <div className={isActive ? 'sidebar-item-active' : 'sidebar-item'}>
                <Icon size={18} />
                <span>{label}</span>
              </div>
            )}
          </NavLink>
        ))}
      </nav>

      {/* User Profile + Logout */}
      <div className="px-3 py-4 border-t border-surface-border space-y-2">
        <div className="flex items-center gap-3 px-3 py-2">
          <div className="w-9 h-9 rounded-full bg-brand-600/30 border border-brand-500/40 flex items-center justify-center text-brand-400 font-bold text-sm">
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-semibold text-white truncate">
              {user?.firstName} {user?.lastName}
            </p>
            <p className="text-xs text-slate-500 truncate">{user?.email}</p>
          </div>
        </div>
        <button onClick={handleLogout} className="sidebar-item w-full text-red-400 hover:text-red-300 hover:bg-red-500/10">
          <LogOut size={18} />
          <span>Sign Out</span>
        </button>
      </div>
    </aside>
  )
}
