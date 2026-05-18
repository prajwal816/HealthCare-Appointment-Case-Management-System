import { create } from 'zustand'
import { persist, createJSONStorage } from 'zustand/middleware'

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      accessToken: null,
      refreshToken: null,
      isAuthenticated: false,

      setAuth: (user, accessToken, refreshToken) =>
        set({ user, accessToken, refreshToken, isAuthenticated: true }),

      setTokens: (accessToken, refreshToken) =>
        set({ accessToken, refreshToken }),

      logout: () =>
        set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false }),

      // Helper getters
      getRole: () => get().user?.role ?? null,
      isAdmin: () => get().user?.role === 'ADMIN',
      isTherapist: () => ['PSYCHIATRIST', 'PSYCHOLOGIST'].includes(get().user?.role),
      isReceptionist: () => get().user?.role === 'RECEPTIONIST',
      isPatient: () => get().user?.role === 'PATIENT',
    }),
    {
      name: 'cimhans-auth',
      storage: createJSONStorage(() => localStorage),
      partialize: (state) => ({
        user: state.user,
        accessToken: state.accessToken,
        refreshToken: state.refreshToken,
        isAuthenticated: state.isAuthenticated,
      }),
    }
  )
)
