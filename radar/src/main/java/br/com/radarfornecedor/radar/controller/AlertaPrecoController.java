package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.AlertaPreco;
import br.com.radarfornecedor.radar.model.AlertaPreco.CriterioAlerta;
import br.com.radarfornecedor.radar.model.HistoricoPreco;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.AlertaPrecoService;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controller REST para gerenciamento de Alertas de Preço.
 * 
 * Endpoints disponíveis:
 * - POST   /api/alertas-preco              - Criar novo alerta
 * - GET    /api/alertas-preco              - Listar alertas do usuário
 * - GET    /api/alertas-preco/{id}         - Buscar alerta específico
 * - PUT    /api/alertas-preco/{id}         - Atualizar alerta
 * - DELETE /api/alertas-preco/{id}         - Deletar alerta
 * - GET    /api/alertas-preco/produto/{id} - Verificar se tem alerta para produto
 * - GET    /api/historico-precos/{produtoId} - Buscar histórico de preços
 */
@RestController
@RequestMapping("/api/alertas-preco")
public class AlertaPrecoController {
    
    @Autowired
    private AlertaPrecoService alertaPrecoService;
    
    // ==================== CRUD DE ALERTAS ====================
    
    /**
     * Cria um novo alerta de preço.
     * 
     * Body JSON:
     * {
     *   "produtoId": 123,
     *   "precoAlvo": 99.90,
     *   "criterio": "MENOR_OU_IGUAL",  // Opcional, padrão: MENOR_OU_IGUAL
     *   "notas": "Quero comprar quando estiver abaixo de R$ 100"  // Opcional
     * }
     */
    @PostMapping
    public ResponseEntity<?> criarAlerta(@RequestBody Map<String, Object> payload, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            Long produtoId = Long.valueOf(payload.get("produtoId").toString());
            BigDecimal precoAlvo = new BigDecimal(payload.get("precoAlvo").toString());
            
            CriterioAlerta criterio = CriterioAlerta.MENOR_OU_IGUAL;
            if (payload.containsKey("criterio")) {
                criterio = CriterioAlerta.valueOf(payload.get("criterio").toString());
            }
            
            String notas = payload.containsKey("notas") ? payload.get("notas").toString() : null;
            
            AlertaPreco alerta = alertaPrecoService.criarAlerta(
                usuario.getId(),
                produtoId,
                precoAlvo,
                criterio,
                notas
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(alerta);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Lista todos os alertas do usuário logado.
     * 
     * Query params:
     * - ativos (boolean, opcional): Se true, retorna apenas alertas ativos. Default: true
     */
    @GetMapping
    public ResponseEntity<?> listarAlertas(
        @RequestParam(required = false, defaultValue = "true") Boolean ativos,
        HttpSession session
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            List<Map<String, Object>> alertas = ativos 
                ? alertaPrecoService.listarAlertasAtivos(usuario.getId())
                : alertaPrecoService.listarTodosAlertas(usuario.getId());
            
            return ResponseEntity.ok(alertas);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao listar alertas: " + e.getMessage());
        }
    }
    
    /**
     * Busca alerta específico por ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarAlerta(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            AlertaPreco alerta = alertaPrecoService.buscarAlerta(id, usuario.getId());
            return ResponseEntity.ok(alerta);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Atualiza um alerta existente.
     * 
     * Body JSON (todos os campos são opcionais):
     * {
     *   "precoAlvo": 89.90,
     *   "criterio": "MENOR",
     *   "ativo": true,
     *   "notas": "Atualizado para preço menor"
     * }
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarAlerta(
        @PathVariable Long id,
        @RequestBody Map<String, Object> payload,
        HttpSession session
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            BigDecimal precoAlvo = payload.containsKey("precoAlvo") 
                ? new BigDecimal(payload.get("precoAlvo").toString()) 
                : null;
            
            CriterioAlerta criterio = payload.containsKey("criterio")
                ? CriterioAlerta.valueOf(payload.get("criterio").toString())
                : null;
            
            Boolean ativo = payload.containsKey("ativo")
                ? Boolean.valueOf(payload.get("ativo").toString())
                : null;
            
            String notas = payload.containsKey("notas")
                ? payload.get("notas").toString()
                : null;
            
            AlertaPreco alerta = alertaPrecoService.atualizarAlerta(
                id,
                usuario.getId(),
                precoAlvo,
                criterio,
                ativo,
                notas
            );
            
            return ResponseEntity.ok(alerta);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao atualizar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Deleta um alerta permanentemente.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarAlerta(@PathVariable Long id, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            alertaPrecoService.deletarAlerta(id, usuario.getId());
            return ResponseEntity.ok("Alerta deletado com sucesso");
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao deletar alerta: " + e.getMessage());
        }
    }
    
    /**
     * Verifica se usuário tem alerta ativo para um produto específico.
     * 
     * Retorna: { "temAlerta": true/false }
     */
    @GetMapping("/produto/{produtoId}")
    public ResponseEntity<?> verificarAlertaPorProduto(@PathVariable Long produtoId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            boolean temAlerta = alertaPrecoService.temAlertaAtivo(usuario.getId(), produtoId);
            return ResponseEntity.ok(Map.of("temAlerta", temAlerta));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao verificar alerta: " + e.getMessage());
        }
    }
    
    // ==================== HISTÓRICO DE PREÇOS ====================
    
    /**
     * Busca histórico de preços de um produto.
     * 
     * Query params:
     * - limite (int, opcional): Limita quantidade de registros retornados
     */
    @GetMapping("/historico/{produtoId}")
    public ResponseEntity<?> buscarHistorico(
        @PathVariable Long produtoId,
        @RequestParam(required = false) Integer limite,
        HttpSession session
    ) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            List<HistoricoPreco> historico = alertaPrecoService.buscarHistorico(produtoId, limite);
            return ResponseEntity.ok(historico);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar histórico: " + e.getMessage());
        }
    }
    
    /**
     * Busca reduções de preço de um produto.
     */
    @GetMapping("/historico/{produtoId}/reducoes")
    public ResponseEntity<?> buscarReducoes(@PathVariable Long produtoId, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
        }
        
        try {
            List<HistoricoPreco> reducoes = alertaPrecoService.buscarReducoes(produtoId);
            return ResponseEntity.ok(reducoes);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar reduções: " + e.getMessage());
        }
    }
}
