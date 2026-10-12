package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma conversa comercial entre dois participantes.
 * Uma conversa contém mensagens trocadas entre cliente e fornecedor (ou representante).
 */
@Entity
@Table(name = "CONVERSAS", indexes = {
    @Index(name = "idx_conv_participante1", columnList = "PARTICIPANTE1_ID"),
    @Index(name = "idx_conv_participante2", columnList = "PARTICIPANTE2_ID"),
    @Index(name = "idx_conv_fornecedor", columnList = "FORNECEDOR_ID"),
    @Index(name = "idx_conv_ultima_msg", columnList = "ULTIMA_MENSAGEM_EM")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_conv_dedup", 
        columnNames = {"PARTICIPANTE1_ID", "PARTICIPANTE2_ID", "FORNECEDOR_ID", "CONTEXTO_REFERENCIA"})
})
public class Conversa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID do primeiro participante (quem iniciou a conversa)
     * Referência para Usuario.id
     */
    @NotNull
    @Column(name = "PARTICIPANTE1_ID", nullable = false)
    private Long participante1Id;

    /**
     * ID do segundo participante (destinatário inicial)
     * Referência para Usuario.id
     */
    @NotNull
    @Column(name = "PARTICIPANTE2_ID", nullable = false)
    private Long participante2Id;

    /**
     * ID do fornecedor relacionado à conversa (contexto comercial)
     * Referência para Fornecedor.id
     * Usado para validar permissões de representantes
     */
    @Column(name = "FORNECEDOR_ID")
    private Long fornecedorId;

    /**
     * Contexto da conversa (PRODUTO, ORCAMENTO, GERAL)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "CONTEXTO", nullable = false, length = 50)
    private ContextoConversa contexto;

    /**
     * ID da entidade de contexto (Produto.id, Orcamento.id, etc)
     * Permite vincular a conversa a um produto específico ou orçamento
     */
    @Column(name = "CONTEXTO_REFERENCIA")
    private Long contextoReferencia;

    /**
     * Data e hora de início da conversa
     */
    @NotNull
    @Column(name = "INICIADA_EM", nullable = false)
    private LocalDateTime iniciadaEm;

    /**
     * Data e hora da última mensagem enviada
     * Usado para ordenação da lista de conversas
     */
    @Column(name = "ULTIMA_MENSAGEM_EM")
    private LocalDateTime ultimaMensagemEm;

    /**
     * Indica se a conversa está ativa ou foi encerrada
     */
    @NotNull
    @Column(name = "ATIVA", nullable = false)
    private Boolean ativa = true;

    /**
     * Contador de mensagens não lidas pelo participante 1
     */
    @NotNull
    @Column(name = "PARTICIPANTE1_NAO_LIDAS", nullable = false)
    private Integer participante1NaoLidas = 0;

    /**
     * Contador de mensagens não lidas pelo participante 2
     */
    @NotNull
    @Column(name = "PARTICIPANTE2_NAO_LIDAS", nullable = false)
    private Integer participante2NaoLidas = 0;

    @PrePersist
    protected void onCreate() {
        iniciadaEm = LocalDateTime.now();
        ultimaMensagemEm = LocalDateTime.now();
        if (ativa == null) ativa = true;
        if (participante1NaoLidas == null) participante1NaoLidas = 0;
        if (participante2NaoLidas == null) participante2NaoLidas = 0;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParticipante1Id() {
        return participante1Id;
    }

    public void setParticipante1Id(Long participante1Id) {
        this.participante1Id = participante1Id;
    }

    public Long getParticipante2Id() {
        return participante2Id;
    }

    public void setParticipante2Id(Long participante2Id) {
        this.participante2Id = participante2Id;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public ContextoConversa getContexto() {
        return contexto;
    }

    public void setContexto(ContextoConversa contexto) {
        this.contexto = contexto;
    }

    public Long getContextoReferencia() {
        return contextoReferencia;
    }

    public void setContextoReferencia(Long contextoReferencia) {
        this.contextoReferencia = contextoReferencia;
    }

    public LocalDateTime getIniciadaEm() {
        return iniciadaEm;
    }

    public void setIniciadaEm(LocalDateTime iniciadaEm) {
        this.iniciadaEm = iniciadaEm;
    }

    public LocalDateTime getUltimaMensagemEm() {
        return ultimaMensagemEm;
    }

    public void setUltimaMensagemEm(LocalDateTime ultimaMensagemEm) {
        this.ultimaMensagemEm = ultimaMensagemEm;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public Integer getParticipante1NaoLidas() {
        return participante1NaoLidas;
    }

    public void setParticipante1NaoLidas(Integer participante1NaoLidas) {
        this.participante1NaoLidas = participante1NaoLidas;
    }

    public Integer getParticipante2NaoLidas() {
        return participante2NaoLidas;
    }

    public void setParticipante2NaoLidas(Integer participante2NaoLidas) {
        this.participante2NaoLidas = participante2NaoLidas;
    }
}
