import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { patientApi } from '../../api/services'
import { Search, UserPlus, ChevronLeft, ChevronRight, User } from 'lucide-react'
import { format } from 'date-fns'

export default function PatientList() {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [debouncedSearch, setDebouncedSearch] = useState('')

  const handleSearch = (val) => {
    setSearch(val)
    clearTimeout(window._searchTimer)
    window._searchTimer = setTimeout(() => { setDebouncedSearch(val); setPage(0) }, 400)
  }

  const { data, isLoading } = useQuery({
    queryKey: ['patients', debouncedSearch, page],
    queryFn: () => debouncedSearch
      ? patientApi.search(debouncedSearch, { page, size: 15 })
      : patientApi.getAll({ page, size: 15, sortBy: 'createdAt', sortDir: 'desc' }),
  })

  const patients = data?.data?.data?.content ?? []
  const totalPages = data?.data?.data?.totalPages ?? 0
  const totalElements = data?.data?.data?.totalElements ?? 0

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="page-title">Patient Directory</h1>
          <p className="text-slate-400 mt-1">{totalElements} total patients</p>
        </div>
        <button className="btn-primary">
          <UserPlus size={16} /> Register Patient
        </button>
      </div>

      {/* Search */}
      <div className="relative">
        <Search size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
        <input
          value={search}
          onChange={e => handleSearch(e.target.value)}
          placeholder="Search by name, email, phone, or MRN..."
          className="input pl-11"
        />
      </div>

      {/* Table */}
      <div className="card p-0 overflow-hidden">
        <table className="w-full">
          <thead className="border-b border-surface-border bg-surface/50">
            <tr>
              {['Patient', 'MRN', 'DOB / Age', 'Contact', 'Status', 'Registered'].map(h => (
                <th key={h} className="table-header text-left">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {isLoading ? (
              <tr><td colSpan={6} className="text-center py-16 text-slate-500">
                <div className="w-7 h-7 border-2 border-brand-500 border-t-transparent rounded-full animate-spin mx-auto" />
              </td></tr>
            ) : patients.length === 0 ? (
              <tr><td colSpan={6} className="text-center py-16 text-slate-500">
                <User size={40} className="mx-auto mb-3 opacity-30" />
                <p>No patients found</p>
              </td></tr>
            ) : patients.map((p) => (
              <tr key={p.id} className="hover:bg-surface-hover transition-colors cursor-pointer">
                <td className="table-cell">
                  <div className="flex items-center gap-3">
                    <div className="w-9 h-9 rounded-full bg-brand-600/20 border border-brand-500/30 flex items-center justify-center text-brand-400 font-semibold text-sm">
                      {p.firstName?.[0]}{p.lastName?.[0]}
                    </div>
                    <div>
                      <p className="font-semibold text-white">{p.firstName} {p.lastName}</p>
                      <p className="text-xs text-slate-500">{p.email}</p>
                    </div>
                  </div>
                </td>
                <td className="table-cell">
                  <span className="font-mono text-xs bg-surface px-2 py-1 rounded-lg text-brand-300">{p.mrn}</span>
                </td>
                <td className="table-cell">
                  <p className="text-sm">{p.dateOfBirth}</p>
                  <p className="text-xs text-slate-500">{p.gender ?? '—'}</p>
                </td>
                <td className="table-cell">
                  <p className="text-sm">{p.phone ?? '—'}</p>
                </td>
                <td className="table-cell">
                  {p.isActiveCase
                    ? <span className="badge-success">Active</span>
                    : <span className="badge-gray">Inactive</span>}
                </td>
                <td className="table-cell text-slate-500 text-xs">
                  {p.createdAt ? format(new Date(p.createdAt), 'dd MMM yyyy') : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-3 border-t border-surface-border">
            <p className="text-xs text-slate-500">Page {page + 1} of {totalPages}</p>
            <div className="flex gap-2">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="btn-secondary py-1.5 px-3 text-xs">
                <ChevronLeft size={14} />
              </button>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} className="btn-secondary py-1.5 px-3 text-xs">
                <ChevronRight size={14} />
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
