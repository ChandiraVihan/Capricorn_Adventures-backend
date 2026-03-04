import { useState, useEffect } from 'react'
import { AppBackground, PageCard, Logo, Field, PrimaryButton, OutlineButton, Alert } from '../components'
import { S } from '../shared-styles'

const API = '/api/auth'

export default function ForgotScreen({ onNavigate }) {
  const [email, setEmail]     = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError]     = useState('')
  const [success, setSuccess] = useState('')
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    setTimeout(() => setVisible(true), 30)
    const handler = (e) => { if (e.key === 'Enter') handleForgot() }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [email])

  async function handleForgot() {
    setError(''); setSuccess('')
    if (!email) { setError('Please enter your email.'); return }

    setLoading(true)
    try {
      const res = await fetch(`${API}/forgot-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      })

      const data = await res.json()

      if (!res.ok) {
        setError(data.error || 'Failed to send reset email. Please try again.')
      } else {
        setSuccess('Reset link sent! Please check your inbox (and spam folder).')
      }
    } catch (e) {
      setError('Could not connect to server. Make sure the backend is running.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <AppBackground />
      <PageCard visible={visible}>
        <Logo />

        <div style={S.heading}>Reset Password</div>
        <div style={S.subheading}>Enter your email to receive a reset link</div>

        <Alert message={error} type="error" />
        <Alert message={success} type="success" />

        <Field
          label="Email Address" id="forgot-email" type="email"
          placeholder="you@example.com"
          value={email} onChange={setEmail}
          autoComplete="email" inputMode="email"
        />

        <PrimaryButton onClick={handleForgot} loading={loading}>
          Send Reset Link
        </PrimaryButton>

        <OutlineButton onClick={() => onNavigate('login')}>
          ← Back to Login
        </OutlineButton>
      </PageCard>
    </>
  )
}
