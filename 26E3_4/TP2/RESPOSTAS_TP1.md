# Trabalho Prático 1 (TP1) - Arquitetura de Software e DDD

---

### Questão 1
**Enunciado:** Explique de forma sucinta qual a razão de criar Agregados.

**Resposta:**  
No Domain-Driven Design (DDD), a principal razão para criar **Agregados** (*Aggregates*) é estabelecer **fronteiras claras de consistência transacional** e encapsular as **invariantes de negócio**. 

Em vez de tratar o modelo de domínio como uma teia conectada e desordenada de entidades em memória (o que geraria concorrência excessiva, locks no banco de dados e violação de regras), o Agregado agrupa entidades e objetos de valor relacionados em uma unidade coesa. O acesso externo ocorre exclusivamente através de uma entidade principal, a **Raiz do Agregado** (*Aggregate Root*). Com isso, garante-se que nenhuma modificação interna ocorra sem que as regras do domínio sejam integralmente validadas dentro do escopo de uma única transação atômica.

---

### Questão 2
**Enunciado:** O que significa “consistência transacional”?

**Resposta:**  
**Consistência transacional** é a garantia de que uma transação (conjunto de operações de leitura e escrita) levará os dados do sistema de um **estado válido inicial para outro estado igualmente válido**, respeitando de forma imediata e estrita todas as regras de integridade e invariantes de negócio. 

Se qualquer operação dentro do bloco transacional violar uma regra ou falhar por erro de sistema, toda a transação sofre reversão (*rollback*), impedindo que modificações parciais, incoerentes ou inválidas persistam no banco de dados. No contexto de DDD, o limite de um Agregado define exatamente o limite da consistência transacional: cada transação deve modificar apenas uma instância de Agregado.

---

### Questão 3
**Enunciado:** Cite as 4 propriedades cruciais que definem transações.

**Resposta:**  
As 4 propriedades fundamentais que definem transações compõem o acrônimo **ACID**:

1. **Atomicidade (*Atomicity*):** Princípio do "tudo ou nada". Todas as operações contidas na transação são executadas com sucesso e confirmadas, ou nenhuma alteração é aplicada, desfazendo qualquer modificação intermediária em caso de falha.
2. **Consistência (*Consistency*):** Assegura que o sistema passe de um estado válido a outro estado válido, respeitando todas as regras de validação, restrições estruturais (*constraints*) e invariantes do domínio.
3. **Isolamento (*Isolation*):** Garante que a execução paralela de transações concorrentes não interfira umas nas outras. O resultado visível no banco deve ser idêntico ao que ocorreria se fossem executadas de forma estritamente sequencial.
4. **Durabilidade (*Durability*):** Assegura que, uma vez efetuado o *commit* da transação, as alterações tornam-se permanentes e não serão perdidas, mesmo em caso de falhas de hardware, reinicializações do servidor ou falta de energia.

---

### Questão 4
**Enunciado:** O que são “invariantes de negócio”?

**Resposta:**  
**Invariantes de negócio** são regras, condições lógicas e restrições obrigatórias do domínio que **devem permanecer verdadeiras e válidas em todos os momentos** durante o ciclo de vida dos dados e das operações do sistema.

São restrições que definem a própria integridade e coerência do negócio. Se uma invariante for violada, o estado do sistema é considerado inconsistente e corrompido.
- *Exemplos:* 
  - "O saldo de uma conta bancária sem limite de crédito não pode ser negativo."
  - "Um pedido não pode ser finalizado/pago sem ao menos um item associado."
  - "A quantidade de um item em um pedido deve ser maior que zero."

No DDD, a responsabilidade de fiscalizar e garantir que nenhuma invariante seja violada pertence à Raiz do Agregado antes de confirmar qualquer transição de estado.

---

### Questão 5
**Enunciado:** Porque um agregado só deve ter acesso a outro agregado pelo ID?

**Resposta:**  
Um agregado só deve referenciar outro agregado através do seu identificador único (**ID**), e não por referência direta de objeto em memória (como `@ManyToOne EntidadeOutroAgregado`), pelos seguintes motivos arquiteturais:

