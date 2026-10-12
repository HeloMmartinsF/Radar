package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entidade que representa um item (produto) dentro de uma lista de compras.
 * 
 * REGRAS:
 * - Cada item pertence a uma lista
 * - Contém produto + quantidade + unidade de medida
 * - Pode ser marcado como concluído
 * - Preço unitário capturado no momento da adição (para cálculo de total)
 * - Observações opcionais por item
 */
@Entity
@Table(
    name = "ITENS_LISTA_COMPRAS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_lista_produto",
            columnNames = {"LISTA_ID", "PRODUTO_ID"}
        )
    },
    indexes = {
        @Index(name = "idx_item_lista", columnList = "LISTA_ID"),
        @Index(name = "idx_item_produto", columnList = "PRODUTO_ID"),
        @Index(name = "idx_item_concluido", columnList = "CONCLUIDO"),
        @Index(name = "idx_item_adicionado", columnList = "ADICIONADO_EM")
    }
)
public class ItemListaCompras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * Lista à qual este item pertence
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "LISTA_ID", nullable = false)
    private ListaCompras lista;

    /**
     * ID do produto referenciado
     */
    @NotNull
    @Column(name = "PRODUTO_ID", nullable = false)
    private Long produtoId;

    /**
     * Quantidade desejada
     */
    @NotNull
    @Positive
    @Column(name = "QUANTIDADE", nullable = false)
    private Double quantidade;

    /**
     * Unidade de medida (kg, unidade, caixa, pacote, etc)
     * Capturado do produto ou da publicação no momento da adição
     */
    @Column(name = "UNIDADE", length = 50)
    private String unidade;

    /**
     * Preço unitário capturado no momento da adição (para estimativa)
     * Pode ficar desatualizado, mas serve como referência
     */
    @Column(name = "PRECO_UNITARIO", precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    /**
     * ID da publicação de origem (se adicionado via publicação do feed)
     * Permite rastrear ofertas específicas
     */
    @Column(name = "PUBLICACAO_ID")
    private Long publicacaoId;

    /**
     * Se o item já foi comprado/concluído
     */
    @NotNull
    @Column(name = "CONCLUIDO", nullable = false)
    private Boolean concluido = false;

    /**
     * Data em que foi marcado como concluído
     */
    @Column(name = "CONCLUIDO_EM")
    private LocalDateTime concluidoEm;

    /**
     * Data em que o item foi adicionado à lista
     */
    @NotNull
    @Column(name = "ADICIONADO_EM", nullable = false)
    private LocalDateTime adicionadoEm;

    /**
     * Observações do usuário sobre este item
     * Ex: "Verificar promoção", "Pegar no fornecedor X"
     */
    @Column(name = "OBSERVACOES", length = 500)
    private String observacoes;

    /**
     * Construtor padrão JPA
     */
    public ItemListaCompras() {
        this.adicionadoEm = LocalDateTime.now();
        this.concluido = false;
    }

    /**
     * Construtor para criar novo item
     */
    public ItemListaCompras(ListaCompras lista, Long produtoId, Double quantidade, String unidade) {
        this();
        this.lista = lista;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.unidade = unidade;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ListaCompras getLista() {
        return lista;
    }

    public void setLista(ListaCompras lista) {
        this.lista = lista;
    }

    public Long getProdutoId() {
        return produtoId;
    }

    public void setProdutoId(Long produtoId) {
        this.produtoId = produtoId;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public void setPrecoUnitario(BigDecimal precoUnitario) {
        this.precoUnitario = precoUnitario;
    }

    public Long getPublicacaoId() {
        return publicacaoId;
    }

    public void setPublicacaoId(Long publicacaoId) {
        this.publicacaoId = publicacaoId;
    }

    public Boolean getConcluido() {
        return concluido;
    }

    public void setConcluido(Boolean concluido) {
        this.concluido = concluido;
        if (Boolean.TRUE.equals(concluido) && this.concluidoEm == null) {
            this.concluidoEm = LocalDateTime.now();
        }
    }

    public LocalDateTime getConcluidoEm() {
        return concluidoEm;
    }

    public void setConcluidoEm(LocalDateTime concluidoEm) {
        this.concluidoEm = concluidoEm;
    }

    public LocalDateTime getAdicionadoEm() {
        return adicionadoEm;
    }

    public void setAdicionadoEm(LocalDateTime adicionadoEm) {
        this.adicionadoEm = adicionadoEm;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    /**
     * Calcula o valor total estimado deste item (quantidade * preço unitário)
     * Retorna null se não houver preço disponível
     */
    public BigDecimal calcularValorTotal() {
        if (precoUnitario == null || quantidade == null) {
            return null;
        }
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
