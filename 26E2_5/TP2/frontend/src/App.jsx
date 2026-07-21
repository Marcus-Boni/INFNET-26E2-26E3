import { useState, useEffect } from 'react';
import toast, { Toaster } from 'react-hot-toast';

import { Header } from './components/layout/Header';
import { CatalogTab } from './components/catalog/CatalogTab';
import { CartSidebar } from './components/cart/CartSidebar';
import { OrdersTab } from './components/orders/OrdersTab';
import { AuditTab } from './components/audit/AuditTab';

import {
  fetchProductsApi,
  fetchOrdersApi,
  fetchAuditLogsApi,
  createOrderApi,
  shipOrderApi,
  cancelOrderApi,
  adjustStockApi
} from './services/api';

import './App.css';

function App() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [cart, setCart] = useState([]);
  const [activeTab, setActiveTab] = useState('catalog');

  // Search & Filter State
  const [searchTerm, setSearchTerm] = useState('');
  const [auditEntityFilter, setAuditEntityFilter] = useState('ALL');

  // Stepper values per product: { [productId]: quantity }
  const [quantities, setQuantities] = useState({});

  // Stock Adjustment Modal / State
  const [selectedProductForStock, setSelectedProductForStock] = useState(null);

  // Checkout Form State
  const [form, setForm] = useState({
    customerEmail: '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
  });

  // Fetch initial data
  useEffect(() => {
    loadProducts();
    loadOrders();
    loadAuditLogs();
  }, []);

  const showToast = (message, type = 'success') => {
    if (type === 'success') {
      toast.success(message);
    } else {
      toast.error(message);
    }
  };

  const loadProducts = async (query = '') => {
    try {
      const data = await fetchProductsApi(query);
      setProducts(data);
      const newQuantities = {};
      data.forEach(p => {
        newQuantities[p.id] = p.stock > 0 ? 1 : 0;
      });
      setQuantities(newQuantities);
    } catch (err) {
      showToast(err.message || 'Não foi possível conectar ao servidor backend.', 'error');
    }
  };

  const loadOrders = async () => {
    try {
      const data = await fetchOrdersApi();
      setOrders(data);
    } catch (err) {
      console.error('Erro ao buscar pedidos:', err);
    }
  };

  const loadAuditLogs = async (entityName = '') => {
    try {
      const data = await fetchAuditLogsApi(entityName);
      setAuditLogs(data);
    } catch (err) {
      console.error('Erro ao buscar logs de auditoria:', err);
    }
  };

  const handleSearchSubmit = (e) => {
    e.preventDefault();
    loadProducts(searchTerm);
  };

  const handleAuditFilterChange = (entity) => {
    setAuditEntityFilter(entity);
    loadAuditLogs(entity);
  };

  const getCartQty = (productId) => {
    const item = cart.find(i => i.product.id === productId);
    return item ? item.quantity : 0;
  };

  // Quantity Stepper Handlers
  const handleIncrement = (productId, stock) => {
    setQuantities(prev => {
      const currentQty = prev[productId] || 1;
      const inCart = getCartQty(productId);
      const available = stock - inCart;

      if (currentQty >= available) {
        showToast('Limite de estoque atingido para este produto.', 'error');
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

    setQuantities(prev => ({
      ...prev,
      [productId]: 1
    }));
  };

  const calculateCartTotal = () => {
    return cart.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

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
      await createOrderApi(payload);
      showToast('Pedido realizado com sucesso! Histórico gravado.', 'success');
      setCart([]);
      setForm({
        customerEmail: '',
        street: '',
        city: '',
        state: '',
        zipCode: '',
      });
      loadProducts();
      loadOrders();
      loadAuditLogs();
      setActiveTab('orders');
    } catch (err) {
      showToast(err.message || 'Erro ao realizar o pedido.', 'error');
    }
  };

  const shipOrder = async (orderId) => {
    try {
      await shipOrderApi(orderId);
      showToast(`Pedido #${orderId} enviado! Histórico de status atualizado.`, 'success');
      loadOrders();
      loadAuditLogs();
    } catch (err) {
      showToast(err.message || 'Erro ao enviar pedido.', 'error');
    }
  };

  const cancelOrder = async (orderId) => {
    try {
      await cancelOrderApi(orderId);
      showToast(`Pedido #${orderId} cancelado. Estoque estornado e auditado!`, 'success');
      loadOrders();
      loadProducts();
      loadAuditLogs();
    } catch (err) {
      showToast(err.message || 'Erro ao cancelar pedido.', 'error');
    }
  };

  const handleAdjustStockSubmit = async (productId, delta) => {
    try {
      await adjustStockApi(productId, delta);
      showToast(`Estoque do produto #${productId} ajustado com sucesso!`, 'success');
      loadProducts();
      loadAuditLogs();
      setSelectedProductForStock(null);
    } catch (err) {
      showToast(err.message || 'Erro ao ajustar estoque.', 'error');
    }
  };

  const handleToggleStockPanel = (productId) => {
    setSelectedProductForStock(prev => (prev === productId ? null : productId));
  };

  return (
    <>
      <Toaster 
        position="top-right" 
        toastOptions={{
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
        }} 
      />

      <div className="container animate-fade-in">
        <Header 
          activeTab={activeTab}
          onTabChange={(tab) => {
            setActiveTab(tab);
            if (tab === 'history') {
              loadAuditLogs(auditEntityFilter);
            }
          }}
          ordersCount={orders.length}
          auditLogsCount={auditLogs.length}
        />

        {activeTab === 'catalog' && (
          <CatalogTab 
            products={products}
            searchTerm={searchTerm}
            onSearchTermChange={setSearchTerm}
            onSearchSubmit={handleSearchSubmit}
            getCartQty={getCartQty}
            quantities={quantities}
            selectedProductForStock={selectedProductForStock}
            onToggleStockPanel={handleToggleStockPanel}
            onAdjustStock={handleAdjustStockSubmit}
            onIncrement={handleIncrement}
            onDecrement={handleDecrement}
            onAddToCart={addToCart}
            cartSidebarComponent={
              <CartSidebar 
                cart={cart}
                onRemoveFromCart={removeFromCart}
                cartTotal={calculateCartTotal()}
                form={form}
                onInputChange={handleInputChange}
                onCheckout={handleCheckout}
              />
            }
          />
        )}

        {activeTab === 'orders' && (
          <OrdersTab 
            orders={orders}
            onCancelOrder={cancelOrder}
            onShipOrder={shipOrder}
          />
        )}

        {activeTab === 'history' && (
          <AuditTab 
            auditLogs={auditLogs}
            currentFilter={auditEntityFilter}
            onFilterChange={handleAuditFilterChange}
            onRefresh={() => loadAuditLogs(auditEntityFilter)}
          />
        )}
      </div>
    </>
  );
}

export default App;
