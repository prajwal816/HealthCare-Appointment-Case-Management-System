import { Link } from 'react-router-dom'

export default function NotFound() {
  return (
    <div className="min-h-screen bg-surface flex items-center justify-center">
      <div className="text-center space-y-6 animate-fade-in">
        <div className="relative">
          <p className="text-[10rem] font-black text-surface-card leading-none select-none">404</p>
          <p className="absolute inset-0 text-[10rem] font-black text-brand-600/20 leading-none blur-sm select-none">404</p>
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">Page Not Found</h1>
          <p className="text-slate-400 mt-2">The page you're looking for doesn't exist or has been moved.</p>
        </div>
        <Link to="/" className="btn-primary inline-flex">← Back to Dashboard</Link>
      </div>
    </div>
  )
}
