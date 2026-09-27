import { PackageSearch } from 'lucide-react';
import { ProductCard } from './ProductCard';
import { StockAdjustmentModal } from './StockAdjustmentModal';
import { EmptyState } from '../common/EmptyState';

export function CatalogTab({
  products,
  searchTerm,
  onSearchTermChange,
  onSearchSubmit,
  getCartQty,
  quantities,
  selectedProductForStock,
  onToggleStockPanel,
  onAdjustStock,
  onIncrement,
  onDecrement,
  onAddToCart,
  cartSidebarComponent
}) {
  return (
    <div className="main-grid">
      <section className="catalog-section">
        <div className="catalog-header">
          <h2>Catálogo de Produtos</h2>
          <form className="search-form" onSubmit={onSearchSubmit}>
            <input 
              type="text" 
              placeholder="Buscar produtos por nome ou descrição..." 
              value={searchTerm}
              onChange={(e) => onSearchTermChange(e.target.value)}
            />
            <button type="submit" className="btn btn-secondary">Buscar</button>
          </form>
        </div>

        {products.length === 0 ? (
          <EmptyState 
            icon={PackageSearch} 
            message="Nenhum produto encontrado correspondente à busca." 
          />
        ) : (
          <div className="products-grid">
            {products.map(product => (
              <ProductCard
                key={product.id}
                product={product}
                cartQty={getCartQty(product.id)}
                currentQty={quantities[product.id] || 1}
                isStockPanelOpen={selectedProductForStock === product.id}
                onToggleStockPanel={onToggleStockPanel}
                onIncrement={onIncrement}
                onDecrement={onDecrement}
                onAddToCart={onAddToCart}
                stockAdjustmentModal={
                  <StockAdjustmentModal 
                    product={product}
                    onAdjustStock={onAdjustStock}
                    onClose={() => onToggleStockPanel(product.id)}
                  />
                }
              />
            ))}
          </div>
        )}
      </section>

      {cartSidebarComponent}
    </div>
  );
}
