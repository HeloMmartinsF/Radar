@echo off
chcp 65001 >nul
echo === INSERINDO TODAS AS SUBCATEGORIAS ===
echo.
powershell -ExecutionPolicy Bypass -Command "$json = Get-Content 'subcategorias_completas.json' -Raw | ConvertFrom-Json; $api = 'http://localhost:8080/api/subcategorias'; $total = 0; $erros = 0; for ($i = 0; $i -lt $json.Count; $i++) { $cat = $json[$i]; Write-Host \"[$($i+1)/24] $($cat.categoria)\" -ForegroundColor Yellow; foreach ($sub in $cat.subcategorias) { $body = @{nome=$sub.nome; descricao=$sub.descricao; categoria=$cat.categoria; ativa=$true} | ConvertTo-Json -Compress; try { Invoke-RestMethod -Uri $api -Method Post -Body $body -ContentType 'application/json' -ErrorAction Stop | Out-Null; Write-Host \"  OK: $($sub.nome)\" -ForegroundColor Green; $total++ } catch { Write-Host \"  ERRO: $($sub.nome)\" -ForegroundColor Red; $erros++ } }; Write-Host \"\" }; Write-Host \"=== RESUMO ===\" -ForegroundColor Cyan; Write-Host \"Inseridas: $total\" -ForegroundColor Green; Write-Host \"Erros: $erros\" -ForegroundColor $(if ($erros -gt 0) {'Red'} else {'Green'}); try { $stats = Invoke-RestMethod 'http://localhost:8080/api/subcategorias/estatisticas'; Write-Host \"Total no banco: $($stats.total)\" -ForegroundColor Cyan } catch {}"
echo.
echo Pressione qualquer tecla para sair...
pause >nul
