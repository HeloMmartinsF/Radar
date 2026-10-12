package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.EventoProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventoProdutoRepository extends JpaRepository<EventoProduto, Long> {

    /**
     * Conta total de visualizações de produtos do fornecedor
     */
    Long countByFornecedorId(Long fornecedorId);

    /**
     * Conta visualizações em um período específico
     */
    @Query("SELECT COUNT(e) FROM EventoProduto e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.criadoEm BETWEEN :dataInicio AND :dataFim")
    Long countByFornecedorIdAndPeriodo(
        @Param("fornecedorId") Long fornecedorId,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Conta visualizações de um produto específico
     */
    Long countByProdutoId(Long produtoId);

    /**
     * Retorna produtos mais visualizados do fornecedor (com contagem)
     */
    @Query("SELECT e.produtoId as produtoId, COUNT(e) as total " +
           "FROM EventoProduto e WHERE e.fornecedorId = :fornecedorId " +
           "GROUP BY e.produtoId ORDER BY total DESC")
    List<Map<String, Object>> findProdutosMaisVisualizados(@Param("fornecedorId") Long fornecedorId);

    /**
     * Retorna evolução de visualizações por dia (últimos N dias)
     */
    @Query("SELECT FUNCTION('DATE', e.criadoEm) as data, COUNT(e) as total " +
           "FROM EventoProduto e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.criadoEm >= :dataInicio " +
           "GROUP BY FUNCTION('DATE', e.criadoEm) ORDER BY data")
    List<Map<String, Object>> findEvolucaoPorDia(
        @Param("fornecedorId") Long fornecedorId,
        @Param("dataInicio") LocalDateTime dataInicio
    );

    /**
     * Verifica se já existe visualização recente (deduplicação)
     * Retorna true se já existe evento do mesmo usuário/IP no mesmo produto nas últimas 24h
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EventoProduto e " +
           "WHERE e.produtoId = :produtoId AND e.criadoEm >= :limiteData " +
           "AND ((:usuarioId IS NOT NULL AND e.usuarioId = :usuarioId) " +
           "OR (:usuarioId IS NULL AND e.ipUsuario = :ipUsuario))")
    boolean existsVisualizacaoRecente(
        @Param("produtoId") Long produtoId,
        @Param("usuarioId") Long usuarioId,
        @Param("ipUsuario") String ipUsuario,
        @Param("limiteData") LocalDateTime limiteData
    );
}
