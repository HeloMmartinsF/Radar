package br.com.radarfornecedor.radar.model;

/**
 * Enum para categorias de atendimento da Central de Suporte.
 * Define os tipos de problemas que podem ser reportados pelos usuários.
 */
public enum CategoriaSuporte {
    ACESSO_CONTA("Acesso à conta", "Problemas de login, senha esquecida, bloqueio de conta"),
    CADASTRO("Cadastro", "Dificuldades no cadastro, aprovação pendente, alteração de dados"),
    PRODUTOS("Produtos", "Problemas com cadastro, edição ou visualização de produtos"),
    PRECOS("Preços", "Dúvidas sobre precificação, alertas de preço, histórico"),
    PERFIL("Perfil", "Alteração de perfil, foto, informações comerciais"),
    MENSAGENS("Mensagens", "Problemas com chat, mensagens não recebidas"),
    ORCAMENTOS("Orçamentos", "Dificuldades com orçamentos, negociações"),
    DENUNCIAS("Denúncias", "Reportar conteúdo inadequado, fraude, violação de termos"),
    ERRO_TECNICO("Erro no site", "Erros de sistema, páginas quebradas, funcionalidades não funcionam"),
    NOTIFICACOES("Notificações", "Problemas com notificações, alertas"),
    FAVORITOS_LISTAS("Favoritos e Listas", "Problemas com favoritos ou listas de compras"),
    BUSCA_FILTROS("Busca e Filtros", "Dificuldades na busca inteligente ou filtros"),
    PAGAMENTOS("Pagamentos", "Questões sobre métodos de pagamento, cobranças"),
    OUTROS("Outros", "Assuntos diversos não listados nas categorias acima");

    private final String nome;
    private final String descricao;

    CategoriaSuporte(String nome, String descricao) {
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
