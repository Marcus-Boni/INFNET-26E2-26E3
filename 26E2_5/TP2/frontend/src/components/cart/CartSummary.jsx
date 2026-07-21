export function CartSummary({ total }) {
  const formattedTotal = total.toFixed(2);

  return (
    <div className="cart-summary">
      <div className="summary-row">
        <span>Subtotal</span>
        <span>R$ {formattedTotal}</span>
      </div>
      <div className="summary-row total">
        <span>Total</span>
        <span>R$ {formattedTotal}</span>
      </div>
    </div>
  );
}
