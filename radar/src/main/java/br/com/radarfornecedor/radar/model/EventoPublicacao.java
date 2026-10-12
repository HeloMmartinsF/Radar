package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade EventoPublicacao
 * Rastreia eventos de interação com publicações (visualizações e cliques em contato)
 * Para evitar contagem duplicada, eventos do mesmo usuário na mesma publicação
 * são registrados apenas uma vez por dia
 */
@Entity
@Table(name = "EVENTOS_PUBLICACAO",
       indexes = {
           @Index(name = "idx_evento_publicacao_id", columnList = "PUBLICACAO_ID"),
           @Index(name = "idx_evento_fornecedor_id", columnList = "FORNECEDOR_ID"),
           @Index(name = "idx_evento_criado_em", columnList = "CRIADO_EM")
       })
public class EventoPublicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Column(name = "PUBLICACAO_ID", nullable = false)
    private Long publicacaoId;

    @NotNull
    @Column(name = "FORNECEDOR_ID", nullable = false)
    private Long fornecedorId;

    /**
     * Tipo de evento: VIEW (visualização) ou CONTATO (clique em entrar em contato)
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "TIPO_EVENTO", nullable = false, length = 20)
    private TipoEventoPublicacao tipoEvento;

    /**
     * ID do usuário que gerou o evento (pode ser null para usuários não logados)
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

    public Long getPublicacaoId() {
        return publicacaoId;
    }

    public void setPublicacaoId(Long publicacaoId) {
        this.publicacaoId = publicacaoId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public TipoEventoPublicacao getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoPublicacao tipoEvento) {
        this.tipoEvento = tipoEvento;
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
