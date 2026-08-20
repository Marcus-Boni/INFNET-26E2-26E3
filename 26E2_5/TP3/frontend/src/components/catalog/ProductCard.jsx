import { SlidersHorizontal } from 'lucide-react';

export function ProductCard({
  product,
  cartQty,
  currentQty,
  isStockPanelOpen,
  onToggleStockPanel,
  onIncrement,
  onDecrement,
  onAddToCart,
  stockAdjustmentModal
}) {
  const isOutOfStock = product.stock <= 0;
  const isLowStock = product.stock > 0 && product.stock <= 4;
  const availableStock = product.stock - cartQty;

  return (
    <div className="glass-card product-card">
      <div className="product-info">
        <div className="product-header">
          <h3 className="product-name">{product.name}</h3>
          {isOutOfStock ? (
            <span className="badge badge-danger">Esgotado</span>
          ) : isLowStock ? (
            <span className="badge badge-warning">{product.stock} un.</span>
          ) : (
            <span className="badge badge-success">{product.stock} em estoque</span>
          )}
        </div>
        <p className="product-description">{product.description}</p>
        
        <div className="product-meta">
          <span className="product-price">
            R$ {product.price?.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </span>
          <button 
            type="button" 
            className="btn-stock-adjust"
            onClick={() => onToggleStockPanel(product.id)}
          >
            <SlidersHorizontal size={12} /> Estoque
          </button>
        </div>
        
        {isStockPanelOpen && stockAdjustmentModal}
      </div>

      <div className="purchase-controls">
        {!isOutOfStock && (
          <div className="quantity-stepper">
            <button 
              type="button" 
              className="stepper-btn"
              onClick={() => onDecrement(product.id)}
              disabled={currentQty <= 1}
            >
              -
            </button>
            <span className="stepper-value">{currentQty}</span>
            <button 
              type="button" 
              className="stepper-btn"
              onClick={() => onIncrement(product.id, product.stock)}
              disabled={currentQty >= availableStock}
            >
              +
            </button>
          </div>
        )}
        
        <button 
          type="button"
          className="btn btn-primary"
          onClick={() => onAddToCart(product)}
          disabled={isOutOfStock || availableStock <= 0}
          style={{ flexGrow: 1 }}
        >
          {isOutOfStock ? 'Indisponível' : availableStock <= 0 ? 'No Carrinho' : 'Adicionar'}
        </button>
      </div>
    </div>
  );
}
