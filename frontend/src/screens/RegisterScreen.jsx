import { useState, useEffect } from 'react'
import { registerApi } from '../api'
import { AppBackground, PageCard, Logo, Field, PrimaryButton, Alert } from '../components'
import { S } from '../shared-styles'

export default function RegisterScreen({ onSuccess, onNavigate }) {
  const [firstName, setFirstName]           = useState('')
  const [lastName, setLastName]             = useState('')
  const [email, setEmail]                   = useState('')
  const [phone, setPhone]                   = useState('')
  const [password, setPassword]             = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [loading, setLoading]               = useState(false)
  const [error, setError]                   = useState('')
  const [success, setSuccess]               = useState('')
  const [visible, setVisible]               = useState(false)

  useEffect(() => {
    setTimeout(() => setVisible(true), 30)
    const handler = (e) => { if (e.key === 'Enter') handleRegister() }
    window.addEventListener('keydown', handler)
    return () => window.removeEventListener('keydown', handler)
  }, [firstName, lastName, email, phone, password, confirmPassword])

  async function handleRegister() {
    setError(''); setSuccess('')
    if (!firstName || !lastName || !email || !phone || !password || !confirmPassword) {
      setError('Please fill in all fields.'); return
    }
    if (password.length < 8) {
      setError('Password must be at least 8 characters.'); return
    }
    if (password !== confirmPassword) {
      setError('Passwords do not match.'); return
    }
    setLoading(true)
    try {
      const data = await registerApi(firstName, lastName, email, password)
      setSuccess('Account created successfully!')
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

        <div style={S.heading}>Create Account</div>
        <div style={S.subheading}>Welcome! Please enter your details</div>

        <Alert message={error} type="error" />
        <Alert message={success} type="success" />

        <div style={S.row}>
          <Field
            label="First Name" id="reg-first" type="text"
            placeholder="John"
            value={firstName} onChange={setFirstName}
            autoComplete="given-name"
          />
          <Field
            label="Last Name" id="reg-last" type="text"
            placeholder="Doe"
            value={lastName} onChange={setLastName}
            autoComplete="family-name"
          />
        </div>

        <Field
          label="Email" id="reg-email" type="email"
          placeholder="you@example.com"
          value={email} onChange={setEmail}
          autoComplete="email" inputMode="email"
        />

        <Field
          label="Phone" id="reg-phone" type="tel"
          placeholder="+94 77 123 4567"
          value={phone} onChange={setPhone}
          autoComplete="tel" inputMode="tel"
        />

        <Field
          label="Password" id="reg-password" type="password"
          placeholder="Min. 8 characters"
          value={password} onChange={setPassword}
          autoComplete="new-password"
        />

        <Field
          label="Confirm Password" id="reg-confirm" type="password"
          placeholder="Re-enter your password"
          value={confirmPassword} onChange={setConfirmPassword}
          autoComplete="new-password"
        />

        <PrimaryButton onClick={handleRegister} loading={loading}>
          Continue
        </PrimaryButton>

        <div style={S.linkRow}>
          Already have an account?{' '}
          <span style={S.link} onClick={() => onNavigate('login')}>Sign in</span>
        </div>
      </PageCard>
    </>
  )
}
