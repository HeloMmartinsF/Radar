package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Subcategoria;
import br.com.radarfornecedor.radar.repository.SubcategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/subcategorias")
@CrossOrigin(origins = "*")
public class SubcategoriaController {

    private final SubcategoriaRepository repo;

    public SubcategoriaController(SubcategoriaRepository repo) {
        this.repo = repo;
    }

    /** Lista todas as subcategorias ativas de uma categoria específica. */
    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<List<Subcategoria>> listarPorCategoria(
            @PathVariable String categoria) {
        List<Subcategoria> lista = repo.findByCategoriaAndAtivaTrue(categoria.toUpperCase());
        return ResponseEntity.ok(lista);
    }

    /** Lista todas as subcategorias ativas. */
    @GetMapping
    public ResponseEntity<List<Subcategoria>> listarTodas() {
        return ResponseEntity.ok(repo.findByAtivaTrue());
    }

    /** Estatísticas gerais de subcategorias. */
    @GetMapping("/estatisticas")
    public ResponseEntity<Map<String, Long>> estatisticas() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", repo.count());
        stats.put("ativas", repo.countAtivas());
        return ResponseEntity.ok(stats);
    }

    /** Busca subcategoria por ID. */
    @GetMapping("/{id}")
    public ResponseEntity<Subcategoria> buscarPorId(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Cria nova subcategoria (uso admin). */
    @PostMapping
    public ResponseEntity<Subcategoria> criar(@RequestBody Subcategoria subcategoria) {
        if (subcategoria.getCategoria() != null) {
            subcategoria.setCategoria(subcategoria.getCategoria().toUpperCase());
        }
        if (subcategoria.getAtiva() == null) {
            subcategoria.setAtiva(true);
        }
        Subcategoria salva = repo.save(subcategoria);
        return ResponseEntity.ok(salva);
    }

    /** Atualiza subcategoria existente (uso admin). */
    @PutMapping("/{id}")
    public ResponseEntity<Subcategoria> atualizar(
            @PathVariable Long id, @RequestBody Subcategoria dados) {
        return repo.findById(id).map(sub -> {
            if (dados.getNome() != null) sub.setNome(dados.getNome());
            if (dados.getDescricao() != null) sub.setDescricao(dados.getDescricao());
            if (dados.getAtiva() != null) sub.setAtiva(dados.getAtiva());
            return ResponseEntity.ok(repo.save(sub));
        }).orElse(ResponseEntity.notFound().build());
    }
}
