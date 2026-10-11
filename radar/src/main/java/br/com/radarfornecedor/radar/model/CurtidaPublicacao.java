package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade CurtidaPublicacao
 * Representa uma curtida de um usuário em uma publicação
 */
@Entity
@Table(name = "CURTIDAS_PUBLICACAO", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"PUBLICACAO_ID", "USUARIO_ID"}))
public class CurtidaPublicacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotNull
    @Column(name = "PUBLICACAO_ID", nullable = false)
    private Long publicacaoId;

    @NotNull
    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    @Column(name = "CRIADA_EM", nullable = false)
    private LocalDateTime criadaEm;

    @PrePersist
    protected void onCreate() {
        criadaEm = LocalDateTime.now();
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

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }
}
