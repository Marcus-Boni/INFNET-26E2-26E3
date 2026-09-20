import os
import docx
from docx.shared import Pt, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DOCX = os.path.join(BASE_DIR, "TP3_Respostas_Microsservicos_Comunicacao.docx")
IMG_DIR = os.path.join(BASE_DIR, "images")

doc = docx.Document()

# Margens padrão A4 / Carta (1.0 polegada)
for section in doc.sections:
    section.top_margin = Inches(1.0)
    section.bottom_margin = Inches(1.0)
    section.left_margin = Inches(1.0)
    section.right_margin = Inches(1.0)

COLOR_PRIMARY = RGBColor(31, 78, 121)    # Navy Blue
COLOR_SECONDARY = RGBColor(89, 89, 89)   # Slate Gray
COLOR_TEXT = RGBColor(38, 38, 38)        # Charcoal
COLOR_ENUNCIADO = RGBColor(46, 117, 182) # Accent Blue

def set_run_font(run, name="Calibri", size_pt=11, color=COLOR_TEXT, bold=False, italic=False):
    run.font.name = name
    run.font.size = Pt(size_pt)
    run.font.color.rgb = color
    run.bold = bold
    run.italic = italic

def add_header_title(doc, title, subtitle):
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    run_t = p.add_run(title)
    set_run_font(run_t, name="Calibri", size_pt=18, color=COLOR_PRIMARY, bold=True)
    
    p2 = doc.add_paragraph()
    p2.alignment = WD_ALIGN_PARAGRAPH.CENTER
    p2.paragraph_format.space_after = Pt(14)
    run_s = p2.add_run(subtitle)
    set_run_font(run_s, name="Calibri", size_pt=11, color=COLOR_SECONDARY, italic=True)
    
    p_hr = doc.add_paragraph()
    p_hr.paragraph_format.space_after = Pt(14)
    p_hr_border = parse_xml(r'<w:pBdr xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
                            r'<w:bottom w:val="single" w:sz="12" w:space="1" w:color="1F4E79"/>'
                            r'</w:pBdr>')
    p_hr._p.get_or_add_pPr().append(p_hr_border)

def add_code_block(doc, code_str):
    table = doc.add_table(rows=1, cols=1)
    table.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = table.cell(0, 0)
    
    shading = parse_xml(r'<w:shd {} w:fill="F4F5F7"/>'.format(nsdecls('w')))
    cell._tc.get_or_add_tcPr().append(shading)
    
    borders = parse_xml(r'<w:tcBorders xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">'
                        r'<w:top w:val="single" w:sz="4" w:space="0" w:color="D1D5DB"/>'
                        r'<w:left w:val="single" w:sz="18" w:space="0" w:color="1F4E79"/>'
                        r'<w:bottom w:val="single" w:sz="4" w:space="0" w:color="D1D5DB"/>'
                        r'<w:right w:val="single" w:sz="4" w:space="0" w:color="D1D5DB"/>'
                        r'</w:tcBorders>')
    cell._tc.get_or_add_tcPr().append(borders)
    
    cell.paragraphs[0].text = ""
    lines = code_str.strip().split("\n")
    for idx, line in enumerate(lines):
        p = cell.paragraphs[0] if idx == 0 else cell.add_paragraph()
        p.paragraph_format.space_before = Pt(0)
        p.paragraph_format.space_after = Pt(0)
        p.paragraph_format.line_spacing = 1.05
        run = p.add_run(line if line else " ")
        set_run_font(run, name="Consolas", size_pt=9.0, color=RGBColor(33, 37, 41))
        
    p_sp = doc.add_paragraph()
    p_sp.paragraph_format.space_after = Pt(6)

def add_image_with_caption(doc, image_path, caption_text):
    if os.path.exists(image_path):
        p_img = doc.add_paragraph()
        p_img.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_img.paragraph_format.space_before = Pt(8)
        p_img.paragraph_format.space_after = Pt(4)
        run_img = p_img.add_run()
        run_img.add_picture(image_path, width=Inches(6.2))
        
        p_cap = doc.add_paragraph()
        p_cap.alignment = WD_ALIGN_PARAGRAPH.CENTER
        p_cap.paragraph_format.space_before = Pt(0)
        p_cap.paragraph_format.space_after = Pt(8)
        run_cap = p_cap.add_run(caption_text)
        set_run_font(run_cap, name="Calibri", size_pt=9.5, color=COLOR_SECONDARY, italic=True)

