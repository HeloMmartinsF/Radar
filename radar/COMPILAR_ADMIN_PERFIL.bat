@echo off
echo ========================================
echo COMPILANDO RECURSOS ADMIN PERFIS
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/2] Limpando e compilando projeto...
call mvnw.cmd clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo [2/2] Copiando arquivos estaticos atualizados...
xcopy /Y "src\main\resources\static\pages\admin-perfis.html" "target\classes\static\pages\"
xcopy /Y "src\main\resources\static\pages\perfil.html" "target\classes\static\pages\"
xcopy /Y "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\"

echo.
echo ========================================
echo COMPILACAO CONCLUIDA COM SUCESSO!
echo ========================================
echo.
echo Proximos passos:
echo 1. Reinicie o servidor Spring Boot
echo 2. Teste acesso ao perfil como fornecedor/cliente
echo 3. Teste acesso admin em /pages/admin-perfis.html
echo.
pause
