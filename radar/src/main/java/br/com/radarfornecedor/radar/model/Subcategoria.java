package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "subcategoria")
public class Subcategoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private CategoriaProduto categoria;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm = LocalDateTime.now();

    // Construtores
    public Subcategoria() {
    }

    public Subcategoria(String nome, String descricao, CategoriaProduto categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.categoria = categoria;
        this.ativa = true;
        this.criadaEm = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public CategoriaProduto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProduto categoria) {
        this.categoria = categoria;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }
}
