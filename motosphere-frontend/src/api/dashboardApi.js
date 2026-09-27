import axiosClient from './axiosClient'

export const getAdminDashboard = () => axiosClient.get('/dashboard/admin').then((r) => r.data)

export const getManagerDashboard = () => axiosClient.get('/dashboard/manager').then((r) => r.data)
