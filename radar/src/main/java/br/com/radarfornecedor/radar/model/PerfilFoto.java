package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Galeria de fotos adicionais de um perfil (Fornecedor ou Cliente).
 *
 * Segue o mesmo padrão de armazenamento de imagens já usado pelo projeto:
 * Base64 em coluna TEXT do banco de dados H2.
 *
 * Vinculação:
 *   - Para Fornecedor: donoPerfil = "FORNECEDOR", donoId = fornecedor.id
 *   - Para Cliente:    donoPerfil = "CLIENTE",    donoId = cliente.id
 */
@Entity
@Table(name = "PerfilFotos")
public class PerfilFoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tipo do dono: "FORNECEDOR" ou "CLIENTE".
     * Evita FK dupla e mantém a arquitetura sem JPA join para entidades distintas.
     */
    @Column(name = "DonoPerfil", nullable = false, length = 20)
    private String donoPerfil;

    /** ID do Fornecedor ou Cliente dono desta foto. */
    @Column(name = "DonoId", nullable = false)
    private Long donoId;

    /** Conteúdo da imagem em Base64 — mesmo padrão de Fornecedor.foto e Cliente.foto. */
    @Column(name = "Foto", columnDefinition = "TEXT", nullable = false)
    private String foto;

    /** Nome original do arquivo, ex: "loja.jpg". */
    @Column(name = "FotoNome", length = 255)
    private String fotoNome;

    /** Legenda opcional da foto (máx 200 chars). */
    @Column(name = "Legenda", length = 200)
    private String legenda;

    /** Ordem de exibição na galeria (menor = primeiro). */
    @Column(name = "Ordem")
    private Integer ordem = 0;

    @Column(name = "CriadaEm", nullable = false)
    private LocalDateTime criadaEm;

    @PrePersist
    public void prePersist() {
        if (criadaEm == null) {
            criadaEm = LocalDateTime.now();
        }
        if (ordem == null) {
            ordem = 0;
        }
    }

    // ── Construtores ──────────────────────────────────────────────────────────

    public PerfilFoto() {}

    public PerfilFoto(String donoPerfil, Long donoId, String foto, String fotoNome) {
        this.donoPerfil = donoPerfil;
        this.donoId     = donoId;
        this.foto       = foto;
        this.fotoNome   = fotoNome;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getDonoPerfil() { return donoPerfil; }
    public void setDonoPerfil(String donoPerfil) { this.donoPerfil = donoPerfil; }

    public Long getDonoId() { return donoId; }
    public void setDonoId(Long donoId) { this.donoId = donoId; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getFotoNome() { return fotoNome; }
    public void setFotoNome(String fotoNome) { this.fotoNome = fotoNome; }

    public String getLegenda() { return legenda; }
    public void setLegenda(String legenda) { this.legenda = legenda; }

    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }

    public LocalDateTime getCriadaEm() { return criadaEm; }
    public void setCriadaEm(LocalDateTime criadaEm) { this.criadaEm = criadaEm; }
}
