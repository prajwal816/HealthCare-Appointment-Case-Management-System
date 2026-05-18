import { useState } from 'react'
import { useAuthStore } from '../../store/authStore'
import { Settings, User, Shield, Bell, Database, Save, CheckCircle } from 'lucide-react'
import toast from 'react-hot-toast'

const Section = ({ icon: Icon, title, children }) => (
  <div className="card">
    <div className="flex items-center gap-3 mb-6 pb-4 border-b border-surface-border">
      <div className="w-9 h-9 rounded-xl bg-brand-600/20 flex items-center justify-center">
        <Icon size={18} className="text-brand-400" />
      </div>
      <h2 className="section-title">{title}</h2>
    </div>
    {children}
  </div>
)

export default function SettingsPage() {
  const { user } = useAuthStore()
  const [saved, setSaved] = useState(false)

  const handleSave = () => {
    setSaved(true)
    toast.success('Settings saved successfully')
    setTimeout(() => setSaved(false), 3000)
  }

  return (
    <div className="space-y-6 animate-fade-in max-w-3xl">
      {/* Header */}
      <div>
        <h1 className="page-title flex items-center gap-3">
          <Settings size={28} className="text-brand-400" />
          Settings
        </h1>
        <p className="text-slate-400 mt-1">Manage your account and system preferences</p>
      </div>

      {/* Profile */}
      <Section icon={User} title="Profile Information">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div>
            <label className="block text-sm font-medium text-slate-400 mb-2">First Name</label>
            <input defaultValue={user?.firstName} className="input w-full" readOnly />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-400 mb-2">Last Name</label>
            <input defaultValue={user?.lastName} className="input w-full" readOnly />
          </div>
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-slate-400 mb-2">Email Address</label>
            <input defaultValue={user?.email} className="input w-full" readOnly />
          </div>
          <div>
            <label className="block text-sm font-medium text-slate-400 mb-2">Role</label>
            <div className="input flex items-center gap-2">
              <Shield size={14} className="text-brand-400" />
              <span className="text-slate-300">{user?.role?.replace('_', ' ')}</span>
            </div>
          </div>
        </div>
      </Section>

      {/* Notifications */}
      <Section icon={Bell} title="Notification Preferences">
        <div className="space-y-4">
          {[
            { label: 'Email notifications for new appointments', defaultChecked: true },
            { label: 'SMS reminders for upcoming sessions', defaultChecked: true },
            { label: 'System alerts and announcements', defaultChecked: false },
            { label: 'Weekly analytics reports', defaultChecked: true },
          ].map(({ label, defaultChecked }) => (
            <label key={label} className="flex items-center justify-between p-4 bg-surface rounded-xl cursor-pointer hover:bg-surface-border/50 transition-colors">
              <span className="text-sm text-slate-300">{label}</span>
              <input type="checkbox" defaultChecked={defaultChecked}
                className="w-4 h-4 accent-brand-500 cursor-pointer" />
            </label>
          ))}
        </div>
      </Section>

      {/* System Info */}
      <Section icon={Database} title="System Information">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[
            { label: 'System Name', value: 'CIMHANS Mental Health Platform' },
            { label: 'Version',     value: '1.0.0' },
            { label: 'Backend API', value: 'http://localhost:8081' },
            { label: 'Database',    value: 'PostgreSQL 16.14' },
          ].map(({ label, value }) => (
            <div key={label} className="p-4 bg-surface rounded-xl">
              <p className="text-xs text-slate-500 mb-1">{label}</p>
              <p className="text-sm font-medium text-slate-300">{value}</p>
            </div>
          ))}
        </div>
      </Section>

      {/* Save Button */}
      <div className="flex justify-end">
        <button onClick={handleSave}
          className={`btn-primary flex items-center gap-2 ${saved ? 'bg-emerald-600 hover:bg-emerald-700' : ''}`}>
          {saved ? <CheckCircle size={16} /> : <Save size={16} />}
          {saved ? 'Saved!' : 'Save Changes'}
        </button>
      </div>
    </div>
  )
}