def add_question(doc, num, enunciado, resposta_paragraphs, code_snippets=None, table_data=None, image_info=None, note=None):
    p_q = doc.add_paragraph()
    p_q.paragraph_format.space_before = Pt(14)
    p_q.paragraph_format.space_after = Pt(3)
    p_q.paragraph_format.keep_with_next = True
    run_q = p_q.add_run(f"Questão {num}")
    set_run_font(run_q, name="Calibri", size_pt=13, color=COLOR_PRIMARY, bold=True)
    
    p_e = doc.add_paragraph()
    p_e.paragraph_format.space_after = Pt(6)
    p_e.paragraph_format.left_indent = Inches(0.2)
    p_e.paragraph_format.keep_with_next = True
    run_el = p_e.add_run("Enunciado: ")
    set_run_font(run_el, name="Calibri", size_pt=11, color=COLOR_ENUNCIADO, bold=True)
    run_et = p_e.add_run(enunciado)
    set_run_font(run_et, name="Calibri", size_pt=11, color=COLOR_ENUNCIADO, italic=True)
    
    p_r = doc.add_paragraph()
    p_r.paragraph_format.space_after = Pt(4)
    p_r.paragraph_format.keep_with_next = True
    run_rl = p_r.add_run("Resposta:")
    set_run_font(run_rl, name="Calibri", size_pt=11, color=COLOR_PRIMARY, bold=True)
    
    for item in resposta_paragraphs:
        p_b = doc.add_paragraph()
        p_b.paragraph_format.space_after = Pt(5)
        p_b.paragraph_format.line_spacing = 1.15
        if isinstance(item, tuple) and len(item) == 2:
            prefix, text = item
            run_p = p_b.add_run(prefix)
            set_run_font(run_p, name="Calibri", size_pt=11, color=COLOR_TEXT, bold=True)
            run_t = p_b.add_run(text)
            set_run_font(run_t, name="Calibri", size_pt=11, color=COLOR_TEXT)
        else:
            run_t = p_b.add_run(item)
            set_run_font(run_t, name="Calibri", size_pt=11, color=COLOR_TEXT)
            
    if table_data:
        headers, rows = table_data
        table = doc.add_table(rows=len(rows) + 1, cols=len(headers))
        table.alignment = WD_TABLE_ALIGNMENT.CENTER
        
        for col_idx, h in enumerate(headers):
            cell = table.cell(0, col_idx)
            cell.text = h
            shading = parse_xml(r'<w:shd {} w:fill="1F4E79"/>'.format(nsdecls('w')))
            cell._tc.get_or_add_tcPr().append(shading)
            for p in cell.paragraphs:
                p.alignment = WD_ALIGN_PARAGRAPH.CENTER
                for run in p.runs:
                    set_run_font(run, name="Calibri", size_pt=10.0, color=RGBColor(255, 255, 255), bold=True)
                    
        for row_idx, row in enumerate(rows):
            for col_idx, val in enumerate(row):
                cell = table.cell(row_idx + 1, col_idx)
                cell.text = val
                bg = "F9FAFB" if row_idx % 2 == 0 else "FFFFFF"
                shading = parse_xml(r'<w:shd {} w:fill="{}"/>'.format(nsdecls('w'), bg))
                cell._tc.get_or_add_tcPr().append(shading)
                for p in cell.paragraphs:
                    p.paragraph_format.space_before = Pt(3)
                    p.paragraph_format.space_after = Pt(3)
                    for run in p.runs:
                        set_run_font(run, name="Calibri", size_pt=9.5, color=COLOR_TEXT)
        
        p_sp = doc.add_paragraph()
        p_sp.paragraph_format.space_after = Pt(6)

    if code_snippets:
        for code in code_snippets:
            add_code_block(doc, code)

    if image_info:
        img_path, caption = image_info
        add_image_with_caption(doc, img_path, caption)

    if note:
        p_n = doc.add_paragraph()
        p_n.paragraph_format.space_before = Pt(3)
        p_n.paragraph_format.space_after = Pt(8)
        p_n.paragraph_format.left_indent = Inches(0.2)
        run_nl = p_n.add_run("Referência / Código-Fonte: ")
        set_run_font(run_nl, name="Calibri", size_pt=10, color=COLOR_SECONDARY, bold=True)
        run_nt = p_n.add_run(note)
        set_run_font(run_nt, name="Calibri", size_pt=10, color=COLOR_SECONDARY, italic=True)

# ==============================================================================
# CABEÇALHO DO DOCUMENTO
# ==============================================================================
add_header_title(
    doc,
    "Trabalho Prático 3 (TP3) - Arquitetura de Microsserviços e Mensageria",
    "Engenharia Disciplinada de Software | Padrões de Comunicação, Brokers, Resiliência e Sagas"
)

