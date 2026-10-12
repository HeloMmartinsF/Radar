package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.MensagemChamado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciamento de Mensagens de Chamados de Suporte.
 */
@Repository
public interface MensagemChamadoRepository extends JpaRepository<MensagemChamado, Long> {

    /**
     * Lista todas as mensagens de um chamado (ordenadas por data)
     */
    List<MensagemChamado> findByChamadoIdOrderByCriadaEmAsc(Long chamadoId);

    /**
     * Conta total de mensagens de um chamado
     */
    Long countByChamadoId(Long chamadoId);

    /**
     * Conta mensagens não lidas de um chamado por tipo de autor
     * (ex: mensagens de ADMIN não lidas pelo usuário)
     */
    @Query("SELECT COUNT(m) FROM MensagemChamado m WHERE m.chamadoId = :chamadoId AND m.autorTipo = :autorTipo AND m.lida = false")
    Long countMensagensNaoLidasPorTipo(@Param("chamadoId") Long chamadoId, @Param("autorTipo") String autorTipo);

    /**
     * Lista mensagens não lidas de um chamado
     */
    List<MensagemChamado> findByChamadoIdAndLidaFalseOrderByCriadaEmAsc(Long chamadoId);

    /**
     * Deleta todas as mensagens de um chamado (usado ao deletar chamado)
     */
    void deleteByChamadoId(Long chamadoId);
}
