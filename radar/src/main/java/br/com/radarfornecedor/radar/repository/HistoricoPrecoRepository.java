package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.HistoricoPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoPrecoRepository extends JpaRepository<HistoricoPreco, Long> {
    
    /**
     * Busca histórico de um produto, ordenado por data (mais recente primeiro).
     */
    List<HistoricoPreco> findByProdutoIdOrderByRegistradoEmDesc(Long produtoId);
    
    /**
     * Busca histórico de um produto dentro de um período.
     */
    List<HistoricoPreco> findByProdutoIdAndRegistradoEmBetweenOrderByRegistradoEmAsc(
        Long produtoId, 
        LocalDateTime inicio, 
        LocalDateTime fim
    );
    
    /**
     * Busca o registro de preço mais recente de um produto.
     */
    Optional<HistoricoPreco> findFirstByProdutoIdOrderByRegistradoEmDesc(Long produtoId);
    
    /**
     * Busca o registro de preço anterior ao mais recente (para comparar variação).
     */
    @Query("SELECT h FROM HistoricoPreco h WHERE h.produtoId = :produtoId " +
           "AND h.registradoEm < (SELECT MAX(h2.registradoEm) FROM HistoricoPreco h2 WHERE h2.produtoId = :produtoId) " +
           "ORDER BY h.registradoEm DESC")
    Optional<HistoricoPreco> findPenultimoRegistro(@Param("produtoId") Long produtoId);
    
    /**
     * Busca histórico limitado aos últimos N registros.
     */
    List<HistoricoPreco> findTop10ByProdutoIdOrderByRegistradoEmDesc(Long produtoId);
    
    /**
     * Busca registros que indicam redução de preço (variação negativa).
     */
    @Query("SELECT h FROM HistoricoPreco h WHERE h.produtoId = :produtoId " +
           "AND h.variacaoPercentual < 0 ORDER BY h.registradoEm DESC")
    List<HistoricoPreco> findReducoesPrecoPorProduto(@Param("produtoId") Long produtoId);
    
    /**
     * Conta quantos registros de preço existem para um produto.
     */
    long countByProdutoId(Long produtoId);
    
    /**
     * Verifica se já existe registro de preço para um produto hoje.
     * Evita duplicação de registros no mesmo dia.
     */
    @Query("SELECT COUNT(h) > 0 FROM HistoricoPreco h WHERE h.produtoId = :produtoId " +
           "AND DATE(h.registradoEm) = CURRENT_DATE")
    boolean existeRegistroHoje(@Param("produtoId") Long produtoId);
    
    /**
     * Busca produtos que tiveram redução de preço recentemente (últimos N dias).
     */
    @Query("SELECT DISTINCT h.produtoId FROM HistoricoPreco h " +
           "WHERE h.registradoEm >= :dataInicio AND h.variacaoPercentual < 0")
    List<Long> findProdutosComReducaoRecente(@Param("dataInicio") LocalDateTime dataInicio);
}
