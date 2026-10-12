package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.AlertaPreco;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertaPrecoRepository extends JpaRepository<AlertaPreco, Long> {
    
    /**
     * Busca todos os alertas ativos de um usuário, ordenados por data de criação.
     */
    List<AlertaPreco> findByUsuarioIdAndAtivoTrueOrderByCriadoEmDesc(Long usuarioId);
    
    /**
     * Busca todos os alertas de um usuário (ativos e inativos).
     */
    List<AlertaPreco> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
    
    /**
     * Busca alerta específico de um usuário.
     */
    Optional<AlertaPreco> findByIdAndUsuarioId(Long id, Long usuarioId);
    
    /**
     * Busca alertas ativos de um usuário para um produto específico.
     */
    List<AlertaPreco> findByUsuarioIdAndProdutoIdAndAtivoTrue(Long usuarioId, Long produtoId);
    
    /**
     * Busca todos os alertas ativos (para processamento pelo job scheduled).
     */
    List<AlertaPreco> findByAtivoTrue();
    
    /**
     * Busca alertas ativos para um produto específico.
     * Usado para verificar quais usuários devem ser notificados.
     */
    List<AlertaPreco> findByProdutoIdAndAtivoTrue(Long produtoId);
    
    /**
     * Conta quantos alertas ativos um usuário possui.
     */
    long countByUsuarioIdAndAtivoTrue(Long usuarioId);
    
    /**
     * Conta quantos alertas um usuário possui para um produto específico.
     */
    long countByUsuarioIdAndProdutoId(Long usuarioId, Long produtoId);
    
    /**
     * Verifica se existe alerta ativo para usuário + produto.
     */
    boolean existsByUsuarioIdAndProdutoIdAndAtivoTrue(Long usuarioId, Long produtoId);
    
    /**
     * Atualiza a data de última verificação de um alerta.
     */
    @Modifying
    @Query("UPDATE AlertaPreco a SET a.ultimaVerificacao = :data WHERE a.id = :id")
    void atualizarUltimaVerificacao(@Param("id") Long id, @Param("data") LocalDateTime data);
    
    /**
     * Marca alerta como disparado (preço atingido).
     */
    @Modifying
    @Query("UPDATE AlertaPreco a SET a.disparadoEm = :data, a.precoDisparado = :preco, a.ativo = false WHERE a.id = :id")
    void marcarComoDisparado(@Param("id") Long id, @Param("data") LocalDateTime data, @Param("preco") java.math.BigDecimal preco);
    
    /**
     * Deleta alertas de um usuário para um produto específico.
     */
    @Modifying
    @Query("DELETE FROM AlertaPreco a WHERE a.usuarioId = :usuarioId AND a.produtoId = :produtoId")
    void deleteByUsuarioIdAndProdutoId(@Param("usuarioId") Long usuarioId, @Param("produtoId") Long produtoId);
}
