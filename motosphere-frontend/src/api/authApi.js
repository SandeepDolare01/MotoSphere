import axiosClient from './axiosClient'

export const login = (payload) => axiosClient.post('/auth/login', payload).then((r) => r.data)
export const registerCustomer = (payload) => axiosClient.post('/auth/register', payload).then((r) => r.data)
export const registerGarageManager = (payload) =>
  axiosClient.post('/auth/register-garage-manager', payload).then((r) => r.data)
export const registerSuperAdmin = (payload) =>
  axiosClient.post('/auth/register-super-admin', payload).then((r) => r.data)
