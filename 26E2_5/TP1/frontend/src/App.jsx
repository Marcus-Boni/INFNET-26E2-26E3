import { useState, useEffect } from 'react';
import toast, { Toaster } from 'react-hot-toast';
import { 
  ShoppingCart, 
  Package, 
  Trash2, 
  Truck, 
  Plus, 
  Minus, 
  XCircle 
} from 'lucide-react';
import './App.css';

const API_BASE = 'http://localhost:8080/api';

function App() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [cart, setCart] = useState([]);
  const [activeTab, setActiveTab] = useState('catalog'); // 'catalog' | 'orders'
  
  // Stepper values per product: { [productId]: quantity }
  const [quantities, setQuantities] = useState({});
  
  // Checkout Form State
  const [form, setForm] = useState({
    customerEmail: '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
  });

  // Notification handled by react-hot-toast

  // Fetch initial data
  useEffect(() => {
    fetchProducts();
    fetchOrders();
  }, []);

  const fetchProducts = async () => {
    try {
      const res = await fetch(`${API_BASE}/products`);
      if (res.ok) {
        const data = await res.json();
        setProducts(data);
        // Initialize quantities stepper to 1 for in-stock items
        const newQuantities = {};
        data.forEach(p => {
          newQuantities[p.id] = p.stock > 0 ? 1 : 0;
        });
        setQuantities(newQuantities);
      } else {
        showToast('Falha ao carregar produtos do catálogo.', 'error');
      }
    } catch (err) {
      showToast('Não foi possível conectar ao servidor backend.', 'error');
    }
  };

  const fetchOrders = async () => {
    try {
      const res = await fetch(`${API_BASE}/orders`);
      if (res.ok) {
        const data = await res.json();
        // Sort orders by id descending (newest first)
        data.sort((a, b) => b.id - a.id);
        setOrders(data);
      }
    } catch (err) {
      console.error('Erro ao buscar pedidos:', err);
    }
  };

  const showToast = (message, type = 'success') => {
    if (type === 'success') {
      toast.success(message);
    } else {
      toast.error(message);
    }
  };

  // Quantity Stepper Handlers
  const handleIncrement = (productId, stock) => {
    setQuantities(prev => {
      const currentQty = prev[productId] || 1;
      const inCart = getCartQty(productId);
      const available = stock - inCart;
      
      if (currentQty >= available) {
        showToast(`Limite de estoque atingido para este produto.`, 'error');
        return prev;
      }
      return { ...prev, [productId]: currentQty + 1 };
    });
  };

  const handleDecrement = (productId) => {
    setQuantities(prev => {
      const currentQty = prev[productId] || 1;
      if (currentQty <= 1) return prev;
      return { ...prev, [productId]: currentQty - 1 };
    });
  };

  const getCartQty = (productId) => {
    const item = cart.find(i => i.product.id === productId);
    return item ? item.quantity : 0;
  };

  // Cart operations
  const addToCart = (product) => {
    const qtyToAdd = quantities[product.id] || 1;
    if (qtyToAdd <= 0) return;

    const inCart = getCartQty(product.id);
    if (inCart + qtyToAdd > product.stock) {
      showToast(`Estoque insuficiente! Você já tem ${inCart} no carrinho e o estoque é ${product.stock}.`, 'error');
      return;
    }

    setCart(prev => {
      const existingIndex = prev.findIndex(item => item.product.id === product.id);
      if (existingIndex > -1) {
        const newCart = [...prev];
        newCart[existingIndex].quantity += qtyToAdd;
        return newCart;
      } else {
        return [...prev, { product, quantity: qtyToAdd }];
      }
    });

    showToast(`Adicionado ${qtyToAdd}x "${product.name}" ao carrinho!`, 'success');
    
    // Reset stepper to 1 or remaining available stock
    const remainingStock = product.stock - (inCart + qtyToAdd);
    setQuantities(prev => ({
      ...prev,
      [product.id]: remainingStock > 0 ? 1 : 0
    }));
  };

  const removeFromCart = (productId) => {
    const itemToRemove = cart.find(i => i.product.id === productId);
    if (!itemToRemove) return;

    setCart(prev => prev.filter(item => item.product.id !== productId));
    showToast(`Removido "${itemToRemove.product.name}" do carrinho.`, 'success');
    
    // Reset stepper to 1
    setQuantities(prev => ({
      ...prev,
      [productId]: 1
    }));
  };

  const calculateCartTotal = () => {
    return cart.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  };

  // Form input handler
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  // Checkout submission
  const handleCheckout = async (e) => {
    e.preventDefault();
    if (cart.length === 0) {
      showToast('Seu carrinho está vazio.', 'error');
      return;
    }

    const payload = {
      customerEmail: form.customerEmail,
      street: form.street,
      city: form.city,
      state: form.state,
      zipCode: form.zipCode,
      items: cart.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }))
    };

    try {
      const res = await fetch(`${API_BASE}/orders`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      const data = await res.json();

      if (res.ok) {
        showToast('Pedido realizado com sucesso!', 'success');
        setCart([]);
        setForm({
          customerEmail: '',
          street: '',
          city: '',
          state: '',
          zipCode: '',
        });
        fetchProducts(); // Refresh stocks
        fetchOrders();    // Refresh history
        setActiveTab('orders'); // Jump to orders view
      } else {
        // Display validation errors or domain error
        const errMsg = data.error || 'Erro ao realizar o pedido.';
        showToast(errMsg, 'error');
      }
    } catch (err) {
      showToast('Erro ao se conectar ao servidor para checkout.', 'error');
    }
  };

  // Shipping action
  const shipOrder = async (orderId) => {
    try {
      const res = await fetch(`${API_BASE}/orders/${orderId}/ship`, {
        method: 'PUT'
      });
      const data = await res.json();
      if (res.ok) {
        showToast(`Pedido #${orderId} enviado com sucesso!`, 'success');
        fetchOrders();
      } else {
        showToast(data.error || 'Erro ao enviar pedido.', 'error');
      }
    } catch (err) {
      showToast('Erro ao conectar ao servidor para enviar o pedido.', 'error');
    }
  };

  // Cancel action
  const cancelOrder = async (orderId) => {
    try {
      const res = await fetch(`${API_BASE}/orders/${orderId}/cancel`, {
        method: 'PUT'
      });
      const data = await res.json();
      if (res.ok) {
        showToast(`Pedido #${orderId} cancelado. Estoque devolvido!`, 'success');
        fetchOrders();
        fetchProducts(); // Refresh stock quantities restored
      } else {
        showToast(data.error || 'Erro ao cancelar pedido.', 'error');
      }
    } catch (err) {
      showToast('Erro ao conectar ao servidor para cancelar o pedido.', 'error');
    }
  };

  // Helper for stock badges
  const renderStockBadge = (stock) => {
    const inCartQty = getCartQty;
    if (stock === 0) return <span className="badge badge-danger">Esgotado</span>;
    if (stock <= 5) return <span className="badge badge-warning">Apenas {stock} restam</span>;
    return <span className="badge badge-success">{stock} em estoque</span>;
  };

  const renderStatusBadge = (status) => {
    switch (status) {
      case 'PENDING':
        return <span className="badge badge-pending">Pendente</span>;
      case 'SHIPPED':
        return <span className="badge badge-success">Enviado</span>;
      case 'CANCELLED':
        return <span className="badge badge-danger">Cancelado</span>;
      default:
        return <span className="badge">{status}</span>;
    }
  };

  return (
    <>
      {/* Toast Notification Library */}
      <Toaster position="top-right" toastOptions={{
        style: {
          background: 'var(--bg-card)',
          color: 'var(--text-primary)',
          border: '1px solid var(--border-color)',
          borderRadius: 'var(--radius-md)',
          backdropFilter: 'var(--card-blur)',
          fontFamily: 'var(--font-sans)',
        },
        success: {
          iconTheme: {
            primary: 'var(--color-success)',
            secondary: '#fff',
          },
        },
        error: {
          iconTheme: {
            primary: 'var(--color-danger)',
            secondary: '#fff',
          },
        },
      }} />

      <div className="container animate-fade-in">
        {/* Header Panel */}
      <header className="header">
        <div className="brand-section">
          <h1>Nexus Store</h1>
          <span className="brand-subtitle">Projeto de Bloco: Engenharia de Softwares Escaláveis</span>
        </div>
        <nav className="tabs-navigation">
          <button 
            className={`tab-btn ${activeTab === 'catalog' ? 'active' : ''}`}
            onClick={() => setActiveTab('catalog')}
          >
            <ShoppingCart size={16} /> Catálogo
          </button>
          <button 
            className={`tab-btn ${activeTab === 'orders' ? 'active' : ''}`}
            onClick={() => setActiveTab('orders')}
          >
            <Package size={16} /> Pedidos 
            {orders.length > 0 && <span className="tab-badge">{orders.length}</span>}
          </button>
        </nav>
      </header>

      {/* Main Content Area */}
      {activeTab === 'catalog' ? (
        <div className="main-grid">
          {/* Products Grid */}
          <div className="products-grid-container">
            <h2 style={{ marginBottom: '1.5rem' }}>Produtos Disponíveis</h2>
            <div className="products-grid">
              {products.map(product => {
                const inCartQty = getCartQty(product.id);
                const availableStock = product.stock - inCartQty;
                const currentStepperVal = quantities[product.id] || 0;
                
                return (
                  <div key={product.id} className="glass-card product-card">
                    <div className="product-info">
                      <div className="product-header">
                        <h3 className="product-name">{product.name}</h3>
                        {renderStockBadge(product.stock)}
                      </div>
                      <p className="product-description">{product.description}</p>
                      <div className="product-meta">
                        <span className="product-price">
                          R$ {product.price.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                        </span>
                        {inCartQty > 0 && (
                          <span className="product-stock" style={{ color: 'var(--primary)' }}>
                            {inCartQty} no carrinho
                          </span>
                        )}
                      </div>
                    </div>

                    {/* Stock action control */}
                    <div className="purchase-controls">
                      {product.stock > 0 && availableStock > 0 ? (
                        <>
                          <div className="quantity-stepper">
                            <button 
                              className="stepper-btn"
                              onClick={() => handleDecrement(product.id)}
                              disabled={currentStepperVal <= 1}
                              title="Diminuir"
                            >
                              <Minus size={14} />
                            </button>
                            <span className="stepper-value">{currentStepperVal}</span>
                            <button 
                              className="stepper-btn"
                              onClick={() => handleIncrement(product.id, product.stock)}
                              disabled={currentStepperVal >= availableStock}
                              title="Aumentar"
                            >
                              <Plus size={14} />
                            </button>
                          </div>
                          <button 
                            className="btn btn-primary"
                            style={{ flexGrow: 1 }}
                            onClick={() => addToCart(product)}
                          >
                            <ShoppingCart size={16} /> Adicionar
                          </button>
                        </>
                      ) : (
                        <button className="btn btn-secondary" style={{ width: '100%' }} disabled>
                          {product.stock === 0 ? 'Indisponível' : 'Estoque Limite no Carrinho'}
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Cart Sidebar Panel */}
          <aside className="glass-card cart-card">
            <div className="cart-title-row">
              <h3>Carrinho</h3>
              <span className="badge badge-success">{cart.reduce((s, i) => s + i.quantity, 0)} itens</span>
            </div>

            {cart.length === 0 ? (
              <div className="empty-state" style={{ padding: '2rem 1rem' }}>
                <ShoppingCart size={40} style={{ margin: '0 auto 1rem', display: 'block', opacity: 0.5 }} />
                <p>Seu carrinho está vazio. Adicione produtos para começar.</p>
              </div>
            ) : (
              <>
                <div className="cart-items-list">
                  {cart.map(item => (
                    <div key={item.product.id} className="cart-item">
                      <div>
                        <div className="cart-item-name">{item.product.name}</div>
                        <span className="cart-item-qty">{item.quantity}x • R$ {item.product.price.toFixed(2)}</span>
                      </div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span className="cart-item-price">R$ {(item.product.price * item.quantity).toFixed(2)}</span>
                        <button 
                          className="cart-item-remove"
                          onClick={() => removeFromCart(item.product.id)}
                          title="Remover"
                          style={{ display: 'flex', alignItems: 'center' }}
                        >
                          <Trash2 size={16} />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>

                <div className="cart-summary">
                  <div className="summary-row">
                    <span>Subtotal</span>
                    <span>R$ {calculateCartTotal().toFixed(2)}</span>
                  </div>
                  <div className="summary-row">
                    <span>Frete</span>
                    <span style={{ color: 'var(--color-success)', fontWeight: 'bold' }}>GRÁTIS</span>
                  </div>
                  <div className="summary-row total">
                    <span>Total</span>
                    <span>R$ {calculateCartTotal().toFixed(2)}</span>
                  </div>
                </div>

                {/* Shipping & Order Confirmation Form */}
                <form onSubmit={handleCheckout}>
                  <h4 style={{ marginBottom: '0.75rem', fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
                    Endereço de Entrega
                  </h4>
                  <div className="form-group" style={{ marginBottom: '0.75rem' }}>
                    <label>E-mail do Cliente</label>
                    <input 
                      type="email" 
                      name="customerEmail"
                      value={form.customerEmail}
                      onChange={handleInputChange}
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
                      onChange={handleInputChange}
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
                        onChange={handleInputChange}
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
                        onChange={handleInputChange}
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
                      onChange={handleInputChange}
                      placeholder="20040-003"
                      required
                    />
                  </div>

                  <button type="submit" className="btn btn-primary" style={{ width: '100%' }}>
                    Confirmar e Finalizar Pedido
                  </button>
                </form>
              </>
            )}
          </aside>
        </div>
      ) : (
        /* Orders History View */
        <div className="orders-list-container">
          <h2 style={{ marginBottom: '1.5rem' }}>Histórico de Pedidos</h2>
          {orders.length === 0 ? (
            <div className="glass-card empty-state">
              <Package size={48} style={{ margin: '0 auto 1rem', display: 'block', opacity: 0.5 }} />
              <p>Nenhum pedido foi realizado ainda.</p>
            </div>
          ) : (
            <div className="orders-list">
              {orders.map(order => (
                <div key={order.id} className="glass-card order-card">
                  <div className="order-header">
                    <div className="order-title-block">
                      <span className="order-number">Pedido #{order.id}</span>
                      <span className="order-date">
                        Cliente: <strong>{order.customerEmail}</strong> • Realizado em:{' '}
                        {new Date(order.createdAt).toLocaleString('pt-BR')}
                      </span>
                    </div>
                    <div>
                      {renderStatusBadge(order.status)}
                    </div>
                  </div>

                  <div className="order-details-grid">
                    {/* Items Table */}
                    <div>
                      <table className="order-items-table">
                        <thead>
                          <tr>
                            <th>Produto</th>
                            <th style={{ textAlign: 'center' }}>Qtd</th>
                            <th style={{ textAlign: 'right' }}>Preço Unit.</th>
                            <th style={{ textAlign: 'right' }}>Total</th>
                          </tr>
                        </thead>
                        <tbody>
                          {order.items.map(item => (
                            <tr key={item.id}>
                              <td>{item.productName}</td>
                              <td style={{ textAlign: 'center' }}>{item.quantity}</td>
                              <td style={{ textAlign: 'right' }}>
                                R$ {item.unitPrice.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                              </td>
                              <td style={{ textAlign: 'right', fontWeight: '500' }}>
                                R$ {(item.unitPrice * item.quantity).toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                              </td>
                            </tr>
                          ))}
                          <tr>
                            <td colSpan="3" style={{ textAlign: 'right', fontWeight: 'bold', border: 'none', paddingTop: '1rem' }}>
                              Total do Pedido:
                            </td>
                            <td style={{ textAlign: 'right', fontWeight: 'bold', fontSize: '1.1rem', color: 'var(--text-primary)', border: 'none', paddingTop: '1rem' }}>
                              R$ {order.totalPrice.toLocaleString('pt-BR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>

                    {/* Shipping Address */}
                    <div className="order-address-box">
                      <h4 className="order-address-title">Endereço de Entrega</h4>
                      <p style={{ color: 'var(--text-primary)', marginBottom: '0.25rem' }}>{order.shippingAddress.street}</p>
                      <p>
                        {order.shippingAddress.city} - {order.shippingAddress.state}
                      </p>
                      <p style={{ color: 'var(--text-muted)' }}>CEP: {order.shippingAddress.zipCode}</p>
                    </div>
                  </div>

                  {/* Contextual actions based on status */}
                  {order.status === 'PENDING' && (
                    <div className="order-actions">
                      <button 
                        className="btn btn-danger" 
                        onClick={() => cancelOrder(order.id)}
                      >
                        <XCircle size={16} /> Cancelar Pedido
                      </button>
                      <button 
                        className="btn btn-success" 
                        onClick={() => shipOrder(order.id)}
                      >
                        <Truck size={16} /> Enviar Pedido
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
    </>
  );
}

export default App;
