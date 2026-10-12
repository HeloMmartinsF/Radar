package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.ChamadoSuporteRepository;
import br.com.radarfornecedor.radar.repository.MensagemChamadoRepository;
import br.com.radarfornecedor.radar.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service para gerenciamento de Chamados de Suporte.
 * 
 * RESPONSABILIDADES:
 * - Criar, atualizar e gerenciar chamados
 * - Validar permissões (usuário só vê seus chamados, admin vê todos)
 * - Gerar números únicos de chamados
 * - Gerenciar mensagens
 * - Alterar status
 * - Integrar com sistema de notificações
 * 
 * SEGURANÇA:
 * - Todas as operações validam propriedade do chamado
 * - Observações internas NUNCA são expostas ao usuário
 * - Admin identificado por username="admin"
 */
@Service
public class ChamadoSuporteService {

    private final ChamadoSuporteRepository chamadoRepository;
    private final MensagemChamadoRepository mensagemRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacaoService notificacaoService;

    public ChamadoSuporteService(
            ChamadoSuporteRepository chamadoRepository,
            MensagemChamadoRepository mensagemRepository,
            UsuarioRepository usuarioRepository,
            NotificacaoService notificacaoService) {
        this.chamadoRepository = chamadoRepository;
        this.mensagemRepository = mensagemRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;
    }

    // ============================================
    // CRIAR CHAMADO
    // ============================================

    /**
     * Cria um novo chamado de suporte.
     * Gera número único, associa ao usuário e envia notificação aos admins.
     * 
     * @param usuario Usuário autenticado que está abrindo o chamado
     * @param dados Map com: assunto, categoria, descricao, urgencia, infoTecnicas
     * @return Chamado criado
     */
    @Transactional
    public ChamadoSuporte criarChamado(Usuario usuario, Map<String, String> dados) {
        // Criar entidade
        ChamadoSuporte chamado = new ChamadoSuporte();
        chamado.setUsuarioId(usuario.getId());
        chamado.setUsuarioNome(usuario.getUsername());
        chamado.setUsuarioTipo(identificarTipoUsuario(usuario));
        chamado.setCnpjCpf(usuario.getCnpjOuCpf());
        
        chamado.setAssunto(dados.get("assunto"));
        chamado.setCategoria(CategoriaSuporte.valueOf(dados.get("categoria")));
        chamado.setDescricao(dados.get("descricao"));
        chamado.setUrgencia(NivelUrgencia.valueOf(dados.get("urgencia")));
        chamado.setInfoTecnicas(dados.get("infoTecnicas"));
        
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setCriadoEm(LocalDateTime.now());
        
        // Salvar para gerar ID
        chamado = chamadoRepository.save(chamado);
        
        // Gerar número único do chamado
        String numeroChamado = gerarNumeroChamado(chamado.getId());
        chamado.setNumeroChamado(numeroChamado);
        
        // Salvar novamente com número
        chamado = chamadoRepository.save(chamado);
        
        // Notificar administradores
        notificarAdminsNovoChamado(chamado);
        
        System.out.println("[Suporte] Chamado " + numeroChamado + " criado por usuário ID: " + usuario.getId());
        
        return chamado;
    }

    /**
     * Gera número único no formato SUP-2026-00001
     */
    private String gerarNumeroChamado(Long id) {
        int ano = Year.now().getValue();
        String numero = String.format("%05d", id);
        return "SUP-" + ano + "-" + numero;
    }

    /**
     * Identifica tipo de usuário para exibição
     */
    private String identificarTipoUsuario(Usuario usuario) {
        if (Boolean.TRUE.equals(usuario.getFornecedor())) return "Fornecedor";
        if (Boolean.TRUE.equals(usuario.getCliente())) return "Cliente";
        if (Boolean.TRUE.equals(usuario.getRepresentante())) return "Representante";
        if (Boolean.TRUE.equals(usuario.getComprador())) return "Comprador";
        return "Usuário";
    }

    // ============================================
    // LISTAR CHAMADOS (USUÁRIO)
    // ============================================

