# Script PowerShell para commit completo
Set-Location "C:\Projeto\Projeto_Radar_Fornecedor\radar"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  GIT COMMIT - BUSCA INTELIGENTE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "[1/5] Status atual do Git..." -ForegroundColor Yellow
git status --short
Write-Host ""

Write-Host "[2/5] Adicionando TODOS os arquivos..." -ForegroundColor Yellow
git add -A
Write-Host "[OK] Arquivos adicionados!" -ForegroundColor Green
Write-Host ""

Write-Host "[3/5] Criando commit..." -ForegroundColor Yellow
$commitMessage = @"
feat: Implementa Busca Inteligente + Corrige compatibilidade Java 17

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
  * CHANGELOG_BUSCA_INTELIGENTE.md

- [SCRIPTS] Automacao de Build e Servidor
  * COMPILAR_AGORA_JAVA17.bat
  * INICIAR_SERVIDOR_AGORA.bat
  * COMMIT_TUDO_AGORA.bat

Tested: Compilacao OK (Java 17), Servidor iniciado, 91 arquivos compilados
"@

git commit -m $commitMessage

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Commit criado com sucesso!" -ForegroundColor Green
} else {
    Write-Host "[ERRO] Falha ao criar commit!" -ForegroundColor Red
    Read-Host "Pressione Enter para sair"
    exit 1
}
Write-Host ""

Write-Host "[4/5] Detalhes do commit..." -ForegroundColor Yellow
git log -1 --stat
Write-Host ""

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "[5/5] PUSH PARA O REPOSITORIO REMOTO?" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
$resposta = Read-Host "Deseja fazer push agora? (S/N)"

if ($resposta -eq "S" -or $resposta -eq "s") {
    Write-Host ""
    Write-Host "[INFO] Fazendo push..." -ForegroundColor Yellow
    git push
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  SUCESSO!" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "Commit e push realizados com sucesso!" -ForegroundColor Green
        Write-Host "Todas as alteracoes estao salvas no Git." -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host ""
        Write-Host "[ERRO] Falha no push!" -ForegroundColor Red
        Write-Host "Verifique sua conexao e credenciais." -ForegroundColor Red
        Write-Host ""
    }
} else {
    Write-Host ""
    Write-Host "[INFO] Push cancelado." -ForegroundColor Yellow
    Write-Host "Voce pode fazer push depois com: git push" -ForegroundColor Yellow
    Write-Host ""
}

Write-Host "Pressione Enter para fechar..."
Read-Host
