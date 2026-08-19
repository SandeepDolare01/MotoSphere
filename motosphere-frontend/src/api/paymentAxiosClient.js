import axios from 'axios'

// Payment now lives in its own microservice on a separate port/base URL -
// the frontend talks to it directly (not proxied through the core backend).
// Same JWT-attaching and error-normalizing behavior as axiosClient.js, since
// payment-service's GlobalExceptionHandler returns the identical
// { message, status, timeStamp } shape.
const paymentAxiosClient = axios.create({
  baseURL: import.meta.env.VITE_PAYMENT_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

paymentAxiosClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem('ms_auth')
  if (raw) {
    const { token } = JSON.parse(raw)
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

paymentAxiosClient.interceptors.response.use(
  (res) => res,
  (err) => {
    const data = err.response?.data
    let message = 'Something went wrong. Please try again.'
    if (data) {
      if (typeof data.message === 'string') message = data.message
      else {
        const fieldErrors = Object.values(data).filter((v) => typeof v === 'string')
        if (fieldErrors.length) message = fieldErrors.join(' · ')
      }
    }
    if (err.response?.status === 401) {
      localStorage.removeItem('ms_auth')
    }
    return Promise.reject(new Error(message))
  }
)

export default paymentAxiosClient
