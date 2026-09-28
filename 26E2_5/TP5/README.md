# Nexus Store — TP5: Conteinerização, Orquestração e Observabilidade

> 🎥 **Vídeo de Demonstração da Execução (Google Drive):**  
> **Link:** [https://drive.google.com/file/d/1khsR6jYwMQvs_zZ5BL5l68umr2OhTjTu/view?usp=sharing](https://drive.google.com/file/d/1khsR6jYwMQvs_zZ5BL5l68umr2OhTjTu/view?usp=sharing)

---

## 📌 Objetivo da Etapa (TP5)

Preparar o sistema desenvolvido para operação através de **conteinerização**, **monitoramento**, **orquestração** e **testes automatizados**, consolidando os microsserviços evoluídos no TP4 (Arquitetura Orientada a Eventos com RabbitMQ) para ambientes de produção resilientes.

### Subcompetências Desenvolvidas:
1. **Implantação com Docker e Kubernetes:**
   - Conteinerização dos microsserviços com Dockerfiles Multi-stage (Java 21 / Vite + NGINX).
   - Orquestração declarativa via Kubernetes (Namespace, Deployments, Services, Ingress NGINX, HPA e Probes Liveness/Readiness).
2. **Monitoramento e Observabilidade de Microsserviços:**
   - Coleta de métricas e telemetria com **Spring Boot Actuator** e exportador **Micrometer Prometheus**.
   - Servidor **Prometheus** com scraping configurado a cada 15s.
   - Dashboards de métricas via **Grafana**.
   - Rastreamento distribuído de ponta a ponta com **Zipkin Tracing** (propagação de TraceId/SpanId HTTP e AMQP).
3. **Gestão de Configuração e Versionamento:**
   - Esteiras automatizadas de CI/CD via **GitHub Actions** (`.github/workflows/ci.yml`).
   - Validação contínua de testes unitários/integração do Backend, Shipping Service, build do Frontend e dry-run dos manifestos do Kubernetes.

---

## 🚀 Como Executar o Projeto

### Pré-requisitos:
- **Docker Desktop** (com suporte a Linux containers)
- **Docker Compose v2+**

### Inicialização Rápida (Recomendado):

Execute o script `.bat` na raiz do diretório `TP5`:

```bash
start-all-docker.bat
```

Ou execute diretamente via terminal:

```bash
docker compose up -d --build
```

Para encerrar todos os serviços:

```bash
stop-all.bat
# ou
docker compose down
```

---

## 🌐 URLs dos Serviços e Dashboards

| Serviço | Porta / URL | Finalidade |
| :--- | :--- | :--- |
| **Frontend SPA (React + Vite)** | [http://localhost:5173](http://localhost:5173) | Interface do cliente, catálogo, pedidos, eventos e devops |
| **Backend Orders & Catalog API** | [http://localhost:8080](http://localhost:8080) | Microsserviço de produtos, pedidos e checkout |
| **Backend Actuator Health** | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) | Sonda de saúde e probes do Kubernetes |
| **Backend Prometheus Metrics** | [http://localhost:8080/actuator/prometheus](http://localhost:8080/actuator/prometheus) | Métricas JVM, HTTP e mensageria |
| **Shipping Logistics Service** | [http://localhost:8082](http://localhost:8082) | Microsserviço de frete, cálculo e rastreamento |
| **Shipping Actuator Health** | [http://localhost:8082/actuator/health](http://localhost:8082/actuator/health) | Sonda de saúde do microsserviço de frete |
| **RabbitMQ Management UI** | [http://localhost:15672](http://localhost:15672) | Gestão de exchanges, filas e dead letter queue (`guest`/`guest`) |
| **Zipkin Tracing UI** | [http://localhost:9411](http://localhost:9411) | Rastreamento distribuído de spans e latência |
| **Prometheus Server UI** | [http://localhost:9090](http://localhost:9090) | Consultas PromQL e status dos targets monitorados |
| **Grafana Dashboards** | [http://localhost:3001](http://localhost:3001) | Painéis de telemetria e métricas visuais |

---

## 📂 Manifestos Kubernetes (`k8s/`)

O diretório [`k8s/`](./k8s) contém os manifestos declarativos estruturados com **Kustomize**:

```
k8s/
├── namespace.yaml                  # Namespace isolado 'nexus-store'
├── backend-deployment.yaml         # Deployment com 2 réplicas e probes /actuator/health
├── backend-service.yaml            # Service ClusterIP na porta 8080
├── backend-hpa.yaml                # Autoscaling horizontal (min 2, max 6 réplicas)
├── shipping-deployment.yaml        # Deployment com 2 réplicas e probes /actuator/health
├── shipping-service.yaml           # Service ClusterIP na porta 8082
├── shipping-hpa.yaml               # Autoscaling horizontal (min 2, max 5 réplicas)
├── frontend-deployment.yaml        # Deployment com 2 réplicas (NGINX)
├── frontend-service.yaml           # Service ClusterIP na porta 80
├── rabbitmq-deployment.yaml        # Broker RabbitMQ e Service ClusterIP (5672/15672)
├── ingress.yaml                    # Ingress Controller NGINX unificando rotas
└── kustomization.yaml              # Agregador Kustomize
```

Para aplicar no cluster Kubernetes:

```bash
kubectl apply -k k8s/
```

---

## 📖 Documentação Detalhada

Para uma análise aprofundada dos trade-offs, diagramas de sequência, topologia AMQP, Dead Letter Queues (DLQ) e pipelines de CI/CD, consulte o arquivo:
- [**ARCHITECTURE.md**](./ARCHITECTURE.md)
