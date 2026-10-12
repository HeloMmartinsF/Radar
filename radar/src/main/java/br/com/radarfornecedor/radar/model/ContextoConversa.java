package br.com.radarfornecedor.radar.model;

/**
 * Enum que define o contexto de uma conversa comercial.
 * Permite rastrear sobre o que os participantes estão conversando.
 */
public enum ContextoConversa {
    /**
     * Conversa sobre um produto específico
     */
    PRODUTO("Produto", "Conversa sobre um produto específico"),
    
    /**
     * Conversa sobre um orçamento/cotação
     */
    ORCAMENTO("Orçamento", "Conversa sobre orçamento ou cotação"),
    
    /**
     * Conversa geral sem contexto específico
     */
    GERAL("Geral", "Conversa geral sobre negócios");

    private final String nome;
    private final String descricao;

    ContextoConversa(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
}