# ==============================================================================
# QUESTÃO 1
# ==============================================================================
add_question(
    doc, 1,
    "Explique os padrões de comunicação utilizados em uma arquitetura de microsserviços.",
    [
        "Em uma arquitetura monolítica, os componentes comunicam-se em memória por chamadas de métodos locais (in-process). Em microsserviços, como os serviços operam em processos e servidores fisicamente distintos, toda a comunicação é distribuída e ocorre através da rede (Inter-Process Communication - IPC).",
        "Os padrões de comunicação estruturam-se em três dimensões fundamentais:",
        ("1. Dimensão Temporal (Síncrona vs. Assíncrona): ", "Na comunicação síncrona (Request/Response), o cliente envia uma solicitação e aguarda bloqueado o retorno da resposta antes de continuar seu fluxo (ex.: HTTP/REST, gRPC, GraphQL). Na comunicação assíncrona (Event-driven / Message-driven), o produtor posta uma mensagem ou evento na rede e retoma imediatamente sua execução sem esperar o processamento do consumidor."),
        ("2. Dimensão de Cardinalidade (Ponto a Ponto vs. Publicador/Assinante): ", "No padrão Ponto a Ponto (Point-to-Point / 1-to-1), a mensagem destina-se a exatamente um único consumidor específico (ex.: fila de tarefas). No padrão Publicador/Assinante (Publish/Subscribe / 1-to-Many), o produtor publica um evento em um tópico compartilhado e múltiplos microsserviços interessados recebem cópias e o processam paralelamente."),
        ("3. Dimensão Topológica (Comunicação Direta vs. Intermediada): ", "Na comunicação direta (Peer-to-Peer), os serviços conectam-se diretamente às portas e IPs dos pares através de Service Discovery. Na comunicação intermediada por Message Broker, os serviços não conhecem a localização física uns dos outros; todas as mensagens trafegam por um middleware central (ex.: Apache Kafka, RabbitMQ).")
    ]
)

# ==============================================================================
# QUESTÃO 2
# ==============================================================================
table_q2 = (
    ["Critério de Comparação", "Comunicação Síncrona", "Comunicação Assíncrona"],
    [
        ["Bloqueio de Threads", "Sim (thread retida aguardando resposta)", "Não (thread liberada imediatamente)"],
        ["Acoplamento Temporal", "Alto (ambos devem estar online juntos)", "Baixo (desacoplamento total no tempo)"],
        ["Protocolos Comuns", "HTTP/REST, gRPC, GraphQL", "AMQP, Kafka Protocol, MQTT, STOMP"],
        ["Resiliência e Falhas", "Frágil (sujeita a falhas em cascata)", "Alta (mensagens ficam no buffer do broker)"],
        ["Absorção de Picos", "Pode sobrecarregar o receptor", "Excelente (amortecimento / load leveling)"],
        ["Complexidade Operacional", "Simples de entender e rastrear", "Maior (consistência eventual, tracing distribuído)"]
    ]
)

add_question(
    doc, 2,
    "Qual a diferença entre a comunicação síncrona e a comunicação assíncrona em uma arquitetura de microsserviços?",
    [
        "A principal diferença reside no acoplamento temporal, de execução e de resiliência entre o serviço remetente (produtor) e o serviço destinatário (consumidor):",
        ("1. Bloqueio de Threads e Uso de Recursos: ", "Na comunicação síncrona, o remetente mantém sua thread bloqueada aguardando os dados atravessarem a rede, serem processados no destino e retornarem. Se o serviço destino estiver lento, as threads do remetente esgotam-se rapidamente (thread starvation). Na comunicação assíncrona, o remetente posta a mensagem no broker e libera a thread imediatamente para atender novos clientes."),
        ("2. Acoplamento Temporal e Disponibilidade: ", "A comunicação síncrona exige que emissor e receptor estejam 100% disponíveis no mesmo milissegundo. Se o receptor estiver reiniciando, a chamada falha de imediato. Na comunicação assíncrona, há desacoplamento temporal completo: se o consumidor estiver fora do ar, as mensagens aguardam no buffer persistente do broker e são processadas normalmente assim que ele retornar, sem perda de dados."),
        ("3. Propagação de Latência em Cascata: ", "No modelo síncrono, as latências de uma cadeia de 4 serviços somam-se diretamente no tempo de espera do usuário final. No modelo assíncrono, as etapas subsequentes ocorrem em paralelo em segundo plano.")
    ],
    table_data=table_q2
)

