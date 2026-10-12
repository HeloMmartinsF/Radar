package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade que representa uma lista de compras de um usuário.
 * Permite organizar produtos que pretende adquirir futuramente.
 * 
 * REGRAS:
 * - Cada usuário pode ter múltiplas listas
 * - Lista contém itens (produtos) com quantidades
 * - Lista é privada (apenas o dono pode ver/modificar)
 * - Itens podem ser marcados como concluídos
 * - Cálculo de valor estimado baseado em preços disponíveis
 */
@Entity
@Table(
    name = "LISTAS_COMPRAS",
    indexes = {
        @Index(name = "idx_lista_usuario", columnList = "USUARIO_ID"),
        @Index(name = "idx_lista_criada", columnList = "CRIADA_EM"),
        @Index(name = "idx_lista_ativa", columnList = "ATIVA")
    }
)
public class ListaCompras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID do usuário dono da lista
     */
    @NotNull
    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    /**
     * Nome/título da lista (ex: "Compras de Março", "Material de Escritório")
     */
    @NotBlank
    @Column(name = "NOME", nullable = false, length = 100)
    private String nome;

    /**
     * Descrição opcional da lista
     */
    @Column(name = "DESCRICAO", length = 500)
    private String descricao;

    /**
     * Data de criação da lista
     */
    @NotNull
    @Column(name = "CRIADA_EM", nullable = false)
    private LocalDateTime criadaEm;

    /**
     * Data da última atualização
     */
    @Column(name = "ATUALIZADA_EM")
    private LocalDateTime atualizadaEm;

    /**
     * Se a lista está ativa (permite arquivar listas antigas)
     */
    @NotNull
    @Column(name = "ATIVA", nullable = false)
    private Boolean ativa = true;

    /**
     * Itens da lista (produtos + quantidades)
     * Relacionamento OneToMany - quando deletar lista, deleta itens também
     */
    @OneToMany(mappedBy = "lista", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemListaCompras> itens = new ArrayList<>();

    /**
     * Construtor padrão JPA
     */
    public ListaCompras() {
        this.criadaEm = LocalDateTime.now();
        this.atualizadaEm = LocalDateTime.now();
        this.ativa = true;
    }

    /**
     * Construtor para criar nova lista
     */
    public ListaCompras(Long usuarioId, String nome) {
        this();
        this.usuarioId = usuarioId;
        this.nome = nome;
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadaEm = LocalDateTime.now();
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

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public LocalDateTime getAtualizadaEm() {
        return atualizadaEm;
    }

    public void setAtualizadaEm(LocalDateTime atualizadaEm) {
        this.atualizadaEm = atualizadaEm;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public List<ItemListaCompras> getItens() {
        return itens;
    }

    public void setItens(List<ItemListaCompras> itens) {
        this.itens = itens;
    }

    // Métodos auxiliares

    /**
     * Adiciona um item à lista
     */
    public void adicionarItem(ItemListaCompras item) {
        itens.add(item);
        item.setLista(this);
    }

    /**
     * Remove um item da lista
     */
    public void removerItem(ItemListaCompras item) {
        itens.remove(item);
        item.setLista(null);
    }
}
