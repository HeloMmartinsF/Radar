@echo off
echo ========================================
echo   COPIANDO ARQUIVOS DO PERFIL SOCIAL
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/3] Criando pastas necessarias...
if not exist "target\classes\static\pages" mkdir "target\classes\static\pages"
if not exist "target\classes\static\layout" mkdir "target\classes\static\layout"
if not exist "target\classes\static\uploads\perfis" mkdir "target\classes\static\uploads\perfis"
echo [OK] Pastas criadas!
echo.

echo [2/3] Copiando arquivos HTML...
copy /Y "src\main\resources\static\pages\perfil.html" "target\classes\static\pages\perfil.html"
copy /Y "src\main\resources\static\layout\menu-lateral.html" "target\classes\static\layout\menu-lateral.html"
echo [OK] Arquivos HTML copiados!
echo.

echo [3/3] Verificando...
if exist "target\classes\static\pages\perfil.html" (
    echo [OK] perfil.html copiado com sucesso!
) else (
    echo [ERRO] perfil.html NAO foi copiado!
)

if exist "target\classes\static\layout\menu-lateral.html" (
    echo [OK] menu-lateral.html copiado com sucesso!
) else (
    echo [ERRO] menu-lateral.html NAO foi copiado!
)

echo.
echo ========================================
echo   CONCLUIDO!
echo ========================================
echo.
echo O sistema de Perfil Social esta pronto!
echo Acesse: http://localhost:8080/pages/perfil.html
echo.
pause
