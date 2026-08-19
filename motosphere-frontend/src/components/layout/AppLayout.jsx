import { Outlet } from 'react-router-dom'
import Sidebar from './Sidebar'
import Topbar from './Topbar'

export default function AppLayout() {
  return (
    <div className="ms-shell">
      <Sidebar />
      <div className="ms-main">
        <Topbar />
        <div className="ms-content">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
