package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Notificacao;
import br.com.radarfornecedor.radar.model.TipoNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar notificações dos usuários
 */
public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {

    /**
     * Lista todas as notificações de um usuário, ordenadas por data (mais recentes primeiro)
     */
    List<Notificacao> findByUsuarioIdOrderByCriadaEmDesc(Long usuarioId);

    /**
     * Lista as N notificações mais recentes de um usuário
     */
    @Query("SELECT n FROM Notificacao n WHERE n.usuarioId = :usuarioId " +
           "ORDER BY n.criadaEm DESC")
    List<Notificacao> findTopNByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista apenas notificações não lidas de um usuário
     */
    List<Notificacao> findByUsuarioIdAndLidaFalseOrderByCriadaEmDesc(Long usuarioId);

    /**
     * Conta total de notificações não lidas de um usuário
     */
    Long countByUsuarioIdAndLidaFalse(Long usuarioId);

    /**
     * Lista notificações de um tipo específico para um usuário
     */
    List<Notificacao> findByUsuarioIdAndTipoOrderByCriadaEmDesc(Long usuarioId, TipoNotificacao tipo);

    /**
     * Busca uma notificação específica e valida que pertence ao usuário (para autorização)
     */
    Optional<Notificacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Verifica se já existe notificação para o mesmo evento (deduplicação)
     * Evita criar múltiplas notificações para o mesmo evento
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notificacao n " +
           "WHERE n.usuarioId = :usuarioId AND n.eventoTipo = :eventoTipo " +
           "AND n.eventoId = :eventoId")
    boolean existsByUsuarioIdAndEventoTipoAndEventoId(
        @Param("usuarioId") Long usuarioId,
        @Param("eventoTipo") String eventoTipo,
        @Param("eventoId") Long eventoId
    );

    /**
     * Marca uma notificação como lida (atualização individual)
     */
    @Modifying
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.id = :id AND n.usuarioId = :usuarioId")
    int marcarComoLida(@Param("id") Long id, @Param("usuarioId") Long usuarioId);

    /**
     * Marca todas as notificações de um usuário como lidas
     */
    @Modifying
    @Query("UPDATE Notificacao n SET n.lida = true WHERE n.usuarioId = :usuarioId AND n.lida = false")
    int marcarTodasComoLidas(@Param("usuarioId") Long usuarioId);

    /**
     * Deleta notificações antigas (opcional - para limpeza periódica)
     * Exemplo: deletar notificações lidas com mais de 90 dias
     */
    @Modifying
    @Query("DELETE FROM Notificacao n WHERE n.usuarioId = :usuarioId AND n.lida = true " +
           "AND n.criadaEm < CURRENT_TIMESTAMP - 90")
    int deletarNotificacoesAntigas(@Param("usuarioId") Long usuarioId);
}
