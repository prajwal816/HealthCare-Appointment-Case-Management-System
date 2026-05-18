import { Link } from 'react-router-dom'
import { Users, Calendar, UserPlus, CalendarPlus } from 'lucide-react'

export default function ReceptionistDashboard() {
  return (
    <div className="space-y-8 animate-fade-in">
      <div>
        <h1 className="page-title">Receptionist Dashboard</h1>
        <p className="text-slate-400 mt-1">Manage patients and appointments</p>
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
        {[
          { icon: UserPlus,    label: 'Register New Patient',  desc: 'Add a new patient to the system',           to: '/patients/new',      color: 'bg-brand-600' },
          { icon: CalendarPlus,label: 'Book Appointment',      desc: 'Schedule a session for a patient',          to: '/appointments/book', color: 'bg-emerald-600' },
          { icon: Users,       label: 'Patient Directory',     desc: 'Search and view patient records',           to: '/patients',          color: 'bg-violet-600' },
          { icon: Calendar,    label: 'Appointment Calendar',  desc: 'View and manage all appointments',          to: '/appointments',      color: 'bg-amber-600' },
        ].map(({ icon: Icon, label, desc, to, color }) => (
          <Link key={to} to={to} className="card hover:border-brand-500/40 transition-all duration-300 group flex items-center gap-5">
            <div className={`w-12 h-12 rounded-xl ${color} flex items-center justify-center flex-shrink-0 group-hover:scale-110 transition-transform`}>
              <Icon size={24} className="text-white" />
            </div>
            <div>
              <h3 className="font-semibold text-white">{label}</h3>
              <p className="text-sm text-slate-500 mt-0.5">{desc}</p>
            </div>
          </Link>
        ))}
      </div>
    </div>
  )
}
