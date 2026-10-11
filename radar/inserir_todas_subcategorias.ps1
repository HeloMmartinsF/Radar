# Script para inserir todas as subcategorias via API REST
# Lê o arquivo JSON e insere uma por uma

$jsonContent = Get-Content "subcategorias_completas.json" -Raw | ConvertFrom-Json
$apiUrl = "http://localhost:8080/api/subcategorias"
$totalInseridas = 0
$erros = 0

Write-Host "=== INICIANDO INSERCAO DE SUBCATEGORIAS ===" -ForegroundColor Cyan
Write-Host ""

foreach ($categoriaObj in $jsonContent) {
    $categoria = $categoriaObj.categoria
    $subcategorias = $categoriaObj.subcategorias
    
    Write-Host "Categoria: $categoria" -ForegroundColor Yellow
    Write-Host "Subcategorias a inserir: $($subcategorias.Count)" -ForegroundColor Gray
    Write-Host ""
    
    foreach ($sub in $subcategorias) {
        $body = @{
            nome = $sub.nome
            descricao = $sub.descricao
            categoria = $categoria
            ativa = $true
        } | ConvertTo-Json -Depth 10
        
        try {
            $response = Invoke-RestMethod -Uri $apiUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
            Write-Host "  [OK] $($sub.nome)" -ForegroundColor Green
            $totalInseridas++
        }
        catch {
            Write-Host "  [ERRO] $($sub.nome) - $($_.Exception.Message)" -ForegroundColor Red
            $erros++
        }
        
        # Pausa de 100ms para não sobrecarregar a API
        Start-Sleep -Milliseconds 100
    }
    
    Write-Host ""
}

Write-Host "=== RESUMO ===" -ForegroundColor Cyan
Write-Host "Total inseridas: $totalInseridas" -ForegroundColor Green
Write-Host "Erros: $erros" -ForegroundColor $(if ($erros -gt 0) { "Red" } else { "Green" })
Write-Host ""

# Verificar estatísticas finais
try {
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/subcategorias/estatisticas"
    Write-Host "Total no banco: $($stats.total)" -ForegroundColor Cyan
    Write-Host "Ativas: $($stats.ativas)" -ForegroundColor Cyan
}
catch {
    Write-Host "Não foi possível obter estatísticas finais" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
