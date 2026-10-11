@echo off
cls
echo ========================================
echo   RADAR FORNECEDOR - INICIANDO SERVIDOR
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [INFO] Verificando Java...
java -version
echo.

echo [INFO] Sincronizando arquivos estaticos...
xcopy /Y /Q "src\main\resources\static\*.html" "target\classes\static\" 2>nul
xcopy /Y /Q /S "src\main\resources\static\pages\*.html" "target\classes\static\pages\" 2>nul
xcopy /Y /Q /S "src\main\resources\static\js\*.*" "target\classes\static\js\" 2>nul
xcopy /Y /Q "src\main\resources\static\*.css" "target\classes\static\" 2>nul
echo [OK] Arquivos sincronizados!
echo.

echo [INFO] Verificando se ja existe servidor rodando...
tasklist /FI "IMAGENAME eq java.exe" 2>NUL | find /I /N "java.exe">NUL
if %ERRORLEVEL%==0 (
    echo [AVISO] Servidor Java ja esta rodando!
    echo [OPCAO] Deseja parar o servidor atual? ^(S/N^)
    choice /C SN /N
    if !ERRORLEVEL!==1 (
        echo [INFO] Parando servidor...
        taskkill /F /IM java.exe >nul 2>&1
        timeout /t 3 >nul
        echo [OK] Servidor parado!
    )
)

echo.
echo ========================================
echo   INICIANDO SPRING BOOT...
echo   Aguarde 20-30 segundos
echo ========================================
echo.

start "Radar Fornecedor - Spring Boot" cmd /c "mvnw.cmd spring-boot:run"

echo [INFO] Servidor iniciando em nova janela...
echo.
timeout /t 5 >nul

echo ========================================
echo   SERVIDOR INICIADO!
echo ========================================
echo.
echo Aguarde ate ver "Started RadarApplication"
echo na janela que abriu.
echo.
echo Depois acesse:
echo.
echo   http://localhost:8080
echo   http://localhost:8080/pages/busca.html
echo.
echo Pressione qualquer tecla para fechar esta janela...
pause >nul
