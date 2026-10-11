package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.AvaliacaoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * AvaliacaoFornecedorRepository - Acesso a dados de avaliações de fornecedores
 * 
 * Queries principais:
 * - Listar avaliações de um fornecedor
 * - Calcular média de notas
 * - Verificar se usuário já avaliou
 * - Contar total de avaliações
 */
@Repository
public interface AvaliacaoFornecedorRepository extends JpaRepository<AvaliacaoFornecedor, Long> {

    /**
     * Busca todas as avaliações de um fornecedor, ordenadas da mais recente para a mais antiga
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista de avaliações ordenadas por data de criação (DESC)
     */
    List<AvaliacaoFornecedor> findByFornecedorIdOrderByCriadoEmDesc(Long fornecedorId);

    /**
     * Busca todas as avaliações de um fornecedor sem ordenação específica
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista de avaliações
     */
    List<AvaliacaoFornecedor> findByFornecedorId(Long fornecedorId);

    /**
     * Conta o total de avaliações recebidas por um fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @return Número total de avaliações
     */
    Long countByFornecedorId(Long fornecedorId);

    /**
     * Verifica se um usuário já avaliou determinado fornecedor
     * Útil para impedir avaliações duplicadas
     * 
     * @param fornecedorId ID do fornecedor
     * @param avaliadorUsuarioId ID do usuário avaliador
     * @return true se já existe avaliação, false caso contrário
     */
    boolean existsByFornecedorIdAndAvaliadorUsuarioId(Long fornecedorId, Long avaliadorUsuarioId);

    /**
     * Busca uma avaliação específica de um usuário para um fornecedor
     * 
     * @param fornecedorId ID do fornecedor
     * @param avaliadorUsuarioId ID do usuário avaliador
     * @return Optional com a avaliação, se existir
     */
    Optional<AvaliacaoFornecedor> findByFornecedorIdAndAvaliadorUsuarioId(Long fornecedorId, Long avaliadorUsuarioId);

    /**
     * Calcula a média das notas recebidas por um fornecedor
     * Retorna NULL se o fornecedor não tiver nenhuma avaliação
     * 
     * @param fornecedorId ID do fornecedor
     * @return Média das notas (ex: 4.5) ou NULL se não houver avaliações
     */
    @Query("SELECT AVG(a.nota) FROM AvaliacaoFornecedor a WHERE a.fornecedorId = :fornecedorId")
    Double calcularMediaNotas(@Param("fornecedorId") Long fornecedorId);

    /**
     * Busca avaliações de um fornecedor que possuem resposta
     * Útil para exibir interações do fornecedor com clientes
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista de avaliações com resposta
     */
    @Query("SELECT a FROM AvaliacaoFornecedor a WHERE a.fornecedorId = :fornecedorId AND a.respostaFornecedor IS NOT NULL ORDER BY a.criadoEm DESC")
    List<AvaliacaoFornecedor> findAvaliacoesComResposta(@Param("fornecedorId") Long fornecedorId);

    /**
     * Busca avaliações de um fornecedor que ainda não foram respondidas
     * Útil para o fornecedor ver quais avaliações precisam de atenção
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista de avaliações sem resposta
     */
    @Query("SELECT a FROM AvaliacaoFornecedor a WHERE a.fornecedorId = :fornecedorId AND a.respostaFornecedor IS NULL ORDER BY a.criadoEm DESC")
    List<AvaliacaoFornecedor> findAvaliacoesSemResposta(@Param("fornecedorId") Long fornecedorId);

    /**
     * Busca avaliações por nota específica
     * Útil para análise de satisfação (ex: ver apenas avaliações 5 estrelas ou apenas 1 estrela)
     * 
     * @param fornecedorId ID do fornecedor
     * @param nota Nota desejada (1 a 5)
     * @return Lista de avaliações com a nota especificada
     */
    List<AvaliacaoFornecedor> findByFornecedorIdAndNota(Long fornecedorId, Integer nota);

    /**
     * Conta quantas avaliações de cada nota um fornecedor possui
     * Útil para exibir distribuição de avaliações (histograma)
     * 
     * @param fornecedorId ID do fornecedor
     * @param nota Nota específica
     * @return Quantidade de avaliações com aquela nota
     */
    Long countByFornecedorIdAndNota(Long fornecedorId, Integer nota);

    /**
     * Busca todas as avaliações feitas por um usuário específico
     * Útil para ver histórico de avaliações de um usuário
     * 
     * @param avaliadorUsuarioId ID do usuário avaliador
     * @return Lista de avaliações feitas por este usuário
     */
    List<AvaliacaoFornecedor> findByAvaliadorUsuarioIdOrderByCriadoEmDesc(Long avaliadorUsuarioId);

    /**
     * Busca as N avaliações mais recentes de um fornecedor
     * Útil para exibir preview de avaliações sem carregar todas
     * 
     * Exemplo de uso no Service:
     * <pre>
     * PageRequest pageRequest = PageRequest.of(0, limit);
     * return repository.findTopNByFornecedorId(fornecedorId, pageRequest);
     * </pre>
     * 
     * @param fornecedorId ID do fornecedor
     * @return Lista limitada de avaliações mais recentes
     */
    @Query("SELECT a FROM AvaliacaoFornecedor a WHERE a.fornecedorId = :fornecedorId ORDER BY a.criadoEm DESC")
    List<AvaliacaoFornecedor> findTopNByFornecedorId(@Param("fornecedorId") Long fornecedorId);
}
