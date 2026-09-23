#!/usr/bin/env bash
# ==============================================================================
# Script de Demonstração e Coleta de Evidências - Freela Marketplace (Bash)
# ==============================================================================

set -e

GATEWAY_URL=${1:-"http://localhost:8080"}
CORRELATION_ID="evidencia-$(date +%s)"

echo "=================================================================="
echo " INICIANDO VALIDAÇÃO DE PONTA A PONTA: FREELA MARKETPLACE"
echo " Correlation ID da Operação: $CORRELATION_ID"
echo "=================================================================="

# 1. Criar Contrato
echo ""
echo "[EVIDÊNCIA 1] Criando Contrato via API Gateway (POST /api/contratos)..."
RESP_CRIAR=$(curl -s -X POST "$GATEWAY_URL/api/contratos" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: $CORRELATION_ID" \
  -d '{
    "clienteId": "11111111-1111-1111-1111-111111111111",
    "freelancerId": "22222222-2222-2222-2222-222222222222",
    "titulo": "Construção de API de Pagamentos com Kafka",
    "valor": 3500.00
  }')

echo "Resposta: $RESP_CRIAR"
CONTRATO_ID=$(echo "$RESP_CRIAR" | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo "Contrato ID: $CONTRATO_ID"

sleep 1

# 2. Registrar Entrega
echo ""
echo "[EVIDÊNCIA 2] Registrando Entrega (PUT /api/contratos/$CONTRATO_ID/entrega)..."
curl -s -X PUT "$GATEWAY_URL/api/contratos/$CONTRATO_ID/entrega" \
  -H "X-Correlation-Id: $CORRELATION_ID"

sleep 1

# 3. Concluir Contrato
echo ""
echo "[EVIDÊNCIA 3] Concluindo Contrato (PUT /api/contratos/$CONTRATO_ID/concluir)..."
curl -s -X PUT "$GATEWAY_URL/api/contratos/$CONTRATO_ID/concluir" \
  -H "X-Correlation-Id: $CORRELATION_ID"

echo ""
echo "[AGUARDO] Aguardando 3 segundos para processamento assíncrono no Kafka..."
sleep 3

# 4. Notificações
echo ""
echo "[EVIDÊNCIA 4] Consultando Notificações (GET /api/notificacoes)..."
curl -s -X GET "$GATEWAY_URL/api/notificacoes" -H "X-Correlation-Id: $CORRELATION_ID"

# 5. Reputação
echo ""
echo ""
echo "[EVIDÊNCIA 5] Consultando Reputação (GET /api/reputacoes)..."
curl -s -X GET "$GATEWAY_URL/api/reputacoes" -H "X-Correlation-Id: $CORRELATION_ID"

# 6. Auditoria
echo ""
echo ""
echo "[EVIDÊNCIA 6] Consultando Auditoria (GET /api/auditoria?contratoId=$CONTRATO_ID)..."
curl -s -X GET "$GATEWAY_URL/api/auditoria?contratoId=$CONTRATO_ID" -H "X-Correlation-Id: $CORRELATION_ID"

echo ""
echo "=================================================================="
echo " VERIFICAÇÃO CONCLUÍDA COM SUCESSO!"
echo "=================================================================="
