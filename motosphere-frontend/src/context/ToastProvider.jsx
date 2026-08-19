import { useCallback, useState } from 'react'
import { ToastContainer, Toast } from 'react-bootstrap'
import { ToastContext } from './toastContext'

let nextId = 1

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const push = useCallback(
    (message, variant = 'dark') => {
      const id = nextId++
      setToasts((prev) => [...prev, { id, message, variant }])
      setTimeout(() => dismiss(id), 4200)
    },
    [dismiss]
  )

  const value = {
    success: (msg) => push(msg, 'success'),
    error: (msg) => push(msg, 'danger'),
    info: (msg) => push(msg, 'dark'),
  }

  return (
    <ToastContext.Provider value={value}>
      {children}
      <ToastContainer position="top-end" className="p-3" style={{ zIndex: 2000 }}>
        {toasts.map((t) => (
          <Toast key={t.id} bg={t.variant} onClose={() => dismiss(t.id)}>
            <Toast.Body className={t.variant === 'success' || t.variant === 'danger' || t.variant === 'dark' ? 'text-white' : ''}>
              {t.message}
            </Toast.Body>
          </Toast>
        ))}
      </ToastContainer>
    </ToastContext.Provider>
  )
}
