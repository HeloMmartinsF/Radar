package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa uma mensagem individual em uma conversa comercial.
 * Cada mensagem pertence a uma conversa e tem um remetente.
 */
@Entity
@Table(name = "MENSAGENS_CHAT", indexes = {
    @Index(name = "idx_msg_conversa", columnList = "CONVERSA_ID"),
    @Index(name = "idx_msg_remetente", columnList = "REMETENTE_ID"),
    @Index(name = "idx_msg_enviada", columnList = "ENVIADA_EM")
})
public class MensagemChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * ID da conversa à qual esta mensagem pertence
     * Referência para Conversa.id
     */
    @NotNull
    @Column(name = "CONVERSA_ID", nullable = false)
    private Long conversaId;

    /**
     * ID do usuário que enviou a mensagem
     * Referência para Usuario.id
     */
    @NotNull
    @Column(name = "REMETENTE_ID", nullable = false)
    private Long remetenteId;

    /**
     * Conteúdo da mensagem (texto)
     * Máximo 5000 caracteres
     */
    @NotBlank
    @Column(name = "TEXTO", nullable = false, columnDefinition = "TEXT")
    private String texto;

    /**
     * Data e hora de envio da mensagem
     */
    @NotNull
    @Column(name = "ENVIADA_EM", nullable = false)
    private LocalDateTime enviadaEm;

    /**
     * Indica se a mensagem foi lida pelo destinatário
     */
    @NotNull
    @Column(name = "LIDA", nullable = false)
    private Boolean lida = false;

    /**
     * Indica se a mensagem foi editada após o envio
     */
    @NotNull
    @Column(name = "EDITADA", nullable = false)
    private Boolean editada = false;

    @PrePersist
    protected void onCreate() {
        enviadaEm = LocalDateTime.now();
        if (lida == null) lida = false;
        if (editada == null) editada = false;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversaId() {
        return conversaId;
    }

    public void setConversaId(Long conversaId) {
        this.conversaId = conversaId;
    }

    public Long getRemetenteId() {
        return remetenteId;
    }

    public void setRemetenteId(Long remetenteId) {
        this.remetenteId = remetenteId;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public LocalDateTime getEnviadaEm() {
        return enviadaEm;
    }

    public void setEnviadaEm(LocalDateTime enviadaEm) {
        this.enviadaEm = enviadaEm;
    }

    public Boolean getLida() {
        return lida;
    }

    public void setLida(Boolean lida) {
        this.lida = lida;
    }

    public Boolean getEditada() {
        return editada;
    }

    public void setEditada(Boolean editada) {
        this.editada = editada;
    }
}