# ==============================================================================
# QUESTÃO 3
# ==============================================================================
add_question(
    doc, 3,
    "Apresente um exemplo real da diferença da comunicação síncrona e assíncrona em uma arquitetura de microsserviços.",
    [
        "Um caso real exemplar é o fluxo de Finalização de Compra (Checkout) em uma plataforma de comércio eletrônico (E-commerce):",
        ("Cenário Síncrono (Anti-pattern quando usado em excesso): ", "O usuário clica em 'Finalizar Pedido'. O Serviço de Checkout executa chamadas HTTP sequenciais: (1) chama o Serviço de Pagamento (espera 1.2s); (2) chama o Serviço de Estoque para reservar itens (espera 0.8s); (3) chama o Serviço Fiscal para gerar Nota Fiscal na SEFAZ (espera 2.5s); (4) chama o Serviço de Notificação para enviar e-mail (espera 0.9s); (5) chama o Serviço de Logística para gerar frete (espera 1.5s). O usuário fica travado olhando uma barra de carregamento por quase 7 segundos. Se a SEFAZ estiver instável, toda a compra falha e o cliente cancela a compra."),
        ("Cenário Híbrido / Assíncrono com Mensageria (Recomendado): ", "O usuário clica em 'Finalizar Pedido'. O Serviço de Checkout processa de forma síncrona apenas o estritamente imediato: valida os dados e autoriza a cobrança no gateway de cartão (500ms). Ao receber a aprovação, o serviço grava o pedido no banco, publica o evento PedidoPagoEvent no Message Broker e devolve de imediato HTTP 200 OK 'Pedido confirmado!' ao usuário em menos de 1 segundo. De forma assíncrona e em paralelo, os microsserviços de Estoque, Fiscal, Notificação e Logística consom o evento do broker no seu próprio ritmo. Se o serviço fiscal demorar alguns segundos, o usuário não é afetado; a mensagem fica segura na fila até a emissão ser concluída.")
    ]
)

# ==============================================================================
# QUESTÃO 4
# ==============================================================================
add_question(
    doc, 4,
    "Explique os tipos de comunicação assíncrona em uma arquitetura de microsserviços.",
    [
        "A comunicação assíncrona subdivide-se em padrões distintos de acordo com a finalidade e a topologia dos dados:",
        ("1. One-Way Messaging (Fire-and-Forget / Enviar e Esquecer): ", "O produtor publica um comando ou registro em uma fila e continua seu processamento sem esperar nenhuma resposta posterior. Típico de logs de auditoria, envio de telemetria, indexação de busca ou webhooks."),
        ("2. Publish/Subscribe (Pub/Sub): ", "O emissor publica uma mensagem em um tópico sem conhecer a identidade ou a quantidade de destinatários. O broker entrega cópias independentes para todos os serviços que assinaram aquele tópico. Ideal para eventos de domínio de negócio onde múltiplos departamentos precisam reagir ao mesmo fato."),
        ("3. Request / Asynchronous Response: ", "O emissor necessita de uma resposta, mas sem bloquear a conexão. Ele envia a requisição com um CorrelationId e uma fila de retorno (ReplyTo). O consumidor processa e posta o resultado na fila ReplyTo. O emissor consome a resposta de forma assíncrona associando-a pelo CorrelationId."),
        ("4. Event-Carried State Transfer (ECST): ", "O produtor emite um evento que carrega consigo todos os dados completos do estado da entidade (payload rico). Os microsserviços consumidores atualizam suas réplicas locais de dados sem precisar consultar a API do produtor de volta, garantindo autonomia máxima."),
        ("5. Notification Events: ", "O evento carrega apenas um identificador conciso e o tipo de ocorrência (ex.: {'evento': 'PEDIDO_ATUALIZADO', 'id': '123'}). Se o consumidor precisar de dados detalhados, ele realiza uma consulta posterior à API do dono do dado.")
    ]
)

# ==============================================================================
# QUESTÃO 5
# ==============================================================================
add_question(
    doc, 5,
    "Explique a comunicação assíncrona em uma arquitetura de microsserviços utilizando o padrão de mensagens.",
    [
        "O Padrão de Mensagens (Messaging Pattern) estrutura a troca de dados entre microsserviços por meio de unidades de dados autocontidas denominadas Mensagens, transmitidas por canais virtuais.",
        ("Estrutura de uma Mensagem Autocontida: ", "Uma mensagem padrão é composta por duas seções essenciais: (1) Cabeçalho (Headers/Metadata): armazena identificadores únicos como MessageId (UUID), CorrelationId (para rastreamento distribuído entre serviços), Timestamp, RoutingKey e ContentType; (2) Corpo (Payload): contém os dados concretos do domínio serializados em formatos neutros (JSON, Protocol Buffers ou Apache Avro)."),
        ("Desacoplamento de Plataforma e Contrato: ", "O produtor pode ser implementado em Go e o consumidor em Java. Eles não compartilham memória nem contratos de interfaces binárias; o único acoplamento existente é o esquema de dados acordado da mensagem."),
        ("Garantia de Persistência Temporária: ", "A mensagem permanece protegida no canal até que o destinatário correto esteja disponível para consumi-la, eliminando a perda de transações decorrente de oscilações de rede.")
    ]
)

