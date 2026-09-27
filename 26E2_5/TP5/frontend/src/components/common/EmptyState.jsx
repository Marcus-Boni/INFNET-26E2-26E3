export function EmptyState({ icon: Icon, message, style }) {
  return (
    <div 
      className="glass-card" 
      style={{
        padding: '3.5rem 1.5rem',
        textAlign: 'center',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '0.75rem',
        color: 'var(--text-muted)',
        ...style
      }}
    >
      {Icon && <Icon size={44} strokeWidth={1.5} style={{ color: 'var(--text-secondary)' }} />}
      <p style={{ fontSize: '0.95rem', maxWidth: '400px' }}>{message}</p>
    </div>
  );
}
