package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * AvaliacaoFornecedor - Avaliações globais de empresas fornecedoras
 * 
 * Diferente de AvaliacaoProduto (que avalia produtos específicos),
 * esta entidade representa a avaliação da EMPRESA como um todo:
 * - Qualidade do atendimento
 * - Confiabilidade geral
 * - Experiência de compra
 * - Reputação comercial
 * 
 * Regras de Negócio:
 * - Um usuário pode avaliar cada fornecedor apenas UMA vez (constraint UNIQUE)
 * - O fornecedor NÃO pode avaliar a própria empresa (validado no Service)
 * - Apenas o fornecedor avaliado pode responder à sua avaliação
 * - A resposta é pública e aparece vinculada à avaliação
 */
@Entity
@Table(
    name = "AVALIACOES_FORNECEDOR",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_avaliacao_por_usuario",
        columnNames = {"fornecedor_id", "avaliador_usuario_id"}
    )
)
public class AvaliacaoFornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * ID do fornecedor sendo avaliado
     */
    @NotNull(message = "Fornecedor é obrigatório")
    @Column(name = "fornecedor_id", nullable = false)
    private Long fornecedorId;

    /**
     * ID do usuário que está avaliando
     * Pode ser cliente, representante ou outro tipo de usuário
     * NÃO pode ser o próprio fornecedor (validado no Service)
     */
    @NotNull(message = "Avaliador é obrigatório")
    @Column(name = "avaliador_usuario_id", nullable = false)
    private Long avaliadorUsuarioId;

    /**
     * Nota de 1 a 5 estrelas
     * 1 = Muito ruim
     * 2 = Ruim
     * 3 = Regular
     * 4 = Bom
     * 5 = Excelente
     */
    @NotNull(message = "Nota é obrigatória")
    @Min(value = 1, message = "Nota mínima é 1")
    @Max(value = 5, message = "Nota máxima é 5")
    @Column(name = "nota", nullable = false)
    private Integer nota;

    /**
     * Comentário/review (opcional)
     * Máximo 500 caracteres
     */
    @Column(name = "comentario", columnDefinition = "TEXT")
    private String comentario;

    /**
     * Resposta pública do fornecedor à avaliação
     * Apenas o fornecedor avaliado pode responder
     * Máximo 500 caracteres
     */
    @Column(name = "resposta_fornecedor", columnDefinition = "TEXT")
    private String respostaFornecedor;

    /**
     * Data em que o fornecedor respondeu à avaliação
     */
    @Column(name = "data_resposta")
    private LocalDateTime dataResposta;

    /**
     * Data de criação da avaliação
     */
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    // ========================================
    // LIFECYCLE CALLBACKS
    // ========================================

    @PrePersist
    protected void onCreate() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    // ========================================
    // GETTERS E SETTERS
    // ========================================

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

    public Long getAvaliadorUsuarioId() {
        return avaliadorUsuarioId;
    }

    public void setAvaliadorUsuarioId(Long avaliadorUsuarioId) {
        this.avaliadorUsuarioId = avaliadorUsuarioId;
    }

    public Integer getNota() {
        return nota;
    }

    public void setNota(Integer nota) {
        this.nota = nota;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getRespostaFornecedor() {
        return respostaFornecedor;
    }

    public void setRespostaFornecedor(String respostaFornecedor) {
        this.respostaFornecedor = respostaFornecedor;
    }

    public LocalDateTime getDataResposta() {
        return dataResposta;
    }

    public void setDataResposta(LocalDateTime dataResposta) {
        this.dataResposta = dataResposta;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    // ========================================
    // MÉTODOS AUXILIARES
    // ========================================

    /**
     * Verifica se a avaliação já possui resposta do fornecedor
     */
    public boolean temResposta() {
        return respostaFornecedor != null && !respostaFornecedor.trim().isEmpty();
    }

    /**
     * Retorna representação textual da nota em estrelas
     */
    public String getNotaEmEstrelas() {
        return "★".repeat(nota) + "☆".repeat(5 - nota);
    }

    @Override
    public String toString() {
        return "AvaliacaoFornecedor{" +
                "id=" + id +
                ", fornecedorId=" + fornecedorId +
                ", avaliadorUsuarioId=" + avaliadorUsuarioId +
                ", nota=" + nota +
                ", temResposta=" + temResposta() +
                ", criadoEm=" + criadoEm +
                '}';
    }
}
