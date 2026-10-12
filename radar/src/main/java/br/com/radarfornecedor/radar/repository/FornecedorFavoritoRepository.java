package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.FornecedorFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar fornecedores favoritos/seguidos pelos usuários.
 * Queries otimizadas com índices para performance.
 */
@Repository
public interface FornecedorFavoritoRepository extends JpaRepository<FornecedorFavorito, Long> {

    /**
     * Lista todos os fornecedores favoritos de um usuário
     * Ordenados por data de adição (mais recentes primeiro)
     *
     * @param usuarioId ID do usuário
     * @return Lista de fornecedores favoritos
     */
    List<FornecedorFavorito> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    /**
     * Verifica se um fornecedor está favoritado/seguido por um usuário
     * Útil para exibir estado do botão de favorito/seguir
     *
     * @param usuarioId ID do usuário
     * @param fornecedorId ID do fornecedor
     * @return true se está favoritado, false caso contrário
     */
    boolean existsByUsuarioIdAndFornecedorId(Long usuarioId, Long fornecedorId);

    /**
     * Busca um favorito específico de um usuário
     * Usado para validar permissões antes de remover
     *
     * @param usuarioId ID do usuário
     * @param fornecedorId ID do fornecedor
     * @return Optional com o favorito, se existir
     */
    Optional<FornecedorFavorito> findByUsuarioIdAndFornecedorId(Long usuarioId, Long fornecedorId);

    /**
     * Remove um favorito específico de um usuário (deixa de seguir)
     * Retorna quantidade de registros deletados (0 ou 1)
     *
     * @param usuarioId ID do usuário (autorização)
     * @param fornecedorId ID do fornecedor a desfavoritar
     * @return Quantidade de registros deletados
     */
    @Modifying
    @Query("DELETE FROM FornecedorFavorito ff WHERE ff.usuarioId = :usuarioId AND ff.fornecedorId = :fornecedorId")
    int deleteByUsuarioIdAndFornecedorId(@Param("usuarioId") Long usuarioId, @Param("fornecedorId") Long fornecedorId);

    /**
     * Conta quantos fornecedores um usuário está seguindo
     * Útil para estatísticas e badges
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de fornecedores favoritos
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Conta quantos seguidores um fornecedor tem
     * Útil para estatísticas do fornecedor e gamificação
     *
     * @param fornecedorId ID do fornecedor
     * @return Quantidade de seguidores
     */
    long countByFornecedorId(Long fornecedorId);

    /**
     * Lista IDs de todos os fornecedores favoritos de um usuário
     * Query otimizada para verificação rápida
     *
     * @param usuarioId ID do usuário
     * @return Lista de IDs dos fornecedores favoritos
     */
    @Query("SELECT ff.fornecedorId FROM FornecedorFavorito ff WHERE ff.usuarioId = :usuarioId")
    List<Long> findFornecedorIdsByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista usuários que estão seguindo um fornecedor específico
     * COM notificações ativas (para enviar alertas de novas ofertas)
     *
     * @param fornecedorId ID do fornecedor
     * @return Lista de IDs dos usuários seguidores com notificações ativas
     */
    @Query("SELECT ff.usuarioId FROM FornecedorFavorito ff WHERE ff.fornecedorId = :fornecedorId AND ff.notificarNovasOfertas = true")
    List<Long> findUsuarioIdsComNotificacoesAtivasByFornecedorId(@Param("fornecedorId") Long fornecedorId);

    /**
     * Lista TODOS os usuários que estão seguindo um fornecedor
     * Independente de terem notificações ativas
     *
     * @param fornecedorId ID do fornecedor
     * @return Lista de IDs dos usuários seguidores
     */
    @Query("SELECT ff.usuarioId FROM FornecedorFavorito ff WHERE ff.fornecedorId = :fornecedorId")
    List<Long> findUsuarioIdsByFornecedorId(@Param("fornecedorId") Long fornecedorId);

    /**
     * Atualiza a flag de notificações de um favorito
     * Permite o usuário "silenciar" sem desfavoritar
     *
     * @param usuarioId ID do usuário (autorização)
     * @param fornecedorId ID do fornecedor
     * @param notificar true para ativar, false para desativar
     * @return Quantidade de registros atualizados
     */
    @Modifying
    @Query("UPDATE FornecedorFavorito ff SET ff.notificarNovasOfertas = :notificar WHERE ff.usuarioId = :usuarioId AND ff.fornecedorId = :fornecedorId")
    int updateNotificacoes(@Param("usuarioId") Long usuarioId, @Param("fornecedorId") Long fornecedorId, @Param("notificar") Boolean notificar);
}
