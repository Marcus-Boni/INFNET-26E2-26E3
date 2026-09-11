# RELATÓRIO TÉCNICO E ACADÊMICO — TRABALHO PRÁTICO 2 (TP2)
## Arquitetura de Microsserviços com Docker, Docker Compose e Kubernetes

---

### Identificação do Trabalho

| Campo | Detalhes |
| :--- | :--- |
| **Instituição** | Instituto Infnet |
| **Curso** | Engenharia de Software |
| **Disciplina** | Engenharia Disciplinada |
| **Turma** | Segunda e Quarta |
| **Modalidade** | Individual |
| **Aluno** | Marcus Boni |
| **E-mail** | `marcus.boni@al.infnet.edu.br` |
| **Repositório GitHub** | [https://github.com/Marcus-Boni/INFNET-26E2-26E3](https://github.com/Marcus-Boni/INFNET-26E2-26E3) |
| **Projeto Base** | TechMarket (Evolução contínua do TP1) |

---

## Sumário das Etapas Desenvolvidas

1. [Etapa 1: Planejamento da Aplicação](#etapa-1-planejamento-da-aplicação)
2. [Etapa 2: Máquina Virtual × Container](#etapa-2-máquina-virtual--container)
3. [Etapa 3: Criando o Primeiro Microsserviço (`product-service`)](#etapa-3-criando-o-primeiro-microsserviço-product-service)
4. [Etapa 4: Criando o Segundo Microsserviço (`order-service`)](#etapa-4-criando-o-segundo-microsserviço-order-service)
5. [Etapa 5: Criando as Imagens Docker](#etapa-5-criando-as-imagens-docker)
6. [Etapa 6: Entendendo os Componentes Docker](#etapa-6-entendendo-os-componentes-docker)
7. [Etapa 7: Comunicação entre Microsserviços](#etapa-7-comunicação-entre-microsserviços)
8. [Etapa 8: Docker Network](#etapa-8-docker-network)
9. [Etapa 9: Docker Compose](#etapa-9-docker-compose)
10. [Etapa 10: Preparando a Migração para Kubernetes](#etapa-10-preparando-a-migração-para-kubernetes)
11. [Etapa 11: Primeiro Microsserviço no Kubernetes](#etapa-11-primeiro-microsserviço-no-kubernetes)
12. [Etapa 12: Migrando o Segundo Microsserviço](#etapa-12-migrando-o-segundo-microsserviço)
13. [Etapa 13: Descoberta de Serviço e Balanceamento de Carga](#etapa-13-descoberta-de-serviço-e-balanceamento-de-carga)
14. [Etapa 14: Avaliação e Reflexão sobre o Projeto](#etapa-14-avaliação-e-reflexão-sobre-o-projeto)

---

## Etapa 1: Planejamento da Aplicação

### 1.1. Nome do Projeto
**TechMarket — Plataforma Distribuída de E-commerce de Tecnologia**

### 1.2. Objetivo do Sistema
O objetivo do **TechMarket TP2** é construir uma plataforma de comércio eletrônico baseada em uma arquitetura moderna e progressiva de microsserviços. O sistema resolve a separação de responsabilidades entre o catálogo de equipamentos de tecnologia (hardware, computadores e periféricos) e a gestão transacional de pedidos de compra, implementando o ciclo de vida completo de engenharia de software: desde a concepção de APIs REST independentes até a containerização isolada com Docker, orquestração com Docker Compose e implantação resiliente em cluster Kubernetes com descoberta de serviços dinâmica (*Service Discovery*) e distribuição de carga (*Load Balancing*).

### 1.3. Nomes e Responsabilidades dos Dois Microsserviços

```mermaid
graph LR
    subgraph "TechMarket - Arquitetura de Microsserviços"
        ProductService["product-service<br/>(Porta 8081)"]
        OrderService["order-service<br/>(Porta 8082)"]
    end

    OrderService -->|Validação HTTP GET /products/{id}| ProductService
```

#### A) `product-service` (Microsserviço de Produtos)
- **Porta Padrão:** `8081` (mapeada no container e NodePort `30081` no Kubernetes).
- **Responsabilidade Principal:** Manutenção, persistência e exposição dos produtos do catálogo tecnológico. É responsável por cadastrar novos produtos (nome, descrição, preço unitário e estoque), fornecer a listagem completa dos itens cadastrados e responder detalhadamente a consultas por identificador único (`id`).
- **Endpoints Expostos:**
  - `GET /products`: Retorna a lista completa de produtos disponíveis.
  - `GET /products/{id}`: Retorna os detalhes de um produto específico (ou status 404 caso não exista).
  - `POST /products`: Realiza a validação e persistência de um novo produto no catálogo.
  - `GET /products/info`: Endpoint auxiliar de observabilidade que informa o status e a identificação do host/pod que processou a requisição.

#### B) `order-service` (Microsserviço de Pedidos)
- **Porta Padrão:** `8082` (mapeada no container e NodePort `30082` no Kubernetes).
- **Responsabilidade Principal:** Gerenciamento do ciclo de vida dos pedidos de compra dos clientes. É responsável por receber as intenções de compra, validar remotamente junto ao `product-service` se o produto solicitado é válido e existente, calcular o valor total (`preço unitário × quantidade`) e persistir o pedido com status confirmado.
- **Endpoints Expostos:**
  - `GET /orders`: Retorna a listagem de todos os pedidos realizados.
  - `GET /orders/{id}`: Retorna os dados completos de um pedido específico por ID.
  - `POST /orders`: Cria um novo pedido, acionando a validação síncrona com o `product-service`.
  - `GET /orders/info`: Endpoint de diagnóstico indicando o status e a URL do serviço de produtos configurada.

---

## Etapa 2: Máquina Virtual × Container

Imagine o cenário em que a aplicação TechMarket precisa ser disponibilizada para ser executada em outro computador ou servidor de homologação/produção.

### a) Como seria executar essa aplicação utilizando uma Máquina Virtual (VM)?
Para disponibilizar e executar a aplicação utilizando máquinas virtuais tradicionais, seria necessário criar ou exportar uma imagem de VM (ex.: formato OVA/VMDK gerenciado por um Hypervisor como VirtualBox, VMware ou KVM). Cada VM conteria um **Sistema Operacional convidado (*Guest OS*) completo** (kernel Linux, drivers virtuais, gerenciador de pacotes, serviços de sistema, systemd, etc.), além do JDK 21, runtime e a aplicação. Se os dois microsserviços fossem isolados em VMs distintas, teríamos a duplicação de dois sistemas operacionais completos rodando sobre a máquina hospedeira. A transferência exigiria arquivos de vários gigabytes e o boot demandaria dezenas de segundos a minutos para inicializar todo o SO convidado.

### b) Como seria executar utilizando Containers?
Utilizando containers Docker, a aplicação é empacotada em uma imagem OCI (*Open Container Initiative*) que contém estritamente o código compilado da aplicação (JAR), as dependências do runtime (JRE 21 Alpine) e as configurações de execução. Os containers **compartilham o mesmo kernel do sistema operacional hospedeiro**, sendo isolados logicamente em nível de processo através de recursos nativos do kernel Linux: **Namespaces** (isolamento de rede, processos PID, sistema de arquivos e montagens) e **cgroups** (limitação de uso de CPU e memória RAM). O artefato gerado possui menos de 100 MB compactados, pode ser baixado em segundos de um registry e inicializa em menos de 2 segundos.

### c) Qual solução tende a consumir menos recursos?
**Os containers consomem substancialmente menos recursos (ordens de grandeza inferiores).** Enquanto uma Máquina Virtual precisa alocar previamente gigabytes de memória RAM e núcleos de CPU fixos para sustentar o seu *Guest OS*, buffers de kernel e daemons de sistema, os containers não executam um kernel próprio. Um processo Java dentro de um container Docker consome apenas a memória alocada para a JVM e as bibliotecas mínimas do SO de suporte (como o Alpine Linux com musl libc), permitindo que um mesmo hardware execute dezenas ou centenas de containers simultâneos, enquanto suportaria apenas poucas máquinas virtuais equivalentes.

### d) Por que containers são interessantes para uma arquitetura de microsserviços?
1. **Densidade e Eficiência de Custo:** Microsserviços dividem o monolito em vários pequenos serviços autônomos. Em VMs, rodar 20 serviços significaria 20 SOs duplicados consumindo recursos ociosos. Containers permitem alta densidade com custo operacional reduzido.
2. **Tempo de Inicialização Ultrarrápido:** Containers sobem em segundos, viabilizando estratégias elásticas de autoscaling horizontal (HPA) e recuperação automática imediata em caso de falhas (*self-healing*).
3. **Paridade de Ambientes (*Immutable Infrastructure*):** A premissa de *"funciona na minha máquina, funciona em produção"* é garantida porque o container encapsula exatamente as mesmas dependências e configurações em qualquer nó de execução.
4. **Alinhamento com CI/CD e Orquestradores:** Imagens de container são a unidade padrão de entrega em pipelines de automação modernas e a unidade atômica fundamental de gerenciamento em orquestradores de larga escala como o Kubernetes.

### Tabela Comparativa

| Característica | Máquina Virtual (VM) | Container (Docker) |
| :--- | :--- | :--- |
| **Sistema Operacional** | Possui SO Convidado (*Guest OS*) completo e independente para cada VM, com seu próprio kernel. | Compartilha o kernel do SO Hospedeiro (*Host OS*); contém apenas binários e bibliotecas de usuário (*User space*). |
| **Consumo de Recursos** | Alto. Overhead de gigabytes de RAM e disco alocados estaticamente para cada SO convidado. | Muito baixo. Overhead insignificante (~poucos MBs além do processo da aplicação); compartilha recursos dinamicamente via *cgroups*. |
| **Inicialização** | Lenta (de 30 segundos a vários minutos para carregar kernel, daemons e serviços do SO convidado). | Quase instantânea (de milissegundos a poucos segundos, dependendo apenas do tempo de inicialização da JVM). |
| **Isolamento** | Forte e em nível de hardware via instruções de CPU e Hypervisor (Tipo 1 ou 2). | Lógico e em nível de processo no kernel do SO via *Linux Namespaces* e *cgroups*. |
| **Tamanho da Imagem** | Muito grande (de 2 GB a dezenas de GBs por imagem OVA/VMDK). | Leve e enxuta (a imagem do projeto TechMarket possui apenas ~95 MB compactados). |
| **Portabilidade** | Dependente do formato do hypervisor e configurações pesadas de rede virtual. | Padronizada pela especificação aberta OCI; roda identicamente em Linux, Windows, macOS e nuvem. |

---

## Etapa 3: Criando o Primeiro Microsserviço (`product-service`)

O microsserviço `product-service` foi desenvolvido utilizando **Java 21** e **Spring Boot 3.3.5**. Ele foi projetado para operar com alta performance e sem dependências pesadas de bancos de dados externos, utilizando um repositório thread-safe em memória (`ConcurrentHashMap`).

### 3.1. Estrutura de Código
- **`Product.java`:** Modelo de domínio contendo `id`, `name`, `description`, `price` e `stockQuantity`.
- **`ProductRequest.java` e `ProductResponse.java`:** DTOs com validações `jakarta.validation` (`@NotBlank`, `@DecimalMin`, `@Min`) e inclusão do metadado de rastreabilidade `servedBy` (identificador do host/pod).
- **`InMemoryProductRepository.java`:** Repositório que garante thread-safety e controle sequencial de chaves atômicas via `AtomicLong`.
- **`ProductController.java`:** Controlador REST expondo os endpoints na rota base `/products`.

### 3.2. Endpoints Implementados
- `GET /products`: Retorna a lista de produtos.
- `GET /products/{id}`: Retorna um produto por identificador ou `404 Not Found`.
- `POST /products`: Cadastra novo produto retornando `201 Created`.
- `GET /products/info`: Diagnóstico da réplica.

### 3.3. Carga Inicial de Dados (Seed)
Na inicialização da aplicação, o bean `CommandLineRunner` insere automaticamente produtos representativos no catálogo, incluindo explicitamente o produto com **ID 10** (*Monitor Gamer UltraWide 34 LG Curved*), atendendo pontualmente ao exemplo do enunciado.

---

## Etapa 4: Criando o Segundo Microsserviço (`order-service`)

O microsserviço `order-service` foi implementado como uma aplicação Spring Boot independente, responsável pelo gerenciamento de pedidos de compra.

### 4.1. Estrutura de Código
- **`Order.java`:** Modelo de domínio contendo `id`, `customerName`, `productId`, `productName`, `quantity`, `unitPrice`, `totalPrice`, `status`, `createdAt` e `validatedByPod`.
- **`OrderStatus.java`:** Enum contendo os estados do pedido (`PENDING`, `CONFIRMED`, `CANCELLED`).
- **`OrderRequest.java` e `OrderResponse.java`:** Contratos de entrada e saída.
- **`InMemoryOrderRepository.java`:** Repositório em memória thread-safe.
- **`OrderController.java`:** Controlador REST na rota `/orders`.

### 4.2. Independência dos Serviços
O serviço possui seu próprio ciclo de vida, build Maven isolado, dependências próprias e porta dedicada (`8082`), podendo ser executado, testado e compilado de forma totalmente autônoma em relação ao `product-service`.

---

## Etapa 5: Criando as Imagens Docker

Foram criados dois arquivos `Dockerfile` otimizados utilizando o padrão **Multi-Stage Build**, que separa o ambiente de compilação pesado (Maven + JDK) da imagem final de produção (JRE mínima baseada em Alpine Linux).

### 5.1. Dockerfiles Implementados

#### `product-service/Dockerfile`:
```dockerfile
# Estágio 1: Compilação e empacotamento da aplicação
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Estágio 2: Imagem enxuta de runtime baseada em Alpine Linux
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENV SERVER_PORT=8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### `order-service/Dockerfile`:
```dockerfile
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8082
ENV SERVER_PORT=8082
ENV PRODUCT_SERVICE_URL=http://product-service:8081/products
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 5.2. Comandos de Construção das Imagens
```bash
# Construção da imagem de produtos
docker build -t product-image:latest -f product-service/Dockerfile product-service

# Construção da imagem de pedidos
docker build -t order-image:latest -f order-service/Dockerfile order-service
```

As imagens geradas (`product-image` e `order-image`) possuem apenas **95.7 MB** de transferência e executam diretamente sobre o runtime Java 21 LTS.

---

## Etapa 6: Entendendo os Componentes Docker

Abaixo são detalhados os componentes fundamentais da tecnologia Docker com exemplos práticos extraídos deste projeto:

1. **Dockerfile:** É um arquivo de receita de texto declarativo contendo o conjunto sequencial de instruções que o Docker executa para montar uma imagem. No projeto, os arquivos `product-service/Dockerfile` e `order-service/Dockerfile` definem desde a imagem-base (`eclipse-temurin:21-jre-alpine`), cópia dos arquivos JAR até o comando de inicialização `ENTRYPOINT ["java", "-jar", "app.jar"]`.
2. **Image (Imagem):** É um template somente-leitura imutável composto por camadas empilhadas (*read-only layers*). Representa a fotografia congelada da aplicação e de todo o seu ambiente de execução. No projeto, as imagens geradas foram `product-image:latest` e `order-image:latest`.
3. **Container:** É uma instância ativa e executável de uma imagem Docker. Ao rodar uma imagem, o Docker cria uma camada de leitura/escrita (*writable container layer*) no topo das camadas imutáveis da imagem e isola o processo usando Namespaces e cgroups. No projeto, os containers `product-service` e `order-service` são as instâncias em execução.
4. **Docker Engine:** É o software cliente-servidor instalado no host responsável por construir, executar e gerenciar os objetos Docker. É composto pelo daemon `dockerd`, pela interface de controle `containerd`, pelo executor OCI de baixo nível `runc` e pela CLI `docker`.
5. **Port Mapping (Mapeamento de Portas):** É o mecanismo de redirecionamento de tráfego de rede (NAT via iptables/nftables) que conecta uma porta da máquina hospedeira (*Host Port*) a uma porta interna do container (*Container Port*). Por exemplo, `-p 8081:8081` instrui o Docker Engine a encaminhar qualquer requisição que chegue na porta 8081 do computador físico para a porta 8081 interna do container `product-service`.
6. **Network (Rede Docker):** É o subsistema que provê conectividade e isolamento de comunicação entre containers. A rede padrão criada no projeto foi a rede bridge customizada `techmarket-net`, que habilita o serviço de DNS interno integrado do Docker Engine.

### Qual é a diferença entre uma imagem e um container?

A diferença pode ser compreendida através de analogias de software e engenharia:
- **Analogia da Programação Orientada a Objetos:** A **Imagem** é a **Classe** (definição imutável do modelo, atributos e comportamentos), enquanto o **Container** é o **Objeto instanciado em memória** (que possui estado dinâmico, ciclo de vida e consome CPU/RAM).
- **Analogia da Construção Civil:** A **Imagem** é a **Planta arquitetônica** no papel; o **Container** é a **Casa construída e habitada**.
- **Definição Técnica:** A imagem é um artefato estático, distribuível, composto por camadas de sistema de arquivos somente leitura com um hash criptográfico SHA-256. O container é o processo do sistema operacional em execução isolada no kernel com uma camada efêmera de escrita (*read-write layer*) e interfaces de rede ativas. É possível criar dezenas de containers simultâneos a partir de uma única e idêntica imagem Docker.

---

## Etapa 7: Comunicação entre Microsserviços

O `order-service` integra-se com o `product-service` através do cliente HTTP moderno `ProductClient`, implementado com o `RestClient` nativo do Spring Boot 3.

### 7.1. Fluxo de Validação
Ao receber uma requisição de criação de pedido (`POST /orders`):
1. O `order-service` extrai o `productId` solicitado no corpo da requisição.
2. Dispara uma requisição remota:
   $$\text{GET } \$\{PRODUCT\_SERVICE\_URL\}/\{productId\}$$
3. Se o produto existir (status 200), obtém o nome e preço oficial unitário e calcula o total:
   $$\text{totalPrice} = \text{unitPrice} \times \text{quantity}$$
4. Se o produto não existir (status 404 retornado pelo catálogo), o `order-service` aborta a criação e retorna imediatamente `HTTP 400 Bad Request` com a mensagem:
   `"Não foi possível criar o pedido: Produto com ID 'X' não existe no product-service."`

### 7.2. Por que NÃO utilizar `localhost` entre containers?
Em sistemas operacionais e arquitetura TCP/IP, cada container Docker possui o seu próprio **Network Namespace** isolado. Isso significa que a interface de rede local e o endereço IP de loopback `127.0.0.1` (`localhost`) de um container pertencem **exclusivamente a ele mesmo**. Se o container do `order-service` disparasse uma requisição para `http://localhost:8081`, o kernel tentaria localizar a porta 8081 dentro do próprio container `order-service`, resultando em erro imediato de conexão recusada (*Connection Refused*).

### 7.3. Como os containers se encontram? (Docker Embedded DNS)
Ao conectarmos containers em uma rede customizada do Docker (como a `techmarket-net`), o Docker Engine ativa um servidor **DNS Interno Embutido** (*Embedded DNS Server*) que escuta no IP virtual `127.0.0.11`. Quando o `order-service` envia uma requisição para `http://product-service:8081`, o resolver do sistema operacional consulta esse DNS interno, que traduz o nome do container (`product-service`) para o endereço IP virtual alocado a ele na rede bridge.

---

## Etapa 8: Docker Network

Para permitir que os containers se comuniquem utilizando seus nomes lógicos, foi criada uma rede bridge dedicada:

### 8.1. Comando Utilizado para Criar a Rede
```bash
docker network create techmarket-net
```

### 8.2. Comandos para Iniciar os Containers Conectados
```bash
# Iniciar product-service na rede
docker run -d \
  --name product-service \
  --network techmarket-net \
  -p 8081:8081 \
  product-image:latest

# Iniciar order-service na mesma rede, apontando para o hostname do product-service
docker run -d \
  --name order-service \
  --network techmarket-net \
  -p 8082:8082 \
  -e PRODUCT_SERVICE_URL=http://product-service:8081/products \
  order-image:latest
```

### 8.3. Topologia da Rede
```
+-------------------------------------------------------------+
|                Docker Network: techmarket-net               |
|                                                             |
|   +--------------------+               +----------------+   |
|   |  product-service   | <------------ | order-service  |   |
|   |  (IP: 172.18.0.2)  |  HTTP /products (IP: 172.18.0.3)|   |
|   |  Porta: 8081       |  via DNS      | Porta: 8082    |   |
|   +--------------------+               +----------------+   |
+-------------+-----------------------------------+-----------+
              |                                   |
              | Mapeamento -p                     | Mapeamento -p
              v                                   v
    Host: localhost:8081                Host: localhost:8082
```

### 8.4. Evidência Real de Execução e Comunicação
Execução da requisição de compra no `order-service` com validação no produto 10:
```json
// POST http://localhost:8082/orders
// Request Body: { "customerName": "Marcus Boni", "productId": "10", "quantity": 2 }
// Resposta HTTP 201 Created:
{
  "id": "1",
  "customerName": "Marcus Boni",
  "productId": "10",
  "productName": "Monitor Gamer UltraWide 34 LG Curved",
  "quantity": 2,
  "unitPrice": 2899.00,
  "totalPrice": 5798.00,
  "status": "CONFIRMED",
  "createdAt": "2026-09-10T21:49:24.780",
  "validatedByPod": "6d4315061855",
  "handledByInstance": "274a24dca294"
}
```
*O campo `validatedByPod` confirma que a instância `6d4315061855` (container `product-service`) foi consultada com sucesso pelo container `274a24dca294` (`order-service`).*

---

## Etapa 9: Docker Compose

Para automatizar a criação manual de redes e containers, foi concebido o arquivo declarativo `docker-compose.yml`.

### 9.1. Arquivo `docker-compose.yml`
```yaml
version: '3.8'

services:
  product-service:
    build:
      context: ./product-service
      dockerfile: Dockerfile
    image: product-image:latest
    container_name: product-service
    ports:
      - "8081:8081"
    environment:
      - SERVER_PORT=8081
    networks:
      - techmarket-net
    restart: unless-stopped

  order-service:
    build:
      context: ./order-service
      dockerfile: Dockerfile
    image: order-image:latest
    container_name: order-service
    ports:
      - "8082:8082"
    environment:
      - SERVER_PORT=8082
      - PRODUCT_SERVICE_URL=http://product-service:8081/products
    depends_on:
      - product-service
    networks:
      - techmarket-net
    restart: unless-stopped

networks:
  techmarket-net:
    name: techmarket-net
    driver: bridge
```

### 9.2. Execução do Desafio
Os containers criados manualmente nas etapas anteriores foram completamente removidos e a aplicação inteira foi inicializada declarativamente com o Docker Compose:
```bash
# Limpeza de containers manuais
docker rm -f product-service order-service

# Inicialização limpa de toda a infraestrutura
docker compose up -d
```

---

## Etapa 10: Preparando a Migração para Kubernetes

O Kubernetes introduz conceitos de orquestração distribuída em clusters com alta disponibilidade, autoscaling e recuperação automática de falhas.

### 10.1. Tabela de Equivalência Docker × Kubernetes

| Docker | Kubernetes | Descrição e Papel no Kubernetes |
| :--- | :--- | :--- |
| **Container** | **Pod** (especificação de container) | A menor unidade de execução gerenciável no K8s. Um Pod encapsula um ou mais containers que compartilham armazenamento e rede (IP único). |
| **Network (Rede)** | **CNI / Pod Network / ClusterIP** | Implementado pelo plugin de rede (CNI). Cada Pod recebe um IP roteável dentro de uma rede plana de cluster, permitindo comunicação Pod-to-Pod sem NAT. |
| **Port Mapping (`-p`)** | **Service (`NodePort` / `ClusterIP` / `targetPort`)** | O Service desacopla o tráfego externo/interno dos IPs efêmeros dos Pods, roteando a porta pública (`nodePort` ou `port`) para a `targetPort` do Pod. |
| **Serviço da aplicação** | **Deployment + ReplicaSet** | Abstração que gerencia o ciclo de vida da aplicação declarativamente, garantindo estratégias de atualização sem downtime (*RollingUpdate*) e autorrecuperação. |
| **Múltiplos containers** | **Pod com Sidecars** ou **Múltiplos Deployments** | Vários containers dentro do mesmo Pod trabalham em conjunto (padrão Sidecar/InitContainer); múltiplos microsserviços utilizam Deployments distintos interligados por Services. |

### 10.2. Por que uma aplicação que funciona com Docker não precisa ser completamente reescrita para funcionar no Kubernetes?

Uma aplicação containerizada com Docker não necessita de nenhuma alteração em seu código-fonte para rodar no Kubernetes devido a três pilares da computação em nuvem moderna:

1. **Padronização OCI (*Open Container Initiative*):** As imagens geradas pelo Docker seguem a especificação OCI de imagem e runtime. O Kubernetes utiliza interfaces padronizadas como o **CRI (*Container Runtime Interface*)** (através do `containerd` ou `CRI-O`), sendo perfeitamente capaz de puxar e executar as mesmas imagens `product-image` e `order-image` sem nenhuma modificação.
2. **Desacoplamento entre Aplicação e Orquestrador:** Uma boa aplicação de microsserviço segue as diretrizes do **Twelve-Factor App**. Ela não conhece nem deve conhecer a infraestrutura subjacente: ela simplesmente escuta em uma porta TCP e obtém suas configurações através de **Variáveis de Ambiente** (`SERVER_PORT`, `PRODUCT_SERVICE_URL`). Seja no Docker CLI, Docker Compose ou em um manifesto de Pod do Kubernetes, a injeção dessas variáveis de ambiente é idêntica.
3. **Comunicação por Protocolos Universais e DNS:** A comunicação entre os microsserviços utiliza chamadas padrão HTTP/REST sobre TCP/IP e resolução de nomes por DNS. O Kubernetes provê internamente o **CoreDNS**, permitindo que o `order-service` resolva o endereço `http://product-service:8081` de forma transparente, exatamente da mesma forma como fazia na rede bridge do Docker.

---

## Etapa 11: Primeiro Microsserviço no Kubernetes (`product-service`)

Foi realizada a migração do `product-service` para o Kubernetes criando os manifestos `Deployment` e `Service`.

### 11.1. Arquitetura no Kubernetes
```
Kubernetes Cluster
│
└── Deployment: product-service
    │
    └── ReplicaSet
        │
        └── Pod: product-service-xxxxx
            │
            └── Container: product-image:latest (Porta 8081)
```

### 11.2. Manifestos Implementados

#### `k8s/product-service-deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: product-service
  labels:
    app: product-service
    tier: backend
spec:
  replicas: 1 # Escalado para 3 na Etapa 13
  selector:
    matchLabels:
      app: product-service
  template:
    metadata:
      labels:
        app: product-service
        tier: backend
    spec:
      containers:
        - name: product-service
          image: product-image:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8081
              name: http
          env:
            - name: SERVER_PORT
              value: "8081"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 15
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8081
            initialDelaySeconds: 10
            periodSeconds: 5
```

#### `k8s/product-service-service.yaml`:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: product-service
  labels:
    app: product-service
spec:
  type: NodePort
  selector:
    app: product-service
  ports:
    - name: http
      port: 8081
      targetPort: 8081
      nodePort: 30081
```

---

## Etapa 12: Migrando o Segundo Microsserviço (`order-service`)

Na Etapa 12, o `order-service` é implantado no cluster Kubernetes e passa a consultar o `product-service` utilizando exclusivamente o **nome do Service** (`product-service`) via CoreDNS, sem utilizar IP fixo.

### 12.1. Manifestos Implementados

#### `k8s/order-service-deployment.yaml`:
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: order-service
  labels:
    app: order-service
    tier: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: order-service
  template:
    metadata:
      labels:
        app: order-service
        tier: backend
    spec:
      containers:
        - name: order-service
          image: order-image:latest
          imagePullPolicy: IfNotPresent
          ports:
            - containerPort: 8082
              name: http
          env:
            - name: SERVER_PORT
              value: "8082"
            # Configuração de Service Discovery via CoreDNS do Kubernetes sem IP fixo
            - name: PRODUCT_SERVICE_URL
              value: "http://product-service:8081/products"
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8082
            initialDelaySeconds: 15
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8082
            initialDelaySeconds: 10
            periodSeconds: 5
```

#### `k8s/order-service-service.yaml`:
```yaml
apiVersion: v1
kind: Service
metadata:
  name: order-service
  labels:
    app: order-service
spec:
  type: NodePort
  selector:
    app: order-service
  ports:
    - name: http
      port: 8082
      targetPort: 8082
      nodePort: 30082
```

### 12.2. Resolução de Nomes no Kubernetes (CoreDNS)
Quando o `order-service` dispara uma requisição para:
```
http://product-service:8081/products/10
```
O serviço de DNS do Kubernetes (**CoreDNS**) intercepta a consulta e traduz o nome `product-service` para o **ClusterIP** virtual estável do Service. O kube-proxy e o subsistema de rede encaminham os pacotes TCP diretamente para os Pods saudáveis associados aos seletores `app: product-service`.

---

## Etapa 13: Descoberta de Serviço e Balanceamento de Carga

Na Etapa 13, o número de réplicas do `product-service` é ampliado para 3 (`replicas: 3`), demonstrando a escalabilidade horizontal e o balanceamento de carga automático promovido pelo Service.

### 13.1. Topologia de Balanceamento
```
                              Requisições HTTP
                        (NodePort 30081 ou CoreDNS)
                                     │
                                     ▼
                    +─────────────────────────────────+
                    |    Kubernetes Service           |
                    |    nome: product-service:8081   |
                    |    (ClusterIP Virtual Estável)  |
                    +────────────────+────────────────+
                                     │
             ┌───────────────────────┼───────────────────────┐
             │ (Round-Robin)         │ (Round-Robin)         │ (Round-Robin)
             ▼                       ▼                       ▼
   +───────────────────+   +───────────────────+   +───────────────────+
   |   Pod 1           |   |   Pod 2           |   |   Pod 3           |
   | product-service-1 |   | product-service-2 |   | product-service-3 |
   | IP: 10.244.0.15   |   | IP: 10.244.0.16   |   | IP: 10.244.0.17   |
   +───────────────────+   +───────────────────+   +───────────────────+
```

### 13.2. Perguntas Teóricas da Etapa 13

#### a) Qual é a função do Service?
A função central do **Service** no Kubernetes é atuar como uma **abstração de ponto de entrada único, estável e imutável** para um conjunto dinâmico de Pods que executam a mesma aplicação. Como os Pods são recursos efêmeros que podem ser destruídos, recriados ou migrados de nó a qualquer momento (recebendo um novo endereço IP a cada reinicialização), o Service provê um nome DNS persistente e um endereço IP virtual fixo (*ClusterIP*), mantendo atualizada a lista de endpoints saudáveis através de seletores de labels (`app: product-service`).

#### b) Por que podemos ter vários Pods do mesmo microsserviço?
Podemos ter vários Pods por dois motivos primordiais de engenharia de confiabilidade:
1. **Escalabilidade Horizontal (*Scale-Out*):** Dividir a carga de requisições concorrentes entre múltiplas instâncias da JVM, permitindo que a aplicação processe um volume muito maior de requisições por segundo sem sobrecarregar uma única CPU ou esgotar a heap de memória.
2. **Alta Disponibilidade e Tolerância a Falhas (*High Availability*):** Eliminação de pontos únicos de falha (*Single Point of Failure - SPOF*). Se um nó físico ou um Pod sofrer degradação ou falha inesperada, as outras réplicas continuam operando e atendendo aos usuários sem interrupção de serviço.

#### c) O que acontece se um dos Pods parar de funcionar?
Se um dos Pods travar, falhar nos probes de saúde (*Liveness / Readiness Probe*) ou for subitamente encerrado:
1. O controlador de **Readiness Probe** do Kubernetes detecta a falha e remove imediatamente o Pod do objeto **Endpoints** do Service. Com isso, o Service **cessa instantaneamente o envio de novas requisições para aquele Pod defeituoso**, evitando erros 500 para os clientes.
2. Simultaneamente, o controlador do **Deployment / ReplicaSet** detecta que o número de réplicas ativas ficou abaixo do estado desejado (`desejado = 3, atual = 2`) e aciona automaticamente o scheduler para provisionar um **novo Pod idêntico em fração de segundos** (*self-healing*).

#### d) Como o Service ajuda na distribuição das requisições?
O Service atua como um **balanceador de carga interno L4 (camada de transporte TCP)**. Ele rastreia continuamente todos os Pods saudáveis marcados com as labels correspondentes. Através do **kube-proxy** (utilizando regras de *iptables* com módulo probabilístico de escolha ou tabelas de hash no *IPVS*), cada nova conexão TCP direcionada ao IP/porta do Service é automaticamente roteada em modo round-robin/aleatório balanceado para um dos Pods disponíveis, assegurando a equalização do consumo de recursos entre todas as réplicas.

---

## Etapa 14: Avaliação e Reflexão sobre o Projeto

### a) Qual foi a parte mais fácil e qual foi a parte mais difícil do trabalho? Explique.
- **Parte Mais Fácil:** A implementação dos controladores REST e entidades com Spring Boot 3 e Java 21, devido à maturidade do framework Spring, à facilidade de injeção de dependências e ao uso de repositórios thread-safe em memória que agilizam o feedback imediato de desenvolvimento.
- **Parte Mais Difícil:** A depuração e sincronização da comunicação entre containers com resolução de nomes por DNS e o tratamento estrito de contratos HTTP (como tratar corretamente respostas 404 sem disparar exceções não tratadas de desserialização no cliente REST). A transição mental do paradigma estático de endereços IP para a descoberta dinâmica baseada em labels e Services no Kubernetes também exige compreensão sólida de conceitos de redes de computadores.

### b) Qual foi o principal aprendizado que você teve sobre Docker, containers e Kubernetes?
O principal aprendizado foi compreender na prática o conceito de **Infraestrutura Imutável e Desacoplamento de Runtime**. Ver uma aplicação ser construída localmente, depois encapsulada em uma imagem OCI enxuta que consome recursos mínimos do kernel e, finalmente, ser orquestrada pelo Kubernetes com três réplicas recebendo tráfego balanceado via DNS dinâmico — sem alterar uma única linha de código Java — consolida o valor dos padrões de microsserviços modernos, do Twelve-Factor App e da orquestração em nuvem.

### c) Autoavaliação e Nota de Desempenho
- **Nota Proposta:** **10 / 10**
- **Justificativa:** Todas as 14 etapas propostas no enunciado foram cumpridas integralmente e com rigor técnico exemplar:
  1. Planejamento conceitual completo baseado no tema da Loja Virtual TechMarket;
  2. Tabela comparativa e embasamento teórico detalhado entre VMs e Containers;
  3. Desenvolvimento de microsserviços em Spring Boot 3 + Java 21 com endpoints exigidos;
  4. Dockerfiles multi-stage otimizados para imagens compactas (<100 MB);
  5. Comunicação remota entre containers via rede bridge customizada sem uso de `localhost`;
  6. Docker Compose funcional com desafio de subida unificada cumprido;
  7. Manifestos declarativos completos para Kubernetes (Deployments, Services e NodePorts);
  8. Configuração de escalabilidade horizontal com 3 réplicas e comprovação de balanceamento de carga;
  9. Documentação rica com diagramas, tabelas e suíte de testes `requests.http`.
