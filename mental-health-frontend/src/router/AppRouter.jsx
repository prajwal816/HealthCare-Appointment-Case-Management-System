import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import DashboardLayout from '../components/layout/DashboardLayout'
import Login from '../pages/auth/Login'
import AdminDashboard from '../pages/admin/AdminDashboard'
import AnalyticsPage from '../pages/admin/AnalyticsPage'
import SettingsPage from '../pages/admin/SettingsPage'
import TherapistDashboard from '../pages/therapist/TherapistDashboard'
import SessionNotesPage from '../pages/therapist/SessionNotesPage'
import PatientDashboard from '../pages/patient/PatientDashboard'
import ReceptionistDashboard from '../pages/receptionist/ReceptionistDashboard'
import PatientList from '../pages/patients/PatientList'
import AppointmentCalendar from '../pages/appointments/AppointmentCalendar'
import NotFound from '../pages/NotFound'

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, user } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (allowedRoles && !allowedRoles.includes(user?.role)) return <Navigate to="/unauthorized" replace />
  return children
}

const RoleDashboard = () => {
  const role = useAuthStore((s) => s.user?.role)
  if (role === 'ADMIN')        return <AdminDashboard />
  if (role === 'PSYCHIATRIST' || role === 'PSYCHOLOGIST') return <TherapistDashboard />
  if (role === 'RECEPTIONIST') return <ReceptionistDashboard />
  if (role === 'PATIENT')      return <PatientDashboard />
  return <Navigate to="/login" replace />
}

export default function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<Login />} />

        {/* Protected layout wrapper */}
        <Route path="/" element={<ProtectedRoute><DashboardLayout /></ProtectedRoute>}>
          <Route index element={<RoleDashboard />} />
          <Route path="dashboard" element={<RoleDashboard />} />

          {/* Patients */}
          <Route path="patients" element={
            <ProtectedRoute allowedRoles={['ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST']}>
              <PatientList />
            </ProtectedRoute>
          }/>

          {/* Appointments */}
          <Route path="appointments" element={
            <ProtectedRoute allowedRoles={['ADMIN','RECEPTIONIST','PSYCHIATRIST','PSYCHOLOGIST','PATIENT']}>
              <AppointmentCalendar />
            </ProtectedRoute>
          }/>

          {/* Analytics (Admin only) */}
          <Route path="analytics" element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <AnalyticsPage />
            </ProtectedRoute>
          }/>

          {/* Settings (Admin only) */}
          <Route path="settings" element={
            <ProtectedRoute allowedRoles={['ADMIN']}>
              <SettingsPage />
            </ProtectedRoute>
          }/>

          {/* Session Notes (Therapists) */}
          <Route path="session-notes" element={
            <ProtectedRoute allowedRoles={['ADMIN','PSYCHIATRIST','PSYCHOLOGIST']}>
              <SessionNotesPage />
            </ProtectedRoute>
          }/>
        </Route>

        {/* 403 */}
        <Route path="/unauthorized" element={
          <div className="min-h-screen bg-surface flex items-center justify-center">
            <div className="card text-center p-12">
              <h1 className="text-4xl font-bold text-red-400 mb-2">403</h1>
              <p className="text-slate-400">You don't have permission to access this page.</p>
            </div>
          </div>
        }/>

        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </BrowserRouter>
  )
}
