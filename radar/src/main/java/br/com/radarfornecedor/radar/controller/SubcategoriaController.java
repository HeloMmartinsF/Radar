package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Subcategoria;
import br.com.radarfornecedor.radar.model.CategoriaProduto;
import br.com.radarfornecedor.radar.repository.SubcategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subcategorias")
public class SubcategoriaController {

    private final SubcategoriaRepository subcategoriaRepository;

    public SubcategoriaController(SubcategoriaRepository subcategoriaRepository) {
        this.subcategoriaRepository = subcategoriaRepository;
    }

    /**
     * GET /api/subcategorias
     * Lista todas as subcategorias
     */
    @GetMapping
    public ResponseEntity<List<Subcategoria>> listarTodas() {
        return ResponseEntity.ok(subcategoriaRepository.findAll());
    }

    /**
     * GET /api/subcategorias/ativas
     * Lista apenas subcategorias ativas
     */
    @GetMapping("/ativas")
    public ResponseEntity<List<Subcategoria>> listarAtivas() {
        return ResponseEntity.ok(subcategoriaRepository.findByAtivaTrue());
    }

    /**
     * GET /api/subcategorias/categoria/{categoria}
     * Lista subcategorias de uma categoria específica
     */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Subcategoria>> listarPorCategoria(@PathVariable String categoria) {
        try {
            CategoriaProduto cat = CategoriaProduto.valueOf(categoria.toUpperCase());
            return ResponseEntity.ok(subcategoriaRepository.findByCategoria(cat));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /api/subcategorias/estatisticas
     * Retorna estatísticas de subcategorias
     */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Object>> obterEstatisticas() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", subcategoriaRepository.count());
        stats.put("ativas", subcategoriaRepository.countByAtivaTrue());
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/subcategorias/{id}
     * Busca subcategoria por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Subcategoria> buscarPorId(@PathVariable Long id) {
        return subcategoriaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/subcategorias/lote
     * Cria várias subcategorias de uma vez
     */
    @PostMapping("/lote")
    public ResponseEntity<?> criarEmLote(@RequestBody List<Map<String, Object>> listaSubcategorias) {
        try {
            int criadas = 0;
            int erros = 0;
            List<String> mensagensErro = new java.util.ArrayList<>();

            for (Map<String, Object> dados : listaSubcategorias) {
                try {
                    String nome = (String) dados.get("nome");
                    String descricao = (String) dados.get("descricao");
                    String categoriaStr = (String) dados.get("categoria");
                    Boolean ativa = dados.get("ativa") != null ? (Boolean) dados.get("ativa") : true;

                    if (nome == null || categoriaStr == null) {
                        erros++;
                        mensagensErro.add("Subcategoria sem nome ou categoria");
                        continue;
                    }

                    CategoriaProduto categoria = CategoriaProduto.valueOf(categoriaStr.toUpperCase());
                    Subcategoria subcategoria = new Subcategoria(nome, descricao, categoria);
                    subcategoria.setAtiva(ativa);
                    subcategoriaRepository.save(subcategoria);
                    criadas++;

                } catch (Exception e) {
                    erros++;
                    mensagensErro.add(dados.get("nome") + ": " + e.getMessage());
                }
            }

            Map<String, Object> resultado = new HashMap<>();
            resultado.put("criadas", criadas);
            resultado.put("erros", erros);
            resultado.put("total", listaSubcategorias.size());
            if (!mensagensErro.isEmpty()) {
                resultado.put("mensagensErro", mensagensErro);
            }

            return ResponseEntity.ok(resultado);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar subcategorias em lote: " + e.getMessage());
        }
    }

    /**
     * POST /api/subcategorias
     * Cria nova subcategoria
     */
    @PostMapping
    public ResponseEntity<?> criar(@Valid @RequestBody Map<String, Object> dados) {
        try {
            String nome = (String) dados.get("nome");
            String descricao = (String) dados.get("descricao");
            String categoriaStr = (String) dados.get("categoria");
            Boolean ativa = dados.get("ativa") != null ? (Boolean) dados.get("ativa") : true;

            if (nome == null || nome.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Nome é obrigatório");
            }

            if (categoriaStr == null || categoriaStr.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Categoria é obrigatória");
            }

            CategoriaProduto categoria = CategoriaProduto.valueOf(categoriaStr.toUpperCase());

            Subcategoria subcategoria = new Subcategoria(nome, descricao, categoria);
            subcategoria.setAtiva(ativa);

            Subcategoria salva = subcategoriaRepository.save(subcategoria);
            return ResponseEntity.status(HttpStatus.CREATED).body(salva);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Categoria inválida: " + dados.get("categoria"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar subcategoria: " + e.getMessage());
        }
    }

    /**
     * PUT /api/subcategorias/{id}
     * Atualiza subcategoria existente
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody Map<String, Object> dados) {
        return subcategoriaRepository.findById(id)
                .map(subcategoria -> {
                    if (dados.containsKey("nome")) {
                        subcategoria.setNome((String) dados.get("nome"));
                    }
                    if (dados.containsKey("descricao")) {
                        subcategoria.setDescricao((String) dados.get("descricao"));
                    }
                    if (dados.containsKey("ativa")) {
                        subcategoria.setAtiva((Boolean) dados.get("ativa"));
                    }
                    if (dados.containsKey("categoria")) {
                        try {
                            CategoriaProduto categoria = CategoriaProduto.valueOf(
                                    ((String) dados.get("categoria")).toUpperCase()
                            );
                            subcategoria.setCategoria(categoria);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.badRequest().body("Categoria inválida");
                        }
                    }

                    Subcategoria atualizada = subcategoriaRepository.save(subcategoria);
                    return ResponseEntity.ok(atualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/subcategorias/{id}
     * Remove subcategoria (soft delete - apenas marca como inativa)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletar(@PathVariable Long id) {
        return subcategoriaRepository.findById(id)
                .map(subcategoria -> {
                    subcategoria.setAtiva(false);
                    subcategoriaRepository.save(subcategoria);
                    return ResponseEntity.ok().body("Subcategoria desativada com sucesso");
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/subcategorias/{id}/permanente
     * Remove subcategoria permanentemente do banco
     */
    @DeleteMapping("/{id}/permanente")
    public ResponseEntity<?> deletarPermanente(@PathVariable Long id) {
        if (!subcategoriaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        subcategoriaRepository.deleteById(id);
        return ResponseEntity.ok().body("Subcategoria removida permanentemente");
    }
}
