package br.com.radarfornecedor.radar.model;

/**
 * Enum para status de chamados de suporte.
 * Define os possíveis estados de um chamado ao longo do ciclo de atendimento.
 */
public enum StatusChamado {
    ABERTO("Aberto", "Chamado criado e aguardando início do atendimento"),
    EM_ATENDIMENTO("Em atendimento", "Administrador está analisando o chamado"),
    AGUARDANDO_USUARIO("Aguardando resposta do usuário", "Administrador respondeu e aguarda retorno do usuário"),
    AGUARDANDO_ADMIN("Aguardando resposta do suporte", "Usuário respondeu e aguarda retorno do administrador"),
    RESOLVIDO("Resolvido", "Problema foi solucionado mas chamado ainda pode ser reaberto"),
    ENCERRADO("Encerrado", "Chamado finalizado e arquivado");

    private final String nome;
    private final String descricao;

    StatusChamado(String nome, String descricao) {
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
