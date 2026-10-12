package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.ListaCompras;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar listas de compras.
 * Queries otimizadas com índices para performance.
 */
@Repository
public interface ListaComprasRepository extends JpaRepository<ListaCompras, Long> {

    /**
     * Lista todas as listas de compras ativas de um usuário
     * Ordenadas por data de atualização (mais recentes primeiro)
     *
     * @param usuarioId ID do usuário
     * @return Lista de listas de compras ativas
     */
    List<ListaCompras> findByUsuarioIdAndAtivaOrderByAtualizadaEmDesc(Long usuarioId, Boolean ativa);

    /**
     * Lista TODAS as listas de um usuário (ativas + arquivadas)
     * Ordenadas por data de atualização
     *
     * @param usuarioId ID do usuário
     * @return Lista completa de listas de compras
     */
    List<ListaCompras> findByUsuarioIdOrderByAtualizadaEmDesc(Long usuarioId);

    /**
     * Busca uma lista específica de um usuário
     * Valida que a lista pertence ao usuário (autorização)
     *
     * @param id ID da lista
     * @param usuarioId ID do usuário
     * @return Optional com a lista, se existir e pertencer ao usuário
     */
    Optional<ListaCompras> findByIdAndUsuarioId(Long id, Long usuarioId);

    /**
     * Conta quantas listas ativas um usuário possui
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de listas ativas
     */
    long countByUsuarioIdAndAtiva(Long usuarioId, Boolean ativa);

    /**
     * Verifica se uma lista existe e pertence ao usuário
     * Útil para validações rápidas
     *
     * @param id ID da lista
     * @param usuarioId ID do usuário
     * @return true se existe e pertence ao usuário
     */
    boolean existsByIdAndUsuarioId(Long id, Long usuarioId);
}
