package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma mensagem em um chamado de suporte.
 * Permite a conversa entre usuário e administradores.
 * 
 * SEGURANÇA:
 * - Mensagens só podem ser visualizadas pelo dono do chamado e administradores
 * - Usuários não podem enviar mensagens em chamados de outras pessoas
 * - O campo autorTipo identifica se é USUARIO ou ADMIN
 */
@Entity
@Table(
    name = "MENSAGENS_CHAMADO",
    indexes = {
        @Index(name = "idx_mensagem_chamado", columnList = "CHAMADO_ID"),
        @Index(name = "idx_mensagem_criada", columnList = "CRIADA_EM"),
        @Index(name = "idx_mensagem_autor", columnList = "AUTOR_USUARIO_ID")
    }
)
public class MensagemChamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID do chamado ao qual esta mensagem pertence
     */
    @NotNull
    @Column(name = "CHAMADO_ID", nullable = false)
    private Long chamadoId;

    /**
     * ID do usuário que escreveu a mensagem
     */
    @NotNull
    @Column(name = "AUTOR_USUARIO_ID", nullable = false)
    private Long autorUsuarioId;

    /**
     * Nome do autor (cache para exibição)
     */
    @Column(name = "AUTOR_NOME", length = 200)
    private String autorNome;

    /**
     * Tipo do autor: USUARIO (cliente que abriu o chamado) ou ADMIN (administrador)
     */
    @NotBlank
    @Column(name = "AUTOR_TIPO", nullable = false, length = 20)
    private String autorTipo; // "USUARIO" ou "ADMIN"

    /**
     * Conteúdo da mensagem
     */
    @NotBlank
    @Column(name = "MENSAGEM", nullable = false, columnDefinition = "TEXT")
    private String mensagem;

    /**
     * Data/hora de criação da mensagem
     */
    @NotNull
    @Column(name = "CRIADA_EM", nullable = false)
    private LocalDateTime criadaEm;

    /**
     * Indica se mensagem foi lida pelo destinatário
     * - Se autorTipo=USUARIO, indica se admin leu
     * - Se autorTipo=ADMIN, indica se usuário leu
     */
    @Column(name = "LIDA")
    private Boolean lida = false;

    // Construtores

    public MensagemChamado() {
        this.criadaEm = LocalDateTime.now();
    }

    public MensagemChamado(Long chamadoId, Long autorUsuarioId, String autorTipo, String mensagem) {
        this();
        this.chamadoId = chamadoId;
        this.autorUsuarioId = autorUsuarioId;
        this.autorTipo = autorTipo;
        this.mensagem = mensagem;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChamadoId() {
        return chamadoId;
    }

    public void setChamadoId(Long chamadoId) {
        this.chamadoId = chamadoId;
    }

    public Long getAutorUsuarioId() {
        return autorUsuarioId;
    }

    public void setAutorUsuarioId(Long autorUsuarioId) {
        this.autorUsuarioId = autorUsuarioId;
    }

    public String getAutorNome() {
        return autorNome;
    }

    public void setAutorNome(String autorNome) {
        this.autorNome = autorNome;
    }

    public String getAutorTipo() {
        return autorTipo;
    }

    public void setAutorTipo(String autorTipo) {
        this.autorTipo = autorTipo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }
}
