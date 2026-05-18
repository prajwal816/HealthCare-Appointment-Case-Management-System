import api from './axiosInstance'

// ===== AUTH =====
export const authApi = {
  login:       (data)        => api.post('/auth/login', data),
  register:    (data)        => api.post('/auth/register', data),
  googleLogin: (credential)  => api.post('/auth/google', { credential }),
  refresh:     (refreshToken)=> api.post('/auth/refresh', { refreshToken }),
  logout:      ()            => api.post('/auth/logout'),
}

// ===== PATIENTS =====
export const patientApi = {
  create: (data) => api.post('/patients', data),
  getById: (id) => api.get(`/patients/${id}`),
  getByMrn: (mrn) => api.get(`/patients/mrn/${mrn}`),
  getAll: (params) => api.get('/patients', { params }),
  search: (q, params) => api.get('/patients/search', { params: { q, ...params } }),
  delete: (id) => api.delete(`/patients/${id}`),
}

// ===== APPOINTMENTS =====
export const appointmentApi = {
  book: (data) => api.post('/appointments', data),
  confirm: (id) => api.patch(`/appointments/${id}/confirm`),
  complete: (id) => api.patch(`/appointments/${id}/complete`),
  cancel: (id, reason) => api.patch(`/appointments/${id}/cancel`, { reason }),
  getByPatient: (patientId, params) => api.get(`/appointments/patient/${patientId}`, { params }),
  getByTherapist: (therapistId, params) => api.get(`/appointments/therapist/${therapistId}`, { params }),
  getDailySchedule: (therapistId, date) => api.get(`/appointments/therapist/${therapistId}/schedule`, { params: { date } }),
}

// ===== SESSION NOTES =====
export const sessionNoteApi = {
  create: (appointmentId, data) => api.post(`/session-notes/appointment/${appointmentId}`, data),
  getByAppointment: (appointmentId) => api.get(`/session-notes/appointment/${appointmentId}`),
  getByPatient: (patientId, params) => api.get(`/session-notes/patient/${patientId}`, { params }),
  finalize: (noteId) => api.patch(`/session-notes/${noteId}/finalize`),
}

// ===== ADMIN =====
export const adminApi = {
  getDashboardStats: () => api.get('/admin/dashboard/stats'),
}

// ===== THERAPISTS =====
export const therapistApi = {
  getAvailable: () => api.get('/therapists/available'),
  getSlots: (therapistId, date) => api.get(`/therapists/${therapistId}/slots`, { params: { date } }),
}

// ===== NOTIFICATIONS =====
export const notificationApi = {
  getAll: (params) => api.get('/notifications', { params }),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAllRead: () => api.post('/notifications/mark-all-read'),
}
