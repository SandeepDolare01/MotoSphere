import axios from 'axios'

const axiosClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

// Attach the JWT (if we have one) to every outgoing request.
axiosClient.interceptors.request.use((config) => {
  const raw = localStorage.getItem('ms_auth')
  if (raw) {
    const { token } = JSON.parse(raw)
    if (token) config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Normalize error messages so every screen can just do err.message.
// The backend's GlobalExceptionHandler returns either:
//   - { message, status, timeStamp } for most errors
//   - a plain { field: "error message", ... } map for bean-validation failures
axiosClient.interceptors.response.use(
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
      // token missing/expired/invalid - force back to login
      localStorage.removeItem('ms_auth')
    }
    return Promise.reject(new Error(message))
  }
)

export default axiosClient
