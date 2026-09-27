export function AuditLogItem({ log }) {
  const getActionBadgeClass = (action) => {
    switch (action) {
      case 'CREATE': return 'badge-success';
      case 'UPDATE': return 'badge-info';
      case 'STOCK_CHANGE': return 'badge-warning';
      case 'STATUS_CHANGE': return 'badge-purple';
      case 'SHIPPING_REGISTERED':
      case 'SHIPPING_DISPATCH': return 'badge-info';
      case 'DELETE': return 'badge-danger';
      default: return 'badge-info';
    }
  };

  return (
    <div className="glass-card audit-item">
      <div className="audit-item-header">
        <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
          <span className={`badge ${getActionBadgeClass(log.action)}`}>
            {log.action}
          </span>
          <strong>{log.entityName} #{log.entityId}</strong>
        </div>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
          {new Date(log.timestamp).toLocaleString('pt-BR')}
        </span>
      </div>

      <p style={{ fontSize: '0.85rem', color: 'var(--text-primary)', margin: '0.2rem 0' }}>
        {log.detailDescription}
      </p>

      {(log.previousValue || log.newValue) && (
        <div style={{
          display: 'grid',
          gridTemplateColumns: '1fr 1fr',
          gap: '0.5rem',
          background: 'rgba(15, 23, 42, 0.6)',
          padding: '0.5rem 0.75rem',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.8rem',
          marginTop: '0.25rem'
        }}>
          <div>
            <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.7rem' }}>Valor Anterior:</span>
            <span style={{ color: '#fca5a5' }}>{log.previousValue || '—'}</span>
          </div>
          <div>
            <span style={{ color: 'var(--text-muted)', display: 'block', fontSize: '0.7rem' }}>Novo Valor:</span>
            <span style={{ color: '#86efac' }}>{log.newValue || '—'}</span>
          </div>
        </div>
      )}
    </div>
  );
}
