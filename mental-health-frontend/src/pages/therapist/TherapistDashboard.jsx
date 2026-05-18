import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../../store/authStore'
import { appointmentApi } from '../../api/services'
import { format } from 'date-fns'
import { Calendar, Clock, User, FileText, CheckCircle, AlertCircle } from 'lucide-react'

const statusBadge = {
  SCHEDULED:  <span className="badge-info">Scheduled</span>,
  CONFIRMED:  <span className="badge-success">Confirmed</span>,
  COMPLETED:  <span className="badge-gray">Completed</span>,
  CANCELLED:  <span className="badge-danger">Cancelled</span>,
  NO_SHOW:    <span className="badge-warning">No-Show</span>,
}

export default function TherapistDashboard() {
  const user = useAuthStore((s) => s.user)
  const today = format(new Date(), 'yyyy-MM-dd')
  const [selectedDate, setSelectedDate] = useState(today)

  const { data: scheduleRes, isLoading } = useQuery({
    queryKey: ['therapist-schedule', selectedDate],
    queryFn: () => appointmentApi.getDailySchedule(user?.therapistId || 'me', selectedDate),
    enabled: !!selectedDate,
  })

  const appointments = scheduleRes?.data?.data ?? []

  return (
    <div className="space-y-8 animate-fade-in">
      <div>
        <h1 className="page-title">My Dashboard</h1>
        <p className="text-slate-400 mt-1">Dr. {user?.firstName} {user?.lastName} — {user?.role}</p>
      </div>

      {/* Quick Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {[
          { label: "Today's Appointments", value: appointments.length, icon: Calendar, color: 'bg-brand-600' },
          { label: 'Completed Today',   value: appointments.filter(a => a.status === 'COMPLETED').length, icon: CheckCircle, color: 'bg-emerald-600' },
          { label: 'Pending Confirm',   value: appointments.filter(a => a.status === 'SCHEDULED').length, icon: AlertCircle, color: 'bg-amber-600' },
          { label: 'Notes Pending',     value: appointments.filter(a => a.status === 'COMPLETED' && !a.hasSessionNote).length, icon: FileText, color: 'bg-violet-600' },
        ].map(({ label, value, icon: Icon, color }) => (
          <div key={label} className="stat-card">
            <div className="flex justify-between items-start">
              <p className="text-sm text-slate-400">{label}</p>
              <div className={`w-8 h-8 rounded-lg flex items-center justify-center ${color}`}>
                <Icon size={15} className="text-white" />
              </div>
            </div>
            <p className="text-3xl font-bold text-white">{value}</p>
          </div>
        ))}
      </div>

      {/* Date Picker + Schedule */}
      <div className="card">
        <div className="flex items-center justify-between mb-6">
          <h2 className="section-title">Daily Schedule</h2>
          <input
            type="date"
            value={selectedDate}
            onChange={e => setSelectedDate(e.target.value)}
            className="input w-auto text-sm"
          />
        </div>

        {isLoading ? (
          <div className="flex justify-center py-12">
            <div className="w-7 h-7 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
          </div>
        ) : appointments.length === 0 ? (
          <div className="text-center py-12 text-slate-500">
            <Calendar size={40} className="mx-auto mb-3 opacity-40" />
            <p>No appointments scheduled for this day</p>
          </div>
        ) : (
          <div className="space-y-3">
            {appointments.map((appt) => (
              <div key={appt.id} className="flex items-center gap-4 p-4 bg-surface rounded-xl border border-surface-border hover:border-brand-500/30 transition-colors">
                <div className="w-16 text-center">
                  <p className="text-sm font-bold text-brand-400">{appt.startTime?.slice(0,5)}</p>
                  <p className="text-xs text-slate-500">{appt.endTime?.slice(0,5)}</p>
                </div>
                <div className="w-px h-10 bg-surface-border" />
                <div className="flex-1 min-w-0">
                  <p className="font-semibold text-white truncate">{appt.patientName}</p>
                  <p className="text-xs text-slate-500">{appt.type?.replace(/_/g,' ')} • MRN: {appt.patientMrn}</p>
                </div>
                <div className="flex items-center gap-3">
                  {statusBadge[appt.status]}
                  {appt.hasSessionNote && <span className="badge-gray"><FileText size={10} className="mr-1" />Note</span>}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
