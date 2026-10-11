package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.AvaliacaoFornecedor;
import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.repository.AvaliacaoFornecedorRepository;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * AvaliacaoFornecedorService - Lógica de negócio para avaliações de fornecedores
 * 
 * Responsabilidades:
 * - Validar regras de negócio (não auto-avaliar, não duplicar, etc)
 * - Calcular estatísticas (média, total, distribuição)
 * - Gerenciar respostas do fornecedor
 * - Retornar dados enriquecidos para o frontend
 */
@Service
public class AvaliacaoFornecedorService {

    private final AvaliacaoFornecedorRepository avaliacaoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final UsuarioRepository usuarioRepository;

    public AvaliacaoFornecedorService(
            AvaliacaoFornecedorRepository avaliacaoRepository,
            FornecedorRepository fornecedorRepository,
            UsuarioRepository usuarioRepository) {
        this.avaliacaoRepository = avaliacaoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ========================================
    // LISTAR AVALIAÇÕES
    // ========================================

    /**
     * Lista todas as avaliações de um fornecedor com dados enriquecidos
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista de avaliações com nome do avaliador
     */
    public List<Map<String, Object>> listarAvaliacoes(Long fornecedorId) {
        List<AvaliacaoFornecedor> avaliacoes = avaliacaoRepository.findByFornecedorIdOrderByCriadoEmDesc(fornecedorId);
        
        return avaliacoes.stream().map(this::mapearAvaliacaoParaDTO).toList();
    }

    /**
     * Mapeia AvaliacaoFornecedor para DTO com dados do avaliador
     */
    private Map<String, Object> mapearAvaliacaoParaDTO(AvaliacaoFornecedor avaliacao) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", avaliacao.getId());
        dto.put("nota", avaliacao.getNota());
        dto.put("comentario", avaliacao.getComentario());
        dto.put("respostaFornecedor", avaliacao.getRespostaFornecedor());
        dto.put("dataResposta", avaliacao.getDataResposta());
        dto.put("criadoEm", avaliacao.getCriadoEm());
        dto.put("temResposta", avaliacao.temResposta());
        
        // Buscar nome do avaliador
        Optional<Usuario> avaliador = usuarioRepository.findById(avaliacao.getAvaliadorUsuarioId());
        if (avaliador.isPresent()) {
            dto.put("nomeAvaliador", avaliador.get().getUsername());
            dto.put("tipoAvaliador", avaliador.get().getTipo().getDescricao());
        } else {
            dto.put("nomeAvaliador", "Usuário removido");
            dto.put("tipoAvaliador", "Desconhecido");
        }
        
        return dto;
    }

    // ========================================
    // ESTATÍSTICAS
    // ========================================

    /**
     * Calcula estatísticas completas de avaliações de um fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @return Map com média, total, distribuição por estrelas
     */
    public Map<String, Object> calcularEstatisticas(Long fornecedorId) {
        Map<String, Object> stats = new HashMap<>();
        
        // Média de notas
        Double media = avaliacaoRepository.calcularMediaNotas(fornecedorId);
        stats.put("mediaNotas", media != null ? Math.round(media * 10.0) / 10.0 : 0.0);
        
        // Total de avaliações
        Long total = avaliacaoRepository.countByFornecedorId(fornecedorId);
        stats.put("totalAvaliacoes", total);
        
        // Distribuição por estrelas (1 a 5)
        Map<Integer, Long> distribuicao = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            Long count = avaliacaoRepository.countByFornecedorIdAndNota(fornecedorId, i);
            distribuicao.put(i, count);
        }
        stats.put("distribuicao", distribuicao);
        
        // Percentual de cada nota
        if (total > 0) {
            Map<Integer, Double> percentuais = new HashMap<>();
            for (int i = 1; i <= 5; i++) {
                double percentual = (distribuicao.get(i) * 100.0) / total;
                percentuais.put(i, Math.round(percentual * 10.0) / 10.0);
            }
            stats.put("percentuais", percentuais);
        }
        
