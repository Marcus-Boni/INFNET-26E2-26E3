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
  Cpu, 
  Box, 
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
      // Ordena por data decrescente
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

  const getEventBadgeClass = (eventType) => {
    switch (eventType) {
      case 'ORDER_CREATED':
        return 'bg-emerald-500/10 text-emerald-400 border-emerald-500/30';
      case 'SHIPMENT_CREATED':
        return 'bg-blue-500/10 text-blue-400 border-blue-500/30';
      case 'ORDER_DISPATCHED':
        return 'bg-amber-500/10 text-amber-400 border-amber-500/30';
      case 'SHIPMENT_STATUS_UPDATED':
        return 'bg-purple-500/10 text-purple-400 border-purple-500/30';
      case 'ORDER_CANCELLED':
        return 'bg-rose-500/10 text-rose-400 border-rose-500/30';
      case 'SIMULATED_POISON_PILL_DLQ':
      case 'CORRUPTED_POISON_PILL':
        return 'bg-red-500/20 text-red-300 border-red-500/50';
      default:
        return 'bg-slate-500/10 text-slate-300 border-slate-500/30';
    }
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Top Banner: EDA Overview */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-purple-900/40 via-indigo-900/30 to-slate-900/60 p-6 md:p-8 border border-purple-500/20 shadow-2xl backdrop-blur-xl">
        <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-purple-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-purple-500/20 text-purple-300 border border-purple-500/30 mb-3">
              <Radio className="w-3.5 h-3.5 animate-pulse text-purple-400" />
              Arquitetura Orientada a Eventos (EDA)
            </div>
            <h2 className="text-2xl md:text-3xl font-bold text-white tracking-tight">
              Monitor de Mensagens & Topologia RabbitMQ
            </h2>
            <p className="mt-2 text-slate-300 text-sm md:text-base max-w-3xl leading-relaxed">
              Comunicação 100% assíncrona entre o <strong>Backend de Pedidos (:8080)</strong> e o <strong>Microsserviço de Frete (:8082)</strong> via RabbitMQ com Topic Exchanges, Filas Duráveis, Rastreabilidade por Correlation ID e Dead Letter Queues (DLQ).
            </p>
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button
              onClick={() => setAutoRefresh(!autoRefresh)}
              className={`px-4 py-2 rounded-xl text-xs font-medium border flex items-center gap-2 transition-all ${
                autoRefresh
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-300 hover:bg-emerald-500/20'
                  : 'bg-slate-800 border-slate-700 text-slate-400 hover:bg-slate-700'
              }`}
            >
              <span className={`w-2 h-2 rounded-full ${autoRefresh ? 'bg-emerald-400 animate-ping' : 'bg-slate-500'}`} />
              {autoRefresh ? 'Auto-refresh Ativo (3s)' : 'Auto-refresh Pausado'}
            </button>

            <button
              onClick={handleManualRefresh}
              disabled={loading}
              className="px-4 py-2 rounded-xl bg-purple-600 hover:bg-purple-500 text-white text-xs font-semibold flex items-center gap-2 shadow-lg shadow-purple-600/25 transition-all disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              Atualizar
            </button>
          </div>
        </div>

        {/* Counter KPI Cards */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6 pt-6 border-t border-white/10">
          <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800">
            <div className="flex items-center gap-2 text-slate-400 text-xs font-medium">
              <Zap className="w-4 h-4 text-purple-400" />
              Broker AMQP
            </div>
            <div className="mt-1 flex items-baseline gap-2">
              <span className="text-xl font-bold text-white">RabbitMQ</span>
              <span className="text-xs text-emerald-400 font-medium">Ativo (:5672)</span>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800">
            <div className="flex items-center gap-2 text-slate-400 text-xs font-medium">
              <Send className="w-4 h-4 text-emerald-400" />
              Eventos Publicados
            </div>
            <div className="mt-1 flex items-baseline gap-2">
              <span className="text-2xl font-bold text-emerald-400">{publishedCount}</span>
              <span className="text-xs text-slate-400">mensagens</span>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800">
            <div className="flex items-center gap-2 text-slate-400 text-xs font-medium">
              <Download className="w-4 h-4 text-blue-400" />
              Eventos Consumidos
            </div>
            <div className="mt-1 flex items-baseline gap-2">
              <span className="text-2xl font-bold text-blue-400">{receivedCount}</span>
              <span className="text-xs text-slate-400">mensagens</span>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-900/60 border border-slate-800">
            <div className="flex items-center gap-2 text-slate-400 text-xs font-medium">
              <ShieldAlert className="w-4 h-4 text-rose-400" />
              Dead Letter Queue
            </div>
            <div className="mt-1 flex items-baseline gap-2">
              <span className="text-2xl font-bold text-rose-400">{dlqCount}</span>
              <span className="text-xs text-slate-400">falhas isoladas</span>
            </div>
          </div>
        </div>
      </div>

      {/* Topologia e DLQ Simulator (Grid de 2 Colunas) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Topologia RabbitMQ */}
        <div className="lg:col-span-7 bg-slate-900/80 rounded-2xl p-6 border border-slate-800 shadow-xl backdrop-blur-md">
          <div className="flex items-center gap-2 mb-4">
            <Layers className="w-5 h-5 text-indigo-400" />
            <h3 className="text-lg font-bold text-white">Topologia de Exchanges & Filas AMQP</h3>
          </div>

          <div className="space-y-4 text-sm">
            {/* Topic Exchange 1 */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-indigo-500/20">
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-indigo-300">nexus.order.exchange</span>
                <span className="text-xs px-2 py-0.5 rounded bg-indigo-500/20 text-indigo-300 border border-indigo-500/30">Topic Exchange</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">Publicado pelo Backend principal (:8080) quando pedidos sofrem mutações.</p>
              <div className="mt-3 flex flex-wrap gap-2">
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-emerald-400 font-semibold">order.created</span> → shipping.order-created.queue
                </span>
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-amber-400 font-semibold">order.dispatched</span> → shipping.order-dispatched.queue
                </span>
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-rose-400 font-semibold">order.cancelled</span> → shipping.order-cancelled.queue
                </span>
              </div>
            </div>

            {/* Topic Exchange 2 */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-purple-500/20">
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-purple-300">nexus.shipping.exchange</span>
                <span className="text-xs px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 border border-purple-500/30">Topic Exchange</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">Publicado pelo Microsserviço de Logística (:8082) ao gerar rastreio e atualizar marcos.</p>
              <div className="mt-3 flex flex-wrap gap-2">
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-blue-400 font-semibold">shipping.created</span> → order.shipment-created.queue
                </span>
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-purple-400 font-semibold">shipping.status-updated</span> → order.shipment-status-updated.queue
                </span>
              </div>
            </div>

            {/* Dead Letter Exchange */}
            <div className="p-4 rounded-xl bg-slate-800/60 border border-rose-500/20">
              <div className="flex items-center justify-between">
                <span className="font-mono font-bold text-rose-300">nexus.dlx.exchange</span>
                <span className="text-xs px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 border border-rose-500/30">Dead Letter Exchange</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">Isola mensagens venenosas após esgotamento de 3 tentativas de retry exponencial.</p>
              <div className="mt-3 flex flex-wrap gap-2">
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-rose-400 font-semibold">order.dlq</span> → order.dead-letter.queue
                </span>
                <span className="text-xs font-mono px-2 py-1 rounded bg-slate-900 text-slate-300 border border-slate-700">
                  <span className="text-rose-400 font-semibold">shipping.dlq</span> → shipping.dead-letter.queue
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Simulador Interativo de DLQ & Resiliência */}
        <div className="lg:col-span-5 bg-slate-900/80 rounded-2xl p-6 border border-slate-800 shadow-xl backdrop-blur-md flex flex-col justify-between">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <ShieldAlert className="w-5 h-5 text-rose-400" />
              <h3 className="text-lg font-bold text-white">Laboratório de Resiliência (DLQ)</h3>
            </div>
            <p className="text-xs text-slate-400 mb-4 leading-relaxed">
              Demonstre o padrão de <strong>Dead Letter Queue (DLQ)</strong> e tratamento de mensagens venenosas (Poison Pills). Ao disparar a simulação, uma mensagem com carga inválida é enviada e roteada para isolamento sem travar os consumidores.
            </p>

            <form onSubmit={handleSimulateDlq} className="space-y-3">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Fila Alvo de Teste:</label>
                <select
                  value={dlqTarget}
                  onChange={(e) => setDlqTarget(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-rose-500"
                >
                  <option value="order">order.dead-letter.queue (Backend de Pedidos)</option>
                  <option value="shipping">shipping.dead-letter.queue (Microsserviço de Logística)</option>
                </select>
              </div>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1">Motivo da Falha Simulada:</label>
                <input
                  type="text"
                  value={dlqReason}
                  onChange={(e) => setDlqReason(e.target.value)}
                  className="w-full bg-slate-800 border border-slate-700 rounded-xl px-3 py-2 text-xs text-white focus:outline-none focus:border-rose-500"
                  placeholder="Ex: Falha de integridade referencial ou erro na serialização"
                />
              </div>

              <button
                type="submit"
                disabled={isSimulatingDlq}
                className="w-full mt-2 py-2.5 px-4 rounded-xl bg-gradient-to-r from-rose-600 to-red-600 hover:from-rose-500 hover:to-red-500 text-white text-xs font-bold shadow-lg shadow-rose-600/30 flex items-center justify-center gap-2 transition-all disabled:opacity-50"
              >
                <AlertTriangle className="w-4 h-4" />
                {isSimulatingDlq ? 'Injetando no Broker...' : 'Disparar Simulação para DLQ'}
              </button>
            </form>
          </div>

          {lastDlqResult && (
            <div className="mt-4 p-3 rounded-xl bg-rose-500/10 border border-rose-500/30 text-xs">
              <div className="flex items-center gap-1.5 text-rose-300 font-semibold mb-1">
                <CheckCircle2 className="w-3.5 h-3.5" />
                Mensagem roteada para DLQ com sucesso
              </div>
              <div className="font-mono text-slate-400 truncate">
                CorrelationId: {lastDlqResult.correlationId}
              </div>
              <div className="font-mono text-slate-400">
                Fila: {lastDlqResult.targetQueue}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Live Event Stream / Filtros e Linha do Tempo */}
      <div className="bg-slate-900/80 rounded-2xl p-6 border border-slate-800 shadow-xl backdrop-blur-md">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pb-6 border-b border-slate-800">
          <div>
            <div className="flex items-center gap-2">
              <Workflow className="w-5 h-5 text-purple-400" />
              <h3 className="text-lg font-bold text-white">Fluxo de Eventos em Tempo Real</h3>
            </div>
            <p className="text-xs text-slate-400 mt-1">Trilha cronológica unificada de eventos emitidos e consumidos pelos microsserviços.</p>
          </div>

          {/* Filtros e Barra de Pesquisa */}
          <div className="flex flex-wrap items-center gap-2">
            <div className="relative">
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
              <input
                type="text"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                placeholder="Buscar por correlação, tipo, rota..."
                className="pl-9 pr-3 py-1.5 rounded-xl bg-slate-800 border border-slate-700 text-xs text-white placeholder-slate-500 focus:outline-none focus:border-purple-500 w-64"
              />
            </div>

            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="px-3 py-1.5 rounded-xl bg-slate-800 border border-slate-700 text-xs text-slate-200 focus:outline-none focus:border-purple-500"
            >
              <option value="ALL">Todos os Tipos de Eventos</option>
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
        <div className="mt-6 space-y-3">
          {filteredEvents.length === 0 ? (
            <div className="text-center py-12 text-slate-400 text-sm">
              <ArrowRightLeft className="w-8 h-8 mx-auto text-slate-600 mb-2" />
              Nenhum evento registrado até o momento. Realize um pedido no Catálogo ou despache uma encomenda para ver os eventos fluindo no RabbitMQ!
            </div>
          ) : (
            filteredEvents.map((evt) => {
              const isExpanded = expandedPayloadId === evt.id;
              const isPublished = evt.direction === 'PUBLISHED';
              const isDlq = evt.status === 'DLQ' || (evt.eventType && evt.eventType.includes('DLQ'));

              return (
                <div
                  key={`${evt.id}-${evt.eventId}`}
                  className={`p-4 rounded-xl border transition-all ${
                    isDlq
                      ? 'bg-red-950/20 border-red-500/30 hover:border-red-500/50'
                      : isPublished
                      ? 'bg-slate-800/40 border-slate-700/60 hover:border-slate-600'
                      : 'bg-indigo-950/20 border-indigo-500/30 hover:border-indigo-500/50'
                  }`}
                >
                  <div className="flex flex-col md:flex-row md:items-center justify-between gap-3">
                    <div className="flex items-start md:items-center gap-3">
                      <div className={`p-2 rounded-lg ${
                        isDlq 
                          ? 'bg-red-500/20 text-red-400' 
                          : isPublished 
                          ? 'bg-emerald-500/20 text-emerald-400' 
                          : 'bg-blue-500/20 text-blue-400'
                      }`}>
                        {isDlq ? <ShieldAlert className="w-4 h-4" /> : isPublished ? <Send className="w-4 h-4" /> : <Download className="w-4 h-4" />}
                      </div>

                      <div>
                        <div className="flex flex-wrap items-center gap-2">
                          <span className={`px-2 py-0.5 rounded text-xs font-mono font-bold border ${getEventBadgeClass(evt.eventType)}`}>
                            {evt.eventType}
                          </span>
                          <span className={`text-xs px-2 py-0.5 rounded font-medium ${
                            isPublished ? 'bg-emerald-500/10 text-emerald-300' : 'bg-blue-500/10 text-blue-300'
                          }`}>
                            {evt.direction === 'PUBLISHED' ? 'PUBLICADO' : 'RECEBIDO / CONSUMIDO'}
                          </span>
                          {evt.status === 'DLQ' && (
                            <span className="text-xs px-2 py-0.5 rounded bg-rose-500/20 text-rose-300 border border-rose-500/40 font-bold">
                              DLQ ISOLADO
                            </span>
                          )}
                        </div>

                        <p className="text-xs text-slate-300 mt-1 font-medium">{evt.details || 'Sem descrição adicional'}</p>
                      </div>
                    </div>

                    <div className="flex flex-wrap items-center gap-3 text-xs text-slate-400">
                      {evt.correlationId && (
                        <div className="flex items-center gap-1 font-mono bg-slate-900 px-2 py-1 rounded border border-slate-700/60 text-slate-300">
                          <span className="text-slate-500">corr:</span>
                          <span className="text-purple-300">{evt.correlationId}</span>
                        </div>
                      )}

                      <div className="flex items-center gap-1">
                        <Clock className="w-3.5 h-3.5 text-slate-500" />
                        <span>{new Date(evt.timestamp).toLocaleTimeString('pt-BR')}</span>
                      </div>

                      {evt.payload && (
                        <button
                          onClick={() => setExpandedPayloadId(isExpanded ? null : evt.id)}
                          className="px-2 py-1 rounded bg-slate-800 hover:bg-slate-700 text-slate-300 flex items-center gap-1 transition-colors"
                        >
                          <Code2 className="w-3 h-3 text-indigo-400" />
                          <span>Payload</span>
                          {isExpanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
                        </button>
                      )}
                    </div>
                  </div>

                  {/* Detalhes de roteamento */}
                  <div className="mt-2.5 pt-2.5 border-t border-slate-800/80 flex flex-wrap items-center gap-4 text-xs font-mono text-slate-400">
                    <div>
                      <span className="text-slate-500">Exchange: </span>
                      <span className="text-slate-300 font-semibold">{evt.exchange || 'N/A'}</span>
                    </div>
                    <div>
                      <span className="text-slate-500">Routing Key: </span>
                      <span className="text-slate-300 font-semibold">{evt.routingKey || 'N/A'}</span>
                    </div>
                    <div>
                      <span className="text-slate-500">EventId: </span>
                      <span className="text-slate-500">{evt.eventId}</span>
                    </div>
                  </div>

                  {/* Visualizador de Payload JSON Expandível */}
                  {isExpanded && evt.payload && (
                    <div className="mt-3 p-3 rounded-lg bg-slate-950 border border-slate-800 text-xs font-mono text-slate-300 overflow-x-auto">
                      <pre className="whitespace-pre-wrap break-all">
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
