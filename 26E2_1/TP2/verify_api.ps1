# Script de Validação Automatizada da API REST - TP2
# Sistema de Reserva de Passagens de Ônibus

$baseUrl = "http://localhost:8080/passagens"
$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " INICIANDO TESTES AUTOMATIZADOS DA API (TP2)" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Função auxiliar para imprimir status
function Print-TestResult($testName, $status, $color) {
    Write-Host "[ " -NoNewline
    Write-Host "$status" -ForegroundColor $color -NoNewline
    Write-Host " ] $testName"
}

try {
    # -------------------------------------------------------------
    # Teste 1: GET /passagens (Listar inicial - deve conter 3 passagens)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 1: Listagem Inicial ---" -ForegroundColor Yellow
    $response = Invoke-RestMethod -Uri $baseUrl -Method Get -UseBasicParsing
    if ($response.Count -eq 3) {
        Print-TestResult "GET /passagens retornou exatamente 3 registros iniciais." "SUCESSO" "Green"
        $response | ForEach-Object {
            Write-Host "  - ID: $($_.id) | Passageiro: $($_.passageiro) | Assento: $($_.assento) | Destino: $($_.destino) | Status: $($_.status)" -ForegroundColor DarkGray
        }
    } else {
        Print-TestResult "GET /passagens não retornou a quantidade esperada. Encontrado: $($response.Count)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 2: POST /passagens (Criar passagem com sucesso)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 2: Criar Passagem com Sucesso ---" -ForegroundColor Yellow
    $body = @{
        passageiro = "Juliana Costa"
        assento = 20
        origem = "Rio de Janeiro"
        destino = "Brasília"
        data = "2026-06-01"
        status = "CONFIRMADA"
    } | ConvertTo-Json

    $postResponse = Invoke-WebRequest -Uri $baseUrl -Method Post -ContentType "application/json; charset=utf-8" -Body $body -UseBasicParsing
    if ($postResponse.StatusCode -eq 201) {
        $content = $postResponse.Content | ConvertFrom-Json
        Print-TestResult "POST /passagens retornou HTTP 201 Created." "SUCESSO" "Green"
        Print-TestResult "Corpo do retorno contém ID: $($content.id) e Passageiro: $($content.passageiro)" "SUCESSO" "Green"
    } else {
        Print-TestResult "Falha ao criar passagem. Status: $($postResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 3: POST /passagens (Verificar assento duplicado - 400 Bad Request)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 3: Validar Assento Duplicado ---" -ForegroundColor Yellow
    $duplicateBody = @{
        passageiro = "Felipe Ramos"
        assento = 20  # Mesmo assento criado no teste anterior
        origem = "Curitiba"
        destino = "Florianópolis"
        data = "2026-06-05"
        status = "PENDENTE"
    } | ConvertTo-Json

    try {
        $dupResponse = Invoke-WebRequest -Uri $baseUrl -Method Post -ContentType "application/json; charset=utf-8" -Body $duplicateBody -UseBasicParsing
        Print-TestResult "Permitiu criar passagem com assento duplicado!" "FALHA" "Red"
    } catch {
        $errStatus = $_.Exception.Response.StatusCode.Value__
        if ($errStatus -eq 400) {
            Print-TestResult "POST com assento duplicado lançou HTTP 400 Bad Request corretamente." "SUCESSO" "Green"
        } else {
            Print-TestResult "POST com assento duplicado retornou HTTP $errStatus em vez de 400." "FALHA" "Red"
        }
    }

    # -------------------------------------------------------------
    # Teste 4: GET /passagens/{id} (Buscar passagem existente)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 4: Buscar Passagem por ID Existente ---" -ForegroundColor Yellow
    $getByIdResponse = Invoke-RestMethod -Uri "$baseUrl/1" -Method Get -UseBasicParsing
    if ($getByIdResponse.id -eq 1 -and $getByIdResponse.passageiro -eq "Carlos Silva") {
        Print-TestResult "GET /passagens/1 localizou corretamente a passagem do Carlos Silva." "SUCESSO" "Green"
    } else {
        Print-TestResult "GET /passagens/1 retornou dados incorretos ou falhou." "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 5: GET /passagens/{id} (Buscar ID não existente - 404 Not Found)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 5: Buscar Passagem por ID Inexistente ---" -ForegroundColor Yellow
    try {
        $null = Invoke-WebRequest -Uri "$baseUrl/99" -Method Get -UseBasicParsing
        Print-TestResult "Localizou ID 99 que não deveria existir!" "FALHA" "Red"
    } catch {
        $errStatus = $_.Exception.Response.StatusCode.Value__
        if ($errStatus -eq 404) {
            Print-TestResult "GET /passagens/99 lançou HTTP 404 Not Found corretamente." "SUCESSO" "Green"
        } else {
            Print-TestResult "GET /passagens/99 retornou HTTP $errStatus em vez de 404." "FALHA" "Red"
        }
    }

    # -------------------------------------------------------------
    # Teste 6: PUT /passagens/{id} (Atualizar passagem existente)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 6: Atualizar Passagem por ID ---" -ForegroundColor Yellow
    $updateBody = @{
        passageiro = "Carlos Silva Atualizado"
        assento = 5
        origem = "Rio de Janeiro"
        destino = "São Paulo"
        data = "2026-06-10"
        status = "CONFIRMADA"
    } | ConvertTo-Json

    $putResponse = Invoke-WebRequest -Uri "$baseUrl/1" -Method Put -ContentType "application/json; charset=utf-8" -Body $updateBody -UseBasicParsing
    if ($putResponse.StatusCode -eq 200) {
        $content = $putResponse.Content | ConvertFrom-Json
        if ($content.passageiro -eq "Carlos Silva Atualizado") {
            Print-TestResult "PUT /passagens/1 atualizou o nome do passageiro com sucesso para 'Carlos Silva Atualizado'." "SUCESSO" "Green"
        } else {
            Print-TestResult "PUT /passagens/1 retornou sucesso, mas o nome não foi alterado no JSON de retorno." "FALHA" "Red"
        }
    } else {
        Print-TestResult "PUT /passagens/1 falhou com HTTP status: $($putResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 7: PUT /passagens/{id} (Atualizar passagem com ID inexistente - 404 Not Found)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 7: Atualizar Passagem Inexistente ---" -ForegroundColor Yellow
    try {
        $null = Invoke-WebRequest -Uri "$baseUrl/99" -Method Put -ContentType "application/json; charset=utf-8" -Body $updateBody -UseBasicParsing
        Print-TestResult "Permitiu atualizar ID 99 que não existe!" "FALHA" "Red"
    } catch {
        $errStatus = $_.Exception.Response.StatusCode.Value__
        if ($errStatus -eq 404) {
            Print-TestResult "PUT /passagens/99 lançou HTTP 404 Not Found corretamente." "SUCESSO" "Green"
        } else {
            Print-TestResult "PUT /passagens/99 retornou HTTP $errStatus em vez de 404." "FALHA" "Red"
        }
    }

    # -------------------------------------------------------------
    # Teste 8: GET /passagens/busca (Filtrar por destino)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 8: Buscar por Destino ---" -ForegroundColor Yellow
    $buscaResponse = Invoke-RestMethod -Uri "$baseUrl/busca?destino=belo%20horizonte" -Method Get -UseBasicParsing
    if ($buscaResponse.Count -gt 0 -and $buscaResponse[0].destino -eq "Belo Horizonte") {
        Print-TestResult "GET /passagens/busca?destino=belo horizonte retornou a passagem esperada ignorando casing." "SUCESSO" "Green"
        Write-Host "  - Localizado: $($buscaResponse[0].passageiro) viajando para $($buscaResponse[0].destino)" -ForegroundColor DarkGray
    } else {
        Print-TestResult "Busca por destino falhou ou não encontrou os registros." "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 9: DELETE /passagens/{id} (Remover passagem existente)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 9: Remover Passagem Existente ---" -ForegroundColor Yellow
    $delResponse = Invoke-WebRequest -Uri "$baseUrl/1" -Method Delete -UseBasicParsing
    if ($delResponse.StatusCode -eq 204) {
        Print-TestResult "DELETE /passagens/1 retornou HTTP 204 No Content corretamente." "SUCESSO" "Green"
    } else {
        Print-TestResult "DELETE /passagens/1 falhou com HTTP status: $($delResponse.StatusCode)" "FALHA" "Red"
    }

    # -------------------------------------------------------------
    # Teste 10: GET /passagens/{id} (Buscar passagem deletada - 404 Not Found)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 10: Buscar Passagem Deletada ---" -ForegroundColor Yellow
    try {
        $null = Invoke-WebRequest -Uri "$baseUrl/1" -Method Get -UseBasicParsing
        Print-TestResult "Ainda localizou o ID 1 após a deleção!" "FALHA" "Red"
    } catch {
        $errStatus = $_.Exception.Response.StatusCode.Value__
        if ($errStatus -eq 404) {
            Print-TestResult "GET /passagens/1 lançou HTTP 404 Not Found após deletado." "SUCESSO" "Green"
        } else {
            Print-TestResult "GET /passagens/1 retornou HTTP $errStatus em vez de 404." "FALHA" "Red"
        }
    }

    # -------------------------------------------------------------
    # Teste 11: DELETE /passagens/{id} (Deletar ID inexistente - 404 Not Found)
    # -------------------------------------------------------------
    Write-Host "`n--- Executando Teste 11: Deletar Passagem Inexistente ---" -ForegroundColor Yellow
    try {
        $null = Invoke-WebRequest -Uri "$baseUrl/99" -Method Delete -UseBasicParsing
        Print-TestResult "Permitiu deletar ID 99 sem lançar erro!" "FALHA" "Red"
    } catch {
        $errStatus = $_.Exception.Response.StatusCode.Value__
        if ($errStatus -eq 404) {
            Print-TestResult "DELETE /passagens/99 lançou HTTP 404 Not Found corretamente." "SUCESSO" "Green"
        } else {
            Print-TestResult "DELETE /passagens/99 retornou HTTP $errStatus em vez de 404." "FALHA" "Red"
        }
    }

    Write-Host "`n==========================================================" -ForegroundColor Cyan
    Write-Host " TODOS OS TESTES PASSARAM COM EXCELÊNCIA! " -ForegroundColor Green
    Write-Host "==========================================================" -ForegroundColor Cyan

} catch {
    Write-Host "`nOcorreu um erro inesperado durante a execução dos testes:" -ForegroundColor Red
    Write-Error $_
}
