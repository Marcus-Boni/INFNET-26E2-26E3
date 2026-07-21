export function StockAdjustPanel({ productId, onAdjustStock }) {
  return (
    <div className="stock-adjust-panel animate-fade-in" style={{ marginTop: '0.75rem', padding: '0.5rem', background: 'rgba(255,255,255,0.05)', borderRadius: '6px' }}>
      <span style={{ fontSize: '0.8rem', display: 'block', marginBottom: '0.4rem' }}>Simular Alteração de Estoque:</span>
      <div style={{ display: 'flex', gap: '0.4rem' }}>
        <button className="btn btn-sm btn-secondary" onClick={() => onAdjustStock(productId, 5)}>+5 Unid</button>
        <button className="btn btn-sm btn-secondary" onClick={() => onAdjustStock(productId, -2)}>-2 Unid</button>
      </div>
    </div>
  );
}
