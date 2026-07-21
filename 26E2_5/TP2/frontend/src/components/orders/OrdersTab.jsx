import { Package } from 'lucide-react';
import { OrderCard } from './OrderCard';
import { EmptyState } from '../common/EmptyState';

export function OrdersTab({ orders, onCancelOrder, onShipOrder }) {
  return (
    <div className="orders-list-container">
      <h2 style={{ marginBottom: '1.5rem' }}>Histórico de Pedidos</h2>
      {orders.length === 0 ? (
        <EmptyState 
          icon={Package} 
          message="Nenhum pedido foi realizado ainda." 
        />
      ) : (
        <div className="orders-list">
          {orders.map(order => (
            <OrderCard 
              key={order.id} 
              order={order} 
              onCancelOrder={onCancelOrder} 
              onShipOrder={onShipOrder} 
            />
          ))}
        </div>
      )}
    </div>
  );
}
