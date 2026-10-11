package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.PerfilSocial;
import br.com.radarfornecedor.radar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para PerfilSocial
 */
@Repository
public interface PerfilSocialRepository extends JpaRepository<PerfilSocial, Long> {
    
    /**
     * Busca o perfil social de um usuário específico
     * @param usuario Usuario autenticado
     * @return Perfil social do usuário (se existir)
     */
    Optional<PerfilSocial> findByUsuario(Usuario usuario);
    
    /**
     * Busca perfil social pelo ID do usuário
     * @param usuarioId ID do usuário
     * @return Perfil social do usuário (se existir)
     */
    Optional<PerfilSocial> findByUsuarioId(Long usuarioId);
    
    /**
     * Verifica se um usuário já tem perfil social criado
     * @param usuario Usuario
     * @return true se já existe perfil
     */
    boolean existsByUsuario(Usuario usuario);
}
