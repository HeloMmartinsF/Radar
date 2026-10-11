@echo off
echo ========================================
echo COMPILANDO CORRECAO - BUSCA INTELIGENTE
echo ========================================
echo.

cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/2] Limpando e compilando...
call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCESSO! Compilacao concluida.
    echo ========================================
    echo.
    echo Agora voce pode iniciar o servidor com:
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
