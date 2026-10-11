package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    List<Cliente> findByTipoPessoa(String tipoPessoa);
    
    // Método para Perfil Social
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);
}
