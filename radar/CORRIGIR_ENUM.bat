@echo off
echo ========================================
echo CORRECAO FINAL: Uso do Enum TipoUsuario
echo ========================================
echo.
echo Problema: getTipoUsuario() nao existe
echo Correcao: usuario.getTipo() retorna enum TipoUsuario
echo Comparacao: usuario.getTipo() == TipoUsuario.ADMIN
echo.
cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/1] Recompilando...
call mvnw.cmd clean compile

echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo SUCESSO! PRONTO PARA TESTAR!
    echo ========================================
    echo.
    echo PROXIMOS PASSOS:
    echo 1. Feche o servidor Spring Boot atual
    echo 2. Execute: "Iniciar SpringBoot Radar Fornecedor.bat"
    echo 3. Faca login novamente
    echo 4. Acesse: /pages/perfil.html
    echo.
    echo Agora o perfil deve funcionar corretamente!
) else (
    echo ========================================
    echo ERRO - Verifique mensagens acima
    echo ========================================
)
echo.
pause
