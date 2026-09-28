@echo off
title Nexus Store TP5 - Parar Todos os Servicos
echo ========================================================
echo   Encerrando conteineres e processos do TP5...
echo ========================================================
docker compose down >nul 2>&1
echo Conteineres Docker encerrados.
echo.
echo Finalizando processos Java e Node em execucao...
taskkill /F /IM "java.exe" >nul 2>&1
taskkill /F /IM "node.exe" >nul 2>&1
echo Todos os servicos locais foram finalizados com sucesso!
pause
