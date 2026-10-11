@echo off
echo ============================================
echo   RADAR FORNECEDOR - MIGRANDO PARA SQLITE
echo ============================================
echo.

echo [1/4] Encerrando Java...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul

echo [2/4] Baixando dependencias Maven...
echo (Isso pode levar alguns minutos na primeira vez)
echo.

set JAVA_HOME=C:\Users\Home-PC\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

call mvnw.cmd clean install -DskipTests

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ERRO: Falha ao compilar o projeto!
    pause
    exit /b 1
)

echo.
echo [3/4] Compilacao concluida com sucesso!
echo.
echo [4/4] Iniciando servidor com SQLite...
echo.
echo IMPORTANTE: O servidor agora usará DBLRadar.db (SQLite)
echo Aguarde a mensagem "Started RadarApplication"
echo Depois acesse: http://localhost:8080
echo.
echo ============================================
echo.

call mvnw.cmd spring-boot:run

pause
