import { XCircle, Truck } from 'lucide-react';

export function OrderActions({ orderId, onCancel, onShip }) {
  return (
    <div className="order-actions">
      <button 
        className="btn btn-danger" 
        onClick={() => onCancel(orderId)}
      >
        <XCircle size={16} /> Cancelar Pedido
      </button>
      <button 
        className="btn btn-success" 
        onClick={() => onShip(orderId)}
      >
        <Truck size={16} /> Enviar Pedido
      </button>
    </div>
  );
}
