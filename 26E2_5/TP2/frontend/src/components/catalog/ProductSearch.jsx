import { Search } from 'lucide-react';

export function ProductSearch({ searchTerm, onSearchTermChange, onSearchSubmit }) {
  return (
    <form onSubmit={onSearchSubmit} style={{ display: 'flex', gap: '0.5rem' }}>
      <input 
        type="text" 
        placeholder="Buscar por nome (Spring Data JPA)..." 
        value={searchTerm}
        onChange={(e) => onSearchTermChange(e.target.value)}
        style={{ padding: '0.5rem 0.75rem', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)', background: 'var(--bg-input)', color: 'var(--text-primary)' }}
      />
      <button type="submit" className="btn btn-secondary">
        <Search size={16} />
      </button>
    </form>
  );
}
