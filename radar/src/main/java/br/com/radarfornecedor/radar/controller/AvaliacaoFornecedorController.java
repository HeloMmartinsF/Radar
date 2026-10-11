package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.AvaliacaoFornecedor;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.AvaliacaoFornecedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AvaliacaoFornecedorController - Endpoints REST para sistema de avaliações de fornecedores
 * 
 * Endpoints principais:
 * - GET /api/avaliacoes-fornecedor/{fornecedorId} - Lista avaliações
 * - GET /api/avaliacoes-fornecedor/{fornecedorId}/estatisticas - Estatísticas
 * - POST /api/avaliacoes-fornecedor - Criar avaliação
 * - PUT /api/avaliacoes-fornecedor/{id}/resposta - Responder avaliação
 * - GET /api/avaliacoes-fornecedor/minhas - Avaliações feitas pelo usuário logado
 */
@RestController
@RequestMapping("/api/avaliacoes-fornecedor")
public class AvaliacaoFornecedorController {

    private final AvaliacaoFornecedorService avaliacaoService;

    public AvaliacaoFornecedorController(AvaliacaoFornecedorService avaliacaoService) {
        this.avaliacaoService = avaliacaoService;
    }

    // ========================================
    // LISTAR AVALIAÇÕES
    // ========================================

    /**
     * GET /api/avaliacoes-fornecedor/{fornecedorId}
     * Lista todas as avaliações de um fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista de avaliações com dados enriquecidos
     */
    @GetMapping("/{fornecedorId}")
    public ResponseEntity<?> listarAvaliacoes(@PathVariable Long fornecedorId) {
        try {
            List<Map<String, Object>> avaliacoes = avaliacaoService.listarAvaliacoes(fornecedorId);
            return ResponseEntity.ok(avaliacoes);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar avaliações: " + e.getMessage()));
        }
    }

    /**
     * GET /api/avaliacoes-fornecedor/{fornecedorId}/estatisticas
     * Retorna estatísticas de avaliações de um fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @return Map com média, total, distribuição por estrelas
     */
    @GetMapping("/{fornecedorId}/estatisticas")
    public ResponseEntity<?> obterEstatisticas(@PathVariable Long fornecedorId) {
        try {
            Map<String, Object> stats = avaliacaoService.calcularEstatisticas(fornecedorId);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao calcular estatísticas: " + e.getMessage()));
        }
    }

