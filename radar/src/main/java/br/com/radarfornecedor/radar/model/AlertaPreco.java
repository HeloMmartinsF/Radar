package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity para Alertas de Preço
 * Permite que usuários criem alertas para serem notificados quando
 * o preço de um produto atingir um valor-alvo específico.
 */
@Entity
@Table(name = "alertas_preco", indexes = {
    @Index(name = "idx_alerta_usuario", columnList = "usuario_id"),
    @Index(name = "idx_alerta_produto", columnList = "produto_id"),
    @Index(name = "idx_alerta_ativo", columnList = "ativo"),
    @Index(name = "idx_alerta_usuario_produto", columnList = "usuario_id, produto_id")
})
public class AlertaPreco {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;
    
    @Column(name = "produto_id", nullable = false)
    private Long produtoId;
    
    /**
     * Preço-alvo que o usuário deseja ser notificado.
     * Quando o preço do produto atingir ou ficar abaixo deste valor,
     * uma notificação será enviada.
     */
    @Column(name = "preco_alvo", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoAlvo;
    
    /**
     * Critério de comparação:
     * - MENOR_OU_IGUAL: notifica quando preço <= precoAlvo (padrão)
     * - MENOR: notifica quando preço < precoAlvo
     * - IGUAL: notifica quando preço == precoAlvo (tolerância de 0.01)
     */
    @Column(name = "criterio", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private CriterioAlerta criterio = CriterioAlerta.MENOR_OU_IGUAL;
    
    /**
     * Indica se o alerta está ativo.
     * Alertas inativos não são verificados pelo job scheduled.
     */
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;
    
    /**
     * Data/hora em que o alerta foi criado.
     */
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();
    
    /**
     * Data/hora da última verificação do alerta.
     * Atualizado pelo job scheduled a cada execução.
     */
    @Column(name = "ultima_verificacao")
    private LocalDateTime ultimaVerificacao;
    
    /**
     * Data/hora em que o alerta foi disparado (preço atingido).
     * NULL se ainda não foi disparado.
     */
    @Column(name = "disparado_em")
    private LocalDateTime disparadoEm;
    
    /**
     * Preço do produto no momento em que o alerta foi disparado.
     */
    @Column(name = "preco_disparado", precision = 10, scale = 2)
    private BigDecimal precoDisparado;
    
    /**
     * Notas/observações do usuário sobre este alerta.
     */
    @Column(name = "notas", length = 1000)
    private String notas;
    
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
    
    public BigDecimal getPrecoAlvo() {
        return precoAlvo;
    }
    
    public void setPrecoAlvo(BigDecimal precoAlvo) {
        this.precoAlvo = precoAlvo;
    }
    
    public CriterioAlerta getCriterio() {
        return criterio;
    }
    
    public void setCriterio(CriterioAlerta criterio) {
        this.criterio = criterio;
    }
    
    public Boolean getAtivo() {
        return ativo;
    }
    
    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
    
    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
    
    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
    
    public LocalDateTime getUltimaVerificacao() {
        return ultimaVerificacao;
    }
    
    public void setUltimaVerificacao(LocalDateTime ultimaVerificacao) {
        this.ultimaVerificacao = ultimaVerificacao;
    }
    
    public LocalDateTime getDisparadoEm() {
        return disparadoEm;
    }
    
    public void setDisparadoEm(LocalDateTime disparadoEm) {
        this.disparadoEm = disparadoEm;
    }
    
    public BigDecimal getPrecoDisparado() {
        return precoDisparado;
    }
    
    public void setPrecoDisparado(BigDecimal precoDisparado) {
        this.precoDisparado = precoDisparado;
    }
    
    public String getNotas() {
        return notas;
    }
    
    public void setNotas(String notas) {
        this.notas = notas;
    }
    
    /**
     * Enum para critérios de comparação de preço.
     */
    public enum CriterioAlerta {
        MENOR_OU_IGUAL,  // Preço <= alvo
        MENOR,            // Preço < alvo
        IGUAL             // Preço == alvo (com tolerância de 0.01)
    }
}
