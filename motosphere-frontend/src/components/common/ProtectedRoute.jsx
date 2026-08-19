import { Navigate, Outlet } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import LoadingBlock from './LoadingBlock'

// Guards a route subtree behind login, and optionally behind a set of
// allowed roles. Unknown/unauthorized access redirects rather than
// rendering a blank/broken page.
export default function ProtectedRoute({ allowedRoles }) {
  const { auth, initializing, isAuthenticated } = useAuth()

  if (initializing) return <LoadingBlock label="Checking your session…" />
  if (!isAuthenticated) return <Navigate to="/login" replace />
  if (allowedRoles && !allowedRoles.includes(auth.role)) return <Navigate to="/" replace />

  return <Outlet />
}
