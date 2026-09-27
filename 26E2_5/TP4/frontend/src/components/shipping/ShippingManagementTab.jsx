import { useState } from 'react';
import { Truck, RefreshCw, Eye, MapPin } from 'lucide-react';
import { StatusBadge } from '../common/StatusBadge';
import { CarrierBadge } from '../common/CarrierBadge';
import { EmptyState } from '../common/EmptyState';

export function ShippingManagementTab({
  shipments,
  onRefreshShipments,
  onUpdateShipmentStatus,
  onOpenTrackingDetails
}) {
  const [searchTerm, setSearchTerm] = useState('');

  const filteredShipments = shipments.filter(s => {
    if (!searchTerm.trim()) return true;
    const term = searchTerm.toLowerCase().trim();
    return (
      s.trackingNumber?.toLowerCase().includes(term) ||
      s.customerEmail?.toLowerCase().includes(term) ||
      s.destinationCity?.toLowerCase().includes(term) ||
      String(s.orderId).includes(term)
    );
  });

  const totalCount = shipments.length;
  const createdCount = shipments.filter(s => s.status === 'CREATED').length;
  const inTransitCount = shipments.filter(s => ['DISPATCHED', 'IN_TRANSIT', 'OUT_FOR_DELIVERY'].includes(s.status)).length;
  const deliveredCount = shipments.filter(s => s.status === 'DELIVERED').length;

  return (
    <div className="audit-history-container">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1.5rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h2>Central de Logística & Rastreio</h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem' }}>
            Gestão operacional do microsserviço de frete e controle de despachos em tempo real.
          </p>
        </div>

        <button className="btn btn-secondary btn-sm" onClick={onRefreshShipments}>
          <RefreshCw size={13} />
          <span>Atualizar</span>
        </button>
      </div>

      {/* KPI Stats Cards */}
      <div className="logistics-stats">
        <div className="log-stat-card">
          <div className="log-stat-num">{totalCount}</div>
          <div className="log-stat-label">Total de Envios</div>
        </div>

        <div className="log-stat-card">
          <div className="log-stat-num">{createdCount}</div>
          <div className="log-stat-label">Em Separação</div>
        </div>

        <div className="log-stat-card">
          <div className="log-stat-num">{inTransitCount}</div>
          <div className="log-stat-label">Em Trânsito</div>
        </div>

        <div className="log-stat-card">
          <div className="log-stat-num">{deliveredCount}</div>
          <div className="log-stat-label">Entregues</div>
        </div>
      </div>

      {/* Search Input */}
      <div style={{ marginBottom: '1.25rem', maxWidth: '400px' }}>
        <input 
          type="text" 
          placeholder="Pesquisar por rastreio (NX-...), pedido ou cliente..." 
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
        />
      </div>

      {/* Shipments List */}
      {filteredShipments.length === 0 ? (
        <EmptyState 
          icon={Truck}
          message="Nenhum envio registrado ou correspondente à busca." 
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {filteredShipments.map(shipment => (
            <div key={shipment.id || shipment.trackingNumber} className="glass-card" style={{ padding: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '0.75rem', gap: '1rem', flexWrap: 'wrap' }}>
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
                    <span style={{ fontSize: '0.95rem', fontWeight: '600', fontFamily: 'monospace' }}>
                      {shipment.trackingNumber}
                    </span>
                    <CarrierBadge carrier={shipment.carrier} />
                    <StatusBadge status={shipment.status} />
                  </div>
                  <span style={{ fontSize: '0.775rem', color: 'var(--text-secondary)' }}>
                    Pedido: <strong>#{shipment.orderId}</strong> • Cliente: <strong>{shipment.customerEmail}</strong> • Criado em: {new Date(shipment.createdAt).toLocaleString('pt-BR')}
                  </span>
                </div>

                <button 
                  className="btn btn-secondary btn-sm"
                  onClick={() => onOpenTrackingDetails(shipment.orderId, shipment.trackingNumber)}
                >
                  <Eye size={13} />
                  <span>Timeline</span>
                </button>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: '#18181b', border: '1px solid var(--border-color)', padding: '0.6rem 0.85rem', borderRadius: 'var(--radius-md)', fontSize: '0.8rem', flexWrap: 'wrap', gap: '0.5rem' }}>
                <div>
                  <span style={{ color: 'var(--text-muted)' }}>Destino: </span>
                  <MapPin size={11} style={{ display: 'inline', margin: '0 0.15rem' }} />
                  <strong>{shipment.destinationCity} / {shipment.destinationState}</strong> ({shipment.destinationZipCode}) • Frete: R$ {shipment.freightCost?.toFixed(2)}
                </div>

                {/* Status Advancement Simulation Buttons */}
                <div style={{ display: 'flex', gap: '0.35rem' }}>
                  {shipment.status === 'CREATED' && (
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => onUpdateShipmentStatus(shipment.trackingNumber, 'DISPATCHED', 'Objeto despachado para a unidade de tratamento.', 'Hub Central de Cajamar / SP')}
                    >
                      Despachar
                    </button>
                  )}

                  {shipment.status === 'DISPATCHED' && (
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => onUpdateShipmentStatus(shipment.trackingNumber, 'IN_TRANSIT', 'Em trânsito interestadual para a central de destino.', 'Em Trânsito Rodoviário')}
                    >
                      Em Trânsito
                    </button>
                  )}

                  {shipment.status === 'IN_TRANSIT' && (
                    <button 
                      className="btn btn-secondary btn-sm"
                      onClick={() => onUpdateShipmentStatus(shipment.trackingNumber, 'OUT_FOR_DELIVERY', 'Objeto saiu para entrega ao destinatário.', `CDD ${shipment.destinationCity} / ${shipment.destinationState}`)}
                    >
                      Saiu p/ Entrega
                    </button>
                  )}

                  {shipment.status === 'OUT_FOR_DELIVERY' && (
                    <button 
                      className="btn btn-primary btn-sm"
                      onClick={() => onUpdateShipmentStatus(shipment.trackingNumber, 'DELIVERED', 'Objeto entregue ao destinatário com sucesso.', `${shipment.destinationCity} / ${shipment.destinationState}`)}
                    >
                      Confirmar Entrega
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
