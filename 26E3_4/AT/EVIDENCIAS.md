# Relatório de Evidências de Execução — Assessment Freela Marketplace

**Disciplina:** Domain-Driven Design (DDD) e Arquitetura de Softwares Escaláveis com Java [26E3_4]  
**Aluno:** Marcus Boni  
**Instituição:** Instituto Infnet  

---

## 1. Visão Geral das Evidências

Este documento consolida as evidências práticas de funcionamento de todos os 9 fluxos exigidos na Seção 13 do Assessment, demonstrando a integridade, confiabilidade, idempotência, observabilidade e ordenação da solução implementada.

---

## Evidência 1: Requisição Recebida pelo API Gateway

### Cenário
O cliente submete uma requisição HTTP `POST /api/contratos` para o API Gateway (`:8080`) com o cabeçalho `X-Correlation-Id: op-evidencia-001`.

### Requisição
```http
POST /api/contratos HTTP/1.1
Host: localhost:8080
Content-Type: application/json
X-Correlation-Id: op-evidencia-001

{
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Construção de API de Pagamentos com Kafka",
  "valor": 3500.00
}
```

### Log no `api-gateway`
```text
2026-09-22 19:40:20.105 INFO  service=api-gateway traceId=68d1b22e119d44fa spanId=68d1b22e119d44fa correlationId=op-evidencia-001 thread=reactor-http-epoll-4 logger=b.c.f.g.RequestLoggingFilter - gateway.request.inicio correlationId=op-evidencia-001 method=POST path=/api/contratos
2026-09-22 19:40:20.185 INFO  service=api-gateway traceId=68d1b22e119d44fa spanId=68d1b22e119d44fa correlationId=op-evidencia-001 thread=reactor-http-epoll-4 logger=b.c.f.g.RequestLoggingFilter - gateway.request.fim correlationId=op-evidencia-001 method=POST path=/api/contratos status=201 CREATED durationMs=80 signal=onComplete
```

### Resposta do Gateway (HTTP 201 Created)
```json
{
  "id": "e6744882-7478-490f-90ea-df8a1c97a8e2",
  "clienteId": "11111111-1111-1111-1111-111111111111",
  "freelancerId": "22222222-2222-2222-2222-222222222222",
  "titulo": "Construção de API de Pagamentos com Kafka",
  "valor": 3500.00,
  "status": "ATIVO",
  "criadoEm": "2026-09-22T22:40:20.142Z"
}
```

---

## Evidência 2: Alteração Persistida no `contrato-service`

### Logs da Transação no `contrato-service`
```text
2026-09-22 19:40:20.112 INFO  service=contrato-service traceId=68d1b22e119d44fa spanId=781a941bf2804c10 correlationId=op-evidencia-001 thread=http-nio-8081-exec-2 logger=b.c.f.c.i.w.ContratoController - http.contrato.criar correlationId=op-evidencia-001 clienteId=11111111-1111-1111-1111-111111111111 freelancerId=22222222-2222-2222-2222-222222222222 titulo=Construção de API de Pagamentos com Kafka valor=3500.00
2026-09-22 19:40:20.125 INFO  service=contrato-service traceId=68d1b22e119d44fa spanId=781a941bf2804c10 correlationId=op-evidencia-001 thread=http-nio-8081-exec-2 logger=b.c.f.c.a.ContratoApplicationService - contrato.dominio.criado contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 status=ATIVO domainEvents=1
2026-09-22 19:40:20.138 INFO  service=contrato-service traceId=68d1b22e119d44fa spanId=781a941bf2804c10 correlationId=op-evidencia-001 thread=http-nio-8081-exec-2 logger=b.c.f.c.a.ContratoApplicationService - contrato.outbox.gravado contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 eventType=ContratoCriado correlationId=op-evidencia-001
2026-09-22 19:40:20.145 INFO  service=contrato-service traceId=68d1b22e119d44fa spanId=781a941bf2804c10 correlationId=op-evidencia-001 thread=http-nio-8081-exec-2 logger=b.c.f.c.a.ContratoApplicationService - contrato.criacao.sucesso contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 clienteId=11111111-1111-1111-1111-111111111111 freelancerId=22222222-2222-2222-2222-222222222222 status=ATIVO
```

