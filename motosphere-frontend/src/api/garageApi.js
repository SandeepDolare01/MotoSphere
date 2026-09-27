import axiosClient from './axiosClient'

export const getApprovedGarages = () => axiosClient.get('/garages').then((r) => r.data)
export const getGarageById = (garageId) => axiosClient.get(`/garages/${garageId}`).then((r) => r.data)
export const createGarage = (payload) => axiosClient.post('/garages', payload).then((r) => r.data)
export const updateGarage = (garageId, payload) => axiosClient.put(`/garages/${garageId}`, payload).then((r) => r.data)
export const getPendingGarages = () => axiosClient.get('/garages/pending').then((r) => r.data)
export const approveGarage = (garageId, payload) =>
  axiosClient.patch(`/garages/${garageId}/approve`, payload).then((r) => r.data)
export const rejectGarage = (garageId, payload) =>
  axiosClient.patch(`/garages/${garageId}/reject`, payload).then((r) => r.data)

// garage manager's own mechanic management
export const getMyGarageMechanics = () => axiosClient.get('/garages/my/mechanics').then((r) => r.data)
export const addMechanic = (payload) => axiosClient.post('/garages/my/mechanics', payload).then((r) => r.data)
export const deactivateMechanic = (userId) =>
  axiosClient.patch(`/garages/my/mechanics/${userId}/deactivate`).then((r) => r.data)
export const reactivateMechanic = (userId) =>
  axiosClient.patch(`/garages/my/mechanics/${userId}/reactivate`).then((r) => r.data)

// garage manager's own garage photo (single image, replaces any existing one)
export const uploadMyGarageImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return axiosClient
    .post('/garages/my/image', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    .then((r) => r.data)
}
export const deleteMyGarageImage = () => axiosClient.delete('/garages/my/image').then((r) => r.data)

// Image URLs returned by the API (e.g. /garages/12/image) are relative -
// they're plain <img> src values (public GET endpoint, no auth needed), so
// they just need the API's base URL prefixed once.
export const garageImageUrl = (relativeUrl) => `${import.meta.env.VITE_API_BASE_URL}${relativeUrl}`
