package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.CurtidaPublicacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurtidaPublicacaoRepository extends JpaRepository<CurtidaPublicacao, Long> {
    
    /**
     * Verifica se um usuário já curtiu uma publicação
     */
    boolean existsByPublicacaoIdAndUsuarioId(Long publicacaoId, Long usuarioId);
    
    /**
     * Busca uma curtida específica (para descurtir)
     */
    Optional<CurtidaPublicacao> findByPublicacaoIdAndUsuarioId(Long publicacaoId, Long usuarioId);
    
    /**
     * Conta curtidas de uma publicação
     */
    Long countByPublicacaoId(Long publicacaoId);
    
    /**
     * Busca todas as curtidas de um usuário
     */
    List<CurtidaPublicacao> findByUsuarioId(Long usuarioId);
    
    /**
     * Busca todas as curtidas de uma publicação
     */
    List<CurtidaPublicacao> findByPublicacaoId(Long publicacaoId);
    
    /**
     * Deleta curtida de uma publicação por um usuário
     */
    void deleteByPublicacaoIdAndUsuarioId(Long publicacaoId, Long usuarioId);
}