1. **Delimitação da fronteira transacional:** A regra fundamental do DDD estabelece que **uma única transação deve modificar apenas uma instância de Agregado**. Se houvesse referências de objetos diretas em memória, desenvolvedores poderiam inadvertidamente carregar e alterar múltiplos agregados dentro da mesma transação, quebrando o isolamento.
2. **Escalabilidade e persistência desacoplada:** Ao armazenar apenas o ID, os agregados podem ser facilmente separados em tabelas distintas, bancos de dados diferentes ou até em microsserviços e esquemas independentes, sem acoplamento de schema relacional.
3. **Desempenho e consumo de memória:** Evita o problema de carregar grafos gigantescos de objetos interdependentes em cascata (*cascade eager/lazy loading* ineficiente), otimizando queries e tempo de resposta.
4. **Respeito ao encapsulamento:** Impede que um agregado acesse métodos internos de outro agregado sem passar pelo contrato formal de fronteira.

---

### Questão 6
**Enunciado:** Crie um trecho de código Java de uma entidade que represente um agregado e faça referência a outro agregado dentro do escopo do projeto Pet Friends.

**Resposta:**  
Abaixo está o exemplo da entidade `Pedido`, que atua como Raiz do Agregado (*Aggregate Root*). Ela referencia outro agregado (`Cliente`) exclusivamente por meio de seu identificador (`clienteId`), além de encapsular seus itens internos:

```java
package br.edu.infnet.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Pedido {

    private final UUID id;
    
    // Referência a outro agregado (Cliente) estritamente pelo ID:
    private final UUID clienteId;
    
    private final List<ItemPedido> itens = new ArrayList<>();
    private StatusPedido status;
    private BigDecimal valorTotal;

    public Pedido(UUID id, UUID clienteId) {
        this.id = Objects.requireNonNull(id, "ID do pedido é obrigatório.");
        this.clienteId = Objects.requireNonNull(clienteId, "ClienteID é obrigatório.");
        this.status = StatusPedido.CRIADO;
        this.valorTotal = BigDecimal.ZERO;
    }

    public void adicionarItem(UUID produtoId, String descricao, int quantidade, BigDecimal precoUnitario) {
        if (this.status != StatusPedido.CRIADO) {
            throw new IllegalStateException("Não é permitido alterar pedidos já fechados.");
        }
        ItemPedido item = new ItemPedido(produtoId, descricao, quantidade, precoUnitario);
        this.itens.add(item);
        this.valorTotal = this.valorTotal.add(item.calcularSubtotal());
    }

    public UUID getId() { return id; }
    public UUID getClienteId() { return clienteId; }
    public StatusPedido getStatus() { return status; }
    public BigDecimal getValorTotal() { return valorTotal; }
    public List<ItemPedido> getItens() { return Collections.unmodifiableList(itens); }
}
```

> **Localização no Repositório / Código Fonte:**  
> Arquivo: `TP1/src/main/java/br/edu/infnet/domain/model/Pedido.java`

---

### Questão 7
**Enunciado:** Elabore um trecho de código Java que mostre um método de negócio com a previsão de publicação de um evento de domínio dentro do escopo do projeto Pet Friends.

**Resposta:**  
No agregado `Pedido`, o método de negócio `pagar` valida as invariantes de negócio, altera o estado interno do agregado e registra a ocorrência do evento de domínio `PedidoPagoEvent` na sua lista interna de eventos pendentes de publicação:

```java
    /**
     * Método de negócio que altera o estado e registra um evento de domínio.
     */
    public void pagar(BigDecimal valorPago) {
        // 1. Validação de Invariantes de Negócio
        if (this.status == StatusPedido.PAGO) {
            throw new IllegalStateException("O pedido já se encontra pago.");
        }
        if (this.status == StatusPedido.CANCELADO) {
            throw new IllegalStateException("Não é possível pagar um pedido cancelado.");
        }
        if (this.itens.isEmpty()) {
            throw new IllegalStateException("O pedido não possui itens cadastrados.");
        }
        if (valorPago == null || valorPago.compareTo(this.valorTotal) < 0) {
            throw new IllegalArgumentException("Valor insuficiente para quitar o pedido.");
        }

        // 2. Mudança de estado interno do agregado
        this.status = StatusPedido.PAGO;

        // 3. Previsão e registro do evento de domínio para futura publicação
        PedidoPagoEvent evento = new PedidoPagoEvent(this.id, this.clienteId, valorPago);
        this.domainEvents.add(evento);
    }
```

> **Localização no Repositório / Código Fonte:**  
> Arquivo: `TP1/src/main/java/br/edu/infnet/domain/model/Pedido.java` (linhas 45 a 70)

---

### Questão 8
**Enunciado:** O que é “evento de domínio”?

**Resposta:**  
Um **Evento de Domínio** (*Domain Event*) é um registro imutável que expressa a ocorrência de um acontecimento relevante e significativo para os especialistas de negócio que **ocorreu no passado dentro do domínio**.

