import { useAuthStore } from '../../store/authStore'
import { Calendar, FileText, Bell, User } from 'lucide-react'

export default function PatientDashboard() {
  const user = useAuthStore((s) => s.user)

  return (
    <div className="space-y-8 animate-fade-in">
      <div className="card bg-gradient-to-br from-brand-900/50 to-surface-card border-brand-500/20">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-brand-600/30 border border-brand-500/40 flex items-center justify-center text-brand-300 font-bold text-xl">
            {user?.firstName?.[0]}{user?.lastName?.[0]}
          </div>
          <div>
            <p className="text-slate-400 text-sm">Welcome back,</p>
            <h1 className="text-2xl font-bold text-white">{user?.firstName} {user?.lastName}</h1>
            <p className="text-slate-500 text-sm mt-0.5">{user?.email}</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
        {[
          { icon: Calendar, label: 'My Appointments', desc: 'View and manage your upcoming sessions', color: 'bg-brand-600', href: '/appointments' },
          { icon: FileText, label: 'My Records',      desc: 'Access your medical history and notes',  color: 'bg-emerald-600', href: '/records' },
          { icon: Bell,     label: 'Notifications',   desc: 'Stay updated on your care plan',          color: 'bg-violet-600', href: '/notifications' },
        ].map(({ icon: Icon, label, desc, color }) => (
          <div key={label} className="card hover:border-brand-500/40 transition-all duration-300 cursor-pointer group">
            <div className={`w-11 h-11 rounded-xl ${color} flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
              <Icon size={22} className="text-white" />
            </div>
            <h3 className="font-semibold text-white mb-1">{label}</h3>
            <p className="text-sm text-slate-500">{desc}</p>
          </div>
        ))}
      </div>

      <div className="card">
        <h2 className="section-title mb-4">Your Care Team</h2>
        <p className="text-slate-500 text-sm">Contact your assigned therapist through the appointment system.</p>
      </div>
    </div>
  )
}
