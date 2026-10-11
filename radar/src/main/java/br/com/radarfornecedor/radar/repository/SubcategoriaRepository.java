package br.com.radarfornecedor.radar.repository;

import br.com.radarfornecedor.radar.model.Subcategoria;
import br.com.radarfornecedor.radar.model.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubcategoriaRepository extends JpaRepository<Subcategoria, Long> {

    List<Subcategoria> findByCategoria(CategoriaProduto categoria);

    List<Subcategoria> findByCategoriaAndAtivaTrue(CategoriaProduto categoria);

    List<Subcategoria> findByAtivaTrue();

    @Query("SELECT COUNT(s) FROM Subcategoria s WHERE s.ativa = true")
    long countByAtivaTrue();

    @Query("SELECT s.categoria, COUNT(s) FROM Subcategoria s GROUP BY s.categoria")
    List<Object[]> countByCategoria();
}
