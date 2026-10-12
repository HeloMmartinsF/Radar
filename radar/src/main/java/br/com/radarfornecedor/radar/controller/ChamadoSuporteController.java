package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.service.ChamadoSuporteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para Central de Ajuda e Suporte.
 * 
 * ENDPOINTS PARA USUÁRIOS (clientes, fornecedores, representantes):
 * - POST   /api/suporte/chamados                    - Criar chamado
 * - GET    /api/suporte/chamados                    - Listar meus chamados
 * - GET    /api/suporte/chamados/{numero}           - Ver detalhes do chamado
 * - POST   /api/suporte/chamados/{numero}/mensagens - Enviar mensagem
 * - PUT    /api/suporte/chamados/{numero}/reabrir   - Reabrir chamado
 * - GET    /api/suporte/chamados/{numero}/mensagens - Listar mensagens
 * 
 * ENDPOINTS PARA ADMINISTRADORES:
 * - GET    /api/suporte/admin/chamados              - Listar todos os chamados
 * - GET    /api/suporte/admin/chamados/{numero}     - Ver detalhes (com observações internas)
 * - PUT    /api/suporte/admin/chamados/{numero}/status - Alterar status
 * - POST   /api/suporte/admin/chamados/{numero}/mensagens - Responder
 * - PUT    /api/suporte/admin/chamados/{numero}/observacoes - Atualizar observações internas
 * - GET    /api/suporte/admin/estatisticas          - Dashboard de estatísticas
 * 
 * SEGURANÇA:
 * - Todas as operações validam autenticação via HttpSession
 * - Usuários só podem ver e modificar seus próprios chamados
 * - Administradores identificados por username="admin"
 * - Observações internas NUNCA expostas a usuários comuns
 */
@RestController
@RequestMapping("/api/suporte")
public class ChamadoSuporteController {

    private final ChamadoSuporteService chamadoService;

    public ChamadoSuporteController(ChamadoSuporteService chamadoService) {
        this.chamadoService = chamadoService;
    }

    // ============================================
    // ENDPOINTS PARA USUÁRIOS
    // ============================================

    /**
     * POST /api/suporte/chamados
     * Cria um novo chamado de suporte.
     * 
     * Body JSON:
     * {
     *   "assunto": "Não consigo fazer login",
     *   "categoria": "ACESSO_CONTA",
     *   "descricao": "Esqueci minha senha e não recebo email de recuperação",
     *   "urgencia": "ALTA",
     *   "infoTecnicas": "Navegador: Chrome 120, SO: Windows 11"
     * }
     * 
     * SEGURANÇA: Valida autenticação na sessão
     */
    @PostMapping("/chamados")
    public ResponseEntity<?> criarChamado(
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Validar campos obrigatórios
            if (!dados.containsKey("assunto") || dados.get("assunto").trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Assunto é obrigatório"));
            }
            if (!dados.containsKey("categoria")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Categoria é obrigatória"));
            }
            if (!dados.containsKey("descricao") || dados.get("descricao").trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Descrição é obrigatória"));
            }
            if (!dados.containsKey("urgencia")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Urgência é obrigatória"));
            }

            // Criar chamado
            ChamadoSuporte chamado = chamadoService.criarChamado(usuario, dados);

            // Remover observações internas antes de retornar
            chamado = chamadoService.removerObservacoesInternas(chamado);

            return ResponseEntity.status(HttpStatus.CREATED).body(chamado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Valor inválido: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao criar chamado: " + e.getMessage()));
        }
    }

