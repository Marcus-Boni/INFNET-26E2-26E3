export function StockBadge({ stock }) {
  if (stock === 0) {
    return <span className="badge badge-danger">Esgotado</span>;
  }
  if (stock <= 5) {
    return <span className="badge badge-warning">Apenas {stock} restam</span>;
  }
  return <span className="badge badge-success">{stock} em estoque</span>;
}
