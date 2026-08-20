import { RefreshCw } from 'lucide-react';

export function AuditFilter({ currentFilter, onFilterChange, onRefresh }) {
  return (
    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
      <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Filtrar:</span>
      <button 
        className={`btn btn-sm ${currentFilter === 'ALL' ? 'btn-primary' : 'btn-secondary'}`}
        onClick={() => onFilterChange('ALL')}
      >
        Todas
      </button>
      <button 
        className={`btn btn-sm ${currentFilter === 'Product' ? 'btn-primary' : 'btn-secondary'}`}
        onClick={() => onFilterChange('Product')}
      >
        Produtos
      </button>
      <button 
        className={`btn btn-sm ${currentFilter === 'Order' ? 'btn-primary' : 'btn-secondary'}`}
        onClick={() => onFilterChange('Order')}
      >
        Pedidos
      </button>
      <button 
        className="btn btn-sm btn-secondary" 
        onClick={onRefresh}
        title="Atualizar Logs"
      >
        <RefreshCw size={13} />
      </button>
    </div>
  );
}
