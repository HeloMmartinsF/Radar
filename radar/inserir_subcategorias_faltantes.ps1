# Script PowerShell para inserir subcategorias faltantes via API REST
# Não depende de Python - usa Invoke-RestMethod nativo do PowerShell

Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host ("=" * 69) -ForegroundColor Cyan
Write-Host " INSERÇÃO DE SUBCATEGORIAS FALTANTES" -ForegroundColor Yellow
Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host ("=" * 69) -ForegroundColor Cyan
Write-Host ""

$API_URL = "http://localhost:8080/api/subcategorias"

# Definir subcategorias
$subcategorias = @{
    "INDUSTRIAL" = @(
        "Ferramentas e Equipamentos", "Máquinas Industriais", "Componentes e Peças",
        "Parafusos e Fixadores", "Soldagem e Corte", "Equipamentos Hidráulicos",
        "Equipamentos Pneumáticos", "Rolamentos e Transmissão", "Vedação e Lubrificação",
        "Instrumentação e Controle"
    )
    "LIMPEZA_E_HIGIENE" = @(
        "Produtos de Limpeza", "Material de Higiene", "Equipamentos de Limpeza",
        "Descartáveis", "Saneantes e Desinfetantes", "Panos e Esponjas",
        "Vassouras e Rodos", "Papel Higiênico e Toalhas", "Sabonetes e Dispensers",
        "Lixeiras e Sacos de Lixo"
    )
    "SEGURANCA" = @(
        "EPIs (Equipamentos de Proteção)", "Capacetes e Óculos de Proteção", "Luvas de Segurança",
        "Botas e Calçados de Proteção", "Cintos e Talabartes", "Máscaras e Respiradores",
        "Protetores Auriculares", "Uniformes de Segurança", "Sinalização de Segurança",
        "Extintores e Equipamentos Contra Incêndio"
    )
    "PET" = @(
        "Ração e Alimentação", "Brinquedos para Pets", "Camas e Casinhas",
        "Coleiras e Guias", "Higiene e Banho", "Medicamentos Veterinários",
        "Roupas para Pets", "Aquarismo", "Acessórios para Gaiolas",
        "Transporte de Animais"
    )
    "ENERGIA" = @(
        "Painéis Solares", "Inversores e Controladores", "Baterias e Acumuladores",
        "Geradores de Energia", "Cabos e Conectores Elétricos", "Lâmpadas LED e Econômicas",
        "Nobreaks e Estabilizadores", "Disjuntores e Quadros Elétricos", "Iluminação Solar",
        "Medidores e Monitoramento"
    )
    "ESPORTES_E_FITNESS" = @(
        "Musculação e Halteres", "Esteiras e Bicicletas", "Bolas e Equipamentos Esportivos",
        "Roupas Esportivas", "Suplementos Alimentares", "Acessórios de Yoga e Pilates",
        "Equipamentos de Natação", "Luvas e Equipamentos de Luta", "Raquetes e Redes",
        "Monitores Cardíacos"
    )
    "GRAFICA_E_PERSONALIZADOS" = @(
        "Impressão Digital", "Camisetas Personalizadas", "Canetas e Brindes",
        "Adesivos e Banners", "Cartões de Visita", "Convites e Papelaria",
        "Chaveiros Personalizados", "Canecas e Copos", "Troféus e Medalhas",
        "Carimbos e Selos"
    )
    "BRINQUEDOS_E_INFANTIL" = @(
        "Bonecas e Pelúcias", "Carrinhos e Miniaturas", "Jogos de Tabuleiro",
        "Blocos de Montar", "Brinquedos Educativos", "Bicicletas e Patinetes Infantis",
        "Piscinas e Brinquedos Infláveis", "Fantasias e Acessórios", "Instrumentos Musicais Infantis",
        "Parquinhos e Playgrounds"
    )
    "ARTESANATO_E_HOBBIES" = @(
        "Tecidos e Aviamentos", "Tintas e Pincéis", "Papéis para Scrapbook",
        "Miçangas e Bijuterias", "Argila e Massas de Modelar", "Ferramentas para Artesanato",
        "Fios e Linhas", "Adesivos Decorativos", "Carimbos e Stencils",
        "Kits de Pintura"
    )
    "CASA_E_DECORACAO" = @(
        "Quadros e Molduras", "Vasos e Plantas Artificiais", "Cortinas e Persianas",
        "Tapetes e Carpetes", "Almofadas e Mantas", "Espelhos Decorativos",
        "Luminárias e Abajures", "Porta-retratos", "Relógios de Parede",
        "Objetos Decorativos"
    )
    "OUTROS" = @(
        "Serviços Diversos", "Produtos Importados", "Artigos Religiosos",
        "Produtos Promocionais", "Embalagens e Packaging", "Instrumentos de Medição",
        "Equipamentos Audiovisuais", "Produtos para Festas", "Artigos Funerários",
        "Itens Não Classificados"
    )
}

