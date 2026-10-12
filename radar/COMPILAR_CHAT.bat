@echo off
echo ========================================
echo Compilando Chat Comercial
echo ========================================
cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo.
echo [1/2] Compilando projeto...
call mvnw.cmd clean compile
if errorlevel 1 (
    echo ERRO na compilacao!
    pause
    exit /b 1
)

echo.
echo [2/2] Copiando arquivos estaticos do chat...
copy /Y "src\main\resources\static\pages\mensagens.html" "target\classes\static\pages\mensagens.html"
copy /Y "src\main\resources\static\pages\conversa.html" "target\classes\static\pages\conversa.html"
copy /Y "src\main\resources\static\js\mensagens.js" "target\classes\static\js\mensagens.js"
copy /Y "src\main\resources\static\js\conversa.js" "target\classes\static\js\conversa.js"
copy /Y "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\menu-lateral.html"

echo.
echo ========================================
echo Compilacao e copia concluidas!
echo ========================================
echo.
echo Proximo passo: Reiniciar o Spring Boot
echo.
pause