Principais características:
- **Nomeado no particípio passado:** Descreve algo que já é um fato consolidado (ex.: `PedidoCriado`, `PedidoPago`, `ConsultaAgendada`).
- **Imutabilidade:** Como representa um fato histórico, não pode ser modificado ou desfeito (apenas compensado por um novo evento futuro).
- **Conteúdo explicativo (*Payload*):** Transporta os dados necessários para compreender a mudança (identificador do agregado, carimbo de data/hora `occurredOn` e dados específicos da alteração).
- **Desacoplamento e Consistência Eventual:** Permite notificar outros agregados, módulos ou microsserviços de forma assíncrona, viabilizando consistência eventual sem bloquear a transação principal.

---

### Questão 9
**Enunciado:** Crie um trecho de código Java que mostre uma abstração de um objeto do tipo “evento de domínio”.

**Resposta:**  
A abstração padroniza o contrato fundamental de qualquer evento no sistema, assegurando identificação única e rastreabilidade temporal:

```java
package br.edu.infnet.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Abstração de Contrato para Eventos de Domínio.
 */
public interface DomainEvent {

    /**
     * Identificador universal e exclusivo da ocorrência do evento.
     */
    UUID getEventId();

    /**
     * Timestamp (data/hora UTC) em que o evento ocorreu no negócio.
     */
    Instant getOccurredOn();
}
```

> **Localização no Repositório / Código Fonte:**  
> Arquivo: `TP1/src/main/java/br/edu/infnet/domain/event/DomainEvent.java`

---

### Questão 10
**Enunciado:** Crie um trecho de código Java que mostre a implementação de um “evento de domínio” dentro do escopo do projeto Pet Friends.

**Resposta:**  
Implementação concreta do evento `PedidoPagoEvent` utilizando o recurso nativo de **Java Records** (Java 17+), que garante imutabilidade automática dos atributos e semântica expressiva:

```java
package br.edu.infnet.domain.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PedidoPagoEvent(
    UUID eventId,
    Instant occurredOn,
    UUID pedidoId,
    UUID clienteId,
    BigDecimal valorPago
) implements DomainEvent {

    /**
     * Construtor auxiliar que inicializa identificador único e timestamp atual.
     */
    public PedidoPagoEvent(UUID pedidoId, UUID clienteId, BigDecimal valorPago) {
        this(UUID.randomUUID(), Instant.now(), pedidoId, clienteId, valorPago);
    }

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public Instant getOccurredOn() {
        return occurredOn;
    }
}
```

> **Localização no Repositório / Código Fonte:**  
> Arquivo: `TP1/src/main/java/br/edu/infnet/domain/event/PedidoPagoEvent.java`

---

### Questão 11
**Enunciado:** Qual é a diferença entre filas e tópicos e como estes elementos funcionam em conjunto?

**Resposta:**  

#### Diferença Fundamental
- **Fila (*Queue* - Ponto a Ponto / *Point-to-Point*):**
  Uma mensagem enviada para uma fila é entregue a **apenas um consumidor** entre os possíveis ouvintes disponíveis (modelo concorrente/distribuição de carga). Uma vez que o consumidor processa e confirma a mensagem (*acknowledgement*), ela é removida da fila. Ideal para balanceamento de tarefas de trabalho.
- **Tópico (*Topic* - Publicação/Assinatura / *Publish-Subscribe*):**
  Uma mensagem enviada para um tópico é replicada e transmitida para **todos os assinantes inscritos** (*fan-out* / broadcast). Cada participante interessado recebe uma cópia idêntica da mensagem.

| Critério | Fila (*Queue*) | Tópico (*Topic*) |
| :--- | :--- | :--- |
| **Padrão de Comunicação** | Ponto a Ponto (1 para 1) | Publicação/Assinatura (1 para Muitos) |
| **Destino da Mensagem** | Consumida por um único trabalhador | Recebida por todos os assinantes ativos |
| **Objetivo Principal** | Balanceamento de carga de trabalho | Notificação de eventos a múltiplos serviços |

#### Como funcionam em conjunto
Eles operam de forma combinada no padrão **Fan-out com Filas Dedicadas** (amplamente empregado em RabbitMQ via *Exchanges* ou AWS SNS + SQS):
1. O serviço produtor publica um evento de domínio em um **Tópico**.
2. O tópico duplica o evento e o encaminha para as **Filas exclusivas** de cada serviço consumidor interessado (ex.: Fila do Faturamento, Fila do Estoque, Fila de Notificações).
3. Cada serviço consome de sua própria fila no seu próprio ritmo, com garantias de persistência, retentativas (*retries*) e isolamento: se um serviço consumidor ficar temporariamente fora do ar, sua respectiva fila retém as mensagens sem prejudicar os demais assinantes.