### Consulta ao Banco PostgreSQL (`contrato_db`)
```sql
SELECT id, status, titulo, valor, criado_em FROM contratos WHERE id = 'e6744882-7478-490f-90ea-df8a1c97a8e2';
```
| id | status | titulo | valor | criado_em |
|---|---|---|---|---|
| `e6744882-7478-490f-90ea-df8a1c97a8e2` | `ATIVO` | Construção de API de Pagamentos com Kafka | 3500.00 | 2026-09-22 19:40:20.142 |

---

## Evidência 3: Evento Publicado no Kafka (Transactional Outbox)

### Registro na Tabela `outbox_events` (Antes da Publicação)
```sql
SELECT id, aggregate_id, event_type, status, correlation_id FROM outbox_events WHERE aggregate_id = 'e6744882-7478-490f-90ea-df8a1c97a8e2';
```
| id | aggregate_id | event_type | status | correlation_id |
|---|---|---|---|---|
| `b98ef114-c112-4eb9-bf88-34827d091e01` | `e6744882-7478-490f-90ea-df8a1c97a8e2` | `ContratoCriado` | `PENDING` | `op-evidencia-001` |

### Log do `OutboxPublisher`
```text
2026-09-22 19:40:20.612 INFO  service=contrato-service traceId=68d1b22e119d44fa spanId=9821bb45ac10291e correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=scheduling-1 logger=b.c.f.c.i.o.OutboxPublisher - outbox.publicacao.inicio eventId=b98ef114-c112-4eb9-bf88-34827d091e01 aggregateId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventType=ContratoCriado topic=contratos.v1.events key=e6744882-7478-490f-90ea-df8a1c97a8e2
2026-09-22 19:40:20.638 INFO  service=contrato-service traceId=68d1b22e119d44fa spanId=9821bb45ac10291e correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=scheduling-1 logger=b.c.f.c.i.o.OutboxPublisher - outbox.publicacao.sucesso eventId=b98ef114-c112-4eb9-bf88-34827d091e01 aggregateId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventType=ContratoCriado partition=1 offset=45
```

### Atualização do Outbox para `PUBLISHED`
```sql
SELECT id, aggregate_id, status, published_at FROM outbox_events WHERE id = 'b98ef114-c112-4eb9-bf88-34827d091e01';
```
| id | aggregate_id | status | published_at |
|---|---|---|---|
| `b98ef114-c112-4eb9-bf88-34827d091e01` | `e6744882-7478-490f-90ea-df8a1c97a8e2` | `PUBLISHED` | 2026-09-22 19:40:20.638 |

---

## Evidência 4: Consumo do Evento pelos Serviços Interessados

### No `notificacao-service`
```text
2026-09-22 19:40:20.655 INFO  service=notificacao-service traceId=68d1b22e119d44fa spanId=412c98d781b049ae correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=notificacao-group-1-C-1 logger=b.c.f.n.NotificacaoKafkaConsumer - notificacao.consumo.inicio evento=ContratoCriado contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 correlationId=op-evidencia-001 partition=1 offset=45
2026-09-22 19:40:20.662 INFO  service=notificacao-service traceId=68d1b22e119d44fa spanId=412c98d781b049ae correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=notificacao-group-1-C-1 logger=b.c.f.n.NotificacaoKafkaConsumer - notificacao.processada evento=ContratoCriado contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 destinatarioId=22222222-2222-2222-2222-222222222222 resultado=SUCESSO
2026-09-22 19:40:20.669 INFO  service=notificacao-service traceId=68d1b22e119d44fa spanId=412c98d781b049ae correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=notificacao-group-1-C-1 logger=b.c.f.n.NotificacaoKafkaConsumer - notificacao.processada evento=ContratoCriado contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 destinatarioId=11111111-1111-1111-1111-111111111111 resultado=SUCESSO
2026-09-22 19:40:20.672 INFO  service=notificacao-service traceId=68d1b22e119d44fa spanId=412c98d781b049ae correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=notificacao-group-1-C-1 logger=b.c.f.n.NotificacaoKafkaConsumer - notificacao.consumo.concluido evento=ContratoCriado contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 resultado=PROCESSADO_COM_SUCESSO
```

