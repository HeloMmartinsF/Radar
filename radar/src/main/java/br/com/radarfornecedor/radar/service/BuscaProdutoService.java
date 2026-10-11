package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.CategoriaProduto;
import br.com.radarfornecedor.radar.model.Produto;
import br.com.radarfornecedor.radar.repository.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Service para busca inteligente de produtos
 * Implementa busca tolerante, filtros avançados e ordenação
 */
@Service
public class BuscaProdutoService {

    private final ProdutoRepository produtoRepository;
    private final EntityManager entityManager;

    public BuscaProdutoService(ProdutoRepository produtoRepository, EntityManager entityManager) {
        this.produtoRepository = produtoRepository;
        this.entityManager = entityManager;
    }

    /**
     * Busca inteligente com filtros e paginação
     */
    public Page<Produto> buscarProdutos(
            String termo,
            CategoriaProduto categoria,
            BigDecimal precoMin,
            BigDecimal precoMax,
            Long fornecedorId,
            String ordenacao,
            Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Produto> query = cb.createQuery(Produto.class);
        Root<Produto> produto = query.from(Produto.class);

        List<Predicate> predicates = new ArrayList<>();

        // Busca tolerante no nome e descrição
        if (termo != null && !termo.trim().isEmpty()) {
            String termoNormalizado = normalizarTexto(termo);
            String pattern = "%" + termoNormalizado.toLowerCase() + "%";

            Predicate nomeMatch = cb.like(
                    cb.lower(cb.function("REPLACE", String.class,
                            cb.function("REPLACE", String.class,
                                    cb.function("REPLACE", String.class,
                                            produto.get("nome"),
                                            cb.literal("á"), cb.literal("a")),
                                    cb.literal("é"), cb.literal("e")),
                            cb.literal("ã"), cb.literal("a"))),
                    pattern
            );

            Predicate descricaoMatch = cb.like(
                    cb.lower(cb.function("REPLACE", String.class,
                            produto.get("descricao"),
                            cb.literal("á"), cb.literal("a"))),
                    pattern
            );

            predicates.add(cb.or(nomeMatch, descricaoMatch));
        }

        // Filtro por categoria
        if (categoria != null) {
            predicates.add(cb.equal(produto.get("categoria"), categoria));
        }

        // Filtro por faixa de preço
        if (precoMin != null) {
            predicates.add(cb.greaterThanOrEqualTo(produto.get("preco"), precoMin));
        }
        if (precoMax != null) {
            predicates.add(cb.lessThanOrEqualTo(produto.get("preco"), precoMax));
        }

        // Filtro por fornecedor
        if (fornecedorId != null) {
            predicates.add(cb.equal(produto.get("fornecedorId"), fornecedorId));
        }

        // Aplicar predicates
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Ordenação
        aplicarOrdenacao(cb, query, produto, ordenacao, termo);

        // Executar query com paginação
        TypedQuery<Produto> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Produto> resultados = typedQuery.getResultList();

        // Contar total
        long total = contarTotal(predicates, cb);

        return new PageImpl<>(resultados, pageable, total);
    }

    /**
     * Aplicar ordenação aos resultados
     */
    private void aplicarOrdenacao(CriteriaBuilder cb, CriteriaQuery<Produto> query,
                                   Root<Produto> produto, String ordenacao, String termo) {
        if (ordenacao == null || ordenacao.equals("relevancia")) {
            // Relevância: produtos com termo no nome primeiro
            if (termo != null && !termo.trim().isEmpty()) {
                query.orderBy(cb.asc(produto.get("nome")));
            } else {
                query.orderBy(cb.asc(produto.get("id")));
            }
        } else if (ordenacao.equals("menor_preco")) {
            query.orderBy(cb.asc(produto.get("preco")));
        } else if (ordenacao.equals("maior_preco")) {
            query.orderBy(cb.desc(produto.get("preco")));
        } else if (ordenacao.equals("nome_az")) {
            query.orderBy(cb.asc(produto.get("nome")));
        } else if (ordenacao.equals("nome_za")) {
            query.orderBy(cb.desc(produto.get("nome")));
        }
    }

    /**
     * Contar total de resultados (para paginação)
     */
    private long contarTotal(List<Predicate> predicates, CriteriaBuilder cb) {
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Produto> countRoot = countQuery.from(Produto.class);
        countQuery.select(cb.count(countRoot));

        if (!predicates.isEmpty()) {
            countQuery.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    /**
     * Normaliza texto removendo acentos
     */
    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String normalized = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalized.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
    }

    /**
     * Busca simples (para compatibilidade)
     */
    public List<Produto> buscarPorNome(String nome) {
        return produtoRepository.findAll().stream()
                .filter(p -> p.getNome().toLowerCase().contains(nome.toLowerCase()))
                .toList();
    }

    /**
     * Busca por categoria
     */
    public List<Produto> buscarPorCategoria(CategoriaProduto categoria) {
        return produtoRepository.findByCategoria(categoria);
    }
}