    /**
     * GET /api/avaliacoes-fornecedor/{fornecedorId}/pendentes
     * Lista avaliações que ainda não foram respondidas pelo fornecedor
     * Acesso restrito ao próprio fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @param session Sessão HTTP
     * @return Lista de avaliações pendentes de resposta
     */
    @GetMapping("/{fornecedorId}/pendentes")
    public ResponseEntity<?> listarPendentes(@PathVariable Long fornecedorId, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            List<Map<String, Object>> pendentes = avaliacaoService.listarAvaliacoesPendentes(fornecedorId);
            return ResponseEntity.ok(pendentes);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar avaliações pendentes: " + e.getMessage()));
        }
    }

    /**
     * GET /api/avaliacoes-fornecedor/minhas
     * Lista todas as avaliações feitas pelo usuário logado
     * 
     * @param session Sessão HTTP
     * @return Lista de avaliações do usuário
     */
    @GetMapping("/minhas")
    public ResponseEntity<?> listarMinhasAvaliacoes(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            List<Map<String, Object>> avaliacoes = avaliacaoService.listarAvaliacoesDoUsuario(usuario.getId());
            return ResponseEntity.ok(avaliacoes);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar suas avaliações: " + e.getMessage()));
        }
    }

    /**
     * GET /api/avaliacoes-fornecedor/{fornecedorId}/ja-avaliou
     * Verifica se o usuário logado já avaliou determinado fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @param session Sessão HTTP
     * @return { "jaAvaliou": true/false }
     */
    @GetMapping("/{fornecedorId}/ja-avaliou")
    public ResponseEntity<?> verificarSeJaAvaliou(@PathVariable Long fornecedorId, HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.ok(Map.of("jaAvaliou", false, "motivo", "Usuário não autenticado"));
            }

            boolean jaAvaliou = avaliacaoService.usuarioJaAvaliou(fornecedorId, usuario.getId());
            return ResponseEntity.ok(Map.of("jaAvaliou", jaAvaliou));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao verificar avaliação: " + e.getMessage()));
        }
    }

    // ========================================
    // CRIAR AVALIAÇÃO
    // ========================================

    /**
     * POST /api/avaliacoes-fornecedor
     * Cria uma nova avaliação de fornecedor
     * 
     * Body esperado:
     * {
     *   "fornecedorId": 123,
     *   "nota": 5,
     *   "comentario": "Excelente fornecedor!"
     * }
     * 
     * @param request Dados da avaliação
     * @param session Sessão HTTP
     * @return Avaliação criada ou erro de validação
     */
    @PostMapping
    public ResponseEntity<?> criarAvaliacao(
            @Valid @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado. Faça login para avaliar."));
            }

            // Validar campos obrigatórios
            if (!request.containsKey("fornecedorId") || !request.containsKey("nota")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Campos fornecedorId e nota são obrigatórios"));
            }

            // Criar entidade AvaliacaoFornecedor
            AvaliacaoFornecedor avaliacao = new AvaliacaoFornecedor();
            avaliacao.setFornecedorId(Long.valueOf(request.get("fornecedorId").toString()));
            avaliacao.setNota(Integer.valueOf(request.get("nota").toString()));
            
            if (request.containsKey("comentario") && request.get("comentario") != null) {
                avaliacao.setComentario(request.get("comentario").toString());
            }

            // Service valida regras de negócio
            AvaliacaoFornecedor avaliacaoCriada = avaliacaoService.adicionarAvaliacao(avaliacao, usuario);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Avaliação enviada com sucesso!",
                    "avaliacao", avaliacaoCriada
            ));

        } catch (IllegalArgumentException e) {
            // Erros de validação de negócio
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao criar avaliação: " + e.getMessage()));
        }
    }

    // ========================================
    // RESPONDER AVALIAÇÃO
    // ========================================

    /**
     * PUT /api/avaliacoes-fornecedor/{id}/resposta
     * Permite que o fornecedor responda a uma avaliação
     * 
     * Body esperado:
     * {
     *   "resposta": "Obrigado pelo feedback!"
     * }
     * 
     * @param id ID da avaliação
     * @param request Dados com a resposta
     * @param session Sessão HTTP
     * @return Avaliação atualizada ou erro
     */
    @PutMapping("/{id}/resposta")
    public ResponseEntity<?> responderAvaliacao(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!request.containsKey("resposta")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Campo 'resposta' é obrigatório"));
            }

            String resposta = request.get("resposta");
            
            // Service valida se usuário é o fornecedor avaliado
            AvaliacaoFornecedor avaliacaoAtualizada = avaliacaoService.responderAvaliacao(id, resposta, usuario);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Resposta publicada com sucesso!",
                    "avaliacao", avaliacaoAtualizada
            ));

        } catch (IllegalArgumentException e) {
            // Erros de validação de negócio
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao responder avaliação: " + e.getMessage()));
        }
    }

    // ========================================
    // EDITAR RESPOSTA
    // ========================================

    /**
     * PUT /api/avaliacoes-fornecedor/{id}/editar-resposta
     * Permite que o fornecedor edite sua resposta a uma avaliação
     * 
     * @param id ID da avaliação
     * @param request Dados com a nova resposta
     * @param session Sessão HTTP
     * @return Avaliação atualizada
     */
    @PutMapping("/{id}/editar-resposta")
    public ResponseEntity<?> editarResposta(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            if (!request.containsKey("resposta")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Campo 'resposta' é obrigatório"));
            }

            String novaResposta = request.get("resposta");
            
            // Reutiliza o mesmo método do Service
            AvaliacaoFornecedor avaliacaoAtualizada = avaliacaoService.responderAvaliacao(id, novaResposta, usuario);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Resposta atualizada com sucesso!",
                    "avaliacao", avaliacaoAtualizada
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao editar resposta: " + e.getMessage()));
        }
    }

    // ========================================
    // RESUMO RÁPIDO (para exibição em cards)
    // ========================================

    /**
     * GET /api/avaliacoes-fornecedor/{fornecedorId}/resumo
     * Retorna apenas média e total (para exibição rápida em cards/listagens)
     * 
     * @param fornecedorId ID do fornecedor
     * @return { "media": 4.5, "total": 127 }
     */
    @GetMapping("/{fornecedorId}/resumo")
    public ResponseEntity<?> obterResumo(@PathVariable Long fornecedorId) {
        try {
            Map<String, Object> resumo = new HashMap<>();
            resumo.put("media", avaliacaoService.obterMediaAvaliacoes(fornecedorId));
            resumo.put("total", avaliacaoService.contarAvaliacoes(fornecedorId));
            
            return ResponseEntity.ok(resumo);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao obter resumo: " + e.getMessage()));
        }
    }
}
