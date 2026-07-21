import { StatusBadge } from '../common/StatusBadge';
import { OrderItemsTable } from './OrderItemsTable';
import { OrderAddress } from './OrderAddress';
import { OrderActions } from './OrderActions';

export function OrderCard({ order, onCancelOrder, onShipOrder }) {
  return (
    <div className="glass-card order-card">
      <div className="order-header">
        <div className="order-title-block">
          <span className="order-number">Pedido #{order.id}</span>
          <span className="order-date">
            Cliente: <strong>{order.customerEmail}</strong> • Realizado em:{' '}
            {new Date(order.createdAt).toLocaleString('pt-BR')}
          </span>
        </div>
        <div>
          <StatusBadge status={order.status} />
        </div>
      </div>

      <div className="order-details-grid">
        <div>
          <OrderItemsTable 
            items={order.items} 
            totalPrice={order.totalPrice} 
          />
        </div>

        <OrderAddress address={order.shippingAddress} />
      </div>

      {order.status === 'PENDING' && (
        <OrderActions 
          orderId={order.id} 
          onCancel={onCancelOrder} 
          onShip={onShipOrder} 
        />
      )}
    </div>
  );
}
