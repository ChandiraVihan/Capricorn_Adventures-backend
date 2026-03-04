import { useState, useEffect } from 'react'
import { getMeApi, refreshTokenApi } from './api'
import LoginScreen         from './screens/LoginScreen'
import RegisterScreen      from './screens/RegisterScreen'
import ForgotScreen        from './screens/ForgotScreen'
import ResetPasswordScreen from './screens/ResetPasswordScreen'
import ProfileScreen       from './screens/ProfileScreen'

export default function App() {
  const [screen, setScreen]             = useState('login')
  const [resetToken, setResetToken]     = useState(null)
  const [accessToken, setAccessToken]   = useState(() => localStorage.getItem('accessToken'))
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem('refreshToken'))
  const [user, setUser]                 = useState(null)

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)

    // ── OAuth2 redirect with tokens in URL ──────────────────────
    if (params.get('accessToken')) {
      const at = params.get('accessToken')
      const rt = params.get('refreshToken')
      saveTokens(at, rt)
      window.history.replaceState({}, '', window.location.pathname)
      loadProfile(at, rt)
      return
    }

    // ── Password-reset link: ?token=xxxx ───────────────────────
    // Vite SPA always loads at '/', so just check for the token param directly
    const token = params.get('token')
    if (token) {
      setResetToken(token)
      setScreen('reset-password')
      // Remove token from URL bar so it can't be bookmarked or accidentally reused
      window.history.replaceState({}, '', '/')
      return
    }

    // ── Already logged in ────────────────────────────────────────
    if (accessToken) loadProfile(accessToken, refreshToken)
  }, [])

  function saveTokens(at, rt) {
    setAccessToken(at)
    setRefreshToken(rt)
    localStorage.setItem('accessToken', at)
    if (rt) localStorage.setItem('refreshToken', rt)
  }

  async function loadProfile(at, rt) {
    try {
      const userData = await getMeApi(at)
      setUser(userData)
      setScreen('profile')
    } catch {
      if (rt) {
        try {
          const data = await refreshTokenApi(rt)
          saveTokens(data.accessToken, rt)
          const userData = await getMeApi(data.accessToken)
          setUser(userData)
          setScreen('profile')
        } catch { logout() }
      } else { logout() }
    }
  }

  function handleAuthSuccess(data) {
    saveTokens(data.accessToken, data.refreshToken)
    loadProfile(data.accessToken, data.refreshToken)
  }

  function logout() {
    setAccessToken(null)
    setRefreshToken(null)
    setUser(null)
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    setScreen('login')
  }

  return (
    <>
      {screen === 'login'          && <LoginScreen         onSuccess={handleAuthSuccess} onNavigate={setScreen} />}
      {screen === 'register'       && <RegisterScreen      onSuccess={handleAuthSuccess} onNavigate={setScreen} />}
      {screen === 'forgot'         && <ForgotScreen        onNavigate={setScreen} />}
      {screen === 'reset-password' && <ResetPasswordScreen token={resetToken}           onNavigate={setScreen} />}
      {screen === 'profile'        && <ProfileScreen       user={user} accessToken={accessToken} onLogout={logout} />}
    </>
  )
}
