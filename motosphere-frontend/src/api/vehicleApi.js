import axiosClient from './axiosClient'

export const getMyVehicles = () => axiosClient.get('/vehicles/my').then((r) => r.data)
export const addVehicle = (payload) => axiosClient.post('/vehicles', payload).then((r) => r.data)
