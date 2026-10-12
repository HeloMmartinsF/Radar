package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.ItemListaCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar itens de listas de compras.
 * Queries otimizadas com índices para performance.
 */
@Repository
public interface ItemListaComprasRepository extends JpaRepository<ItemListaCompras, Long> {

    /**
     * Lista todos os itens de uma lista de compras
     * Ordenados por data de adição (mais recentes primeiro)
     *
     * @param listaId ID da lista
     * @return Lista de itens
     */
    @Query("SELECT i FROM ItemListaCompras i WHERE i.lista.id = :listaId ORDER BY i.adicionadoEm DESC")
    List<ItemListaCompras> findByListaIdOrderByAdicionadoEmDesc(@Param("listaId") Long listaId);

    /**
     * Lista apenas itens NÃO concluídos de uma lista
     *
     * @param listaId ID da lista
     * @return Lista de itens pendentes
     */
    @Query("SELECT i FROM ItemListaCompras i WHERE i.lista.id = :listaId AND i.concluido = false ORDER BY i.adicionadoEm DESC")
    List<ItemListaCompras> findByListaIdAndConcluidoFalse(@Param("listaId") Long listaId);

    /**
     * Lista apenas itens CONCLUÍDOS de uma lista
     *
     * @param listaId ID da lista
     * @return Lista de itens concluídos
     */
    @Query("SELECT i FROM ItemListaCompras i WHERE i.lista.id = :listaId AND i.concluido = true ORDER BY i.concluidoEm DESC")
    List<ItemListaCompras> findByListaIdAndConcluidoTrue(@Param("listaId") Long listaId);

    /**
     * Busca um item específico de uma lista
     * Valida que o item pertence à lista (autorização indireta)
     *
     * @param id ID do item
     * @param listaId ID da lista
     * @return Optional com o item, se existir e pertencer à lista
     */
    @Query("SELECT i FROM ItemListaCompras i WHERE i.id = :id AND i.lista.id = :listaId")
    Optional<ItemListaCompras> findByIdAndListaId(@Param("id") Long id, @Param("listaId") Long listaId);

    /**
     * Verifica se um produto já está em uma lista
     * Útil para prevenir duplicatas
     *
     * @param listaId ID da lista
     * @param produtoId ID do produto
     * @return true se o produto já está na lista
     */
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM ItemListaCompras i WHERE i.lista.id = :listaId AND i.produtoId = :produtoId")
    boolean existsByListaIdAndProdutoId(@Param("listaId") Long listaId, @Param("produtoId") Long produtoId);

    /**
     * Conta quantos itens uma lista possui
     *
     * @param listaId ID da lista
     * @return Quantidade total de itens
     */
    @Query("SELECT COUNT(i) FROM ItemListaCompras i WHERE i.lista.id = :listaId")
    long countByListaId(@Param("listaId") Long listaId);

    /**
     * Conta quantos itens NÃO concluídos uma lista possui
     *
     * @param listaId ID da lista
     * @return Quantidade de itens pendentes
     */
    @Query("SELECT COUNT(i) FROM ItemListaCompras i WHERE i.lista.id = :listaId AND i.concluido = false")
    long countByListaIdAndConcluidoFalse(@Param("listaId") Long listaId);

    /**
     * Deleta um item específico de uma lista
     * Retorna quantidade de registros deletados (0 ou 1)
     *
     * @param id ID do item
     * @param listaId ID da lista (autorização)
     * @return Quantidade de registros deletados
     */
    @Modifying
    @Query("DELETE FROM ItemListaCompras i WHERE i.id = :id AND i.lista.id = :listaId")
    int deleteByIdAndListaId(@Param("id") Long id, @Param("listaId") Long listaId);

    /**
     * Busca item por produto em uma lista específica
     * Útil para atualizar quantidade se produto já existe
     *
     * @param listaId ID da lista
     * @param produtoId ID do produto
     * @return Optional com o item, se existir
     */
    @Query("SELECT i FROM ItemListaCompras i WHERE i.lista.id = :listaId AND i.produtoId = :produtoId")
    Optional<ItemListaCompras> findByListaIdAndProdutoId(@Param("listaId") Long listaId, @Param("produtoId") Long produtoId);
}
