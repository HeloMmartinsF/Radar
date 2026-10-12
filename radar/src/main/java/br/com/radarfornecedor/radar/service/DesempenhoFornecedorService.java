package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.dto.*;
import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DesempenhoFornecedorService {

    private final PublicacaoRepository publicacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final EventoPublicacaoRepository eventoPublicacaoRepository;
    private final EventoProdutoRepository eventoProdutoRepository;
    private final CurtidaPublicacaoRepository curtidaPublicacaoRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DesempenhoFornecedorService(
            PublicacaoRepository publicacaoRepository,
            ProdutoRepository produtoRepository,
            EventoPublicacaoRepository eventoPublicacaoRepository,
            EventoProdutoRepository eventoProdutoRepository,
            CurtidaPublicacaoRepository curtidaPublicacaoRepository) {
        this.publicacaoRepository = publicacaoRepository;
        this.produtoRepository = produtoRepository;
        this.eventoPublicacaoRepository = eventoPublicacaoRepository;
        this.eventoProdutoRepository = eventoProdutoRepository;
        this.curtidaPublicacaoRepository = curtidaPublicacaoRepository;
    }

    /**
     * Calcula todos os indicadores de desempenho do fornecedor
     */
    @Transactional(readOnly = true)
    public IndicadoresDesempenhoDTO calcularIndicadores(Long fornecedorId, Integer diasHistorico) {
        if (diasHistorico == null || diasHistorico <= 0) {
            diasHistorico = 30; // Padrão: últimos 30 dias
        }

        IndicadoresDesempenhoDTO indicadores = new IndicadoresDesempenhoDTO();

        // Totais gerais
        indicadores.setTotalPublicacoes(
            publicacaoRepository.countByFornecedorId(fornecedorId)
        );

        indicadores.setTotalVisualizacoesPublicacoes(
            eventoPublicacaoRepository.countTotalVisualizacoes(fornecedorId)
        );

        indicadores.setTotalCurtidas(
            calcularTotalCurtidas(fornecedorId)
        );

        indicadores.setTotalCliquesContato(
            eventoPublicacaoRepository.countTotalContatos(fornecedorId)
        );

        indicadores.setTotalVisualizacoesProdutos(
            eventoProdutoRepository.countByFornecedorId(fornecedorId)
        );

        // Top performers
        indicadores.setPublicacoesMaisVisualizadas(
            calcularPublicacoesMaisVisualizadas(fornecedorId, 5)
        );

        indicadores.setPublicacoesMaisInteragidas(
            calcularPublicacoesMaisInteragidas(fornecedorId, 5)
        );

        indicadores.setProdutosMaisVisualizados(
            calcularProdutosMaisVisualizados(fornecedorId, 5)
        );

        // Evolução temporal
        LocalDateTime dataInicio = LocalDateTime.now().minusDays(diasHistorico);

        indicadores.setEvolucaoVisualizacoes(
            calcularEvolucaoVisualizacoes(fornecedorId, dataInicio)
        );

        indicadores.setEvolucaoCurtidas(
            calcularEvolucaoCurtidas(fornecedorId, dataInicio)
        );

        indicadores.setEvolucaoContatos(
            calcularEvolucaoContatos(fornecedorId, dataInicio)
        );

        return indicadores;
    }

    /**
     * Registra visualização de publicação (com deduplicação)
     */
    @Transactional
    public boolean registrarVisualizacaoPublicacao(Long publicacaoId, Long usuarioId, String ipUsuario) {
        Publicacao publicacao = publicacaoRepository.findById(publicacaoId)
            .orElseThrow(() -> new RuntimeException("Publicação não encontrada"));

        // Verificar se já existe visualização recente (últimas 24h)
        LocalDateTime limiteData = LocalDateTime.now().minusHours(24);
        boolean jaVisualizou = eventoPublicacaoRepository.existsEventoRecente(
            publicacaoId, TipoEventoPublicacao.VIEW, usuarioId, ipUsuario, limiteData
        );

        if (jaVisualizou) {
            return false; // Já contabilizado recentemente
        }

        // Registrar evento
        EventoPublicacao evento = new EventoPublicacao();
        evento.setPublicacaoId(publicacaoId);
        evento.setFornecedorId(publicacao.getFornecedorId());
        evento.setTipoEvento(TipoEventoPublicacao.VIEW);
        evento.setUsuarioId(usuarioId);
        evento.setIpUsuario(ipUsuario);
        eventoPublicacaoRepository.save(evento);

        return true;
    }

    /**
     * Registra clique em contato
     */
    @Transactional
    public boolean registrarCliqueContato(Long publicacaoId, Long usuarioId, String ipUsuario) {
        Publicacao publicacao = publicacaoRepository.findById(publicacaoId)
            .orElseThrow(() -> new RuntimeException("Publicação não encontrada"));

        // Registrar evento de contato (não deduplicar contatos, cada clique é intencional)
        EventoPublicacao evento = new EventoPublicacao();
        evento.setPublicacaoId(publicacaoId);
        evento.setFornecedorId(publicacao.getFornecedorId());
        evento.setTipoEvento(TipoEventoPublicacao.CONTATO);
        evento.setUsuarioId(usuarioId);
        evento.setIpUsuario(ipUsuario);
        eventoPublicacaoRepository.save(evento);

        return true;
    }

    /**
     * Registra visualização de produto (com deduplicação)
     */
    @Transactional
    public boolean registrarVisualizacaoProduto(Long produtoId, Long usuarioId, String ipUsuario) {
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        if (produto.getFornecedorId() == null) {
            return false; // Produto sem fornecedor associado
        }

        // Verificar se já existe visualização recente (últimas 24h)
        LocalDateTime limiteData = LocalDateTime.now().minusHours(24);
        boolean jaVisualizou = eventoProdutoRepository.existsVisualizacaoRecente(
            produtoId, usuarioId, ipUsuario, limiteData
        );

        if (jaVisualizou) {
            return false; // Já contabilizado recentemente
        }

        // Registrar evento
        EventoProduto evento = new EventoProduto();
        evento.setProdutoId(produtoId);
        evento.setFornecedorId(produto.getFornecedorId());
        evento.setUsuarioId(usuarioId);
        evento.setIpUsuario(ipUsuario);
        eventoProdutoRepository.save(evento);

        return true;
    }

    // === MÉTODOS AUXILIARES ===

    private Long calcularTotalCurtidas(Long fornecedorId) {
        List<Publicacao> publicacoes = publicacaoRepository.findByFornecedorId(fornecedorId);
        return publicacoes.stream()
            .mapToLong(p -> p.getTotalCurtidas() != null ? p.getTotalCurtidas() : 0)
            .sum();
    }

    private List<PublicacaoComMetricasDTO> calcularPublicacoesMaisVisualizadas(Long fornecedorId, int limit) {
        List<Map<String, Object>> resultado = eventoPublicacaoRepository.findPublicacoesMaisVisualizadas(fornecedorId);
        
        return resultado.stream()
            .limit(limit)
            .map(map -> {
                Long publicacaoId = ((Number) map.get("publicacaoId")).longValue();
                Long visualizacoes = ((Number) map.get("total")).longValue();
                
                return publicacaoRepository.findById(publicacaoId)
                    .map(pub -> montarPublicacaoComMetricas(pub, visualizacoes))
                    .orElse(null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<PublicacaoComMetricasDTO> calcularPublicacoesMaisInteragidas(Long fornecedorId, int limit) {
        List<Publicacao> publicacoes = publicacaoRepository.findByFornecedorId(fornecedorId);
        
        return publicacoes.stream()
            .map(pub -> {
                Long visualizacoes = eventoPublicacaoRepository.countByPublicacaoIdAndTipoEvento(
                    pub.getId(), TipoEventoPublicacao.VIEW
                );
                return montarPublicacaoComMetricas(pub, visualizacoes);
            })
            .sorted((a, b) -> Long.compare(b.getTotalInteracoes(), a.getTotalInteracoes()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private PublicacaoComMetricasDTO montarPublicacaoComMetricas(Publicacao pub, Long visualizacoes) {
        PublicacaoComMetricasDTO dto = new PublicacaoComMetricasDTO();
        dto.setId(pub.getId());
        dto.setTitulo(pub.getTitulo());
        
        // Truncar descrição
        if (pub.getDescricao() != null && pub.getDescricao().length() > 100) {
            dto.setDescricao(pub.getDescricao().substring(0, 100) + "...");
        } else {
            dto.setDescricao(pub.getDescricao());
        }
        
        // Primeira imagem
        if (pub.getImagens() != null && !pub.getImagens().isEmpty()) {
            String[] imagens = pub.getImagens().split(",");
            dto.setPrimeiraImagem(imagens[0].trim());
        }
        
        dto.setVisualizacoes(visualizacoes != null ? visualizacoes : 0L);
        dto.setCurtidas(pub.getTotalCurtidas() != null ? pub.getTotalCurtidas().longValue() : 0L);
        
        Long cliquesContato = eventoPublicacaoRepository.countByPublicacaoIdAndTipoEvento(
            pub.getId(), TipoEventoPublicacao.CONTATO
        );
        dto.setCliquesContato(cliquesContato != null ? cliquesContato : 0L);
        
        dto.setTotalInteracoes(dto.getCurtidas() + dto.getCliquesContato());
        
        return dto;
    }

    private List<ProdutoComMetricasDTO> calcularProdutosMaisVisualizados(Long fornecedorId, int limit) {
        List<Map<String, Object>> resultado = eventoProdutoRepository.findProdutosMaisVisualizados(fornecedorId);
        
        return resultado.stream()
            .limit(limit)
            .map(map -> {
                Long produtoId = ((Number) map.get("produtoId")).longValue();
                Long visualizacoes = ((Number) map.get("total")).longValue();
                
                return produtoRepository.findById(produtoId)
                    .map(prod -> {
                        ProdutoComMetricasDTO dto = new ProdutoComMetricasDTO();
                        dto.setId(prod.getId());
                        dto.setNome(prod.getNome());
                        dto.setDescricao(prod.getDescricao());
                        dto.setCategoria(prod.getCategoria() != null ? prod.getCategoria().toString() : null);
                        dto.setVisualizacoes(visualizacoes);
                        return dto;
                    })
                    .orElse(null);
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    private List<EvolucaoDiariaDTO> calcularEvolucaoVisualizacoes(Long fornecedorId, LocalDateTime dataInicio) {
        List<Map<String, Object>> resultado = eventoPublicacaoRepository.findEvolucaoPorDia(
            fornecedorId, TipoEventoPublicacao.VIEW, dataInicio
        );
        
        return resultado.stream()
            .map(map -> {
                Object dataObj = map.get("data");
                Long total = ((Number) map.get("total")).longValue();
                
                String dataStr = dataObj != null ? dataObj.toString() : LocalDate.now().toString();
                return new EvolucaoDiariaDTO(dataStr, total);
            })
            .collect(Collectors.toList());
    }

    private List<EvolucaoDiariaDTO> calcularEvolucaoCurtidas(Long fornecedorId, LocalDateTime dataInicio) {
        // Buscar curtidas agrupadas por data
        List<Publicacao> publicacoes = publicacaoRepository.findByFornecedorId(fornecedorId);
        List<Long> publicacaoIds = publicacoes.stream().map(Publicacao::getId).collect(Collectors.toList());
        
        if (publicacaoIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Buscar curtidas e agrupar manualmente por data
        Map<String, Long> curtidasPorDia = new HashMap<>();
        for (Long pubId : publicacaoIds) {
            List<CurtidaPublicacao> curtidas = curtidaPublicacaoRepository.findByPublicacaoId(pubId);
            for (CurtidaPublicacao curtida : curtidas) {
                if (curtida.getCriadaEm().isAfter(dataInicio)) {
                    String data = curtida.getCriadaEm().toLocalDate().format(DATE_FORMATTER);
                    curtidasPorDia.merge(data, 1L, Long::sum);
                }
            }
        }
        
        return curtidasPorDia.entrySet().stream()
            .map(e -> new EvolucaoDiariaDTO(e.getKey(), e.getValue()))
            .sorted(Comparator.comparing(EvolucaoDiariaDTO::getData))
            .collect(Collectors.toList());
    }

    private List<EvolucaoDiariaDTO> calcularEvolucaoContatos(Long fornecedorId, LocalDateTime dataInicio) {
        List<Map<String, Object>> resultado = eventoPublicacaoRepository.findEvolucaoPorDia(
            fornecedorId, TipoEventoPublicacao.CONTATO, dataInicio
        );
        
        return resultado.stream()
            .map(map -> {
                Object dataObj = map.get("data");
                Long total = ((Number) map.get("total")).longValue();
                
                String dataStr = dataObj != null ? dataObj.toString() : LocalDate.now().toString();
                return new EvolucaoDiariaDTO(dataStr, total);
            })
            .collect(Collectors.toList());
    }
}