        return stats;
    }

    /**
     * Retorna a média de avaliações de um fornecedor
     */
    public Double obterMediaAvaliacoes(Long fornecedorId) {
        Double media = avaliacaoRepository.calcularMediaNotas(fornecedorId);
        return media != null ? Math.round(media * 10.0) / 10.0 : 0.0;
    }

    /**
     * Retorna o total de avaliações de um fornecedor
     */
    public Long contarAvaliacoes(Long fornecedorId) {
        return avaliacaoRepository.countByFornecedorId(fornecedorId);
    }

    // ========================================
    // ADICIONAR AVALIAÇÃO
    // ========================================

    /**
     * Adiciona uma nova avaliação de fornecedor
     * 
     * Validações:
     * - Fornecedor existe
     * - Usuário não é o próprio fornecedor (não pode auto-avaliar)
     * - Usuário não avaliou anteriormente (evita duplicatas)
     * - Nota entre 1 e 5
     * 
     * @param avaliacao Avaliação a ser criada
     * @param usuarioLogado Usuário que está avaliando
     * @return Avaliação criada
     * @throws IllegalArgumentException se validação falhar
     */
    @Transactional
    public AvaliacaoFornecedor adicionarAvaliacao(AvaliacaoFornecedor avaliacao, Usuario usuarioLogado) {
        // Validação 1: Fornecedor existe?
        Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findById(avaliacao.getFornecedorId());
        if (fornecedorOpt.isEmpty()) {
            throw new IllegalArgumentException("Fornecedor não encontrado");
        }
        Fornecedor fornecedor = fornecedorOpt.get();
        
        // Validação 2: Usuário não pode avaliar a própria empresa
        if (isProprioFornecedor(usuarioLogado, fornecedor)) {
            throw new IllegalArgumentException("Você não pode avaliar a própria empresa");
        }
        
        // Validação 3: Usuário já avaliou este fornecedor?
        boolean jaAvaliou = avaliacaoRepository.existsByFornecedorIdAndAvaliadorUsuarioId(
            avaliacao.getFornecedorId(), 
            usuarioLogado.getId()
        );
        if (jaAvaliou) {
            throw new IllegalArgumentException("Você já avaliou este fornecedor anteriormente");
        }
        
        // Validação 4: Nota válida?
        if (avaliacao.getNota() == null || avaliacao.getNota() < 1 || avaliacao.getNota() > 5) {
            throw new IllegalArgumentException("Nota deve estar entre 1 e 5");
        }
        
        // Limitar tamanho do comentário
        if (avaliacao.getComentario() != null && avaliacao.getComentario().length() > 500) {
            throw new IllegalArgumentException("Comentário não pode exceder 500 caracteres");
        }
        
        // Definir avaliador
        avaliacao.setAvaliadorUsuarioId(usuarioLogado.getId());
        
        // Salvar
        return avaliacaoRepository.save(avaliacao);
    }

    /**
     * Verifica se o usuário logado é o próprio fornecedor sendo avaliado
     */
    private boolean isProprioFornecedor(Usuario usuario, Fornecedor fornecedor) {
        // Verifica se o usuário tem vínculo com este fornecedor
        // Assumindo que existe um campo cnpjOuCpf no Usuario que vincula ao Fornecedor
        if (usuario.getCnpjOuCpf() != null && fornecedor.getCnpj() != null) {
            return usuario.getCnpjOuCpf().equals(fornecedor.getCnpj());
        }
        return false;
    }

    // ========================================
    // RESPONDER AVALIAÇÃO
    // ========================================

    /**
     * Permite que o fornecedor responda a uma avaliação
     * 
     * Validações:
     * - Avaliação existe
     * - Usuário logado é o fornecedor avaliado
     * - Resposta não excede 500 caracteres
     * 
     * @param avaliacaoId ID da avaliação
     * @param resposta Texto da resposta
     * @param usuarioLogado Usuário que está respondendo
     * @return Avaliação atualizada
     * @throws IllegalArgumentException se validação falhar
     */
    @Transactional
    public AvaliacaoFornecedor responderAvaliacao(Long avaliacaoId, String resposta, Usuario usuarioLogado) {
        // Validação 1: Avaliação existe?
        Optional<AvaliacaoFornecedor> avaliacaoOpt = avaliacaoRepository.findById(avaliacaoId);
        if (avaliacaoOpt.isEmpty()) {
            throw new IllegalArgumentException("Avaliação não encontrada");
        }
        AvaliacaoFornecedor avaliacao = avaliacaoOpt.get();
        
        // Validação 2: Fornecedor existe?
        Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findById(avaliacao.getFornecedorId());
        if (fornecedorOpt.isEmpty()) {
            throw new IllegalArgumentException("Fornecedor não encontrado");
        }
        Fornecedor fornecedor = fornecedorOpt.get();
        
        // Validação 3: Usuário logado é o fornecedor avaliado?
        if (!isProprioFornecedor(usuarioLogado, fornecedor)) {
            throw new IllegalArgumentException("Apenas o fornecedor avaliado pode responder");
        }
        
        // Validação 4: Resposta não excede limite?
        if (resposta != null && resposta.length() > 500) {
            throw new IllegalArgumentException("Resposta não pode exceder 500 caracteres");
        }
        
        // Atualizar resposta
        avaliacao.setRespostaFornecedor(resposta);
        avaliacao.setDataResposta(LocalDateTime.now());
        
        return avaliacaoRepository.save(avaliacao);
    }

    // ========================================
    // VERIFICAÇÕES
    // ========================================

    /**
     * Verifica se um usuário já avaliou determinado fornecedor
     */
    public boolean usuarioJaAvaliou(Long fornecedorId, Long usuarioId) {
        return avaliacaoRepository.existsByFornecedorIdAndAvaliadorUsuarioId(fornecedorId, usuarioId);
    }

    /**
     * Busca avaliação específica de um usuário para um fornecedor
     */
    public Optional<AvaliacaoFornecedor> buscarAvaliacaoDoUsuario(Long fornecedorId, Long usuarioId) {
        return avaliacaoRepository.findByFornecedorIdAndAvaliadorUsuarioId(fornecedorId, usuarioId);
    }

    /**
     * Lista avaliações que ainda não foram respondidas pelo fornecedor
     */
    public List<Map<String, Object>> listarAvaliacoesPendentes(Long fornecedorId) {
        List<AvaliacaoFornecedor> pendentes = avaliacaoRepository.findAvaliacoesSemResposta(fornecedorId);
        return pendentes.stream().map(this::mapearAvaliacaoParaDTO).toList();
    }

    /**
     * Lista todas as avaliações feitas por um usuário
     */
    public List<Map<String, Object>> listarAvaliacoesDoUsuario(Long usuarioId) {
        List<AvaliacaoFornecedor> avaliacoes = avaliacaoRepository.findByAvaliadorUsuarioIdOrderByCriadoEmDesc(usuarioId);
        return avaliacoes.stream().map(avaliacao -> {
            Map<String, Object> dto = mapearAvaliacaoParaDTO(avaliacao);
            
            // Adicionar nome do fornecedor avaliado
            Optional<Fornecedor> fornecedor = fornecedorRepository.findById(avaliacao.getFornecedorId());
            if (fornecedor.isPresent()) {
                dto.put("nomeFornecedor", fornecedor.get().getNome());
                dto.put("cnpjFornecedor", fornecedor.get().getCnpj());
            }
            
            return dto;
        }).toList();
    }
}
