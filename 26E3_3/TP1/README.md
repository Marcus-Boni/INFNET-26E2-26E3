# TechMarket — Arquitetura de Microservices

> **Trabalho Prático 1 (TP1) — Entrega 1: Proposta e Arquitetura Inicial de Microservices**  
> **Turma:** Segunda e Quarta | **Modalidade:** Individual  
> **Aluno:** Marcus Boni (`marcus.boni@al.infnet.edu.br`)  
> **Repositório:** [https://github.com/Marcus-Boni/INFNET-26E2-26E3](https://github.com/Marcus-Boni/INFNET-26E2-26E3)

---

## 1. Descrição do Projeto

O **TechMarket** é uma plataforma de e-commerce focada em produtos de tecnologia e informática (hardware, smartphones, computadores e periféricos). O sistema resolve dois desafios centrais:

1. **Catálogo de produtos com fichas técnicas altamente dinâmicas e heterogêneas:** Diferentes categorias de eletrônicos possuem especificações técnicas completamente distintas (ex.: placas de vídeo têm VRAM, TDP e clocks; monitores têm resolução, taxa de Hz e tipo de painel). Para isso, foi adotado um banco **NoSQL (MongoDB)** orientado a documentos semi-estruturados.
2. **Ciclo de vida transacional e estrito de pedidos:** Criação, cálculo, itens e atualizações de status de pedidos que demandam integridade referencial e garantias ACID, utilizando um banco **SQL Relacional (PostgreSQL)**.

A arquitetura adota o padrão **Database per Service**, comunicação síncrona com **Spring Cloud OpenFeign**, tolerância a falhas e mitigação de falhas em cascata com **Resilience4j (Circuit Breaker, Retry e Fallback)**, registro e descoberta dinâmica com **Netflix Eureka Server** e ponto de entrada unificado com **Spring Cloud Gateway**.

---

## 2. Arquitetura

```
+-----------------------------------------------------------------------------------+
|                                 CLIENTE EXTERNO                                   |
|                     (Postman / cURL / Frontend / requests.http)                   |
+------------------------------------------+----------------------------------------+
                                           | HTTP Requests (:8080)
                                           v
                     +--------------------------------------------+
                     |                API GATEWAY                 |
                     |         (Spring Cloud Gateway :8080)       |
                     +-----+--------------------------------+-----+
                           |                                |
        /api/products/**   |                                |   /api/orders/**
                           v                                v
+------------------------------------+            +------------------------------------+
|          CATALOG-SERVICE           |            |           ORDER-SERVICE            |
|       (Spring Boot :8082)          |            |        (Spring Boot :8081)         |
+------------------+-----------------+            +------------------+-----------------+
                   |                                                 |
                   |                                                 | [OpenFeign Client]
                   | <-----------------------------------------------+ Circuit Breaker
                   |                                                   & Fallback (Resilience4j)
                   v                                                 v
+------------------------------------+            +------------------------------------+
|              MONGODB               |            |             POSTGRESQL             |
|         (NoSQL :27017)             |            |            (SQL :5432)             |
|          [catalog_db]              |            |             [order_db]             |
+------------------------------------+            +------------------------------------+
                   ^                                                 ^
                   |                                                 |
                   +------------------------+------------------------+
                                            | Registro & Descoberta
                                            v
                     +--------------------------------------------+
                     |              DISCOVERY SERVER              |
                     |        (Netflix Eureka Server :8761)       |
                     +--------------------------------------------+
```

---

## 3. Microservices e Componentes

| Serviço | Tipo | Porta | Banco de Dados | Responsabilidade |
| :--- | :--- | :--- | :--- | :--- |
| **`discovery-server`** | Infraestrutura | `8761` | N/A | Service Registry e Discovery com Netflix Eureka Server |
| **`api-gateway`** | Infraestrutura | `8080` | N/A | Ponto único de entrada, roteamento reativo e load balancing |
| **`catalog-service`** | Negócio | `8082` | **MongoDB (NoSQL)** | Gestão de produtos, tags e especificações técnicas dinâmicas |
| **`order-service`** | Negócio | `8081` | **PostgreSQL (SQL)** | Gestão de pedidos, cálculo de itens e integração resiliente |

---

## 4. Persistência de Dados e Justificativa do NoSQL

### 4.1. `catalog-service` $\rightarrow$ MongoDB (NoSQL Document Store)
- **Justificativa Técnica:** Produtos de tecnologia possuem especificações técnicas heterogêneas que variam radicalmente por categoria. No MongoDB, os dados são armazenados em documentos BSON flexíveis com `specifications: Map<String, Object>` e `tags: List<String>`. Isso elimina o anti-padrão EAV (*Entity-Attribute-Value*) de bancos relacionais e dispensa migrações de DDL (`ALTER TABLE`) a cada novo atributo técnico.
- **Benefício de Consulta:** Otimizado para alta frequência de leituras (*read-heavy*), permitindo recuperar o produto completo e suas características em uma única operação de I/O, além de buscas flexíveis por categorias e tags.

### 4.2. `order-service` $\rightarrow$ PostgreSQL (SQL Relacional)
- **Justificativa Técnica:** Pedidos, itens de pedidos, valores e clientes exigem estrita integridade referencial, constraints e propriedades ACID para evitar inconsistências em transações de compra.

---

## 5. Estratégia de Resiliência entre Microservices

- **Cenário:** O `order-service` precisa consultar o preço atual e nome do produto no `catalog-service` no momento de criação do pedido.
- **Padrões Implementados:**
  1. **Circuit Breaker (Resilience4j):** Monitora a taxa de falha das chamadas remotas. Se a taxa de erro ultrapassar 50% em uma janela de chamadas, o circuito abre (*OPEN*), evitando sobrecarregar o serviço de catálogo e prevenindo esgotamento de threads no serviço de pedidos.
  2. **Fallback Gracioso:** Quando o circuito está aberto ou ocorre falha de conexão/timeout, o método de fallback é acionado automaticamente. Ele recupera os dados de um cache resiliente em memória ou utiliza dados de contingência seguros, retornando o pedido criado com o status `"catalogServiceStatus": "DEGRADED (Circuit Breaker / Fallback)"` sem quebrar com erro 500.
  3. **Retry:** Repete até 3 tentativas com backoff exponencial para falhas transientes.
  4. **Timeout:** Limite estrito de 3 segundos para leitura de resposta.

---

## 6. Como Executar o Projeto

### 6.1. Pré-requisitos
- **Java 21 (JDK 21)**
- **Maven 3.9+**
- **Docker e Docker Compose**

### 6.2. Passo 1: Subir os Bancos de Dados via Docker
No diretório raiz do projeto:
```bash
docker compose up -d
```
Verifique se os containers `techmarket-catalog-mongodb` (porta 27017) e `techmarket-order-postgres` (porta 5432) estão em execução:
```bash
docker compose ps
```

### 6.3. Passo 2: Compilar os Microservices
```bash
mvn clean package -DskipTests
```

### 6.4. Passo 3: Iniciar os Serviços (em terminais separados)

1. **Discovery Server:**
   ```bash
   cd discovery-server
   mvn spring-boot:run
   ```
   *(Aguarde ~10s até inicializar na porta 8761)*

2. **Catalog Service:**
   ```bash
   cd catalog-service
   mvn spring-boot:run
   ```

3. **Order Service:**
   ```bash
   cd order-service
   mvn spring-boot:run
   ```

4. **API Gateway:**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

---

## 7. Discovery Server (Eureka)

- **URL do Dashboard:** [http://localhost:8761](http://localhost:8761)
- **Serviços Registrados Automaticamente:**
  - `API-GATEWAY`
  - `CATALOG-SERVICE`
  - `ORDER-SERVICE`

---

## 8. API Gateway e Rotas Configuradas

Todas as chamadas externas devem ser realizadas através do **API Gateway (porta 8080)**:

| Rota Externa (Gateway) | Destino Lógico | Microservice |
| :--- | :--- | :--- |
| `http://localhost:8080/api/products/**` | `lb://catalog-service` | `catalog-service` (:8082) |
| `http://localhost:8080/api/orders/**` | `lb://order-service` | `order-service` (:8081) |

---

## 9. Exemplos de Requisições para Teste

Você pode utilizar a extensão **REST Client** (arquivo [requests.http](file:///requests.http)) ou executar via `curl` / PowerShell:

### 9.1. Listar Produtos do Catálogo (via Gateway)
```bash
curl -X GET http://localhost:8080/api/products
```

### 9.2. Cadastrar Novo Produto com Atributos Flexíveis (MongoDB NoSQL)
```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Placa de Vídeo RTX 4090 OC 24GB",
    "description": "Placa topo de linha para renderização 3D e 4K Ultra",
    "category": "Componentes",
    "price": 14999.90,
    "stockQuantity": 8,
    "specifications": {
      "vram": "24GB GDDR6X",
      "cudaCores": 16384,
      "memoryBus": "384-bit",
      "powerConsumption": "450W"
    },
    "tags": ["gpu", "nvidia", "rtx4090", "hardware"]
  }'
```

### 9.3. Criar Pedido (comunicação Feign com Catalog Service via Gateway)
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CLI-1001",
    "customerEmail": "marcus.boni@aluno.infnet.edu.br",
    "items": [
      {
        "productId": "<ID_DO_PRODUTO_CRIADO>",
        "quantity": 1
      }
    ]
  }'
```

### 9.4. Listar Pedidos (via Gateway)
```bash
curl -X GET http://localhost:8080/api/orders
```

### 9.5. Atualizar Status do Pedido (via Gateway)
```bash
curl -X PATCH "http://localhost:8080/api/orders/1/status?status=CONFIRMED"
```

### 9.6. Teste de Resiliência (Simulação de Falha / Fallback)
Para testar a resiliência:
1. Pare o processo do `catalog-service`.
2. Envie uma requisição de criação de pedido para `POST http://localhost:8080/api/orders`.
3. O `order-service` acionará o Fallback do Circuit Breaker, retornando HTTP 201 com o pedido criado e `"catalogServiceStatus": "DEGRADED (Circuit Breaker / Fallback)"` sem travar a aplicação.
4. Verifique as métricas em `http://localhost:8081/actuator/circuitbreakers`.

---

## 10. Estrutura do Repositório

```
TP1/
├── pom.xml                     # Parent POM multi-módulo
├── docker-compose.yml          # Definição dos bancos MongoDB e PostgreSQL
├── requests.http               # Arquivo com suíte de testes HTTP pronta
├── README.md                   # Documentação do projeto
├── DOCUMENTO_PROPOSTA_TP1.md   # Proposta formal para entrega
├── discovery-server/           # Netflix Eureka Server (8761)
├── api-gateway/                # Spring Cloud Gateway (8080)
├── catalog-service/            # Microservice NoSQL MongoDB (8082)
└── order-service/              # Microservice SQL PostgreSQL + Resiliência (8081)
```
