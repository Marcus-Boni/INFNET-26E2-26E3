import { RefreshCw } from 'lucide-react';

export function AuditFilter({ currentFilter, onFilterChange, onRefresh }) {
  return (
    <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center' }}>
      <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Filtrar por Entidade:</span>
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
        <RefreshCw size={14} />
      </button>
    </div>
  );
}
