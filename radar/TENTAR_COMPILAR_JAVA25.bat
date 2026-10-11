@echo off
echo ========================================
echo TENTATIVA: COMPILAR COM JAVA 25
echo ========================================
echo.
echo AVISO: Esta e uma solucao experimental!
echo A solucao recomendada e instalar Java 17.
echo.
echo Tentando compilar com flags de compatibilidade...
echo.

cd /d "c:\Projeto\Projeto_Radar_Fornecedor\radar"

set MAVEN_OPTS=--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED

echo Executando: mvnw.cmd clean compile
echo.

call mvnw.cmd clean compile

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SUCESSO INESPERADO!
    echo ========================================
    echo.
    echo A compilacao funcionou com Java 25!
    echo Inicie o servidor: Iniciar SpringBoot Radar Fornecedor.bat
    echo.
    echo ATENCAO: Podem haver erros em runtime.
    echo Recomendamos instalar Java 17 para estabilidade.
    echo.
) else (
    echo.
    echo ========================================
    echo FALHOU (como esperado)
    echo ========================================
    echo.
    echo Java 25 nao e compativel com Spring Boot 2.7.x
    echo.
    echo SOLUCAO:
    echo 1. Execute: INSTALAR_JAVA_17.bat
    echo 2. Baixe e instale Java 17
    echo 3. Feche este terminal e abra um novo
    echo 4. Verifique: java -version
    echo 5. Compile novamente
    echo.
)

pause
