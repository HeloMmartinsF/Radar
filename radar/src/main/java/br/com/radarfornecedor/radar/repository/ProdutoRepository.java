package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.CategoriaProduto;
import br.com.radarfornecedor.radar.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    List<Produto> findByCategoria(CategoriaProduto categoria);
    
    // Métodos para Perfil Social
    List<Produto> findByFornecedorId(Long fornecedorId);
    long countByFornecedorId(Long fornecedorId);
    
    // Busca por nome (usado em Lista de Compras)
    List<Produto> findByNomeContainingIgnoreCase(String nome);
}
