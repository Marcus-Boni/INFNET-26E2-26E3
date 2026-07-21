import { ShoppingCart, Edit2 } from 'lucide-react';
import { StockBadge } from '../common/StockBadge';
import { QuantityStepper } from '../common/QuantityStepper';
import { StockAdjustPanel } from './StockAdjustPanel';

export function ProductCard({
  product,
  inCartQty,
  currentStepperVal,
  selectedProductForStock,
  onToggleStockPanel,
  onAdjustStock,
  onIncrement,
  onDecrement,
  onAddToCart
}) {
  const availableStock = product.stock - inCartQty;

  return (
    <div className="glass-card product-card">
      <div className="product-info">
        <div className="product-header">
          <h3 className="product-name">{product.name}</h3>
          <StockBadge stock={product.stock} />
        </div>
        <p className="product-description">{product.description}</p>
        <div className="product-meta">
          <span className="product-price">
            R$ {product.price.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
          </span>
          <button 
            className="btn-stock-adjust"
            onClick={() => onToggleStockPanel(product.id)}
            title="Ajustar estoque para testar histórico"
          >
            <Edit2 size={12} /> Ajustar Estoque
          </button>
        </div>
        
        {selectedProductForStock === product.id && (
          <StockAdjustPanel 
            productId={product.id}
            onAdjustStock={onAdjustStock}
          />
        )}
      </div>

      <div className="purchase-controls">
        {product.stock > 0 && availableStock > 0 ? (
          <>
            <QuantityStepper
              value={currentStepperVal}
              onIncrement={() => onIncrement(product.id, product.stock)}
              onDecrement={() => onDecrement(product.id)}
              minDisabled={currentStepperVal <= 1}
              maxDisabled={currentStepperVal >= availableStock}
            />
            <button 
              className="btn btn-primary"
              style={{ flexGrow: 1 }}
              onClick={() => onAddToCart(product)}
            >
              <ShoppingCart size={16} /> Adicionar
            </button>
          </>
        ) : (
          <button className="btn btn-secondary" style={{ width: '100%' }} disabled>
            {product.stock === 0 ? 'Indisponível' : 'Estoque Limite no Carrinho'}
          </button>
        )}
      </div>
    </div>
  );
}
