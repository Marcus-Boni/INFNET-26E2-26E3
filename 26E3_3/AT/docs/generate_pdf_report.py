import fitz  # PyMuPDF
import os

def hex_to_rgb(hex_str):
    hex_str = hex_str.lstrip('#')
    return tuple(int(hex_str[i:i+2], 16) / 255.0 for i in (0, 2, 4))

COLOR_PRIMARY = hex_to_rgb("#0f172a")     # Slate 900
COLOR_SECONDARY = hex_to_rgb("#1e293b")   # Slate 800
COLOR_ACCENT = hex_to_rgb("#0284c7")      # Cyan 600
COLOR_TEAL = hex_to_rgb("#0f766e")        # Teal 700
COLOR_MUTED = hex_to_rgb("#64748b")       # Slate 500
COLOR_BG_CARD = hex_to_rgb("#f8fafc")     # Slate 50
COLOR_BORDER = hex_to_rgb("#cbd5e1")      # Slate 300
COLOR_TEXT_DARK = hex_to_rgb("#0f172a")
COLOR_GREEN = hex_to_rgb("#16a34a")

def draw_header_footer(page, page_num, total_pages):
    # Header
    page.draw_rect(fitz.Rect(40, 25, 555, 26), color=COLOR_BORDER, fill=COLOR_BORDER)
    page.insert_text(fitz.Point(40, 20), "INSTITUTO INFNET · MICROSSERVIÇOS COM SPRING CLOUD [26E3_3] · AT", 
                     fontsize=8, fontname="helv", color=COLOR_MUTED)
    
    # Footer
    page.draw_rect(fitz.Rect(40, 815, 555, 816), color=COLOR_BORDER, fill=COLOR_BORDER)
    page.insert_text(fitz.Point(40, 828), "Aluno: Marcus Evandro Galvão Boni (marcus.boni@al.infnet.edu.br)", 
                     fontsize=8, fontname="helv", color=COLOR_MUTED)
    page.insert_text(fitz.Point(495, 828), f"Página {page_num} de {total_pages}", 
                     fontsize=8, fontname="helv", color=COLOR_MUTED)

