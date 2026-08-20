export function StatusBadge({ status }) {
  let badgeClass = 'badge-pending';
  let label = status;

  switch (status) {
    case 'PENDING':
    case 'CREATED':
      badgeClass = 'badge-warning';
      label = status === 'PENDING' ? 'Pendente' : 'Criado';
      break;
    case 'DISPATCHED':
      badgeClass = 'badge-pending';
      label = 'Despachado';
      break;
    case 'IN_TRANSIT':
      badgeClass = 'badge-pending';
      label = 'Em Trânsito';
      break;
    case 'OUT_FOR_DELIVERY':
      badgeClass = 'badge-pending';
      label = 'Saiu p/ Entrega';
      break;
    case 'SHIPPED':
    case 'DELIVERED':
      badgeClass = 'badge-success';
      label = status === 'SHIPPED' ? 'Enviado' : 'Entregue';
      break;
    case 'CANCELLED':
      badgeClass = 'badge-danger';
      label = 'Cancelado';
      break;
    default:
      badgeClass = 'badge-pending';
  }

  return (
    <span className={`badge ${badgeClass}`}>
      {label}
    </span>
  );
}
