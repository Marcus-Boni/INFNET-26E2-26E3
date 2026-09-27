import React from 'react';
import { ShoppingCart, Package, Truck, History, Radio, Cpu } from 'lucide-react';

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
        <h1>Nexus Store <span className="tp-badge" style={{ background: 'linear-gradient(135deg, #06b6d4, #3b82f6)' }}>TP5</span></h1>
        <span className="brand-subtitle">Operação, Conteinerização (Docker & Kubernetes) & Observabilidade</span>
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
        >
          <Radio size={16} className={activeTab === 'events' ? 'text-purple-400' : ''} /> 
          Eventos (RabbitMQ)
          {eventsCount > 0 && <span className="tab-badge" style={{ background: '#7e22ce' }}>{eventsCount}</span>}
        </button>

        <button 
          className={`tab-btn ${activeTab === 'devops' ? 'active' : ''}`}
          onClick={() => onTabChange('devops')}
          style={activeTab === 'devops' ? { borderColor: '#06b6d4', color: '#22d3ee' } : {}}
        >
          <Cpu size={16} className={activeTab === 'devops' ? 'text-cyan-400' : ''} /> 
          DevOps & K8s
          <span className="tab-badge" style={{ background: '#0284c7' }}>K8s</span>
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
