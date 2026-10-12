package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.ProdutoFavorito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar produtos favoritos dos usuários.
 * Queries otimizadas com índices para performance.
 */
@Repository
public interface ProdutoFavoritoRepository extends JpaRepository<ProdutoFavorito, Long> {

    /**
     * Lista todos os produtos favoritos de um usuário
     * Ordenados por data de adição (mais recentes primeiro)
     *
     * @param usuarioId ID do usuário
     * @return Lista de favoritos do usuário
     */
    List<ProdutoFavorito> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);

    /**
     * Verifica se um produto está favoritado por um usuário
     * Útil para exibir estado do botão de favorito
     *
     * @param usuarioId ID do usuário
     * @param produtoId ID do produto
     * @return true se está favoritado, false caso contrário
     */
    boolean existsByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);

    /**
     * Busca um favorito específico de um usuário
     * Usado para validar permissões antes de remover
     *
     * @param usuarioId ID do usuário
     * @param produtoId ID do produto
     * @return Optional com o favorito, se existir
     */
    Optional<ProdutoFavorito> findByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);

    /**
     * Remove um favorito específico de um usuário
     * Retorna quantidade de registros deletados (0 ou 1)
     *
     * @param usuarioId ID do usuário (autorização)
     * @param produtoId ID do produto a desfavoritar
     * @return Quantidade de registros deletados
     */
    @Modifying
    @Query("DELETE FROM ProdutoFavorito pf WHERE pf.usuarioId = :usuarioId AND pf.produtoId = :produtoId")
    int deleteByUsuarioIdAndProdutoId(@Param("usuarioId") Long usuarioId, @Param("produtoId") Long produtoId);

    /**
     * Conta quantos produtos um usuário favoritou
     * Útil para estatísticas e badges
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de produtos favoritos
     */
    long countByUsuarioId(Long usuarioId);

    /**
     * Conta quantos usuários favoritaram um produto específico
     * Útil para popularidade/analytics do produto
     *
     * @param produtoId ID do produto
     * @return Quantidade de usuários que favoritaram
     */
    long countByProdutoId(Long produtoId);

    /**
     * Lista IDs de todos os produtos favoritos de um usuário
     * Query otimizada para verificação rápida (sem carregar objetos completos)
     *
     * @param usuarioId ID do usuário
     * @return Lista de IDs dos produtos favoritos
     */
    @Query("SELECT pf.produtoId FROM ProdutoFavorito pf WHERE pf.usuarioId = :usuarioId")
    List<Long> findProdutoIdsByUsuarioId(@Param("usuarioId") Long usuarioId);

    /**
     * Lista usuários que favoritaram um produto específico
     * Usado para notificações quando houver alteração de preço
     *
     * @param produtoId ID do produto
     * @return Lista de IDs dos usuários que favoritaram
     */
    @Query("SELECT pf.usuarioId FROM ProdutoFavorito pf WHERE pf.produtoId = :produtoId")
    List<Long> findUsuarioIdsByProdutoId(@Param("produtoId") Long produtoId);
}
