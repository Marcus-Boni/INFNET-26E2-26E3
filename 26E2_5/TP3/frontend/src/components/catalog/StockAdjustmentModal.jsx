import { useState } from 'react';

export function StockAdjustmentModal({ product, onAdjustStock, onClose }) {
  const [delta, setDelta] = useState(1);

  const handleSubmit = (e) => {
    e.preventDefault();
    onAdjustStock(product.id, parseInt(delta, 10));
  };

  return (
    <form 
      onSubmit={handleSubmit}
      style={{
        background: 'rgba(15, 23, 42, 0.95)',
        border: '1px solid var(--border-highlight)',
        borderRadius: 'var(--radius-md)',
        padding: '0.85rem',
        margin: '0.75rem 0',
        display: 'flex',
        flexDirection: 'column',
        gap: '0.5rem'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem' }}>
        <span>Ajuste de Estoque ({product.name})</span>
        <button type="button" onClick={onClose} style={{ background: 'none', border: 'none', color: 'var(--text-muted)', cursor: 'pointer' }}>✕</button>
      </div>
      <div style={{ display: 'flex', gap: '0.5rem' }}>
        <input 
          type="number" 
          value={delta} 
          onChange={(e) => setDelta(e.target.value)} 
          placeholder="Ex: 5 ou -2"
          style={{ width: '80px', padding: '0.4rem' }}
          required
        />
        <button type="submit" className="btn btn-primary btn-sm" style={{ flex: 1 }}>
          Aplicar
        </button>
      </div>
    </form>
  );
}
