@echo off
echo ========================================
echo PUSH para Repositorio Remoto
echo ========================================
echo.
cd /d "C:\Projeto\Projeto_Radar_Fornecedor"
git push
echo.
if %ERRORLEVEL% EQU 0 (
    echo ========================================
    echo PUSH CONCLUIDO COM SUCESSO!
    echo ========================================
    echo.
    echo Perfil Social salvo no repositorio remoto!
) else (
    echo ========================================
    echo Verifique se ha algum problema acima
    echo ========================================
)
echo.
pause
