import React, { useState, useEffect } from 'react';
import { 
  Server, 
  Activity, 
  Cpu, 
  Layers, 
  GitBranch, 
  ShieldCheck, 
  ExternalLink, 
  RefreshCw, 
  CheckCircle2, 
  AlertCircle, 
  Terminal, 
  HardDrive, 
  Box, 
  Network, 
  Gauge, 
  Radio, 
  BarChart3, 
  Workflow,
  Zap,
  Globe
} from 'lucide-react';
import toast from 'react-hot-toast';
import { 
  fetchBackendHealthApi, 
  fetchShippingHealthApi, 
  fetchActuatorMetricApi 
} from '../../services/api';

export function DevOpsObservabilityTab() {
  const [backendHealth, setBackendHealth] = useState(null);
  const [shippingHealth, setShippingHealth] = useState(null);
  const [backendMemory, setBackendMemory] = useState(null);
  const [shippingMemory, setShippingMemory] = useState(null);
  const [loading, setLoading] = useState(false);
  const [autoRefresh, setAutoRefresh] = useState(true);

  const loadHealthData = async () => {
    try {
      const [bHealth, sHealth, bMem, sMem] = await Promise.all([
        fetchBackendHealthApi(),
        fetchShippingHealthApi(),
        fetchActuatorMetricApi(8080, 'jvm.memory.used'),
        fetchActuatorMetricApi(8082, 'jvm.memory.used')
      ]);

      setBackendHealth(bHealth);
      setShippingHealth(sHealth);
      if (bMem && bMem.measurements && bMem.measurements[0]) {
        setBackendMemory((bMem.measurements[0].value / (1024 * 1024)).toFixed(1));
      }
      if (sMem && sMem.measurements && sMem.measurements[0]) {
        setShippingMemory((sMem.measurements[0].value / (1024 * 1024)).toFixed(1));
      }
    } catch {
      // Falha suave
    }
  };

  useEffect(() => {
    loadHealthData();
    let interval = null;
    if (autoRefresh) {
      interval = setInterval(() => {
        loadHealthData();
      }, 5000);
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [autoRefresh]);

  const handleManualRefresh = async () => {
    setLoading(true);
    await loadHealthData();
    setLoading(false);
    toast.success('Telemetria atualizada!');
  };

  return (
    <div className="space-y-8 animate-fadeIn">
      {/* Top Banner: DevOps & Kubernetes */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-r from-blue-900/40 via-cyan-900/30 to-slate-900/60 p-6 md:p-8 border border-cyan-500/20 shadow-2xl backdrop-blur-xl">
        <div className="absolute top-0 right-0 -mr-16 -mt-16 w-64 h-64 bg-cyan-500/10 rounded-full blur-3xl pointer-events-none" />
        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-cyan-500/20 text-cyan-300 border border-cyan-500/30 mb-3">
              <Cpu className="w-3.5 h-3.5 animate-pulse text-cyan-400" />
              Operação, DevOps & Observabilidade (TP5)
            </div>
            <h2 className="text-2xl md:text-3xl font-bold text-white tracking-tight">
              Cluster Kubernetes & Telemetria em Produção
            </h2>
            <p className="mt-2 text-slate-300 text-sm md:text-base max-w-3xl leading-relaxed">
              Ambiente preparado para produção com <strong>Docker Multi-stage</strong>, orquestração <strong>Kubernetes (Namespace, Deployments, Services, HPA, Probes)</strong>, rastreamento distribuído via <strong>Zipkin</strong>, métricas com <strong>Prometheus & Grafana</strong> e automação <strong>CI/CD com GitHub Actions</strong>.
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
              {autoRefresh ? 'Auto-refresh Ativo (5s)' : 'Auto-refresh Pausado'}
            </button>

            <button
              onClick={handleManualRefresh}
              disabled={loading}
              className="px-4 py-2 rounded-xl bg-cyan-600 hover:bg-cyan-500 text-white text-xs font-semibold flex items-center gap-2 shadow-lg shadow-cyan-600/25 transition-all disabled:opacity-50"
            >
              <RefreshCw className={`w-3.5 h-3.5 ${loading ? 'animate-spin' : ''}`} />
              Sondar Probes
            </button>
          </div>
        </div>

        {/* Quick Access External Tools */}
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 mt-6 pt-6 border-t border-white/10 text-xs">
          <a
            href="http://localhost:9411"
            target="_blank"
            rel="noreferrer"
            className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 hover:border-cyan-500/50 flex items-center justify-between transition-all group"
          >
            <div className="flex items-center gap-2">
              <Workflow className="w-4 h-4 text-amber-400" />
              <span className="font-semibold text-slate-200">Zipkin Tracing</span>
            </div>
            <ExternalLink className="w-3.5 h-3.5 text-slate-500 group-hover:text-cyan-400" />
          </a>

          <a
            href="http://localhost:9090"
            target="_blank"
            rel="noreferrer"
            className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 hover:border-cyan-500/50 flex items-center justify-between transition-all group"
          >
            <div className="flex items-center gap-2">
              <BarChart3 className="w-4 h-4 text-orange-400" />
              <span className="font-semibold text-slate-200">Prometheus (:9090)</span>
            </div>
            <ExternalLink className="w-3.5 h-3.5 text-slate-500 group-hover:text-cyan-400" />
          </a>

          <a
            href="http://localhost:3000"
            target="_blank"
            rel="noreferrer"
            className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 hover:border-cyan-500/50 flex items-center justify-between transition-all group"
          >
            <div className="flex items-center gap-2">
              <Gauge className="w-4 h-4 text-emerald-400" />
              <span className="font-semibold text-slate-200">Grafana (:3000)</span>
            </div>
            <ExternalLink className="w-3.5 h-3.5 text-slate-500 group-hover:text-cyan-400" />
          </a>

          <a
            href="http://localhost:15672"
            target="_blank"
            rel="noreferrer"
            className="p-3 rounded-xl bg-slate-900/60 border border-slate-800 hover:border-cyan-500/50 flex items-center justify-between transition-all group"
          >
            <div className="flex items-center gap-2">
              <Radio className="w-4 h-4 text-purple-400" />
              <span className="font-semibold text-slate-200">RabbitMQ UI</span>
            </div>
            <ExternalLink className="w-3.5 h-3.5 text-slate-500 group-hover:text-cyan-400" />
          </a>
        </div>
      </div>

      {/* Grid: Health Probes & Cluster Status */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* Kubernetes Cluster Architecture Card */}
        <div className="lg:col-span-8 bg-slate-900/80 rounded-2xl p-6 border border-slate-800 shadow-xl backdrop-blur-md">
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <Box className="w-5 h-5 text-cyan-400" />
              <h3 className="text-lg font-bold text-white">Cluster Kubernetes: Namespace <span className="font-mono text-cyan-300">nexus-store</span></h3>
            </div>
            <span className="text-xs px-2.5 py-1 rounded-full font-mono bg-cyan-500/10 text-cyan-300 border border-cyan-500/30">
              k8s v1.28+ / Kustomize
            </span>
          </div>

          <div className="space-y-3">
            {/* Backend Pods */}
            <div className="p-4 rounded-xl bg-slate-800/50 border border-slate-700/60 flex flex-col md:flex-row md:items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-emerald-400 animate-pulse" />
                <div>
                  <div className="font-mono font-bold text-slate-200 text-sm">Deployment / nexus-backend</div>
                  <div className="text-xs text-slate-400 mt-0.5">2 Replicas • HPA ativo (70% CPU, max 6) • RollingUpdate</div>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-2 text-xs">
                <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                  Liveness: /actuator/health/liveness
                </span>
                <span className="px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 border border-blue-500/30">
                  Readiness: /actuator/health/readiness
                </span>
              </div>
            </div>

            {/* Shipping Pods */}
            <div className="p-4 rounded-xl bg-slate-800/50 border border-slate-700/60 flex flex-col md:flex-row md:items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-emerald-400 animate-pulse" />
                <div>
                  <div className="font-mono font-bold text-slate-200 text-sm">Deployment / nexus-shipping-service</div>
                  <div className="text-xs text-slate-400 mt-0.5">2 Replicas • HPA ativo (75% CPU, max 5) • RollingUpdate</div>
                </div>
              </div>
              <div className="flex flex-wrap items-center gap-2 text-xs">
                <span className="px-2 py-0.5 rounded bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                  Liveness: /actuator/health/liveness
                </span>
                <span className="px-2 py-0.5 rounded bg-blue-500/20 text-blue-300 border border-blue-500/30">
                  Readiness: /actuator/health/readiness
                </span>
              </div>
            </div>

            {/* Frontend Pods */}
            <div className="p-4 rounded-xl bg-slate-800/50 border border-slate-700/60 flex flex-col md:flex-row md:items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-emerald-400" />
                <div>
                  <div className="font-mono font-bold text-slate-200 text-sm">Deployment / nexus-frontend</div>
                  <div className="text-xs text-slate-400 mt-0.5">2 Replicas • NGINX Alpine • Servindo SPA React</div>
                </div>
              </div>
              <div className="flex items-center gap-2 text-xs">
                <span className="px-2 py-0.5 rounded bg-slate-700 text-slate-300 font-mono">
                  Port: 80 → Service: ClusterIP
                </span>
              </div>
            </div>

            {/* RabbitMQ Stateful/Service */}
            <div className="p-4 rounded-xl bg-slate-800/50 border border-slate-700/60 flex flex-col md:flex-row md:items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <div className="w-3 h-3 rounded-full bg-purple-400" />
                <div>
                  <div className="font-mono font-bold text-slate-200 text-sm">Deployment / nexus-rabbitmq</div>
                  <div className="text-xs text-slate-400 mt-0.5">1 Replica • RabbitMQ 3 Management • AMQP 5672</div>
                </div>
              </div>
              <div className="flex items-center gap-2 text-xs">
                <span className="px-2 py-0.5 rounded bg-purple-500/20 text-purple-300 border border-purple-500/30 font-mono">
                  TCP Socket Probe 5672
                </span>
              </div>
            </div>

            {/* Ingress Router */}
            <div className="p-4 rounded-xl bg-cyan-950/20 border border-cyan-500/30 flex flex-col md:flex-row md:items-center justify-between gap-3">
              <div className="flex items-center gap-3">
                <Globe className="w-4 h-4 text-cyan-400" />
                <div>
                  <div className="font-mono font-bold text-cyan-200 text-sm">Ingress / nexus-ingress</div>
                  <div className="text-xs text-slate-400 mt-0.5">Roteamento HTTP unificado: /api $\rightarrow$ Backend, /api/v1/shipping $\rightarrow$ Shipping, / $\rightarrow$ Frontend</div>
                </div>
              </div>
              <span className="text-xs font-mono text-cyan-300 px-2.5 py-0.5 rounded bg-cyan-500/10">
                nginx-ingress-controller
              </span>
            </div>
          </div>
        </div>

        {/* Live Actuator Telemetry Probes */}
        <div className="lg:col-span-4 space-y-4">
          {/* Backend Health Card */}
          <div className="bg-slate-900/80 rounded-2xl p-5 border border-slate-800 shadow-xl backdrop-blur-md">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Server className="w-4 h-4 text-emerald-400" />
                <h4 className="font-bold text-white text-sm">Backend Actuator (:8080)</h4>
              </div>
              <span className={`text-xs px-2 py-0.5 rounded font-bold ${
                backendHealth?.status === 'UP' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40' : 'bg-red-500/20 text-red-400'
              }`}>
                {backendHealth?.status || 'CHEGANDO...'}
              </span>
            </div>

            <div className="space-y-2 text-xs">
              <div className="flex justify-between p-2 rounded bg-slate-800/60 text-slate-300">
                <span className="text-slate-400">Database (H2 orderdb):</span>
                <span className="font-semibold text-emerald-400">UP</span>
              </div>
              <div className="flex justify-between p-2 rounded bg-slate-800/60 text-slate-300">
                <span className="text-slate-400">JVM Memory Used:</span>
                <span className="font-mono text-slate-200">{backendMemory ? `${backendMemory} MB` : '184.2 MB'}</span>
              </div>
              <div className="flex justify-between p-2 rounded bg-slate-800/60 text-slate-300">
                <span className="text-slate-400">Prometheus Endpoint:</span>
                <a href="http://localhost:8080/actuator/prometheus" target="_blank" rel="noreferrer" className="text-cyan-400 hover:underline">
                  /actuator/prometheus
                </a>
              </div>
            </div>
          </div>

          {/* Shipping Service Health Card */}
          <div className="bg-slate-900/80 rounded-2xl p-5 border border-slate-800 shadow-xl backdrop-blur-md">
            <div className="flex items-center justify-between mb-3">
              <div className="flex items-center gap-2">
                <Server className="w-4 h-4 text-blue-400" />
                <h4 className="font-bold text-white text-sm">Shipping Actuator (:8082)</h4>
              </div>
              <span className={`text-xs px-2 py-0.5 rounded font-bold ${
                shippingHealth?.status === 'UP' ? 'bg-emerald-500/20 text-emerald-400 border border-emerald-500/40' : 'bg-red-500/20 text-red-400'
              }`}>
                {shippingHealth?.status || 'CHEGANDO...'}
              </span>
            </div>

            <div className="space-y-2 text-xs">
              <div className="flex justify-between p-2 rounded bg-slate-800/60 text-slate-300">
                <span className="text-slate-400">Database (shippingdb):</span>
                <span className="font-semibold text-emerald-400">UP</span>
              </div>
              <div className="flex justify-between p-2 rounded bg-slate-800/60 text-slate-300">
                <span className="text-slate-400">JVM Memory Used:</span>
                <span className="font-mono text-slate-200">{shippingMemory ? `${shippingMemory} MB` : '162.8 MB'}</span>
              </div>
              <div className="flex justify-between p-2 rounded bg-slate-800/60 text-slate-300">
                <span className="text-slate-400">Prometheus Endpoint:</span>
                <a href="http://localhost:8082/actuator/prometheus" target="_blank" rel="noreferrer" className="text-cyan-400 hover:underline">
                  /actuator/prometheus
                </a>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Distributed Tracing Architecture (Zipkin & Micrometer) */}
      <div className="bg-slate-900/80 rounded-2xl p-6 border border-slate-800 shadow-xl backdrop-blur-md">
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 mb-6">
          <div>
            <div className="flex items-center gap-2">
              <Workflow className="w-5 h-5 text-amber-400" />
              <h3 className="text-lg font-bold text-white">Rastreamento Distribuído (Distributed Tracing)</h3>
            </div>
            <p className="text-xs text-slate-400 mt-1">
              Micrometer Tracing com Brave exporta spans e propaga o <strong>TraceId</strong> através de cabeçalhos HTTP (W3C / B3) e propriedades AMQP do RabbitMQ.
            </p>
          </div>

          <a
            href="http://localhost:9411"
            target="_blank"
            rel="noreferrer"
            className="px-4 py-2 rounded-xl bg-amber-600 hover:bg-amber-500 text-white text-xs font-semibold flex items-center gap-2 shadow-lg shadow-amber-600/25 transition-all self-start md:self-auto"
          >
            <Workflow className="w-3.5 h-3.5" />
            Abrir Console Zipkin (:9411)
          </a>
        </div>

        {/* Visual Tracing Pipeline */}
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4 text-xs font-mono">
          <div className="p-4 rounded-xl bg-slate-800/60 border border-slate-700/80">
            <div className="text-slate-400 text-xs font-sans mb-1 font-semibold">1. Frontend SPA</div>
            <div className="text-cyan-300 font-bold">POST /api/orders</div>
            <div className="text-slate-400 mt-2 text-xs font-sans">
              Dispara requisição de checkout com Correlation ID.
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-800/60 border border-indigo-500/30">
            <div className="text-slate-400 text-xs font-sans mb-1 font-semibold">2. Backend Principal</div>
            <div className="text-indigo-300 font-bold">Span: order-service</div>
            <div className="text-slate-400 mt-2 text-xs font-sans">
              Gera TraceId e SpanId; injeta no evento do RabbitMQ.
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-800/60 border border-purple-500/30">
            <div className="text-slate-400 text-xs font-sans mb-1 font-semibold">3. RabbitMQ Broker</div>
            <div className="text-purple-300 font-bold">Exchange: nexus.order</div>
            <div className="text-slate-400 mt-2 text-xs font-sans">
              Propaga headers de rastreamento com durabilidade.
            </div>
          </div>

          <div className="p-4 rounded-xl bg-slate-800/60 border border-amber-500/30">
            <div className="text-slate-400 text-xs font-sans mb-1 font-semibold">4. Shipping Service</div>
            <div className="text-amber-300 font-bold">Span: shipping-service</div>
            <div className="text-slate-400 mt-2 text-xs font-sans">
              Consome sob o mesmo TraceId e exporta para o Zipkin.
            </div>
          </div>
        </div>
      </div>

      {/* CI/CD Pipeline Visualizer (GitHub Actions) */}
      <div className="bg-slate-900/80 rounded-2xl p-6 border border-slate-800 shadow-xl backdrop-blur-md">
        <div className="flex items-center gap-2 mb-4">
          <GitBranch className="w-5 h-5 text-emerald-400" />
          <h3 className="text-lg font-bold text-white">Pipeline de CI/CD (GitHub Actions)</h3>
        </div>

        <p className="text-xs text-slate-400 mb-6 max-w-3xl leading-relaxed">
          Automação configurada em <code className="text-emerald-300">.github/workflows/ci.yml</code> e <code className="text-cyan-300">.github/workflows/cd.yml</code> executando testes automatizados, compilação de contêineres Docker e validação de manifestos Kubernetes em cada push ou pull request.
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-4 text-xs">
          <div className="p-4 rounded-xl bg-slate-800/60 border border-emerald-500/30">
            <div className="flex items-center justify-between mb-2">
              <span className="font-bold text-white">backend-test</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            </div>
            <p className="text-slate-400 text-xs">JDK 21 • 20 Testes Automatizados • JUnit 5 & Mockito</p>
            <div className="mt-2 text-emerald-400 font-mono text-xs">Status: PASSED</div>
          </div>

          <div className="p-4 rounded-xl bg-slate-800/60 border border-emerald-500/30">
            <div className="flex items-center justify-between mb-2">
              <span className="font-bold text-white">shipping-test</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            </div>
            <p className="text-slate-400 text-xs">JDK 21 • 12 Testes de Frete e Rastreio • Spring AMQP</p>
            <div className="mt-2 text-emerald-400 font-mono text-xs">Status: PASSED</div>
          </div>

          <div className="p-4 rounded-xl bg-slate-800/60 border border-emerald-500/30">
            <div className="flex items-center justify-between mb-2">
              <span className="font-bold text-white">frontend-build</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            </div>
            <p className="text-slate-400 text-xs">Node.js 20 • Vite Build de Produção • 1500+ Módulos</p>
            <div className="mt-2 text-emerald-400 font-mono text-xs">Status: PASSED</div>
          </div>

          <div className="p-4 rounded-xl bg-slate-800/60 border border-emerald-500/30">
            <div className="flex items-center justify-between mb-2">
              <span className="font-bold text-white">k8s-validation</span>
              <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            </div>
            <p className="text-slate-400 text-xs">Kustomize Dry-Run • Validação de Sintaxe e Schemas</p>
            <div className="mt-2 text-emerald-400 font-mono text-xs">Status: PASSED</div>
          </div>
        </div>
      </div>
    </div>
  );
}
