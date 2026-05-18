import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { sessionNoteApi, patientApi } from '../../api/services'
import { useAuthStore } from '../../store/authStore'
import { FileText, Plus, Lock, AlertCircle, ChevronDown, ChevronUp } from 'lucide-react'
import toast from 'react-hot-toast'

const MoodBadge = ({ score }) => {
  const colors = {
    1: 'bg-red-500/20 text-red-400',
    2: 'bg-orange-500/20 text-orange-400',
    3: 'bg-amber-500/20 text-amber-400',
    4: 'bg-lime-500/20 text-lime-400',
    5: 'bg-emerald-500/20 text-emerald-400',
  }
  const labels = { 1:'Very Low', 2:'Low', 3:'Moderate', 4:'Good', 5:'Excellent' }
  return (
    <span className={`badge ${colors[score] ?? 'bg-slate-500/20 text-slate-400'}`}>
      Mood: {labels[score] ?? score}
    </span>
  )
}

const NoteCard = ({ note }) => {
  const [open, setOpen] = useState(false)
  return (
    <div className="card hover:border-brand-500/30 transition-colors">
      <div className="flex items-start justify-between cursor-pointer" onClick={() => setOpen(o => !o)}>
        <div className="flex-1">
          <div className="flex items-center gap-3 flex-wrap">
            <span className="text-sm font-semibold text-white">{note.sessionDate ?? 'Session'}</span>
            {note.moodScore && <MoodBadge score={note.moodScore} />}
            {note.isFinalized && (
              <span className="badge bg-violet-500/20 text-violet-400 flex items-center gap-1">
                <Lock size={10} /> Finalized
              </span>
            )}
          </div>
          <p className="text-xs text-slate-500 mt-1 truncate max-w-lg">{note.subjective}</p>
        </div>
        <button className="text-slate-400 ml-4 mt-1 shrink-0">
          {open ? <ChevronUp size={16} /> : <ChevronDown size={16} />}
        </button>
      </div>
      {open && (
        <div className="mt-4 pt-4 border-t border-surface-border space-y-3">
          <div>
            <p className="text-xs text-slate-500 mb-1">Subjective (Patient's Report)</p>
            <p className="text-sm text-slate-300">{note.subjective ?? '—'}</p>
          </div>
          <div>
            <p className="text-xs text-slate-500 mb-1">Objective (Clinical Observations)</p>
            <p className="text-sm text-slate-300">{note.objective ?? '—'}</p>
          </div>
          <div>
            <p className="text-xs text-slate-500 mb-1">Assessment</p>
            <p className="text-sm text-slate-300">{note.assessment ?? '—'}</p>
          </div>
          <div>
            <p className="text-xs text-slate-500 mb-1">Plan</p>
            <p className="text-sm text-slate-300">{note.plan ?? '—'}</p>
          </div>
          {note.homeworkAssigned && (
            <div>
              <p className="text-xs text-slate-500 mb-1">Homework</p>
              <p className="text-sm text-slate-300">{note.homeworkAssigned}</p>
            </div>
          )}
        </div>
      )}
    </div>
  )
}

export default function SessionNotesPage() {
  const { user } = useAuthStore()
  const qc = useQueryClient()
  const [selectedPatientId, setSelectedPatientId] = useState('')
  const [showForm, setShowForm] = useState(false)
  const [form, setForm] = useState({
    subjective: '', objective: '', assessment: '', plan: '',
    moodScore: 3, homeworkAssigned: '', appointmentId: ''
  })

  const { data: patientsRes } = useQuery({
    queryKey: ['patients-list'],
    queryFn: () => patientApi.getAll({ size: 100 }),
  })
  const patients = patientsRes?.data?.data?.content ?? []

  const { data: notesRes, isLoading } = useQuery({
    queryKey: ['session-notes-patient', selectedPatientId],
    queryFn: () => sessionNoteApi.getByPatient(selectedPatientId, { size: 50 }),
    enabled: !!selectedPatientId,
  })
  const notes = notesRes?.data?.data?.content ?? []

  const createMutation = useMutation({
    mutationFn: ({ appointmentId, data }) => sessionNoteApi.create(appointmentId, data),
    onSuccess: () => {
      toast.success('Session note saved')
      setShowForm(false)
      setForm({ subjective:'', objective:'', assessment:'', plan:'', moodScore:3, homeworkAssigned:'', appointmentId:'' })
      qc.invalidateQueries(['session-notes-patient', selectedPatientId])
    },
    onError: (err) => toast.error(err.response?.data?.error ?? 'Failed to save note'),
  })

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!form.appointmentId) return toast.error('Appointment ID is required')
    createMutation.mutate({ appointmentId: form.appointmentId, data: form })
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title flex items-center gap-3">
            <FileText size={28} className="text-brand-400" />
            Session Notes
          </h1>
          <p className="text-slate-400 mt-1">SOAP notes for clinical documentation</p>
        </div>
        <button onClick={() => setShowForm(v => !v)} className="btn-primary flex items-center gap-2">
          <Plus size={16} />
          New Note
        </button>
      </div>

      {/* New Note Form */}
      {showForm && (
        <div className="card border-brand-500/30">
          <h2 className="section-title mb-6">New Session Note</h2>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-slate-400 mb-2">Appointment ID *</label>
              <input className="input w-full" placeholder="Enter appointment UUID"
                value={form.appointmentId} onChange={e => setForm(f => ({ ...f, appointmentId: e.target.value }))} required />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-2">Subjective (S)</label>
                <textarea className="input w-full h-24 resize-none" placeholder="Patient's report of symptoms..."
                  value={form.subjective} onChange={e => setForm(f => ({ ...f, subjective: e.target.value }))} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-2">Objective (O)</label>
                <textarea className="input w-full h-24 resize-none" placeholder="Clinical observations..."
                  value={form.objective} onChange={e => setForm(f => ({ ...f, objective: e.target.value }))} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-2">Assessment (A)</label>
                <textarea className="input w-full h-24 resize-none" placeholder="Diagnosis and analysis..."
                  value={form.assessment} onChange={e => setForm(f => ({ ...f, assessment: e.target.value }))} />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-2">Plan (P)</label>
                <textarea className="input w-full h-24 resize-none" placeholder="Treatment plan..."
                  value={form.plan} onChange={e => setForm(f => ({ ...f, plan: e.target.value }))} />
              </div>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-2">Mood Score (1–5)</label>
                <select className="input w-full" value={form.moodScore}
                  onChange={e => setForm(f => ({ ...f, moodScore: Number(e.target.value) }))}>
                  <option value={1}>1 – Very Low</option>
                  <option value={2}>2 – Low</option>
                  <option value={3}>3 – Moderate</option>
                  <option value={4}>4 – Good</option>
                  <option value={5}>5 – Excellent</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-400 mb-2">Homework Assigned</label>
                <input className="input w-full" placeholder="Optional homework task..."
                  value={form.homeworkAssigned} onChange={e => setForm(f => ({ ...f, homeworkAssigned: e.target.value }))} />
              </div>
            </div>
            <div className="flex gap-3">
              <button type="submit" disabled={createMutation.isPending} className="btn-primary flex items-center gap-2">
                {createMutation.isPending ? <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" /> : <FileText size={16} />}
                Save Note
              </button>
              <button type="button" onClick={() => setShowForm(false)} className="btn-secondary">Cancel</button>
            </div>
          </form>
        </div>
      )}

      {/* Patient Selector */}
      <div className="card">
        <label className="block text-sm font-medium text-slate-400 mb-3">Select Patient to View Notes</label>
        <select className="input w-full max-w-md" value={selectedPatientId}
          onChange={e => setSelectedPatientId(e.target.value)}>
          <option value="">— Choose a patient —</option>
          {patients.map(p => (
            <option key={p.id} value={p.id}>{p.fullName ?? `${p.firstName} ${p.lastName}`} — MRN: {p.mrn}</option>
          ))}
        </select>
      </div>

      {/* Notes List */}
      {selectedPatientId && (
        <div className="space-y-4">
          <h2 className="section-title">Session History</h2>
          {isLoading ? (
            <div className="flex justify-center py-8">
              <div className="w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full animate-spin" />
            </div>
          ) : notes.length === 0 ? (
            <div className="card flex flex-col items-center justify-center py-12 text-slate-500">
              <AlertCircle size={40} className="mb-3 opacity-30" />
              <p>No session notes found for this patient</p>
            </div>
          ) : (
            notes.map(note => <NoteCard key={note.id} note={note} />)
          )}
        </div>
      )}

      {!selectedPatientId && !showForm && (
        <div className="card flex flex-col items-center justify-center py-16 text-slate-500">
          <FileText size={48} className="mb-4 opacity-20" />
          <p className="text-lg font-medium mb-1">Select a patient above</p>
          <p className="text-sm">to view or create session notes</p>
        </div>
      )}
    </div>
  )
}
