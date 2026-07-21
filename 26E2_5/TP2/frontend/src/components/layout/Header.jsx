import { NavigationTabs } from './NavigationTabs';

export function Header({ activeTab, onTabChange, ordersCount, auditLogsCount }) {
  return (
    <header className="header">
      <div className="brand-section">
        <h1>Nexus Store <span className="tp-badge">TP2</span></h1>
        <span className="brand-subtitle">Camada de Persistência JPA & Auditoria de Histórico de Dados</span>
      </div>
      <NavigationTabs 
        activeTab={activeTab}
        onTabChange={onTabChange}
        ordersCount={ordersCount}
        auditLogsCount={auditLogsCount}
      />
    </header>
  );
}