---

### Questão 12
**Enunciado:** Dê um exemplo de solução de arquitetura para publicação de eventos de domínio dentro do escopo do projeto Pet Friends (ideal um desenho).

**Resposta:**  
A solução de referência para garantir a publicação confiável e sem perda de dados em arquiteturas distribuídas é a combinação do padrão **Transactional Outbox** com um **Message Broker** (como Apache Kafka ou RabbitMQ):

#### Diagrama de Arquitetura

```
+---------------------------------------------------------------------------------------+
|                              MICROSSERVIÇO DE PEDIDOS                                 |
|                                                                                       |
|   [Cliente / API]                                                                     |
|          │                                                                            |
|          ▼                                                                            |
|   [PedidoService] ──► [Agregado Pedido]                                               |
|          │                 │                                                          |
|          │                 ▼                                                          |
|          │          Gera PedidoPagoEvent                                              |
|          │                                                                            |
|          ▼ (Mesma Transação ACID do Banco de Dados)                                   |
|   ┌────────────────────────────────────────────────────────┐                          |
|   │                  BANCO DE DADOS LOCAL                  │                          |
|   │  ┌─────────────────────────┐ ┌──────────────────────┐  │                          |
|   │  │    Tabela tb_pedido     │ │ Tabela tb_outbox     │  │                          |
|   │  │  (Estado Atual Pedido)  │ │ (Eventos pendentes)  │  │                          |
|   │  └─────────────────────────┘ └──────────┬───────────┘  │                          |
|   └─────────────────────────────────────────┼──────────────┘                          |
|                                             │                                         |
|                                    Lê registros pendentes                             |
|                                             ▼                                         |
|                             [Outbox Publisher / Worker / CDC]                         |
+─────────────────────────────────────────────┼─────────────────────────────────────────+
                                              │ Publica Mensagem
                                              ▼
                             ┌──────────────────────────────────┐
                             │       TÓPICO: pedidos-eventos    │ (Message Broker:
                             │      [PedidoPagoEvent Payload]   │  Kafka / RabbitMQ)
                             └────────────────┬─────────────────┘
                                              │
                      ┌───────────────────────┴───────────────────────┐
                      ▼                                               ▼
     ┌─────────────────────────────────┐             ┌─────────────────────────────────┐
     │      FILA: faturamento-pedidos  │             │     FILA: notificacao-pedidos   │
     └────────────────┬────────────────┘             └────────────────┬────────────────┘
                      ▼                                               ▼
         [Serviço de Faturamento]                        [Serviço de Notificação]
       (Emite Nota Fiscal / Cobrança)                     (Envia E-mail/WhatsApp Tutor)
```

#### Explicação dos Componentes:
1. **Transação Atômica Local (Outbox Pattern):** Quando o método `pagar` é executado, a aplicação salva a alteração na tabela de pedidos e, dentro da **mesma transação ACID**, grava o evento na tabela `tb_outbox`. Isso elimina a possibilidade de o banco salvar o pedido mas falhar na publicação do evento (ou vice-versa).
2. **Outbox Worker / CDC (Change Data Capture):** Um processo em segundo plano (como Debezium ou agendador interno) lê os eventos gravados na `tb_outbox` e os envia ao Message Broker.
3. **Tópico de Eventos (*Broker*):** Distribui o evento para as filas de cada microsserviço assinante.
4. **Consumidores Autônomos:** Os serviços interessados (ex.: Notificações, Logística e Faturamento) processam o evento de maneira assíncrona e desacoplada.

---

### Questão 13
**Enunciado:** Explique qual a finalidade de uma Event Store no contexto de eventos de domínio.

**Resposta:**  
Uma **Event Store** é um banco de dados especializado projetado exclusivamente para armazenar e consultar sequências de eventos de domínio imutáveis ordenados temporalmente (*event streams*).