# ==============================================================================
# QUESTÃO 6
# ==============================================================================
add_question(
    doc, 6,
    "Explique a comunicação assíncrona em uma arquitetura de microsserviços utilizando um message broker.",
    [
        "O Message Broker é um componente intermediário de infraestrutura (middleware) projetado especificamente para receber, armazenar em buffer, rotear e entregar mensagens entre microsserviços desacoplados.",
        ("Funcionamento Detalhado: ", "Em vez de serviços chamarem diretamente os endereços de rede dos pares, todas as mensagens são direcionadas ao broker. O broker gerencia Filas (Queues) para processamento ponto a ponto concorrente e Tópicos (Topics/Exchanges) para difusão Publish/Subscribe."),
        ("Mecanismos de Confiabilidade (ACK / NACK): ", "O broker só considera uma mensagem concluída quando o consumidor envia uma confirmação de sucesso (ACK - Acknowledgement). Se o consumidor sofrer uma queda ou timeout antes do ACK, o broker reatribui a mensagem a outro nó disponível (NACK)."),
        ("Dead Letter Queues (DLQ): ", "Mensagens que causam falhas repetidas (poison pills) são automaticamente isoladas em uma fila de cartas mortas (DLQ), impedindo o travamento da fila principal e viabilizando auditoria e correção posterior.")
    ]
)

# ==============================================================================
# QUESTÃO 7
# ==============================================================================
add_question(
    doc, 7,
    "Apresente as vantagens e desvantagens da comunicação assíncrona em uma arquitetura de microsserviços utilizando um message broker.",
    [
        ("Principais Vantagens:", ""),
        ("• Desacoplamento Espacial e Temporal: ", "Produtores e consumidores operam sem conhecer a localização física uns dos outros e sem necessidade de estarem online simultaneamente."),
        ("• Nivelamento de Carga (Load Leveling / Traffic Smoothing): ", "O broker atua como um amortecedor contra picos abruptos de tráfego, acumulando requisições no buffer para que os serviços processem no seu ritmo ideal sem sofrer sobrecarga (backpressure)."),
        ("• Alta Resiliência: ", "Falhas transitórias em microsserviços ou bancos não causam perda de dados nem interrupções no fluxo de entrada do cliente."),
        ("• Escalabilidade Horizontal Independente: ", "Pode-se aumentar o poder de processamento de qualquer etapa apenas adicionando mais instâncias trabalhadoras na fila correspondente."),
        ("Principais Desvantagens:", ""),
        ("• Complexidade Operacional e de Infraestrutura: ", "Exige configuração, monitoramento de clusters de brokers, dimensionamento de partições, réplicas e storage."),
        ("• Adoção Obrigatória de Consistência Eventual: ", "As alterações não se refletem instantaneamente em todos os serviços; há um intervalo temporal onde leituras em diferentes serviços podem divergir temporariamente."),
        ("• Dificuldade de Depuração e Observabilidade: ", "Rastrear fluxos distribuídos por múltiplos tópicos assíncronos requer soluções complexas de Tracing Distribuído (OpenTelemetry/Jaeger) e Correlation IDs."),
        ("• Risco de Mensagens Duplicadas e Reordenação: ", "Requer lógica de idempotência em todos os consumidores para prevenir efeitos colaterais repetidos gerados por reentregas.")
    ]
)

# ==============================================================================
# QUESTÃO 8
# ==============================================================================
add_question(
    doc, 8,
    "Dê exemplos de, pelo menos, três message brokers de código aberto. Explique o funcionamento e as vantagens de um deles.",
    [
        ("Três Exemplos de Message Brokers Open Source: ", "1. Apache Kafka; 2. RabbitMQ; 3. Apache Pulsar (ou Apache ActiveMQ)."),
        ("Detalhamento Aprofundado: Apache Kafka", ""),
        ("Funcionamento do Apache Kafka: ", "O Kafka baseia-se no conceito de Commit Log Imutável Distribuído. As mensagens são salvas em arquivos de log em disco de forma sequencial (append-only). O fluxo é organizado em Tópicos, divididos em Partições distribuídas pelos servidores (Brokers) do cluster. Cada mensagem em uma partição recebe um identificador sequencial crescente chamado Offset. O modelo de consumo é baseado em Pull: os consumidores (Consumer Groups) realizam leituras no seu próprio ritmo e guardam a posição do ponteiro do offset que já processaram. As mensagens não são apagadas após o consumo, permanecendo no log por um tempo de retenção configurável (ex.: 7 dias)."),
        ("Principais Vantagens do Apache Kafka: ", "1. Altíssimo Throughput: processa milhões de mensagens por segundo com latência de milissegundos graças a I/O sequencial em disco, page cache e zero-copy (sendfile); 2. Replay de Histórico (Time-Travel): permite que novos microsserviços redefinam seu offset e processem novamente todo o histórico passado; 3. Ordenação Estrita Garantida por Partição; 4. Escalabilidade Horizontal Nativa distribuída entre nós do cluster.")
    ]
)

