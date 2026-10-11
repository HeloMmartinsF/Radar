package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.CategoriaProduto;
import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.model.Produto;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.service.BuscaProdutoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller para busca inteligente de produtos
 * Endpoint: /api/busca
 */
@RestController
@RequestMapping("/api/busca")
public class BuscaProdutoController {

    private final BuscaProdutoService buscaService;
    private final FornecedorRepository fornecedorRepository;

    public BuscaProdutoController(BuscaProdutoService buscaService,
                                  FornecedorRepository fornecedorRepository) {
        this.buscaService = buscaService;
        this.fornecedorRepository = fornecedorRepository;
    }

    /**
     * GET /api/busca/produtos
     * Busca inteligente com filtros e paginação
     *
     * Parâmetros:
     * - termo: texto a buscar (nome, descrição)
     * - categoria: ALIMENTOS, AUTOMOTIVO, etc
     * - precoMin: preço mínimo
     * - precoMax: preço máximo
     * - fornecedorId: ID do fornecedor
     * - ordenacao: relevancia, menor_preco, maior_preco, nome_az, nome_za
     * - pagina: número da página (default: 0)
     * - tamanho: itens por página (default: 20)
     */
    @GetMapping("/produtos")
    public ResponseEntity<?> buscarProdutos(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) BigDecimal precoMin,
            @RequestParam(required = false) BigDecimal precoMax,
            @RequestParam(required = false) Long fornecedorId,
            @RequestParam(defaultValue = "relevancia") String ordenacao,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamanho) {

        try {
            // Validar parâmetros
            if (tamanho > 100) tamanho = 100; // Máximo 100 itens por página
            if (pagina < 0) pagina = 0;

            // Converter categoria string para enum
            CategoriaProduto categoriaEnum = null;
            if (categoria != null && !categoria.trim().isEmpty()) {
                try {
                    categoriaEnum = CategoriaProduto.valueOf(categoria.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", "Categoria inválida: " + categoria));
                }
            }

            // Criar paginação
            Pageable pageable = PageRequest.of(pagina, tamanho);

            // Executar busca
            Page<Produto> resultados = buscaService.buscarProdutos(
                    termo,
                    categoriaEnum,
                    precoMin,
                    precoMax,
                    fornecedorId,
                    ordenacao,
                    pageable
            );

            // Enriquecer resultados com dados do fornecedor
            List<Map<String, Object>> produtosDTO = resultados.getContent().stream()
                    .map(this::montarProdutoDTO)
                    .collect(Collectors.toList());

            // Montar resposta com metadados de paginação
            Map<String, Object> resposta = new HashMap<>();
            resposta.put("produtos", produtosDTO);
            resposta.put("paginaAtual", resultados.getNumber());
            resposta.put("totalPaginas", resultados.getTotalPages());
            resposta.put("totalItens", resultados.getTotalElements());
            resposta.put("itensPorPagina", resultados.getSize());
            resposta.put("temProxima", resultados.hasNext());
            resposta.put("temAnterior", resultados.hasPrevious());

            return ResponseEntity.ok(resposta);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao buscar produtos: " + e.getMessage()));
        }
    }

    /**
     * GET /api/busca/sugestoes
     * Busca rápida para autocomplete (primeiros 10 resultados)
     */
    @GetMapping("/sugestoes")
    public ResponseEntity<?> buscarSugestoes(@RequestParam String termo) {
        try {
            if (termo == null || termo.trim().length() < 2) {
                return ResponseEntity.ok(List.of());
            }

            Pageable pageable = PageRequest.of(0, 10);
            Page<Produto> resultados = buscaService.buscarProdutos(
                    termo, null, null, null, null, "relevancia", pageable
            );

            List<Map<String, Object>> sugestoes = resultados.getContent().stream()
                    .map(p -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", p.getId());
                        map.put("nome", p.getNome());
                        map.put("categoria", p.getCategoria().name());
                        return map;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(sugestoes);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao buscar sugestões"));
        }
    }

    /**
     * GET /api/busca/categorias
     * Lista todas as categorias disponíveis
     */
    @GetMapping("/categorias")
    public ResponseEntity<?> listarCategorias() {
        try {
            CategoriaProduto[] categorias = CategoriaProduto.values();
            List<Map<String, String>> categoriasDTO = java.util.Arrays.stream(categorias)
                    .map(c -> Map.of(
                            "valor", c.name(),
                            "nome", formatarNomeCategoria(c.name())
                    ))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(categoriasDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar categorias"));
        }
    }

    /**
     * Monta DTO do produto com dados enriquecidos
     */
    private Map<String, Object> montarProdutoDTO(Produto produto) {
        Map<String, Object> dto = new HashMap<>();

        dto.put("id", produto.getId());
        dto.put("nome", produto.getNome());
        dto.put("descricao", produto.getDescricao());
        dto.put("categoria", produto.getCategoria().name());
        dto.put("categoriaNome", formatarNomeCategoria(produto.getCategoria().name()));
        dto.put("preco", produto.getPreco());

        // Buscar dados do fornecedor
        if (produto.getFornecedorId() != null) {
            Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findById(produto.getFornecedorId());
            if (fornecedorOpt.isPresent()) {
                Fornecedor fornecedor = fornecedorOpt.get();
                Map<String, Object> fornecedorDTO = new HashMap<>();
                fornecedorDTO.put("id", fornecedor.getId());
                fornecedorDTO.put("nome", fornecedor.getNome());
                fornecedorDTO.put("cidade", fornecedor.getCidade());
                fornecedorDTO.put("estado", fornecedor.getEstado());
                dto.put("fornecedor", fornecedorDTO);
            }
        }

        return dto;
    }

    /**
     * Formatar nome da categoria para exibição
     */
    private String formatarNomeCategoria(String categoria) {
        String[] palavras = categoria.replace("_", " ").toLowerCase().split(" ");
        StringBuilder resultado = new StringBuilder();
        for (String palavra : palavras) {
            if (palavra.length() > 0) {
                resultado.append(Character.toUpperCase(palavra.charAt(0)))
                        .append(palavra.substring(1))
                        .append(" ");
            }
        }
        return resultado.toString().trim();
    }
}
