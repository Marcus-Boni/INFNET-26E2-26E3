export function CartSummary({ itemsTotal, shippingCost, selectedCarrier }) {
  const total = itemsTotal + (shippingCost || 0);

  return (
    <div className="cart-summary">
      <div className="summary-row">
        <span>Subtotal dos Itens:</span>
        <span>R$ {itemsTotal.toFixed(2)}</span>
      </div>
      <div className="summary-row">
        <span>Frete ({selectedCarrier ? selectedCarrier.carrierName : 'Não calculado'}):</span>
        <span>
          {shippingCost === null || shippingCost === undefined ? (
            'A calcular'
          ) : shippingCost === 0 ? (
            <strong style={{ color: 'var(--color-success)' }}>GRÁTIS</strong>
          ) : (
            `R$ ${shippingCost.toFixed(2)}`
          )}
        </span>
      </div>
      <div className="summary-row summary-total">
        <span>Total do Pedido:</span>
        <span>R$ {total.toFixed(2)}</span>
      </div>
    </div>
  );
}
