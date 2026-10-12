package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.dto.IniciarConversaRequest;
import br.com.radarfornecedor.radar.model.Conversa;
import br.com.radarfornecedor.radar.model.MensagemChat;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.ChatService;
import javax.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST para gerenciamento do Chat Comercial.
 * Fornece endpoints para conversas e mensagens entre clientes, fornecedores e representantes.
 */
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * POST /api/chat/conversas
     * Inicia uma nova conversa ou retorna conversa existente (deduplicação).
     * 
     * Body: {
     *   "destinatarioId": Long,
     *   "fornecedorId": Long,
     *   "contexto": "PRODUTO" | "ORCAMENTO" | "GERAL",
     *   "contextoReferencia": Long (opcional, ID do produto/orçamento)
     * }
     * 
     * @param request Dados da conversa
     * @param session Sessão HTTP
     * @return Conversa criada ou existente
     */
    @PostMapping("/conversas")
    public ResponseEntity<?> iniciarConversa(
            @RequestBody IniciarConversaRequest request,
            HttpSession session) {
        try {
            // Validação 1: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Validação 2: Campos obrigatórios
            if (request.getDestinatarioId() == null) {
                return ResponseEntity.status(400)
                        .body(Map.of("erro", "destinatarioId é obrigatório"));
            }
            if (request.getContexto() == null) {
                return ResponseEntity.status(400)
                        .body(Map.of("erro", "contexto é obrigatório"));
            }

            // Criar ou recuperar conversa
            Conversa conversa = chatService.iniciarConversa(
                    usuario.getId(),
                    request.getDestinatarioId(),
                    request.getFornecedorId(),
                    request.getContexto(),
                    request.getContextoReferencia()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(conversa);

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao iniciar conversa: " + e.getMessage()));
        }
    }

    /**
     * GET /api/chat/conversas
     * Lista todas as conversas do usuário logado.
     * 
     * @param session Sessão HTTP
     * @return Lista de conversas com informações resumidas
     */
    @GetMapping("/conversas")
    public ResponseEntity<?> listarConversas(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            List<Map<String, Object>> conversas = chatService.listarConversas(usuario.getId());
            return ResponseEntity.ok(conversas);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar conversas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/chat/conversas/{id}
     * Retorna detalhes de uma conversa específica.
     * 
     * @param id ID da conversa
     * @param session Sessão HTTP
     * @return Detalhes da conversa
     */
    @GetMapping("/conversas/{id}")
    public ResponseEntity<?> buscarConversa(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Map<String, Object> conversa = chatService.buscarConversa(id, usuario.getId());
            return ResponseEntity.ok(conversa);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao buscar conversa: " + e.getMessage()));
        }
    }

    /**
     * GET /api/chat/conversas/{id}/mensagens
     * Lista todas as mensagens de uma conversa.
     * 
     * @param id ID da conversa
     * @param session Sessão HTTP
     * @return Lista de mensagens ordenadas cronologicamente
     */
    @GetMapping("/conversas/{id}/mensagens")
    public ResponseEntity<?> listarMensagens(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            List<Map<String, Object>> mensagens = chatService.listarMensagens(id, usuario.getId());
            return ResponseEntity.ok(mensagens);

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar mensagens: " + e.getMessage()));
        }
    }

    /**
     * POST /api/chat/conversas/{id}/mensagens
     * Envia uma nova mensagem em uma conversa.
     * 
     * Body: {
     *   "texto": "Conteúdo da mensagem..."
     * }
     * 
     * @param id ID da conversa
     * @param request Dados da mensagem
     * @param session Sessão HTTP
     * @return Mensagem criada
     */
    @PostMapping("/conversas/{id}/mensagens")
    public ResponseEntity<?> enviarMensagem(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            String texto = request.get("texto");
            if (texto == null || texto.trim().isEmpty()) {
                return ResponseEntity.status(400)
                        .body(Map.of("erro", "Texto da mensagem é obrigatório"));
            }

            MensagemChat mensagem = chatService.enviarMensagem(id, usuario.getId(), texto);
            return ResponseEntity.status(HttpStatus.CREATED).body(mensagem);

        } catch (RuntimeException e) {
            return ResponseEntity.status(400)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao enviar mensagem: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/chat/conversas/{id}/marcar-lidas
     * Marca todas as mensagens da conversa como lidas.
     * 
     * @param id ID da conversa
     * @param session Sessão HTTP
     * @return Sucesso
     */
    @PutMapping("/conversas/{id}/marcar-lidas")
    public ResponseEntity<?> marcarComoLidas(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            chatService.marcarComoLidas(id, usuario.getId());
            return ResponseEntity.ok(Map.of("sucesso", true));

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao marcar como lidas: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/chat/conversas/{id}
     * Encerra uma conversa (marca como inativa).
     * 
     * @param id ID da conversa
     * @param session Sessão HTTP
     * @return Sucesso
     */
    @DeleteMapping("/conversas/{id}")
    public ResponseEntity<?> encerrarConversa(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            chatService.encerrarConversa(id, usuario.getId());
            return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Conversa encerrada"));

        } catch (RuntimeException e) {
            return ResponseEntity.status(403)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao encerrar conversa: " + e.getMessage()));
        }
    }

    /**
     * GET /api/chat/nao-lidas
     * Retorna o total de mensagens não lidas do usuário (todas as conversas).
     * Usado para exibir badge no menu.
     * 
     * @param session Sessão HTTP
     * @return Total de mensagens não lidas
     */
    @GetMapping("/nao-lidas")
    public ResponseEntity<?> contarNaoLidas(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Long total = chatService.contarNaoLidas(usuario.getId());
            return ResponseEntity.ok(Map.of("total", total));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao contar não lidas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/chat/pode-iniciar
     * Verifica se o usuário pode iniciar conversa com um fornecedor.
     * Valida permissões de representante.
     * 
     * Query params: fornecedorId
     * 
     * @param fornecedorId ID do fornecedor
     * @param session Sessão HTTP
     * @return { "podeIniciar": true/false, "motivo": "..." }
     */
    @GetMapping("/pode-iniciar")
    public ResponseEntity<?> verificarPermissao(
            @RequestParam Long fornecedorId,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Map<String, Object> resultado = chatService.verificarPermissao(usuario.getId(), fornecedorId);
            return ResponseEntity.ok(resultado);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao verificar permissão: " + e.getMessage()));
        }
    }
}
