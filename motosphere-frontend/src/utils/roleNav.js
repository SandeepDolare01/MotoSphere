// Central place defining what each role sees in the sidebar, and the
// role-friendly label used in the topbar badge. Keeping this in one file
// means adding a new role or page is a one-line change here plus a route.

export const ROLE_LABELS = {
  SUPER_ADMIN: 'Super Admin',
  GARAGE_MANAGER: 'Garage Manager',
  MECHANIC: 'Mechanic',
  CUSTOMER: 'Customer',
}

export const NAV_BY_ROLE = {
  CUSTOMER: [
    { to: '/vehicles', label: 'My vehicles' },
    { to: '/garages', label: 'Browse garages' },
    { to: '/appointments', label: 'My appointments' },
  ],
  GARAGE_MANAGER: [
    { to: '/manager-dashboard', label: 'Dashboard' },
    { to: '/garage-queue', label: 'Garage queue' },
    { to: '/mechanics', label: 'My mechanics' },
    { to: '/garage-photos', label: 'Garage photos' },
    { to: '/garage-details', label: 'Garage details' },
  ],
  MECHANIC: [{ to: '/my-jobs', label: 'My appointments' }],
  SUPER_ADMIN: [
    { to: '/admin-dashboard', label: 'Dashboard' },
    { to: '/pending-garages', label: 'Garage applications' },
    { to: '/create-garage', label: 'Add a garage' },
    { to: '/create-staff', label: 'Add staff' },
    { to: '/all-users', label: 'All users' },
  ],
}

export const HOME_BY_ROLE = {
  CUSTOMER: '/vehicles',
  GARAGE_MANAGER: '/manager-dashboard',
  MECHANIC: '/my-jobs',
  SUPER_ADMIN: '/admin-dashboard',
}
