import { useEffect, useRef } from 'react'
import capricornLogo from './assets/logo.png'
import { S } from './shared-styles'

/* ─── Animated particle background ─── */
export function AppBackground() {
  const canvasRef = useRef(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    let animId
    const resize = () => { canvas.width = window.innerWidth; canvas.height = window.innerHeight }
    resize()
    window.addEventListener('resize', resize)

    const particles = Array.from({ length: 55 }, () => ({
      x: Math.random() * window.innerWidth,
      y: Math.random() * window.innerHeight,
      r: Math.random() * 1.4 + 0.3,
      dx: (Math.random() - 0.5) * 0.28,
      dy: (Math.random() - 0.5) * 0.28,
      o: Math.random() * 0.45 + 0.08,
    }))

    const draw = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)
      particles.forEach(p => {
        ctx.beginPath()
        ctx.arc(p.x, p.y, p.r, 0, Math.PI * 2)
        ctx.fillStyle = `rgba(201,168,76,${p.o})`
        ctx.fill()
        p.x += p.dx; p.y += p.dy
        if (p.x < 0 || p.x > canvas.width) p.dx *= -1
        if (p.y < 0 || p.y > canvas.height) p.dy *= -1
      })
      animId = requestAnimationFrame(draw)
    }
    draw()
    return () => { cancelAnimationFrame(animId); window.removeEventListener('resize', resize) }
  }, [])

  return (
    <>
      <div style={{ position: 'fixed', inset: 0, background: '#02060f', zIndex: 0 }} />
      <div style={{
        position: 'fixed', inset: 0, zIndex: 0, pointerEvents: 'none',
        background: `
          radial-gradient(ellipse 60% 50% at 20% 80%, rgba(201,168,76,0.06) 0%, transparent 60%),
          radial-gradient(ellipse 50% 60% at 80% 20%, rgba(120,80,20,0.07) 0%, transparent 60%)
        `
      }} />
      <canvas ref={canvasRef} style={{ position: 'fixed', inset: 0, zIndex: 0, pointerEvents: 'none' }} />
    </>
  )
}

/* ─── Card shell with gold top/bottom lines ─── */
export function PageCard({ children, visible }) {
  return (
    <div style={S.page}>
      <div style={{
        width: '100%', maxWidth: '440px',
        opacity: visible ? 1 : 0,
        transform: visible ? 'translateY(0) scale(1)' : 'translateY(40px) scale(0.96)',
        transition: 'opacity 0.8s cubic-bezier(0.16,1,0.3,1), transform 0.8s cubic-bezier(0.16,1,0.3,1)',
      }}>
        <div style={S.topLine} />
        <div style={S.card}>{children}</div>
        <div style={S.bottomLine} />
      </div>
    </div>
  )
}

/* ─── Logo ─── */
export function Logo() {
  return (
    <div style={S.logoWrap}>
      <img src={capricornLogo} alt="Capricorn" style={S.logoImg} />
      <div style={S.logoName}>Capricorn Adventures</div>
      <div style={S.logoSub}>Premium Experiences</div>
    </div>
  )
}

/* ─── Input field ─── */
import { useState } from 'react'

export function Field({ label, id, type = 'text', placeholder, value, onChange, autoComplete, inputMode }) {
  const [focused, setFocused] = useState(false)
  const [showPw, setShowPw] = useState(false)
  const isPassword = type === 'password'

  return (
    <div style={S.field}>
      <label htmlFor={id} style={S.label}>{label}</label>
      <div style={{ position: 'relative' }}>
        <input
          id={id}
          type={isPassword ? (showPw ? 'text' : 'password') : type}
          placeholder={placeholder}
          value={value}
          onChange={e => onChange(e.target.value)}
          autoComplete={autoComplete}
          inputMode={inputMode}
          onFocus={() => setFocused(true)}
          onBlur={() => setFocused(false)}
          style={{
            ...S.input,
            borderColor: focused ? 'rgba(201,168,76,0.5)' : 'rgba(255,255,255,0.07)',
            background: focused ? 'rgba(201,168,76,0.04)' : 'rgba(255,255,255,0.03)',
            boxShadow: focused ? '0 0 0 3px rgba(201,168,76,0.08)' : 'none',
            paddingRight: isPassword ? '44px' : '14px',
          }}
        />
        {isPassword && (
          <button
            type="button"
            tabIndex={-1}
            onClick={() => setShowPw(v => !v)}
            style={{ position: 'absolute', right: '13px', top: '50%', transform: 'translateY(-50%)', background: 'none', border: 'none', cursor: 'pointer', color: showPw ? '#c9a84c' : 'rgba(255,255,255,0.28)', padding: '4px', display: 'flex', alignItems: 'center', transition: 'color 0.2s' }}
          >
            {showPw ? (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94"/>
                <path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19"/>
                <line x1="1" y1="1" x2="23" y2="23"/>
              </svg>
            ) : (
              <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle cx="12" cy="12" r="3"/>
              </svg>
            )}
          </button>
        )}
      </div>
    </div>
  )
}

/* ─── Buttons ─── */
export function PrimaryButton({ onClick, disabled, loading, children }) {
  return (
    <button onClick={onClick} disabled={disabled || loading} style={{ ...S.btnPrimary, opacity: (disabled || loading) ? 0.65 : 1 }}>
      {loading ? <Spinner dark /> : children}
    </button>
  )
}

export function OutlineButton({ onClick, children }) {
  return <button onClick={onClick} style={S.btnOutline}>{children}</button>
}

export function GoogleButton({ onClick }) {
  return (
    <button onClick={onClick} style={S.btnGoogle}>
      <GoogleIcon /> Continue with Google
    </button>
  )
}

/* ─── Alerts ─── */
export function Alert({ message, type }) {
  if (!message) return null
  return <div style={type === 'error' ? S.alertError : S.alertSuccess}>{message}</div>
}

/* ─── Divider ─── */
export function Divider() {
  return (
    <div style={S.divider}>
      <div style={S.dividerLine} />
      <span style={S.dividerText}>or</span>
      <div style={S.dividerLine} />
    </div>
  )
}

/* ─── Spinner ─── */
function Spinner({ dark }) {
  return (
    <>
      <style>{`@keyframes _spin { to { transform: rotate(360deg); } } ._spinner { width:18px;height:18px;border-radius:50%;animation:_spin 0.7s linear infinite; }`}</style>
      <div className="_spinner" style={{ border: `2px solid ${dark ? 'rgba(5,13,26,0.25)' : 'rgba(255,255,255,0.15)'}`, borderTopColor: dark ? '#050d1a' : '#c9a84c' }} />
    </>
  )
}

/* ─── Google Icon ─── */
function GoogleIcon() {
  return (
    <svg width="17" height="17" viewBox="0 0 48 48">
      <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
      <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
      <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.27-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24c0 3.88.92 7.54 2.56 10.78l7.97-6.19z"/>
      <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.18 1.48-4.97 2.31-8.16 2.31-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
    </svg>
  )
}
