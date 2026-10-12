@echo off
cd /d C:\Projeto\Projeto_Radar_Fornecedor\radar
echo Compilando projeto...
call mvnw.cmd clean compile > recompile_output.log 2>&1
echo.
echo Copiando arquivos estaticos para target...
copy /Y "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\menu-lateral.html" >nul
copy /Y "src\main\resources\static\js\suporte.js" "target\classes\static\js\suporte.js" >nul
copy /Y "src\main\resources\static\js\admin-suporte.js" "target\classes\static\js\admin-suporte.js" >nul
copy /Y "src\main\resources\static\pages\ajuda-suporte.html" "target\classes\static\pages\ajuda-suporte.html" >nul
copy /Y "src\main\resources\static\pages\admin-suporte.html" "target\classes\static\pages\admin-suporte.html" >nul
echo.
echo Compilacao e copia concluidas!
echo Verifique recompile_output.log para detalhes
pause
