package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade EventoProduto
 * Rastreia visualizações de produtos
 * Para evitar contagem duplicada, eventos do mesmo usuário no mesmo produto
 * são registrados apenas uma vez por dia
 */
@Entity
@Table(name = "EVENTOS_PRODUTO",
       indexes = {
           @Index(name = "idx_evento_produto_id", columnList = "PRODUTO_ID"),
           @Index(name = "idx_evento_prod_fornecedor_id", columnList = "FORNECEDOR_ID"),
           @Index(name = "idx_evento_prod_criado_em", columnList = "CRIADO_EM")
       })
public class EventoProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Column(name = "PRODUTO_ID", nullable = false)
    private Long produtoId;

    @NotNull
    @Column(name = "FORNECEDOR_ID", nullable = false)
    private Long fornecedorId;

    /**
     * ID do usuário que visualizou (pode ser null para usuários não logados)
     */
    @Column(name = "USUARIO_ID")
    private Long usuarioId;

    /**
     * IP do usuário (para deduplicação básica de visualizações anônimas)
     */
    @Column(name = "IP_USUARIO", length = 45)
    private String ipUsuario;

    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getIpUsuario() {
        return ipUsuario;
    }

    public void setIpUsuario(String ipUsuario) {
        this.ipUsuario = ipUsuario;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
