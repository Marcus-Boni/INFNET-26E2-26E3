export function StatusBadge({ status }) {
  switch (status) {
    case 'PENDING':
      return <span className="badge badge-pending">Pendente</span>;
    case 'SHIPPED':
      return <span className="badge badge-success">Enviado</span>;
    case 'CANCELLED':
      return <span className="badge badge-danger">Cancelado</span>;
    default:
      return <span className="badge">{status}</span>;
  }
}
