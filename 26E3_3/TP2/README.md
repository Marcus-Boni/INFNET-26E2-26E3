# TechMarket — Microsserviços com Docker e Kubernetes

> **Trabalho Prático 2 (TP2) — Containers, Docker Compose, Kubernetes & Load Balancing**  
> **Instituição:** Instituto Infnet | **Curso:** Engenharia de Software  
> **Aluno:** Marcus Boni (`marcus.boni@al.infnet.edu.br`)  
> **Baseado em:** TP1 (TechMarket E-commerce Architecture)

---

## 1. Visão Geral da Arquitetura

O projeto **TechMarket TP2** demonstra a evolução de uma arquitetura de microsserviços desde a execução local até a containerização com Docker, orquestração declarativa com Docker Compose e implantação no Kubernetes com Service Discovery e Balanceamento de Carga.

```mermaid
graph TD
    subgraph "Clientes Externos"
        Client["Navegador / cURL / requests.http"]
    end

    subgraph "Docker Compose / Kubernetes Cluster"
        subgraph "order-service"
            OrderApp["order-service (Porta 8082)<br/>Spring Boot 3 + Java 21"]
        end

        subgraph "Service Discovery & Load Balancing (Kubernetes Service)"
            K8sService["Service: product-service:8081<br/>(ClusterIP / NodePort 30081)"]
        end

        subgraph "product-service (3 Réplicas)"
            Pod1["Pod 1: product-service-pod-1"]
            Pod2["Pod 2: product-service-pod-2"]
            Pod3["Pod 3: product-service-pod-3"]
        end
    end

    Client -->|POST /orders :8082| OrderApp
    Client -->|GET /products :8081| K8sService

    OrderApp -->|HTTP GET /products/{id}<br/>Service Discovery via CoreDNS| K8sService
    K8sService -.->|Round-Robin| Pod1
    K8sService -.->|Round-Robin| Pod2
    K8sService -.->|Round-Robin| Pod3
```

---

## 2. Estrutura do Diretório `TP2`

```
TP2/
├── pom.xml                                   # Parent POM agregador Maven
├── docker-compose.yml                        # Orquestração multicontainer dos 2 microsserviços
├── requests.http                             # Suíte de testes HTTP pronta para execução
├── README.md                                 # Guia rápido de execução
├── DOCUMENTO_TP2.md                          # Relatório acadêmico completo com as 14 etapas
├── product-service/                          # Microsserviço de Produtos (porta 8081)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/...
├── order-service/                            # Microsserviço de Pedidos (porta 8082)
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/...
└── k8s/                                      # Manifestos declarativos Kubernetes
    ├── product-service-deployment.yaml       # Deployment com 3 réplicas (Etapas 11 e 13)
    ├── product-service-service.yaml          # Service com NodePort 30081
    ├── order-service-deployment.yaml         # Deployment do order-service comunicando via DNS
    └── order-service-service.yaml            # Service com NodePort 30082
```

---

## 3. Como Executar

### 3.1. Pré-requisitos
- **Java 21** e **Maven 3.9+**
- **Docker Desktop** (com Docker Engine ativo)
- **Kubernetes** (habilitado no Docker Desktop ou Minikube)

---

### 3.2. Opção 1: Execução Local (Spring Boot)

1. Compile todos os módulos:
   ```bash
   mvn clean package -DskipTests
   ```
2. Em um terminal, inicie o `product-service`:
   ```bash
   cd product-service
   mvn spring-boot:run
   ```
   *(Acessível em `http://localhost:8081`)*

3. Em outro terminal, inicie o `order-service`:
   ```bash
   cd order-service
   mvn spring-boot:run
   ```
   *(Acessível em `http://localhost:8082`)*

---

### 3.3. Opção 2: Containers Manuais com Docker Network (Etapas 5, 7 e 8)

1. Construa as imagens Docker:
   ```bash
   docker build -t product-image:latest ./product-service
   docker build -t order-image:latest ./order-service
   ```

2. Crie a rede personalizada do Docker:
   ```bash
   docker network create techmarket-net
   ```

3. Inicie o container do `product-service`:
   ```bash
   docker run -d --name product-service --network techmarket-net -p 8081:8081 product-image:latest
   ```

4. Inicie o container do `order-service` apontando para o hostname do container anterior:
   ```bash
   docker run -d --name order-service --network techmarket-net -p 8082:8082 -e PRODUCT_SERVICE_URL=http://product-service:8081/products order-image:latest
   ```

---

### 3.4. Opção 3: Execução Declarativa com Docker Compose (Etapa 9)

Inicie toda a aplicação com um único comando:
```bash
docker compose up -d
```

Para verificar o status:
```bash
docker compose ps
```

Para ver os logs em tempo real:
```bash
docker compose logs -f
```

Para parar a aplicação:
```bash
docker compose down
```

---

### 3.5. Opção 4: Execução no Kubernetes (Etapas 11, 12 e 13)

1. Certifique-se de que o cluster Kubernetes está ativo (`kubectl get nodes`).
2. Aplique os manifestos do `product-service`:
   ```bash
   kubectl apply -f k8s/product-service-deployment.yaml
   kubectl apply -f k8s/product-service-service.yaml
   ```

3. Aplique os manifestos do `order-service`:
   ```bash
   kubectl apply -f k8s/order-service-deployment.yaml
   kubectl apply -f k8s/order-service-service.yaml
   ```

4. Verifique os Pods e Services em execução:
   ```bash
   kubectl get pods -o wide
   kubectl get services
   ```

---

## 4. Endpoints Principais

### `product-service` (Porta 8081 ou NodePort 30081)
- `GET /products`: Lista todos os produtos
- `GET /products/10`: Busca produto específico (exemplo do enunciado)
- `POST /products`: Cadastra novo produto
- `GET /products/info`: Informações da réplica / Pod que processou a requisição

### `order-service` (Porta 8082 ou NodePort 30082)
- `GET /orders`: Lista todos os pedidos
- `GET /orders/{id}`: Detalhes do pedido por ID
- `POST /orders`: Cria um pedido (valida produto no `product-service` via HTTP remoto)
- `GET /orders/info`: Status e URL do `product-service` configurada
