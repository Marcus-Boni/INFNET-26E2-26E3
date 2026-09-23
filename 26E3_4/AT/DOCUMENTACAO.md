# Documentação Técnica e Arquitetural — Assessment Freela Marketplace

**Disciplina:** Domain-Driven Design (DDD) e Arquitetura de Softwares Escaláveis com Java [26E3_4]  
**Aluno:** Marcus Boni  
**Instituição:** Instituto Infnet  

---

## Sumário Executivo

Este documento apresenta a especificação técnica formal e os detalhes de implementação da evolução da plataforma **Freela Marketplace** para uma **Arquitetura Orientada a Eventos (EDA - Event-Driven Architecture)** de alta resiliência e nível de produção, utilizando **Apache Kafka** como espinha dorsal de mensageria assíncrona, **Spring Boot 4**, **Spring Cloud 2025** e **PostgreSQL**.

A solução elimina qualquer acoplamento síncrono HTTP entre o microsserviço de contratos e os serviços auxiliares, garantindo:
1. Publicação transacional através do padrão **Transactional Outbox**, evitando o problema de *Dual Write*;
2. Processamento concorrente com **preservação estrita da ordem dos eventos (FIFO)** por contrato via particionamento determinístico por `contratoId`;
3. **Idempotência rigorosa** no consumo de mensagens, impedindo duplicação de dados, notificações repetidas e, criticamente, duplo incremento na reputação dos freelancers;
4. Resiliência a falhas com retentativas automáticas e encaminhamento para **Dead Letter Topic (DLT)**;
5. **Observabilidade e rastreabilidade distribuída de ponta a ponta**, com **Zipkin Tracing**, **Correlation ID** propagado em headers HTTP/Kafka e **logs centralizados estruturados com MDC** integrados ao **Grafana Loki**.

---

## 1. Comunicação Baseada em Eventos

### 1.1. Motivação e Desacoplamento Temporal
Em arquiteturas orientadas a microsserviços, chamadas HTTP síncronas diretas entre serviços de negócio para notificação, cálculo de reputação e auditoria introduzem sérios problemas de:
- **Acoplamento Temporal:** A disponibilidade do `contrato-service` fica subordinada à disponibilidade simultânea de todos os serviços consumidores ($D_{total} = D_1 \times D_2 \times \dots \times D_n$).
- **Latência Acumulada:** O tempo de resposta para o cliente final inclui a soma dos tempos de resposta de todas as chamadas encadeadas.
- **Vulnerabilidade a Falhas em Cascata:** A lentidão ou instabilidade em um serviço auxiliar pode provocar esgotamento de threads (*thread pool exhaustion*) no serviço de contratos.

Com a introdução do Apache Kafka, o `contrato-service` apenas publica os fatos consumados de seu domínio em um tópico durável. Os serviços de notificação, reputação e auditoria reagem aos eventos no seu próprio ritmo, com tolerância total a indisponibilidades transitórias dos consumidores.

### 1.2. Estrutura Canônica das Mensagens
Todas as mensagens trafegadas no Kafka possuem a seguinte estrutura de envelope e dados:
- **`eventId`** (`UUID`): Identificador universal único do evento, gerado no momento da ocorrência. Atua como **chave de idempotência** para os consumidores.
- **`eventType`** (`String`): Nome canônico do evento de domínio (`ContratoCriado`, `EntregaRegistrada`, `ContratoConcluido`, `ContratoCancelado`).
- **`contratoId`** (`UUID`): Identificador do contrato relacionado (Aggregate Root). Atua como chave de particionamento (`key`).
- **`occurredAt`** (`Instant`): Timestamp ISO-8601 exato em que o evento ocorreu no domínio.
- **`correlationId`** (`String`): Identificador de correlação distribuída para rastreamento de ponta a ponta.
- **Dados de Domínio Específicos:** `clienteId`, `freelancerId`, `titulo`, `valor`, etc.

---

## 2. Integração dos Serviços

