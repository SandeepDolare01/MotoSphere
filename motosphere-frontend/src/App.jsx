import { Routes, Route, Navigate } from 'react-router-dom'

import useAuth from './hooks/useAuth'
import { HOME_BY_ROLE } from './utils/roleNav'

import AuthLayout from './pages/auth/AuthLayout'
import LoginPage from './pages/auth/LoginPage'
import RegisterCustomerPage from './pages/auth/RegisterCustomerPage'
import RegisterGaragePage from './pages/auth/RegisterGaragePage'
import SuperAdminSetupPage from './pages/auth/SuperAdminSetupPage'

import AppLayout from './components/layout/AppLayout'
import ProtectedRoute from './components/common/ProtectedRoute'

import VehiclesPage from './pages/customer/VehiclesPage'
import GaragesPage from './pages/customer/GaragesPage'
import AppointmentsPage from './pages/customer/AppointmentsPage'

import GarageQueuePage from './pages/manager/GarageQueuePage'
import MechanicsPage from './pages/manager/MechanicsPage'
import ManagerDashboardPage from './pages/manager/ManagerDashboardPage'
import GaragePhotosPage from './pages/manager/GaragePhotosPage'
import GarageDetailsPage from './pages/manager/GarageDetailsPage'

import MyJobsPage from './pages/mechanic/MyJobsPage'

import PendingGaragesPage from './pages/admin/PendingGaragesPage'
import CreateGaragePage from './pages/admin/CreateGaragePage'
import CreateStaffPage from './pages/admin/CreateStaffPage'
import AllUsersPage from './pages/admin/AllUsersPage'
import AdminDashboardPage from './pages/admin/AdminDashboardPage'

function RootRedirect() {
  const { auth, isAuthenticated } = useAuth()
  if (!isAuthenticated) return <Navigate to="/login" replace />
  return <Navigate to={HOME_BY_ROLE[auth.role] || '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<RootRedirect />} />

      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterCustomerPage />} />
        <Route path="/register-garage" element={<RegisterGaragePage />} />
        <Route path="/setup-admin" element={<SuperAdminSetupPage />} />
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['CUSTOMER']} />}>
        <Route element={<AppLayout />}>
          <Route path="/vehicles" element={<VehiclesPage />} />
          <Route path="/garages" element={<GaragesPage />} />
          <Route path="/appointments" element={<AppointmentsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['GARAGE_MANAGER']} />}>
        <Route element={<AppLayout />}>
          <Route path="/manager-dashboard" element={<ManagerDashboardPage />} />
          <Route path="/garage-queue" element={<GarageQueuePage />} />
          <Route path="/mechanics" element={<MechanicsPage />} />
          <Route path="/garage-photos" element={<GaragePhotosPage />} />
          <Route path="/garage-details" element={<GarageDetailsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['MECHANIC']} />}>
        <Route element={<AppLayout />}>
          <Route path="/my-jobs" element={<MyJobsPage />} />
        </Route>
      </Route>

      <Route element={<ProtectedRoute allowedRoles={['SUPER_ADMIN']} />}>
        <Route element={<AppLayout />}>
          <Route path="/admin-dashboard" element={<AdminDashboardPage />} />
          <Route path="/pending-garages" element={<PendingGaragesPage />} />
          <Route path="/create-garage" element={<CreateGaragePage />} />
          <Route path="/create-staff" element={<CreateStaffPage />} />
          <Route path="/all-users" element={<AllUsersPage />} />
        </Route>
      </Route>

      <Route path="*" element={<RootRedirect />} />
    </Routes>
  )
}
