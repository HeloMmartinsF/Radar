package br.com.radarfornecedor.radar.model;

/**
 * Tipos de notificações disponíveis no sistema
 */
public enum TipoNotificacao {
    /**
     * Nova publicação de um fornecedor seguido
     */
    NOVA_PUBLICACAO,
    
    /**
     * Nova avaliação recebida pelo fornecedor
     */
    NOVA_AVALIACAO,
    
    /**
     * Novo seguidor para o fornecedor
     */
    NOVO_SEGUIDOR,
    
    /**
     * Comentário em uma publicação
     */
    NOVO_COMENTARIO,
    
    /**
     * Atualização importante do sistema
     */
    SISTEMA
}