### 2.1. `notificacao-service`
- **Responsabilidade:** Receber eventos de ciclo de vida dos contratos e registrar as notificações correspondentes no banco `notificacao_db`.
- **Eventos Consumidos:**
  - `ContratoCriado`: Gera notificação para o Freelancer ("Novo contrato recebido") e para o Cliente ("Contrato criado com sucesso").
  - `EntregaRegistrada`: Gera notificação para o Cliente ("Entrega submetida pelo freelancer").
  - `ContratoConcluido`: Gera notificação para o Freelancer ("Contrato concluído e pagamento liberado").
  - `ContratoCancelado`: Notifica ambas as partes.
- **Logs de Processamento:** Cada evento processado registra explicitamente:
  - Evento recebido (`eventType`);
  - Contrato associado (`contratoId`);
  - Destinatário da notificação (`destinatarioId`);
  - Resultado da operação (`resultado=SUCESSO` ou `IGNORADO_DUPLICADO`).

### 2.2. `reputacao-service`
- **Responsabilidade:** Reagir exclusivamente a eventos que impactem o histórico ou pontuação do freelancer.
- **Eventos Consumidos:** `ContratoConcluido`. Demais eventos (`ContratoCriado`, `EntregaRegistrada`) são descartados sem alteração de estado.
- **Regra de Negócio:** Incrementa a quantidade de contratos concluídos (`contratosConcluidos++`) e soma o valor do contrato ao faturamento total acumulado (`valorTotal = valorTotal + valor`).
- **Persistência:** Armazena os dados na tabela `reputacoes` do banco `reputacao_db`.

### 2.3. `auditoria-service`
- **Responsabilidade:** Registrar de forma imutável todos os eventos emitidos no ecossistema da plataforma no banco `auditoria_db`.
- **Eventos Consumidos:** Todos os eventos do tópico `contratos.v1.events`.
- **Dados Registrados:**
  - `eventId`: UUID único do evento;
  - `aggregateId`: ID do contrato;
  - `eventType`: Tipo do evento;
  - `correlationId`: Identificador da requisição externa;
  - `payload`: Conteúdo JSON integral da mensagem para reconstrução de estado e auditoria forense;
  - `occurredAt`: Data/hora em que o evento ocorreu no serviço de origem;
  - `recebidoEm`: Data/hora em que a mensagem foi recebida e gravada pelo serviço de auditoria.

---

## 3. Especificação Formal das Mensagens

### 3.1. Matriz de Eventos e Serviços

| Evento | Tópico Kafka | Produtor | Consumidores | Chave da Mensagem |
|---|---|---|---|---|
| **`ContratoCriado`** | `contratos.v1.events` | `contrato-service` | `notificacao-service`, `auditoria-service` | `contratoId` |
| **`EntregaRegistrada`** | `contratos.v1.events` | `contrato-service` | `notificacao-service`, `auditoria-service` | `contratoId` |
| **`ContratoConcluido`** | `contratos.v1.events` | `contrato-service` | `notificacao-service`, `reputacao-service`, `auditoria-service` | `contratoId` |
| **`ContratoCancelado`** | `contratos.v1.events` | `contrato-service` | `notificacao-service`, `auditoria-service` | `contratoId` |

### 3.2. Contratos e Schemas JSON

#### A. ContratoCriado
- **Campos Obrigatórios:** `eventId`, `eventType`, `occurredAt`, `contratoId`, `clienteId`, `freelancerId`, `titulo`, `valor`, `correlationId`.
- **Exemplo de Mensagem:**
```json
{
  "eventId": "a5df9aae-dbdd-4b94-b0a7-e0663c1bbe1f",
  "eventType": "ContratoCriado",
  "contratoId": "8f2d3297-3a51-4bca-949c-d51f103f38bf",
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Construção de API de pagamentos",
  "valor": 3500.00,
  "correlationId": "corr-teste-001",
  "occurredAt": "2026-09-22T19:00:00Z"
}
```

