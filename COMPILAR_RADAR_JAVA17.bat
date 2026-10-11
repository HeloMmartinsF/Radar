@echo off
cls
echo ========================================
echo   RADAR FORNECEDOR - COMPILACAO
echo   Java 17 Detectado!
echo ========================================
echo.

cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [INFO] Verificando versao Java...
java -version
echo.

echo [INFO] Limpando builds anteriores...
echo [INFO] Compilando codigo fonte...
echo [INFO] Aguarde, isso pode levar 1-2 minutos...
echo.

call mvnw.cmd clean compile

echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo   ✓✓✓ SUCESSO! ✓✓✓
    echo ========================================
    echo.
    echo O projeto foi compilado com sucesso!
    echo.
    echo PROXIMO PASSO:
    echo 1. Execute: "Iniciar SpringBoot Radar Fornecedor.bat"
    echo 2. Aguarde servidor iniciar (20-30 segundos)
    echo 3. Acesse: http://localhost:8080
    echo 4. Teste Busca Inteligente: http://localhost:8080/pages/busca.html
    echo.
) else (
    echo ========================================
    echo   ✗✗✗ ERRO NA COMPILACAO ✗✗✗
    echo ========================================
    echo.
    echo Verifique as mensagens de erro acima.
    echo.
    echo Se houver erros de codigo:
    echo - Copie a mensagem de erro
    echo - Envie para analise
    echo.
)

echo.
echo Pressione qualquer tecla para fechar...
pause >nul