    /**
     * Lista todos os chamados de um usuário específico.
     * SEGURANÇA: Retorna apenas chamados do próprio usuário.
     */
    public List<ChamadoSuporte> listarChamadosUsuario(Long usuarioId) {
        return chamadoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
    }

    /**
     * Lista chamados de um usuário filtrados por status.
     */
    public List<ChamadoSuporte> listarChamadosUsuarioPorStatus(Long usuarioId, StatusChamado status) {
        return chamadoRepository.findByUsuarioIdAndStatusOrderByCriadoEmDesc(usuarioId, status);
    }

    /**
     * Conta chamados abertos de um usuário.
     */
    public Long contarChamadosAbertosUsuario(Long usuarioId) {
        return chamadoRepository.countChamadosAbertosUsuario(usuarioId);
    }

    // ============================================
    // LISTAR CHAMADOS (ADMIN)
    // ============================================

    /**
     * Lista todos os chamados (apenas para administradores).
     * SEGURANÇA: Deve ser chamado apenas após validação de admin no controller.
     */
    public List<ChamadoSuporte> listarTodosChamados() {
        return chamadoRepository.findAllOrderedByUrgencyAndDate();
    }

    /**
     * Lista chamados filtrados (para administradores).
     */
    public List<ChamadoSuporte> listarChamadosFiltrados(
            StatusChamado status,
            CategoriaSuporte categoria,
            NivelUrgencia urgencia,
            Long usuarioId) {
        
        if (status != null || categoria != null || urgencia != null || usuarioId != null) {
            return chamadoRepository.findByMultipleFilters(status, categoria, urgencia, usuarioId);
        }
        
        return listarTodosChamados();
    }

    /**
     * Lista apenas chamados abertos (status diferente de ENCERRADO e RESOLVIDO).
     */
    public List<ChamadoSuporte> listarChamadosAbertos() {
        return chamadoRepository.findAllOpenChamados();
    }

    /**
     * Conta total de chamados abertos (para dashboard admin).
     */
    public Long contarTodosChamadosAbertos() {
        return chamadoRepository.countAllChamadosAbertos();
    }

    // ============================================
    // BUSCAR CHAMADO
    // ============================================

    /**
     * Busca chamado por número.
     * SEGURANÇA: Validação de propriedade deve ser feita no controller.
     */
    public Optional<ChamadoSuporte> buscarPorNumero(String numeroChamado) {
        return chamadoRepository.findByNumeroChamado(numeroChamado);
    }

    /**
     * Busca chamado por ID.
     */
    public Optional<ChamadoSuporte> buscarPorId(Long id) {
        return chamadoRepository.findById(id);
    }

    /**
     * Busca chamado e valida propriedade (usuário comum).
     * Retorna Optional.empty() se chamado não pertence ao usuário.
     */
    public Optional<ChamadoSuporte> buscarChamadoUsuario(String numeroChamado, Long usuarioId) {
        Optional<ChamadoSuporte> chamadoOpt = chamadoRepository.findByNumeroChamado(numeroChamado);
        
        if (chamadoOpt.isPresent()) {
            ChamadoSuporte chamado = chamadoOpt.get();
            
            // Validar propriedade
            if (!chamado.getUsuarioId().equals(usuarioId)) {
                System.out.println("[Suporte] SEGURANÇA: Usuário " + usuarioId + 
                                 " tentou acessar chamado " + numeroChamado + 
                                 " que pertence ao usuário " + chamado.getUsuarioId());
                return Optional.empty();
            }
            
            return chamadoOpt;
        }
        
        return Optional.empty();
    }

    // ============================================
    // ALTERAR STATUS (ADMIN)
    // ============================================

