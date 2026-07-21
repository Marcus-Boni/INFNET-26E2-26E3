import { ShoppingCart, Package, History } from 'lucide-react';

export function NavigationTabs({ activeTab, onTabChange, ordersCount, auditLogsCount }) {
  return (
    <nav className="tabs-navigation">
      <button 
        className={`tab-btn ${activeTab === 'catalog' ? 'active' : ''}`}
        onClick={() => onTabChange('catalog')}
      >
        <ShoppingCart size={16} /> Catálogo
      </button>
      <button 
        className={`tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
        onClick={() => onTabChange('orders')}
      >
        <Package size={16} /> Pedidos 
        {ordersCount > 0 && <span className="tab-badge">{ordersCount}</span>}
      </button>
      <button 
        className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
        onClick={() => onTabChange('history')}
      >
        <History size={16} /> Auditoria / Histórico
        {auditLogsCount > 0 && <span className="tab-badge alt">{auditLogsCount}</span>}
      </button>
    </nav>
  );
}
