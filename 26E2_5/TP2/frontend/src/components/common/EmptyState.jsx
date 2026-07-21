export function EmptyState({ icon: Icon, message, style = {} }) {
  return (
    <div className="glass-card empty-state" style={style}>
      {Icon && <Icon size={48} style={{ margin: '0 auto 1rem', display: 'block', opacity: 0.5 }} />}
      <p>{message}</p>
    </div>
  );
}