    /**
     * Altera o status de um chamado.
     * SEGURANÇA: Deve ser chamado apenas após validação de admin.
     */
    @Transactional
    public ChamadoSuporte alterarStatus(String numeroChamado, StatusChamado novoStatus, Usuario admin) {
        ChamadoSuporte chamado = chamadoRepository.findByNumeroChamado(numeroChamado)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        
        StatusChamado statusAnterior = chamado.getStatus();
        chamado.setStatus(novoStatus);
        chamado.setAtualizadoEm(LocalDateTime.now());
        
        // Se está sendo colocado em atendimento, associar admin
        if (novoStatus == StatusChamado.EM_ATENDIMENTO && chamado.getAdminResponsavelId() == null) {
            chamado.setAdminResponsavelId(admin.getId());
            chamado.setAdminResponsavelNome(admin.getUsername());
        }
        
        chamado = chamadoRepository.save(chamado);
        
        // Notificar usuário sobre mudança de status
        notificarUsuarioMudancaStatus(chamado, statusAnterior, novoStatus);
        
        System.out.println("[Suporte] Chamado " + numeroChamado + " mudou de " + 
                         statusAnterior + " para " + novoStatus);
        
        return chamado;
    }

    // ============================================
    // REABRIR CHAMADO
    // ============================================

    /**
     * Reabre um chamado encerrado ou resolvido.
     * Usuário pode reabrir seus próprios chamados.
     */
    @Transactional
    public ChamadoSuporte reabrirChamado(String numeroChamado, Long usuarioId) {
        ChamadoSuporte chamado = buscarChamadoUsuario(numeroChamado, usuarioId)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado ou sem permissão"));
        
        if (chamado.getStatus() != StatusChamado.RESOLVIDO && chamado.getStatus() != StatusChamado.ENCERRADO) {
            throw new RuntimeException("Apenas chamados RESOLVIDOS ou ENCERRADOS podem ser reabertos");
        }
        
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setAtualizadoEm(LocalDateTime.now());
        chamado.setEncerradoEm(null);
        
        chamado = chamadoRepository.save(chamado);
        
        // Notificar admins sobre reabertura
        notificarAdminsReaberturaChamado(chamado);
        
        System.out.println("[Suporte] Chamado " + numeroChamado + " reaberto por usuário " + usuarioId);
        
        return chamado;
    }

    // ============================================
    // OBSERVAÇÕES INTERNAS (ADMIN)
    // ============================================

    /**
     * Atualiza observações internas (SOMENTE ADMIN).
     * Estas observações NUNCA são expostas ao usuário.
     */
    @Transactional
    public ChamadoSuporte atualizarObservacoesInternas(String numeroChamado, String observacoes) {
        ChamadoSuporte chamado = chamadoRepository.findByNumeroChamado(numeroChamado)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        
        chamado.setObservacoesInternas(observacoes);
        chamado.setAtualizadoEm(LocalDateTime.now());
        
        return chamadoRepository.save(chamado);
    }

    /**
     * Remove observações internas de um chamado antes de retornar ao usuário.
     * SEGURANÇA CRÍTICA: Sempre chamar este método antes de expor chamado ao usuário.
     */
    public ChamadoSuporte removerObservacoesInternas(ChamadoSuporte chamado) {
        chamado.setObservacoesInternas(null);
        return chamado;
    }

    /**
     * Remove observações internas de uma lista de chamados.
     */
    public List<ChamadoSuporte> removerObservacoesInternas(List<ChamadoSuporte> chamados) {
        chamados.forEach(c -> c.setObservacoesInternas(null));
        return chamados;
    }

    // ============================================
    // MENSAGENS
    // ============================================

