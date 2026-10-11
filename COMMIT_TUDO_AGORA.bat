@echo off
cls
echo ========================================
echo   GIT COMMIT - BUSCA INTELIGENTE
echo   E CORRECOES JAVA 17
echo ========================================
echo.

cd /d "C:\Projeto\Projeto_Radar_Fornecedor\radar"

echo [1/6] Verificando status do Git...
echo.
git status
echo.

echo ========================================
echo [2/6] Adicionando arquivos ao stage...
echo ========================================
echo.

:: Arquivos principais da Busca Inteligente
git add src/main/java/br/com/radarfornecedor/radar/controller/BuscaProdutoController.java
git add src/main/java/br/com/radarfornecedor/radar/service/BuscaProdutoService.java
git add src/main/resources/static/pages/busca.html
git add src/main/resources/static/layout/menu-lateral.html

:: Configuracoes do projeto (pom.xml atualizado para Java 17)
git add pom.xml

:: Documentacao criada
git add CORRECOES_BUSCA_INTELIGENTE.md
git add SOLUCAO_JAVA_25.md
git add OPCAO_ALTERNATIVA_COMPILAR.md

:: Scripts de compilacao e inicializacao
git add COMPILAR_AGORA_JAVA17.bat
git add COMPILAR_COM_JAVA_CORRETO.bat
git add TENTAR_COMPILAR_JAVA25.bat

echo [OK] Arquivos adicionados ao stage!
echo.

echo ========================================
echo [3/6] Verificando arquivos staged...
echo ========================================
echo.
git status --short
echo.

echo ========================================
echo [4/6] Criando commit...
echo ========================================
echo.

git commit -m "feat: Implementa Busca Inteligente + Corrige compatibilidade Java 17

- [FEATURE] Busca Inteligente de Produtos
  * BuscaProdutoController.java: endpoints REST para busca
  * BuscaProdutoService.java: logica de busca com Criteria API
  * busca.html: interface completa com filtros e paginacao
  * Busca tolerante a acentos e erros de digitacao
  * Filtros: categoria, preco, fornecedor
  * Ordenacao: relevancia, preco, nome
  * Autocomplete em tempo real
  * Paginacao (20 itens/pagina)

- [FIX] Correcoes de Compilacao
  * Linha 139: Map.of() -> HashMap explicito (type inference)
  * Linha 207: formatarNomeCategoria() refatorado (regex -> loop)

- [CONFIG] Atualizacao Java 17
  * pom.xml: java.version 11 -> 17
  * maven-compiler-plugin: 3.10.1 -> 3.13.0
  * Adiciona flags de compatibilidade --add-opens
  * Lombok atualizado: 1.18.28 -> 1.18.30

- [DOCS] Documentacao Completa
  * CORRECOES_BUSCA_INTELIGENTE.md
  * SOLUCAO_JAVA_25.md
  * OPCAO_ALTERNATIVA_COMPILAR.md

- [SCRIPTS] Automacao de Build
  * COMPILAR_AGORA_JAVA17.bat
  * COMPILAR_COM_JAVA_CORRETO.bat
  * TENTAR_COMPILAR_JAVA25.bat

Tested: Compilacao OK (Java 17), Servidor iniciado, 91 arquivos compilados"

if %ERRORLEVEL% NEQ 0 (
    echo [ERRO] Falha ao criar commit!
    pause
    exit /b 1
)

echo [OK] Commit criado com sucesso!
echo.

echo ========================================
echo [5/6] Verificando commit...
echo ========================================
echo.
git log -1 --stat
echo.

echo ========================================
echo [6/6] PUSH para o repositorio remoto?
echo ========================================
echo.
echo Deseja fazer push agora? (S/N)
choice /C SN /N
if %ERRORLEVEL%==1 (
    echo.
    echo [INFO] Fazendo push...
    git push
    
    if %ERRORLEVEL% EQU 0 (
        echo.
        echo ========================================
        echo   SUCESSO!
        echo ========================================
        echo.
        echo Commit e push realizados com sucesso!
        echo Todas as alteracoes estao salvas no Git.
        echo.
    ) else (
        echo.
        echo [ERRO] Falha no push!
        echo Verifique sua conexao e credenciais.
        echo.
    )
) else (
    echo.
    echo [INFO] Push cancelado.
    echo Voce pode fazer push depois com: git push
    echo.
)

echo Pressione qualquer tecla para fechar...
pause >nul
