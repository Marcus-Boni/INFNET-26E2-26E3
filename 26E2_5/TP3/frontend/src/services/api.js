const BACKEND_BASE = 'http://localhost:8080/api';
const SHIPPING_BASE = 'http://localhost:8082/api/v1/shipping';

// --- Catálogo e Produtos ---
export const fetchProductsApi = async (query = '') => {
  const url = query ? `${BACKEND_BASE}/products?search=${encodeURIComponent(query)}` : `${BACKEND_BASE}/products`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Falha ao carregar produtos do catálogo.');
  return res.json();
};

export const adjustStockApi = async (productId, delta) => {
  const res = await fetch(`${BACKEND_BASE}/products/${productId}/stock?delta=${delta}`, {
    method: 'PATCH'
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || 'Erro ao ajustar estoque.');
  return data;
};

// --- Pedidos (Backend Monólito Integrado) ---
export const fetchOrdersApi = async () => {
  const res = await fetch(`${BACKEND_BASE}/orders`);
  if (!res.ok) throw new Error('Erro ao buscar pedidos.');
  const data = await res.json();
  data.sort((a, b) => b.id - a.id);
  return data;
};

export const createOrderApi = async (payload) => {
  const res = await fetch(`${BACKEND_BASE}/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || data.error || 'Erro ao realizar o pedido.');
  return data;
};

export const shipOrderApi = async (orderId) => {
  const res = await fetch(`${BACKEND_BASE}/orders/${orderId}/ship`, {
    method: 'PATCH'
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || data.error || 'Erro ao despachar pedido.');
  return data;
};

export const cancelOrderApi = async (orderId) => {
  const res = await fetch(`${BACKEND_BASE}/orders/${orderId}/cancel`, {
    method: 'PATCH'
  });
  const data = await res.json();
  if (!res.ok) throw new Error(data.message || data.error || 'Erro ao cancelar pedido.');
  return data;
};

export const fetchOrderTrackingApi = async (orderId) => {
  const res = await fetch(`${BACKEND_BASE}/orders/${orderId}/tracking`);
  if (!res.ok) throw new Error('Rastreamento não encontrado para este pedido.');
  return res.json();
};

// --- Cotação e Microsserviço de Frete ---
export const calculateShippingApi = async (zipCode, totalItems, orderTotal) => {
  try {
    const res = await fetch(`${BACKEND_BASE}/shipping/calculate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ zipCode, totalItems, orderTotal })
    });
    if (res.ok) return await res.json();
  } catch {
    console.warn('Falha via proxy, tentando rota direta do microsserviço...');
  }

  // Fallback para comunicação direta com o microsserviço de frete
  const resDirect = await fetch(`${SHIPPING_BASE}/calculate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ zipCode, totalItems, orderTotal })
  });
  if (!resDirect.ok) throw new Error('Não foi possível calcular o frete para o CEP informado.');
  return resDirect.json();
};

export const fetchAllShipmentsApi = async () => {
  const res = await fetch(`${SHIPPING_BASE}/shipments`);
  if (!res.ok) throw new Error('Erro ao listar envios do microsserviço.');
  return res.json();
};

export const fetchShipmentByTrackingApi = async (trackingNumber) => {
  const res = await fetch(`${SHIPPING_BASE}/shipments/${encodeURIComponent(trackingNumber)}`);
  if (!res.ok) throw new Error('Envio não encontrado com este código de rastreamento.');
  return res.json();
};

export const updateShipmentStatusApi = async (trackingNumber, payload) => {
  const res = await fetch(`${SHIPPING_BASE}/shipments/${encodeURIComponent(trackingNumber)}/status`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  if (!res.ok) throw new Error('Erro ao atualizar status do envio.');
  return res.json();
};

// --- Auditoria e Trilha de Histórico ---
export const fetchAuditLogsApi = async (entityName = '') => {
  const url = entityName && entityName !== 'ALL' 
    ? `${BACKEND_BASE}/audit-logs/entity/${entityName}` 
    : `${BACKEND_BASE}/audit-logs`;
  const res = await fetch(url);
  if (!res.ok) throw new Error('Erro ao buscar logs de auditoria.');
  return res.json();
};
