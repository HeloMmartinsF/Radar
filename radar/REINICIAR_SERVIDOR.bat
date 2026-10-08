@echo off
echo ============================================
echo   REINICIANDO SPRING BOOT RADAR FORNECEDOR
echo ============================================
echo.

echo [1/3] Encerrando processos Java...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul

echo [2/3] Limpando logs antigos...
del /Q spring_boot.log 2>nul

echo [3/3] Iniciando servidor na porta 8080...
echo.
echo AGUARDE: O servidor pode levar 30-60 segundos para iniciar.
echo Quando ver "Started RadarApplication", pode acessar: http://localhost:8080
echo.
echo ============================================
echo.

set JAVA_HOME=C:\Users\Home-PC\AppData\Local\Programs\Eclipse Adoptium\jdk-17.0.18.8-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

mvnw.cmd spring-boot:run

pause
