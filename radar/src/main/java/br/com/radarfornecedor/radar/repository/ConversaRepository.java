package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Conversa;
import br.com.radarfornecedor.radar.model.ContextoConversa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciamento de Conversas comerciais.
 * Fornece queries otimizadas para busca e listagem.
 */
@Repository
public interface ConversaRepository extends JpaRepository<Conversa, Long> {

    /**
     * Lista todas as conversas de um usuário (como participante 1 ou 2)
     * Ordenadas por última mensagem (mais recentes primeiro)
     */
    @Query("SELECT c FROM Conversa c WHERE " +
           "(c.participante1Id = :usuarioId OR c.participante2Id = :usuarioId) " +
           "AND c.ativa = true " +
           "ORDER BY c.ultimaMensagemEm DESC")
    List<Conversa> findConversasByUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Lista conversas ativas de um usuário com mensagens não lidas
     */
    @Query("SELECT c FROM Conversa c WHERE " +
           "((c.participante1Id = :usuarioId AND c.participante1NaoLidas > 0) OR " +
           " (c.participante2Id = :usuarioId AND c.participante2NaoLidas > 0)) " +
           "AND c.ativa = true " +
           "ORDER BY c.ultimaMensagemEm DESC")
    List<Conversa> findConversasComNaoLidas(@Param("usuarioId") Long usuarioId);

    /**
     * Busca conversa existente entre dois participantes sobre um contexto específico
     * Usado para evitar duplicação de conversas
     */
    @Query("SELECT c FROM Conversa c WHERE " +
           "((c.participante1Id = :usuario1 AND c.participante2Id = :usuario2) OR " +
           " (c.participante1Id = :usuario2 AND c.participante2Id = :usuario1)) " +
           "AND c.fornecedorId = :fornecedorId " +
           "AND c.contexto = :contexto " +
           "AND (:contextoRef IS NULL OR c.contextoReferencia = :contextoRef) " +
           "AND c.ativa = true")
    Optional<Conversa> findConversaExistente(
        @Param("usuario1") Long usuario1Id,
        @Param("usuario2") Long usuario2Id,
        @Param("fornecedorId") Long fornecedorId,
        @Param("contexto") ContextoConversa contexto,
        @Param("contextoRef") Long contextoReferencia
    );

    /**
     * Lista conversas de um fornecedor específico
     */
    @Query("SELECT c FROM Conversa c WHERE " +
           "c.fornecedorId = :fornecedorId " +
           "AND c.ativa = true " +
           "ORDER BY c.ultimaMensagemEm DESC")
    List<Conversa> findConversasByFornecedor(@Param("fornecedorId") Long fornecedorId);

    /**
     * Conta total de mensagens não lidas de um usuário (soma de todas as conversas)
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN c.participante1Id = :usuarioId THEN c.participante1NaoLidas " +
           "                           WHEN c.participante2Id = :usuarioId THEN c.participante2NaoLidas " +
           "                           ELSE 0 END), 0) " +
           "FROM Conversa c WHERE " +
           "(c.participante1Id = :usuarioId OR c.participante2Id = :usuarioId) " +
           "AND c.ativa = true")
    Long countTotalNaoLidasUsuario(@Param("usuarioId") Long usuarioId);

    /**
     * Busca uma conversa específica e valida se o usuário é participante
     */
    @Query("SELECT c FROM Conversa c WHERE " +
           "c.id = :conversaId " +
           "AND (c.participante1Id = :usuarioId OR c.participante2Id = :usuarioId)")
    Optional<Conversa> findByIdAndUsuario(
        @Param("conversaId") Long conversaId,
        @Param("usuarioId") Long usuarioId
    );

    /**
     * Lista conversas sobre um produto específico
     */
    @Query("SELECT c FROM Conversa c WHERE " +
           "c.contexto = 'PRODUTO' " +
           "AND c.contextoReferencia = :produtoId " +
           "AND c.ativa = true " +
           "ORDER BY c.ultimaMensagemEm DESC")
    List<Conversa> findConversasByProduto(@Param("produtoId") Long produtoId);
}