# Verificar servidor
Write-Host "🔌 Verificando conexão com servidor..." -ForegroundColor Yellow
try {
    $teste = Invoke-RestMethod -Uri "http://localhost:8080/api/subcategorias" -Method GET -TimeoutSec 5
    Write-Host "✅ Servidor online" -ForegroundColor Green
} catch {
    Write-Host "❌ ERRO: Servidor não está respondendo!" -ForegroundColor Red
    Write-Host "   Inicie o servidor primeiro!" -ForegroundColor Yellow
    Read-Host "Pressione Enter para sair"
    exit 1
}

Write-Host ""
Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host ("=" * 69) -ForegroundColor Cyan
Write-Host " INICIANDO INSERÇÃO" -ForegroundColor Yellow
Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host ("=" * 69) -ForegroundColor Cyan
Write-Host ""

$sucesso = 0
$falhas = 0
$idx = 0
$total = $subcategorias.Count

foreach ($categoria in $subcategorias.Keys) {
    $idx++
    Write-Host "[$idx/$total] 📁 Categoria: " -NoNewline -ForegroundColor Cyan
    Write-Host $categoria -ForegroundColor White
    Write-Host "    $($subcategorias[$categoria].Count) subcategorias" -ForegroundColor Gray
    
    $subIdx = 0
    foreach ($nome in $subcategorias[$categoria]) {
        $subIdx++
        
        $body = @{
            categoria = $categoria
            nome = $nome
            ativa = $true
        } | ConvertTo-Json -Compress
        
        try {
            $response = Invoke-RestMethod -Uri $API_URL -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
            Write-Host "    ✅ [$subIdx/10] $nome" -ForegroundColor Green
            $sucesso++
        } catch {
            Write-Host "    ❌ [$subIdx/10] $nome - Erro" -ForegroundColor Red
            $falhas++
        }
        
        Start-Sleep -Milliseconds 100
    }
    
    Write-Host ""
}

# Relatório
Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host ("=" * 69) -ForegroundColor Cyan
Write-Host " RELATÓRIO FINAL" -ForegroundColor Yellow
Write-Host "=" -NoNewline -ForegroundColor Cyan
Write-Host ("=" * 69) -ForegroundColor Cyan
Write-Host "✅ Inseridas com sucesso: $sucesso" -ForegroundColor Green
Write-Host "❌ Falhas: $falhas" -ForegroundColor Red
Write-Host ""

if ($falhas -eq 0) {
    Write-Host "🎉 TODAS AS SUBCATEGORIAS FORAM INSERIDAS COM SUCESSO!" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ As subcategorias estão salvas no banco H2 (DBLRadar.mv.db)" -ForegroundColor Green
    Write-Host "✅ Elas NÃO vão sumir após reiniciar" -ForegroundColor Green
}

Write-Host ""
Read-Host "Pressione Enter para sair"
