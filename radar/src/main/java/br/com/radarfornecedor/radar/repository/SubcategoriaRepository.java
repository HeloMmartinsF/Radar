package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Subcategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {

    List<Subcategoria> findByCategoriaAndAtivaTrue(String categoria);

    List<Subcategoria> findByAtivaTrue();

    @Query("SELECT COUNT(s) FROM Subcategoria s WHERE s.ativa = true")
    long countAtivas();
}
