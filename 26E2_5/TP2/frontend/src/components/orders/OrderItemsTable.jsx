export function OrderItemsTable({ items, totalPrice }) {
  return (
    <table className="order-items-table">
      <thead>
        <tr>
          <th>Produto</th>
          <th style={{ textAlign: 'center' }}>Qtd</th>
          <th style={{ textAlign: 'right' }}>Preço Unit.</th>
          <th style={{ textAlign: 'right' }}>Total</th>
        </tr>
      </thead>
      <tbody>
        {items.map(item => (
          <tr key={item.id}>
            <td>{item.productName}</td>
            <td style={{ textAlign: 'center' }}>{item.quantity}</td>
            <td style={{ textAlign: 'right' }}>
              R$ {item.unitPrice.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </td>
            <td style={{ textAlign: 'right', fontWeight: '500' }}>
              R$ {(item.unitPrice * item.quantity).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </td>
          </tr>
        ))}
        <tr>
          <td colSpan="3" style={{ textAlign: 'right', fontWeight: 'bold', border: 'none', paddingTop: '1rem' }}>
            Total do Pedido:
          </td>
          <td style={{ textAlign: 'right', fontWeight: 'bold', fontSize: '1.1rem', color: 'var(--text-primary)', border: 'none', paddingTop: '1rem' }}>
            R$ {totalPrice.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </td>
        </tr>
      </tbody>
    </table>
  );
}
