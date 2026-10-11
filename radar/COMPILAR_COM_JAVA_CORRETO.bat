@echo off
echo ========================================
echo VERIFICANDO VERSAO JAVA
echo ========================================
echo.

java -version 2>&1 | findstr /C:"version \"25" >nul
if %ERRORLEVEL% EQU 0 (
    echo [PROBLEMA DETECTADO]
    echo Voce esta usando Java 25, mas o projeto precisa de Java 11 ou 17.
    echo.
    echo SOLUCOES:
    echo.
    echo OPCAO 1 - Instalar Java 17 LTS (RECOMENDADO):
    echo   1. Baixe: https://adoptium.net/temurin/releases/?version=17
    echo   2. Instale o Java 17
    echo   3. Configure JAVA_HOME para apontar para Java 17
    echo   4. Execute este .bat novamente
    echo.
    echo OPCAO 2 - Usar Java 11 (se ja tiver instalado):
    echo   1. Configure JAVA_HOME para apontar para Java 11
    echo   2. Execute este .bat novamente
    echo.
    echo OPCAO 3 - Temporariamente definir Java 17 (se ja instalado):
    echo   Execute: set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.X.X-hotspot
    echo   Depois execute: mvnw.cmd clean compile
    echo.
    pause
    exit /b 1
)

echo Versao Java detectada:
java -version
echo.

cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"

echo ========================================
echo COMPILANDO PROJETO
echo ========================================
echo.

call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCESSO! Compilacao concluida.
    echo ========================================
    echo.
    echo Agora inicie o servidor com:
    echo "Iniciar SpringBoot Radar Fornecedor.bat"
    echo.
) else (
    echo.
    echo ========================================
    echo ERRO na compilacao!
    echo Verifique os erros acima.
    echo ========================================
)

pause
