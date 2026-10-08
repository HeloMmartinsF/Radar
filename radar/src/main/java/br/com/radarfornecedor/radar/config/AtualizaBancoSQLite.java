package br.com.radarfornecedor.radar.config;

import java.util.regex.Pattern;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AtualizaBancoSQLite implements ApplicationRunner {

    private static final Pattern IDENTIFICADOR_SEGURO = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private final JdbcTemplate jdbcTemplate;

    public AtualizaBancoSQLite(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("[MIGRATIONS] Verificando subcategorias...");
        inserirSubcategoriasSeNecessario();
    }

    private void inserirSubcategoriasSeNecessario() {
        // Mapa: código do enum -> array de [nome, descrição]
        Object[][][] categorias = {

            // ── AUTOMOTIVO ──────────────────────────────────────────────
            { {"AUTOMOTIVO"}, {"Peças e Acessórios","Peças originais e acessórios para veículos"},
              {"AUTOMOTIVO"}, {"Pneus e Rodas","Pneus, rodas e câmaras para veículos"},
              {"AUTOMOTIVO"}, {"Suspensão e Direção","Amortecedores, molas e componentes de direção"},
              {"AUTOMOTIVO"}, {"Freios","Pastilhas, discos, tambores e fluido de freio"},
              {"AUTOMOTIVO"}, {"Motor e Transmissão","Motores, câmbios, embreagens e componentes"},
              {"AUTOMOTIVO"}, {"Elétrica Automotiva","Baterias, alternadores, velas e sistemas elétricos"},
              {"AUTOMOTIVO"}, {"Iluminação Automotiva","Faróis, lanternas e LEDs automotivos"},
              {"AUTOMOTIVO"}, {"Carroceria e Lataria","Para-choques, capôs, portas e lataria"},
              {"AUTOMOTIVO"}, {"Vidros e Borrachas","Para-brisas, vidros laterais e vedações"},
              {"AUTOMOTIVO"}, {"Tintas e Pintura","Tintas automotivas, primers e produtos de pintura"},
              {"AUTOMOTIVO"}, {"Ar-condicionado Auto","Compressores, filtros e componentes de A/C"},
              {"AUTOMOTIVO"}, {"Lubrificantes e Fluidos","Óleos, graxas e fluidos automotivos"},
              {"AUTOMOTIVO"}, {"Som e Multimídia","Rádios, alto-falantes e centrais multimídia"},
              {"AUTOMOTIVO"}, {"Escapamento","Silenciosos, catalisadores e tubulações"},
              {"AUTOMOTIVO"}, {"Ferramentas Automotivas","Ferramentas e equipamentos para manutenção veicular"} }
        };

        // Insere subcategoria a subcategoria usando JDBC direto
        String[][] subs = {
            // AUTOMOTIVO
            {"AUTOMOTIVO","Peças e Acessórios","Peças originais e acessórios para veículos"},
            {"AUTOMOTIVO","Pneus e Rodas","Pneus, rodas e câmaras para veículos"},
            {"AUTOMOTIVO","Suspensão e Direção","Amortecedores, molas e componentes de direção"},
            {"AUTOMOTIVO","Freios","Pastilhas, discos, tambores e fluido de freio"},
            {"AUTOMOTIVO","Motor e Transmissão","Motores, câmbios, embreagens e componentes internos"},
            {"AUTOMOTIVO","Elétrica Automotiva","Baterias, alternadores, velas e sistemas elétricos"},
            {"AUTOMOTIVO","Iluminação Automotiva","Faróis, lanternas e LEDs automotivos"},
            {"AUTOMOTIVO","Carroceria e Lataria","Para-choques, capôs, portas e peças de lataria"},
            {"AUTOMOTIVO","Vidros e Borrachas","Para-brisas, vidros laterais e vedações"},
            {"AUTOMOTIVO","Tintas e Pintura","Tintas automotivas, primers e produtos de pintura"},
            {"AUTOMOTIVO","Ar-condicionado Auto","Compressores, filtros e componentes de A/C"},
            {"AUTOMOTIVO","Lubrificantes e Fluidos","Óleos, graxas e fluidos automotivos"},
            {"AUTOMOTIVO","Som e Multimídia","Rádios, alto-falantes e centrais multimídia"},
            {"AUTOMOTIVO","Escapamento","Silenciosos, catalisadores e tubulações"},
            {"AUTOMOTIVO","Ferramentas Automotivas","Ferramentas para manutenção veicular"},

            // MOTOCICLETAS
            {"MOTOCICLETAS","Peças para Moto","Peças originais e compatíveis para motos"},
            {"MOTOCICLETAS","Pneus de Moto","Pneus e câmaras para motocicletas"},
            {"MOTOCICLETAS","Capacetes","Capacetes integrais, abertos e off-road"},
            {"MOTOCICLETAS","Equipamentos de Proteção","Jaquetas, luvas, joelheiras e proteções"},
            {"MOTOCICLETAS","Escapamentos de Moto","Escapamentos esportivos e originais"},
            {"MOTOCICLETAS","Elétrica de Moto","Baterias, faróis e componentes elétricos"},
            {"MOTOCICLETAS","Freios de Moto","Pastilhas, discos e cabos de freio"},
            {"MOTOCICLETAS","Acessórios para Moto","Alforjes, suportes, retrovisor e personalização"},
            {"MOTOCICLETAS","Lubrificantes Moto","Óleos e fluidos para motocicletas"},
            {"MOTOCICLETAS","Suspensão de Moto","Amortecedores e molas para motos"},

            // MODA_E_TEXTIL
            {"MODA_E_TEXTIL","Camisetas e Blusas","Camisetas femininas, masculinas e infantis"},
            {"MODA_E_TEXTIL","Calças e Bermudas","Jeans, sociais, esportivos e shorts"},
            {"MODA_E_TEXTIL","Vestidos e Saias","Vestidos para todas as ocasiões"},
            {"MODA_E_TEXTIL","Casacos e Jaquetas","Casacos, blazers e jaquetas"},
            {"MODA_E_TEXTIL","Calçados","Sapatos, tênis, sandálias e botas"},
            {"MODA_E_TEXTIL","Acessórios Moda","Bolsas, cintos, chapéus e bijuterias"},
            {"MODA_E_TEXTIL","Tecidos e Aviamentos","Tecidos, linhas, botões e zíperes"},
            {"MODA_E_TEXTIL","Moda Praia","Biquínis, sungas, saídas de praia"},
            {"MODA_E_TEXTIL","Moda Fitness","Legging, tops, camisetas esportivas"},
            {"MODA_E_TEXTIL","Uniformes","Uniformes profissionais e escolares"},
            {"MODA_E_TEXTIL","Roupas Infantis","Roupas e acessórios para crianças"},
            {"MODA_E_TEXTIL","Lingerie","Roupas íntimas femininas e masculinas"},

            // EMBALAGENS
            {"EMBALAGENS","Caixas de Papelão","Caixas para transporte e presente"},
            {"EMBALAGENS","Sacolas e Sacos","Sacolas plásticas, papel e ecobags"},
            {"EMBALAGENS","Embalagens Plásticas","Potes, bandejas, filmes e blisters"},
            {"EMBALAGENS","Embalagens de Vidro","Frascos, potes e garrafas de vidro"},
            {"EMBALAGENS","Embalagens Metálicas","Latas, tampas e embalagens de alumínio"},
            {"EMBALAGENS","Fitas e Lacres","Fitas adesivas, lacres e seladores"},
            {"EMBALAGENS","Embalagens Térmicas","Caixas térmicas e embalagens para frios"},
            {"EMBALAGENS","Etiquetas e Rótulos","Etiquetas adesivas e rótulos personalizados"},
            {"EMBALAGENS","Embalagens para Alimentos","Embalagens food-grade e marmitex"},
            {"EMBALAGENS","Espumas e Proteção","Espumas, bolhas e materiais de proteção"},

            // TECNOLOGIA
            {"TECNOLOGIA","Notebooks e PCs","Computadores portáteis e desktops"},
            {"TECNOLOGIA","Smartphones e Tablets","Celulares e tablets"},
            {"TECNOLOGIA","Periféricos","Teclados, mouses, monitores e webcams"},
            {"TECNOLOGIA","Componentes de Hardware","Placas, memórias, SSDs e processadores"},
            {"TECNOLOGIA","Áudio e Vídeo","Fones, caixas de som, câmeras e projetores"},
            {"TECNOLOGIA","Redes e Conectividade","Roteadores, switches, cabos e Wi-Fi"},
            {"TECNOLOGIA","Impressoras e Suprimentos","Impressoras, cartuchos e papel"},
            {"TECNOLOGIA","Softwares e Licenças","Licenças de software e aplicativos"},
            {"TECNOLOGIA","Gadgets e Wearables","Smartwatches, rastreadores e acessórios tech"},
            {"TECNOLOGIA","Segurança Eletrônica","Câmeras, alarmes e controle de acesso"},
            {"TECNOLOGIA","Baterias e Carregadores","Power banks, carregadores e adaptadores"},

            // CONSTRUCAO
            {"CONSTRUCAO","Materiais de Construção","Cimento, areia, tijolos e blocos"},
            {"CONSTRUCAO","Pisos e Revestimentos","Cerâmica, porcelanato, laminado e vinílico"},
            {"CONSTRUCAO","Tintas e Vernizes","Tintas imobiliárias, vernizes e seladores"},
            {"CONSTRUCAO","Hidráulica","Tubos, conexões, torneiras e caixas d'água"},
            {"CONSTRUCAO","Esquadrias","Portas, janelas, portões e grades"},
            {"CONSTRUCAO","Telhados e Coberturas","Telhas, calhas e estruturas metálicas"},
            {"CONSTRUCAO","Iluminação Construção","Lâmpadas, luminárias e fios elétricos"},
            {"CONSTRUCAO","Ferragens e Fixação","Parafusos, buchas, dobradiças e fechaduras"},
            {"CONSTRUCAO","Impermeabilização","Mantas, resinas e produtos impermeabilizantes"},
            {"CONSTRUCAO","Gesso e Drywall","Placas, perfis e acessórios para drywall"},
            {"CONSTRUCAO","Paisagismo","Pedras decorativas, grama e jardins"},

            // MOVEIS_E_ESCRITORIO
            {"MOVEIS_E_ESCRITORIO","Sofás e Poltronas","Sofás de sala, poltronas e chaises"},
            {"MOVEIS_E_ESCRITORIO","Mesas e Cadeiras","Mesas de jantar, centro e cadeiras"},
            {"MOVEIS_E_ESCRITORIO","Móveis para Quarto","Camas, guarda-roupas e cômodas"},
            {"MOVEIS_E_ESCRITORIO","Móveis para Escritório","Mesas de escritório, cadeiras ergonômicas"},
            {"MOVEIS_E_ESCRITORIO","Armários e Estantes","Armários de cozinha, banheiro e estantes"},
            {"MOVEIS_E_ESCRITORIO","Colchões e Travesseiros","Colchões, travesseiros e protetores"},
            {"MOVEIS_E_ESCRITORIO","Móveis Infantis","Berços, camas e mesas infantis"},
            {"MOVEIS_E_ESCRITORIO","Papelaria e Escritório","Canetas, cadernos, grampeadores e clips"},
            {"MOVEIS_E_ESCRITORIO","Organização","Caixas organizadoras, prateleiras e racks"},

            // ELETRICA_E_ELETRONICA
            {"ELETRICA_E_ELETRONICA","Fios e Cabos Elétricos","Fios, cabos e mangueiras elétricas"},
            {"ELETRICA_E_ELETRONICA","Disjuntores e Quadros","Disjuntores, quadros de distribuição e DPS"},
            {"ELETRICA_E_ELETRONICA","Tomadas e Interruptores","Tomadas, interruptores e espelhos"},
            {"ELETRICA_E_ELETRONICA","Iluminação Elétrica","Lâmpadas LED, luminárias e spots"},
            {"ELETRICA_E_ELETRONICA","Motores e Bombas","Motores elétricos e bombas d'água"},
            {"ELETRICA_E_ELETRONICA","Transformadores e Estabilizadores","Transformadores e nobreaks"},
            {"ELETRICA_E_ELETRONICA","Componentes Eletrônicos","Resistores, capacitores, transistores"},
            {"ELETRICA_E_ELETRONICA","Automação Residencial","Módulos smart home e controladores"},
            {"ELETRICA_E_ELETRONICA","Placas e Circuitos","Arduino, Raspberry Pi e protoboards"},

            // FERRAMENTAS_E_EQUIPAMENTOS
            {"FERRAMENTAS_E_EQUIPAMENTOS","Ferramentas Manuais","Chaves, martelos, alicates e serras"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Ferramentas Elétricas","Furadeiras, serras, lixadeiras e parafusadeiras"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Equipamentos de Medição","Paquímetros, nível, trenas e multímetros"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Soldagem","Máquinas de solda, eletrodos e acessórios"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","EPI – Proteção Individual","Capacetes, óculos, luvas e botas de segurança"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Equipamentos para Jardim","Roçadeiras, podadeiras e regadores"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Compressores e Pneumática","Compressores de ar e ferramentas pneumáticas"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Elevação e Movimentação","Paleteiras, guinchos e macaquinhos"},
            {"FERRAMENTAS_E_EQUIPAMENTOS","Equipamentos de Limpeza","Lavadoras de pressão e aspiradores industriais"},

            // BELEZA_E_COSMETICOS
            {"BELEZA_E_COSMETICOS","Skin Care","Cremes, séruns, hidratantes e protetores solares"},
            {"BELEZA_E_COSMETICOS","Maquiagem","Bases, batons, paletas e pincéis"},
            {"BELEZA_E_COSMETICOS","Cabelos","Shampoos, condicionadores, coloração e finalizadores"},
            {"BELEZA_E_COSMETICOS","Perfumes","Perfumes femininos, masculinos e unissex"},
            {"BELEZA_E_COSMETICOS","Higiene Pessoal","Sabonetes, desodorantes e produtos de banho"},
            {"BELEZA_E_COSMETICOS","Depilação","Ceras, lâminas e cremes depilatórios"},
            {"BELEZA_E_COSMETICOS","Manicure e Pedicure","Esmaltes, removedores e kits de unhas"},
            {"BELEZA_E_COSMETICOS","Equipamentos Estéticos","Dermaroller, luz LED e aparelhos de beleza"},
            {"BELEZA_E_COSMETICOS","Suplementos Beleza","Colágeno, vitaminas e suplementos para pele"},

            // SAUDE_E_HOSPITALAR
            {"SAUDE_E_HOSPITALAR","Medicamentos OTC","Vitaminas, analgésicos e suplementos sem receita"},
            {"SAUDE_E_HOSPITALAR","Equipamentos Médicos","Esfigmomanômetros, oxímetros e termômetros"},
            {"SAUDE_E_HOSPITALAR","Descartáveis Hospitalares","Seringas, luvas, máscaras e curativos"},
            {"SAUDE_E_HOSPITALAR","Ortopedia e Reabilitação","Órteses, muletas, bengalas e cadeiras de rodas"},
            {"SAUDE_E_HOSPITALAR","Diagnóstico","Glicosímetros, tiras reagentes e testes rápidos"},
            {"SAUDE_E_HOSPITALAR","Saúde Bucal","Escovas, fios dentais e enxaguantes"},
            {"SAUDE_E_HOSPITALAR","Nutrição Clínica","Fórmulas nutricionais e suplementos clínicos"},
            {"SAUDE_E_HOSPITALAR","Mobiliário Hospitalar","Camas hospitalares, macas e cadeiras de exame"},

            // ALIMENTOS_E_BEBIDAS
            {"ALIMENTOS_E_BEBIDAS","Grãos e Cereais","Arroz, feijão, milho, quinoa e aveia"},
            {"ALIMENTOS_E_BEBIDAS","Massas e Farinhas","Macarrão, farinha de trigo e fubá"},
            {"ALIMENTOS_E_BEBIDAS","Laticínios","Leite, queijos, iogurtes e manteiga"},
            {"ALIMENTOS_E_BEBIDAS","Carnes e Proteínas","Bovinos, suínos, aves, peixes e ovos"},
            {"ALIMENTOS_E_BEBIDAS","Frutas e Verduras","Frutas frescas, legumes e verduras"},
            {"ALIMENTOS_E_BEBIDAS","Bebidas","Sucos, refrigerantes, águas e energéticos"},
            {"ALIMENTOS_E_BEBIDAS","Bebidas Alcoólicas","Cervejas, vinhos, destilados e licores"},
            {"ALIMENTOS_E_BEBIDAS","Doces e Chocolates","Chocolates, balas, gomas e sobremesas"},
            {"ALIMENTOS_E_BEBIDAS","Condimentos e Temperos","Sal, pimenta, molhos e temperos prontos"},
            {"ALIMENTOS_E_BEBIDAS","Congelados e Resfriados","Pizzas, pratos prontos e sorvetes"},
            {"ALIMENTOS_E_BEBIDAS","Orgânicos e Naturais","Produtos orgânicos, naturais e sem aditivos"},
            {"ALIMENTOS_E_BEBIDAS","Panificação","Pães, bolos, biscoitos e confeitaria"},

            // AGRONEGOCIO
            {"AGRONEGOCIO","Sementes e Mudas","Sementes para plantio e mudas diversas"},
            {"AGRONEGOCIO","Fertilizantes e Corretivos","Adubos, calcário e fertilizantes foliar"},
            {"AGRONEGOCIO","Defensivos Agrícolas","Herbicidas, fungicidas e inseticidas"},
            {"AGRONEGOCIO","Irrigação","Sistemas de irrigação, mangueiras e gotejadores"},
            {"AGRONEGOCIO","Máquinas Agrícolas","Tratores, colhedoras e implementos"},
            {"AGRONEGOCIO","Nutrição Animal","Rações, suplementos e minerais para animais"},
            {"AGRONEGOCIO","Veterinária","Vacinas, medicamentos e equipamentos veterinários"},
            {"AGRONEGOCIO","Silos e Armazenagem","Silos, bags e sistemas de armazenagem"},
            {"AGRONEGOCIO","Aquicultura e Pesca","Redes, equipamentos e insumos para aquicultura"},

            // LIMPEZA_E_HIGIENE
            {"LIMPEZA_E_HIGIENE","Produtos de Limpeza","Detergentes, desinfetantes e multiuso"},
            {"LIMPEZA_E_HIGIENE","Limpeza Industrial","Produtos para limpeza industrial e desincrustação"},
            {"LIMPEZA_E_HIGIENE","Higiene para Banheiro","Papel higiênico, sabonete e álcool gel"},
            {"LIMPEZA_E_HIGIENE","Limpeza de Piso","Ceras, seladores e limpadores de piso"},
            {"LIMPEZA_E_HIGIENE","Lavanderia","Sabão em pó, amaciante e tira-manchas"},
            {"LIMPEZA_E_HIGIENE","Equipamentos de Limpeza","Vassouras, rodos, mops e aspiradores"},
            {"LIMPEZA_E_HIGIENE","Descartáveis de Higiene","Luvas, mascaras, toucas e protetores"},
            {"LIMPEZA_E_HIGIENE","Aromatizantes","Sprays, difusores e sachês aromáticos"},

            // PET
            {"PET","Ração e Petiscos","Rações e petiscos para cães, gatos e outros"},
            {"PET","Acessórios Pet","Coleiras, guias, roupinhas e camas"},
            {"PET","Higiene e Banho Pet","Shampoos, escovas e produtos de higiene"},
            {"PET","Brinquedos Pet","Bolas, mordedores e brinquedos interativos"},
            {"PET","Saúde Animal","Antipulgas, vermífugos e suplementos"},
            {"PET","Aquários e Peixes","Aquários, filtros, rações e acessórios"},
            {"PET","Pássaros","Gaiolas, rações e acessórios para aves"},
            {"PET","Transporte Pet","Caixas de transporte e bolsas para pets"},

            // ESPORTES_E_FITNESS
            {"ESPORTES_E_FITNESS","Academia e Musculação","Halteres, barras, anilhas e suportes"},
            {"ESPORTES_E_FITNESS","Ciclismo","Bikes, capacetes, luvas e acessórios"},
            {"ESPORTES_E_FITNESS","Corrida e Caminhada","Tênis, meias e acessórios para corrida"},
            {"ESPORTES_E_FITNESS","Natação","Óculos, maiôs, pranchas e nadadeiras"},
            {"ESPORTES_E_FITNESS","Esportes Coletivos","Bolas, redes e equipamentos para futebol/vôlei"},
            {"ESPORTES_E_FITNESS","Artes Marciais","Quimonos, luvas de boxe e proteções"},
            {"ESPORTES_E_FITNESS","Yoga e Pilates","Tapetes, blocos, alças e acessórios"},
            {"ESPORTES_E_FITNESS","Suplementos Esportivos","Whey, creatina, BCAA e pré-treinos"},
            {"ESPORTES_E_FITNESS","Esportes Radicais","Equipamentos para skate, surf e escalada"},
            {"ESPORTES_E_FITNESS","Camping e Trilha","Barracas, mochilas, lanternas e utensílios"},

            // BRINQUEDOS_E_INFANTIL
            {"BRINQUEDOS_E_INFANTIL","Brinquedos Educativos","Quebra-cabeças, blocos e jogos pedagógicos"},
            {"BRINQUEDOS_E_INFANTIL","Bonecos e Bonecas","Action figures, bonecas e acessórios"},
            {"BRINQUEDOS_E_INFANTIL","Carrinhos e Veículos","Carrinhos, pistas e miniaturas"},
            {"BRINQUEDOS_E_INFANTIL","Games e Eletrônicos Infantis","Videogames, tablets e games educativos"},
            {"BRINQUEDOS_E_INFANTIL","Parque e Lazer","Escorregadores, balanços e piscinas de bolinha"},
            {"BRINQUEDOS_E_INFANTIL","Pelúcias e Fantasias","Pelúcias, fantasias e roupas de festa"},
            {"BRINQUEDOS_E_INFANTIL","Arte e Pintura Infantil","Tinta, massinha, giz e canetinhas"},
            {"BRINQUEDOS_E_INFANTIL","Bebê","Fraldas, chupetas, mamadeiras e itens para bebê"},
            {"BRINQUEDOS_E_INFANTIL","Mochilas e Material Escolar","Mochilas, lancheiras e material escolar"},

            // CASA_E_DECORACAO
            {"CASA_E_DECORACAO","Decoração de Sala","Quadros, almofadas, tapetes e vasos"},
            {"CASA_E_DECORACAO","Cama, Mesa e Banho","Lençóis, toalhas, jogos de cama e mesa"},
            {"CASA_E_DECORACAO","Utensílios de Cozinha","Panelas, talheres, utensílios e acessórios"},
            {"CASA_E_DECORACAO","Eletrodomésticos","Geladeiras, fogões, micro-ondas e lavadoras"},
            {"CASA_E_DECORACAO","Jardim e Varanda","Vasos, plantas, ferramentas e mobiliário externo"},
            {"CASA_E_DECORACAO","Iluminação Decorativa","Luminárias, pendentes e abajures"},
            {"CASA_E_DECORACAO","Artes e Quadros","Quadros, esculturas e objetos decorativos"},
            {"CASA_E_DECORACAO","Cortinas e Persianas","Cortinas, persianas, barras e trilhos"},
            {"CASA_E_DECORACAO","Organizadores e Armazenamento","Cestos, caixas e prateleiras organizadoras"},

            // INDUSTRIAL
            {"INDUSTRIAL","Máquinas Industriais","Tornos, fresadoras, prensas e injetoras"},
            {"INDUSTRIAL","Manutenção Industrial","Rolamentos, correias, retentores e polias"},
            {"INDUSTRIAL","Embalagens Industriais","Bombonas, IBC, tambores e sacarias"},
            {"INDUSTRIAL","Pneumática e Hidráulica","Cilindros, válvulas, mangueiras e conexões"},
            {"INDUSTRIAL","Instrumentação","Sensores, transdutores e controladores PID"},
            {"INDUSTRIAL","EPIs Industriais","Capacetes, botinas, protetores e macacões"},
            {"INDUSTRIAL","Movimentação de Cargas","Empilhadeiras, talhas, paletes e esteiras"},
            {"INDUSTRIAL","Química Industrial","Ácidos, solventes, tintas industriais e graxas"},
            {"INDUSTRIAL","Soldagem Industrial","Máquinas MIG, TIG, eletrodos e consumíveis"},

            // SEGURANCA
            {"SEGURANCA","Câmeras de Segurança","DVRs, câmeras IP, CFTV e acessórios"},
            {"SEGURANCA","Alarmes e Sensores","Centrais de alarme, sensores e sirenes"},
            {"SEGURANCA","Controle de Acesso","Catracas, leitores biométricos e fechaduras eletrônicas"},
            {"SEGURANCA","Rastreamento Veicular","Rastreadores GPS e sistemas de monitoramento"},
            {"SEGURANCA","Segurança Patrimonial","Cercas elétricas, concertinas e portões"},
            {"SEGURANCA","Extintores e Combate a Incêndio","Extintores, sprinklers e detectores de fumaça"},
            {"SEGURANCA","EPIs de Segurança","Coletes, capacetes, luvas e óculos de proteção"},
            {"SEGURANCA","Cofres e Arquivos","Cofres eletrônicos, gavetas de segurança"},

            // ENERGIA
            {"ENERGIA","Energia Solar","Painéis fotovoltaicos, inversores e kits solares"},
            {"ENERGIA","Baterias e Armazenamento","Baterias estacionárias, lítio e UPS"},
            {"ENERGIA","Geradores","Geradores a diesel, gasolina e gás"},
            {"ENERGIA","Elétrica Industrial","Transformadores, motores e quadros elétricos"},
            {"ENERGIA","Energia Eólica","Aerogeradores e acessórios para energia eólica"},
            {"ENERGIA","Iluminação Eficiente","Luminárias LED, sensores e automação de luz"},
            {"ENERGIA","Medição de Energia","Medidores, analisadores de energia e telemetria"},

            // GRAFICA_E_PERSONALIZADOS
            {"GRAFICA_E_PERSONALIZADOS","Impressão Gráfica","Flyers, banners, cartazes e folders"},
            {"GRAFICA_E_PERSONALIZADOS","Brindes Personalizados","Canetas, canecas, camisetas e chaveiros"},
            {"GRAFICA_E_PERSONALIZADOS","Embalagens Personalizadas","Caixas, sacolas e tags com logomarca"},
            {"GRAFICA_E_PERSONALIZADOS","Sinalização","Placas, totens, letreiros e banners"},
            {"GRAFICA_E_PERSONALIZADOS","Sublimação","Produtos para sublimação e transfer"},
            {"GRAFICA_E_PERSONALIZADOS","Insumos Gráficos","Papéis especiais, tintas e filmes"},
            {"GRAFICA_E_PERSONALIZADOS","Bordado e Silk","Bordados, silk screen e estamparia"},

            // ARTESANATO_E_HOBBIES
            {"ARTESANATO_E_HOBBIES","Tecidos e Costura","Tecidos, linhas, agulhas e aviamentos"},
            {"ARTESANATO_E_HOBBIES","Pintura Artística","Tintas, pincéis, telas e cavaletes"},
            {"ARTESANATO_E_HOBBIES","Papelaria Criativa","Papéis especiais, carimbos e scrapbook"},
            {"ARTESANATO_E_HOBBIES","Modelagem","Argila, biscuit, resina e moldes"},
            {"ARTESANATO_E_HOBBIES","Bijuteria e Joalheria","Fios, pedras, peças e ferramentas de joalheria"},
            {"ARTESANATO_E_HOBBIES","Crochê e Tricô","Fios, agulhas e moldes para crochê/tricô"},
            {"ARTESANATO_E_HOBBIES","Marcenaria e Madeira","Madeiras, ferramentas e acabamentos"},
            {"ARTESANATO_E_HOBBIES","Colecionismo","Miniaturas, cards, selos e itens colecionáveis"},

            // OUTROS
            {"OUTROS","Produtos Gerais","Produtos que não se encaixam nas demais categorias"},
            {"OUTROS","Serviços","Prestação de serviços diversos"},
            {"OUTROS","Importados","Produtos importados variados"},
            {"OUTROS","Promoções","Produtos em oferta e liquidação"}
        };

        int inseridos = 0;
        int pulados = 0;
        for (String[] sub : subs) {
            String categoria = sub[0];
            String nome      = sub[1];
            String descricao = sub[2];
            try {
                Integer existe = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM Subcategorias WHERE nome = ? AND categoria = ?",
                    Integer.class, nome, categoria);
                if (existe == null || existe == 0) {
                    jdbcTemplate.update(
                        "INSERT INTO Subcategorias (nome, descricao, categoria, ativa) VALUES (?, ?, ?, TRUE)",
                        nome, descricao, categoria);
                    inseridos++;
                } else {
                    pulados++;
                }
            } catch (Exception e) {
                System.err.println("[SUBCATS] Erro ao inserir '" + nome + "' [" + categoria + "]: " + e.getMessage());
            }
        }
        System.out.println("[SUBCATS] " + inseridos + " subcategorias inseridas, " + pulados + " já existiam.");
    }

    private void validarIdentificador(String identificador) {
        if (!IDENTIFICADOR_SEGURO.matcher(identificador).matches()) {
            throw new IllegalArgumentException("Identificador SQL invalido: " + identificador);
        }
    }
}
