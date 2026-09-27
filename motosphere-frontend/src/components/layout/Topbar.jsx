import useAuth from '../../hooks/useAuth'
import { ROLE_LABELS } from '../../utils/roleNav'

export default function Topbar() {
  const { auth } = useAuth()
  const { profile } = auth
  const name = profile ? `${profile.firstName} ${profile.lastName || ''}`.trim() : `User #${auth.userId}`
  const sub = profile?.garageName || (auth.role === 'SUPER_ADMIN' ? 'Platform admin' : '')

  return (
    <div className="ms-topbar">
      <span className="ms-role-badge">
        <span className="ms-dot" />
        {ROLE_LABELS[auth.role] || auth.role}
      </span>
      <div className="text-end">
        <div className="fw-semibold" style={{ fontSize: '.85rem' }}>{name}</div>
        {sub && <div className="text-secondary" style={{ fontSize: '.72rem' }}>{sub}</div>}
      </div>
    </div>
  )
}
