package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma notificação no sistema.
 * Cada usuário recebe notificações sobre eventos relevantes (publicações, avaliações, seguidores).
 * Inclui campos para deduplicação (evita notificações duplicadas do mesmo evento).
 */
@Entity
@Table(name = "NOTIFICACOES", indexes = {
    @Index(name = "idx_notif_usuario", columnList = "USUARIO_ID"),
    @Index(name = "idx_notif_lida", columnList = "LIDA"),
    @Index(name = "idx_notif_criada", columnList = "CRIADA_EM"),
    @Index(name = "idx_notif_dedup", columnList = "USUARIO_ID,EVENTO_TIPO,EVENTO_ID")
})
public class Notificacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID do usuário que receberá esta notificação
     */
    @NotNull
    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    /**
     * Tipo da notificação (NOVA_PUBLICACAO, NOVA_AVALIACAO, etc)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO", nullable = false, length = 50)
    private TipoNotificacao tipo;

    /**
     * Título curto da notificação (ex: "Nova publicação de Fornecedor X")
     */
    @NotBlank
    @Column(name = "TITULO", nullable = false, length = 200)
    private String titulo;

    /**
     * Mensagem detalhada da notificação
     */
    @Column(name = "MENSAGEM", length = 500)
    private String mensagem;

    /**
     * Link de destino ao clicar na notificação (ex: /perfil-comercial?cnpj=12345678000190)
     */
    @Column(name = "LINK_DESTINO", length = 500)
    private String linkDestino;

    /**
     * Indica se a notificação foi lida pelo usuário
     */
    @NotNull
    @Column(name = "LIDA", nullable = false)
    private Boolean lida = false;

    /**
     * Data e hora de criação da notificação
     */
    @NotNull
    @Column(name = "CRIADA_EM", nullable = false)
    private LocalDateTime criadaEm;

    /**
     * ID do evento que originou a notificação (ex: ID da publicação, ID da avaliação)
     * Usado para deduplicação (evita criar múltiplas notificações do mesmo evento)
     */
    @Column(name = "EVENTO_ID")
    private Long eventoId;

    /**
     * Tipo do evento (ex: "PUBLICACAO", "AVALIACAO", "SEGUIDOR")
     * Usado para deduplicação junto com eventoId
     */
    @Column(name = "EVENTO_TIPO", length = 50)
    private String eventoTipo;

    /**
     * Nome do usuário/fornecedor que gerou o evento (para exibição)
     */
    @Column(name = "ORIGEM_NOME", length = 200)
    private String origemNome;

    /**
     * Construtor padrão JPA
     */
    public Notificacao() {
        this.criadaEm = LocalDateTime.now();
    }

    /**
     * Construtor para criar notificação com dados básicos
     */
    public Notificacao(Long usuarioId, TipoNotificacao tipo, String titulo, String mensagem) {
        this();
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
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

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoNotificacao tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getLinkDestino() {
        return linkDestino;
    }

    public void setLinkDestino(String linkDestino) {
        this.linkDestino = linkDestino;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public Long getEventoId() {
        return eventoId;
    }

    public void setEventoId(Long eventoId) {
        this.eventoId = eventoId;
    }

    public String getEventoTipo() {
        return eventoTipo;
    }

    public void setEventoTipo(String eventoTipo) {
        this.eventoTipo = eventoTipo;
    }

    public String getOrigemNome() {
        return origemNome;
    }

    public void setOrigemNome(String origemNome) {
        this.origemNome = origemNome;
    }
}
