import axiosClient from './axiosClient'

export const bookAppointment = (payload) => axiosClient.post('/appointments', payload).then((r) => r.data)
export const getAvailableSlots = (garageId, date) =>
  axiosClient.get('/appointments/available-slots', { params: { garageId, date } }).then((r) => r.data)
export const getMyAppointments = () => axiosClient.get('/appointments/my').then((r) => r.data)
export const getGarageAppointments = () => axiosClient.get('/appointments/garage').then((r) => r.data)
export const getMechanicAppointments = () => axiosClient.get('/appointments/mechanic').then((r) => r.data)
export const assignMechanic = (appointmentId, payload) =>
  axiosClient.patch(`/appointments/${appointmentId}/assign-mechanic`, payload).then((r) => r.data)
export const cancelAppointment = (appointmentId) =>
  axiosClient.patch(`/appointments/${appointmentId}/cancel`).then((r) => r.data)
