import { Truck } from 'lucide-react';

export function CarrierBadge({ carrier }) {
  if (!carrier) return null;

  return (
    <span style={{ 
      display: 'inline-flex', 
      alignItems: 'center', 
      gap: '0.3rem', 
      fontSize: '0.725rem',
      fontWeight: '500',
      color: 'var(--text-secondary)',
      background: '#18181b',
      padding: '0.1rem 0.45rem',
      borderRadius: 'var(--radius-sm)',
      border: '1px solid var(--border-color)'
    }}>
      <Truck size={12} />
      {carrier}
    </span>
  );
}
