$API_URL = "http://localhost:8080/api/subcategorias"

$subs = @(
    @("INDUSTRIAL", "Ferramentas e Equipamentos"),
    @("INDUSTRIAL", "Maquinas Industriais"),
    @("INDUSTRIAL", "Componentes e Pecas"),
    @("INDUSTRIAL", "Parafusos e Fixadores"),
    @("INDUSTRIAL", "Soldagem e Corte"),
    @("INDUSTRIAL", "Equipamentos Hidraulicos"),
    @("INDUSTRIAL", "Equipamentos Pneumaticos"),
    @("INDUSTRIAL", "Rolamentos e Transmissao"),
    @("INDUSTRIAL", "Vedacao e Lubrificacao"),
    @("INDUSTRIAL", "Instrumentacao e Controle"),
    
    @("LIMPEZA_E_HIGIENE", "Produtos de Limpeza"),
    @("LIMPEZA_E_HIGIENE", "Material de Higiene"),
    @("LIMPEZA_E_HIGIENE", "Equipamentos de Limpeza"),
    @("LIMPEZA_E_HIGIENE", "Descartaveis"),
    @("LIMPEZA_E_HIGIENE", "Saneantes e Desinfetantes"),
    @("LIMPEZA_E_HIGIENE", "Panos e Esponjas"),
    @("LIMPEZA_E_HIGIENE", "Vassouras e Rodos"),
    @("LIMPEZA_E_HIGIENE", "Papel Higienico e Toalhas"),
    @("LIMPEZA_E_HIGIENE", "Sabonetes e Dispensers"),
    @("LIMPEZA_E_HIGIENE", "Lixeiras e Sacos de Lixo"),
    
    @("SEGURANCA", "EPIs Equipamentos de Protecao"),
    @("SEGURANCA", "Capacetes e Oculos de Protecao"),
    @("SEGURANCA", "Luvas de Seguranca"),
    @("SEGURANCA", "Botas e Calcados de Protecao"),
    @("SEGURANCA", "Cintos e Talabartes"),
    @("SEGURANCA", "Mascaras e Respiradores"),
    @("SEGURANCA", "Protetores Auriculares"),
    @("SEGURANCA", "Uniformes de Seguranca"),
    @("SEGURANCA", "Sinalizacao de Seguranca"),
    @("SEGURANCA", "Extintores e Equipamentos Contra Incendio"),
    
    @("PET", "Racao e Alimentacao"),
    @("PET", "Brinquedos para Pets"),
    @("PET", "Camas e Casinhas"),
    @("PET", "Coleiras e Guias"),
    @("PET", "Higiene e Banho"),
    @("PET", "Medicamentos Veterinarios"),
    @("PET", "Roupas para Pets"),
    @("PET", "Aquarismo"),
    @("PET", "Acessorios para Gaiolas"),
    @("PET", "Transporte de Animais"),
    
    @("ENERGIA", "Paineis Solares"),
    @("ENERGIA", "Inversores e Controladores"),
    @("ENERGIA", "Baterias e Acumuladores"),
    @("ENERGIA", "Geradores de Energia"),
    @("ENERGIA", "Cabos e Conectores Eletricos"),
    @("ENERGIA", "Lampadas LED e Economicas"),
    @("ENERGIA", "Nobreaks e Estabilizadores"),
    @("ENERGIA", "Disjuntores e Quadros Eletricos"),
    @("ENERGIA", "Iluminacao Solar"),
    @("ENERGIA", "Medidores e Monitoramento"),
    
    @("ESPORTES_E_FITNESS", "Musculacao e Halteres"),
    @("ESPORTES_E_FITNESS", "Esteiras e Bicicletas"),
    @("ESPORTES_E_FITNESS", "Bolas e Equipamentos Esportivos"),
    @("ESPORTES_E_FITNESS", "Roupas Esportivas"),
    @("ESPORTES_E_FITNESS", "Suplementos Alimentares"),
    @("ESPORTES_E_FITNESS", "Acessorios de Yoga e Pilates"),
    @("ESPORTES_E_FITNESS", "Equipamentos de Natacao"),
    @("ESPORTES_E_FITNESS", "Luvas e Equipamentos de Luta"),
    @("ESPORTES_E_FITNESS", "Raquetes e Redes"),
    @("ESPORTES_E_FITNESS", "Monitores Cardiacos"),
    
    @("GRAFICA_E_PERSONALIZADOS", "Impressao Digital"),
    @("GRAFICA_E_PERSONALIZADOS", "Camisetas Personalizadas"),
    @("GRAFICA_E_PERSONALIZADOS", "Canetas e Brindes"),
    @("GRAFICA_E_PERSONALIZADOS", "Adesivos e Banners"),
    @("GRAFICA_E_PERSONALIZADOS", "Cartoes de Visita"),
    @("GRAFICA_E_PERSONALIZADOS", "Convites e Papelaria"),
    @("GRAFICA_E_PERSONALIZADOS", "Chaveiros Personalizados"),
    @("GRAFICA_E_PERSONALIZADOS", "Canecas e Copos"),
    @("GRAFICA_E_PERSONALIZADOS", "Trofeus e Medalhas"),
    @("GRAFICA_E_PERSONALIZADOS", "Carimbos e Selos"),
    
    @("BRINQUEDOS_E_INFANTIL", "Bonecas e Pelucias"),
    @("BRINQUEDOS_E_INFANTIL", "Carrinhos e Miniaturas"),
    @("BRINQUEDOS_E_INFANTIL", "Jogos de Tabuleiro"),
    @("BRINQUEDOS_E_INFANTIL", "Blocos de Montar"),
    @("BRINQUEDOS_E_INFANTIL", "Brinquedos Educativos"),
    @("BRINQUEDOS_E_INFANTIL", "Bicicletas e Patinetes Infantis"),
    @("BRINQUEDOS_E_INFANTIL", "Piscinas e Brinquedos Inflaveis"),
    @("BRINQUEDOS_E_INFANTIL", "Fantasias e Acessorios"),
    @("BRINQUEDOS_E_INFANTIL", "Instrumentos Musicais Infantis"),
    @("BRINQUEDOS_E_INFANTIL", "Parquinhos e Playgrounds"),
    
    @("ARTESANATO_E_HOBBIES", "Tecidos e Aviamentos"),
    @("ARTESANATO_E_HOBBIES", "Tintas e Pinceis"),
    @("ARTESANATO_E_HOBBIES", "Papeis para Scrapbook"),
    @("ARTESANATO_E_HOBBIES", "Micangas e Bijuterias"),
    @("ARTESANATO_E_HOBBIES", "Argila e Massas de Modelar"),
    @("ARTESANATO_E_HOBBIES", "Ferramentas para Artesanato"),
    @("ARTESANATO_E_HOBBIES", "Fios e Linhas"),
    @("ARTESANATO_E_HOBBIES", "Adesivos Decorativos"),
    @("ARTESANATO_E_HOBBIES", "Carimbos e Stencils"),
    @("ARTESANATO_E_HOBBIES", "Kits de Pintura"),
    
    @("CASA_E_DECORACAO", "Quadros e Molduras"),
    @("CASA_E_DECORACAO", "Vasos e Plantas Artificiais"),
    @("CASA_E_DECORACAO", "Cortinas e Persianas"),
    @("CASA_E_DECORACAO", "Tapetes e Carpetes"),
    @("CASA_E_DECORACAO", "Almofadas e Mantas"),
    @("CASA_E_DECORACAO", "Espelhos Decorativos"),
    @("CASA_E_DECORACAO", "Luminarias e Abajures"),
    @("CASA_E_DECORACAO", "Porta-retratos"),
    @("CASA_E_DECORACAO", "Relogios de Parede"),
    @("CASA_E_DECORACAO", "Objetos Decorativos"),
    
    @("OUTROS", "Servicos Diversos"),
    @("OUTROS", "Produtos Importados"),
    @("OUTROS", "Artigos Religiosos"),
    @("OUTROS", "Produtos Promocionais"),
    @("OUTROS", "Embalagens e Packaging"),
    @("OUTROS", "Instrumentos de Medicao"),
    @("OUTROS", "Equipamentos Audiovisuais"),
    @("OUTROS", "Produtos para Festas"),
    @("OUTROS", "Artigos Funerarios"),
    @("OUTROS", "Itens Nao Classificados")
)

Write-Host "Inserindo 110 subcategorias..." -ForegroundColor Cyan
$ok = 0
$erro = 0

foreach ($s in $subs) {
    $body = @{ categoria = $s[0]; nome = $s[1]; ativa = $true } | ConvertTo-Json
    try {
        $null = Invoke-RestMethod -Uri $API_URL -Method POST -Body $body -ContentType "application/json" -TimeoutSec 10
        $ok++
        Write-Host "." -NoNewline -ForegroundColor Green
    } catch {
        $erro++
        Write-Host "X" -NoNewline -ForegroundColor Red
    }
    if ($ok % 10 -eq 0) { Write-Host "" }
}

Write-Host "`n`nConcluido: $ok inseridas, $erro falhas" -ForegroundColor Yellow
