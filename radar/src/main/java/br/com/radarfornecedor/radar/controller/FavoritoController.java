package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.FavoritoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gerenciar favoritos de produtos e fornecedores.
 * 
 * SEGURANÇA:
 * - Todos endpoints validam autenticação via HttpSession
 * - Cada usuário só acessa seus próprios favoritos
 * - Validações no backend (não confia em parâmetros do frontend)
 * 
 * ENDPOINTS:
 * - POST   /api/favoritos/produtos/{id}          - Favoritar produto
 * - DELETE /api/favoritos/produtos/{id}          - Desfavoritar produto
 * - GET    /api/favoritos/produtos               - Listar produtos favoritos
 * - GET    /api/favoritos/produtos/{id}/status   - Verificar se produto está favoritado
 * - POST   /api/favoritos/fornecedores/{id}      - Favoritar fornecedor
 * - DELETE /api/favoritos/fornecedores/{id}      - Desfavoritar fornecedor
 * - GET    /api/favoritos/fornecedores           - Listar fornecedores favoritos
 * - GET    /api/favoritos/fornecedores/{id}/status - Verificar se fornecedor está favoritado
 * - PUT    /api/favoritos/fornecedores/{id}/notificacoes - Ativar/desativar notificações
 * - GET    /api/favoritos/estatisticas           - Estatísticas de favoritos
 */
@RestController
@RequestMapping("/api/favoritos")
public class FavoritoController {

    private final FavoritoService favoritoService;

    public FavoritoController(FavoritoService favoritoService) {
        this.favoritoService = favoritoService;
    }

    // ======================== PRODUTOS FAVORITOS ========================

