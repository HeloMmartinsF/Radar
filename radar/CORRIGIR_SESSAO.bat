@echo off
echo ========================================
echo CORRECAO: Buscar Usuario da Sessao
echo ========================================
echo.
echo Problema: PerfilSocialController buscava session.getAttribute("username")
echo Correcao: Agora busca session.getAttribute("usuario") - objeto Usuario completo
echo.
echo Isso resolve o erro "Usuario nao autenticado" mesmo estando logado.
echo.
cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/1] Recompilando...
call mvnw.cmd clean compile

echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo CORRECAO APLICADA COM SUCESSO!
    echo ========================================
    echo.
    echo PROXIMOS PASSOS:
    echo 1. Feche o servidor Spring Boot atual
    echo 2. Execute: "Iniciar SpringBoot Radar Fornecedor.bat"
    echo 3. Faca login novamente
    echo 4. Acesse: /pages/perfil.html
    echo.
    echo O perfil deve carregar corretamente agora!
) else (
    echo ========================================
    echo ERRO - Verifique mensagens acima
    echo ========================================
)
echo.
pause
