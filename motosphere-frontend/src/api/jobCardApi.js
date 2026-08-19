import axiosClient from './axiosClient'

export const createJobCard = (appointmentId, payload) =>
  axiosClient.post(`/jobcards/appointment/${appointmentId}`, payload).then((r) => r.data)

export const getJobCardByAppointmentId = (appointmentId) =>
  axiosClient.get(`/jobcards/appointment/${appointmentId}`).then((r) => r.data)

export const getJobCard = (jobCardId) => axiosClient.get(`/jobcards/${jobCardId}`).then((r) => r.data)

export const addJobCardItem = (jobCardId, payload) =>
  axiosClient.post(`/jobcards/${jobCardId}/items`, payload).then((r) => r.data)

export const completeJobCard = (jobCardId) => axiosClient.patch(`/jobcards/${jobCardId}/complete`).then((r) => r.data)
