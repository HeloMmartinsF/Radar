@echo off
echo ========================================
echo  COMPILANDO PROJETO
echo ========================================
echo.
call mvnw.cmd clean compile -DskipTests
echo.
echo ========================================
if %ERRORLEVEL% EQU 0 (
    echo  BUILD SUCCESS - Copiando arquivos...
    echo ========================================
    xcopy /E /I /Y "src\main\resources\static\uploads" "target\classes\static\uploads\"
    xcopy /Y "src\main\resources\static\pages\feed.html" "target\classes\static\pages\"
    echo.
    echo ✅ PRONTO! Agora inicie o servidor:
    echo    "Iniciar SpringBoot Radar Fornecedor.bat"
) else (
    echo  BUILD FAILED - Verifique os erros acima
    echo ========================================
)
echo.
pause
