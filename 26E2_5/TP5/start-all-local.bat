@echo off
title Nexus Store TP5 - Inicializador Hibrido Local
echo ========================================================
echo   Nexus Store - TP5: Inicializando Servicos Locais
echo ========================================================
echo.
echo Verificando se o Docker Desktop esta em execucao...
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [AVISO] Docker Desktop nao esta aberto.
    echo Para que RabbitMQ, Zipkin e Prometheus funcionem,
    echo abra o Docker Desktop no Windows.
    echo.
) else (
    echo 1. Iniciando Infraestrutura Docker (RabbitMQ, Zipkin, Prometheus, Grafana)...
    docker compose up -d rabbitmq zipkin prometheus grafana
)

echo.
echo 2. Abrindo Terminal do Microsservico de Frete (porta 8082)...
start "TP5 - Shipping Service (8082)" powershell -NoExit -Command "cd '%~dp0shipping-service'; Write-Host 'Iniciando shipping-service na porta 8082...' -ForegroundColor Cyan; mvn spring-boot:run"

timeout /t 5 /nobreak >nul

echo.
echo 3. Abrindo Terminal do Backend Principal (porta 8080)...
start "TP5 - Backend (8080)" powershell -NoExit -Command "cd '%~dp0backend'; Write-Host 'Iniciando tp5-backend na porta 8080...' -ForegroundColor Green; mvn spring-boot:run"

timeout /t 5 /nobreak >nul

echo.
echo 4. Abrindo Terminal do Frontend Vite (porta 5173)...
start "TP5 - Frontend (5173)" powershell -NoExit -Command "cd '%~dp0frontend'; Write-Host 'Iniciando frontend React/Vite na porta 5173...' -ForegroundColor Yellow; npm run dev"

echo.
echo ========================================================
echo   Todos os servicos foram iniciados em novas janelas!
echo.
echo   [URLs do Sistema para o Video]:
echo   - Frontend Loja & DevOps:  http://localhost:5173
echo   - Backend Actuator Health: http://localhost:8080/actuator/health
echo   - Backend Prometheus:      http://localhost:8080/actuator/prometheus
echo   - Shipping Actuator:       http://localhost:8082/actuator/health
echo   - Zipkin Tracing:          http://localhost:9411
echo   - RabbitMQ Dashboard:      http://localhost:15672 (guest/guest)
echo   - Prometheus Server:       http://localhost:9090
echo   - Grafana Dashboards:      http://localhost:3000 (admin/admin)
echo ========================================================
pause
