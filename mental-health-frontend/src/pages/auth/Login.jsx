import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useAuthStore } from '../../store/authStore'
import { authApi } from '../../api/services'
import toast from 'react-hot-toast'
import { Brain, Eye, EyeOff, Lock, Mail, Loader2 } from 'lucide-react'

const schema = z.object({
  email: z.string().email('Please enter a valid email'),
  password: z.string().min(8, 'Password must be at least 8 characters'),
})

export default function Login() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((s) => s.setAuth)
  const [showPassword, setShowPassword] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  const { register, handleSubmit, formState: { errors } } = useForm({
    resolver: zodResolver(schema),
  })

  const onSubmit = async (data) => {
    setIsLoading(true)
    try {
      const res = await authApi.login(data)
      const { accessToken, refreshToken, user } = res.data.data
      setAuth(user, accessToken, refreshToken)
      toast.success(`Welcome back, ${user.firstName}!`)
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.error || 'Invalid email or password')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-surface flex">
      {/* Left panel — branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-brand-900 via-brand-800 to-surface flex-col justify-between p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute top-20 left-20 w-64 h-64 rounded-full bg-brand-400 blur-3xl" />
          <div className="absolute bottom-20 right-20 w-64 h-64 rounded-full bg-brand-600 blur-3xl" />
        </div>
        <div className="relative z-10 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-white/20 flex items-center justify-center">
            <Brain size={22} className="text-white" />
          </div>
          <div>
            <p className="font-bold text-white text-lg">CIMHANS</p>
            <p className="text-brand-300 text-xs">Mental Health Centre</p>
          </div>
        </div>
        <div className="relative z-10 space-y-6">
          <h1 className="text-4xl font-bold text-white leading-tight">
            Compassionate Care,<br />
            <span className="text-brand-300">Intelligent Management</span>
          </h1>
          <p className="text-slate-300 text-lg leading-relaxed max-w-md">
            A secure, HIPAA-aligned platform for mental health professionals to manage
            appointments, patient records, and clinical notes.
          </p>
          <div className="grid grid-cols-3 gap-4 pt-4">
            {[
              { label: 'Patients Served', value: '2,400+' },
              { label: 'Therapists', value: '48' },
              { label: 'Sessions/Month', value: '1,200+' },
            ].map(({ label, value }) => (
              <div key={label} className="bg-white/10 backdrop-blur rounded-2xl p-4 border border-white/10">
                <p className="text-2xl font-bold text-white">{value}</p>
                <p className="text-xs text-brand-300 mt-1">{label}</p>
              </div>
            ))}
          </div>
        </div>
        <p className="relative z-10 text-slate-500 text-xs">
          © 2024 CIMHANS. All rights reserved. Confidential & Secure.
        </p>
      </div>

      {/* Right panel — login form */}
      <div className="flex-1 flex items-center justify-center p-8">
        <div className="w-full max-w-md space-y-8 animate-fade-in">
          {/* Mobile logo */}
          <div className="lg:hidden flex items-center gap-3 mb-8">
            <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
              <Brain size={22} className="text-white" />
            </div>
            <p className="font-bold text-white text-lg">CIMHANS</p>
          </div>

          <div>
            <h2 className="text-3xl font-bold text-white">Sign in</h2>
            <p className="text-slate-400 mt-2">Access your CIMHANS account</p>
          </div>

          <form onSubmit={handleSubmit(onSubmit)} className="space-y-5">
            <div>
              <label className="label">Email Address</label>
              <div className="relative">
                <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  {...register('email')}
                  type="email"
                  placeholder="you@cimhans.com"
                  className="input pl-11"
                  autoComplete="email"
                />
              </div>
              {errors.email && <p className="text-red-400 text-xs mt-1">{errors.email.message}</p>}
            </div>

            <div>
              <label className="label">Password</label>
              <div className="relative">
                <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
                <input
                  {...register('password')}
                  type={showPassword ? 'text' : 'password'}
                  placeholder="Enter your password"
                  className="input pl-11 pr-11"
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-500 hover:text-slate-300"
                >
                  {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
              {errors.password && <p className="text-red-400 text-xs mt-1">{errors.password.message}</p>}
            </div>

            <button type="submit" className="btn-primary w-full justify-center py-3" disabled={isLoading}>
              {isLoading ? <><Loader2 size={18} className="animate-spin" /> Signing in...</> : 'Sign In'}
            </button>
          </form>

          <div className="card bg-surface/50 text-xs text-slate-500 space-y-1">
            <p className="font-semibold text-slate-400 mb-2">Demo Credentials:</p>
            <p>Admin: admin@cimhans.com / Admin@123</p>
          </div>
        </div>
      </div>
    </div>
  )
}
