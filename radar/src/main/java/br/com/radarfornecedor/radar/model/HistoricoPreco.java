package br.com.radarfornecedor.radar.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity para Histórico de Preços
 * Registra todas as mudanças de preço dos produtos ao longo do tempo.
 * Usado para:
 * - Detectar reduções de preço
 * - Análise de tendências
 * - Validar alertas de preço
 * - Gerar gráficos de variação
 */
@Entity
@Table(name = "historico_precos", indexes = {
    @Index(name = "idx_historico_produto", columnList = "produto_id"),
    @Index(name = "idx_historico_data", columnList = "registrado_em"),
    @Index(name = "idx_historico_produto_data", columnList = "produto_id, registrado_em")
})
public class HistoricoPreco {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "produto_id", nullable = false)
    private Long produtoId;
    
    /**
     * Preço do produto neste momento do histórico.
     */
    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
    
    /**
     * Preço promocional (se houver) neste momento.
     */
    @Column(name = "preco_promocional", precision = 10, scale = 2)
    private BigDecimal precoPromocional;
    
    /**
     * Data/hora em que este preço foi registrado.
     */
    @Column(name = "registrado_em", nullable = false)
    private LocalDateTime registradoEm = LocalDateTime.now();
    
    /**
     * Origem do registro:
     * - MANUAL: Cadastrado manualmente
     * - PUBLICACAO: Extraído de uma publicação no feed
     * - JOB_VERIFICACAO: Registrado pelo job scheduled de verificação
     * - API_EXTERNA: Obtido de API externa
     */
    @Column(name = "origem", length = 30)
    @Enumerated(EnumType.STRING)
    private OrigemPreco origem = OrigemPreco.JOB_VERIFICACAO;
    
    /**
     * ID da publicação, se origem for PUBLICACAO.
     */
    @Column(name = "publicacao_id")
    private Long publicacaoId;
    
    /**
     * Variação percentual em relação ao preço anterior.
     * Positivo = aumento, Negativo = redução.
     * NULL se for o primeiro registro.
     */
    @Column(name = "variacao_percentual", precision = 5, scale = 2)
    private BigDecimal variacaoPercentual;
    
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
    
    public BigDecimal getPreco() {
        return preco;
    }
    
    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
    
    public BigDecimal getPrecoPromocional() {
        return precoPromocional;
    }
    
    public void setPrecoPromocional(BigDecimal precoPromocional) {
        this.precoPromocional = precoPromocional;
    }
    
    public LocalDateTime getRegistradoEm() {
        return registradoEm;
    }
    
    public void setRegistradoEm(LocalDateTime registradoEm) {
        this.registradoEm = registradoEm;
    }
    
    public OrigemPreco getOrigem() {
        return origem;
    }
    
    public void setOrigem(OrigemPreco origem) {
        this.origem = origem;
    }
    
    public Long getPublicacaoId() {
        return publicacaoId;
    }
    
    public void setPublicacaoId(Long publicacaoId) {
        this.publicacaoId = publicacaoId;
    }
    
    public BigDecimal getVariacaoPercentual() {
        return variacaoPercentual;
    }
    
    public void setVariacaoPercentual(BigDecimal variacaoPercentual) {
        this.variacaoPercentual = variacaoPercentual;
    }
    
    /**
     * Enum para origem do registro de preço.
     */
    public enum OrigemPreco {
        MANUAL,
        PUBLICACAO,
        JOB_VERIFICACAO,
        API_EXTERNA
    }
}
