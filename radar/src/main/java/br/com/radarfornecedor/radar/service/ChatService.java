package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service para gerenciamento do Chat Comercial.
 * Responsável por criar conversas, enviar mensagens e gerenciar permissões.
 */
@Service
public class ChatService {

    private final ConversaRepository conversaRepository;
    private final MensagemChatRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final FornecedorRepository fornecedorRepository;
    private final RepresentanteRepository representanteRepository;
    private final ProdutoRepository produtoRepository;
    private final NotificacaoService notificacaoService;

    public ChatService(
            ConversaRepository conversaRepository,
            MensagemChatRepository mensagemRepository,
            UsuarioRepository usuarioRepository,
            FornecedorRepository fornecedorRepository,
            RepresentanteRepository representanteRepository,
            ProdutoRepository produtoRepository,
            NotificacaoService notificacaoService) {
        this.conversaRepository = conversaRepository;
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.representanteRepository = representanteRepository;
        this.produtoRepository = produtoRepository;
        this.notificacaoService = notificacaoService;
    }

    /**
     * Inicia uma nova conversa ou retorna conversa existente
     * Aplica deduplicação: se já existe conversa entre os mesmos participantes sobre o mesmo contexto, reutiliza
     */
    @Transactional
    public Conversa iniciarConversa(
            Long usuarioRemetenteId,
            Long usuarioDestinatarioId,
            Long fornecedorId,
            ContextoConversa contexto,
            Long contextoReferencia) {

        // Validação 1: Usuários existem?
        Usuario remetente = usuarioRepository.findById(usuarioRemetenteId)
                .orElseThrow(() -> new RuntimeException("Remetente não encontrado"));
        Usuario destinatario = usuarioRepository.findById(usuarioDestinatarioId)
                .orElseThrow(() -> new RuntimeException("Destinatário não encontrado"));

        // Validação 2: Fornecedor existe?
        if (fornecedorId != null) {
            fornecedorRepository.findById(fornecedorId)
                    .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));
        }

        // Validação 3: Contexto PRODUTO → Produto deve existir
        if (contexto == ContextoConversa.PRODUTO && contextoReferencia != null) {
            produtoRepository.findById(contextoReferencia)
                    .orElseThrow(() -> new RuntimeException("Produto não encontrado"));
        }

        // Deduplicação: Buscar conversa existente
        Optional<Conversa> conversaExistente = conversaRepository.findConversaExistente(
                usuarioRemetenteId,
                usuarioDestinatarioId,
                fornecedorId,
                contexto,
                contextoReferencia
        );

        if (conversaExistente.isPresent()) {
            return conversaExistente.get();
        }

        // Criar nova conversa
        Conversa conversa = new Conversa();
        conversa.setParticipante1Id(usuarioRemetenteId);
        conversa.setParticipante2Id(usuarioDestinatarioId);
        conversa.setFornecedorId(fornecedorId);
        conversa.setContexto(contexto);
        conversa.setContextoReferencia(contextoReferencia);
        conversa.setAtiva(true);

        return conversaRepository.save(conversa);
    }

    /**
     * Envia uma mensagem em uma conversa existente
     */
    @Transactional
    public MensagemChat enviarMensagem(
            Long conversaId,
            Long remetenteId,
            String texto) {

        // Validação 1: Conversa existe?
        Conversa conversa = conversaRepository.findById(conversaId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada"));

        // Validação 2: Remetente é participante?
        if (!conversa.getParticipante1Id().equals(remetenteId) &&
            !conversa.getParticipante2Id().equals(remetenteId)) {
            throw new RuntimeException("Você não é participante desta conversa");
        }

        // Validação 3: Conversa está ativa?
        if (!conversa.getAtiva()) {
            throw new RuntimeException("Conversa encerrada");
        }

        // Validação 4: Texto não vazio e limite de caracteres
        if (texto == null || texto.trim().isEmpty()) {
            throw new RuntimeException("Mensagem não pode ser vazia");
        }
        if (texto.length() > 5000) {
            throw new RuntimeException("Mensagem muito longa (máximo 5000 caracteres)");
        }

        // Criar mensagem
        MensagemChat mensagem = new MensagemChat();
        mensagem.setConversaId(conversaId);
        mensagem.setRemetenteId(remetenteId);
        mensagem.setTexto(texto.trim());
        mensagem.setLida(false);

        mensagem = mensagemRepository.save(mensagem);

        // Atualizar conversa
        conversa.setUltimaMensagemEm(LocalDateTime.now());

        // Incrementar contador de não lidas do destinatário
        Long destinatarioId;
        if (conversa.getParticipante1Id().equals(remetenteId)) {
            // Remetente é participante1 → destinatário é participante2
            destinatarioId = conversa.getParticipante2Id();
            conversa.setParticipante2NaoLidas(conversa.getParticipante2NaoLidas() + 1);
        } else {
            // Remetente é participante2 → destinatário é participante1
            destinatarioId = conversa.getParticipante1Id();
            conversa.setParticipante1NaoLidas(conversa.getParticipante1NaoLidas() + 1);
        }

        conversaRepository.save(conversa);

        // Enviar notificação ao destinatário
        enviarNotificacaoNovaMensagem(conversa, mensagem, remetenteId, destinatarioId);

        System.out.println("[CHAT] Mensagem enviada: conversa=" + conversaId + ", remetente=" + remetenteId);

        return mensagem;
    }

    /**
     * Lista todas as conversas de um usuário
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarConversas(Long usuarioId) {
        List<Conversa> conversas = conversaRepository.findConversasByUsuario(usuarioId);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (Conversa conversa : conversas) {
            Map<String, Object> conversaInfo = montarInfoConversa(conversa, usuarioId);
            resultado.add(conversaInfo);
        }

        return resultado;
    }

    /**
     * Busca detalhes de uma conversa específica
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buscarConversa(Long conversaId, Long usuarioId) {
        Conversa conversa = conversaRepository.findByIdAndUsuario(conversaId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada ou você não é participante"));

        return montarInfoConversa(conversa, usuarioId);
    }

    /**
     * Lista mensagens de uma conversa
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarMensagens(Long conversaId, Long usuarioId) {
        // Validar participação
        Conversa conversa = conversaRepository.findByIdAndUsuario(conversaId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada ou você não é participante"));

        List<MensagemChat> mensagens = mensagemRepository.findByConversaIdOrderByEnviadaEmAsc(conversaId);
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (MensagemChat mensagem : mensagens) {
            Map<String, Object> mensagemInfo = new HashMap<>();
            mensagemInfo.put("id", mensagem.getId());
            mensagemInfo.put("remetenteId", mensagem.getRemetenteId());
            mensagemInfo.put("texto", mensagem.getTexto());
            mensagemInfo.put("enviadaEm", mensagem.getEnviadaEm());
            mensagemInfo.put("lida", mensagem.getLida());
            mensagemInfo.put("editada", mensagem.getEditada());
            mensagemInfo.put("ehMinha", mensagem.getRemetenteId().equals(usuarioId));

            resultado.add(mensagemInfo);
        }

        return resultado;
    }

    /**
     * Marca todas as mensagens de uma conversa como lidas
     */
    @Transactional
    public void marcarComoLidas(Long conversaId, Long usuarioId) {
        // Validar participação
        Conversa conversa = conversaRepository.findByIdAndUsuario(conversaId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada ou você não é participante"));

        // Marcar mensagens como lidas
        int marcadas = mensagemRepository.marcarTodasComoLidas(conversaId, usuarioId);

        // Zerar contador de não lidas
        if (conversa.getParticipante1Id().equals(usuarioId)) {
            conversa.setParticipante1NaoLidas(0);
        } else {
            conversa.setParticipante2NaoLidas(0);
        }

        conversaRepository.save(conversa);

        System.out.println("[CHAT] Mensagens marcadas como lidas: " + marcadas + " mensagens");
    }

    /**
     * Conta total de mensagens não lidas do usuário (todas as conversas)
     */
    @Transactional(readOnly = true)
    public Long contarNaoLidas(Long usuarioId) {
        return conversaRepository.countTotalNaoLidasUsuario(usuarioId);
    }

    /**
     * Encerra uma conversa (marca como inativa)
     */
    @Transactional
    public void encerrarConversa(Long conversaId, Long usuarioId) {
        Conversa conversa = conversaRepository.findByIdAndUsuario(conversaId, usuarioId)
                .orElseThrow(() -> new RuntimeException("Conversa não encontrada ou você não é participante"));

        conversa.setAtiva(false);
        conversaRepository.save(conversa);

        System.out.println("[CHAT] Conversa encerrada: " + conversaId);
    }

    /**
     * Verifica se um usuário pode iniciar conversa com um fornecedor
     * Valida permissões de representante
     */
    @Transactional(readOnly = true)
    public Map<String, Object> verificarPermissao(Long usuarioId, Long fornecedorId) {
        Map<String, Object> resultado = new HashMap<>();

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado"));

        // Cliente pode sempre iniciar conversa
        if (Boolean.TRUE.equals(usuario.getCliente())) {
            resultado.put("podeIniciar", true);
            resultado.put("motivo", "Cliente autorizado");
            return resultado;
        }

        // Fornecedor pode conversar (owner)
        if (Boolean.TRUE.equals(usuario.getFornecedor())) {
            String cnpjUsuario = usuario.getCnpjOuCpf();
            if (cnpjUsuario != null && cnpjUsuario.replaceAll("\\D", "").equals(fornecedor.getCnpj())) {
                resultado.put("podeIniciar", true);
                resultado.put("motivo", "Fornecedor proprietário");
                return resultado;
            }
        }

        // Representante → validar vínculo
        if (Boolean.TRUE.equals(usuario.getRepresentante())) {
            String cnpjUsuario = usuario.getCnpjOuCpf();
            if (cnpjUsuario != null) {
                final String cnpjClean = cnpjUsuario.replaceAll("\\D", "");
                Optional<Representante> repOpt = representanteRepository.findAll().stream()
                        .filter(r -> r.getCnpj() != null && r.getCnpj().replaceAll("\\D", "").equals(cnpjClean))
                        .findFirst();

                if (repOpt.isPresent()) {
                    Representante rep = repOpt.get();
                    String cnpjFornecedorVinculado = rep.getCnpjFornecedor();
                    if (cnpjFornecedorVinculado != null &&
                        cnpjFornecedorVinculado.replaceAll("\\D", "").equals(fornecedor.getCnpj())) {
                        resultado.put("podeIniciar", true);
                        resultado.put("motivo", "Representante autorizado");
                        return resultado;
                    }
                }
            }
        }

        resultado.put("podeIniciar", false);
        resultado.put("motivo", "Sem permissão para iniciar conversa");
        return resultado;
    }

    // ========== Métodos auxiliares privados ==========

    /**
     * Monta informações completas de uma conversa para exibição
     */
    private Map<String, Object> montarInfoConversa(Conversa conversa, Long usuarioId) {
        Map<String, Object> info = new HashMap<>();

        info.put("id", conversa.getId());
        info.put("fornecedorId", conversa.getFornecedorId());
        info.put("contexto", conversa.getContexto());
        info.put("contextoReferencia", conversa.getContextoReferencia());
        info.put("iniciadaEm", conversa.getIniciadaEm());
        info.put("ultimaMensagemEm", conversa.getUltimaMensagemEm());
        info.put("ativa", conversa.getAtiva());

        // Identificar o outro participante
        Long outroParticipanteId = conversa.getParticipante1Id().equals(usuarioId)
                ? conversa.getParticipante2Id()
                : conversa.getParticipante1Id();

        Optional<Usuario> outroUsuario = usuarioRepository.findById(outroParticipanteId);
        info.put("outroParticipanteId", outroParticipanteId);
        info.put("outroParticipanteNome", outroUsuario.map(Usuario::getUsername).orElse("Usuário"));

        // Nome do fornecedor
        if (conversa.getFornecedorId() != null) {
            Optional<Fornecedor> fornecedor = fornecedorRepository.findById(conversa.getFornecedorId());
            info.put("fornecedorNome", fornecedor.map(Fornecedor::getNome).orElse("Fornecedor"));
        }

        // Nome do produto (se contexto for PRODUTO)
        if (conversa.getContexto() == ContextoConversa.PRODUTO && conversa.getContextoReferencia() != null) {
            Optional<Produto> produto = produtoRepository.findById(conversa.getContextoReferencia());
            info.put("produtoNome", produto.map(Produto::getNome).orElse("Produto"));
        }

        // Mensagens não lidas
        Integer naoLidas = conversa.getParticipante1Id().equals(usuarioId)
                ? conversa.getParticipante1NaoLidas()
                : conversa.getParticipante2NaoLidas();
        info.put("naoLidas", naoLidas);

        // Última mensagem
        List<MensagemChat> ultimaMensagemList = mensagemRepository.findUltimaMensagem(conversa.getId());
        if (!ultimaMensagemList.isEmpty()) {
            MensagemChat ultimaMensagem = ultimaMensagemList.get(0);
            info.put("ultimaMensagemTexto", ultimaMensagem.getTexto());
            info.put("ultimaMensagemData", ultimaMensagem.getEnviadaEm());
        }

        return info;
    }

    /**
     * Envia notificação de nova mensagem ao destinatário
     */
    private void enviarNotificacaoNovaMensagem(
            Conversa conversa,
            MensagemChat mensagem,
            Long remetenteId,
            Long destinatarioId) {

        try {
            Optional<Usuario> remetenteOpt = usuarioRepository.findById(remetenteId);
            String nomeRemetente = remetenteOpt.map(Usuario::getUsername).orElse("Usuário");

            String textoPreview = mensagem.getTexto();
            if (textoPreview.length() > 100) {
                textoPreview = textoPreview.substring(0, 97) + "...";
            }

            String titulo = "Nova mensagem de " + nomeRemetente;
            String link = "/pages/conversa.html?id=" + conversa.getId();

            notificacaoService.criarNotificacao(
                    destinatarioId,
                    TipoNotificacao.NOVA_MENSAGEM_CHAT,
                    titulo,
                    textoPreview,
                    link,
                    "MENSAGEM_CHAT",
                    mensagem.getId(),
                    nomeRemetente
            );
        } catch (Exception e) {
            System.err.println("[CHAT] Erro ao enviar notificação: " + e.getMessage());
        }
    }
}
