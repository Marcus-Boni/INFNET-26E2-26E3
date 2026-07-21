export function CheckoutForm({ form, onInputChange, onCheckout }) {
  return (
    <form onSubmit={onCheckout}>
      <h4 style={{ marginBottom: '0.75rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
        Endereço de Entrega
      </h4>
      <div className="form-group" style={{ marginBottom: '0.75rem' }}>
        <label>E-mail do Cliente</label>
        <input 
          type="email" 
          name="customerEmail"
          value={form.customerEmail}
          onChange={onInputChange}
          placeholder="seuemail@exemplo.com"
          required
        />
      </div>
      <div className="form-group" style={{ marginBottom: '0.75rem' }}>
        <label>Rua / Logradouro</label>
        <input 
          type="text" 
          name="street"
          value={form.street}
          onChange={onInputChange}
          placeholder="Av. Rio Branco, 156"
          required
        />
      </div>
      <div className="form-grid">
        <div className="form-group">
          <label>Cidade</label>
          <input 
            type="text" 
            name="city"
            value={form.city}
            onChange={onInputChange}
            placeholder="Rio de Janeiro"
            required
          />
        </div>
        <div className="form-group">
          <label>Estado</label>
          <input 
            type="text" 
            name="state"
            value={form.state}
            onChange={onInputChange}
            placeholder="RJ"
            maxLength="2"
            required
          />
        </div>
      </div>
      <div className="form-group" style={{ marginBottom: '1.25rem' }}>
        <label>CEP</label>
        <input 
          type="text" 
          name="zipCode"
          value={form.zipCode}
          onChange={onInputChange}
          placeholder="20040-003"
          required
        />
      </div>

      <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
        Confirmar e Finalizar Pedido
      </button>
    </form>
  );
}
