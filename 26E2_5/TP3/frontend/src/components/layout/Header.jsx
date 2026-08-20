import { ShoppingCart, Package, Truck, History } from 'lucide-react';

export function Header({
  activeTab,
  onTabChange,
  ordersCount,
  auditLogsCount,
  shipmentsCount
}) {
  return (
    <header className="header">
      <div className="brand-section">
        <h1>Nexus Store <span className="tp-badge">TP3</span></h1>
        <span className="brand-subtitle">Plataforma Distribuída • Spring Cloud & Microsserviços</span>
      </div>

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
          className={`tab-btn ${activeTab === 'shipping' ? 'active' : ''}`}
          onClick={() => onTabChange('shipping')}
        >
          <Truck size={16} /> Logística & Rastreio
          {shipmentsCount > 0 && <span className="tab-badge">{shipmentsCount}</span>}
        </button>

        <button 
          className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
          onClick={() => onTabChange('history')}
        >
          <History size={16} /> Auditoria / Histórico
          {auditLogsCount > 0 && <span className="tab-badge">{auditLogsCount}</span>}
        </button>
      </nav>
    </header>
  );
}
