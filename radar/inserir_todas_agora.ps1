# Script para inserir todas as subcategorias
$baseUrl = "http://localhost:8080/api/subcategorias"
$dados = Get-Content "subcategorias_insert.json" -Raw | ConvertFrom-Json

$total = 0
$sucessos = 0
$erros = 0

Write-Host "=== INSERINDO SUBCATEGORIAS ===" -ForegroundColor Cyan
Write-Host ""

foreach ($item in $dados) {
    $categoria = $item.categoria
    $subs = $item.subcategorias
    $count = $subs.Count
    $total += $count
    
    Write-Host "[$categoria] - $count subcategorias" -ForegroundColor Yellow
    
    foreach ($sub in $subs) {
        $body = @{
            nome = $sub.nome
            descricao = $sub.descricao
            categoria = $categoria
            ativa = $true
        } | ConvertTo-Json
        
        try {
            $result = Invoke-RestMethod -Uri $baseUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
            Write-Host "  OK: $($sub.nome)" -ForegroundColor DarkGreen
            $sucessos++
        }
        catch {
            Write-Host "  ERRO: $($sub.nome)" -ForegroundColor Red
            $erros++
        }
        
        Start-Sleep -Milliseconds 50
    }
}

Write-Host ""
Write-Host "=== RESUMO ===" -ForegroundColor Cyan
Write-Host "Total esperado: $total" -ForegroundColor Yellow
Write-Host "Sucessos: $sucessos" -ForegroundColor Green
Write-Host "Erros: $erros" -ForegroundColor Red
Write-Host ""

# Verificar estatísticas finais
$stats = Invoke-RestMethod -Uri "$baseUrl/estatisticas"
Write-Host "=== BANCO DE DADOS ===" -ForegroundColor Cyan
Write-Host "Total salvo: $($stats.total)" -ForegroundColor Yellow
Write-Host "Ativas: $($stats.ativas)" -ForegroundColor Green
