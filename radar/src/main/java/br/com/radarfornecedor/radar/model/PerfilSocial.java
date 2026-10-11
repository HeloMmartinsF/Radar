package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * PerfilSocial - Camada de apresentação social sobre os dados existentes
 * 
 * NÃO DUPLICA DADOS! Apenas adiciona informações de perfil social:
 * - Foto de perfil
 * - Biografia
 * - Configurações de privacidade
 * 
 * Os dados principais (nome, empresa, produtos, etc) vêm das entidades existentes:
 * Usuario, Fornecedor, Cliente, Produto, Publicacao
 */
@Entity
@Table(name = "PERFIS_SOCIAIS")
public class PerfilSocial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * Relacionamento 1:1 com Usuario existente
     * Cada usuário tem um único perfil social
     */
    @OneToOne
    @JoinColumn(name = "USUARIO_ID", nullable = false, unique = true)
    private Usuario usuario;

    /**
     * Foto de perfil (URL ou caminho)
     * Diferente da foto de Fornecedor (que é logotipo da empresa)
     */
    @Column(name = "FOTO_PERFIL", columnDefinition = "TEXT")
    private String fotoPerfil;

    /**
     * Biografia/apresentação pessoal ou profissional (opcional)
     * Máximo 500 caracteres
     */
    @Column(name = "BIOGRAFIA", length = 500)
    private String biografia;

    /**
     * Se o perfil é público (visível para todos) ou privado
     */
    @Column(name = "PERFIL_PUBLICO", nullable = false)
    private Boolean perfilPublico = true;

    /**
     * Se permite receber mensagens de contato
     */
    @Column(name = "PERMITE_CONTATO", nullable = false)
    private Boolean permiteContato = true;

    /**
     * Se exibe o email no perfil público
     */
    @Column(name = "EXIBE_EMAIL", nullable = false)
    private Boolean exibeEmail = false;

    /**
     * Se exibe o telefone no perfil público
     */
    @Column(name = "EXIBE_TELEFONE", nullable = false)
    private Boolean exibeTelefone = false;

    /**
     * Data de criação do perfil
     */
    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    /**
     * Data da última atualização
     */
    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
        if (perfilPublico == null) perfilPublico = true;
        if (permiteContato == null) permiteContato = true;
        if (exibeEmail == null) exibeEmail = false;
        if (exibeTelefone == null) exibeTelefone = false;
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }

    public Boolean getPerfilPublico() {
        return perfilPublico;
    }

    public void setPerfilPublico(Boolean perfilPublico) {
        this.perfilPublico = perfilPublico;
    }

    public Boolean getPermiteContato() {
        return permiteContato;
    }

    public void setPermiteContato(Boolean permiteContato) {
        this.permiteContato = permiteContato;
    }

    public Boolean getExibeEmail() {
        return exibeEmail;
    }

    public void setExibeEmail(Boolean exibeEmail) {
        this.exibeEmail = exibeEmail;
    }

    public Boolean getExibeTelefone() {
        return exibeTelefone;
    }

    public void setExibeTelefone(Boolean exibeTelefone) {
        this.exibeTelefone = exibeTelefone;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
