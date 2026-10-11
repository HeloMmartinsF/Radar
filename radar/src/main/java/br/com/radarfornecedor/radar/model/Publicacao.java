package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade Publicacao - Feed Comercial
 * Representa uma publicação de produto/promoção no feed
 */
@Entity
@Table(name = "PUBLICACOES")
public class Publicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Column(name = "FORNECEDOR_ID", nullable = false)
    private Long fornecedorId;

    @Column(name = "PRODUTO_ID")
    private Long produtoId; // Opcional - pode ser um produto já cadastrado

    @NotBlank
    @Column(name = "TITULO", nullable = false, length = 200)
    private String titulo;

    @Column(name = "DESCRICAO", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "PRECO")
    private Double preco;

    @Column(name = "PRECO_PROMOCIONAL")
    private Double precoPromocional;

    @Column(name = "UNIDADE_VENDA", length = 50)
    private String unidadeVenda; // kg, unidade, caixa, pacote, etc.

    @Column(name = "QUANTIDADE_MINIMA")
    private Integer quantidadeMinima;

    @Column(name = "CONDICOES_ESPECIAIS", length = 500)
    private String condicoesEspeciais; // Preços por volume, condições de pagamento, etc.

    @Column(name = "IMAGENS", columnDefinition = "TEXT")
    private String imagens; // URLs das imagens separadas por vírgula

    @Column(name = "ATIVA", nullable = false)
    private Boolean ativa = true;

    @Column(name = "CRIADA_EM", nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "ATUALIZADA_EM")
    private LocalDateTime atualizadaEm;

    @Column(name = "TOTAL_CURTIDAS", nullable = false)
    private Integer totalCurtidas = 0;

    @PrePersist
    protected void onCreate() {
        criadaEm = LocalDateTime.now();
        atualizadaEm = LocalDateTime.now();
        if (ativa == null) ativa = true;
        if (totalCurtidas == null) totalCurtidas = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadaEm = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Double getPreco() {
        return preco;
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public Double getPrecoPromocional() {
        return precoPromocional;
    }

    public void setPrecoPromocional(Double precoPromocional) {
        this.precoPromocional = precoPromocional;
    }

    public String getUnidadeVenda() {
        return unidadeVenda;
    }

    public void setUnidadeVenda(String unidadeVenda) {
        this.unidadeVenda = unidadeVenda;
    }

    public Integer getQuantidadeMinima() {
        return quantidadeMinima;
    }

    public void setQuantidadeMinima(Integer quantidadeMinima) {
        this.quantidadeMinima = quantidadeMinima;
    }

    public String getCondicoesEspeciais() {
        return condicoesEspeciais;
    }

    public void setCondicoesEspeciais(String condicoesEspeciais) {
        this.condicoesEspeciais = condicoesEspeciais;
    }

    public String getImagens() {
        return imagens;
    }

    public void setImagens(String imagens) {
        this.imagens = imagens;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }

    public Integer getTotalCurtidas() {
        return totalCurtidas;
    }

    public void setTotalCurtidas(Integer totalCurtidas) {
        this.totalCurtidas = totalCurtidas;
    }
}
