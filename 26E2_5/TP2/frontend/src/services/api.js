const API_BASE = 'http://localhost:8080/api';

export const fetchProductsApi = async (query = '') => {
  const url = query ? `${API_BASE}/products?search=${encodeURIComponent(query)}` : `${API_BASE}/products`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error('Falha ao carregar produtos do catálogo.');
  }
  return res.json();
};

export const fetchOrdersApi = async () => {
  const res = await fetch(`${API_BASE}/orders`);
  if (!res.ok) {
    throw new Error('Erro ao buscar pedidos.');
  }
  const data = await res.json();
  data.sort((a, b) => b.id - a.id);
  return data;
};

export const fetchAuditLogsApi = async (entityName = '') => {
  const url = entityName && entityName !== 'ALL' 
    ? `${API_BASE}/audit-logs/entity/${entityName}` 
    : `${API_BASE}/audit-logs`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error('Erro ao buscar logs de auditoria.');
  }
  return res.json();
};

export const createOrderApi = async (payload) => {
  const res = await fetch(`${API_BASE}/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || data.error || 'Erro ao realizar o pedido.');
  }
  return data;
};

export const shipOrderApi = async (orderId) => {
  const res = await fetch(`${API_BASE}/orders/${orderId}/ship`, {
    method: 'PATCH'
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || data.error || 'Erro ao enviar pedido.');
  }
  return data;
};

export const cancelOrderApi = async (orderId) => {
  const res = await fetch(`${API_BASE}/orders/${orderId}/cancel`, {
    method: 'PATCH'
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || data.error || 'Erro ao cancelar pedido.');
  }
  return data;
};

export const adjustStockApi = async (productId, delta) => {
  const res = await fetch(`${API_BASE}/products/${productId}/stock?delta=${delta}`, {
    method: 'PATCH'
  });
  const data = await res.json();
  if (!res.ok) {
    throw new Error(data.message || 'Erro ao ajustar estoque.');
  }
  return data;
};
