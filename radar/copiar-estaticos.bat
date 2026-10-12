@echo off
cd /d C:\Projeto\Projeto_Radar_Fornecedor\radar
echo Copiando arquivos estaticos para target...
copy /Y "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\menu-lateral.html"
copy /Y "src\main\resources\static\js\suporte.js" "target\classes\static\js\suporte.js"
copy /Y "src\main\resources\static\js\admin-suporte.js" "target\classes\static\js\admin-suporte.js"
copy /Y "src\main\resources\static\pages\ajuda-suporte.html" "target\classes\static\pages\ajuda-suporte.html"
copy /Y "src\main\resources\static\pages\admin-suporte.html" "target\classes\static\pages\admin-suporte.html"
echo Arquivos copiados com sucesso!
pause
