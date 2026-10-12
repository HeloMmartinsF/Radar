@echo off
cd /d C:\Projeto\Projeto_Radar_Fornecedor
echo Verificando status do Git...
git status
echo.
echo Adicionando arquivos...
git add -A
echo.
echo Fazendo commit...
git commit -m "ajustes finais"
echo.
echo Commit concluido!
pause
