# DOCUMENTO DE PROPOSTA E ARQUITETURA INICIAL DE MICROSERVICES
## Entrega 1 (TP1) — Arquitetura de Microservices

### 1. Identificação do Trabalho e Equipe

| Campo | Detalhes |
| :--- | :--- |
| **Instituição** | Instituto Infnet |
| **Curso / Período** | Engenharia de Software / Disciplinada |
| **Turma** | **Segunda e Quarta** |
| **Modalidade** | **Individual** |
| **Aluno** | Marcus Boni |
| **E-mail** | `marcus.boni@al.infnet.edu.br` |
| **Repositório do Projeto** | [GitHub - INFNET-26E2-26E3 / TP1](https://github.com/Marcus-Boni/INFNET-26E2-26E3) |
| **Papéis e Responsabilidades** | Responsável único por API Gateway, Discovery Server, Catalog Service, Order Service, bancos de dados, resiliência e documentação |

---

### 2. Tema do Projeto e Descrição do Domínio

#### 2.1. Nome do Projeto
**TechMarket — Plataforma de E-commerce de Tecnologia e Catálogo Inteligente**

#### 2.2. O Problema que o Sistema Resolve
No segmento de comércio eletrônico especializado em tecnologia e eletrônicos, os produtos apresentam características técnicas com esquemas altamente heterogêneos. Por exemplo, uma placa de vídeo requer campos como quantidade de VRAM, barramento de memória, consumo de energia e suporte a Ray Tracing; enquanto um monitor exige taxa de atualização, curvatura e tipo de painel; e um processador exige socket, contagem de núcleos e TDP. 

Modelar esse catálogo flexível em um banco de dados relacional tradicional frequentemente resulta no anti-padrão EAV (*Entity-Attribute-Value*) ou em tabelas com dezenas de colunas nulas e constantes migrações DDL que degradam a performance.

Adicionalmente, as transações de compra (pedidos, itens de pedidos e totalizadores) exigem rigorosa consistência transacional e integridade referencial ACID. 

A arquitetura monolítica tradicional acopla essas duas naturezas distintas de carga de trabalho e estrutura de dados, gerando gargalos de escalabilidade e pontos únicos de falha. A solução proposta adota uma arquitetura baseada em **Microservices com Persistência Poliglota**, separando o catálogo dinâmico de alta leitura e esquema flexível da gestão transacional de pedidos.

#### 2.3. Usuários Principais
1. **Clientes Finais:** Navegam pelo catálogo com filtros por categoria e tags, visualizam fichas técnicas ricas e realizam pedidos de compra.
2. **Administradores do Sistema / Gerentes de Catálogo:** Cadastram novos produtos tecnológicos com especificações dinâmicas e acompanham o ciclo de vida dos pedidos.

#### 2.4. Principais Funcionalidades Previstas
- **Gestão de Catálogo de Produtos:** Cadastro, listagem e filtros dinâmicos de produtos com especificações técnicas chave-valor flexíveis e tags.
- **Gestão e Processamento de Pedidos:** Criação de pedidos com validação síncrona de itens junto ao catálogo, cálculo de subtotal/total e atualização de status do ciclo de vida (*PENDING*, *CONFIRMED*, *CANCELLED*, *COMPLETED*).
- **Roteamento Centralizado e Seguro:** Ponto único de entrada via API Gateway.
- **Registro e Descoberta Dinâmica:** Resolução dinâmica de instâncias de serviço por nome lógico via Discovery Server.
- **Comunicação Resiliente com Fallback:** Proteção contra falhas em cascata com Circuit Breaker caso o serviço de catálogo sofra instabilidade ou latência.

#### 2.5. Por que o Tema Faz Sentido para Microservices?
- **Padrões de Carga Divergentes:** O catálogo de produtos é caracterizado por altíssima taxa de leitura (*read-heavy*) e necessidade de escalabilidade horizontal elástica. O serviço de pedidos é transacional (*write-heavy/ACID*), demandando consistência estrita.
- **Heterogeneidade de Dados:** O catálogo se beneficia de um modelo NoSQL orientado a documentos (*schema-less*), enquanto os pedidos necessitam de um modelo relacional SQL com constraints e integridade referencial.
- **Isolamento de Falhas:** Uma sobrecarga ou indisponibilidade no catálogo não deve derrubar o processamento de pedidos nem travar a camada de borda (Gateway).

---

### 3. Definição dos Microservices e Infraestrutura

```mermaid
graph TD
    Client["Cliente Externo (Postman / Frontend)"] -->|Requisições HTTP :8080| Gateway["API Gateway :8080<br/>(Spring Cloud Gateway)"]

    subgraph "Infraestrutura de Descoberta"
        Eureka["Discovery Server :8761<br/>(Netflix Eureka Server)"]
    end

    Gateway -.->|Resolve rotas dinamicamente| Eureka
    CatalogService -.->|Registra 'CATALOG-SERVICE'| Eureka
    OrderService -.->|Registra 'ORDER-SERVICE'| Eureka

    Gateway -->|/api/products/**| CatalogService["catalog-service :8082<br/>(Spring Boot + MongoRepository)"]
    Gateway -->|/api/orders/**| OrderService["order-service :8081<br/>(Spring Boot + Spring Data JPA)"]

    OrderService -->|OpenFeign + Resilience4j<br/>(Circuit Breaker + Fallback + Timeout)| CatalogService

    CatalogService -->|Persistência Documental| DB_Catalog[(MongoDB :27017<br/>catalog_db)]
    OrderService -->|Persistência Relacional| DB_Order[(PostgreSQL :5432<br/>order_db)]
```

#### 3.1. Quadro Resumo dos Serviços

| Serviço | Tipo | Porta | Banco de Dados / Tipo | Responsabilidade Principal |
| :--- | :--- | :--- | :--- | :--- |
| **`discovery-server`** | Infraestrutura | `8761` | N/A | Service Registry e Discovery (Netflix Eureka) |
| **`api-gateway`** | Infraestrutura | `8080` | N/A | Ponto único de entrada, roteamento reativo e load balancing |
| **`catalog-service`** | Negócio | `8082` | **MongoDB (NoSQL Document)** | Gerenciamento do catálogo de produtos e fichas técnicas flexíveis |
| **`order-service`** | Negócio | `8081` | **PostgreSQL (SQL Relacional)** | Gestão do ciclo de vida transacional de pedidos e itens |

---

### 4. Detalhamento dos Microservices de Negócio

#### 4.1. `catalog-service`
- **Responsabilidade:** Responsável pela manutenção dos dados do catálogo de produtos, fichas técnicas dinâmicas, controle de estoque e filtros por tags/categorias.
- **Entidades Manipuladas:**
  - `Product`: Identificador único (`id`), nome (`name`), descrição (`description`), categoria (`category`), preço unitário (`price`), quantidade em estoque (`stockQuantity`), status ativo (`active`), especificações técnicas (`specifications` - `Map<String, Object>`), etiquetas (`tags` - `List<String>`), timestamps (`createdAt`, `updatedAt`).
- **Endpoints:**
  - `POST /api/products`: Cadastro de novos produtos.
  - `GET /api/products`: Listagem de produtos (suporta `?category=` e `?tag=`).
  - `GET /api/products/{id}`: Detalhamento de produto por ID.
  - `DELETE /api/products/{id}`: Exclusão de produto.
- **Banco de Dados Utilizado:** MongoDB (Database `catalog_db`, Collection `products`).

#### 4.2. `order-service`
- **Responsabilidade:** Responsável pela criação, validação de regras de negócio, totalização e acompanhamento de status de pedidos.
- **Entidades Manipuladas:**
  - `Order`: Identificador (`id`), identificador do cliente (`customerId`), e-mail do cliente (`customerEmail`), status do pedido (`status`: *PENDING*, *CONFIRMED*, *CANCELLED*, *COMPLETED*), valor total (`totalAmount`), timestamps (`createdAt`, `updatedAt`).
  - `OrderItem`: Identificador (`id`), identificador do produto de referência (`productId`), nome do produto (`productName`), preço unitário capturado no momento da compra (`unitPrice`), quantidade comprada (`quantity`), subtotal (`subtotal`).
- **Endpoints:**
  - `POST /api/orders`: Criação de novo pedido (consulta síncrona ao catálogo via Feign com Circuit Breaker).
  - `GET /api/orders`: Listagem geral de pedidos ou filtrados por cliente (`?customerId=`).
  - `GET /api/orders/{id}`: Detalhes de um pedido específico com seus itens.
  - `PATCH /api/orders/{id}/status`: Transição de status do pedido.
- **Banco de Dados Utilizado:** PostgreSQL (Database `order_db`, Tabelas `orders` e `order_items`).

---

### 5. Isolamento de Bancos de Dados e Justificativa do Banco Não Relacional

#### 5.1. Database per Service
Cada microservice é proprietário exclusivo da sua base de dados (*Database per Service*):
- O `order-service` conecta-se exclusivamente ao PostgreSQL na base `order_db`.
- O `catalog-service` conecta-se exclusivamente ao MongoDB na base `catalog_db`.
- Não há compartilhamento de tabelas, coleções ou conexões cruzadas diretas entre os bancos. Qualquer necessidade de informação entre domínios é realizada estritamente via APIs/contratos HTTP.

#### 5.2. Justificativa Técnica do Uso de Banco Não Relacional (MongoDB)
Para o `catalog-service`, o uso do **MongoDB** foi selecionado com base nos seguintes pilares técnicos:

1. **Estrutura Semi-estruturada e Esquema Dinâmico (*Schema-less*):**
   No domínio de produtos de tecnologia, a diversidade de atributos entre categorias impossibilita a criação de um esquema relacional estático sem perdas. Em bancos relacionais, adotar o padrão EAV (*Entity-Attribute-Value*) para contornar isso gera degradação severa em operações de *JOIN* e indexação. O MongoDB armazena as especificações como documentos JSON/BSON nativos, permitindo que cada produto possua seu conjunto próprio de atributos sem exigir alterações de DDL (*ALTER TABLE*) a cada novo tipo de produto.

2. **Performance para Operações de Alta Leitura (*Read-Heavy*):**
   Em plataformas de comércio eletrônico, a proporção de leituras do catálogo em relação a compras é tipicamente superior a 100:1. O armazenamento em documento único do MongoDB permite recuperar a entidade completa do produto com todas as suas especificações e tags em uma única operação de I/O, dispensando múltiplos *JOINs* custosos.

3. **Consultas Flexíveis e Indexação de Arrays/Tags:**
   O MongoDB oferece suporte nativo a índices multikey em listas (ex.: `tags`) e campos aninhados (ex.: `specifications.gpu`), facilitando buscas multifacetadas essenciais para catálogos modernos.

---

### 6. Discovery Server (Netflix Eureka)

A arquitetura inclui um serviço dedicado de Service Discovery (`discovery-server`) utilizando **Spring Cloud Netflix Eureka Server**:
- **Porta:** `8761`
- **Dashboard Web:** Acessível em `http://localhost:8761`
- **Funcionamento:** Cada microservice (`order-service`, `catalog-service`, `api-gateway`) é configurado como cliente Eureka (`@EnableDiscoveryClient`). Na inicialização, registram seu nome lógico (`CATALOG-SERVICE`, `ORDER-SERVICE`, `API-GATEWAY`), endereço IP e porta.
- **Resolução Dinâmica:** O API Gateway e o Feign Client no `order-service` utilizam o prefixo `lb://<SERVICE-NAME>`, permitindo balanceamento de carga no lado do cliente e eliminação de endereços IP/portas estáticos (*hardcoded*).

---

### 7. API Gateway (Spring Cloud Gateway)

O `api-gateway` atua como o ponto único de entrada (*Single Entry Point*) para todas as aplicações clientes:
- **Porta:** `8080`
- **Tecnologia:** Spring Cloud Gateway (reativo, baseado em Project Reactor e Netty).
- **Roteamento Declarado:**
  - `http://localhost:8080/api/products/**` $\rightarrow$ `lb://catalog-service`
  - `http://localhost:8080/api/orders/**` $\rightarrow$ `lb://order-service`
- **Benefícios Arquiteturais:**
  - Desacoplamento entre clientes e portas internas dos microservices.
  - Habilidade de aplicar filtros globais, CORS, métricas e autenticação centralizada em etapas futuras.

---

### 8. Estratégia de Resiliência entre Microservices

#### 8.1. Ponto de Comunicação Inter-serviços
Ao criar um pedido (`POST /api/orders`), o `order-service` realiza uma chamada HTTP síncrona ao `catalog-service` utilizando **Spring Cloud OpenFeign** para consultar o preço vigente, nome do produto e disponibilidade.

#### 8.2. Riscos Identificados
1. O `catalog-service` pode estar temporariamente fora do ar por manutenção ou falha.
2. Latência de rede excessiva que retenha conexões abertas e esgote as threads do pool de execução do `order-service` (*Thread Starvation*).
3. Falhas em cascata que propaguem indisponibilidade por toda a plataforma.

#### 8.3. Mecanismos de Resiliência Aplicados (Resilience4j)
1. **Circuit Breaker:**
   - Implementado através da anotação `@CircuitBreaker(name = "catalogService", fallbackMethod = "fetchProductFallback")`.
   - Configurado com janela deslizante (*slidingWindowSize = 5*), limite de taxa de falhas de 50% (*failureRateThreshold = 50*) e tempo de espera no estado aberto de 10 segundos (*waitDurationInOpenState = 10000ms*).
   - Quando a taxa de falha é atingida, o circuito abre (*OPEN*), rejeitando novas chamadas imediatas e desviando a execução diretamente para o fallback.
2. **Fallback Gracioso (*Graceful Degradation*):**
   - O método `fetchProductFallback(String productId, Throwable throwable)` intercepta a exceção de indisponibilidade ou timeout.
   - Caso o item já tenha sido consultado anteriormente, recupera os dados de um cache resiliente em memória.
   - Caso seja um produto novo, gera um registro de produto de contingência com valores seguros e sinaliza no payload de resposta que o status de integração do catálogo operou em modo `DEGRADED (Circuit Breaker / Fallback)`.
3. **Retry com Backoff Exponencial:**
   - Anotação `@Retry(name = "catalogService")` configurada para até 3 tentativas com multiplicador de 2x, mitigando falhas transientes de rede.
4. **Timeouts:**
   - Limites de conexão e leitura de 3000ms configurados no cliente Feign para evitar bloqueio indefinido.

#### 8.4. Como Simular e Testar a Resiliência
1. Com todos os serviços rodando, execute `POST http://localhost:8080/api/orders` com um produto válido do catálogo $\rightarrow$ Resposta retorna `catalogServiceStatus: "HEALTHY (Online)"`.
2. Pare o container ou processo do `catalog-service`.
3. Execute novamente o `POST http://localhost:8080/api/orders` $\rightarrow$ O `order-service` não trava, não retorna HTTP 500, e sim o pedido gerado com aviso `catalogServiceStatus: "DEGRADED (Circuit Breaker / Fallback)"`.
4. Consulte as métricas do Circuit Breaker em `http://localhost:8081/actuator/circuitbreakers` para verificar o estado do circuito.

---

### 9. Instruções de Execução do Projeto

#### 9.1. Pré-requisitos
- **Java JDK 21**
- **Apache Maven 3.9+**
- **Docker e Docker Compose**

#### 9.2. Subindo os Bancos de Dados
No diretório raiz do projeto, execute:
```bash
docker compose up -d
```
Isso iniciará:
- MongoDB na porta `27017`
- PostgreSQL na porta `5432`

#### 9.3. Compilando e Empacotando o Projeto
```bash
mvn clean package -DskipTests
```

#### 9.4. Ordem de Inicialização dos Serviços
Abra terminais separados (ou execute via IDE) na seguinte sequência recomendada:

1. **Discovery Server (Porta 8761):**
   ```bash
   cd discovery-server
   mvn spring-boot:run
   ```
   *Aguarde ~10 segundos até o Eureka inicializar.*

2. **Catalog Service (Porta 8082):**
   ```bash
   cd catalog-service
   mvn spring-boot:run
   ```

3. **Order Service (Porta 8081):**
   ```bash
   cd order-service
   mvn spring-boot:run
   ```

4. **API Gateway (Porta 8080):**
   ```bash
   cd api-gateway
   mvn spring-boot:run
   ```

5. **Acesso aos Testes:**
   Utilize o arquivo `requests.http` incluído na raiz do projeto ou chamadas `curl` conforme documentado no `README.md`.
