package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Notificacao;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller para gerenciar notificações dos usuários
 * 
 * AUTORIZAÇÃO:
 * - Apenas usuários autenticados podem acessar
 * - Cada usuário só pode ver e modificar suas próprias notificações
 * - Validação no backend (não confia em parâmetros do frontend)
 */
@RestController
@RequestMapping("/api/notificacoes")
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    public NotificacaoController(NotificacaoService notificacaoService) {
        this.notificacaoService = notificacaoService;
    }

    /**
     * GET /api/notificacoes
     * Lista todas as notificações do usuário autenticado
     * 
     * Query Params:
     * - limite: número máximo de notificações (opcional)
     * - apenasNaoLidas: se true, retorna apenas não lidas (opcional)
     * 
     * SEGURANÇA:
     * - Valida autenticação na sessão
     * - Retorna apenas notificações do próprio usuário
     */
    @GetMapping
    public ResponseEntity<?> listarNotificacoes(
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false, defaultValue = "false") Boolean apenasNaoLidas,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            List<Notificacao> notificacoes;

            if (Boolean.TRUE.equals(apenasNaoLidas)) {
                // Apenas não lidas
                notificacoes = notificacaoService.listarNotificacoesNaoLidas(usuario.getId());
            } else if (limite != null && limite > 0) {
                // Com limite
                notificacoes = notificacaoService.listarUltimasNotificacoes(usuario.getId(), limite);
            } else {
                // Todas
                notificacoes = notificacaoService.listarNotificacoes(usuario.getId());
            }

            return ResponseEntity.ok(notificacoes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar notificações: " + e.getMessage()));
        }
    }

    /**
     * GET /api/notificacoes/nao-lidas/count
     * Retorna quantidade de notificações não lidas (para badge)
     * 
     * SEGURANÇA:
     * - Valida autenticação na sessão
     * - Retorna apenas contagem do próprio usuário
     */
    @GetMapping("/nao-lidas/count")
    public ResponseEntity<?> contarNaoLidas(HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Long count = notificacaoService.contarNaoLidas(usuario.getId());

            return ResponseEntity.ok(Map.of(
                "count", count,
                "sucesso", true
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao contar notificações: " + e.getMessage()));
        }
    }

    /**
     * GET /api/notificacoes/{id}
     * Busca uma notificação específica
     * 
     * SEGURANÇA:
     * - Valida que a notificação pertence ao usuário autenticado
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarNotificacao(
            @PathVariable Long id,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Optional<Notificacao> notificacao = notificacaoService.buscarNotificacao(id, usuario.getId());

            if (notificacao.isPresent()) {
                return ResponseEntity.ok(notificacao.get());
            } else {
                return ResponseEntity.status(404)
                        .body(Map.of("erro", "Notificação não encontrada ou não pertence ao usuário"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao buscar notificação: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/notificacoes/{id}/ler
     * Marca uma notificação como lida
     * 
     * SEGURANÇA:
     * - Valida que a notificação pertence ao usuário autenticado
     * - Não permite marcar notificações de outros usuários
     */
    @PutMapping("/{id}/ler")
    public ResponseEntity<?> marcarComoLida(
            @PathVariable Long id,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            boolean sucesso = notificacaoService.marcarComoLida(id, usuario.getId());

            if (sucesso) {
                return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Notificação marcada como lida"
                ));
            } else {
                return ResponseEntity.status(404)
                        .body(Map.of("erro", "Notificação não encontrada ou não pertence ao usuário"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao marcar notificação como lida: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/notificacoes/ler-todas
     * Marca todas as notificações do usuário como lidas
     * 
     * SEGURANÇA:
     * - Valida autenticação na sessão
     * - Marca apenas notificações do próprio usuário
     */
    @PutMapping("/ler-todas")
    public ResponseEntity<?> marcarTodasComoLidas(HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            int quantidadeMarcada = notificacaoService.marcarTodasComoLidas(usuario.getId());

            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "quantidadeMarcada", quantidadeMarcada,
                "mensagem", quantidadeMarcada + " notificação(ões) marcada(s) como lida(s)"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao marcar notificações como lidas: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/notificacoes/{id}
     * Deleta uma notificação específica
     * 
     * SEGURANÇA:
     * - Valida que a notificação pertence ao usuário autenticado
     * - Não permite deletar notificações de outros usuários
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarNotificacao(
            @PathVariable Long id,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            boolean sucesso = notificacaoService.deletarNotificacao(id, usuario.getId());

            if (sucesso) {
                return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Notificação deletada com sucesso"
                ));
            } else {
                return ResponseEntity.status(404)
                        .body(Map.of("erro", "Notificação não encontrada ou não pertence ao usuário"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao deletar notificação: " + e.getMessage()));
        }
    }
}
