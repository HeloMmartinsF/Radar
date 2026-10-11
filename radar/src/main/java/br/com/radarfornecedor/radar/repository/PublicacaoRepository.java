package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Publicacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PublicacaoRepository extends JpaRepository<Publicacao, Long> {
    
    /**
     * Busca publicações ativas ordenadas por data mais recente
     */
    List<Publicacao> findByAtivaOrderByCriadaEmDesc(Boolean ativa);
    
    /**
     * Busca publicações de um fornecedor específico
     */
    List<Publicacao> findByFornecedorIdOrderByCriadaEmDesc(Long fornecedorId);
    
    /**
     * Busca publicações ativas de um fornecedor específico
     */
    List<Publicacao> findByFornecedorIdAndAtivaOrderByCriadaEmDesc(Long fornecedorId, Boolean ativa);
    
    /**
     * Busca publicações ativas de um fornecedor específico com paginação
     */
    List<Publicacao> findByFornecedorIdAndAtivaOrderByCriadaEmDesc(Long fornecedorId, Boolean ativa, Pageable pageable);
    
    /**
     * Busca publicações ativas de um fornecedor (para estatísticas)
     */
    List<Publicacao> findByFornecedorIdAndAtiva(Long fornecedorId, Boolean ativa);
    
    /**
     * Busca publicações com paginação (para scroll infinito)
     * Retorna publicações mais recentes que um determinado ID
     */
    @Query("SELECT p FROM Publicacao p WHERE p.ativa = true AND p.id < :lastId ORDER BY p.id DESC")
    List<Publicacao> findNextPublicacoes(@Param("lastId") Long lastId);
    
    /**
     * Conta publicações ativas de um fornecedor
     */
    Long countByFornecedorIdAndAtiva(Long fornecedorId, Boolean ativa);
}
