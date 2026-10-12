package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.ItemListaCompras;
import br.com.radarfornecedor.radar.model.ListaCompras;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.ListaComprasService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para gerenciar listas de compras.
 * 
 * SEGURANÇA:
 * - Todos endpoints validam autenticação via HttpSession
 * - Cada usuário só acessa suas próprias listas
 * - Validações no backend
 * 
 * ENDPOINTS:
 * - GET    /api/lista-compras                      - Listar listas ativas
 * - POST   /api/lista-compras                      - Criar lista
 * - GET    /api/lista-compras/{id}                 - Buscar lista específica
 * - PUT    /api/lista-compras/{id}                 - Atualizar lista
 * - DELETE /api/lista-compras/{id}                 - Deletar lista
 * - POST   /api/lista-compras/{id}/itens           - Adicionar item
 * - GET    /api/lista-compras/{id}/itens           - Listar itens
 * - PUT    /api/lista-compras/itens/{itemId}       - Atualizar item
 * - DELETE /api/lista-compras/itens/{itemId}       - Remover item
 * - PUT    /api/lista-compras/itens/{itemId}/concluir - Marcar concluído
 * - GET    /api/lista-compras/{id}/total           - Calcular valor total
 * - GET    /api/lista-compras/{id}/estatisticas    - Estatísticas da lista
 */
@RestController
@RequestMapping("/api/lista-compras")
public class ListaComprasController {

    private final ListaComprasService listaComprasService;

    public ListaComprasController(ListaComprasService listaComprasService) {
        this.listaComprasService = listaComprasService;
    }

    // ======================== LISTAS ========================

    /**
     * GET /api/lista-compras
     * Lista todas as listas ativas do usuário.
     */
    @GetMapping
    public ResponseEntity<?> listarListas(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            List<ListaCompras> listas = listaComprasService.listarListasAtivas(usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "quantidade", listas.size(),
                    "listas", listas
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar listas: " + e.getMessage()));
        }
    }

