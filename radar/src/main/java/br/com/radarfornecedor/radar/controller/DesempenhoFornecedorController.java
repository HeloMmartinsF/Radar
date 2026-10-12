package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.dto.IndicadoresDesempenhoDTO;
import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.service.DesempenhoFornecedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * Controller para painel de desempenho exclusivo de fornecedores
 * 
 * AUTORIZAÇÃO:
 * - Apenas fornecedores autenticados podem acessar
 * - Cada fornecedor só pode ver seus próprios indicadores
 * - Validação no backend (não confia em parâmetros do frontend)
 */
@RestController
@RequestMapping("/api/desempenho")
public class DesempenhoFornecedorController {

    private final DesempenhoFornecedorService desempenhoService;
    private final FornecedorRepository fornecedorRepository;

    public DesempenhoFornecedorController(
            DesempenhoFornecedorService desempenhoService,
            FornecedorRepository fornecedorRepository) {
        this.desempenhoService = desempenhoService;
        this.fornecedorRepository = fornecedorRepository;
    }

    /**
     * GET /api/desempenho/indicadores
     * Retorna todos os indicadores de desempenho do fornecedor autenticado
     * 
     * Query Params:
     * - diasHistorico: número de dias para evolução temporal (padrão: 30)
     * 
     * SEGURANÇA:
     * - Valida autenticação na sessão
     * - Valida que usuário é fornecedor
     * - Retorna apenas dados do próprio fornecedor
     */
    @GetMapping("/indicadores")
    public ResponseEntity<?> obterIndicadores(
            @RequestParam(required = false, defaultValue = "30") Integer diasHistorico,
            HttpSession session) {
        try {
            // Validação 1: Usuário autenticado?
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Validação 2: Usuário é fornecedor?
            if (!Boolean.TRUE.equals(usuario.getFornecedor())) {
                return ResponseEntity.status(403)
                        .body(Map.of("erro", "Acesso exclusivo para fornecedores"));
            }

            // Validação 3: Buscar fornecedor pelo CNPJ do usuário
            Fornecedor fornecedor = fornecedorRepository.findByCnpj(usuario.getCnpjOuCpf())
                    .orElse(null);

            if (fornecedor == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("erro", "Fornecedor não encontrado no sistema"));
            }

            // Calcular indicadores usando ID do fornecedor vinculado ao usuário
            IndicadoresDesempenhoDTO indicadores = desempenhoService.calcularIndicadores(
                fornecedor.getId(), diasHistorico
            );

            return ResponseEntity.ok(indicadores);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao calcular indicadores: " + e.getMessage()));
        }
    }

    /**
     * POST /api/desempenho/visualizacao/publicacao/{publicacaoId}
     * Registra visualização de uma publicação
     * 
     * Este endpoint é chamado automaticamente quando uma publicação é exibida
     * Deduplicação: máximo 1 visualização por usuário/IP a cada 24h
     */
    @PostMapping("/visualizacao/publicacao/{publicacaoId}")
    public ResponseEntity<?> registrarVisualizacaoPublicacao(
            @PathVariable Long publicacaoId,
            HttpSession session,
            HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Long usuarioId = usuario != null ? usuario.getId() : null;
            String ipUsuario = obterIpCliente(request);

            boolean registrado = desempenhoService.registrarVisualizacaoPublicacao(
                publicacaoId, usuarioId, ipUsuario
            );

            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "registrado", registrado,
                "mensagem", registrado ? "Visualização registrada" : "Já visualizado recentemente"
            ));

        } catch (Exception e) {
            // Não falhar silenciosamente, mas também não expor erro ao usuário
            return ResponseEntity.ok(Map.of("sucesso", false));
        }
    }

    /**
     * POST /api/desempenho/visualizacao/produto/{produtoId}
     * Registra visualização de um produto
     * 
     * Deduplicação: máximo 1 visualização por usuário/IP a cada 24h
     */
    @PostMapping("/visualizacao/produto/{produtoId}")
    public ResponseEntity<?> registrarVisualizacaoProduto(
            @PathVariable Long produtoId,
            HttpSession session,
            HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Long usuarioId = usuario != null ? usuario.getId() : null;
            String ipUsuario = obterIpCliente(request);

            boolean registrado = desempenhoService.registrarVisualizacaoProduto(
                produtoId, usuarioId, ipUsuario
            );

            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "registrado", registrado
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("sucesso", false));
        }
    }

    /**
     * POST /api/desempenho/contato/{publicacaoId}
     * Registra clique no botão "Entrar em contato"
     * 
     * Cada clique é registrado (sem deduplicação, pois cada contato é intencional)
     */
    @PostMapping("/contato/{publicacaoId}")
    public ResponseEntity<?> registrarCliqueContato(
            @PathVariable Long publicacaoId,
            HttpSession session,
            HttpServletRequest request) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            Long usuarioId = usuario != null ? usuario.getId() : null;
            String ipUsuario = obterIpCliente(request);

            boolean registrado = desempenhoService.registrarCliqueContato(
                publicacaoId, usuarioId, ipUsuario
            );

            return ResponseEntity.ok(Map.of(
                "sucesso", true,
                "registrado", registrado
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("sucesso", false));
        }
    }

    /**
     * Extrai IP real do cliente, considerando proxies e load balancers
     */
    private String obterIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Se X-Forwarded-For contém múltiplos IPs, pegar o primeiro
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
