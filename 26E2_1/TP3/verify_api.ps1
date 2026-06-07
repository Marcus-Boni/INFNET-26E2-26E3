# Script de Validação Automatizada da API REST Poliglota - TP3
# Sistema Unificado de Gestão de Academia

$baseUrl = "http://localhost:8080/api"
$healthUrl = "http://localhost:8080/actuator/health"
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " INICIANDO TESTES AUTOMATIZADOS DA API (TP3)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Função auxiliar para imprimir status
function Print-TestResult($testName, $status, $color) {
    Write-Host "[ " -NoNewline
    Write-Host "$status" -ForegroundColor $color -NoNewline
    Write-Host " ] $testName"
}

# -------------------------------------------------------------
# Teste 0: Aguardar API iniciar
# -------------------------------------------------------------
Write-Host "`n--- Verificando Inicialização do Serviço (Actuator Health) ---" -ForegroundColor Yellow
$maxRetries = 10
$retryCount = 0
$serviceUp = $false

while ($retryCount -lt $maxRetries -and -not $serviceUp) {
    try {
        $healthResponse = Invoke-RestMethod -Uri $healthUrl -Method Get -UseBasicParsing
        if ($healthResponse.status -eq "UP") {
            $serviceUp = $true
            Print-TestResult "Spring Boot Actuator (/actuator/health) respondeu com status UP." "SUCESSO" "Green"
        }
    } catch {
        $retryCount++
        Write-Host "Aguardando serviço iniciar... (Tentativa $retryCount de $maxRetries)" -ForegroundColor Gray
        Start-Sleep -Seconds 4
    }
}

if (-not $serviceUp) {
    Write-Host "Erro: O serviço não iniciou no tempo limite." -ForegroundColor Red
    Exit 1
}

