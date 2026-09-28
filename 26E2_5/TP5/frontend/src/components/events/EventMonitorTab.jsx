import React, { useState, useEffect } from 'react';
import { 
  Radio, 
  Send, 
  Download, 
  AlertTriangle, 
  RefreshCw, 
  Search, 
  Layers, 
  ArrowRightLeft, 
  ShieldAlert, 
  CheckCircle2, 
  Clock, 
  Code2, 
  Workflow, 
  Zap,
  ChevronDown,
  ChevronUp
} from 'lucide-react';
import toast from 'react-hot-toast';
import { 
  fetchBackendEventsApi, 
  fetchShippingEventsApi, 
  fetchEventTopologyApi, 
  simulateDlqApi 
} from '../../services/api';

export function EventMonitorTab() {
  const [events, setEvents] = useState([]);
  const [topology, setTopology] = useState(null);
  const [loading, setLoading] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [filterType, setFilterType] = useState('ALL');
  const [searchTerm, setSearchTerm] = useState('');
  const [expandedPayloadId, setExpandedPayloadId] = useState(null);

  // DLQ Simulator State
  const [dlqTarget, setDlqTarget] = useState('order');
  const [dlqReason, setDlqReason] = useState('Simulação de falha de validação ou payload corrompido');
  const [isSimulatingDlq, setIsSimulatingDlq] = useState(false);
  const [lastDlqResult, setLastDlqResult] = useState(null);

  const loadData = async () => {
    try {
      const [backendEvts, shippingEvts, topo] = await Promise.all([
        fetchBackendEventsApi(),
        fetchShippingEventsApi(),
        fetchEventTopologyApi()
      ]);

      const all = [...(backendEvts || []), ...(shippingEvts || [])];
      all.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      setEvents(all);
      if (topo) setTopology(topo);
    } catch {
      // Ignora falhas pontuais de polling
    }
  };

  useEffect(() => {
    loadData();
    let interval = null;
    if (autoRefresh) {
      interval = setInterval(() => {
        loadData();
      }, 3000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [autoRefresh]);

  const handleManualRefresh = async () => {
    setLoading(true);
    await loadData();
    setLoading(false);
    toast.success('Eventos atualizados com sucesso!');
  };

  const handleSimulateDlq = async (e) => {
    e.preventDefault();
    setIsSimulatingDlq(true);
    try {
      const result = await simulateDlqApi(dlqTarget, dlqReason);
      setLastDlqResult(result);
      toast.success(result.message || 'Mensagem enviada para a Dead Letter Queue!');
      await loadData();
    } catch (err) {
      toast.error(err.message || 'Erro ao simular envio para DLQ.');
    } finally {
      setIsSimulatingDlq(false);
    }
  };

  const filteredEvents = events.filter(evt => {
    const matchesType = filterType === 'ALL' || evt.eventType === filterType || (filterType === 'DLQ' && (evt.status === 'DLQ' || evt.eventType.includes('DLQ')));
    const matchesSearch = !searchTerm || 
      (evt.correlationId && evt.correlationId.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (evt.eventType && evt.eventType.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (evt.routingKey && evt.routingKey.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (evt.details && evt.details.toLowerCase().includes(searchTerm.toLowerCase()));
    return matchesType && matchesSearch;
  });

  const publishedCount = events.filter(e => e.direction === 'PUBLISHED').length;
  const receivedCount = events.filter(e => e.direction === 'RECEIVED').length;
  const dlqCount = events.filter(e => e.status === 'DLQ' || (e.eventType && e.eventType.includes('DLQ'))).length;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '2rem' }}>
      
      {/* Header Minimalista Preto e Branco */}
      <div style={{ 
        display: 'flex', 
        justifyContent: 'space-between', 
        alignItems: 'flex-start', 
        flexWrap: 'wrap', 
        gap: '1.5rem',
        paddingBottom: '1.5rem',
        borderBottom: '1px solid var(--border-color)'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.5rem' }}>
            <span className="tp-badge" style={{ margin: 0 }}>EDA</span>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
              Arquitetura Orientada a Eventos
            </span>
          </div>
          <h2 style={{ fontSize: '1.6rem', fontWeight: 700, letterSpacing: '-0.02em', margin: '0 0 0.5rem 0', color: 'var(--text-primary)' }}>
            Monitor de Mensagens & Topologia RabbitMQ
          </h2>
          <p style={{ color: 'var(--text-secondary)', fontSize: '0.875rem', maxWidth: '820px', lineHeight: 1.6, margin: 0 }}>
            Comunicação 100% assíncrona entre o <strong>Backend de Pedidos (:8080)</strong> e o <strong>Microsserviço de Frete (:8082)</strong> via RabbitMQ com Topic Exchanges, Filas Duráveis, Rastreabilidade por Correlation ID e Dead Letter Queues (DLQ).
          </p>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            className="btn btn-secondary btn-sm"
            style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
          >
            <span style={{ 
              width: '8px', 
              height: '8px', 
              borderRadius: '50%', 
              background: autoRefresh ? 'var(--color-success)' : 'var(--text-muted)',
              display: 'inline-block'
            }} />
            {autoRefresh ? 'Auto-refresh (3s)' : 'Auto-refresh Pausado'}
          </button>

          <button
            onClick={handleManualRefresh}
            disabled={loading}
            className="btn btn-primary btn-sm"
          >
            <RefreshCw size={14} className={loading ? 'animate-spin' : ''} />
            Atualizar
          </button>
        </div>
      </div>

      {/* Grid de 4 Cards de Métricas / KPIs */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', 
        gap: '1rem' 
      }}>
        
        {/* Broker AMQP */}
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Broker AMQP
            </span>
            <Radio size={16} color="var(--text-secondary)" />
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
            RabbitMQ
          </div>
          <div style={{ marginTop: '0.35rem', display: 'flex', alignItems: 'center', gap: '0.4rem', fontSize: '0.75rem' }}>
            <span style={{ width: '6px', height: '6px', borderRadius: '50%', background: 'var(--color-success)' }} />
            <span style={{ color: 'var(--color-success)', fontWeight: 500 }}>Ativo (:5672)</span>
          </div>
        </div>

        {/* Eventos Publicados */}
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Eventos Publicados
            </span>
            <Send size={16} color="var(--text-secondary)" />
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
            {publishedCount}
          </div>
          <div style={{ marginTop: '0.35rem', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
            mensagens enviadas
          </div>
        </div>

        {/* Eventos Consumidos */}
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Eventos Consumidos
            </span>
            <Download size={16} color="var(--text-secondary)" />
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
            {receivedCount}
          </div>
          <div style={{ marginTop: '0.35rem', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
            processados com sucesso
          </div>
        </div>

        {/* Dead Letter Queue */}
        <div className="glass-card" style={{ padding: '1.25rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
            <span style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
              Dead Letter Queue
            </span>
            <ShieldAlert size={16} color="var(--text-secondary)" />
          </div>
          <div style={{ fontSize: '1.5rem', fontWeight: 700, color: 'var(--text-primary)', fontFamily: 'monospace' }}>
            {dlqCount}
          </div>
          <div style={{ marginTop: '0.35rem', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
            falhas isoladas em DLQ
          </div>
        </div>

      </div>

      {/* Grid Principal: Topologia AMQP e Simulador de DLQ */}
      <div style={{ 
        display: 'grid', 
        gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', 
        gap: '1.5rem',
        alignItems: 'start'
      }}>
        
        {/* Card: Topologia de Exchanges & Filas */}
        <div className="glass-card" style={{ padding: '1.75rem' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '1.25rem' }}>
            <Layers size={20} color="var(--text-primary)" />
            <h3 style={{ fontSize: '1.1rem', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>
              Topologia de Exchanges & Filas AMQP
            </h3>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            
            {/* Exchange 1 */}
            <div style={{ 
              padding: '1rem', 
              background: '#121214', 
              border: '1px solid var(--border-color)', 
              borderRadius: 'var(--radius-md)' 
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <span style={{ fontFamily: 'monospace', fontWeight: 600, color: 'var(--text-primary)', fontSize: '0.9rem' }}>
                  nexus.order.exchange
                </span>
                <span className="badge" style={{ background: '#18181b', border: '1px solid #27272a', color: '#a1a1aa', fontFamily: 'monospace', fontSize: '0.7rem' }}>
                  Topic Exchange
                </span>
              </div>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', margin: '0 0 0.75rem 0' }}>
                Publicado pelo Backend (:8080) quando pedidos sofrem mutações.
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>order.created</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ shipping.order-created.queue</span>
                </div>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>order.dispatched</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ shipping.order-dispatched.queue</span>
                </div>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>order.cancelled</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ shipping.order-cancelled.queue</span>
                </div>
              </div>
            </div>

            {/* Exchange 2 */}
            <div style={{ 
              padding: '1rem', 
              background: '#121214', 
              border: '1px solid var(--border-color)', 
              borderRadius: 'var(--radius-md)' 
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <span style={{ fontFamily: 'monospace', fontWeight: 600, color: 'var(--text-primary)', fontSize: '0.9rem' }}>
                  nexus.shipping.exchange
                </span>
                <span className="badge" style={{ background: '#18181b', border: '1px solid #27272a', color: '#a1a1aa', fontFamily: 'monospace', fontSize: '0.7rem' }}>
                  Topic Exchange
                </span>
              </div>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', margin: '0 0 0.75rem 0' }}>
                Publicado pelo Microsserviço de Logística (:8082) ao gerar rastreio e atualizar marcos.
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>shipping.created</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ order.shipment-created.queue</span>
                </div>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>shipping.status-updated</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ order.shipment-status-updated.queue</span>
                </div>
              </div>
            </div>

            {/* DLX Exchange */}
            <div style={{ 
              padding: '1rem', 
              background: '#121214', 
              border: '1px solid var(--border-color)', 
              borderRadius: 'var(--radius-md)' 
            }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '0.35rem' }}>
                <span style={{ fontFamily: 'monospace', fontWeight: 600, color: 'var(--text-primary)', fontSize: '0.9rem' }}>
                  nexus.dlx.exchange
                </span>
                <span className="badge" style={{ background: '#18181b', border: '1px solid #27272a', color: '#a1a1aa', fontFamily: 'monospace', fontSize: '0.7rem' }}>
                  Dead Letter Exchange
                </span>
              </div>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', margin: '0 0 0.75rem 0' }}>
                Isola mensagens venenosas após esgotamento de 3 tentativas de retry exponencial.
              </p>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '0.35rem' }}>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>order.dlq</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ order.dead-letter.queue</span>
                </div>
                <div style={{ 
                  display: 'flex', 
                  alignItems: 'center', 
                  justifyContent: 'space-between',
                  padding: '0.4rem 0.6rem', 
                  background: '#09090b', 
                  borderRadius: 'var(--radius-sm)', 
                  border: '1px solid #1f1f23',
                  fontFamily: 'monospace', 
                  fontSize: '0.75rem' 
                }}>
                  <span style={{ color: 'var(--text-primary)', fontWeight: 600 }}>shipping.dlq</span>
                  <span style={{ color: 'var(--text-secondary)' }}>→ shipping.dead-letter.queue</span>
                </div>
              </div>
            </div>

          </div>
        </div>

        {/* Card: Laboratório de Resiliência (DLQ) */}
        <div className="glass-card" style={{ padding: '1.75rem', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem', marginBottom: '0.5rem' }}>
              <ShieldAlert size={20} color="var(--text-primary)" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>
                Laboratório de Resiliência (DLQ)
              </h3>
            </div>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.5, margin: '0 0 1.25rem 0' }}>
              Demonstre o padrão de <strong>Dead Letter Queue (DLQ)</strong> e isolamento de mensagens corrompidas (Poison Pills). Ao disparar a simulação, uma mensagem com carga inválida é enviada e roteada para isolamento sem travar os consumidores.
            </p>

            <form onSubmit={handleSimulateDlq} style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.4rem' }}>
                  Fila Alvo de Teste:
                </label>
                <select
                  value={dlqTarget}
                  onChange={(e) => setDlqTarget(e.target.value)}
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.85rem',
                    background: '#09090b',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    color: 'var(--text-primary)',
                    fontSize: '0.85rem',
                    outline: 'none'
                  }}
                >
                  <option value="order">order.dead-letter.queue (Backend de Pedidos)</option>
                  <option value="shipping">shipping.dead-letter.queue (Microsserviço de Logística)</option>
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-secondary)', marginBottom: '0.4rem' }}>
                  Motivo da Falha Simulada:
                </label>
                <input
                  type="text"
                  value={dlqReason}
                  onChange={(e) => setDlqReason(e.target.value)}
                  placeholder="Ex: Falha de validação ou payload corrompido"
                  style={{
                    width: '100%',
                    padding: '0.65rem 0.85rem',
                    background: '#09090b',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    color: 'var(--text-primary)',
                    fontSize: '0.85rem',
                    outline: 'none'
                  }}
                />
              </div>

              <button
                type="submit"
                disabled={isSimulatingDlq}
                className="btn btn-primary"
                style={{ width: '100%', marginTop: '0.5rem', padding: '0.75rem 1rem' }}
              >
                <AlertTriangle size={15} />
                {isSimulatingDlq ? 'Injetando no Broker...' : 'Disparar Simulação para DLQ'}
              </button>
            </form>
          </div>

          {lastDlqResult && (
            <div style={{ 
              marginTop: '1.25rem', 
              padding: '1rem', 
              background: '#121214', 
              border: '1px solid var(--border-color)', 
              borderRadius: 'var(--radius-md)',
              fontSize: '0.8rem' 
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', color: 'var(--color-success)', fontWeight: 600, marginBottom: '0.35rem' }}>
                <CheckCircle2 size={16} />
                Mensagem roteada para DLQ com sucesso
              </div>
              <div style={{ fontFamily: 'monospace', color: 'var(--text-secondary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                CorrelationId: <span style={{ color: 'var(--text-primary)' }}>{lastDlqResult.correlationId}</span>
              </div>
              <div style={{ fontFamily: 'monospace', color: 'var(--text-secondary)', marginTop: '0.2rem' }}>
                Fila: <span style={{ color: 'var(--text-primary)' }}>{lastDlqResult.targetQueue}</span>
              </div>
            </div>
          )}
        </div>

      </div>

      {/* Stream de Eventos em Tempo Real */}
      <div className="glass-card" style={{ padding: '1.75rem' }}>
        
        {/* Barra Superior do Feed: Título e Controles de Busca */}
        <div style={{ 
          display: 'flex', 
          justifyContent: 'space-between', 
          alignItems: 'center', 
          flexWrap: 'wrap', 
          gap: '1rem',
          paddingBottom: '1.25rem',
          borderBottom: '1px solid var(--border-color)'
        }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '0.6rem' }}>
              <Workflow size={20} color="var(--text-primary)" />
              <h3 style={{ fontSize: '1.1rem', fontWeight: 600, margin: 0, color: 'var(--text-primary)' }}>
                Fluxo de Eventos em Tempo Real
              </h3>
            </div>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', margin: '0.25rem 0 0 0' }}>
              Trilha cronológica unificada de eventos emitidos e consumidos pelos microsserviços.
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap' }}>
            <div style={{ position: 'relative', width: '240px' }}>
              <Search size={14} color="var(--text-muted)" style={{ position: 'absolute', left: '0.75rem', top: '50%', transform: 'translateY(-50%)' }} />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Buscar correlação, tipo..."
                style={{
                  paddingLeft: '2.2rem',
                  paddingRight: '0.75rem',
                  paddingTop: '0.45rem',
                  paddingBottom: '0.45rem',
                  fontSize: '0.8rem',
                  background: '#121214',
                  border: '1px solid var(--border-color)',
                  borderRadius: 'var(--radius-sm)',
                  color: 'var(--text-primary)'
                }}
              />
            </div>

            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              style={{
                width: 'auto',
                padding: '0.45rem 0.75rem',
                fontSize: '0.8rem',
                background: '#121214',
                border: '1px solid var(--border-color)',
                borderRadius: 'var(--radius-sm)',
                color: 'var(--text-primary)'
              }}
            >
              <option value="ALL">Todos os Tipos</option>
              <option value="ORDER_CREATED">ORDER_CREATED</option>
              <option value="SHIPMENT_CREATED">SHIPMENT_CREATED</option>
              <option value="ORDER_DISPATCHED">ORDER_DISPATCHED</option>
              <option value="SHIPMENT_STATUS_UPDATED">SHIPMENT_STATUS_UPDATED</option>
              <option value="ORDER_CANCELLED">ORDER_CANCELLED</option>
              <option value="DLQ">Somente Eventos DLQ</option>
            </select>
          </div>
        </div>

        {/* Lista de Eventos */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem', marginTop: '1.25rem' }}>
          {filteredEvents.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '3rem 1rem', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
              <ArrowRightLeft size={32} color="var(--text-muted)" style={{ margin: '0 auto 0.75rem auto' }} />
              <div>Nenhum evento registrado até o momento.</div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>
                Realize um pedido no Catálogo ou despache uma encomenda para ver os eventos fluindo no RabbitMQ!
              </div>
            </div>
          ) : (
            filteredEvents.map((evt) => {
              const isExpanded = expandedPayloadId === evt.id;
              const isPublished = evt.direction === 'PUBLISHED';
              const isDlq = evt.status === 'DLQ' || (evt.eventType && evt.eventType.includes('DLQ'));

              return (
                <div
                  key={`${evt.id}-${evt.eventId}`}
                  style={{
                    padding: '1rem',
                    background: '#121214',
                    border: '1px solid var(--border-color)',
                    borderRadius: 'var(--radius-md)',
                    transition: 'border-color var(--transition-fast)'
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '0.75rem' }}>
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.75rem' }}>
                      <div style={{ 
                        padding: '0.5rem', 
                        borderRadius: 'var(--radius-sm)', 
                        background: '#18181b', 
                        border: '1px solid var(--border-color)',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        marginTop: '2px'
                      }}>
                        {isDlq ? (
                          <ShieldAlert size={16} color="var(--color-danger)" />
                        ) : isPublished ? (
                          <Send size={16} color="var(--text-primary)" />
                        ) : (
                          <Download size={16} color="var(--text-primary)" />
                        )}
                      </div>

                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', flexWrap: 'wrap' }}>
                          <span style={{ 
                            fontFamily: 'monospace', 
                            fontSize: '0.8rem', 
                            fontWeight: 700, 
                            color: 'var(--text-primary)',
                            padding: '0.15rem 0.45rem',
                            background: '#18181b',
                            border: '1px solid var(--border-color)',
                            borderRadius: 'var(--radius-sm)'
                          }}>
                            {evt.eventType}
                          </span>

                          <span className={isPublished ? 'badge' : 'badge'} style={{
                            background: '#18181b',
                            border: '1px solid #27272a',
                            color: isPublished ? '#a1a1aa' : '#fafafa',
                            fontFamily: 'monospace',
                            fontSize: '0.7rem'
                          }}>
                            {isPublished ? 'PUBLICADO' : 'RECEBIDO / CONSUMIDO'}
                          </span>

                          {isDlq && (
                            <span className="badge badge-danger" style={{ fontFamily: 'monospace', fontSize: '0.7rem' }}>
                              DLQ ISOLADO
                            </span>
                          )}
                        </div>

                        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '0.35rem 0 0 0' }}>
                          {evt.details || 'Sem descrição adicional'}
                        </p>
                      </div>
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem', flexWrap: 'wrap', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                      {evt.correlationId && (
                        <div style={{ 
                          fontFamily: 'monospace', 
                          padding: '0.2rem 0.45rem', 
                          background: '#09090b', 
                          borderRadius: 'var(--radius-sm)', 
                          border: '1px solid var(--border-color)',
                          color: 'var(--text-secondary)'
                        }}>
                          <span style={{ color: 'var(--text-muted)' }}>corr: </span>
                          <span style={{ color: 'var(--text-primary)' }}>{evt.correlationId}</span>
                        </div>
                      )}

                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.3rem' }}>
                        <Clock size={13} color="var(--text-muted)" />
                        <span>{new Date(evt.timestamp).toLocaleTimeString('pt-BR')}</span>
                      </div>

                      {evt.payload && (
                        <button
                          onClick={() => setExpandedPayloadId(isExpanded ? null : evt.id)}
                          className="btn btn-secondary btn-sm"
                          style={{ padding: '0.25rem 0.5rem', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '0.3rem' }}
                        >
                          <Code2 size={12} />
                          <span>Payload</span>
                          {isExpanded ? <ChevronUp size={12} /> : <ChevronDown size={12} />}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Detalhes de roteamento */}
                  <div style={{ 
                    marginTop: '0.65rem', 
                    paddingTop: '0.65rem', 
                    borderTop: '1px solid #1f1f23', 
                    display: 'flex', 
                    flexWrap: 'wrap', 
                    gap: '1.25rem', 
                    fontSize: '0.75rem', 
                    fontFamily: 'monospace', 
                    color: 'var(--text-muted)' 
                  }}>
                    <div>
                      <span>Exchange: </span>
                      <span style={{ color: 'var(--text-primary)' }}>{evt.exchange || 'N/A'}</span>
                    </div>
                    <div>
                      <span>Routing Key: </span>
                      <span style={{ color: 'var(--text-primary)' }}>{evt.routingKey || 'N/A'}</span>
                    </div>
                    <div>
                      <span>EventId: </span>
                      <span style={{ color: 'var(--text-muted)' }}>{evt.eventId}</span>
                    </div>
                  </div>

                  {/* Visualizador de Payload JSON Expandível */}
                  {isExpanded && evt.payload && (
                    <div style={{ 
                      marginTop: '0.75rem', 
                      padding: '0.85rem', 
                      background: '#09090b', 
                      borderRadius: 'var(--radius-sm)', 
                      border: '1px solid var(--border-color)', 
                      fontSize: '0.75rem', 
                      fontFamily: 'monospace', 
                      color: 'var(--text-primary)',
                      overflowX: 'auto' 
                    }}>
                      <pre style={{ margin: 0, whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                        {(() => {
                          try {
                            return JSON.stringify(JSON.parse(evt.payload), null, 2);
                          } catch {
                            return evt.payload;
                          }
                        })()}
                      </pre>
                    </div>
                  )}
                </div>
              );
            })
          )}
        </div>
      </div>

    </div>
  );
}
