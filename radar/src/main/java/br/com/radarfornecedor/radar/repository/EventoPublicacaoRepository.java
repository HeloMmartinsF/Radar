package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.EventoPublicacao;
import br.com.radarfornecedor.radar.model.TipoEventoPublicacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventoPublicacaoRepository extends JpaRepository<EventoPublicacao, Long> {

    /**
     * Conta total de eventos de um tipo para um fornecedor
     */
    Long countByFornecedorIdAndTipoEvento(Long fornecedorId, TipoEventoPublicacao tipoEvento);

    /**
     * Conta eventos de um tipo em um período específico
     */
    @Query("SELECT COUNT(e) FROM EventoPublicacao e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.tipoEvento = :tipoEvento AND e.criadoEm BETWEEN :dataInicio AND :dataFim")
    Long countByFornecedorIdAndTipoEventoAndPeriodo(
        @Param("fornecedorId") Long fornecedorId,
        @Param("tipoEvento") TipoEventoPublicacao tipoEvento,
        @Param("dataInicio") LocalDateTime dataInicio,
        @Param("dataFim") LocalDateTime dataFim
    );

    /**
     * Conta visualizações de uma publicação específica
     */
    Long countByPublicacaoIdAndTipoEvento(Long publicacaoId, TipoEventoPublicacao tipoEvento);

    /**
     * Retorna publicações mais visualizadas do fornecedor (com contagem)
     */
    @Query("SELECT e.publicacaoId as publicacaoId, COUNT(e) as total " +
           "FROM EventoPublicacao e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.tipoEvento = 'VIEW' GROUP BY e.publicacaoId ORDER BY total DESC")
    List<Map<String, Object>> findPublicacoesMaisVisualizadas(@Param("fornecedorId") Long fornecedorId);

    /**
     * Retorna evolução de eventos por dia (últimos N dias)
     * Usa CAST para converter timestamp para date (compatível com H2)
     */
    @Query("SELECT CAST(e.criadoEm AS date) as data, COUNT(e) as total " +
           "FROM EventoPublicacao e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.tipoEvento = :tipoEvento AND e.criadoEm >= :dataInicio " +
           "GROUP BY CAST(e.criadoEm AS date) ORDER BY data")
    List<Map<String, Object>> findEvolucaoPorDia(
        @Param("fornecedorId") Long fornecedorId,
        @Param("tipoEvento") TipoEventoPublicacao tipoEvento,
        @Param("dataInicio") LocalDateTime dataInicio
    );

    /**
     * Verifica se já existe evento similar recente (deduplicação)
     * Retorna true se já existe evento do mesmo usuário/IP na mesma publicação nas últimas 24h
     */
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM EventoPublicacao e " +
           "WHERE e.publicacaoId = :publicacaoId AND e.tipoEvento = :tipoEvento " +
           "AND e.criadoEm >= :limiteData " +
           "AND ((:usuarioId IS NOT NULL AND e.usuarioId = :usuarioId) " +
           "OR (:usuarioId IS NULL AND e.ipUsuario = :ipUsuario))")
    boolean existsEventoRecente(
        @Param("publicacaoId") Long publicacaoId,
        @Param("tipoEvento") TipoEventoPublicacao tipoEvento,
        @Param("usuarioId") Long usuarioId,
        @Param("ipUsuario") String ipUsuario,
        @Param("limiteData") LocalDateTime limiteData
    );

    /**
     * Conta total de visualizações de todas as publicações do fornecedor
     */
    @Query("SELECT COUNT(e) FROM EventoPublicacao e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.tipoEvento = 'VIEW'")
    Long countTotalVisualizacoes(@Param("fornecedorId") Long fornecedorId);

    /**
     * Conta total de cliques em contato
     */
    @Query("SELECT COUNT(e) FROM EventoPublicacao e WHERE e.fornecedorId = :fornecedorId " +
           "AND e.tipoEvento = 'CONTATO'")
    Long countTotalContatos(@Param("fornecedorId") Long fornecedorId);
}
