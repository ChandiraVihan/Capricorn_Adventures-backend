import { useState, useEffect } from 'react'
import { AppBackground, PageCard, OutlineButton } from '../components'
import { S } from '../shared-styles'

export default function ProfileScreen({ user, accessToken, onLogout }) {
  const [visible, setVisible] = useState(false)

  useEffect(() => {
    setTimeout(() => setVisible(true), 30)
  }, [])

  const initials = [user?.firstName, user?.lastName]
    .filter(Boolean).map(n => n[0].toUpperCase()).join('') || (user?.email || '?')[0].toUpperCase()

  const displayName = [user?.firstName, user?.lastName].filter(Boolean).join(' ') || user?.email?.split('@')[0] || '—'

  return (
    <>
      <AppBackground />
      <style>{`
        .prof-avatar {
          width: 76px; height: 76px; border-radius: 50%;
          background: linear-gradient(135deg, #b8933e, #e8c97a);
          display: flex; align-items: center; justify-content: center;
          font-family: 'Cinzel', serif;
          font-size: 26px; color: #050d1a; font-weight: 700;
          margin: 0 auto 12px;
          box-shadow: 0 8px 28px rgba(201,168,76,0.35);
        }
        .info-grid {
          background: rgba(255,255,255,0.02);
          border-radius: 12px;
          border: 1px solid rgba(255,255,255,0.06);
          overflow: hidden; margin-bottom: 16px;
        }
        .info-row {
          display: flex; justify-content: space-between; align-items: center;
          padding: 12px 16px; border-bottom: 1px solid rgba(255,255,255,0.04);
          font-size: 13px;
        }
        .info-row:last-child { border-bottom: none; }
        .badge {
          display: inline-flex; align-items: center; gap: 5px;
          padding: 4px 10px; border-radius: 20px; font-size: 11px; font-weight: 500;
        }
        .badge-ok  { background: rgba(39,174,96,0.12); color: #68d391; border: 1px solid rgba(39,174,96,0.2); }
        .badge-warn { background: rgba(201,168,76,0.12); color: #c9a84c; border: 1px solid rgba(201,168,76,0.2); }
        .token-box {
          background: rgba(0,0,0,0.25); border-radius: 10px;
          padding: 11px 14px; margin-bottom: 4px;
          border: 1px solid rgba(201,168,76,0.1);
        }
      `}</style>

      <PageCard visible={visible}>
        <div className="prof-avatar">{initials}</div>

        <div style={{ fontFamily: "'Cinzel', serif", fontSize: '20px', textAlign: 'center', color: '#f0e8d8', letterSpacing: '0.5px' }}>
          {displayName}
        </div>
        <div style={{ textAlign: 'center', fontSize: '13px', color: '#4a5568', margin: '4px 0 12px' }}>
          {user?.email || '—'}
        </div>
        <div style={{ textAlign: 'center', marginBottom: '20px' }}>
          <span className={`badge ${user?.emailVerified ? 'badge-ok' : 'badge-warn'}`}>
            {user?.emailVerified ? '✓ Email Verified' : '⚠ Email Unverified'}
          </span>
        </div>

        <div className="info-grid">
          <div className="info-row">
            <span style={{ color: '#4a5568' }}>User ID</span>
            <span style={{ color: '#f0e8d8', fontWeight: 500, fontSize: '11px', fontFamily: 'monospace' }}>
              {user?.id ? user.id.substring(0, 8) + '...' : '—'}
            </span>
          </div>
          <div className="info-row">
            <span style={{ color: '#4a5568' }}>Role</span>
            <span style={{ color: '#f0e8d8', fontWeight: 500 }}>{user?.role || '—'}</span>
          </div>
          <div className="info-row">
            <span style={{ color: '#4a5568' }}>Session</span>
            <span style={{ color: '#68d391', fontWeight: 500 }}>● Active</span>
          </div>
        </div>

        <div className="token-box">
          <div style={{ fontSize: '10px', color: '#c9a84c', letterSpacing: '1.5px', textTransform: 'uppercase', marginBottom: '5px' }}>
            Access Token
          </div>
          <div style={{ fontSize: '10px', color: '#4a5568', wordBreak: 'break-all', fontFamily: 'monospace', lineHeight: 1.6 }}>
            {accessToken ? accessToken.substring(0, 64) + '...' : '—'}
          </div>
        </div>

        <OutlineButton onClick={onLogout}>
          Sign Out
        </OutlineButton>
      </PageCard>
    </>
  )
}
