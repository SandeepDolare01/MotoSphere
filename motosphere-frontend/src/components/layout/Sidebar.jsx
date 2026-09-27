import { NavLink } from 'react-router-dom'
import useAuth from '../../hooks/useAuth'
import { NAV_BY_ROLE } from '../../utils/roleNav'

export default function Sidebar() {
  const { auth, logout } = useAuth()
  const items = NAV_BY_ROLE[auth.role] || []

  return (
    <aside className="ms-rail">
      <div className="ms-rail-brand">
        <span className="ms-dot" />
        <span>MotoSphere</span>
      </div>
      <div className="ms-rail-label">Console</div>
      <nav className="d-flex flex-column">
        {items.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => 'ms-rail-link' + (isActive ? ' active' : '')}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>
      <div className="ms-rail-foot">
        <button className="ms-rail-logout" onClick={logout}>
          Sign out
        </button>
      </div>
    </aside>
  )
}
