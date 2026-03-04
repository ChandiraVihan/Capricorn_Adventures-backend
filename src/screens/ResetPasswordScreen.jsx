import { useState, useEffect } from 'react'
import { AppBackground, PageCard, Logo, Field, PrimaryButton, OutlineButton, Alert } from '../components'
import { S } from '../shared-styles'

const API = '/api/auth'

export default function ResetPasswordScreen({ token, onNavigate }) {
  const [newPassword, setNewPassword]     = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading]             = useState(false)
  const [error, setError]                 = useState('')
  const [success, setSuccess]             = useState('')
  const [visible, setVisible]             = useState(false)

  useEffect(() => {
    setTimeout(() => setVisible(true), 30)
  }, [])

  // If no token in URL, show an error straight away
  useEffect(() => {
    if (!token) setError('Invalid or missing reset link. Please request a new one.')
  }, [token])

  async function handleReset() {
    setError(''); setSuccess('')

    if (!newPassword || !confirmPassword) {
      setError('Please fill in both fields.')
      return
    }
    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }
    if (newPassword !== confirmPassword) {
      setError('Passwords do not match.')
      return
    }

    setLoading(true)
    try {
      const res = await fetch(`${API}/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, newPassword })
      })

      const data = await res.json()

      if (!res.ok) {
        setError(data.error || 'Failed to reset password. The link may have expired.')
      } else {
        setSuccess('Password changed successfully! Redirecting to login…')
        setTimeout(() => onNavigate('login'), 2200)
      }
    } catch {
      setError('Could not connect to server. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <AppBackground />
      <PageCard visible={visible}>
        <Logo />

        <div style={S.heading}>Create New Password</div>
        <div style={S.subheading}>Choose a strong password for your account</div>

        <Alert message={error}   type="error"   />
        <Alert message={success} type="success" />

        <Field
          label="New Password" id="reset-new-password" type="password"
          placeholder="At least 8 characters"
          value={newPassword} onChange={setNewPassword}
          autoComplete="new-password"
        />
        <Field
          label="Confirm Password" id="reset-confirm-password" type="password"
          placeholder="Repeat your new password"
          value={confirmPassword} onChange={setConfirmPassword}
          autoComplete="new-password"
        />

        {/* Password strength hint */}
        {newPassword.length > 0 && newPassword.length < 8 && (
          <div style={{ fontSize: '12px', color: '#fc8181', marginTop: '-8px', marginBottom: '10px' }}>
            Password is too short
          </div>
        )}
        {newPassword.length >= 8 && confirmPassword.length > 0 && newPassword !== confirmPassword && (
          <div style={{ fontSize: '12px', color: '#fc8181', marginTop: '-8px', marginBottom: '10px' }}>
            Passwords do not match
          </div>
        )}
        {newPassword.length >= 8 && confirmPassword.length > 0 && newPassword === confirmPassword && (
          <div style={{ fontSize: '12px', color: '#68d391', marginTop: '-8px', marginBottom: '10px' }}>
            ✓ Passwords match
          </div>
        )}

        <PrimaryButton
          onClick={handleReset}
          loading={loading}
          disabled={!token || !!success}
        >
          Set New Password
        </PrimaryButton>

        <OutlineButton onClick={() => onNavigate('login')}>
          ← Back to Login
        </OutlineButton>
      </PageCard>
    </>
  )
}
