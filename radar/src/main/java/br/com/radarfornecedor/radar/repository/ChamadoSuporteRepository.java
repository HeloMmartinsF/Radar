package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.ChamadoSuporte;
import br.com.radarfornecedor.radar.model.CategoriaSuporte;
import br.com.radarfornecedor.radar.model.NivelUrgencia;
import br.com.radarfornecedor.radar.model.StatusChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Chamados de Suporte.
 * Fornece queries otimizadas para busca e filtros.
 */
@Repository
public interface ChamadoSuporteRepository extends JpaRepository<ChamadoSuporte, Long> {

    /**
     * Busca chamado por número único
     */
    Optional<ChamadoSuporte> findByNumeroChamado(String numeroChamado);

    /**
     * Lista todos os chamados de um usuário específico (ordenados por data)
     */
    List<ChamadoSuporte> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    /**
     * Lista chamados de um usuário filtrados por status
     */
    List<ChamadoSuporte> findByUsuarioIdAndStatusOrderByCriadoEmDesc(Long usuarioId, StatusChamado status);

    /**
     * Lista todos os chamados (para administradores) ordenados por urgência e data
     */
    @Query("SELECT c FROM ChamadoSuporte c ORDER BY c.urgencia ASC, c.criadoEm DESC")
    List<ChamadoSuporte> findAllOrderedByUrgencyAndDate();

    /**
     * Lista chamados filtrados por status (para administradores)
     */
    List<ChamadoSuporte> findByStatusOrderByCriadoEmDesc(StatusChamado status);

    /**
     * Lista chamados filtrados por categoria
     */
    List<ChamadoSuporte> findByCategoriaOrderByCriadoEmDesc(CategoriaSuporte categoria);

    /**
     * Lista chamados filtrados por urgência
     */
    List<ChamadoSuporte> findByUrgenciaOrderByCriadoEmDesc(NivelUrgencia urgencia);

    /**
     * Lista chamados com novos mensagens para admin
     */
    List<ChamadoSuporte> findByTemNovasMensagensAdminTrueOrderByCriadoEmDesc();

    /**
     * Lista chamados abertos (todos exceto RESOLVIDO e ENCERRADO)
     */
    @Query("SELECT c FROM ChamadoSuporte c WHERE c.status NOT IN (br.com.radarfornecedor.radar.model.StatusChamado.RESOLVIDO, br.com.radarfornecedor.radar.model.StatusChamado.ENCERRADO) ORDER BY c.urgencia ASC, c.criadoEm DESC")
    List<ChamadoSuporte> findAllOpenChamados();

    /**
     * Conta chamados abertos de um usuário
     */
    @Query("SELECT COUNT(c) FROM ChamadoSuporte c WHERE c.usuarioId = :usuarioId AND c.status NOT IN ('ENCERRADO', 'RESOLVIDO')")
    Long countChamadosAbertosUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Conta total de chamados abertos (para dashboard admin)
     */
    @Query("SELECT COUNT(c) FROM ChamadoSuporte c WHERE c.status NOT IN ('ENCERRADO', 'RESOLVIDO')")
    Long countAllChamadosAbertos();

    /**
     * Busca chamados criados em um período
     */
    List<ChamadoSuporte> findByCriadoEmBetweenOrderByCriadoEmDesc(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Busca chamados por múltiplos filtros (query complexa para admin)
     */
    @Query("SELECT c FROM ChamadoSuporte c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:categoria IS NULL OR c.categoria = :categoria) AND " +
           "(:urgencia IS NULL OR c.urgencia = :urgencia) AND " +
           "(:usuarioId IS NULL OR c.usuarioId = :usuarioId) " +
           "ORDER BY c.urgencia ASC, c.criadoEm DESC")
    List<ChamadoSuporte> findByMultipleFilters(
        @Param("status") StatusChamado status,
        @Param("categoria") CategoriaSuporte categoria,
        @Param("urgencia") NivelUrgencia urgencia,
        @Param("usuarioId") Long usuarioId
    );

    /**
     * Gera o próximo número de chamado sequencial
     */
    @Query("SELECT MAX(c.id) FROM ChamadoSuporte c")
    Long findMaxId();
}
