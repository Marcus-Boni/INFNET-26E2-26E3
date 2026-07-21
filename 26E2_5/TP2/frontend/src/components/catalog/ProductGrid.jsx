import { ProductCard } from './ProductCard';

export function ProductGrid({
  products,
  getCartQty,
  quantities,
  selectedProductForStock,
  onToggleStockPanel,
  onAdjustStock,
  onIncrement,
  onDecrement,
  onAddToCart
}) {
  return (
    <div className="products-grid">
      {products.map(product => {
        const inCartQty = getCartQty(product.id);
        const currentStepperVal = quantities[product.id] || 0;

        return (
          <ProductCard
            key={product.id}
            product={product}
            inCartQty={inCartQty}
            currentStepperVal={currentStepperVal}
            selectedProductForStock={selectedProductForStock}
            onToggleStockPanel={onToggleStockPanel}
            onAdjustStock={onAdjustStock}
            onIncrement={onIncrement}
            onDecrement={onDecrement}
            onAddToCart={onAddToCart}
          />
        );
      })}
    </div>
  );
}
