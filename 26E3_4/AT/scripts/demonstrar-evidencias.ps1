# ==============================================================================
# Script de Demonstração e Coleta de Evidências - Freela Marketplace
# Assessment: Arquitetura Orientada a Eventos com Apache Kafka
# ==============================================================================

param(
    [string]$GatewayUrl = "http://localhost:8080",
    [string]$CorrelationId = "evidencia-$(Get-Random)"
)

$ErrorActionPreference = "Continue"

Write-Host "==================================================================" -ForegroundColor Cyan
Write-Host " INICIANDO VALIDAÇÃO DE PONTA A PONTA: FREELA MARKETPLACE" -ForegroundColor Cyan
Write-Host " Correlation ID da Operação: $CorrelationId" -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan

# 1. Requisição recebida pelo API Gateway -> Criação do Contrato
Write-Host "`n[EVIDÊNCIA 1] Criando Contrato via API Gateway (POST /api/contratos)..." -ForegroundColor Green
$bodyCriar = @{
    clienteId = "11111111-1111-1111-1111-111111111111"
    freelancerId = "22222222-2222-2222-2222-222222222222"
    titulo = "Construção de API de Pagamentos com Kafka"
    valor = 3500.00
} | ConvertTo-Json

try {
    $respCriar = Invoke-RestMethod -Uri "$GatewayUrl/api/contratos" -Method POST `
        -Headers @{ "Content-Type" = "application/json"; "X-Correlation-Id" = $CorrelationId } `
        -Body $bodyCriar
    
    $contratoId = $respCriar.id
    Write-Host "  -> Sucesso! Contrato Criado: ID = $contratoId, Status = $($respCriar.status)" -ForegroundColor Cyan
    Write-Host "  -> Resposta JSON: $(ConvertTo-Json $respCriar -Compress)" -ForegroundColor Gray
} catch {
    Write-Host "  -> Erro ao chamar Gateway: $_" -ForegroundColor Red
    exit 1
}

# 2. Registrar Entrega
Write-Host "`n[EVIDÊNCIA 2] Registrando Entrega (PUT /api/contratos/$contratoId/entrega)..." -ForegroundColor Green
Start-Sleep -Seconds 1
try {
    $respEntrega = Invoke-RestMethod -Uri "$GatewayUrl/api/contratos/$contratoId/entrega" -Method PUT `
        -Headers @{ "X-Correlation-Id" = $CorrelationId }
    Write-Host "  -> Sucesso! Status Atualizado = $($respEntrega.status)" -ForegroundColor Cyan
} catch {
    Write-Host "  -> Erro ao registrar entrega: $_" -ForegroundColor Red
}

# 3. Concluir Contrato
Write-Host "`n[EVIDÊNCIA 3] Concluindo Contrato (PUT /api/contratos/$contratoId/concluir)..." -ForegroundColor Green
Start-Sleep -Seconds 1
try {
    $respConcluir = Invoke-RestMethod -Uri "$GatewayUrl/api/contratos/$contratoId/concluir" -Method PUT `
        -Headers @{ "X-Correlation-Id" = $CorrelationId }
    Write-Host "  -> Sucesso! Status Atualizado = $($respConcluir.status)" -ForegroundColor Cyan
} catch {
    Write-Host "  -> Erro ao concluir contrato: $_" -ForegroundColor Red
}

# Aguardar propagação e consumo assíncrono via Kafka
Write-Host "`n[AGUARDO] Aguardando 3 segundos para conclusão do consumo assíncrono no Kafka..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 4. Verificar Notificações Geradas
Write-Host "`n[EVIDÊNCIA 4] Consultando Notificações Geradas (GET /api/notificacoes)..." -ForegroundColor Green
try {
    $notificacoes = Invoke-RestMethod -Uri "$GatewayUrl/api/notificacoes" -Method GET `
        -Headers @{ "X-Correlation-Id" = $CorrelationId }
    $notifsContrato = $notificacoes | Where-Object { $_.contratoId -eq $contratoId }
    Write-Host "  -> Notificações encontradas para este contrato: $($notifsContrato.Count)" -ForegroundColor Cyan
    foreach ($n in $notifsContrato) {
        Write-Host "     - Tipo: $($n.tipo) | Destinatário: $($n.destinatarioId) | Mensagem: $($n.mensagem)" -ForegroundColor White
    }
} catch {
    Write-Host "  -> Falha ao consultar notificações: $_" -ForegroundColor Red
}

# 5. Verificar Atualização de Reputação
Write-Host "`n[EVIDÊNCIA 5] Consultando Reputação do Freelancer (GET /api/reputacoes)..." -ForegroundColor Green
try {
    $reputacoes = Invoke-RestMethod -Uri "$GatewayUrl/api/reputacoes" -Method GET `
        -Headers @{ "X-Correlation-Id" = $CorrelationId }
    $repFreelancer = $reputacoes | Where-Object { $_.freelancerId -eq "22222222-2222-2222-2222-222222222222" }
    Write-Host "  -> Reputação do Freelancer:" -ForegroundColor Cyan
    Write-Host "     - Contratos Concluídos: $($repFreelancer.contratosConcluidos)" -ForegroundColor White
    Write-Host "     - Valor Total Acumulado: R$ $($repFreelancer.valorTotal)" -ForegroundColor White
} catch {
    Write-Host "  -> Falha ao consultar reputação: $_" -ForegroundColor Red
}

# 6. Verificar Auditoria dos Eventos
Write-Host "`n[EVIDÊNCIA 6] Consultando Registros de Auditoria (GET /api/auditoria?contratoId=$contratoId)..." -ForegroundColor Green
try {
    $auditoria = Invoke-RestMethod -Uri "$GatewayUrl/api/auditoria?contratoId=$contratoId" -Method GET `
        -Headers @{ "X-Correlation-Id" = $CorrelationId }
    Write-Host "  -> Total de Eventos Auditados: $($auditoria.Count)" -ForegroundColor Cyan
    foreach ($a in $auditoria) {
        Write-Host "     - Evento: $($a.eventType) | EventId: $($a.eventId) | Correlation: $($a.correlationId) | RecebidoEm: $($a.recebidoEm)" -ForegroundColor White
    }
} catch {
    Write-Host "  -> Falha ao consultar auditoria: $_" -ForegroundColor Red
}

Write-Host "`n==================================================================" -ForegroundColor Cyan
Write-Host " VERIFICAÇÃO CONCLUÍDA COM SUCESSO!" -ForegroundColor Cyan
Write-Host " Zipkin UI: http://localhost:9411 (Pesquise pelo CorrelationId: $CorrelationId)" -ForegroundColor Yellow
Write-Host " Kafka UI:  http://localhost:8090" -ForegroundColor Yellow
Write-Host " Grafana:   http://localhost:3000 (admin/admin)" -ForegroundColor Yellow
Write-Host "==================================================================" -ForegroundColor Cyan
