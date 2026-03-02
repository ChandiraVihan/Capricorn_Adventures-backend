const API = '/api/auth'

export async function loginApi(email, password) {
  const res = await fetch(`${API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password })
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || 'Login failed')
  return data
}

export async function registerApi(firstName, lastName, email, password) {
  const res = await fetch(`${API}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ firstName, lastName, email, password })
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data.error || 'Registration failed')
  return data
}

export async function forgotPasswordApi(email) {
  const res = await fetch(`${API}/forgot-password`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email })
  })
  return res
}

export async function getMeApi(token) {
  const res = await fetch(`${API}/me`, {
    headers: { 'Authorization': 'Bearer ' + token }
  })
  if (!res.ok) throw new Error('Unauthorized')
  return res.json()
}

export async function refreshTokenApi(refreshToken) {
  const res = await fetch(`${API}/refresh`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ refreshToken })
  })
  if (!res.ok) throw new Error('Refresh failed')
  return res.json()
}