#### B. EntregaRegistrada
- **Campos Obrigatórios:** `eventId`, `eventType`, `occurredAt`, `contratoId`, `clienteId`, `freelancerId`, `titulo`, `correlationId`.
- **Exemplo de Mensagem:**
```json
{
  "eventId": "53dbf882-6eed-4d0d-a8e4-efc8ab0d6ec8",
  "eventType": "EntregaRegistrada",
  "contratoId": "8f2d3297-3a51-4bca-949c-d51f103f38bf",
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Construção de API de pagamentos",
  "correlationId": "corr-teste-001",
  "occurredAt": "2026-09-22T19:15:00Z"
}
```

#### C. ContratoConcluido
- **Campos Obrigatórios:** `eventId`, `eventType`, `occurredAt`, `contratoId`, `clienteId`, `freelancerId`, `titulo`, `valor`, `correlationId`.
- **Exemplo de Mensagem:**
```json
{
  "eventId": "409bc751-2a92-4de0-b4d3-72f80fe353d9",
  "eventType": "ContratoConcluido",
  "contratoId": "8f2d3297-3a51-4bca-949c-d51f103f38bf",
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Construção de API de pagamentos",
  "valor": 3500.00,
  "correlationId": "corr-teste-001",
  "occurredAt": "2026-09-22T19:30:00Z"
}
```

---

## 4. Processamento Concorrente e Ordenação (FIFO por Contrato)

### 4.1. O Desafio da Concorrência vs. Ordem
Em um marketplace, milhares de contratos podem ser criados e finalizados simultaneamente. No entanto, para um mesmo contrato específico, a sequência temporal de eventos de negócio é mandatória:
$$\text{ContratoCriado} \longrightarrow \text{EntregaRegistrada} \longrightarrow \text{ContratoConcluido}$$
Se `ContratoConcluido` fosse processado antes de `EntregaRegistrada`, o sistema entraria em estado inconsistente.

### 4.2. Estratégia de Particionamento e Consumo
1. **Chave de Partição Determinística:** Todas as mensagens publicadas para um contrato utilizam o `contratoId` (UUID string) como chave do Kafka (`ProducerRecord(topic, contratoId, payload)`).
2. O Apache Kafka aplica o algoritmo de hashing Murmur2 sobre a chave para determinar a partição:
   $$\text{Partição} = \text{hash}(\text{contratoId}) \pmod N$$
   Portanto, **todos os eventos de um mesmo contrato caem invariavelmente na mesma partição**.
3. **Garantia de Ordem no Kafka:** O Kafka assegura ordem FIFO estrita dentro de uma partição individual.
4. **Concorrência Horizontal:** O tópico possui 3 partições e cada consumidor configura `concurrency = 3`. Três threads consumidoras distintas processam as partições 0, 1 e 2 em paralelo.
   - Contratos com IDs diferentes são distribuídos entre as partições e processados concorrentemente.
   - Eventos do mesmo contrato permanecem na mesma partição e são processados pela mesma thread, sequencialmente.

---

## 5. Tratamento de Mensagens Duplicadas (Idempotência)

### 5.1. Semântica de Entrega
O Apache Kafka opera com a garantia de entrega **At-Least-Once** (pelo menos uma vez). Cenários de rebalanceamento de consumidores, timeouts de rede ou reenvio podem fazer com que um consumidor receba uma mensagem mais de uma vez.

### 5.2. Mecanismo de Deduplicação Implementado
Para impedir inconsistências, foi adotado o padrão **Idempotent Consumer** baseado em repositório de mensagens processadas:
1. **Identificador Global Único:** Cada mensagem publicada possui um `eventId` único imutável.
2. **Tabela de Idempotência:** Os serviços consumidores possuem a entidade `MensagemProcessada` mapeada para a tabela `mensagens_processadas`:
   - `eventId` (`UUID`, Primary Key);
   - `eventType` (`String`);
   - `consumerName` (`String`);
   - `processadoEm` (`Instant`).
3. **Execução Atômica:**
   ```java
   if (mensagemProcessadaRepository.existsById(eventId)) {
       log.warn("consumo.duplicada.ignorada eventId={} contratoId={}", eventId, contratoId);
       return; // Descarta sem efeitos colaterais
   }
   // Executa a operação de negócio
   // Persiste MensagemProcessada no mesmo commit transacional
   ```
