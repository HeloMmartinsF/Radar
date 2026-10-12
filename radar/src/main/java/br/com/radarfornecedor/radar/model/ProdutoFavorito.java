package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa um produto favoritado por um usuário.
 * Permite que clientes salvem produtos de interesse para acompanhamento.
 * 
 * REGRAS:
 * - Cada combinação usuarioId + produtoId é única (não permite duplicatas)
 * - Apenas o próprio usuário pode modificar seus favoritos
 * - Remover favorito não deleta o produto
 * - Favoritos são persistentes (continuam após logout/login)
 */
@Entity
@Table(
    name = "PRODUTOS_FAVORITOS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_usuario_produto_favorito",
            columnNames = {"USUARIO_ID", "PRODUTO_ID"}
        )
    },
    indexes = {
        @Index(name = "idx_favorito_usuario", columnList = "USUARIO_ID"),
        @Index(name = "idx_favorito_produto", columnList = "PRODUTO_ID"),
        @Index(name = "idx_favorito_criado", columnList = "CRIADO_EM")
    }
)
public class ProdutoFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID do usuário que favoritou o produto
     */
    @NotNull
    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    /**
     * ID do produto favoritado
     */
    @NotNull
    @Column(name = "PRODUTO_ID", nullable = false)
    private Long produtoId;

    /**
     * Data e hora em que o produto foi favoritado
     */
    @NotNull
    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    /**
     * Notas pessoais do usuário sobre o produto (opcional)
     * Exemplo: "Para o casamento", "Verificar em março"
     */
    @Column(name = "NOTAS", length = 500)
    private String notas;

    /**
     * Construtor padrão JPA
     */
    public ProdutoFavorito() {
        this.criadoEm = LocalDateTime.now();
    }

    /**
     * Construtor para criar novo favorito
     */
    public ProdutoFavorito(Long usuarioId, Long produtoId) {
        this();
        this.usuarioId = usuarioId;
        this.produtoId = produtoId;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
