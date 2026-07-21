import { Trash2 } from 'lucide-react';

export function CartItem({ item, onRemoveFromCart }) {
  return (
    <div className="cart-item">
      <div>
        <div className="cart-item-name">{item.product.name}</div>
        <span className="cart-item-qty">{item.quantity}x • R$ {item.product.price.toFixed(2)}</span>
      </div>
      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
        <span className="cart-item-price">R$ {(item.product.price * item.quantity).toFixed(2)}</span>
        <button 
          className="cart-item-remove"
          onClick={() => onRemoveFromCart(item.product.id)}
          title="Remover do carrinho"
        >
          <Trash2 size={16} />
        </button>
      </div>
    </div>
  );
}