# ==============================================================================
# QUESTÃO 9
# ==============================================================================
add_question(
    doc, 9,
    "Dê um exemplo de message broker oferecido por um provedor de nuvem. Explique o seu funcionamento e suas vantagens.",
    [
        ("Exemplo em Nuvem: AWS SQS (Simple Queue Service) e AWS SNS (Simple Notification Service) na Amazon Web Services (AWS)", ""),
        ("Funcionamento do Padrão AWS SNS + SQS Fan-Out: ", "O AWS SNS atua como o publicador de tópicos Publish/Subscribe. Quando um microsserviço publica um evento no tópico SNS, o serviço replica instantaneamente a mensagem para múltiplas filas AWS SQS inscritas. Cada microsserviço consumidor possui sua própria fila SQS durável. O consumidor busca as mensagens via Long Polling. Durante o processamento, a mensagem entra em estado de Visibility Timeout (fica temporariamente invisível para outros). Se o consumidor processar com sucesso, ele envia o comando DeleteMessage. Se o nó falhar, o tempo expira e a mensagem volta a ficar visível na fila para reprocessamento."),
        ("Principais Vantagens do AWS SQS/SNS: ", "1. 100% Serverless e Gerenciado: zero gerenciamento de instâncias EC2, patches, partições manuais ou clusters; 2. Escalabilidade Infinita Automática: escala de zero a centenas de milhares de mensagens por segundo sob demanda; 3. Alta Disponibilidade Multi-AZ: replicação nativa e durabilidade extrema em múltiplos data centers independentes; 4. Custo Pay-As-You-Go: cobrança estrita por volume de requisições, com tier gratuito perpétuo de 1 milhão de requisições mensais; 5. Integração Nativa: conecta-se perfeitamente com AWS Lambda, IAM e CloudWatch.")
    ]
)

# ==============================================================================
# QUESTÃO 10
# ==============================================================================
add_question(
    doc, 10,
    "Quais os desafios e as soluções para o processamento concorrente de mensagens para garantir a ordenação das mensagens?",
    [
        ("Desafios da Concorrência na Ordenação: ", "Em microsserviços escalados horizontalmente, múltiplos nós consumidores processam mensagens em paralelo. Como a latência de rede e o tempo de execução variam, uma mensagem postada depois (ex.: PedidoCancelado) pode ser concluída antes da mensagem original (ex.: PedidoCriado). Isso acarreta estados inconsistentes no banco de dados e violações de regras de negócio."),
        ("Soluções Arquiteturais:", ""),
        ("1. Particionamento Determinístico por Chave de Negócio (Partition Key / Sharding): ", "O produtor anexa uma Partition Key à mensagem (ex.: orderId ou customerId). O broker aplica uma função hash determinística: Partição = hash(PartitionKey) % total_particoes. Todas as mensagens referentes àquela mesma entidade cairão obrigatoriamente na mesma partição física. Como cada partição é processada por apenas um consumidor ativo por vez, a ordenação estrita é garantida para aquela entidade, enquanto outras entidades continuam sendo processadas concorrentemente em outras partições."),
        ("2. Versionamento Lógico de Estados: ", "As mensagens transmitem números de sequência monotônicos (version=1, version=2...). O consumidor só aplica alterações se a versão recebida for exatamente o sucessor do estado gravado no banco; mensagens fora de ordem aguardam em staging."),
        ("3. Filas FIFO Dedicadas (Single Active Consumer): ", "Utilização de filas estritas FIFO com nó exclusivo de consumo (como AWS SQS FIFO ou Single Active Consumer no RabbitMQ).")
    ]
)

