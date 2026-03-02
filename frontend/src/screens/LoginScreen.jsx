import { useState, useEffect } from 'react'
import { loginApi } from '../api'
import { AppBackground, PageCard, Logo, Field, PrimaryButton, GoogleButton, Alert, Divider } from '../components'
import { S } from '../shared-styles'

export default function LoginScreen({ onSuccess, onNavigate }) {
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading]   = useState(false)
  const [error, setError]       = useState('')
  const [success, setSuccess]   = useState('')
  const [visible, setVisible]   = useState(false)

  useEffect(() => {
    setTimeout(() => setVisible(true), 30)
    const handler = (e) => { if (e.key === 'Enter') handleLogin() }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [email, password])

  async function handleLogin() {
    setError(''); setSuccess('')
    if (!email || !password) { setError('Please fill in all fields.'); return }
    setLoading(true)
    try {
      const data = await loginApi(email, password)
      setSuccess('Welcome back!')
      setTimeout(() => onSuccess(data), 900)
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <AppBackground />
      <PageCard visible={visible}>
        <Logo />

        <div style={S.heading}>Welcome Back</div>
        <div style={S.subheading}>Sign in to your account</div>

        <Alert message={error} type="error" />
        <Alert message={success} type="success" />

        <Field
          label="Email Address" id="login-email" type="email"
          placeholder="you@example.com"
          value={email} onChange={setEmail}
          autoComplete="email" inputMode="email"
        />
        <Field
          label="Password" id="login-password" type="password"
          placeholder="••••••••"
          value={password} onChange={setPassword}
          autoComplete="current-password"
        />

        <div style={S.forgotWrap}>
          <span style={S.forgotLink} onClick={() => onNavigate('forgot')}>Forgot password?</span>
        </div>

        <PrimaryButton onClick={handleLogin} loading={loading}>
          Sign In
        </PrimaryButton>

        <Divider />

        <GoogleButton onClick={() => { window.location.href = '/oauth2/authorization/google' }} />

        <div style={S.linkRow}>
          Don't have an account?{' '}
          <span style={S.link} onClick={() => onNavigate('register')}>Sign up</span>
        </div>
      </PageCard>
    </>
  )
}
