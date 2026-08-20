import { useState, useEffect } from 'react';
import toast, { Toaster } from 'react-hot-toast';

import { Header } from './components/layout/Header';
import { CatalogTab } from './components/catalog/CatalogTab';
import { CartSidebar } from './components/cart/CartSidebar';
import { OrdersTab } from './components/orders/OrdersTab';
import { ShippingManagementTab } from './components/shipping/ShippingManagementTab';
import { AuditTab } from './components/audit/AuditTab';
import { TrackingTimelineModal } from './components/orders/TrackingTimelineModal';

import {
  fetchProductsApi,
  fetchOrdersApi,
  fetchAuditLogsApi,
  createOrderApi,
  shipOrderApi,
  cancelOrderApi,
  adjustStockApi,
  calculateShippingApi,
  fetchOrderTrackingApi,
  fetchShipmentByTrackingApi,
  fetchAllShipmentsApi,
  updateShipmentStatusApi
} from './services/api';

import './App.css';

function App() {
  const [products, setProducts] = useState([]);
  const [orders, setOrders] = useState([]);
  const [shipments, setShipments] = useState([]);
  const [auditLogs, setAuditLogs] = useState([]);
  const [cart, setCart] = useState([]);
  const [activeTab, setActiveTab] = useState('catalog');

  // Search & Filter State
  const [searchTerm, setSearchTerm] = useState('');
  const [auditEntityFilter, setAuditEntityFilter] = useState('ALL');

  // Stepper values per product: { [productId]: quantity }
  const [quantities, setQuantities] = useState({});

  // Stock Adjustment Modal
  const [selectedProductForStock, setSelectedProductForStock] = useState(null);

  // Checkout Form & Shipping Calculation State
  const [form, setForm] = useState({
    customerEmail: '',
    street: '',
    city: '',
    state: '',
    zipCode: '',
  });

  const [shippingOptions, setShippingOptions] = useState([]);
  const [selectedShippingOption, setSelectedShippingOption] = useState(null);
  const [isCalculatingShipping, setIsCalculatingShipping] = useState(false);

  // Tracking Timeline Modal State
  const [isTrackingModalOpen, setIsTrackingModalOpen] = useState(false);
  const [activeTrackingShipment, setActiveTrackingShipment] = useState(null);
  const [isTrackingLoading, setIsTrackingLoading] = useState(false);
  const [trackingError, setTrackingError] = useState(null);

  // Fetch initial data
  useEffect(() => {
    loadProducts();
    loadOrders();
    loadShipments();
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

  const loadShipments = async () => {
    try {
      const data = await fetchAllShipmentsApi();
      setShipments(data);
    } catch (err) {
      console.warn('Microsserviço de frete pode estar iniciando:', err.message);
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

  const calculateCartItemsTotal = () => {
    return cart.reduce((sum, item) => sum + (item.product.price * item.quantity), 0);
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

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  // Freight Calculation via Microservice
  const handleCalculateShipping = async () => {
    if (!form.zipCode || form.zipCode.trim().length < 5) {
      showToast('Digite um CEP válido com pelo menos 5 dígitos.', 'error');
      return;
    }

    setIsCalculatingShipping(true);
    try {
      const totalItems = cart.reduce((sum, i) => sum + i.quantity, 0);
      const orderTotal = calculateCartItemsTotal();
      const res = await calculateShippingApi(form.zipCode, totalItems, orderTotal);

      setShippingOptions(res.options || []);
      if (res.options && res.options.length > 0) {
        // Seleciona opção padrão (STANDARD) ou a primeira
        const standardOpt = res.options.find(o => o.serviceType === 'STANDARD') || res.options[0];
        setSelectedShippingOption(standardOpt);
        showToast(`Frete calculado para ${res.destinationRegion}!`, 'success');
      }
    } catch (err) {
      showToast(err.message || 'Erro ao calcular frete no microsserviço.', 'error');
    } finally {
      setIsCalculatingShipping(false);
    }
  };

  const handleCheckout = async (e) => {
    e.preventDefault();
    if (cart.length === 0) {
      showToast('Seu carrinho está vazio.', 'error');
      return;
    }

    if (!selectedShippingOption) {
      showToast('Por favor, calcule e selecione uma opção de frete antes de continuar.', 'error');
      return;
    }

    const payload = {
      customerEmail: form.customerEmail,
      street: form.street,
      city: form.city,
      state: form.state,
      zipCode: form.zipCode,
      carrier: selectedShippingOption.carrierName,
      serviceType: selectedShippingOption.serviceType,
      shippingCost: selectedShippingOption.price,
      estimatedDeliveryDays: selectedShippingOption.estimatedDays,
      items: cart.map(item => ({
        productId: item.product.id,
        quantity: item.quantity
      }))
    };

    try {
      const newOrder = await createOrderApi(payload);
      showToast(`Pedido #${newOrder.id} realizado com sucesso! Envio e rastreio integrados.`, 'success');
      setCart([]);
      setForm({
        customerEmail: '',
        street: '',
        city: '',
        state: '',
        zipCode: '',
      });
      setShippingOptions([]);
      setSelectedShippingOption(null);

      loadProducts();
      loadOrders();
      loadShipments();
      loadAuditLogs();
      setActiveTab('orders');
    } catch (err) {
      showToast(err.message || 'Erro ao realizar o pedido.', 'error');
    }
  };

  const shipOrder = async (orderId) => {
    try {
      await shipOrderApi(orderId);
      showToast(`Pedido #${orderId} despachado! Status sincronizado no microsserviço de frete.`, 'success');
      loadOrders();
      loadShipments();
      loadAuditLogs();
    } catch (err) {
      showToast(err.message || 'Erro ao despachar pedido.', 'error');
    }
  };

  const cancelOrder = async (orderId) => {
    try {
      await cancelOrderApi(orderId);
      showToast(`Pedido #${orderId} cancelado. Estoque e envio estornados!`, 'success');
      loadOrders();
      loadProducts();
      loadShipments();
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

  // Open Tracking Timeline Modal
  const handleOpenTracking = async (orderId, trackingNumber) => {
    setIsTrackingModalOpen(true);
    setIsTrackingLoading(true);
    setTrackingError(null);
    setActiveTrackingShipment(null);

    try {
      let data = null;
      if (trackingNumber) {
        data = await fetchShipmentByTrackingApi(trackingNumber);
      } else {
        data = await fetchOrderTrackingApi(orderId);
      }
      setActiveTrackingShipment(data);
    } catch (err) {
      setTrackingError(err.message || 'Não foi possível carregar os detalhes de rastreamento.');
    } finally {
      setIsTrackingLoading(false);
    }
  };

  // Logistics Hub Actions: Simulate advancement
  const handleUpdateShipmentStatus = async (trackingNumber, status, message, location) => {
    try {
      await updateShipmentStatusApi(trackingNumber, { status, message, location });
      showToast(`Envio ${trackingNumber} atualizado para "${status}" com novo checkpoint!`, 'success');
      loadShipments();
      loadOrders();
      loadAuditLogs();
    } catch (err) {
      showToast(err.message || 'Erro ao atualizar status do envio.', 'error');
    }
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

      {isTrackingModalOpen && (
        <TrackingTimelineModal 
          shipment={activeTrackingShipment}
          onClose={() => setIsTrackingModalOpen(false)}
          isLoading={isTrackingLoading}
          error={trackingError}
        />
      )}

      <div className="container animate-fade-in">
        <Header 
          activeTab={activeTab}
          onTabChange={(tab) => {
            setActiveTab(tab);
            if (tab === 'history') loadAuditLogs(auditEntityFilter);
            if (tab === 'shipping') loadShipments();
            if (tab === 'orders') loadOrders();
          }}
          ordersCount={orders.length}
          auditLogsCount={auditLogs.length}
          shipmentsCount={shipments.length}
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
                itemsTotal={calculateCartItemsTotal()}
                form={form}
                onInputChange={handleInputChange}
                shippingOptions={shippingOptions}
                selectedShippingOption={selectedShippingOption}
                onSelectShippingOption={setSelectedShippingOption}
                onCalculateShipping={handleCalculateShipping}
                isCalculatingShipping={isCalculatingShipping}
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
            onOpenTracking={handleOpenTracking}
          />
        )}

        {activeTab === 'shipping' && (
          <ShippingManagementTab 
            shipments={shipments}
            onRefreshShipments={loadShipments}
            onUpdateShipmentStatus={handleUpdateShipmentStatus}
            onOpenTrackingDetails={handleOpenTracking}
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
