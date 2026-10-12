package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.AlertaPreco;
import br.com.radarfornecedor.radar.model.AlertaPreco.CriterioAlerta;
import br.com.radarfornecedor.radar.model.HistoricoPreco;
import br.com.radarfornecedor.radar.model.HistoricoPreco.OrigemPreco;
import br.com.radarfornecedor.radar.model.Produto;
import br.com.radarfornecedor.radar.model.TipoNotificacao;
import br.com.radarfornecedor.radar.repository.AlertaPrecoRepository;
import br.com.radarfornecedor.radar.repository.HistoricoPrecoRepository;
import br.com.radarfornecedor.radar.repository.ProdutoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gerenciamento de Alertas de Preço e Histórico de Preços.
 * 
 * Funcionalidades:
 * - CRUD de alertas de preço
 * - Verificação automática de preços (job scheduled)
 * - Registro de histórico de preços
 * - Notificações quando preço-alvo é atingido
 * - Análise de variações de preço
 */
@Service
public class AlertaPrecoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertaPrecoService.class);
    
    @Autowired
    private AlertaPrecoRepository alertaPrecoRepository;
    
    @Autowired
    private HistoricoPrecoRepository historicoPrecoRepository;
    
    @Autowired
    private ProdutoRepository produtoRepository;
    
    @Autowired
    private NotificacaoService notificacaoService;
    
    // ==================== CRUD DE ALERTAS ====================
    
    /**
     * Cria um novo alerta de preço.
     */
    @Transactional
    public AlertaPreco criarAlerta(Long usuarioId, Long produtoId, BigDecimal precoAlvo, CriterioAlerta criterio, String notas) {
        // Validações
        if (usuarioId == null || produtoId == null || precoAlvo == null) {
            throw new IllegalArgumentException("UsuarioId, produtoId e precoAlvo são obrigatórios");
        }
        
        if (precoAlvo.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Preço-alvo deve ser maior que zero");
        }
        
        // Verificar se produto existe
        Produto produto = produtoRepository.findById(produtoId)
            .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        
        // Verificar limite de alertas por usuário (máximo 50)
        long totalAlertas = alertaPrecoRepository.countByUsuarioIdAndAtivoTrue(usuarioId);
        if (totalAlertas >= 50) {
            throw new IllegalArgumentException("Limite de alertas ativos atingido (máximo: 50)");
        }
        
        AlertaPreco alerta = new AlertaPreco();
        alerta.setUsuarioId(usuarioId);
        alerta.setProdutoId(produtoId);
        alerta.setPrecoAlvo(precoAlvo);
        alerta.setCriterio(criterio != null ? criterio : CriterioAlerta.MENOR_OU_IGUAL);
        alerta.setAtivo(true);
        alerta.setNotas(notas);
        alerta.setCriadoEm(LocalDateTime.now());
        
        try {
            return alertaPrecoRepository.save(alerta);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Erro ao criar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Lista todos os alertas ativos de um usuário.
     */
    public List<Map<String, Object>> listarAlertasAtivos(Long usuarioId) {
        List<AlertaPreco> alertas = alertaPrecoRepository.findByUsuarioIdAndAtivoTrueOrderByCriadoEmDesc(usuarioId);
        return enriquecerAlertasComProduto(alertas);
    }
    
    /**
     * Lista todos os alertas de um usuário (ativos e inativos).
     */
    public List<Map<String, Object>> listarTodosAlertas(Long usuarioId) {
        List<AlertaPreco> alertas = alertaPrecoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
        return enriquecerAlertasComProduto(alertas);
    }
    
    /**
     * Busca alerta por ID (com validação de propriedade).
     */
    public AlertaPreco buscarAlerta(Long alertaId, Long usuarioId) {
        return alertaPrecoRepository.findByIdAndUsuarioId(alertaId, usuarioId)
            .orElseThrow(() -> new IllegalArgumentException("Alerta não encontrado"));
    }
    
    /**
     * Atualiza um alerta existente.
     */
    @Transactional
    public AlertaPreco atualizarAlerta(Long alertaId, Long usuarioId, BigDecimal precoAlvo, CriterioAlerta criterio, Boolean ativo, String notas) {
        AlertaPreco alerta = buscarAlerta(alertaId, usuarioId);
        
        if (precoAlvo != null) {
            if (precoAlvo.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Preço-alvo deve ser maior que zero");
            }
            alerta.setPrecoAlvo(precoAlvo);
        }
        
        if (criterio != null) {
            alerta.setCriterio(criterio);
        }
        
        if (ativo != null) {
            alerta.setAtivo(ativo);
        }
        
        if (notas != null) {
            alerta.setNotas(notas);
        }
        
        return alertaPrecoRepository.save(alerta);
    }
    
    /**
     * Desativa um alerta (soft delete).
     */
    @Transactional
    public void desativarAlerta(Long alertaId, Long usuarioId) {
        AlertaPreco alerta = buscarAlerta(alertaId, usuarioId);
        alerta.setAtivo(false);
        alertaPrecoRepository.save(alerta);
    }
    
    /**
     * Deleta um alerta permanentemente.
     */
    @Transactional
    public void deletarAlerta(Long alertaId, Long usuarioId) {
        AlertaPreco alerta = buscarAlerta(alertaId, usuarioId);
        alertaPrecoRepository.delete(alerta);
    }
    
    /**
     * Verifica se usuário tem alerta ativo para um produto.
     */
    public boolean temAlertaAtivo(Long usuarioId, Long produtoId) {
        return alertaPrecoRepository.existsByUsuarioIdAndProdutoIdAndAtivoTrue(usuarioId, produtoId);
    }
    
    /**
     * Conta alertas ativos do usuário.
     */
    public long contarAlertasAtivos(Long usuarioId) {
        return alertaPrecoRepository.countByUsuarioIdAndAtivoTrue(usuarioId);
    }
    
    // ==================== HISTÓRICO DE PREÇOS ====================
    
    /**
     * Registra preço atual de um produto no histórico.
     */
    @Transactional
    public HistoricoPreco registrarPreco(Long produtoId, BigDecimal preco, BigDecimal precoPromocional, OrigemPreco origem, Long publicacaoId) {
        // Buscar último registro para calcular variação
        Optional<HistoricoPreco> ultimoOpt = historicoPrecoRepository.findFirstByProdutoIdOrderByRegistradoEmDesc(produtoId);
        
        HistoricoPreco historico = new HistoricoPreco();
        historico.setProdutoId(produtoId);
        historico.setPreco(preco);
        historico.setPrecoPromocional(precoPromocional);
        historico.setOrigem(origem);
        historico.setPublicacaoId(publicacaoId);
        historico.setRegistradoEm(LocalDateTime.now());
        
        // Calcular variação percentual
        if (ultimoOpt.isPresent()) {
            BigDecimal precoAnterior = ultimoOpt.get().getPreco();
            if (precoAnterior.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal variacao = preco.subtract(precoAnterior)
                    .divide(precoAnterior, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
                historico.setVariacaoPercentual(variacao);
            }
        }
        
        return historicoPrecoRepository.save(historico);
    }
    
    /**
     * Busca histórico de preços de um produto.
     */
    public List<HistoricoPreco> buscarHistorico(Long produtoId, Integer limite) {
        if (limite != null && limite > 0) {
            return historicoPrecoRepository.findTop10ByProdutoIdOrderByRegistradoEmDesc(produtoId)
                .stream()
                .limit(limite)
                .collect(Collectors.toList());
        }
        return historicoPrecoRepository.findByProdutoIdOrderByRegistradoEmDesc(produtoId);
    }
    
    /**
     * Busca reduções de preço de um produto.
     */
    public List<HistoricoPreco> buscarReducoes(Long produtoId) {
        return historicoPrecoRepository.findReducoesPrecoPorProduto(produtoId);
    }
    
    // ==================== JOB SCHEDULED ====================
    
    /**
     * Job que executa a cada 1 hora para verificar alertas de preço.
     * Verifica todos os alertas ativos e dispara notificações quando necessário.
     */
    @Scheduled(cron = "0 0 */1 * * *") // A cada 1 hora
    @Transactional
    public void verificarAlertasDePreco() {
        logger.info("Iniciando verificação de alertas de preço...");
        
        try {
            List<AlertaPreco> alertasAtivos = alertaPrecoRepository.findByAtivoTrue();
            logger.info("Encontrados {} alertas ativos para verificação", alertasAtivos.size());
            
            int alertasDisparados = 0;
            
            for (AlertaPreco alerta : alertasAtivos) {
                try {
                    boolean disparado = verificarEDispararAlerta(alerta);
                    if (disparado) {
                        alertasDisparados++;
                    }
                } catch (Exception e) {
                    logger.error("Erro ao verificar alerta {}: {}", alerta.getId(), e.getMessage());
                }
            }
            
            logger.info("Verificação concluída. {} alertas disparados", alertasDisparados);
            
        } catch (Exception e) {
            logger.error("Erro geral na verificação de alertas: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Verifica um alerta específico e dispara notificação se necessário.
     */
    @Transactional
    protected boolean verificarEDispararAlerta(AlertaPreco alerta) {
        // Buscar produto
        Optional<Produto> produtoOpt = produtoRepository.findById(alerta.getProdutoId());
        if (produtoOpt.isEmpty()) {
            logger.warn("Produto {} não encontrado para alerta {}", alerta.getProdutoId(), alerta.getId());
            return false;
        }
        
        Produto produto = produtoOpt.get();
        BigDecimal precoAtual = produto.getPreco();
        
        if (precoAtual == null || precoAtual.compareTo(BigDecimal.ZERO) == 0) {
            logger.debug("Produto {} sem preço definido", produto.getId());
            alertaPrecoRepository.atualizarUltimaVerificacao(alerta.getId(), LocalDateTime.now());
            return false;
        }
        
        // Verificar se preço atingiu o alvo
        boolean atingiu = verificarCriterio(precoAtual, alerta.getPrecoAlvo(), alerta.getCriterio());
        
        alertaPrecoRepository.atualizarUltimaVerificacao(alerta.getId(), LocalDateTime.now());
        
        if (atingiu) {
            // Marcar alerta como disparado
            alertaPrecoRepository.marcarComoDisparado(alerta.getId(), LocalDateTime.now(), precoAtual);
            
            // Enviar notificação
            String mensagem = String.format(
                "O preço do produto '%s' atingiu R$ %.2f (seu alerta: R$ %.2f)",
                produto.getNome(),
                precoAtual,
                alerta.getPrecoAlvo()
            );
            
            notificacaoService.criarNotificacao(
                alerta.getUsuarioId(),
                TipoNotificacao.ALERTA_PRECO_ATINGIDO,
                mensagem,
                "/pages/produtos.html?id=" + produto.getId()
            );
            
            logger.info("Alerta {} disparado para usuário {}. Produto: {}, Preço: {}",
                alerta.getId(), alerta.getUsuarioId(), produto.getNome(), precoAtual);
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Verifica se o preço atual atende ao critério do alerta.
     */
    private boolean verificarCriterio(BigDecimal precoAtual, BigDecimal precoAlvo, CriterioAlerta criterio) {
        switch (criterio) {
            case MENOR_OU_IGUAL:
                return precoAtual.compareTo(precoAlvo) <= 0;
            case MENOR:
                return precoAtual.compareTo(precoAlvo) < 0;
            case IGUAL:
                // Tolerância de 0.01 para igualdade
                return precoAtual.subtract(precoAlvo).abs().compareTo(new BigDecimal("0.01")) <= 0;
            default:
                return false;
        }
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Enriquece lista de alertas com dados do produto.
     */
    private List<Map<String, Object>> enriquecerAlertasComProduto(List<AlertaPreco> alertas) {
        if (alertas.isEmpty()) {
            return List.of();
        }
        
        // Buscar produtos em batch
        Set<Long> produtoIds = alertas.stream()
            .map(AlertaPreco::getProdutoId)
            .collect(Collectors.toSet());
        
        Map<Long, Produto> produtosMap = produtoRepository.findAllById(produtoIds)
            .stream()
            .collect(Collectors.toMap(Produto::getId, p -> p));
        
        // Montar DTOs enriquecidos
        return alertas.stream()
            .map(alerta -> {
                Map<String, Object> dto = new HashMap<>();
                dto.put("id", alerta.getId());
                dto.put("produtoId", alerta.getProdutoId());
                dto.put("precoAlvo", alerta.getPrecoAlvo());
                dto.put("criterio", alerta.getCriterio());
                dto.put("ativo", alerta.getAtivo());
                dto.put("criadoEm", alerta.getCriadoEm());
                dto.put("ultimaVerificacao", alerta.getUltimaVerificacao());
                dto.put("disparadoEm", alerta.getDisparadoEm());
                dto.put("precoDisparado", alerta.getPrecoDisparado());
                dto.put("notas", alerta.getNotas());
                
                // Adicionar dados do produto
                Produto produto = produtosMap.get(alerta.getProdutoId());
                if (produto != null) {
                    Map<String, Object> produtoDto = new HashMap<>();
                    produtoDto.put("id", produto.getId());
                    produtoDto.put("nome", produto.getNome());
                    produtoDto.put("preco", produto.getPreco());
                    produtoDto.put("categoria", produto.getCategoria());
                    dto.put("produto", produtoDto);
                    
                    // Calcular diferença de preço
                    if (produto.getPreco() != null) {
                        BigDecimal diferenca = alerta.getPrecoAlvo().subtract(produto.getPreco());
                        dto.put("diferencaPreco", diferenca);
                        dto.put("precoAbaixoAlvo", diferenca.compareTo(BigDecimal.ZERO) > 0);
                    }
                }
                
                return dto;
            })
            .collect(Collectors.toList());
    }
}