# ==============================================================================
# QUESTÃO 11
# ==============================================================================
code_q11 = """// Exemplo de Implementação do Padrão Idempotent Consumer em Java:
package br.edu.infnet.messaging.idempotency;

public class IdempotentConsumerService {
    private final Set<UUID> processedMessageStore = Collections.synchronizedSet(new HashSet<>());

    public boolean processMessage(UUID messageId, String messageType, String payload) {
        // 1. Verificação prévia: a mensagem já foi processada?
        if (processedMessageStore.contains(messageId)) {
            System.out.println("[IDEMPOTÊNCIA] Mensagem duplicada ignorada: " + messageId);
            return false; // Descarte seguro
        }

        // 2. Executa a lógica de negócio e registra o ID na mesma transação
        executeBusinessLogic(payload);
        processedMessageStore.add(messageId);
        return true;
    }
}"""

add_question(
    doc, 11,
    "Quais os desafios e as soluções para o processamento concorrente de mensagens para o tratamento de mensagens duplicadas?",
    [
        ("Desafios das Mensagens Duplicadas: ", "A maioria dos message brokers adota a garantia de entrega At-Least-Once (pelo menos uma vez). Se o consumidor processar com sucesso a mensagem mas a rede falhar milissegundos antes do envio do ACK ao broker, o broker assume falha e reentrega a mensagem. Sem proteção, ocorrem efeitos colaterais duplicados graves: cobranças duplas no cartão, baixas múltiplas de estoque ou envio repetido de e-mails."),
        ("Soluções Arquiteturais:", ""),
        ("1. Padrão Consumidor Idempotente (Idempotent Consumer Pattern): ", "Toda mensagem carrega um MessageId (UUID) universalmente único. O consumidor armazena os IDs das mensagens finalizadas em uma tabela de deduplicação (tb_processed_messages) com restrição UNIQUE / PRIMARY KEY. Caso receba uma mensagem com ID já registrado, ele descarta o processamento de imediato e emite o ACK."),
        ("2. Operações de Negócio Comutativas / Finais: ", "Modelar operações de atualização baseadas em estado final (ex.: SET status = 'PAGO') em vez de incrementos delta."),
        ("3. Bloqueio Otimista com Coluna de Versão (@Version): ", "Rejeita alterações de mensagens duplicadas cuja versão seja menor ou igual à já persistida no banco.")
    ],
    code_snippets=[code_q11],
    image_info=(
        os.path.join(IMG_DIR, "ide_idempotent_consumer.png"),
        "Figura 1: Print de tela da IDE com o código de IdempotentConsumerService.java"
    ),
    note="TP3/src/main/java/br/edu/infnet/messaging/idempotency/IdempotentConsumerService.java"
)

# ==============================================================================
# QUESTÃO 12
# ==============================================================================
code_q12 = """// Exemplo de Implementação do Transactional Outbox Pattern em Java:
package br.edu.infnet.messaging.outbox;

public class OrderOutboxService {
    public void createOrder(UUID orderId, UUID customerId, BigDecimal amount) {
        // Início da Transação Local ACID (ex.: @Transactional no Spring)
        try {
            // 1. Grava a entidade de negócio na tabela tb_pedidos
            orderTable.add("ORDER[id=" + orderId + ", total=" + amount + "]");

            // 2. Grava o evento na tabela tb_outbox DENTRO DA MESMA TRANSAÇÃO
            OutboxMessage msg = new OutboxMessage("Order", orderId, "OrderCreated", "{...}");
            outboxTable.add(msg);

            // Commit atômico no banco de dados (Elimina o Dual-Write Problem!)
        } catch (Exception ex) {
            // Rollback atômico garantido
            throw ex;
        }
    }
}"""

add_question(
    doc, 12,
    "Quais os desafios e as soluções para o tratamento de transações de mensagens em bancos de dados em uma arquitetura de microsserviços?",
    [
        ("O Desafio do Dual-Write Problem: ", "Quando um microsserviço precisa atualizar o banco de dados local e enviar um evento ao Message Broker, essas duas operações não compartilham uma transação atômica ACID. Se comitar no banco primeiro e o broker cair, o evento é perdido para sempre. Se publicar no broker primeiro e o commit no banco falhar, outros microsserviços reagirão a um pedido fantasma. Protocolos de transação distribuída como 2PC (Two-Phase Commit / XA) são lentos, bloqueantes e não suportados por brokers modernos."),
        ("A Solução: O Padrão Transactional Outbox (Outbox Pattern): ", "O serviço cria uma tabela tb_outbox no mesmo banco relacional. Dentro da MESMA transação ACID local, ele salva a entidade de negócio e insere o evento na tb_outbox. Ambas as gravações concluem juntas de forma atômica. Um processo desacoplado em segundo plano (Outbox Poller ou ferramenta de CDC como Debezium lendo o Write-Ahead Log do banco) lê as mensagens da tb_outbox e as publica com total confiabilidade no Message Broker.")
    ],
    code_snippets=[code_q12],
    image_info=(
        os.path.join(IMG_DIR, "ide_transactional_outbox.png"),
        "Figura 2: Print de tela da IDE com o código de OrderOutboxService.java (Transactional Outbox)"
    ),
    note="TP3/src/main/java/br/edu/infnet/messaging/outbox/OrderOutboxService.java"
)

