import { AuditLogRow } from './AuditLogRow';

export function AuditTable({ logs }) {
  return (
    <div className="audit-table-container glass-card">
      <table className="audit-table">
        <thead>
          <tr>
            <th>Data/Hora</th>
            <th>Entidade</th>
            <th>ID</th>
            <th>Ação</th>
            <th>Descrição</th>
            <th>Valor Anterior</th>
            <th>Novo Valor</th>
          </tr>
        </thead>
        <tbody>
          {logs.map(log => (
            <AuditLogRow key={log.id} log={log} />
          ))}
        </tbody>
      </table>
    </div>
  );
}
