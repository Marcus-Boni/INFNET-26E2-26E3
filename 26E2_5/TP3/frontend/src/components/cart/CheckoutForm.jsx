import { Truck, Loader2 } from 'lucide-react';

export function CheckoutForm({
  form,
  onInputChange,
  shippingOptions,
  selectedShippingOption,
  onSelectShippingOption,
  onCalculateShipping,
  isCalculatingShipping,
  onCheckout
}) {
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
        <div className="form-group" style={{ marginBottom: 0 }}>
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
        <div className="form-group" style={{ marginBottom: 0 }}>
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

      {/* Cotação de Frete (Microsserviço) */}
      <div className="shipping-calc-box">
        <div className="shipping-calc-title">
          <Truck size={14} />
          <span>Cotação de Frete (Microsserviço)</span>
        </div>

        <div className="cep-row">
          <input 
            type="text" 
            name="zipCode"
            value={form.zipCode}
            onChange={onInputChange}
            placeholder="CEP (ex: 20040-003)"
            maxLength="9"
            required
          />
          <button 
            type="button" 
            className="btn btn-secondary btn-sm"
            onClick={onCalculateShipping}
            disabled={isCalculatingShipping || !form.zipCode}
            style={{ whiteSpace: 'nowrap' }}
          >
            {isCalculatingShipping ? <Loader2 size={13} className="animate-spin" /> : 'Calcular'}
          </button>
        </div>

        {shippingOptions && shippingOptions.length > 0 && (
          <div className="shipping-options-group">
            {shippingOptions.map(opt => {
              const isSelected = selectedShippingOption?.serviceType === opt.serviceType;
              const isFree = opt.price === 0;

              return (
                <div 
                  key={opt.serviceType}
                  className={`shipping-opt-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => onSelectShippingOption(opt)}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.45rem' }}>
                    <input 
                      type="radio" 
                      name="shippingCarrier"
                      checked={isSelected}
                      onChange={() => onSelectShippingOption(opt)}
                      style={{ width: 'auto', margin: 0 }}
                    />
                    <div>
                      <strong>{opt.carrierName}</strong>
                      <span style={{ display: 'block', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                        ~{opt.estimatedDays} dias úteis
                      </span>
                    </div>
                  </div>

                  <div style={{ fontWeight: '600', color: isFree ? 'var(--color-success)' : 'var(--text-primary)' }}>
                    {isFree ? 'GRÁTIS' : `R$ ${opt.price?.toFixed(2)}`}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      <button 
        type="submit" 
        className="btn btn-primary" 
        style={{ width: '100%', marginTop: '0.5rem' }}
        disabled={!selectedShippingOption}
      >
        Confirmar e Finalizar Pedido
      </button>
      {!selectedShippingOption && (
        <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textAlign: 'center', marginTop: '0.35rem' }}>
          Calcule e selecione uma opção de frete acima para concluir.
        </p>
      )}
    </form>
  );
}
