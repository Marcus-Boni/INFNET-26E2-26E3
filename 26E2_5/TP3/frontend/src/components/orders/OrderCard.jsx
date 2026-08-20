import { useState } from 'react';
import { Copy, Check, Truck } from 'lucide-react';
import { StatusBadge } from '../common/StatusBadge';
import { CarrierBadge } from '../common/CarrierBadge';
import { OrderItemsTable } from './OrderItemsTable';
import { OrderAddress } from './OrderAddress';
import { OrderActions } from './OrderActions';

export function OrderCard({ order, onCancelOrder, onShipOrder, onOpenTracking }) {
  const [copied, setCopied] = useState(false);

  const handleCopyTracking = (e) => {
    e.stopPropagation();
    if (order.trackingNumber) {
      navigator.clipboard.writeText(order.trackingNumber);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  return (
    <div className="glass-card order-card">
      <div className="order-header">
        <div className="order-title-block">
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <span className="order-number">Pedido #{order.id}</span>
            <CarrierBadge carrier={order.shippingCarrier} />
          </div>
          <span className="order-date">
            Cliente: <strong>{order.customerEmail}</strong> • Realizado em:{' '}
            {new Date(order.createdAt).toLocaleString('pt-BR')}
          </span>

          {order.trackingNumber && (
            <div style={{ marginTop: '0.25rem' }}>
              <span 
                className="tracking-tag"
                onClick={handleCopyTracking}
                title="Copiar código de rastreamento"
              >
                <Truck size={12} />
                {order.trackingNumber}
                {copied ? <Check size={11} color="var(--color-success)" /> : <Copy size={11} />}
              </span>
            </div>
          )}
        </div>

        <div>
          <StatusBadge status={order.status} />
        </div>
      </div>

      <div className="order-details-grid">
        <div>
          <OrderItemsTable 
            items={order.items || []} 
            itemsTotal={order.itemsTotal || 0}
            shippingCost={order.shippingCost}
            totalPrice={order.totalPrice || 0} 
          />
        </div>

        <OrderAddress address={order.shippingAddress} />
      </div>

      <OrderActions 
        orderId={order.id} 
        onCancel={order.status === 'PENDING' ? onCancelOrder : null} 
        onShip={order.status === 'PENDING' ? onShipOrder : null}
        onOpenTracking={onOpenTracking}
        trackingNumber={order.trackingNumber}
      />
    </div>
  );
}