    /**
     * Envia mensagem em um chamado.
     * Pode ser enviada por usuário (dono do chamado) ou admin.
     */
    @Transactional
    public MensagemChamado enviarMensagem(
            String numeroChamado,
            Long autorUsuarioId,
            String autorTipo,
            String mensagemTexto) {
        
        ChamadoSuporte chamado = chamadoRepository.findByNumeroChamado(numeroChamado)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        
        // Criar mensagem
        MensagemChamado mensagem = new MensagemChamado();
        mensagem.setChamadoId(chamado.getId());
        mensagem.setAutorUsuarioId(autorUsuarioId);
        mensagem.setAutorTipo(autorTipo); // "USUARIO" ou "ADMIN"
        mensagem.setMensagem(mensagemTexto);
        mensagem.setCriadaEm(LocalDateTime.now());
        
        // Buscar nome do autor
        final MensagemChamado mensagemFinal = mensagem;
        usuarioRepository.findById(autorUsuarioId).ifPresent(u -> {
            mensagemFinal.setAutorNome(u.getUsername());
        });
        
        MensagemChamado mensagemSalva = mensagemRepository.save(mensagem);
        
        // Atualizar contador de mensagens no chamado
        chamado.setTotalMensagens(chamado.getTotalMensagens() + 1);
        chamado.setAtualizadoEm(LocalDateTime.now());
        
        // Marcar que há novas mensagens para o destinatário
        if ("USUARIO".equals(autorTipo)) {
            // Usuário enviou → Admin precisa ler
            chamado.setTemNovasMensagensAdmin(true);
            chamado.setStatus(StatusChamado.AGUARDANDO_ADMIN);
            
            // Notificar administradores
            notificarAdminsNovaMensagem(chamado);
        } else {
            // Admin enviou → Usuário precisa ler
            chamado.setTemNovasMensagensUsuario(true);
            chamado.setStatus(StatusChamado.AGUARDANDO_USUARIO);
            
            // Notificar usuário
            notificarUsuarioNovaMensagem(chamado);
        }
        
        chamadoRepository.save(chamado);
        
        System.out.println("[Suporte] Nova mensagem no chamado " + numeroChamado + " de " + autorTipo);
        
        return mensagemSalva;
    }

    /**
     * Lista mensagens de um chamado.
     * SEGURANÇA: Validação de propriedade deve ser feita no controller.
     */
    public List<MensagemChamado> listarMensagens(String numeroChamado) {
        ChamadoSuporte chamado = chamadoRepository.findByNumeroChamado(numeroChamado)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        
        return mensagemRepository.findByChamadoIdOrderByCriadaEmAsc(chamado.getId());
    }

