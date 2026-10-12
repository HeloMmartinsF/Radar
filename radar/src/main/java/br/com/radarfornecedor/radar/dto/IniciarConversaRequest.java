package br.com.radarfornecedor.radar.dto;

import br.com.radarfornecedor.radar.model.ContextoConversa;

/**
 * DTO para request de iniciar uma nova conversa.
 * Contém os dados necessários para criar uma conversa comercial.
 */
public class IniciarConversaRequest {

    private Long destinatarioId;
    private Long fornecedorId;
    private ContextoConversa contexto;
    private Long contextoReferencia; // ID do produto, orçamento, etc

    public IniciarConversaRequest() {}

    public Long getDestinatarioId() {
        return destinatarioId;
    }

    public void setDestinatarioId(Long destinatarioId) {
        this.destinatarioId = destinatarioId;
    }

    public Long getFornecedorId() {
        return fornecedorId;
    }

    public void setFornecedorId(Long fornecedorId) {
        this.fornecedorId = fornecedorId;
    }

    public ContextoConversa getContexto() {
        return contexto;
    }

    public void setContexto(ContextoConversa contexto) {
        this.contexto = contexto;
    }

    public Long getContextoReferencia() {
        return contextoReferencia;
    }

    public void setContextoReferencia(Long contextoReferencia) {
        this.contextoReferencia = contextoReferencia;
    }
}