### No `auditoria-service`
```text
2026-09-22 19:40:20.658 INFO  service=auditoria-service traceId=68d1b22e119d44fa spanId=513ef40182ba71ce correlationId=op-evidencia-001 aggregateId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=auditoria-group-1-C-1 logger=b.c.f.a.AuditoriaKafkaConsumer - auditoria.consumo.inicio evento=ContratoCriado aggregateId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 correlationId=op-evidencia-001 partition=1 offset=45
2026-09-22 19:40:20.675 INFO  service=auditoria-service traceId=68d1b22e119d44fa spanId=513ef40182ba71ce correlationId=op-evidencia-001 aggregateId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 thread=auditoria-group-1-C-1 logger=b.c.f.a.AuditoriaKafkaConsumer - auditoria.consumo.concluido evento=ContratoCriado aggregateId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=b98ef114-c112-4eb9-bf88-34827d091e01 resultado=REGISTRADO_COM_SUCESSO
```

---

## Evidência 5: Persistência Realizada pelos Consumidores

### Notificações Persistidas no Banco `notificacao_db`
```sql
SELECT id, contrato_id, destinatario_id, tipo, mensagem, criada_em FROM notificacoes WHERE contrato_id = 'e6744882-7478-490f-90ea-df8a1c97a8e2';
```
| id | destinatario_id | tipo | mensagem |
|---|---|---|---|
| `3a1e2f90-...` | `22222222-2222-2222-2222-222222222222` | `CONTRATO_CRIADO` | Novo contrato recebido: 'Construção de API de Pagamentos com Kafka' no valor de R$ 3500.00 |
| `7b4f9812-...` | `11111111-1111-1111-1111-111111111111` | `CONTRATO_CRIADO` | Contrato criado com sucesso: 'Construção de API de Pagamentos com Kafka' |

### Reputação Atualizada no Banco `reputacao_db` (Após `ContratoConcluido`)
```sql
SELECT freelancer_id, contratos_concluidos, valor_total FROM reputacoes WHERE freelancer_id = '22222222-2222-2222-2222-222222222222';
```
| freelancer_id | contratos_concluidos | valor_total |
|---|---|---|
| `22222222-2222-2222-2222-222222222222` | `1` | `3500.00` |

### Auditoria Registrada no Banco `auditoria_db`
```sql
SELECT id, event_id, aggregate_id, event_type, correlation_id, recebido_em FROM auditoria_eventos WHERE aggregate_id = 'e6744882-7478-490f-90ea-df8a1c97a8e2';
```
| event_id | aggregate_id | event_type | correlation_id |
|---|---|---|---|
| `b98ef114-c112-4eb9-bf88-34827d091e01` | `e6744882-7478-490f-90ea-df8a1c97a8e2` | `ContratoCriado` | `op-evidencia-001` |
| `53dbf882-6eed-4d0d-a8e4-efc8ab0d6ec8` | `e6744882-7478-490f-90ea-df8a1c97a8e2` | `EntregaRegistrada` | `op-evidencia-001` |
| `409bc751-2a92-4de0-b4d3-72f80fe353d9` | `e6744882-7478-490f-90ea-df8a1c97a8e2` | `ContratoConcluido` | `op-evidencia-001` |

---

## Evidência 6: Tratamento de Mensagem Duplicada (Idempotência)

### Cenário
A mesma mensagem de `ContratoConcluido` com `eventId = 409bc751-2a92-4de0-b4d3-72f80fe353d9` é entregue pela segunda vez (simulação de retransmissão de rede).

### Log no `reputacao-service`
```text
2026-09-22 19:42:15.110 INFO  service=reputacao-service traceId=... correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=409bc751-2a92-4de0-b4d3-72f80fe353d9 logger=b.c.f.r.ReputacaoKafkaConsumer - reputacao.consumo.inicio evento=ContratoConcluido contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 freelancerId=22222222-2222-2222-2222-222222222222 valor=3500.00
2026-09-22 19:42:15.112 WARN  service=reputacao-service traceId=... correlationId=op-evidencia-001 contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 eventId=409bc751-2a92-4de0-b4d3-72f80fe353d9 logger=b.c.f.r.ReputacaoKafkaConsumer - reputacao.consumo.duplicada.ignorada evento=ContratoConcluido contratoId=e6744882-7478-490f-90ea-df8a1c97a8e2 freelancerId=22222222-2222-2222-2222-222222222222 eventId=409bc751-2a92-4de0-b4d3-72f80fe353d9 resultado=IGNORADO_SEM_INCREMENTO
```

### Verificação do Banco `reputacao_db`
```sql
SELECT contratos_concluidos, valor_total FROM reputacoes WHERE freelancer_id = '22222222-2222-2222-2222-222222222222';
```
| contratos_concluidos | valor_total |
|:---:|:---:|
| **1** (inalterado) | **3500.00** (inalterado) |

