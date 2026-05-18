import { Calendar } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function AppointmentCalendar() {
  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Appointments</h1>
          <p className="text-slate-400 mt-1">Manage and track all appointments</p>
        </div>
        <button className="btn-primary"><Calendar size={16} /> Book Appointment</button>
      </div>
      <div className="card flex flex-col items-center justify-center py-24 text-center">
        <Calendar size={56} className="text-brand-500 mb-4 opacity-60" />
        <h2 className="text-xl font-semibold text-white mb-2">Appointment Calendar</h2>
        <p className="text-slate-400 max-w-md">
          Full FullCalendar integration renders here. Book, reschedule, and track appointments
          with drag-and-drop scheduling across daily, weekly, and monthly views.
        </p>
        <div className="mt-8 grid grid-cols-3 gap-4 w-full max-w-lg">
          {['SCHEDULED','CONFIRMED','COMPLETED'].map(s => (
            <div key={s} className="card bg-surface py-4">
              <p className="text-xs text-slate-500 mb-1">{s}</p>
              <p className="text-2xl font-bold text-white">—</p>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
