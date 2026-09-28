@echo off
title Nexus Store TP5 - Docker Compose Completo
echo ========================================================
echo   Nexus Store - TP5: Subindo Toda a Stack via Docker Compose
echo   - RabbitMQ (5672 / 15672)
echo   - Zipkin Distributed Tracing (9411)
echo   - Prometheus Metrics (9090)
echo   - Grafana Dashboard (3000)
echo   - Shipping Service (8082)
echo   - Backend Orders & Catalog (8080)
echo   - Frontend React SPA (5173)
echo ========================================================
echo.
echo Verificando se o Docker Desktop esta em execucao...
docker info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo [ERRO] O Docker Desktop nao esta rodando.
    echo Por favor, abra o aplicativo Docker Desktop no Windows e execute este script novamente.
    echo.
    pause
    exit /b 1
)

echo Docker Desktop detectado com sucesso!
echo Construindo e inicializando os conteineres...
docker compose up --build
pause
