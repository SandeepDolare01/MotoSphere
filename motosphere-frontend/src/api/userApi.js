import axiosClient from './axiosClient'

export const getMyProfile = (userId) => axiosClient.get(`/users/${userId}`).then((r) => r.data)
export const getAllUsers = () => axiosClient.get('/users').then((r) => r.data)
export const createStaff = (payload) => axiosClient.post('/users/staff', payload).then((r) => r.data)
export const deactivateUser = (userId) => axiosClient.patch(`/users/${userId}/deactivate`).then((r) => r.data)
export const reactivateUser = (userId) => axiosClient.patch(`/users/${userId}/reactivate`).then((r) => r.data)