No contexto de eventos de domínio e Event Sourcing, sua finalidade inclui:
1. **Fonte Primária da Verdade (*Single Source of Truth*):** Ao contrário dos bancos convencionais, o estado atual não fica salvo em tabelas mutáveis. A soma de todos os eventos registrados no stream do agregado é a verdade definitiva do sistema.
2. **Armazenamento Estritamente Incremental (*Append-Only*):** Garante que eventos nunca sofram `UPDATE` ou `DELETE`. Apenas novos eventos podem ser adicionados (`INSERT`/Append), garantindo auditoria perfeita e histórico inalterável.
3. **Controle de Concorrência Otimista nativo:** Utiliza controle de versão por fluxo (*stream versioning*). Se dois comandos tentarem gravar o evento de versão `N` simultaneamente, a Event Store rejeita o segundo com erro de concorrência, impedindo estados inconsistentes.
4. **Assinatura e Projeções em Tempo Real:** Fornece mecanismos de subscrição (*catch-up subscriptions* / *pub-sub*) para que consumidores e bancos de leitura (no padrão CQRS) sejam atualizados automaticamente.

---

### Questão 14
**Enunciado:** O que é Event Sourcing e como ele se diferencia da persistência tradicional em bancos de dados relacionais? Explique como os eventos salvos são usados para recuperar o estado atual de um Agregado.

**Resposta:**  

#### 1. O que é Event Sourcing e Diferença da Persistência Tradicional:
- **Persistência Tradicional (Baseada em Estado / CRUD):**  
  Modela o sistema através de tabelas relacionais onde cada linha representa o estado **atual** da entidade. Quando ocorre uma alteração, executa-se uma instrução `UPDATE`, **sobrescrevendo** o valor anterior. Como resultado, perde-se o histórico de alterações intermediárias e o motivo de negócio pelo qual o estado mudou.
- **Event Sourcing (Baseado em Eventos):**  
  Em vez de salvar o estado atual, persiste-se uma **série cronológica de eventos de domínio imutáveis** que documentam cada fato ocorrido com o agregado desde a sua criação até o momento presente. O banco de dados nunca sofre atualizações destrutivas.

#### 2. Como os eventos salvos são usados para recuperar o estado atual:
Para carregar um Agregado em memória, adota-se o processo de **Replay de Eventos (*Hydration*)**:
1. O repositório busca na Event Store todos os eventos associados ao identificador do agregado ordenados por data/versão (`streamId`).
2. Instancia-se o objeto da entidade no seu estado vazio/inicial.
3. Itera-se sobre cada evento chamando métodos internos mutadores (geralmente chamados `apply(event)`). Cada método lê os dados do evento e atualiza pontualmente os campos da entidade.
4. Ao término do processamento do último evento, o agregado encontra-se exatamente no seu estado atual mais recente, pronto para processar novos comandos de negócio.

#### 3. Otimização com Snapshots:
Quando um agregado acumula milhares de eventos, reprocessar todos eles a cada leitura pode gerar impacto de desempenho. Para resolver isso, cria-se periodicamente uma "foto" estática do agregado chamada **Snapshot** (por exemplo, a cada 100 eventos). Na recuperação, carrega-se diretamente o último Snapshot e aplicam-se apenas os eventos ocorridos a partir da versão daquele Snapshot.

---

### Instruções para Questões 6, 7, 9 e 10 (Links do Repositório GitHub)
Os arquivos de código Java foram organizados no projeto e estruturados para versionamento. Uma vez commitados e enviados para o repositório remoto, os links correspondentes são:

- **Questão 6 e Questão 7 (Agregado `Pedido` e Método de Negócio com Evento):**  
  [Pedido.java no GitHub](https://github.com/Marcus-Boni/INFNET-26E2-26E3/blob/main/26E3_4/TP1/src/main/java/br/edu/infnet/domain/model/Pedido.java)  
  *Caminho local:* `TP1/src/main/java/br/edu/infnet/domain/model/Pedido.java`

- **Questão 9 (Abstração `DomainEvent`):**  
  [DomainEvent.java no GitHub](https://github.com/Marcus-Boni/INFNET-26E2-26E3/blob/main/26E3_4/TP1/src/main/java/br/edu/infnet/domain/event/DomainEvent.java)  
  *Caminho local:* `TP1/src/main/java/br/edu/infnet/domain/event/DomainEvent.java`

- **Questão 10 (Implementação `PedidoPagoEvent`):**  
  [PedidoPagoEvent.java no GitHub](https://github.com/Marcus-Boni/INFNET-26E2-26E3/blob/main/26E3_4/TP1/src/main/java/br/edu/infnet/domain/event/PedidoPagoEvent.java)  
  *Caminho local:* `TP1/src/main/java/br/edu/infnet/domain/event/PedidoPagoEvent.java`
