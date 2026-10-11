@echo off
echo ========================================
echo INSTALACAO JAVA 17 - RADAR FORNECEDOR
echo ========================================
echo.
echo SITUACAO ATUAL:
echo - Java instalado: Java 25 (incompativel)
echo - Java necessario: Java 17 LTS
echo.
echo ========================================
echo PASSO 1: DOWNLOAD
echo ========================================
echo.
echo Vou abrir a pagina de download do Java 17 no seu navegador.
echo.
echo IMPORTANTE:
echo 1. Escolha: "JDK" (nao JRE)
echo 2. Escolha: "Windows x64" (.msi)
echo 3. Baixe o arquivo .msi
echo.
pause

start https://adoptium.net/temurin/releases/?version=17

echo.
echo ========================================
echo PASSO 2: INSTALACAO
echo ========================================
echo.
echo Apos baixar, execute o arquivo .msi
echo.
echo Durante a instalacao, MARQUE:
echo [X] Set JAVA_HOME variable
echo [X] Add to PATH
echo [X] JavaSoft (Oracle) registry keys
echo.
echo ========================================
echo PASSO 3: VERIFICACAO
echo ========================================
echo.
echo Apos instalar:
echo 1. FECHE este terminal
echo 2. Abra um NOVO PowerShell/CMD
echo 3. Execute: java -version
echo 4. Deve mostrar: "openjdk version 17.0.X"
echo.
echo Se ainda mostrar Java 25:
echo - Verifique se marcou "Add to PATH" na instalacao
echo - Reinicie o computador
echo.
echo ========================================
echo PASSO 4: COMPILAR O PROJETO
echo ========================================
echo.
echo Depois de confirmar Java 17:
echo Execute: COMPILAR_COM_JAVA_CORRETO.bat
echo.
pause
