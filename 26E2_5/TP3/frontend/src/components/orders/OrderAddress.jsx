import { MapPin } from 'lucide-react';

export function OrderAddress({ address }) {
  if (!address) return null;

  return (
    <div className="order-address-box">
      <h4>
        <MapPin size={14} style={{ display: 'inline', marginRight: '0.35rem' }} />
        Destino de Entrega
      </h4>
      <p style={{ color: 'var(--text-primary)', marginBottom: '0.2rem' }}>
        {address.street}
      </p>
      <p style={{ color: 'var(--text-secondary)' }}>
        {address.city} - {address.state}
      </p>
      <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.2rem' }}>
        CEP: {address.zipCode}
      </p>
    </div>
  );
}