    /**
     * POST /api/favoritos/produtos/{id}
     * Favorita um produto.
     * 
     * @param produtoId ID do produto a favoritar
     * @param session HttpSession para identificar usuário
     * @return 201 Created com favorito criado, ou erro
     */
    @PostMapping("/produtos/{produtoId}")
    public ResponseEntity<?> favoritarProduto(
            @PathVariable Long produtoId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Favoritar produto
            var favorito = favoritoService.favoritarProduto(usuario.getId(), produtoId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "sucesso", true,
                            "mensagem", "Produto adicionado aos favoritos",
                            "favoritoId", favorito.getId(),
                            "produtoId", produtoId
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao favoritar produto: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/favoritos/produtos/{id}
     * Remove um produto dos favoritos.
     * 
     * @param produtoId ID do produto a desfavoritar
     * @param session HttpSession para identificar usuário
     * @return 200 OK se removeu, 404 se não estava favoritado
     */
    @DeleteMapping("/produtos/{produtoId}")
    public ResponseEntity<?> desfavoritarProduto(
            @PathVariable Long produtoId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Desfavoritar produto
            boolean removido = favoritoService.desfavoritarProduto(usuario.getId(), produtoId);

            if (removido) {
                return ResponseEntity.ok(Map.of(
                        "sucesso", true,
                        "mensagem", "Produto removido dos favoritos"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Produto não estava nos favoritos"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao desfavoritar produto: " + e.getMessage()));
        }
    }

    /**
     * GET /api/favoritos/produtos
     * Lista todos os produtos favoritos do usuário autenticado.
     * Retorna dados enriquecidos com informações do produto e fornecedor.
     * 
     * @param session HttpSession para identificar usuário
     * @return Lista de produtos favoritos com dados completos
     */
    @GetMapping("/produtos")
    public ResponseEntity<?> listarProdutosFavoritos(HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Listar favoritos
            List<Map<String, Object>> favoritos = favoritoService.listarProdutosFavoritos(usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "quantidade", favoritos.size(),
                    "favoritos", favoritos
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar favoritos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/favoritos/produtos/{id}/status
     * Verifica se um produto está favoritado pelo usuário.
     * Útil para renderizar estado do botão de favorito.
     * 
     * @param produtoId ID do produto
     * @param session HttpSession para identificar usuário
     * @return { favoritado: true/false }
     */
    @GetMapping("/produtos/{produtoId}/status")
    public ResponseEntity<?> verificarStatusProduto(
            @PathVariable Long produtoId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                // Se não autenticado, retornar false (não está favoritado)
                return ResponseEntity.ok(Map.of("favoritado", false));
            }

            // Verificar status
            boolean favoritado = favoritoService.isProdutoFavoritado(usuario.getId(), produtoId);

            return ResponseEntity.ok(Map.of("favoritado", favoritado));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao verificar status: " + e.getMessage()));
        }
    }

    // ======================== FORNECEDORES FAVORITOS ========================

    /**
     * POST /api/favoritos/fornecedores/{id}
     * Favorita/segue um fornecedor.
     * 
     * @param fornecedorId ID do fornecedor a favoritar
     * @param session HttpSession para identificar usuário
     * @return 201 Created com favorito criado, ou erro
     */
    @PostMapping("/fornecedores/{fornecedorId}")
    public ResponseEntity<?> favoritarFornecedor(
            @PathVariable Long fornecedorId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Favoritar fornecedor
            var favorito = favoritoService.favoritarFornecedor(usuario.getId(), fornecedorId);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "sucesso", true,
                            "mensagem", "Fornecedor adicionado aos favoritos",
                            "favoritoId", favorito.getId(),
                            "fornecedorId", fornecedorId
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao favoritar fornecedor: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/favoritos/fornecedores/{id}
     * Remove um fornecedor dos favoritos (deixa de seguir).
     * 
     * @param fornecedorId ID do fornecedor a desfavoritar
     * @param session HttpSession para identificar usuário
     * @return 200 OK se removeu, 404 se não estava favoritado
     */
    @DeleteMapping("/fornecedores/{fornecedorId}")
    public ResponseEntity<?> desfavoritarFornecedor(
            @PathVariable Long fornecedorId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Desfavoritar fornecedor
            boolean removido = favoritoService.desfavoritarFornecedor(usuario.getId(), fornecedorId);

            if (removido) {
                return ResponseEntity.ok(Map.of(
                        "sucesso", true,
                        "mensagem", "Fornecedor removido dos favoritos"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Fornecedor não estava nos favoritos"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao desfavoritar fornecedor: " + e.getMessage()));
        }
    }

    /**
     * GET /api/favoritos/fornecedores
     * Lista todos os fornecedores favoritos/seguidos pelo usuário.
     * Retorna dados enriquecidos com informações do fornecedor.
     * 
     * @param session HttpSession para identificar usuário
     * @return Lista de fornecedores favoritos com dados completos
     */
    @GetMapping("/fornecedores")
    public ResponseEntity<?> listarFornecedoresFavoritos(HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Listar favoritos
            List<Map<String, Object>> favoritos = favoritoService.listarFornecedoresFavoritos(usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "quantidade", favoritos.size(),
                    "favoritos", favoritos
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar favoritos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/favoritos/fornecedores/{id}/status
     * Verifica se um fornecedor está favoritado pelo usuário.
     * 
     * @param fornecedorId ID do fornecedor
     * @param session HttpSession para identificar usuário
     * @return { favoritado: true/false }
     */
    @GetMapping("/fornecedores/{fornecedorId}/status")
    public ResponseEntity<?> verificarStatusFornecedor(
            @PathVariable Long fornecedorId,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                // Se não autenticado, retornar false
                return ResponseEntity.ok(Map.of("favoritado", false));
            }

            // Verificar status
            boolean favoritado = favoritoService.isFornecedorFavoritado(usuario.getId(), fornecedorId);

            return ResponseEntity.ok(Map.of("favoritado", favoritado));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao verificar status: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/favoritos/fornecedores/{id}/notificacoes
     * Atualiza configuração de notificações para um fornecedor favoritado.
     * Permite "silenciar" notificações sem desfavoritar.
     * 
     * Body: { "notificar": true/false }
     * 
     * @param fornecedorId ID do fornecedor
     * @param body Dados da requisição
     * @param session HttpSession para identificar usuário
     * @return 200 OK se atualizou, 404 se não estava favoritado
     */
    @PutMapping("/fornecedores/{fornecedorId}/notificacoes")
    public ResponseEntity<?> atualizarNotificacoes(
            @PathVariable Long fornecedorId,
            @RequestBody Map<String, Object> body,
            HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Validar parâmetro
            if (!body.containsKey("notificar")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Parâmetro 'notificar' é obrigatório"));
            }

            Boolean notificar = (Boolean) body.get("notificar");

            // Atualizar notificações
            boolean atualizado = favoritoService.atualizarNotificacoesFornecedor(
                    usuario.getId(), fornecedorId, notificar);

            if (atualizado) {
                String mensagem = notificar 
                        ? "Notificações ativadas para este fornecedor" 
                        : "Notificações desativadas para este fornecedor";
                
                return ResponseEntity.ok(Map.of(
                        "sucesso", true,
                        "mensagem", mensagem
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Fornecedor não está nos favoritos"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao atualizar notificações: " + e.getMessage()));
        }
    }

    // ======================== ESTATÍSTICAS ========================

    /**
     * GET /api/favoritos/estatisticas
     * Retorna estatísticas de favoritos do usuário.
     * 
     * @param session HttpSession para identificar usuário
     * @return Contadores de produtos e fornecedores favoritos
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<?> obterEstatisticas(HttpSession session) {
        try {
            // Validação: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            // Buscar estatísticas
            long totalProdutos = favoritoService.contarProdutosFavoritos(usuario.getId());
            long totalFornecedores = favoritoService.contarFornecedoresFavoritos(usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "totalProdutosFavoritos", totalProdutos,
                    "totalFornecedoresFavoritos", totalFornecedores,
                    "totalGeral", totalProdutos + totalFornecedores
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao buscar estatísticas: " + e.getMessage()));
        }
    }
}
