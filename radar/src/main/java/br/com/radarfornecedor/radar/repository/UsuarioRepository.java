package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);
    Optional<Usuario> findByCnpjOuCpf(String cnpjOuCpf);
}