def create_report(output_pdf_path, evidencias_dir):
    doc = fitz.open()
    total_pages = 14
    
    # ----------------------------------------------------
    # PÁGINA 1: CAPA
    # ----------------------------------------------------
    p1 = doc.new_page(width=595, height=842)
    # Background banner
    p1.draw_rect(fitz.Rect(0, 0, 595, 220), color=COLOR_PRIMARY, fill=COLOR_PRIMARY)
    p1.draw_rect(fitz.Rect(0, 216, 595, 224), color=COLOR_ACCENT, fill=COLOR_ACCENT)
    
    # Header texts
    p1.insert_text(fitz.Point(50, 65), "INSTITUTO INFNET", fontsize=18, fontname="helv", color=(1, 1, 1))
    p1.insert_text(fitz.Point(50, 85), "Escola Superior de Tecnologia · Graduação em Engenharia de Software", 
                   fontsize=11, fontname="helv", color=(0.8, 0.85, 0.9))
    p1.insert_text(fitz.Point(50, 140), "Microsserviços e DevOps com Spring Boot e Spring Cloud", 
                   fontsize=15, fontname="helv", color=COLOR_ACCENT)
    p1.insert_text(fitz.Point(50, 175), "Assessment (AT) — fornecedores-service", 
                   fontsize=24, fontname="helv", color=(1, 1, 1))

    # Details Box
    box_rect = fitz.Rect(50, 270, 545, 720)
    p1.draw_rect(box_rect, color=COLOR_BORDER, fill=COLOR_BG_CARD)
    
    p1.insert_text(fitz.Point(75, 310), "IDENTIFICAÇÃO DO PROJETO E DO ALUNO", fontsize=12, fontname="helv", color=COLOR_ACCENT)
    p1.draw_line(fitz.Point(75, 320), fitz.Point(520, 320), color=COLOR_BORDER)
    
    info_items = [
        ("Aluno:", "Marcus Evandro Galvão Boni"),
        ("E-mail Institucional:", "marcus.boni@al.infnet.edu.br"),
        ("Disciplina:", "Microsserviços e DevOps com Spring Boot e Spring Cloud [26E3_3]"),
        ("Curso:", "Engenharia de Software (Live)"),
        ("Professor Responsável:", "Bruno Bastos"),
        ("Repositório Fork GitHub:", "https://github.com/Marcus-Boni/api-vendas"),
        ("Pull Request Aberto:", "https://github.com/Marcus-Boni/api-vendas/pull/1 (atividade-marcusboni -> main)"),
        ("Branch de Desenvolvimento:", "atividade-marcusboni"),
        ("Stack Tecnológica:", "Java 21 LTS · Spring Boot 3.2.5 · Spring Cloud 2023.0.1"),
        ("Componentes Centrais:", "Netflix Eureka · Spring Cloud Config Server · Spring Cloud Gateway"),
        ("Integração e Persistência:", "OpenFeign · Spring Data JPA · H2 Database · Docker Compose"),
        ("Automação / CI-CD:", "GitHub Actions (.github/workflows/fornecedores-service.yml)"),
        ("Data de Conclusão:", "Outubro de 2026")
    ]
    
    y = 350
    for label, val in info_items:
        p1.insert_text(fitz.Point(75, y), label, fontsize=10, fontname="helv", color=COLOR_MUTED)
        p1.insert_text(fitz.Point(235, y), val, fontsize=10, fontname="helv", color=COLOR_TEXT_DARK)
        y += 26
        
    p1.insert_text(fitz.Point(50, 770), "Relatório Técnico de Execução dos 12 Exercícios Práticos com Evidências de Teste", 
                   fontsize=9, fontname="helv", color=COLOR_MUTED)

    # ----------------------------------------------------
    # PÁGINA 2: VISÃO GERAL DA ARQUITETURA
    # ----------------------------------------------------
    p2 = doc.new_page(width=595, height=842)
    draw_header_footer(p2, 2, total_pages)
    
    p2.insert_text(fitz.Point(40, 60), "1. Visão Geral da Arquitetura de Microsserviços", fontsize=16, fontname="helv", color=COLOR_PRIMARY)
    p2.draw_line(fitz.Point(40, 68), fitz.Point(555, 68), color=COLOR_ACCENT)
    
    p2.insert_text(fitz.Point(40, 90), 
        "A solução integra o novo microsserviço fornecedores-service a uma arquitetura distribuída cloud-native\n"
        "composta por descoberta dinâmica de serviços, configuração centralizada, roteamento unificado de borda,\n"
        "persistência relacional em memória e comunicação síncrona declarativa inter-serviços.",
        fontsize=9.5, fontname="helv", color=COLOR_SECONDARY)
    
    # Table of services
    services_data = [
        ("eureka-server", "8761", "Netflix Eureka Service Discovery", "Registro e resolução dinâmica de nomes lógicos"),
        ("config-server", "8888", "Spring Cloud Config Server", "Gestão externa de propriedades centralizada (config-repo)"),
        ("gateway", "8085", "Spring Cloud Gateway (WebFlux)", "Ponto de entrada único e roteamento reativo por nome lógico"),
        ("produtos-service", "8081", "Spring Boot Data JPA (H2)", "Catálogo de produtos tecnológicos consultado via Feign"),
        ("vendas-service", "8082", "Spring Boot Data JPA (H2)", "Gestão e registro de transações de vendas"),
        ("clientes-service", "8083", "Spring Boot Data JPA (H2)", "Gestão cadastral de clientes (serviço base gabarito)"),
        ("fornecedores-service", "8084", "Spring Boot + OpenFeign (H2)", "Novo microsserviço: gestão de fornecedores e integração Feign")
    ]
    
    y = 150
    p2.draw_rect(fitz.Rect(40, y, 555, y+24), color=COLOR_PRIMARY, fill=COLOR_PRIMARY)
    p2.insert_text(fitz.Point(45, y+16), "Microsserviço", fontsize=9, fontname="helv", color=(1,1,1))
    p2.insert_text(fitz.Point(165, y+16), "Porta", fontsize=9, fontname="helv", color=(1,1,1))
    p2.insert_text(fitz.Point(215, y+16), "Tecnologia / Stack", fontsize=9, fontname="helv", color=(1,1,1))
    p2.insert_text(fitz.Point(375, y+16), "Responsabilidade", fontsize=9, fontname="helv", color=(1,1,1))
    y += 24
    
    for s_name, s_port, s_tech, s_resp in services_data:
        fill_col = COLOR_BG_CARD if (services_data.index((s_name, s_port, s_tech, s_resp)) % 2 == 0) else (1,1,1)
        p2.draw_rect(fitz.Rect(40, y, 555, y+22), color=COLOR_BORDER, fill=fill_col)
        is_new = (s_name == "fornecedores-service")
        name_col = COLOR_ACCENT if is_new else COLOR_TEXT_DARK
        p2.insert_text(fitz.Point(45, y+15), s_name, fontsize=8.5, fontname="helv", color=name_col)
        p2.insert_text(fitz.Point(165, y+15), s_port, fontsize=8.5, fontname="helv", color=COLOR_TEXT_DARK)
        p2.insert_text(fitz.Point(215, y+15), s_tech, fontsize=8, fontname="helv", color=COLOR_MUTED)
        p2.insert_text(fitz.Point(375, y+15), s_resp, fontsize=7.5, fontname="helv", color=COLOR_TEXT_DARK)
        y += 22

    # Map of exercises
    y += 20
    p2.insert_text(fitz.Point(40, y), "2. Resumo das Atividades Executadas (Exercícios 1 a 12)", fontsize=13, fontname="helv", color=COLOR_PRIMARY)
    p2.draw_line(fitz.Point(40, y+6), fitz.Point(555, y+6), color=COLOR_ACCENT)
    y += 20
    
    ex_summary = [
        ("Ex 1", "Fork e Clone do Projeto", "Fork para Marcus-Boni/api-vendas e validação do Eureka com produtos-service"),
        ("Ex 2", "Branch e Pull Request", "Branch atividade-marcusboni, identificação no readme.md e PR #1 aberto para a main"),
        ("Ex 3", "Criação do fornecedores-service", "Novo microsserviço criado com artifactId, pacote próprio e escutando na porta 8084"),
        ("Ex 4", "Entidade Fornecedor e H2", "Modelo com CNPJ único e obrigatório, repositório JPA e seed de 5 registros"),
        ("Ex 5", "Camada REST (Listagem e 404)", "Endpoints GET /fornecedores (200 OK) e GET /fornecedores/{id} com 404 Not Found"),
        ("Ex 6", "Registro no Eureka Server", "Configuração do cliente Eureka e validação do registro do FORNECEDORES-SERVICE"),
        ("Ex 7", "Configuração Centralizada", "Propriedades em config-repo e consumo dinâmico via Spring Cloud Config Server"),
        ("Ex 8", "Roteamento pelo Gateway", "Acesso aos fornecedores passando pela porta unificada 8085 do API Gateway"),
        ("Ex 9", "Criação via POST /fornecedores", "Endpoint de cadastro recebendo JSON e retornando HTTP 201 Created com o ID gerado"),
        ("Ex 10", "Integração Feign com Produtos", "Consumo de produtos-service via OpenFeign e endpoint GET /fornecedores/produtos"),
        ("Ex 11", "Containerização Docker Compose", "Dockerfile multi-stage, profile docker e orquestração de todos os containers"),
        ("Ex 12", "Automação CI com GitHub Actions", "Pipeline de compilação Maven com Java 17 executado com sucesso e check verde")
    ]
    
    for ex_code, ex_title, ex_desc in ex_summary:
        p2.draw_rect(fitz.Rect(40, y, 90, y+18), color=COLOR_BORDER, fill=COLOR_BG_CARD)
        p2.insert_text(fitz.Point(46, y+13), ex_code, fontsize=8, fontname="helv", color=COLOR_ACCENT)
        p2.insert_text(fitz.Point(100, y+13), ex_title + ":", fontsize=8.5, fontname="helv", color=COLOR_TEXT_DARK)
        p2.insert_text(fitz.Point(260, y+13), ex_desc, fontsize=8, fontname="helv", color=COLOR_MUTED)
        y += 20

    # ----------------------------------------------------
    # HELPER PARA PÁGINAS DE EXERCÍCIO
    # ----------------------------------------------------
    def add_exercise_page(p_num, ex_title, ex_enunciado, ex_tecnica, img_name, img_caption, extra_text=""):
        page = doc.new_page(width=595, height=842)
        draw_header_footer(page, p_num, total_pages)
        
        # Title
        page.insert_text(fitz.Point(40, 58), ex_title, fontsize=15, fontname="helv", color=COLOR_PRIMARY)
        page.draw_line(fitz.Point(40, 66), fitz.Point(555, 66), color=COLOR_ACCENT)
        
        # Enunciado box
        page.draw_rect(fitz.Rect(40, 78, 555, 126), color=COLOR_BORDER, fill=COLOR_BG_CARD)
        page.insert_text(fitz.Point(48, 92), "ENUNCIADO:", fontsize=8, fontname="helv", color=COLOR_ACCENT)
        
        # Wrap enunciado text
        words = ex_enunciado.split()
        lines = []
        cur = ""
        for w in words:
            if len(cur + " " + w) < 115:
                cur += (" " if cur else "") + w
            else:
                lines.append(cur)
                cur = w
        if cur:
            lines.append(cur)
        ey = 104
        for l in lines[:2]:
            page.insert_text(fitz.Point(48, ey), l, fontsize=8, fontname="helv", color=COLOR_TEXT_DARK)
            ey += 11
            
        # Explicação Técnica
        page.insert_text(fitz.Point(40, 142), "Detalhamento Técnico da Solução:", fontsize=10.5, fontname="helv", color=COLOR_PRIMARY)
        
        # Wrap técnica
        t_words = ex_tecnica.split()
        t_lines = []
        t_cur = ""
        for w in t_words:
            if len(t_cur + " " + w) < 118:
                t_cur += (" " if t_cur else "") + w
            else:
                t_lines.append(t_cur)
                t_cur = w
        if t_cur:
            t_lines.append(t_cur)
            
        ty = 156
        for l in t_lines[:4]:
            page.insert_text(fitz.Point(40, ty), l, fontsize=8.5, fontname="helv", color=COLOR_SECONDARY)
            ty += 12
            
        # Image
        img_path = os.path.join(evidencias_dir, img_name)
        if os.path.exists(img_path):
            img_box = fitz.Rect(40, 210, 555, 730)
            page.draw_rect(img_box, color=COLOR_BORDER, fill=(0.98, 0.98, 0.98))
            
            # Place image maintaining aspect ratio
            img_doc = fitz.open(img_path)
            rect_target = fitz.Rect(45, 215, 550, 725)
            page.insert_image(rect_target, filename=img_path, keep_proportion=True)
            
        # Image caption
        page.draw_rect(fitz.Rect(40, 738, 555, 762), color=COLOR_BORDER, fill=COLOR_BG_CARD)
        page.insert_text(fitz.Point(48, 753), f"Evidência: {img_caption}", fontsize=8.5, fontname="helv", color=COLOR_TEAL)
        
        if extra_text:
            page.insert_text(fitz.Point(40, 780), extra_text, fontsize=8, fontname="helv", color=COLOR_MUTED)

    # ----------------------------------------------------
    # PÁGINA 3: EXERCÍCIO 1
    # ----------------------------------------------------
    add_exercise_page(
        3,
        "Exercício 1 — Fork do Projeto e Registro no Eureka",
        "Faça um fork do repositório do projeto para a sua conta do GitHub, clone o seu fork na sua máquina e suba o eureka-server e depois o produtos-service. Para testar, abra o endereço localhost:8761 e veja o produtos-service na lista de serviços registrados. Entregue um print do painel do Eureka com o serviço registrado.",
        "Foi realizado o fork do repositório base para a conta GitHub Marcus-Boni/api-vendas e efetuado o clone local. O eureka-server foi inicializado na porta padrão 8761 e, em seguida, o produtos-service foi compilado e iniciado, realizando o registro automático como cliente Eureka via Spring Cloud Netflix Eureka Client.",
        "01-eureka-produtos.png",
        "Painel do Eureka Server (localhost:8761) com a instância do PRODUTOS-SERVICE registrada e em estado UP.",
        "Status: Registro validado com sucesso através do heartbeat automático em intervalo de 30 segundos."
    )

    # ----------------------------------------------------
    # PÁGINA 4: EXERCÍCIO 2
    # ----------------------------------------------------
    add_exercise_page(
        4,
        "Exercício 2 — Branch de Trabalho e Pull Request",
        "Crie uma branch chamada atividade-seunome. Edite o readme.md acrescentando uma linha com o seu nome completo e a sua matrícula. Faça o commit, envie a branch para o GitHub e abra um Pull Request dessa branch para a main do seu próprio fork. A partir daqui, todos os commits da atividade vão nessa mesma branch. Entregue um print da tela do Pull Request aberto.",
        "Foi criada a branch isolada 'atividade-marcusboni' a partir da main. O arquivo readme.md foi editado incluindo a identificação do aluno Marcus Evandro Galvão Boni. O commit foi enviado para o repositório remoto e foi aberto o Pull Request #1 no GitHub direcionado à branch main.",
        "02-pull-request-aberto.png",
        "Pull Request #1 aberto no GitHub (Marcus-Boni/api-vendas) comparando atividade-marcusboni com main.",
        "Pull Request URL: https://github.com/Marcus-Boni/api-vendas/pull/1 · Branch: atividade-marcusboni"
    )

    # ----------------------------------------------------
    # PÁGINA 5: EXERCÍCIO 3
    # ----------------------------------------------------
    add_exercise_page(
        5,
        "Exercício 3 — Criação do fornecedores-service (Porta 8084)",
        "Crie o novo microsserviço a partir de uma cópia do clientes-service, ajustando o artifactId e o name no pom.xml, o nome do pacote, o nome da classe principal, o spring.application.name e a porta, que deve ser a 8084. Para testar, a aplicação precisa subir sem erro e ocupar a porta 8084. Entregue um print do terminal mostrando a aplicação iniciada.",
        "O fornecedores-service foi estruturado com o pacote com.exemplo.fornecedoresservice, classe principal FornecedoresServiceApplication, artifactId e name 'fornecedores-service'. A aplicação inicializou o servidor Tomcat embutido na porta 8084 com Spring Boot 3.2.5 e Java 21 LTS.",
        "03-terminal-fornecedores-8084.png",
        "Terminal do PowerShell exibindo a inicialização do fornecedores-service e o Tomcat ouvindo na porta 8084.",
        "Log: 'Tomcat started on port 8084 (http) with context path ''' e 'Started FornecedoresServiceApplication'."
    )

    # ----------------------------------------------------
    # PÁGINA 6: EXERCÍCIO 4
    # ----------------------------------------------------
    add_exercise_page(
        6,
        "Exercício 4 — Entidade Fornecedor, Repositório e H2 Console",
        "Seguindo o modelo da entidade Cliente, crie a entidade Fornecedor com id gerado automaticamente, nome obrigatório e CNPJ obrigatório e único. Crie também o repositório correspondente e uma classe que cadastre cinco fornecedores automaticamente quando a aplicação subir. Para testar, consulte a tabela pelo console do H2. Entregue um print do H2 Console com os cinco registros.",
        "A entidade Fornecedor foi mapeada com JPA (@Id, @GeneratedValue IDENTITY, @Column nullable=false para nome e unique=true para CNPJ). Foi criado o FornecedorRepository estendendo JpaRepository e a classe DataInitializer (CommandLineRunner) para popular automaticamente cinco fornecedores corporativos ao subir.",
        "04-h2-console-fornecedores.png",
        "H2 Web Console consultando 'SELECT * FROM FORNECEDOR' exibindo os 5 fornecedores cadastrados.",
        "Database URL: jdbc:h2:mem:fornecedoresdb · Tabela: FORNECEDOR (Campos: ID, NOME, CNPJ)."
    )

    # ----------------------------------------------------
    # PÁGINA 7: EXERCÍCIO 5
    # ----------------------------------------------------
    add_exercise_page(
        7,
        "Exercício 5 — Endpoints REST: GET /fornecedores e 404 Not Found",
        "Crie a camada de serviço e o controller com dois endpoints: O GET /fornecedores lista todos os fornecedores. O GET /fornecedores/{id} devolve o fornecedor, ou o status 404 quando o id não existir. O controller do produtos-service já resolve exatamente esse caso do 404, use como referência. Entregue um print das duas respostas, e a segunda precisa mostrar o código 404.",
        "Foi implementado o FornecedorService com métodos de negócio e o FornecedorController com mapeamento REST. O endpoint GET /fornecedores devolve HTTP 200 com a lista de fornecedores. O endpoint GET /fornecedores/{id} utiliza Optional com map e orElseGet(() -> ResponseEntity.notFound().build()) devolvendo HTTP 404.",
        "05-get-fornecedores-404.png",
        "Resposta HTTP 404 Not Found ao consultar id inexistente (/fornecedores/999) conforme padrão de projeto.",
        "Complementar: A listagem geral GET /fornecedores retorna status HTTP 200 OK com o payload JSON completo."
    )

    # ----------------------------------------------------
    # PÁGINA 8: EXERCÍCIO 6
    # ----------------------------------------------------
    add_exercise_page(
        8,
        "Exercício 6 — Registro do FORNECEDORES-SERVICE no Eureka",
        "Faça o seu serviço se registrar no Eureka, como os outros já fazem. Confira a dependência no pom.xml e as propriedades necessárias. Para testar, com o Eureka no ar, suba o serviço e recarregue o endereço localhost:8761. Entregue um print do Eureka com o FORNECEDORES-SERVICE na lista.",
        "Foi adicionada a dependência spring-cloud-starter-netflix-eureka-client e a anotação @EnableDiscoveryClient. A propriedade eureka.client.service-url.defaultZone foi configurada apontando para http://localhost:8761/eureka/, registrando a aplicação com o Service ID FORNECEDORES-SERVICE na porta 8084.",
        "06-eureka-fornecedores.png",
        "Dashboard do Eureka Server (localhost:8761) com FORNECEDORES-SERVICE e PRODUTOS-SERVICE ativos.",
        "Service Discovery: Registro automático validado com status UP e lease renewal ativo a cada 30 segundos."
    )

    # ----------------------------------------------------
    # PÁGINA 9: EXERCÍCIO 7
    # ----------------------------------------------------
    add_exercise_page(
        9,
        "Exercício 7 — Configuração Externa via Spring Cloud Config Server",
        "Crie o arquivo de configuração do seu serviço dentro da pasta config-repo, com a porta, o banco H2 e o Eureka. Deixe no application.properties do serviço apenas o nome da aplicação e o endereço do config-server. Suba o config-server antes do seu serviço. Para testar, peça a configuração direto ao config-server pelo navegador e veja as suas propriedades. Entregue um print dessa resposta e um print do serviço subindo com a porta vinda do Config Server.",
        "Foi criado o arquivo config-repo/fornecedores-service.properties contendo server.port=8084, configurações do H2 e do Eureka. O config-server foi configurado com o profile native e o fornecedores-service configurado com spring.config.import=optional:configserver:http://localhost:8888, consumindo suas propriedades remotamente.",
        "07-config-server-resposta.png",
        "Resposta JSON do Config Server (http://localhost:8888/fornecedores-service/default) fornecendo as propriedades.",
        "Origem: PropertySource 'file:../config-repo/fornecedores-service.properties' contendo a porta 8084 e credenciais H2."
    )

    # ----------------------------------------------------
    # PÁGINA 10: EXERCÍCIO 8
    # ----------------------------------------------------
    add_exercise_page(
        10,
        "Exercício 8 — Roteamento Unificado via API Gateway (Porta 8085)",
        "Com o eureka, o config-server, o gateway e o seu serviço no ar, acesse a lista de fornecedores passando pelo gateway, na porta 8085, e não mais pela porta 8084. O gateway descobre os serviços pelo nome registrado no Eureka, então você não precisa escrever nenhuma rota. Entregue um print da resposta vinda da porta 8085.",
        "O Spring Cloud Gateway (porta 8085) utiliza o discovery locator dinâmico reativo (discovery.locator.enabled=true e lower-case-service-id=true). Ao receber a requisição em http://localhost:8085/fornecedores-service/fornecedores, o Gateway consulta o Eureka, resolve o IP/porta do fornecedores-service e roteia o tráfego com HTTP 200.",
        "08-gateway-fornecedores.png",
        "Acesso à lista de fornecedores via API Gateway na porta 8085 (/fornecedores-service/fornecedores).",
        "Padrão Gateway: Ponto único de entrada desacoplado, sem expor as portas internas dos microsserviços aos clientes."
    )

    # ----------------------------------------------------
    # PÁGINA 11: EXERCÍCIO 9
    # ----------------------------------------------------
    add_exercise_page(
        11,
        "Exercício 9 — Cadastro via POST /fornecedores (HTTP 201 Created)",
        "Acrescente o endpoint POST /fornecedores, que recebe um fornecedor em JSON, salva no banco e devolve o status 201 junto com o objeto criado. Para testar, envie um novo fornecedor e confirme que ele aparece no GET /fornecedores. Entregue um print mostrando o 201 e o id do novo registro.",
        "Foi implementado o método criar(@RequestBody Fornecedor fornecedor) no controller, anotado com @PostMapping. O método garante id nulo antes de salvar via repositório e retorna ResponseEntity.status(HttpStatus.CREATED).body(salvo), gerando o status HTTP 201 e o ID sequencial atribuído pelo H2.",
        "09-post-fornecedor-201.png",
        "Requisição POST /fornecedores com retorno de HTTP 201 Created e o objeto persistido com o ID gerado.",
        "Persistência validada: O novo fornecedor foi salvo no H2 Database e passa a constar na listagem geral."
    )

    # ----------------------------------------------------
    # PÁGINA 12: EXERCÍCIO 10
    # ----------------------------------------------------
    add_exercise_page(
        12,
        "Exercício 10 — Comunicação Inter-Serviços com Spring Cloud OpenFeign",
        "Faça o seu serviço consultar o produtos-service usando Feign. Adicione a dependência do OpenFeign, habilite os clientes Feign na aplicação, crie a interface do cliente e o DTO do produto, e crie o endpoint GET /fornecedores/produtos, que devolve a lista de produtos obtida do outro serviço. Entregue um print dos produtos aparecendo na resposta do seu serviço.",
        "Foi adicionada a dependência spring-cloud-starter-openfeign e habilitado o @EnableFeignClients. Foi criada a interface @FeignClient(name = 'produtos-service') com o método listarProdutos() e o ProdutoDTO. O endpoint GET /fornecedores/produtos invoca o cliente Feign, que resolve o produtos-service via Eureka e retorna a lista de produtos.",
        "10-feign-produtos.png",
        "Resposta de GET /fornecedores/produtos consumindo produtos-service via Feign e retornando o catálogo.",
        "Integração síncrona: Comunicação cliente declarativa desacoplada de host/porta físicos com balanceamento de carga."
    )

    # ----------------------------------------------------
    # PÁGINA 13: EXERCÍCIO 11
    # ----------------------------------------------------
    add_exercise_page(
        13,
        "Exercício 11 — Containerização Multi-Stage com Docker Compose",
        "Crie o Dockerfile do seu serviço, no mesmo modelo dos outros, ajustando a porta. Crie o arquivo de configuração do perfil docker na pasta config-repo, apontando o Eureka para o nome do serviço na rede do Docker. Acrescente o seu serviço ao docker-compose.yml, seguindo o bloco de um serviço que já existe. Para testar, suba tudo com um único docker compose up --build e acesse os fornecedores pelo gateway. Entregue um print dos containers rodando e um print da resposta pelo gateway.",
        "Foi elaborado o Dockerfile multi-stage (build com Maven 3.9 e runtime com Eclipse Temurin JRE 17) expondo a porta 8084. Criou-se o profile fornecedores-service-docker.properties no config-repo com o host do Eureka na rede virtual microservicos-net e o bloco fornecedores-service foi adicionado ao docker-compose.yml.",
        "11-docker-compose-ps.png",
        "Execução de 'docker compose up --build' e 'docker compose ps' com todos os 7 containers rodando.",
        "Rede Virtual: Comunicação interna entre containers via nomes de serviço (eureka-server, config-server, etc.)."
    )

    # ----------------------------------------------------
    # PÁGINA 14: EXERCÍCIO 12
    # ----------------------------------------------------
    add_exercise_page(
        14,
        "Exercício 12 — Pipeline de Integração Contínua com GitHub Actions",
        "Crie um workflow do GitHub Actions no seu repositório, dentro da pasta .github/workflows, que a cada push baixe o código do repositório, compile o seu fornecedores-service com o Maven. Faça o commit e o push do arquivo, que o GitHub executa o pipeline sozinho. Para testar, abra a aba Actions do seu repositório e acompanhe a execução. Entregue um print do pipeline concluído com o check verde.",
        "Foi configurado o workflow .github/workflows/fornecedores-service.yml disparado em eventos de push e pull_request. O pipeline executa em runner Ubuntu mais recente, faz checkout do código, configura o JDK 17 com cache do Maven e compila o fornecedores-service através de 'mvn clean package -DskipTests', completando com sucesso em 46s (job em 42s).",
        "12-github-actions-green.png",
        "Pipeline do GitHub Actions no repositório Marcus-Boni/INFNET-26E2-26E3 concluído com o check verde de sucesso.",
        "Run ID: 37389384525 · Job: Build fornecedores-service · Duração: 46 segundos · Status: SUCCESS"
    )

    doc.save(output_pdf_path)
    doc.close()
    print(f"Relatório gerado com sucesso em: {output_pdf_path}")

if __name__ == "__main__":
    base_dir = r"c:\Users\mgalv\Projetos-Programacao\Projetos-Faculdade\Engenharia-Disciplinada-Periodo7\INFNET-26E2-26E3\26E3_3\AT"
    evidencias = os.path.join(base_dir, "docs", "evidencias")
    out_pdf = os.path.join(base_dir, "docs", "Marcus_Boni_DR3_AT.pdf")
    create_report(out_pdf, evidencias)