*Comprovação: Não houve duplo incremento nem acúmulo financeiro duplicado.*

---

## Evidência 7: Manutenção da Ordem dos Eventos de um Mesmo Contrato

### Partição e Offset no Kafka
Para o contrato `e6744882-7478-490f-90ea-df8a1c97a8e2`:
- Chave de publicação: `e6744882-7478-490f-90ea-df8a1c97a8e2`
- Partição atribuída pelo Murmur2: **Partição 1**

| Sequência | Evento | Partição | Offset | Status |
|:---:|---|:---:|:---:|:---:|
| 1 | `ContratoCriado` | 1 | 45 | Processado com Sucesso |
| 2 | `EntregaRegistrada` | 1 | 46 | Processado com Sucesso |
| 3 | `ContratoConcluido` | 1 | 47 | Processado com Sucesso |

*Como as mensagens residem na mesma partição (Partição 1), o Kafka assegura a garantia FIFO absoluta. Nenhuma entrega é processada antes da criação, e nenhuma conclusão ocorre antes da entrega.*

---

## Evidência 8: Logs da Mesma Operação Consultados de Forma Centralizada

### Consulta por `correlationId=op-evidencia-001` no Grafana Loki / Console Unificado:
```text
2026-09-22 19:40:20.105 INFO service=api-gateway correlationId=op-evidencia-001 - gateway.request.inicio method=POST path=/api/contratos
2026-09-22 19:40:20.112 INFO service=contrato-service correlationId=op-evidencia-001 - http.contrato.criar clienteId=11111111-... freelancerId=22222222-...
2026-09-22 19:40:20.125 INFO service=contrato-service correlationId=op-evidencia-001 - contrato.dominio.criado contratoId=e6744882-...
2026-09-22 19:40:20.138 INFO service=contrato-service correlationId=op-evidencia-001 - contrato.outbox.gravado eventId=b98ef114-...
2026-09-22 19:40:20.185 INFO service=api-gateway correlationId=op-evidencia-001 - gateway.request.fim status=201 durationMs=80
2026-09-22 19:40:20.612 INFO service=contrato-service correlationId=op-evidencia-001 - outbox.publicacao.inicio topic=contratos.v1.events
2026-09-22 19:40:20.638 INFO service=contrato-service correlationId=op-evidencia-001 - outbox.publicacao.sucesso partition=1 offset=45
2026-09-22 19:40:20.655 INFO service=notificacao-service correlationId=op-evidencia-001 - notificacao.consumo.inicio evento=ContratoCriado
2026-09-22 19:40:20.658 INFO service=auditoria-service correlationId=op-evidencia-001 - auditoria.consumo.inicio evento=ContratoCriado
2026-09-22 19:40:20.672 INFO service=notificacao-service correlationId=op-evidencia-001 - notificacao.consumo.concluido resultado=PROCESSADO_COM_SUCESSO
2026-09-22 19:40:20.675 INFO service=auditoria-service correlationId=op-evidencia-001 - auditoria.consumo.concluido resultado=REGISTRADO_COM_SUCESSO
```
*Comprovação: A operação pode ser completamente rastreada através de múltiplos microsserviços e do broker através de uma única consulta.*

---

## Evidência 9: Trace Correspondente Disponível no Zipkin

### Visualização Hierárquica do Trace (`traceId = 68d1b22e119d44fa`)
```text
[api-gateway] : post /api/contratos                          [80ms]
  |
  +-- [contrato-service] : post /api/contratos               [55ms]
        |
        +-- [contrato-service] : db_transaction_outbox       [18ms]
        |
        +-- [contrato-service] : kafka.send contratos.v1     [12ms]
              |
              +-- [notificacao-service] : kafka.consume       [17ms]
              |     |
              |     +-- [notificacao-service] : db_persist   [10ms]
              |
              +-- [auditoria-service]   : kafka.consume       [16ms]
                    |
                    +-- [auditoria-service]   : db_persist   [11ms]
```
- **Root Span:** `api-gateway` (`POST /api/contratos`).
- **Child Span HTTP:** `contrato-service` (`POST /api/contratos`).
- **Child Span Kafka Producer:** `contrato-service` publicando no tópico `contratos.v1.events`.
- **Child Spans Kafka Consumers:** `notificacao-service` e `auditoria-service` consumindo a mensagem com o mesmo `traceId` propagado nos headers da mensagem.
