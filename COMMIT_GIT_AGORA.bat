@echo off
cls
echo ========================================
echo   GIT COMMIT - BUSCA INTELIGENTE
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/5] Status atual do Git...
echo.
git status --short
echo.

echo ========================================
echo [2/5] Adicionando TODOS os arquivos...
echo ========================================
git add -A
echo [OK] Arquivos adicionados!
echo.

echo ========================================
echo [3/5] Criando commit...
echo ========================================
git commit -m "feat: Implementa Busca Inteligente + Corrige compatibilidade Java 17" -m "- [FEATURE] Busca Inteligente de Produtos" -m "  * BuscaProdutoController.java: endpoints REST para busca" -m "  * BuscaProdutoService.java: logica de busca com Criteria API" -m "  * busca.html: interface completa com filtros e paginacao" -m "  * Busca tolerante a acentos e erros de digitacao" -m "  * Filtros: categoria, preco, fornecedor" -m "  * Ordenacao: relevancia, preco, nome" -m "  * Autocomplete em tempo real" -m "  * Paginacao (20 itens/pagina)" -m "" -m "- [FIX] Correcoes de Compilacao" -m "  * Linha 139: Map.of() -> HashMap explicito (type inference)" -m "  * Linha 207: formatarNomeCategoria() refatorado (regex -> loop)" -m "" -m "- [CONFIG] Atualizacao Java 17" -m "  * pom.xml: java.version 11 -> 17" -m "  * maven-compiler-plugin: 3.10.1 -> 3.13.0" -m "  * Adiciona flags de compatibilidade --add-opens" -m "  * Lombok atualizado: 1.18.28 -> 1.18.30" -m "" -m "- [DOCS] Documentacao Completa" -m "  * CORRECOES_BUSCA_INTELIGENTE.md" -m "  * SOLUCAO_JAVA_25.md" -m "  * CHANGELOG_BUSCA_INTELIGENTE.md" -m "" -m "- [SCRIPTS] Automacao de Build" -m "  * COMPILAR_AGORA_JAVA17.bat" -m "  * INICIAR_SERVIDOR_AGORA.bat" -m "" -m "Tested: Compilacao OK (Java 17), Servidor iniciado, 91 arquivos compilados"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERRO] Falha ao criar commit!
    echo Pode ser que nao haja mudancas para commitar.
    pause
    exit /b 1
)

echo.
echo [OK] Commit criado com sucesso!
echo.

echo ========================================
echo [4/5] Detalhes do commit...
echo ========================================
git log -1 --oneline
echo.

echo ========================================
echo [5/5] PUSH PARA O REPOSITORIO REMOTO?
echo ========================================
echo.
echo Pressione S para fazer push agora
echo Pressione N para fazer push depois
echo.
choice /C SN /N /M "Sua escolha: "

if %ERRORLEVEL%==1 (
    echo.
    echo [INFO] Fazendo push para o GitHub...
    git push
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================
        echo   SUCESSO!
        echo ========================================
        echo.
        echo Commit e push realizados com sucesso!
        echo Todas as alteracoes estao salvas no Git!
        echo.
    ) else (
        echo.
        echo [ERRO] Falha no push!
        echo.
        echo Verifique:
        echo - Conexao com internet
        echo - Token de acesso GitHub
        echo - Permissoes do repositorio
        echo.
        echo Voce pode tentar novamente com:
        echo   cd C:\Projeto\Projeto_Radar_Fornecedor\radar
        echo   git push
        echo.
    )
) else (
    echo.
    echo [INFO] Push cancelado pelo usuario.
    echo.
    echo Commit criado localmente!
    echo Quando quiser fazer push, execute:
    echo   cd C:\Projeto\Projeto_Radar_Fornecedor\radar
    echo   git push
    echo.
)

echo Pressione qualquer tecla para fechar...
pause >nul
