@echo off
cls
echo ========================================
echo   COMPILANDO CORRECAO - PERFIL SOCIAL
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [INFO] Corrigido: Atividade.getNome() -> Atividade.getDescricao()
echo.
echo [INFO] Compilando...
echo.

call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   SUCESSO! COMPILACAO OK
    echo ========================================
    echo.
    echo Agora inicie o servidor:
    echo "Iniciar SpringBoot Radar Fornecedor.bat"
    echo.
    echo Depois acesse:
    echo http://localhost:8080/pages/perfil.html
    echo.
) else (
    echo.
    echo ========================================
    echo   ERRO NA COMPILACAO!
    echo ========================================
    echo.
    echo Verifique as mensagens acima.
    echo.
)

pause
