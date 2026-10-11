@echo off
echo ========================================
echo COMPILANDO COM JAVA 17
echo ========================================
echo.

cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"

echo Versao Java:
java -version
echo.

echo ========================================
echo Limpando e compilando...
echo ========================================
echo.

call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo ✓ SUCESSO! COMPILACAO CONCLUIDA
    echo ========================================
    echo.
    echo Proximo passo:
    echo Execute: Iniciar SpringBoot Radar Fornecedor.bat
    echo.
    echo Depois acesse: http://localhost:8080/pages/busca.html
    echo.
) else (
    echo.
    echo ========================================
    echo ✗ ERRO NA COMPILACAO
    echo ========================================
    echo Verifique os erros acima.
    echo.
)

pause
