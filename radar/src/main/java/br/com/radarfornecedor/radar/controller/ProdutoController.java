package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.dto.CategoriaProdutoDTO;
import br.com.radarfornecedor.radar.model.CategoriaProduto;
import br.com.radarfornecedor.radar.model.Produto;
import br.com.radarfornecedor.radar.service.ProdutoService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {
    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<Produto> listar(@RequestParam(required = false, name = "categoria") String categoriaStr) {
        CategoriaProduto categoria = categoriaStr == null || categoriaStr.isEmpty() ? null : CategoriaProduto.valueOf(categoriaStr);
        return produtoService.listar(categoria);
    }

    @GetMapping("/categorias")
    public List<CategoriaProdutoDTO> categorias() {
        return Arrays.stream(CategoriaProduto.values())
                .map(categoria -> new CategoriaProdutoDTO(
                        categoria.getCodigo(),
                        categoria.getDescricao(),
                        categoria.getIcone()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<Produto> cadastrar(@Valid @RequestBody Produto produto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(produtoService.salvar(produto));
    }
    
    @GetMapping("/buscar")
    public ResponseEntity<List<Produto>> buscar(@RequestParam("q") String query) {
        List<Produto> produtos = produtoService.buscarPorNome(query);
        return ResponseEntity.ok(produtos);
    }
}
