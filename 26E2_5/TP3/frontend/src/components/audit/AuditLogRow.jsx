import { Clock } from 'lucide-react';
import { ActionBadge } from '../common/ActionBadge';

export function AuditLogRow({ log }) {
  return (
    <tr>
      <td style={{ whiteSpace: 'nowrap', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
        <Clock size={11} style={{ display: 'inline', marginRight: '4px', verticalAlign: 'middle' }} />
        {new Date(log.timestamp).toLocaleString('pt-BR')}
      </td>
      <td><strong>{log.entityName}</strong></td>
      <td>#{log.entityId}</td>
      <td><ActionBadge action={log.action} /></td>
      <td style={{ fontSize: '0.85rem' }}>{log.detailDescription}</td>
      <td style={{ fontSize: '0.8rem', color: 'var(--color-danger)' }}>
        {log.previousValue || '-'}
      </td>
      <td style={{ fontSize: '0.8rem', color: 'var(--color-success)' }}>
        {log.newValue || '-'}
      </td>
    </tr>
  );
}
