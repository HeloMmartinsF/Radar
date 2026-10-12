@echo off
echo ========================================
echo COMPILACAO: Perfil Comercial Fornecedor
echo ========================================
echo.
cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/3] Compilando projeto...
call mvnw.cmd clean compile

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha na compilacao!
    pause
    exit /b 1
)

echo.
echo [2/3] Copiando arquivos static para target...
xcopy /Y "src\main\resources\static\pages\perfil-comercial.html" "target\classes\static\pages\" 2>nul
xcopy /Y "src\main\resources\static\js\perfil-comercial.js" "target\classes\static\js\" 2>nul
xcopy /Y "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\" 2>nul

echo.
echo [3/3] Aplicando migration SQL...
echo NOTA: O Hibernate aplicara automaticamente com ddl-auto=update
echo Se preferir aplicar manualmente: sqlite3 DBLRadar.mv.db ^< migration_perfil_comercial.sql

echo.
echo ========================================
echo COMPILACAO CONCLUIDA COM SUCESSO!
echo ========================================
echo.
echo PROXIMOS PASSOS:
echo 1. Reinicie o servidor: "Iniciar SpringBoot Radar Fornecedor.bat"
echo 2. Aguarde "Started RadarApplication"
echo 3. Execute os testes conforme checklist
echo.
echo TESTES PRIORITARIOS:
echo - Login como fornecedor -^> Clicar "Meu Perfil" -^> Ver perfil comercial
echo - Login como cliente -^> Clicar "Meu Perfil" -^> Ver perfil social (nao afetado)
echo - Cliente avalia fornecedor -^> Avaliacao salva
echo - Fornecedor nao pode se auto-avaliar
echo.
pause
