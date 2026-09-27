# Nexus Store — TP4: Arquitetura Orientada a Eventos com RabbitMQ

Bem-vindo ao repositório do **TP4** do projeto Nexus Store. Este projeto evolui a arquitetura de microsserviços distribuída do TP3 para uma **Arquitetura Orientada a Eventos (Event-Driven Architecture - EDA)** de alta performance, utilizando o **RabbitMQ** como message broker e as abstrações do **Spring Boot / Spring AMQP**.

---

## 🎯 Objetivos e Competências Desenvolvidas

1. **Avaliação de Arquitetura Orientada a Eventos**: Análise técnica aprofundada dos prós, contras e cenários ideais para adoção de EDA.
2. **Desenvolvimento de Padrões de Mensagens**: Implementação de *Publish-Subscribe (Pub/Sub)*, *Event-Carried State Transfer (ECST)*, *Saga Choreography*, *Dead Letter Queue (DLQ)* e *Idempotent Consumers*.
3. **Implementação com RabbitMQ**: Modelagem de Topic Exchanges (`nexus.order.exchange`, `nexus.shipping.exchange`), Dead Letter Exchange (`nexus.dlx.exchange`), Filas duráveis e amarração com routing keys flexíveis.
4. **Simplificação com Spring Boot**: Uso de `@EnableRabbit`, `@RabbitListener`, `RabbitTemplate`, `Jackson2JsonMessageConverter` e gestão de retry com backoff exponencial.
5. **Refatoração do Sistema**: Eliminação do acoplamento síncrono HTTP/OpenFeign para mutações de pedidos e logística, garantindo resposta em milissegundos e tolerância total a falhas temporárias.
6. **Código Fonte e Monitoramento**: Código desacoplado e novo painel interativo no Frontend com monitoramento ao vivo de eventos e simulador de DLQ.
7. **Documentação Arquitetural Completa**: Documento detalhado [`ARCHITECTURE.md`](./ARCHITECTURE.md) com diagramas de sequência, topologia e fluxos de eventos.

---

## 📁 Estrutura do Repositório

```text
TP4/
├── docker-compose.yml              # Orquestração completa (RabbitMQ, Backend, Shipping, Frontend)
├── ARCHITECTURE.md                 # Documentação técnica e arquitetural detalhada
├── README.md                       # Guia de início rápido e execução
│
├── backend/                        # Backend Principal / Catálogo e Pedidos (Porta 8080)
│   ├── src/main/java/com/infnet/tp4/
│   │   ├── event/                  # Contratos de eventos de domínio (OrderCreated, etc.)
│   │   ├── infrastructure/
│   │   │   ├── config/RabbitMqConfig.java   # Topologia AMQP e MessageConverter
│   │   │   └── rabbitmq/           # Produtores e Consumidores (@RabbitListener)
│   │   └── ...
│   └── pom.xml
│
├── shipping-service/               # Microsserviço de Logística e Frete (Porta 8082)
│   ├── src/main/java/com/infnet/shipping/
│   │   ├── event/                  # Contratos de eventos recebidos e emitidos
│   │   ├── infrastructure/
│   │   │   ├── config/RabbitMqConfig.java   # Topologia AMQP
│   │   │   └── rabbitmq/           # Consumidor de pedidos e publicador de envios
│   │   └── ...
│   └── pom.xml
│
└── frontend/                       # Frontend SPA React + Vite (Porta 5173)
    ├── src/
    │   ├── components/events/      # Nova aba: Monitor de Eventos e Simulador DLQ
    │   └── ...
    └── package.json
```

---

## 🚀 Como Executar o Projeto

### Opção 1: Via Docker Compose (Recomendada)
Para subir todos os componentes (RabbitMQ com painel Web, Backend, Shipping Service e Frontend):

```bash
docker compose up --build
```

Acessos:
- **Frontend SPA**: [http://localhost:5173](http://localhost:5173)
- **Backend API**: [http://localhost:8080/api/products](http://localhost:8080/api/products)
- **Logística API**: [http://localhost:8082/api/v1/shipping/shipments](http://localhost:8082/api/v1/shipping/shipments)
- **Painel RabbitMQ Management**: [http://localhost:15672](http://localhost:15672) (login: `guest` / senha: `guest`)

---

### Opção 2: Execução Local Passo a Passo

#### 1. Iniciar o RabbitMQ via Docker
```bash
docker run -d --name nexus-rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

#### 2. Iniciar o Microsserviço de Logística (`shipping-service`)
```bash
cd shipping-service
mvn spring-boot:run
```
*(Inicia na porta 8082)*

#### 3. Iniciar o Backend Principal (`backend`)
```bash
cd backend
mvn spring-boot:run
```
*(Inicia na porta 8080)*

#### 4. Iniciar o Frontend React
```bash
cd frontend
npm install
npm run dev
```
*(Inicia na porta 5173)*

---

## 🧪 Executando os Testes Automatizados

O projeto possui cobertura total de testes unitários e de integração, utilizando **JUnit 5**, **Mockito**, **AssertJ** e mocks do Spring AMQP:

```bash
# Testes do Shipping Service (12 testes)
cd shipping-service
mvn test

# Testes do Backend (20 testes)
cd backend
mvn test
```

Ambos os projetos compilam e executam com **100% de sucesso (`BUILD SUCCESS`)** sem dependência obrigatória do broker RabbitMQ externo para os testes automatizados.

---

## 🌟 Funcionalidades em Destaque no TP4

- **Aba "Eventos (RabbitMQ)" no Frontend**: Visualização em tempo real de mensagens trafegando entre os microsserviços com visualizador de payload JSON formatado.
- **Laboratório de Resiliência (DLQ)**: Botão no frontend para simular envio de *poison pills* e visualizar o descarte seguro e isolamento na Dead Letter Queue.
- **Consistência Eventual Visual**: Indicação visual de processamento logístico em andamento e correlação ID em cada pedido.
- **Documentação Arquitetural Completa**: Consulte [`ARCHITECTURE.md`](./ARCHITECTURE.md) para todos os detalhes teóricos, padrões e diagramas de sequência.
