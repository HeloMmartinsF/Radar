package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa um fornecedor favoritado/seguido por um usuário.
 * Permite que clientes acompanhem fornecedores de interesse e recebam notificações.
 * 
 * REGRAS:
 * - Cada combinação usuarioId + fornecedorId é única (não permite duplicatas)
 * - Apenas o próprio usuário pode modificar seus favoritos
 * - Remover favorito não deleta o fornecedor
 * - Usuário recebe notificações quando fornecedor favoritado publicar ofertas
 * - Favoritos são persistentes (continuam após logout/login)
 */
@Entity
@Table(
    name = "FORNECEDORES_FAVORITOS",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_usuario_fornecedor_favorito",
            columnNames = {"USUARIO_ID", "FORNECEDOR_ID"}
        )
    },
    indexes = {
        @Index(name = "idx_fav_fornecedor_usuario", columnList = "USUARIO_ID"),
        @Index(name = "idx_fav_fornecedor_fornecedor", columnList = "FORNECEDOR_ID"),
        @Index(name = "idx_fav_fornecedor_criado", columnList = "CRIADO_EM"),
        @Index(name = "idx_fav_fornecedor_notificar", columnList = "NOTIFICAR_NOVAS_OFERTAS")
    }
)
public class FornecedorFavorito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID do usuário que está seguindo o fornecedor
     */
    @NotNull
    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    /**
     * ID do fornecedor sendo seguido
     */
    @NotNull
    @Column(name = "FORNECEDOR_ID", nullable = false)
    private Long fornecedorId;

    /**
     * Data e hora em que começou a seguir
     */
    @NotNull
    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    /**
     * Se deve receber notificações de novas ofertas deste fornecedor
     * (permite o usuário "silenciar" notificações sem desfavoritar)
     */
    @NotNull
    @Column(name = "NOTIFICAR_NOVAS_OFERTAS", nullable = false)
    private Boolean notificarNovasOfertas = true;

    /**
     * Notas pessoais sobre o fornecedor (opcional)
     * Exemplo: "Preços bons na terça", "Contato: João"
     */
    @Column(name = "NOTAS", length = 500)
    private String notas;

    /**
     * Construtor padrão JPA
     */
    public FornecedorFavorito() {
        this.criadoEm = LocalDateTime.now();
        this.notificarNovasOfertas = true;
    }

    /**
     * Construtor para criar novo favorito
     */
    public FornecedorFavorito(Long usuarioId, Long fornecedorId) {
        this();
        this.usuarioId = usuarioId;
        this.fornecedorId = fornecedorId;
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

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Boolean getNotificarNovasOfertas() {
        return notificarNovasOfertas;
    }

    public void setNotificarNovasOfertas(Boolean notificarNovasOfertas) {
        this.notificarNovasOfertas = notificarNovasOfertas;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }
}
