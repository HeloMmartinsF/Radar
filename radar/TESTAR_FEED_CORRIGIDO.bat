@echo off
echo ========================================
echo  TESTE DO FEED CORRIGIDO
echo ========================================
echo.
echo Este script vai:
echo 1. Recompilar o projeto com as correções
echo 2. Copiar arquivos estáticos para target
echo 3. Reiniciar o servidor
echo.
pause

echo.
echo [1/3] Recompilando projeto...
call mvnw.cmd clean compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ERRO na compilação!
    pause
    exit /b 1
)

echo.
echo [2/3] Copiando arquivos estáticos...
xcopy /E /I /Y "src\main\resources\static\uploads" "target\classes\static\uploads\"
xcopy /Y "src\main\resources\static\pages\feed.html" "target\classes\static\pages\"

echo.
echo [3/3] Iniciando servidor...
echo.
echo ========================================
echo  TESTES A REALIZAR:
echo ========================================
echo 1. Acesse: http://localhost:8080/pages/feed.html
echo 2. Faça login como FORNECEDOR
echo 3. Crie uma publicação com imagem
echo 4. Verifique se a imagem aparece centralizada
echo 5. Clique em "Editar publicação"
echo 6. Altere o texto e salve
echo 7. Atualize a página (F5)
echo 8. Confirme que a edição foi salva
echo 9. Feche o servidor (Ctrl+C)
echo 10. Reinicie e confirme que tudo continua
echo ========================================
echo.
echo Pressione Ctrl+C para encerrar o servidor quando terminar os testes.
echo.
pause

call mvnw.cmd spring-boot:run