    /**
     * POST /api/lista-compras
     * Cria uma nova lista de compras.
     * Body: { "nome": "...", "descricao": "..." }
     */
    @PostMapping
    public ResponseEntity<?> criarLista(
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            String nome = dados.get("nome");
            if (nome == null || nome.isBlank()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Nome da lista é obrigatório"));
            }

            String descricao = dados.get("descricao");

            ListaCompras lista = listaComprasService.criarLista(usuario.getId(), nome, descricao);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "sucesso", true,
                            "mensagem", "Lista criada com sucesso",
                            "lista", lista
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao criar lista: " + e.getMessage()));
        }
    }

    /**
     * GET /api/lista-compras/{id}
     * Busca uma lista específica com seus itens.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarLista(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            Optional<ListaCompras> listaOpt = listaComprasService.buscarLista(id, usuario.getId());

            if (listaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Lista não encontrada"));
            }

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "lista", listaOpt.get()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao buscar lista: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/lista-compras/{id}
     * Atualiza nome/descrição de uma lista.
     * Body: { "nome": "...", "descricao": "..." }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarLista(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            String nome = dados.get("nome");
            String descricao = dados.get("descricao");

            ListaCompras lista = listaComprasService.atualizarLista(
                    id, usuario.getId(), nome, descricao);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Lista atualizada com sucesso",
                    "lista", lista
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao atualizar lista: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/lista-compras/{id}
     * Deleta uma lista e todos seus itens.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarLista(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            boolean deletado = listaComprasService.deletarLista(id, usuario.getId());

            if (deletado) {
                return ResponseEntity.ok(Map.of(
                        "sucesso", true,
                        "mensagem", "Lista deletada com sucesso"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Lista não encontrada"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao deletar lista: " + e.getMessage()));
        }
    }

    // ======================== ITENS ========================

    /**
     * POST /api/lista-compras/{id}/itens
     * Adiciona um item à lista.
     * Body: { "produtoId": 123, "quantidade": 5.0, "observacoes": "..." }
     * OU
     * Body: { "publicacaoId": 456, "quantidade": 2.0 }
     */
    @PostMapping("/{id}/itens")
    public ResponseEntity<?> adicionarItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            Double quantidade = dados.get("quantidade") != null 
                    ? Double.valueOf(dados.get("quantidade").toString()) 
                    : 1.0;

            ItemListaCompras item;

            // Verificar se é de publicação ou produto direto
            if (dados.containsKey("publicacaoId")) {
                Long publicacaoId = Long.valueOf(dados.get("publicacaoId").toString());
                item = listaComprasService.adicionarItemDePublicacao(
                        id, usuario.getId(), publicacaoId, quantidade);
            } else if (dados.containsKey("produtoId")) {
                Long produtoId = Long.valueOf(dados.get("produtoId").toString());
                String observacoes = (String) dados.get("observacoes");
                item = listaComprasService.adicionarItem(
                        id, usuario.getId(), produtoId, quantidade, observacoes);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("erro", "Informe produtoId ou publicacaoId"));
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "sucesso", true,
                            "mensagem", "Item adicionado à lista",
                            "item", item
                    ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao adicionar item: " + e.getMessage()));
        }
    }

    /**
     * GET /api/lista-compras/{id}/itens
     * Lista itens de uma lista com dados enriquecidos.
     */
    @GetMapping("/{id}/itens")
    public ResponseEntity<?> listarItens(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            List<Map<String, Object>> itens = listaComprasService.listarItensEnriquecidos(
                    id, usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "quantidade", itens.size(),
                    "itens", itens
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao listar itens: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/lista-compras/itens/{itemId}
     * Atualiza quantidade de um item.
     * Body: { "listaId": 1, "quantidade": 10.0 }
     */
    @PutMapping("/itens/{itemId}")
    public ResponseEntity<?> atualizarItem(
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            Long listaId = Long.valueOf(dados.get("listaId").toString());
            Double quantidade = Double.valueOf(dados.get("quantidade").toString());

            ItemListaCompras item = listaComprasService.atualizarQuantidade(
                    itemId, listaId, usuario.getId(), quantidade);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Quantidade atualizada",
                    "item", item
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao atualizar item: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/lista-compras/itens/{itemId}
     * Remove um item da lista.
     * Query param: listaId (obrigatório)
     */
    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<?> removerItem(
            @PathVariable Long itemId,
            @RequestParam Long listaId,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            boolean removido = listaComprasService.removerItem(itemId, listaId, usuario.getId());

            if (removido) {
                return ResponseEntity.ok(Map.of(
                        "sucesso", true,
                        "mensagem", "Item removido da lista"
                ));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Item não encontrado"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao remover item: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/lista-compras/itens/{itemId}/concluir
     * Marca item como concluído ou não concluído.
     * Body: { "listaId": 1, "concluido": true }
     */
    @PutMapping("/itens/{itemId}/concluir")
    public ResponseEntity<?> marcarConcluido(
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            Long listaId = Long.valueOf(dados.get("listaId").toString());
            Boolean concluido = (Boolean) dados.get("concluido");

            ItemListaCompras item = listaComprasService.marcarComoConcluido(
                    itemId, listaId, usuario.getId(), concluido);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", concluido ? "Item marcado como concluído" : "Item desmarcado",
                    "item", item
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao atualizar status: " + e.getMessage()));
        }
    }

    // ======================== CÁLCULOS E ESTATÍSTICAS ========================

    /**
     * GET /api/lista-compras/{id}/total
     * Calcula valor total estimado da lista.
     */
    @GetMapping("/{id}/total")
    public ResponseEntity<?> calcularTotal(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            Map<String, Object> resultado = listaComprasService.calcularValorEstimado(
                    id, usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "calculo", resultado
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao calcular total: " + e.getMessage()));
        }
    }

    /**
     * GET /api/lista-compras/{id}/estatisticas
     * Retorna estatísticas da lista (total, pendentes, concluídos, %).
     */
    @GetMapping("/{id}/estatisticas")
    public ResponseEntity<?> obterEstatisticas(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            Map<String, Object> stats = listaComprasService.obterEstatisticasLista(
                    id, usuario.getId());

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "estatisticas", stats
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao obter estatísticas: " + e.getMessage()));
        }
    }
}
