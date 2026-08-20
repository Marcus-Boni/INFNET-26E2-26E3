import { ShoppingCart } from 'lucide-react';
import { CartItem } from './CartItem';
import { CartSummary } from './CartSummary';
import { CheckoutForm } from './CheckoutForm';
import { EmptyState } from '../common/EmptyState';

export function CartSidebar({
  cart,
  onRemoveFromCart,
  itemsTotal,
  form,
  onInputChange,
  shippingOptions,
  selectedShippingOption,
  onSelectShippingOption,
  onCalculateShipping,
  isCalculatingShipping,
  onCheckout
}) {
  const totalItemsCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <aside className="glass-card cart-card">
      <div className="cart-title-row">
        <h3>Carrinho</h3>
        <span className="badge badge-success">{totalItemsCount} itens</span>
      </div>

      {cart.length === 0 ? (
        <EmptyState 
          icon={ShoppingCart} 
          message="Seu carrinho está vazio. Adicione produtos para começar."
          style={{ padding: '2rem 1rem', background: 'transparent', border: 'none' }}
        />
      ) : (
        <>
          <div className="cart-items-list">
            {cart.map(item => (
              <CartItem 
                key={item.product.id} 
                item={item} 
                onRemoveFromCart={onRemoveFromCart} 
              />
            ))}
          </div>

          <CartSummary 
            itemsTotal={itemsTotal}
            shippingCost={selectedShippingOption ? selectedShippingOption.price : null}
            selectedCarrier={selectedShippingOption}
          />

          <CheckoutForm 
            form={form} 
            onInputChange={onInputChange}
            shippingOptions={shippingOptions}
            selectedShippingOption={selectedShippingOption}
            onSelectShippingOption={onSelectShippingOption}
            onCalculateShipping={onCalculateShipping}
            isCalculatingShipping={isCalculatingShipping}
            onCheckout={onCheckout} 
          />
        </>
      )}
    </aside>
  );
}
