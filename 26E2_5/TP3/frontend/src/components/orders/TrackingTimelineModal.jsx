import { X, Check, Clock, MapPin, AlertTriangle } from 'lucide-react';
import { CarrierBadge } from '../common/CarrierBadge';
import { StatusBadge } from '../common/StatusBadge';

export function TrackingTimelineModal({ shipment, onClose, isLoading, error }) {
  const steps = [
    { key: 'CREATED', label: 'Criado' },
    { key: 'DISPATCHED', label: 'Despachado' },
    { key: 'IN_TRANSIT', label: 'Em Trânsito' },
    { key: 'OUT_FOR_DELIVERY', label: 'Saiu p/ Entrega' },
    { key: 'DELIVERED', label: 'Entregue' }
  ];

  const getStepIndex = (status) => {
    switch (status) {
      case 'CREATED': return 0;
      case 'DISPATCHED': return 1;
      case 'IN_TRANSIT': return 2;
      case 'OUT_FOR_DELIVERY': return 3;
      case 'DELIVERED':
      case 'SHIPPED': return 4;
      default: return 0;
    }
  };

  const currentStepIdx = shipment ? getStepIndex(shipment.status) : 0;
  const isCancelled = shipment?.status === 'CANCELLED';

  return (
    <div className="modal-overlay animate-fade-in" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <div>
            <h3 style={{ fontSize: '1.05rem', fontWeight: '600' }}>
              Rastreamento em Tempo Real
            </h3>
            {shipment && (
              <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Código: <strong style={{ color: 'var(--text-primary)', fontFamily: 'monospace' }}>{shipment.trackingNumber}</strong>
              </span>
            )}
          </div>
          <button className="modal-close-btn" onClick={onClose}>
            <X size={18} />
          </button>
        </div>

        {isLoading ? (
          <div style={{ padding: '2.5rem', textAlign: 'center', color: 'var(--text-muted)' }}>
            <Clock size={24} className="animate-spin" style={{ margin: '0 auto 0.5rem' }} />
            <p style={{ fontSize: '0.85rem' }}>Consultando microsserviço de logística...</p>
          </div>
        ) : error ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: 'var(--color-danger)' }}>
            <AlertTriangle size={24} style={{ margin: '0 auto 0.5rem' }} />
            <p style={{ fontSize: '0.85rem' }}>{error}</p>
          </div>
        ) : shipment ? (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', paddingBottom: '0.75rem', borderBottom: '1px solid var(--border-color)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <CarrierBadge carrier={shipment.carrier} />
                <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                  Prazo: ~{shipment.estimatedDeliveryDays} dias úteis
                </span>
              </div>
              <StatusBadge status={shipment.status} />
            </div>

            {/* Stepper Progress Bar */}
            {!isCancelled ? (
              <div className="tracking-steps">
                {steps.map((step, idx) => {
                  const isCompleted = idx < currentStepIdx || (idx === 4 && currentStepIdx === 4);
                  const isActive = idx === currentStepIdx && currentStepIdx !== 4;

                  return (
                    <div 
                      key={step.key} 
                      className={`step-item ${isCompleted ? 'completed' : ''} ${isActive ? 'active' : ''}`}
                    >
                      <div className="step-dot">
                        {isCompleted ? <Check size={12} /> : idx + 1}
                      </div>
                      <span className="step-name">{step.label}</span>
                    </div>
                  );
                })}
              </div>
            ) : (
              <div style={{ padding: '0.75rem', background: 'var(--bg-danger)', border: '1px solid var(--border-danger)', borderRadius: 'var(--radius-md)', color: 'var(--color-danger)', textAlign: 'center', margin: '1rem 0', fontSize: '0.85rem' }}>
                Envio cancelado no microsserviço de logística.
              </div>
            )}

            {/* Timeline Feed */}
            <h4 style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '1.5rem', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Histórico de Movimentações
            </h4>

            {shipment.events && shipment.events.length > 0 ? (
              <div className="timeline-feed">
                {shipment.events.slice().reverse().map((ev, i) => (
                  <div key={ev.id || i} className="timeline-feed-item">
                    <span className="timeline-feed-time">
                      {new Date(ev.timestamp).toLocaleString('pt-BR')}
                    </span>
                    <span className="timeline-feed-msg">{ev.message}</span>
                    <span className="timeline-feed-loc">
                      <MapPin size={11} style={{ display: 'inline', marginRight: '0.2rem' }} />
                      {ev.location}
                    </span>
                  </div>
                ))}
              </div>
            ) : (
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '0.5rem' }}>
                Nenhum evento de rastreio registrado ainda.
              </p>
            )}
          </div>
        ) : null}
      </div>
    </div>
  );
}
