package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.PerfilFoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfilFotoRepository extends JpaRepository<PerfilFoto, Long> {

    /** Retorna todas as fotos da galeria de um perfil, ordenadas pela coluna Ordem. */
    List<PerfilFoto> findByDonoPerfilAndDonoIdOrderByOrdemAscCriadaEmAsc(String donoPerfil, Long donoId);

    /** Conta quantas fotos um perfil já tem (limite de galeria). */
    long countByDonoPerfilAndDonoId(String donoPerfil, Long donoId);

    /** Remove todas as fotos de um perfil (usado ao excluir Fornecedor/Cliente). */
    void deleteByDonoPerfilAndDonoId(String donoPerfil, Long donoId);
}