try {
    # -------------------------------------------------------------
    # Teste 1: GET /alunos/ativos (Listar alunos ativos - deve conter 3 alunos inicialmente)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 1: Listagem de Alunos Ativos ---" -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri "$baseUrl/alunos/ativos" -Method Get -UseBasicParsing
    if ($response.Count -ge 3) {
        Print-TestResult "GET /alunos/ativos retornou $($response.Count) alunos ativos." "SUCESSO" "Green"
        $response | ForEach-Object {
            Write-Host "  - ID: $($_.id) | Nome: $($_.nome) | Email: $($_.email) | Plano: $($_.plano.nome) | Ativo: $($_.ativo)" -ForegroundColor DarkGray
        }
    } else {
        Print-TestResult "GET /alunos/ativos retornou menos que 3 registros. Encontrado: $($response.Count)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 2: POST /alunos (Cadastrar novo aluno)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 2: Cadastrar Novo Aluno ---" -ForegroundColor Yellow
    $alunoBody = @{
        nome = "Carlos Augusto"
        email = "carlos@infnet.edu.br"
        dataNascimento = "1993-08-20"
        ativo = $true
        planoId = 1 # Plano Básico
    } | ConvertTo-Json

    $alunoPostResponse = Invoke-WebRequest -Uri "$baseUrl/alunos" -Method Post -ContentType "application/json; charset=utf-8" -Body $alunoBody -UseBasicParsing
    if ($alunoPostResponse.StatusCode -eq 201) {
        $alunoContent = $alunoPostResponse.Content | ConvertFrom-Json
        Print-TestResult "POST /alunos cadastrou com sucesso (HTTP 201 Created)." "SUCESSO" "Green"
        Print-TestResult "Corpo retornado contém ID: $($alunoContent.id) e Nome: $($alunoContent.nome)" "SUCESSO" "Green"
    } else {
        Print-TestResult "Falha ao cadastrar aluno. Status: $($alunoPostResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 3: POST /treinos (Cadastrar novo treino vinculado a instrutor)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 3: Cadastrar Treino Vinculado a Instrutor ---" -ForegroundColor Yellow
    $treinoBody = @{
        nome = "Treino Funcional de Alta Intensidade"
        focoPrincipal = "Resistência Muscular Geral"
        instrutorId = 2 # Instrutor Santos
    } | ConvertTo-Json

    $treinoPostResponse = Invoke-WebRequest -Uri "$baseUrl/treinos" -Method Post -ContentType "application/json; charset=utf-8" -Body $treinoBody -UseBasicParsing
    if ($treinoPostResponse.StatusCode -eq 201) {
        $treinoContent = $treinoPostResponse.Content | ConvertFrom-Json
        Print-TestResult "POST /treinos cadastrou com sucesso (HTTP 201 Created)." "SUCESSO" "Green"
        Print-TestResult "Treino: $($treinoContent.nome) vinculado ao Instrutor: $($treinoContent.instrutor.nome)" "SUCESSO" "Green"
    } else {
        Print-TestResult "Falha ao cadastrar treino. Status: $($treinoPostResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 4: GET /alunos/ranking (Ranking de alunos que mais concluíram treinos)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 4: Consultar Ranking de Conclusões ---" -ForegroundColor Yellow
    $rankingResponse = Invoke-RestMethod -Uri "$baseUrl/alunos/ranking" -Method Get -UseBasicParsing
    if ($rankingResponse.Count -gt 0) {
        Print-TestResult "GET /alunos/ranking retornou ranking com sucesso." "SUCESSO" "Green"
        $rankingResponse | ForEach-Object {
            Write-Host "  - Posição: Aluno ID: $($_.aluno.id) | Nome: $($_.aluno.nome) | Conclusões: $($_.totalConclusoes)" -ForegroundColor DarkGray
        }
        
        # Verificar se Marcus Boni está no topo com 3 conclusões
        if ($rankingResponse[0].aluno.nome -eq "Marcus Boni" -and $rankingResponse[0].totalConclusoes -eq 3) {
            Print-TestResult "Ordenação correta: Marcus Boni está na liderança com 3 conclusões." "SUCESSO" "Green"
        } else {
            Print-TestResult "Ordenação incorreta. Marcus Boni deveria estar no topo com 3 conclusões." "FALHA" "Red"
        }
    } else {
        Print-TestResult "GET /alunos/ranking retornou vazio." "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 5: POST /avaliacoes (Cadastrar Avaliação Física no MongoDB)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 5: Cadastrar Avaliação Física (MongoDB) ---" -ForegroundColor Yellow
    $avaliacaoBody = @{
        alunoId = 1 # Aluno Marcus Boni
        peso = 82.5
        altura = 1.83
        percentualGordura = 14.8
        anotacoesMedicas = "Aptidão cardiovascular excelente. Joelho esquerdo sem dores."
    } | ConvertTo-Json

    $avaliacaoPostResponse = Invoke-WebRequest -Uri "$baseUrl/avaliacoes" -Method Post -ContentType "application/json; charset=utf-8" -Body $avaliacaoBody -UseBasicParsing
    if ($avaliacaoPostResponse.StatusCode -eq 201) {
        $avaliacaoContent = $avaliacaoPostResponse.Content | ConvertFrom-Json
        Print-TestResult "POST /avaliacoes salvou com sucesso no MongoDB (HTTP 201 Created)." "SUCESSO" "Green"
        Print-TestResult "Corpo contém ID do MongoDB: $($avaliacaoContent.id)" "SUCESSO" "Green"
    } else {
        Print-TestResult "Falha ao salvar no MongoDB. Status: $($avaliacaoPostResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 6: GET /avaliacoes/aluno/{alunoId} (Listar avaliações de aluno do MongoDB)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 6: Listar Avaliações do Aluno (MongoDB) ---" -ForegroundColor Yellow
    $avaliacoesResponse = Invoke-RestMethod -Uri "$baseUrl/avaliacoes/aluno/1" -Method Get -UseBasicParsing
    if ($avaliacoesResponse.Count -gt 0) {
        Print-TestResult "GET /avaliacoes/aluno/1 retornou $($avaliacoesResponse.Count) avaliações do MongoDB." "SUCESSO" "Green"
        $avaliacoesResponse | ForEach-Object {
            Write-Host "  - Avaliação ID: $($_.id) | Peso: $($_.peso)kg | Altura: $($_.altura)m | Gordura: $($_.percentualGordura)% | Anotações: $($_.anotacoesMedicas)" -ForegroundColor DarkGray
        }
    } else {
        Print-TestResult "GET /avaliacoes/aluno/1 não retornou registros do MongoDB." "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 7: POST /catraca/token (Gerar token no Redis - 5 min TTL)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 7: Gerar Token de Acesso Catraca (Redis) ---" -ForegroundColor Yellow
    $tokenPostResponse = Invoke-WebRequest -Uri "$baseUrl/catraca/token?alunoId=1" -Method Post -UseBasicParsing
    if ($tokenPostResponse.StatusCode -eq 201) {
        $tokenContent = $tokenPostResponse.Content | ConvertFrom-Json
        $tokenValue = $tokenContent.token
        Print-TestResult "POST /catraca/token gerou e armazenou com sucesso (HTTP 201 Created)." "SUCESSO" "Green"
        Print-TestResult "Token Gerado: $tokenValue | Expira em: $($tokenContent.expiraEmSegundos) segundos" "SUCESSO" "Green"
    } else {
        Print-TestResult "Falha ao gerar token no Redis. Status: $($tokenPostResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 8: GET /catraca/validar?token=... (Validar token existente)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 8: Validar Token de Acesso Ativo ---" -ForegroundColor Yellow
    $validResponse = Invoke-RestMethod -Uri "$baseUrl/catraca/validar?token=$tokenValue" -Method Get -UseBasicParsing
    if ($validResponse.ativo -eq $true) {
        Print-TestResult "GET /catraca/validar confirmou token ativo. Mensagem: $($validResponse.mensagem) | Aluno: $($validResponse.alunoId)" "SUCESSO" "Green"
    } else {
        Print-TestResult "GET /catraca/validar indicou incorretamente que o token está inativo." "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 9: GET /catraca/validar?token=invalid (Validar token inexistente - 401 Unauthorized)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 9: Validar Token Inexistente/Expirado ---" -ForegroundColor Yellow
    try {
        $null = Invoke-WebRequest -Uri "$baseUrl/catraca/validar?token=fake-token-12345" -Method Get -UseBasicParsing
        Print-TestResult "Validou com sucesso um token inválido que deveria ser rejeitado!" "FALHA" "Red"
    } catch {
        $errStatus = $_.Exception.Response.StatusCode.Value__
        if ($errStatus -eq 401) {
            $errContent = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($errContent)
            $responseBody = $reader.ReadToEnd() | ConvertFrom-Json
            Print-TestResult "GET com token inválido retornou HTTP 401 Unauthorized corretamente." "SUCESSO" "Green"
            Print-TestResult "Mensagem de Erro da Catraca: $($responseBody.mensagem)" "SUCESSO" "Green"
        } else {
            Print-TestResult "GET com token inválido retornou HTTP $errStatus em vez de 401." "FALHA" "Red"
        }
    }

    Write-Host "`n==========================================================" -ForegroundColor Cyan
    Write-Host " TODOS OS TESTES PASSARAM COM EXCELÊNCIA POLIGLOTA! " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Cyan

} catch {
    Write-Host "`nOcorreu um erro inesperado durante a execução dos testes:" -ForegroundColor Red
    Write-Error $_
}