    /**
     * Marca mensagens como lidas.
     * Quando usuário visualiza, marca mensagens de ADMIN como lidas.
     * Quando admin visualiza, marca mensagens de USUARIO como lidas.
     */
    @Transactional
    public void marcarMensagensComoLidas(String numeroChamado, String tipoVisualizador) {
        ChamadoSuporte chamado = chamadoRepository.findByNumeroChamado(numeroChamado)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));
        
        List<MensagemChamado> mensagens = mensagemRepository.findByChamadoIdOrderByCriadaEmAsc(chamado.getId());
        
        for (MensagemChamado msg : mensagens) {
            // Se visualizador é USUARIO, marcar mensagens de ADMIN como lidas
            // Se visualizador é ADMIN, marcar mensagens de USUARIO como lidas
            if (!msg.getAutorTipo().equals(tipoVisualizador) && !msg.getLida()) {
                msg.setLida(true);
                mensagemRepository.save(msg);
            }
        }
        
        // Atualizar flags no chamado
        if ("USUARIO".equals(tipoVisualizador)) {
            chamado.setTemNovasMensagensUsuario(false);
        } else {
            chamado.setTemNovasMensagensAdmin(false);
        }
        
        chamadoRepository.save(chamado);
    }

    // ============================================
    // NOTIFICAÇÕES
    // ============================================

    /**
     * Notifica administradores sobre novo chamado criado.
     */
    private void notificarAdminsNovoChamado(ChamadoSuporte chamado) {
        // Buscar usuário admin
        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("admin");
        
        if (adminOpt.isPresent()) {
            Usuario admin = adminOpt.get();
            
            String titulo = "Novo chamado de suporte: " + chamado.getNumeroChamado();
            String mensagem = chamado.getUsuarioNome() + " (" + chamado.getUsuarioTipo() + ") " +
                            "abriu um chamado sobre: " + chamado.getAssunto();
            String link = "/pages/admin-suporte.html?chamado=" + chamado.getNumeroChamado();
            
            notificacaoService.criarNotificacao(
                admin.getId(),
                TipoNotificacao.NOVO_CHAMADO_SUPORTE,
                titulo,
                mensagem,
                link,
                "CHAMADO",
                chamado.getId(),
                chamado.getUsuarioNome()
            );
        }
    }

    /**
     * Notifica administradores sobre nova mensagem de usuário.
     */
    private void notificarAdminsNovaMensagem(ChamadoSuporte chamado) {
        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("admin");
        
        if (adminOpt.isPresent()) {
            Usuario admin = adminOpt.get();
            
            String titulo = "Nova resposta em " + chamado.getNumeroChamado();
            String mensagem = chamado.getUsuarioNome() + " respondeu ao chamado: " + chamado.getAssunto();
            String link = "/pages/admin-suporte.html?chamado=" + chamado.getNumeroChamado();
            
            notificacaoService.criarNotificacao(
                admin.getId(),
                TipoNotificacao.RESPOSTA_CHAMADO_SUPORTE,
                titulo,
                mensagem,
                link,
                "MENSAGEM_CHAMADO",
                chamado.getId(),
                chamado.getUsuarioNome()
            );
        }
    }

    /**
     * Notifica usuário sobre resposta do suporte.
     */
    private void notificarUsuarioNovaMensagem(ChamadoSuporte chamado) {
        String titulo = "Resposta recebida em " + chamado.getNumeroChamado();
        String mensagem = "O suporte respondeu seu chamado: " + chamado.getAssunto();
        String link = "/pages/ajuda-suporte.html?chamado=" + chamado.getNumeroChamado();
        
        notificacaoService.criarNotificacao(
            chamado.getUsuarioId(),
            TipoNotificacao.RESPOSTA_CHAMADO_SUPORTE,
            titulo,
            mensagem,
            link,
            "MENSAGEM_CHAMADO",
            chamado.getId(),
            "Suporte Radar"
        );
    }

    /**
     * Notifica usuário sobre mudança de status.
     */
    private void notificarUsuarioMudancaStatus(
            ChamadoSuporte chamado,
            StatusChamado statusAnterior,
            StatusChamado novoStatus) {
        
        String titulo = "Status alterado: " + chamado.getNumeroChamado();
        String mensagem = "Seu chamado mudou de '" + statusAnterior.getNome() + 
                        "' para '" + novoStatus.getNome() + "'";
        String link = "/pages/ajuda-suporte.html?chamado=" + chamado.getNumeroChamado();
        
        notificacaoService.criarNotificacao(
            chamado.getUsuarioId(),
            TipoNotificacao.STATUS_CHAMADO_ALTERADO,
            titulo,
            mensagem,
            link,
            "CHAMADO",
            chamado.getId(),
            "Suporte Radar"
        );
    }

    /**
     * Notifica administradores sobre reabertura de chamado.
     */
    private void notificarAdminsReaberturaChamado(ChamadoSuporte chamado) {
        Optional<Usuario> adminOpt = usuarioRepository.findByUsername("admin");
        
        if (adminOpt.isPresent()) {
            Usuario admin = adminOpt.get();
            
            String titulo = "Chamado reaberto: " + chamado.getNumeroChamado();
            String mensagem = chamado.getUsuarioNome() + " reabriu o chamado: " + chamado.getAssunto();
            String link = "/pages/admin-suporte.html?chamado=" + chamado.getNumeroChamado();
            
            notificacaoService.criarNotificacao(
                admin.getId(),
                TipoNotificacao.NOVO_CHAMADO_SUPORTE,
                titulo,
                mensagem,
                link,
                "CHAMADO",
                chamado.getId(),
                chamado.getUsuarioNome()
            );
        }
    }

    // ============================================
    // ESTATÍSTICAS
    // ============================================

    /**
     * Retorna estatísticas gerais do suporte (para dashboard admin).
     */
    public Map<String, Object> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalAbertos", contarTodosChamadosAbertos());
        stats.put("totalEncerrados", chamadoRepository.findByStatusOrderByCriadoEmDesc(StatusChamado.ENCERRADO).size());
        stats.put("totalUrgenciaAlta", chamadoRepository.findByUrgenciaOrderByCriadoEmDesc(NivelUrgencia.ALTA).size());
        stats.put("totalUrgenciaCritica", chamadoRepository.findByUrgenciaOrderByCriadoEmDesc(NivelUrgencia.CRITICA).size());
        stats.put("totalComNovasMensagens", chamadoRepository.findByTemNovasMensagensAdminTrueOrderByCriadoEmDesc().size());
        
        return stats;
    }
}
