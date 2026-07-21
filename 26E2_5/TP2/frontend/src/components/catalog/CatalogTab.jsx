import { ProductSearch } from './ProductSearch';
import { ProductGrid } from './ProductGrid';

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
      <div className="products-grid-container">
        <div className="catalog-header-bar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
          <h2>Produtos Disponíveis</h2>
          <ProductSearch 
            searchTerm={searchTerm}
            onSearchTermChange={onSearchTermChange}
            onSearchSubmit={onSearchSubmit}
          />
        </div>

        <ProductGrid 
          products={products}
          getCartQty={getCartQty}
          quantities={quantities}
          selectedProductForStock={selectedProductForStock}
          onToggleStockPanel={onToggleStockPanel}
          onAdjustStock={onAdjustStock}
          onIncrement={onIncrement}
          onDecrement={onDecrement}
          onAddToCart={onAddToCart}
        />
      </div>

      {cartSidebarComponent}
    </div>
  );
}