    /**
     * GET /api/suporte/chamados
     * Lista todos os chamados do usuário autenticado.
     * Query params opcionais: status (filtrar por status)
     * 
     * SEGURANÇA: Retorna apenas chamados do próprio usuário
     */
    @GetMapping("/chamados")
    public ResponseEntity<?> listarMeusChamados(
            @RequestParam(required = false) String status,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            List<ChamadoSuporte> chamados;

            if (status != null && !status.isEmpty()) {
                StatusChamado statusEnum = StatusChamado.valueOf(status);
                chamados = chamadoService.listarChamadosUsuarioPorStatus(usuario.getId(), statusEnum);
            } else {
                chamados = chamadoService.listarChamadosUsuario(usuario.getId());
            }

            // Remover observações internas
            chamados = chamadoService.removerObservacoesInternas(chamados);

            return ResponseEntity.ok(chamados);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Status inválido: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar chamados: " + e.getMessage()));
        }
    }

    /**
     * GET /api/suporte/chamados/{numero}
     * Busca detalhes de um chamado específico.
     * 
     * SEGURANÇA: Valida que o chamado pertence ao usuário autenticado
     */
    @GetMapping("/chamados/{numero}")
    public ResponseEntity<?> buscarChamado(
            @PathVariable String numero,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Buscar e validar propriedade
            Optional<ChamadoSuporte> chamadoOpt = chamadoService.buscarChamadoUsuario(numero, usuario.getId());

            if (chamadoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Chamado não encontrado ou sem permissão"));
            }

            ChamadoSuporte chamado = chamadoOpt.get();

            // Remover observações internas
            chamado = chamadoService.removerObservacoesInternas(chamado);

            // Marcar mensagens como lidas pelo usuário
            chamadoService.marcarMensagensComoLidas(numero, "USUARIO");

            return ResponseEntity.ok(chamado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao buscar chamado: " + e.getMessage()));
        }
    }

    /**
     * POST /api/suporte/chamados/{numero}/mensagens
     * Envia uma mensagem em um chamado.
     * 
     * Body JSON: { "mensagem": "Texto da mensagem" }
     * 
     * SEGURANÇA: Valida que o chamado pertence ao usuário
     */
    @PostMapping("/chamados/{numero}/mensagens")
    public ResponseEntity<?> enviarMensagem(
            @PathVariable String numero,
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Validar chamado pertence ao usuário
            Optional<ChamadoSuporte> chamadoOpt = chamadoService.buscarChamadoUsuario(numero, usuario.getId());
            if (chamadoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Sem permissão para enviar mensagem neste chamado"));
            }

            // Validar mensagem
            String mensagemTexto = dados.get("mensagem");
            if (mensagemTexto == null || mensagemTexto.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Mensagem é obrigatória"));
            }

            // Enviar mensagem
            MensagemChamado mensagem = chamadoService.enviarMensagem(
                numero,
                usuario.getId(),
                "USUARIO",
                mensagemTexto
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao enviar mensagem: " + e.getMessage()));
        }
    }

    /**
     * GET /api/suporte/chamados/{numero}/mensagens
     * Lista todas as mensagens de um chamado.
     * 
     * SEGURANÇA: Valida que o chamado pertence ao usuário
     */
    @GetMapping("/chamados/{numero}/mensagens")
    public ResponseEntity<?> listarMensagens(
            @PathVariable String numero,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Validar chamado pertence ao usuário
            Optional<ChamadoSuporte> chamadoOpt = chamadoService.buscarChamadoUsuario(numero, usuario.getId());
            if (chamadoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Sem permissão para visualizar este chamado"));
            }

            List<MensagemChamado> mensagens = chamadoService.listarMensagens(numero);

            return ResponseEntity.ok(mensagens);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar mensagens: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/suporte/chamados/{numero}/reabrir
     * Reabre um chamado encerrado ou resolvido.
     * 
     * SEGURANÇA: Valida que o chamado pertence ao usuário
     */
    @PutMapping("/chamados/{numero}/reabrir")
    public ResponseEntity<?> reabrirChamado(
            @PathVariable String numero,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            ChamadoSuporte chamado = chamadoService.reabrirChamado(numero, usuario.getId());

            // Remover observações internas
            chamado = chamadoService.removerObservacoesInternas(chamado);

            return ResponseEntity.ok(chamado);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao reabrir chamado: " + e.getMessage()));
        }
    }

    // ============================================
    // ENDPOINTS ADMINISTRATIVOS
    // ============================================

    /**
     * GET /api/suporte/admin/chamados
     * Lista todos os chamados do sistema (apenas admin).
     * Query params opcionais: status, categoria, urgencia, usuarioId
     * 
     * SEGURANÇA: Apenas username="admin" pode acessar
     */
    @GetMapping("/admin/chamados")
    public ResponseEntity<?> listarTodosChamadosAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String urgencia,
            @RequestParam(required = false) Long usuarioId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado E é admin?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!"admin".equals(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado. Apenas administradores podem acessar."));
            }

            // Converter parâmetros
            StatusChamado statusEnum = status != null ? StatusChamado.valueOf(status) : null;
            CategoriaSuporte categoriaEnum = categoria != null ? CategoriaSuporte.valueOf(categoria) : null;
            NivelUrgencia urgenciaEnum = urgencia != null ? NivelUrgencia.valueOf(urgencia) : null;

            List<ChamadoSuporte> chamados = chamadoService.listarChamadosFiltrados(
                statusEnum,
                categoriaEnum,
                urgenciaEnum,
                usuarioId
            );

            // Admin pode ver observações internas - não removemos

            return ResponseEntity.ok(chamados);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Parâmetro inválido: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar chamados: " + e.getMessage()));
        }
    }

    /**
     * GET /api/suporte/admin/chamados/{numero}
     * Busca chamado específico com todas as informações (apenas admin).
     * Inclui observações internas.
     * 
     * SEGURANÇA: Apenas username="admin" pode acessar
     */
    @GetMapping("/admin/chamados/{numero}")
    public ResponseEntity<?> buscarChamadoAdmin(
            @PathVariable String numero,
            HttpSession session) {
        try {
            // Validação: Admin?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!"admin".equals(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado"));
            }

            Optional<ChamadoSuporte> chamadoOpt = chamadoService.buscarPorNumero(numero);

            if (chamadoOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Chamado não encontrado"));
            }

            // Admin vê observações internas
            ChamadoSuporte chamado = chamadoOpt.get();

            // Marcar mensagens como lidas pelo admin
            chamadoService.marcarMensagensComoLidas(numero, "ADMIN");

            return ResponseEntity.ok(chamado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao buscar chamado: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/suporte/admin/chamados/{numero}/status
     * Altera o status de um chamado (apenas admin).
     * 
     * Body JSON: { "status": "EM_ATENDIMENTO" }
     * 
     * SEGURANÇA: Apenas username="admin" pode alterar status
     */
    @PutMapping("/admin/chamados/{numero}/status")
    public ResponseEntity<?> alterarStatus(
            @PathVariable String numero,
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            // Validação: Admin?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!"admin".equals(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado"));
            }

            String novoStatusStr = dados.get("status");
            if (novoStatusStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Status é obrigatório"));
            }

            StatusChamado novoStatus = StatusChamado.valueOf(novoStatusStr);
            ChamadoSuporte chamado = chamadoService.alterarStatus(numero, novoStatus, usuario);

            return ResponseEntity.ok(chamado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Status inválido: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao alterar status: " + e.getMessage()));
        }
    }

    /**
     * POST /api/suporte/admin/chamados/{numero}/mensagens
     * Admin responde ao chamado.
     * 
     * Body JSON: { "mensagem": "Texto da resposta" }
     * 
     * SEGURANÇA: Apenas admin pode usar este endpoint
     */
    @PostMapping("/admin/chamados/{numero}/mensagens")
    public ResponseEntity<?> responderChamado(
            @PathVariable String numero,
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            // Validação: Admin?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!"admin".equals(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado"));
            }

            String mensagemTexto = dados.get("mensagem");
            if (mensagemTexto == null || mensagemTexto.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Mensagem é obrigatória"));
            }

            MensagemChamado mensagem = chamadoService.enviarMensagem(
                numero,
                usuario.getId(),
                "ADMIN",
                mensagemTexto
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao enviar mensagem: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/suporte/admin/chamados/{numero}/observacoes
     * Atualiza observações internas (apenas admin).
     * 
     * Body JSON: { "observacoes": "Notas internas do admin" }
     * 
     * SEGURANÇA: Observações NUNCA são expostas ao usuário
     */
    @PutMapping("/admin/chamados/{numero}/observacoes")
    public ResponseEntity<?> atualizarObservacoes(
            @PathVariable String numero,
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            // Validação: Admin?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!"admin".equals(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado"));
            }

            String observacoes = dados.get("observacoes");

            ChamadoSuporte chamado = chamadoService.atualizarObservacoesInternas(numero, observacoes);

            return ResponseEntity.ok(chamado);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao atualizar observações: " + e.getMessage()));
        }
    }

    /**
     * GET /api/suporte/admin/estatisticas
     * Retorna estatísticas do suporte (dashboard admin).
     * 
     * SEGURANÇA: Apenas admin
     */
    @GetMapping("/admin/estatisticas")
    public ResponseEntity<?> obterEstatisticas(HttpSession session) {
        try {
            // Validação: Admin?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!"admin".equals(usuario.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Acesso negado"));
            }

            Map<String, Object> estatisticas = chamadoService.obterEstatisticas();

            return ResponseEntity.ok(estatisticas);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao obter estatísticas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/suporte/categorias
     * Lista todas as categorias disponíveis para facilitar frontend.
     */
    @GetMapping("/categorias")
    public ResponseEntity<?> listarCategorias() {
        Map<String, String> categorias = new HashMap<>();
        for (CategoriaSuporte cat : CategoriaSuporte.values()) {
            categorias.put(cat.name(), cat.getNome());
        }
        return ResponseEntity.ok(categorias);
    }

    /**
     * GET /api/suporte/urgencias
     * Lista todos os níveis de urgência disponíveis.
     */
    @GetMapping("/urgencias")
    public ResponseEntity<?> listarUrgencias() {
        Map<String, String> urgencias = new HashMap<>();
        for (NivelUrgencia urg : NivelUrgencia.values()) {
            urgencias.put(urg.name(), urg.getNome());
        }
        return ResponseEntity.ok(urgencias);
    }

    /**
     * GET /api/suporte/status
     * Lista todos os status possíveis.
     */
    @GetMapping("/status")
    public ResponseEntity<?> listarStatus() {
        Map<String, String> status = new HashMap<>();
        for (StatusChamado st : StatusChamado.values()) {
            status.put(st.name(), st.getNome());
        }
        return ResponseEntity.ok(status);
    }
}
