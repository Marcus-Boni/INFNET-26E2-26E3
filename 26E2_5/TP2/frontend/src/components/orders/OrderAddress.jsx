export function OrderAddress({ address }) {
  if (!address) return null;

  return (
    <div className="order-address-box">
      <h4 className="order-address-title">Endereço de Entrega</h4>
      <p style={{ color: 'var(--text-primary)', marginBottom: '0.25rem' }}>{address.street}</p>
      <p>
        {address.city} - {address.state}
      </p>
      <p style={{ color: 'var(--text-muted)' }}>CEP: {address.zipCode}</p>
    </div>
  );
}
