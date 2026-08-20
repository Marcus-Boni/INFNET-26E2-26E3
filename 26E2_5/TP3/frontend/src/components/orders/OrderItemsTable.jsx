export function OrderItemsTable({ items, itemsTotal, shippingCost, totalPrice }) {
  return (
    <div>
      <table className="order-items-table">
        <thead>
          <tr>
            <th>Item</th>
            <th style={{ textAlign: 'center' }}>Qtd</th>
            <th style={{ textAlign: 'right' }}>Total</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item, index) => (
            <tr key={index}>
              <td>{item.productName}</td>
              <td style={{ textAlign: 'center' }}>{item.quantity}x</td>
              <td style={{ textAlign: 'right' }}>
                R$ {(item.unitPrice * item.quantity).toFixed(2)}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      <div style={{ marginTop: '0.75rem', fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
        {shippingCost !== undefined && (
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.25rem' }}>
            <span>Frete:</span>
            <span>R$ {Number(shippingCost).toFixed(2)}</span>
          </div>
        )}
        <div style={{ display: 'flex', justifyContent: 'space-between', fontWeight: '700', color: '#fff', fontSize: '0.95rem' }}>
          <span>Total Geral:</span>
          <span>R$ {Number(totalPrice).toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
}
