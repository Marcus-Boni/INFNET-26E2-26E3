import { Trash2 } from 'lucide-react';

export function CartItem({ item, onRemoveFromCart }) {
  const itemTotal = item.product.price * item.quantity;

  return (
    <div className="cart-item">
      <div className="cart-item-details">
        <strong>{item.product.name}</strong>
        <span style={{ color: 'var(--text-secondary)' }}>
          {item.quantity}x R$ {item.product.price?.toFixed(2)} = <strong>R$ {itemTotal.toFixed(2)}</strong>
        </span>
      </div>
      <button 
        type="button"
        className="btn-danger btn-icon"
        onClick={() => onRemoveFromCart(item.product.id)}
        title="Remover do carrinho"
      >
        <Trash2 size={15} />
      </button>
    </div>
  );
}