4. **Proteções Comprovadas:**
   - No `notificacao-service`: Não duplica linhas na tabela `notificacoes`.
   - No `reputacao-service`: **Evita duplo incremento**. Caso uma mensagem de conclusão seja reprocessada, o saldo de contratos concluídos e o valor financeiro do freelancer permanecem inalterados.
   - No `auditoria-service`: A constraint única e a checagem prévia impedem registros redundantes na auditoria.

---

## 6. Publicação Transacional (Transactional Outbox Pattern)

### 6.1. O Problema do Dual Write
No `contrato-service`, salvar o estado do contrato no banco relacional PostgreSQL e publicar o evento no Apache Kafka são duas operações distribuídas distintas. Sem publicação transacional:
- Se o banco salvar e o broker Kafka falhar, o evento é perdido para sempre (inconsistência eventual quebrada).
- Se o broker publicar e a transação do banco sofrer rollback, os serviços consumidores processarão um contrato que formalmente nunca existiu.

### 6.2. Solução Implementada
A arquitetura implementa o **Transactional Outbox Pattern**:
1. Tabela `outbox_events` criada no banco `contrato_db`:
   - `id` (UUID);
   - `aggregateType` ("Contrato");
   - `aggregateId` (UUID do contrato);
   - `eventType` (String);
   - `payload` (JSON);
   - `status` (`PENDING`, `PUBLISHED`, `FAILED`);
   - `createdAt`, `publishedAt`, `attempts`, `correlationId`.
2. Quando o `ContratoApplicationService` executa um comando de negócio (`criar`, `registrarEntrega`, `concluir`, `cancelar`), os eventos retirados do Aggregate são convertidos em registros `OutboxEventEntity` e gravados na tabela `outbox_events` **dentro da mesma transação `@Transactional` da entidade `Contrato`**.
3. O componente `OutboxPublisher` executa a cada 500ms via `@Scheduled`, lê os registros com status `PENDING` ordenados por `createdAt ASC`, despacha-os para o Kafka com `acks=all`, e atualiza o status para `PUBLISHED` e `publishedAt = Instant.now()`.
4. Isso garante a semântica **At-Least-Once Transactional Publishing** sem necessidade de protocolo 2PC (Two-Phase Commit).

---

## 7. Observabilidade, Tracing e Centralização de Logs

### 7.1. Rastreamento Distribuído com Zipkin
- Implementado via **Micrometer Tracing** com motor **Brave** (`micrometer-tracing-bridge-brave`) e repórter do **Zipkin** (`zipkin-reporter-brave`).
- Taxa de amostragem configurada para 100% (`sampling.probability: 1.0`).
- O contexto de trace (`traceId`, `spanId`) é propagado:
  - Pelo API Gateway nas chamadas HTTP (`traceparent` / headers B3);
  - Pelo `contrato-service` nos headers dos registros Kafka;
  - Pelos consumidores Kafka, que herdam o contexto e registram spans filhos associados ao mesmo `traceId` raiz.
- O Zipkin UI em `http://localhost:9411` permite visualizar a árvore hierárquica completa da transação distribuída.

### 7.2. Correlação de Operações (`X-Correlation-Id`)
- O `api-gateway` possui o `RequestLoggingFilter`, que verifica a existência do header `X-Correlation-Id`. Se ausente, gera um UUID v4.
- O `correlationId` é propagado em todas as requisições downstream e retornado no cabeçalho da resposta HTTP.
- O `contrato-service` injeta o `correlationId` no payload do evento e nos headers do Kafka.
- Todos os consumidores mantêm o `correlationId` no MDC durante o processamento.

