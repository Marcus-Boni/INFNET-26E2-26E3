import React from 'react';
import { ShoppingCart, Package, Truck, History, Radio } from 'lucide-react';

export function Header({
  activeTab,
  onTabChange,
  ordersCount,
  auditLogsCount,
  shipmentsCount,
  eventsCount
}) {
  return (
    <header className="header">
      <div className="brand-section">
        <h1>Nexus Store <span className="tp-badge" style={{ background: 'linear-gradient(135deg, #9333ea, #6366f1)' }}>TP4</span></h1>
        <span className="brand-subtitle">Arquitetura Orientada a Eventos • RabbitMQ Message Broker</span>
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
          className={`tab-btn ${activeTab === 'events' ? 'active' : ''}`}
          onClick={() => onTabChange('events')}
          style={activeTab === 'events' ? { borderColor: '#a855f7', color: '#c084fc' } : {}}
        >
          <Radio size={16} className={activeTab === 'events' ? 'text-purple-400' : ''} /> 
          Eventos (RabbitMQ)
          {eventsCount > 0 && <span className="tab-badge" style={{ background: '#7e22ce' }}>{eventsCount}</span>}
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
