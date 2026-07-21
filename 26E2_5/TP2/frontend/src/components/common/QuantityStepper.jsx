import { Plus, Minus } from 'lucide-react';

export function QuantityStepper({ value, onIncrement, onDecrement, minDisabled, maxDisabled }) {
  return (
    <div className="quantity-stepper">
      <button 
        className="stepper-btn"
        onClick={onDecrement}
        disabled={minDisabled}
        type="button"
      >
        <Minus size={14} />
      </button>
      <span className="stepper-value">{value}</span>
      <button 
        className="stepper-btn"
        onClick={onIncrement}
        disabled={maxDisabled}
        type="button"
      >
        <Plus size={14} />
      </button>
    </div>
  );
}
