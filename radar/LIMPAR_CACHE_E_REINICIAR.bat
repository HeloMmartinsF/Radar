@echo off
echo ========================================
echo  LIMPANDO CACHE E REINICIANDO SERVIDOR
echo ========================================
echo.

echo [1/4] Parando processos Java (Spring Boot)...
taskkill /F /IM java.exe 2>nul
timeout /t 2 >nul

echo [2/4] Limpando cache do Maven/Target...
if exist "target\classes\static\layout\" (
    del /F /Q "target\classes\static\layout\*.html" 2>nul
    echo    - Cache HTML limpo
)

echo [3/4] Copiando arquivos atualizados...
xcopy /Y /Q "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\" >nul 2>&1
echo    - menu-lateral.html copiado

echo [4/4] Iniciando Spring Boot...
start "Radar Fornecedor" cmd /c "mvnw.cmd spring-boot:run"

echo.
echo ========================================
echo  CONCLUIDO!
echo ========================================
echo.
echo INSTRUCOES IMPORTANTES:
echo 1. Aguarde o Spring Boot inicializar (http://localhost:8080)
echo 2. FECHE COMPLETAMENTE o navegador (todas as abas)
echo 3. Reabra o navegador
echo 4. Acesse: http://localhost:8080
echo 5. Faca login com username = "admin"
echo 6. Verifique se aparecem: Administracao, Config. Email, Pagamentos
echo.
pause
