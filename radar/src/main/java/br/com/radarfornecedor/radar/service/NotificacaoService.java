package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.Notificacao;
import br.com.radarfornecedor.radar.model.TipoNotificacao;
import br.com.radarfornecedor.radar.repository.NotificacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service para gerenciamento de notificações dos usuários
 * Responsável por criar, listar e marcar notificações como lidas
 */
@Service
public class NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;

    public NotificacaoService(NotificacaoRepository notificacaoRepository) {
        this.notificacaoRepository = notificacaoRepository;
    }

    /**
     * Cria uma nova notificação com deduplicação automática
     * Se já existir notificação para o mesmo evento, não cria duplicada
     *
     * @param usuarioId ID do usuário que receberá a notificação
     * @param tipo Tipo da notificação
     * @param titulo Título da notificação
     * @param mensagem Mensagem detalhada
     * @param linkDestino Link para onde a notificação direciona
     * @param eventoTipo Tipo do evento (para deduplicação)
     * @param eventoId ID do evento (para deduplicação)
     * @param origemNome Nome do usuário/fornecedor que originou o evento
     * @return Notificação criada ou null se já existir
     */
    @Transactional
    public Notificacao criarNotificacao(
            Long usuarioId,
            TipoNotificacao tipo,
            String titulo,
            String mensagem,
            String linkDestino,
            String eventoTipo,
            Long eventoId,
            String origemNome) {

        // Deduplicação: verifica se já existe notificação para este evento
        if (eventoTipo != null && eventoId != null) {
            boolean jaExiste = notificacaoRepository.existsByUsuarioIdAndEventoTipoAndEventoId(
                usuarioId, eventoTipo, eventoId
            );

            if (jaExiste) {
                return null; // Não cria notificação duplicada
            }
        }

        // Cria nova notificação
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setLinkDestino(linkDestino);
        notificacao.setEventoTipo(eventoTipo);
        notificacao.setEventoId(eventoId);
        notificacao.setOrigemNome(origemNome);
        notificacao.setLida(false);

        return notificacaoRepository.save(notificacao);
    }

    /**
     * Versão simplificada sem deduplicação (para notificações do sistema)
     */
    @Transactional
    public Notificacao criarNotificacao(
            Long usuarioId,
            TipoNotificacao tipo,
            String titulo,
            String mensagem) {
        return criarNotificacao(usuarioId, tipo, titulo, mensagem, null, null, null, null);
    }

    /**
     * Lista todas as notificações de um usuário (ordenadas por data, mais recentes primeiro)
     */
    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoes(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
    }

    /**
     * Lista apenas as N notificações mais recentes de um usuário
     * Útil para o dropdown de notificações (últimas 5)
     */
    @Transactional(readOnly = true)
    public List<Notificacao> listarUltimasNotificacoes(Long usuarioId, int limite) {
        List<Notificacao> todas = notificacaoRepository.findByUsuarioIdOrderByCriadaEmDesc(usuarioId);
        
        if (todas.size() <= limite) {
            return todas;
        }
        
        return todas.subList(0, limite);
    }

    /**
     * Lista apenas notificações não lidas de um usuário
     */
    @Transactional(readOnly = true)
    public List<Notificacao> listarNotificacoesNaoLidas(Long usuarioId) {
        return notificacaoRepository.findByUsuarioIdAndLidaFalseOrderByCriadaEmDesc(usuarioId);
    }

    /**
     * Conta total de notificações não lidas (para badge)
     */
    @Transactional(readOnly = true)
    public Long contarNaoLidas(Long usuarioId) {
        return notificacaoRepository.countByUsuarioIdAndLidaFalse(usuarioId);
    }

    /**
     * Busca uma notificação específica
     * Valida se a notificação pertence ao usuário (autorização)
     */
    @Transactional(readOnly = true)
    public Optional<Notificacao> buscarNotificacao(Long notificacaoId, Long usuarioId) {
        return notificacaoRepository.findByIdAndUsuarioId(notificacaoId, usuarioId);
    }

    /**
     * Marca uma notificação como lida
     * Valida se a notificação pertence ao usuário (autorização)
     *
     * @return true se marcou como lida, false se não encontrou ou não pertence ao usuário
     */
    @Transactional
    public boolean marcarComoLida(Long notificacaoId, Long usuarioId) {
        int atualizado = notificacaoRepository.marcarComoLida(notificacaoId, usuarioId);
        return atualizado > 0;
    }

    /**
     * Marca todas as notificações de um usuário como lidas
     *
     * @return Quantidade de notificações marcadas como lidas
     */
    @Transactional
    public int marcarTodasComoLidas(Long usuarioId) {
        return notificacaoRepository.marcarTodasComoLidas(usuarioId);
    }

    /**
     * Deleta uma notificação específica
     * Valida se a notificação pertence ao usuário (autorização)
     *
     * @return true se deletou, false se não encontrou ou não pertence ao usuário
     */
    @Transactional
    public boolean deletarNotificacao(Long notificacaoId, Long usuarioId) {
        Optional<Notificacao> notificacao = notificacaoRepository.findByIdAndUsuarioId(notificacaoId, usuarioId);
        
        if (notificacao.isPresent()) {
            notificacaoRepository.delete(notificacao.get());
            return true;
        }
        
        return false;
    }

    /**
     * Lista notificações de um tipo específico
     */
    @Transactional(readOnly = true)
    public List<Notificacao> listarPorTipo(Long usuarioId, TipoNotificacao tipo) {
        return notificacaoRepository.findByUsuarioIdAndTipoOrderByCriadaEmDesc(usuarioId, tipo);
    }

    // ========== Métodos auxiliares para criação de notificações específicas ==========

    /**
     * Cria notificação de nova publicação de um fornecedor seguido
     */
    @Transactional
    public Notificacao notificarNovaPublicacao(
            Long usuarioSeguidorId,
            Long publicacaoId,
            String nomeFornecedor,
            String tituloPublicacao,
            String cnpjFornecedor) {

        String titulo = nomeFornecedor + " publicou algo novo";
        String mensagem = tituloPublicacao;
        String link = "/perfil-comercial?cnpj=" + cnpjFornecedor;

        return criarNotificacao(
            usuarioSeguidorId,
            TipoNotificacao.NOVA_PUBLICACAO,
            titulo,
            mensagem,
            link,
            "PUBLICACAO",
            publicacaoId,
            nomeFornecedor
        );
    }

    /**
     * Cria notificação de nova avaliação recebida
     */
    @Transactional
    public Notificacao notificarNovaAvaliacao(
            Long usuarioFornecedorId,
            Long avaliacaoId,
            String nomeComprador,
            Double nota,
            String cnpjFornecedor) {

        String titulo = "Nova avaliação recebida";
        String mensagem = nomeComprador + " avaliou sua empresa com " + String.format("%.1f", nota) + " estrelas";
        String link = "/perfil-comercial?cnpj=" + cnpjFornecedor + "#avaliacoes";

        return criarNotificacao(
            usuarioFornecedorId,
            TipoNotificacao.NOVA_AVALIACAO,
            titulo,
            mensagem,
            link,
            "AVALIACAO",
            avaliacaoId,
            nomeComprador
        );
    }

    /**
     * Cria notificação de novo seguidor
     */
    @Transactional
    public Notificacao notificarNovoSeguidor(
            Long usuarioFornecedorId,
            Long seguidorId,
            String nomeSeguidor,
            String cnpjFornecedor) {

        String titulo = "Novo seguidor";
        String mensagem = nomeSeguidor + " começou a seguir sua empresa";
        String link = "/perfil-comercial?cnpj=" + cnpjFornecedor;

        return criarNotificacao(
            usuarioFornecedorId,
            TipoNotificacao.NOVO_SEGUIDOR,
            titulo,
            mensagem,
            link,
            "SEGUIDOR",
            seguidorId,
            nomeSeguidor
        );
    }

    /**
     * Cria notificação de novo comentário
     */
    @Transactional
    public Notificacao notificarNovoComentario(
            Long usuarioAutorPublicacaoId,
            Long comentarioId,
            String nomeComentarista,
            String textoComentario,
            String linkPublicacao) {

        String titulo = nomeComentarista + " comentou em sua publicação";
        String mensagem = textoComentario.length() > 100 
            ? textoComentario.substring(0, 97) + "..." 
            : textoComentario;

        return criarNotificacao(
            usuarioAutorPublicacaoId,
            TipoNotificacao.NOVO_COMENTARIO,
            titulo,
            mensagem,
            linkPublicacao,
            "COMENTARIO",
            comentarioId,
            nomeComentarista
        );
    }
}
