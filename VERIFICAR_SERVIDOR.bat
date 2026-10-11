@echo off
cls
echo ========================================
echo   STATUS DO SERVIDOR
echo ========================================
echo.

tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if %ERRORLEVEL%==0 (
    echo [OK] SERVIDOR ESTA RODANDO!
    echo.
    echo Acesse agora:
    echo.
    echo   http://localhost:8080
    echo   http://localhost:8080/pages/busca.html
    echo.
) else (
    echo [AVISO] Servidor NAO esta rodando.
    echo.
    echo Execute: INICIAR_SERVIDOR_AGORA.bat
    echo.
)

echo Pressione qualquer tecla para fechar...
pause >nul