### 7.3. Centralização de Logs com MDC, Loki e Grafana
- O padrão de formatação dos logs em todos os microsserviços inclui as variáveis contextuais:
  ```text
  %d{yyyy-MM-dd HH:mm:ss.SSS} %-5level service=${spring.application.name} traceId=%X{traceId:-} spanId=%X{spanId:-} correlationId=%X{correlationId:-} contratoId=%X{contratoId:-} eventId=%X{eventId:-} thread=%thread logger=%logger{36} - %msg%n
  ```
- A infraestrutura local provê o **Grafana Loki** na porta `3100` e o **Grafana** na porta `3000` com provisionamento automático de datasources.
- É possível realizar consultas unificadas em toda a frota através do Grafana Explore com expressões como:
  ```logql
  {service=~".+"} |= "corr-teste-001"
  {service=~".+"} |= "contratoId=8f2d3297-3a51-4bca-949c-d51f103f38bf"
  ```

---

## 8. Tratamento de Falhas e Dead Letter Topic (DLT)

### 8.1. Política de Retentativas
Cada consumidor Kafka utiliza o `ConcurrentKafkaListenerContainerFactory` com um `DefaultErrorHandler` personalizado:
- **Tentativas:** 3 tentativas de entrega;
- **Backoff:** Intervalo fixo de 1000 milissegundos (`FixedBackOff(1000L, 3L)`).

### 8.2. Encaminhamento para Dead Letter Topic
- Se uma mensagem persistir com erro após esgotadas as 3 tentativas, o `DeadLetterPublishingRecoverer` desvia o registro para o tópico `contratos.v1.events.DLT`.
- A mensagem de erro, stack trace e partição original são gravados nos headers da mensagem descartada.
- Isso impede o travamento da partição (*poison pill problem*) e viabiliza diagnóstico e reprocessamento posterior sem impacto sobre as mensagens saudáveis subsequentes.

---

## 9. Suíte de Testes Automatizados

A suíte de testes cobre integralmente os requisitos arquiteturais e pode ser executada com `mvn clean test`:

1. **`ContratoDomainTest`:**
   - Criação e validação do Aggregate `Contrato`;
   - Transição ordenada de estados: `ATIVO` $\rightarrow$ `ENTREGA_REGISTRADA` $\rightarrow$ `CONCLUIDO`;
   - Geração e esvaziamento de eventos de domínio via `pullDomainEvents()`.
2. **`ContratoOutboxIntegrationTest`:**
   - Validação da gravação atômica de contratos e eventos `PENDING` no Outbox;
   - Publicação assíncrona pelo `OutboxPublisher` com chave `contratoId`;
   - Transição de status para `PUBLISHED` e preenchimento de `publishedAt`.
3. **`NotificacaoConsumerIntegrationTest`:**
   - Consumo de `ContratoCriado`, `EntregaRegistrada` e `ContratoConcluido`;
   - **Teste de Idempotência:** Verificação de que o reprocessamento da mesma mensagem com idêntico `eventId` não duplica registros na tabela `notificacoes`.
4. **`ReputacaoConsumerIntegrationTest`:**
   - Consumo de `ContratoConcluido` com atualização de contratos e valor faturado;
   - **Teste Crítico de Idempotência:** Comprovação de que reentregas do mesmo evento **não incrementam** a contagem de contratos concluídos nem somam o valor financeiro novamente.
5. **`AuditoriaConsumerIntegrationTest`:**
   - Auditoria de todos os eventos da plataforma com payload JSON integral e `correlationId`;
   - Validação de unicidade e ordenação cronológica por `contratoId`.

---

## 10. Conclusão

A arquitetura implementada no projeto **Freela Marketplace** atende plenamente aos 13 requisitos do Assessment, estruturando uma plataforma distribuída orientada a eventos que combina os mais altos padrões de:
- **Desacoplamento e Escalabilidade** (Apache Kafka em KRaft, tópicos particionados com concorrência);
- **Consistência e Confiabilidade** (Transactional Outbox e Idempotent Consumers);
- **Governança e Rastreabilidade** (Zipkin Tracing, correlationId em cascata, MDC e Grafana Loki);
- **Resiliência e Tolerância a Falhas** (Dead Letter Topic e retentativas não-bloqueantes).
