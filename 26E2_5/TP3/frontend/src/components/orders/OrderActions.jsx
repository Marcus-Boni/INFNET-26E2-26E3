import { Send, XCircle, Truck } from 'lucide-react';

export function OrderActions({ orderId, onCancel, onShip, onOpenTracking, trackingNumber }) {
  return (
    <div className="order-actions">
      {trackingNumber && (
        <button 
          type="button" 
          className="btn btn-secondary btn-sm"
          onClick={() => onOpenTracking(orderId, trackingNumber)}
        >
          <Truck size={13} />
          <span>Rastrear</span>
        </button>
      )}

      {onCancel && (
        <button 
          type="button" 
          className="btn btn-danger btn-sm"
          onClick={() => onCancel(orderId)}
        >
          <XCircle size={13} />
          <span>Cancelar Pedido</span>
        </button>
      )}

      {onShip && (
        <button 
          type="button" 
          className="btn btn-primary btn-sm"
          onClick={() => onShip(orderId)}
        >
          <Send size={13} />
          <span>Despachar Pedido</span>
        </button>
      )}
    </div>
  );
}
