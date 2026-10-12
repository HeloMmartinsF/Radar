package br.com.radarfornecedor.radar.model;

/**
 * Enum para nível de urgência de chamados de suporte.
 * Define a prioridade de atendimento baseada na gravidade do problema.
 */
public enum NivelUrgencia {
    BAIXA("Baixa", "Dúvidas gerais, sugestões, melhorias não urgentes", 3),
    NORMAL("Normal", "Problemas que não impedem uso completo da plataforma", 2),
    ALTA("Alta", "Problemas graves que dificultam muito o uso da plataforma", 1),
    CRITICA("Crítica", "Sistema completamente inacessível, perda de dados, segurança comprometida", 0);

    private final String nome;
    private final String descricao;
    private final int prioridade; // 0 = mais urgente, 3 = menos urgente

    NivelUrgencia(String nome, String descricao, int prioridade) {
        this.nome = nome;
        this.descricao = descricao;
        this.prioridade = prioridade;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getPrioridade() {
        return prioridade;
    }
}
