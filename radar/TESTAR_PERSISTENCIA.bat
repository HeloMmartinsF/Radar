@echo off
echo ====================================
echo   TESTANDO PERSISTENCIA H2
echo ====================================
echo.

echo [1] Inserindo 3 subcategorias de teste...
curl -X POST http://localhost:8080/api/subcategorias -H "Content-Type: application/json" -d "{\"nome\":\"Pecas de Motor\",\"descricao\":\"Motores e componentes\",\"categoria\":\"AUTOMOTIVO\",\"ativa\":true}"
echo.
curl -X POST http://localhost:8080/api/subcategorias -H "Content-Type: application/json" -d "{\"nome\":\"Freios e Suspensao\",\"descricao\":\"Sistemas de freio\",\"categoria\":\"AUTOMOTIVO\",\"ativa\":true}"
echo.
curl -X POST http://localhost:8080/api/subcategorias -H "Content-Type: application/json" -d "{\"nome\":\"Pneus e Rodas\",\"descricao\":\"Pneus e acessorios\",\"categoria\":\"AUTOMOTIVO\",\"ativa\":true}"
echo.

echo.
echo [2] Verificando quantas foram salvas...
curl http://localhost:8080/api/subcategorias/estatisticas
echo.

echo.
echo [3] Listando todas...
curl http://localhost:8080/api/subcategorias
echo.

echo.
echo ====================================
echo TESTE CONCLUIDO!
echo.
echo Agora REINICIE o servidor e execute:
echo   curl http://localhost:8080/api/subcategorias/estatisticas
echo.
echo Se mostrar total=3, a persistencia funciona!
echo ====================================
pause
