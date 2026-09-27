import { NavLink, Outlet } from 'react-router-dom'
import { Card } from 'react-bootstrap'

const TABS = [
  { to: '/login', label: 'Sign in' },
  { to: '/register', label: 'Join as customer' },
  { to: '/register-garage', label: 'Register a garage' },
]

export default function AuthLayout() {
  return (
    <div className="ms-auth-wrap">
      <Card className="ms-auth-card shadow-sm">
        <div className="ms-auth-head">
          <div className="d-flex align-items-center gap-2 mb-1">
            <span className="ms-dot" />
            <h1 className="h4 mb-0 text-white">MotoSphere</h1>
          </div>
          <p>
            Garage service management — book service, track your job card, run your garage's
            queue, or approve applications, all from one console.
          </p>
        </div>
        <Card.Header className="d-flex p-0 bg-white">
          {TABS.map((tab) => (
            <NavLink
              key={tab.to}
              to={tab.to}
              className={({ isActive }) =>
                'flex-fill text-center py-2 px-1 small fw-semibold text-decoration-none border-bottom border-2 ' +
                (isActive ? 'border-warning text-dark' : 'border-transparent text-secondary')
              }
              style={{ fontSize: '0.78rem' }}
            >
              {tab.label}
            </NavLink>
          ))}
        </Card.Header>
        <Card.Body className="p-4">
          <Outlet />
        </Card.Body>
      </Card>
    </div>
  )
}