# ==============================================================================
# QUESTÃO 13
# ==============================================================================
add_question(
    doc, 13,
    "Quais os problemas relacionados ao uso de banco de dados em uma arquitetura de microsserviços assíncronos?",
    [
        "A adoção do padrão Database-per-Service em arquiteturas assíncronas gera desafios estruturais de banco de dados:",
        ("1. Ausência de Transações ACID Globais: ", "Não é possível executar um BEGIN TRANSACTION abrangendo múltiplos microsserviços heterogêneos."),
        ("2. Inconsistência Temporária e Lag de Leitura (Read-Your-Own-Writes): ", "Como as mensagens levam tempo para serem consumidas e projetadas nos bancos de destino, o usuário pode cadastrar um registro e não vê-lo de imediato na listagem seguinte."),
        ("3. Impossibilidade de JOINs Distribuídos: ", "Consultas complexas que uniam 4 tabelas no monólito agora exigem padrões como API Composition ou CQRS (Command Query Responsibility Segregation) com bancos de leitura desnormalizados."),
        ("4. Falhas Parciais sem Rollback Nativo: ", "Se a 4ª etapa de um fluxo assíncrono falhar, as etapas anteriores já foram confirmadas no banco, exigindo transações compensatórias para restaurar o estado."),
        ("5. Sincronização e Versionamento de Esquemas: ", "Alterações de colunas no banco afetam os schemas dos eventos, exigindo governança com Schema Registries.")
    ]
)

# ==============================================================================
# QUESTÃO 14
# ==============================================================================
code_q14 = """// Exemplo de Orquestrador de Saga com Transações Compensatórias em Java:
package br.edu.infnet.messaging.saga;

public class OrderSagaOrchestrator {
    private final List<SagaStep> steps = new ArrayList<>();

    public boolean executeSaga() {
        Deque<SagaStep> executedSteps = new ArrayDeque<>();
        for (SagaStep step : steps) {
            if (step.execute()) {
                executedSteps.push(step);
            } else {
                // Em caso de falha, aciona compensações em ordem reversa (LIFO)
                rollback(executedSteps);
                return false;
            }
        }
        return true;
    }

    private void rollback(Deque<SagaStep> executedSteps) {
        while (!executedSteps.isEmpty()) {
            executedSteps.pop().compensate(); // Desfaz semanticamente
        }
    }
}"""

add_question(
    doc, 14,
    "Explique o gerenciamento de transações utilizando o padrão Sagas para manter a consistência dos dados em uma arquitetura de microsserviços assíncronos.",
    [
        ("Conceito Fundamental do Padrão Sagas: ", "Uma Saga é uma sequência ordenada de transações locais distribuídas. Cada etapa atualiza os dados no banco de um microsserviço e emite uma mensagem que aciona a etapa seguinte, substituindo transações distribuídas 2PC."),
        ("Transações Compensatórias (Rollback Semântico): ", "Como cada transação local já realizou o commit físico no seu banco, se alguma etapa posterior falhar, a Saga executa transações compensatórias em ordem estritamente reversa (LIFO) para desfazer semanticamente os efeitos anteriores (ex.: estornar a reserva de estoque se o pagamento for rejeitado)."),
        ("Abordagens de Implementação:", ""),
        ("• Saga por Coreografia: ", "Descentralizada. Os serviços reagem autonomamente aos eventos uns dos outros. Adequada para fluxos simples de poucas etapas."),
        ("• Saga por Orquestração: ", "Centralizada. Um serviço Orquestrador (máquina de estados) coordena explicitamente as etapas, chamando comandos assíncronos e gerenciando a esteira de compensação em caso de erro.")
    ],
    code_snippets=[code_q14],
    image_info=(
        os.path.join(IMG_DIR, "ide_saga_orchestrator.png"),
        "Figura 3: Print de tela da IDE com o código de OrderSagaOrchestrator.java (Padrão Sagas)"
    ),
    note="TP3/src/main/java/br/edu/infnet/messaging/saga/OrderSagaOrchestrator.java"
)

# Salva o arquivo gerado
doc.save(OUTPUT_DOCX)
print(f"Documento DOCX gerado com sucesso em: {OUTPUT_DOCX}")
