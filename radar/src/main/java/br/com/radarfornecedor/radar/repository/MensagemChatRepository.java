package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.MensagemChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para gerenciamento de Mensagens de Chat.
 * Fornece queries otimizadas para busca e atualização.
 */
@Repository
public interface MensagemChatRepository extends JpaRepository<MensagemChat, Long> {

    /**
     * Lista todas as mensagens de uma conversa
     * Ordenadas por data de envio (mais antigas primeiro, para exibição cronológica)
     */
    List<MensagemChat> findByConversaIdOrderByEnviadaEmAsc(Long conversaId);

    /**
     * Lista mensagens de uma conversa a partir de um ID específico
     * Usado para polling (buscar apenas mensagens novas)
     */
    @Query("SELECT m FROM MensagemChat m WHERE " +
           "m.conversaId = :conversaId " +
           "AND m.id > :ultimaMensagemId " +
           "ORDER BY m.enviadaEm ASC")
    List<MensagemChat> findMensagensNovas(
        @Param("conversaId") Long conversaId,
        @Param("ultimaMensagemId") Long ultimaMensagemId
    );

    /**
     * Conta mensagens não lidas de um destinatário em uma conversa
     */
    @Query("SELECT COUNT(m) FROM MensagemChat m WHERE " +
           "m.conversaId = :conversaId " +
           "AND m.remetenteId != :usuarioId " +
           "AND m.lida = false")
    Long countNaoLidasByConversaAndUsuario(
        @Param("conversaId") Long conversaId,
        @Param("usuarioId") Long usuarioId
    );

    /**
     * Marca todas as mensagens de uma conversa como lidas para um usuário específico
     * (marca apenas mensagens que o usuário NÃO enviou)
     */
    @Modifying
    @Query("UPDATE MensagemChat m SET m.lida = true WHERE " +
           "m.conversaId = :conversaId " +
           "AND m.remetenteId != :usuarioId " +
           "AND m.lida = false")
    int marcarTodasComoLidas(
        @Param("conversaId") Long conversaId,
        @Param("usuarioId") Long usuarioId
    );

    /**
     * Busca a última mensagem de uma conversa
     */
    @Query("SELECT m FROM MensagemChat m WHERE " +
           "m.conversaId = :conversaId " +
           "ORDER BY m.enviadaEm DESC")
    List<MensagemChat> findUltimaMensagem(@Param("conversaId") Long conversaId);

    /**
     * Conta total de mensagens em uma conversa
     */
    Long countByConversaId(Long conversaId);
}
