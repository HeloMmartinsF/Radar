Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Copiando arquivos do Chat Comercial" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$base = "C:\Projeto\Projeto_Radar_Fornecedor\radar"
$src = "$base\src\main\resources\static"
$dest = "$base\target\classes\static"

$arquivos = @(
    "pages\mensagens.html",
    "pages\conversa.html",
    "js\mensagens.js",
    "js\conversa.js",
    "layout\menu-lateral.html"
)

foreach ($arq in $arquivos) {
    $srcFile = Join-Path $src $arq
    $destFile = Join-Path $dest $arq
    
    if (Test-Path $srcFile) {
        Copy-Item -Path $srcFile -Destination $destFile -Force
        Write-Host "[OK] Copiado: $arq" -ForegroundColor Green
    } else {
        Write-Host "[ERRO] Arquivo nao encontrado: $srcFile" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Copia concluida!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Proximo passo: Reiniciar o Spring Boot" -ForegroundColor Yellow
Write-Host ""
