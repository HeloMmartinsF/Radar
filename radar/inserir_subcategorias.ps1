# Script para inserir subcategorias via API REST
$baseUrl = "http://localhost:8080/api/subcategorias"
$jsonFile = "subcategorias_insert.json"

# Carregar dados do JSON
$dados = Get-Content $jsonFile | ConvertFrom-Json

$totalCategorias = $dados.Count
$totalSubcategorias = 0
$sucessos = 0
$erros = 0

Write-Host "=== Iniciando inserção de subcategorias ===" -ForegroundColor Cyan
Write-Host "Total de categorias: $totalCategorias" -ForegroundColor Yellow

foreach ($item in $dados) {
    $categoria = $item.categoria
    $subcategorias = $item.subcategorias
    $count = $subcategorias.Count
    $totalSubcategorias += $count
    
    Write-Host "`n[$categoria] - $count subcategorias" -ForegroundColor Green
    
    foreach ($sub in $subcategorias) {
        $body = @{
            nome = $sub.nome
            descricao = $sub.descricao
            categoria = $categoria
            ativa = $true
        } | ConvertTo-Json -Compress
        
        try {
            $response = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
            Write-Host "  ✓ $($sub.nome)" -ForegroundColor DarkGray
            $sucessos++
        }
        catch {
            Write-Host "  ✗ ERRO: $($sub.nome) - $($_.Exception.Message)" -ForegroundColor Red
            $erros++
        }
        
        Start-Sleep -Milliseconds 100
    }
}

Write-Host "`n=== Resumo ===" -ForegroundColor Cyan
Write-Host "Categorias processadas: $totalCategorias" -ForegroundColor Yellow
Write-Host "Total de subcategorias: $totalSubcategorias" -ForegroundColor Yellow
Write-Host "Sucessos: $sucessos" -ForegroundColor Green
Write-Host "Erros: $erros" -ForegroundColor Red

if ($erros -eq 0) {
    Write-Host "`n✓ Todas as subcategorias foram inseridas com sucesso!" -ForegroundColor Green
} else {
    Write-Host "`n⚠ Algumas subcategorias falharam. Verifique os erros acima." -ForegroundColor Yellow
}
