/* shared-styles.js — imported by all screens */

export const S = {
  // Full page wrapper
  page: {
    minHeight: '100dvh',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    padding: '20px 16px',
    position: 'relative',
    zIndex: 1,
    // Fix 4: allow scrolling when content overflows (e.g. profile on short screens)
    overflowY: 'auto',
    WebkitOverflowScrolling: 'touch',
  },

  // Card
  card: {
    width: '100%',
    maxWidth: '440px',
    background: 'linear-gradient(160deg, rgba(12,20,36,0.98) 0%, rgba(6,12,24,0.99) 100%)',
    border: '1px solid rgba(201,168,76,0.18)',
    borderTop: 'none',
    borderRadius: '0 0 20px 20px',
    // Fix 1: reduce padding on small screens via responsive value
    padding: 'clamp(20px, 5vw, 36px) clamp(16px, 5vw, 36px) clamp(20px, 4vw, 32px)',
    boxShadow: '0 40px 100px rgba(0,0,0,0.8), 0 0 0 1px rgba(255,255,255,0.03) inset',
  },

  topLine: {
    height: '1px',
    background: 'linear-gradient(90deg, transparent, #c9a84c, #e8c97a, #c9a84c, transparent)',
  },

  bottomLine: {
    height: '1px',
    background: 'linear-gradient(90deg, transparent, rgba(201,168,76,0.3), transparent)',
    marginTop: '4px',
  },

  // Logo - Fix 5: responsive logo sizing
  logoWrap: {
    textAlign: 'center',
    marginBottom: 'clamp(16px, 3vw, 24px)',
    paddingBottom: 'clamp(14px, 3vw, 20px)',
    borderBottom: '1px solid rgba(201,168,76,0.1)'
  },
  logoImg: {
    // Fix 5: scale logo down on very small screens
    width: 'clamp(52px, 15vw, 72px)',
    height: 'clamp(52px, 15vw, 72px)',
    objectFit: 'contain',
    filter: 'drop-shadow(0 4px 20px rgba(201,168,76,0.35))'
  },
  logoName: {
    fontFamily: "'Cinzel', serif",
    // Fix 5: responsive font size
    fontSize: 'clamp(13px, 4vw, 17px)',
    fontWeight: 600,
    color: '#f0e8d8',
    letterSpacing: '2px',
    marginTop: '8px',
    textTransform: 'uppercase'
  },
  logoSub: {
    fontSize: 'clamp(8px, 2.5vw, 10px)',
    color: '#c9a84c',
    letterSpacing: '4px',
    textTransform: 'uppercase',
    marginTop: '3px'
  },

  // Heading
  heading: {
    fontFamily: "'Cinzel', serif",
    fontSize: 'clamp(18px, 5vw, 21px)',
    fontWeight: 600,
    color: '#f0e8d8',
    marginBottom: '4px',
    letterSpacing: '0.5px'
  },
  subheading: {
    fontSize: 'clamp(12px, 3vw, 13px)',
    color: '#4a5568',
    marginBottom: '20px',
    fontWeight: 400
  },

  // Field
  field: { marginBottom: '14px' },
  label: {
    display: 'block',
    fontSize: '10px',
    textTransform: 'uppercase',
    letterSpacing: '2px',
    color: '#c9a84c',
    marginBottom: '6px',
    fontWeight: 600
  },
  input: {
    width: '100%',
    padding: '12px 14px',
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.07)',
    borderRadius: '10px',
    color: '#f0e8d8',
    // Fix 3: must be 16px to prevent iOS auto-zoom
    fontSize: '16px',
    fontFamily: "'Raleway', sans-serif",
    outline: 'none',
    transition: 'all 0.25s',
    WebkitAppearance: 'none',
    boxSizing: 'border-box',
  },

  // Buttons
  btnPrimary: {
    width: '100%',
    padding: '14px',
    border: 'none',
    borderRadius: '10px',
    background: 'linear-gradient(135deg, #b8933e, #d4aa55, #e8c97a, #c9a84c)',
    color: '#050d1a',
    fontSize: '12px',
    fontFamily: "'Cinzel', serif",
    fontWeight: 700,
    letterSpacing: '3px',
    textTransform: 'uppercase',
    cursor: 'pointer',
    transition: 'all 0.3s',
    marginTop: '8px',
    boxShadow: '0 4px 24px rgba(201,168,76,0.3)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    // Fix 7: remove tap highlight on all buttons
    WebkitTapHighlightColor: 'transparent',
    minHeight: '48px',
    // Good tap target size for mobile
    touchAction: 'manipulation',
  },
  btnOutline: {
    width: '100%',
    padding: '13px',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '10px',
    background: 'rgba(255,255,255,0.03)',
    color: '#a0aec0',
    fontSize: '13px',
    fontFamily: "'Raleway', sans-serif",
    fontWeight: 500,
    cursor: 'pointer',
    transition: 'all 0.25s',
    marginTop: '10px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '8px',
    // Fix 7: remove tap highlight
    WebkitTapHighlightColor: 'transparent',
    minHeight: '46px',
    touchAction: 'manipulation',
  },
  btnGoogle: {
    width: '100%',
    padding: '13px',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '10px',
    background: 'rgba(255,255,255,0.04)',
    color: '#c8d0d8',
    fontSize: '13px',
    fontFamily: "'Raleway', sans-serif",
    fontWeight: 500,
    cursor: 'pointer',
    transition: 'all 0.25s',
    marginTop: '10px',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    gap: '10px',
    // Fix 7: remove tap highlight
    WebkitTapHighlightColor: 'transparent',
    minHeight: '46px',
    touchAction: 'manipulation',
  },

  // Misc
  divider: { display: 'flex', alignItems: 'center', gap: '14px', margin: '16px 0' },
  dividerLine: { flex: 1, height: '1px', background: 'rgba(255,255,255,0.07)' },
  dividerText: { fontSize: '10px', color: '#2d3748', letterSpacing: '2px' },
  linkRow: { textAlign: 'center', marginTop: '18px', fontSize: '13px', color: '#4a5568' },
  link: { color: '#c9a84c', cursor: 'pointer', fontWeight: 600 },
  forgotWrap: { textAlign: 'right', marginTop: '-8px', marginBottom: '18px' },
  forgotLink: { fontSize: '12px', color: '#c9a84c', cursor: 'pointer', fontWeight: 500 },

  // Fix 2: row wraps on very small screens
  row: {
    display: 'flex',
    gap: '10px',
    flexWrap: 'wrap',
  },
  // Fix 2: each field in a row takes at least 120px, grows equally
  rowField: {
    flex: '1 1 120px',
    minWidth: '0',
  },

  alertError: {
    padding: '11px 14px',
    borderRadius: '8px',
    fontSize: '13px',
    marginBottom: '14px',
    lineHeight: 1.5,
    background: 'rgba(180,40,40,0.12)',
    border: '1px solid rgba(180,40,40,0.25)',
    color: '#fc8181'
  },
  alertSuccess: {
    padding: '11px 14px',
    borderRadius: '8px',
    fontSize: '13px',
    marginBottom: '14px',
    lineHeight: 1.5,
    background: 'rgba(39,174,96,0.1)',
    border: '1px solid rgba(39,174,96,0.2)',
    color: '#68d391'
  },
}
