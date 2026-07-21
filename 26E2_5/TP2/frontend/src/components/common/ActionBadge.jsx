export function ActionBadge({ action }) {
  switch (action) {
    case 'CREATE':
      return <span className="badge badge-success">CRIAÇÃO</span>;
    case 'STOCK_CHANGE':
      return <span className="badge badge-warning">ESTOQUE</span>;
    case 'PRICE_CHANGE':
      return <span className="badge badge-pending">PREÇO</span>;
    case 'STATUS_CHANGE':
      return <span className="badge badge-primary">STATUS</span>;
    case 'DELETE':
      return <span className="badge badge-danger">EXCLUSÃO</span>;
    default:
      return <span className="badge">{action}</span>;
  }
}
