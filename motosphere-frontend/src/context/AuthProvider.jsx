import { useEffect, useState, useCallback } from 'react'
import * as authApi from '../api/authApi'
import * as userApi from '../api/userApi'
import { AuthContext } from './authContext'

const STORAGE_KEY = 'ms_auth'

function readStoredAuth() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth)
  const [initializing, setInitializing] = useState(true)

  const persist = useCallback((next) => {
    setAuth(next)
    if (next) localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
    else localStorage.removeItem(STORAGE_KEY)
  }, [])

  // On boot, if we have a token but no cached profile, fetch it. If the
  // token turns out to be expired/invalid the axios interceptor already
  // clears localStorage on a 401 - we just need to fall back cleanly here.
  useEffect(() => {
    async function bootstrap() {
      const stored = readStoredAuth()
      if (stored && !stored.profile) {
        try {
          const profile = await userApi.getMyProfile(stored.userId)
          persist({ ...stored, profile })
        } catch {
          persist(null)
        }
      }
      setInitializing(false)
    }
    bootstrap()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  // Shared by both login() and any other endpoint that returns an
  // AuthResponse-shaped payload (token/userId/role) - currently just
  // register-super-admin, which logs the caller straight in on success
  // since it already hands back a valid JWT.
  const applyAuthResponse = useCallback(
    async (resp) => {
      const next = { token: resp.token, userId: resp.userId, role: resp.role }
      // Persist the token FIRST so the axios interceptor can attach it
      // as the Authorization header on the getMyProfile call below.
      persist(next)
      const profile = await userApi.getMyProfile(resp.userId)
      persist({ ...next, profile })
      return { ...next, profile }
    },
    [persist]
  )

  const login = useCallback(
    async (credentials) => {
      const resp = await authApi.login(credentials)
      return applyAuthResponse(resp)
    },
    [applyAuthResponse]
  )

  const logout = useCallback(() => persist(null), [persist])

  const value = { auth, initializing, login, logout, applyAuthResponse, isAuthenticated: !!auth }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
