import { History } from 'lucide-react';
import { AuditFilter } from './AuditFilter';
import { AuditTable } from './AuditTable';
import { EmptyState } from '../common/EmptyState';

export function AuditTab({ auditLogs, currentFilter, onFilterChange, onRefresh }) {
  return (
    <div className="audit-history-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Trilha de Auditoria & Histórico de Dados</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
            Registro imutável de todas as modificações no banco de dados JPA (Preços, Estoque, Pedidos, Logística).
          </p>
        </div>

        <AuditFilter 
          currentFilter={currentFilter}
          onFilterChange={onFilterChange}
          onRefresh={onRefresh}
        />
      </div>

      {auditLogs.length === 0 ? (
        <EmptyState 
          icon={History} 
          message="Nenhum registro de auditoria encontrado ainda." 
        />
      ) : (
        <AuditTable logs={auditLogs} />
      )}
    </div>
  );
}
