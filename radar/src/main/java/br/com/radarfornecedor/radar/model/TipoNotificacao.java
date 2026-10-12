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
    SISTEMA,
    
    /**
     * Alerta de preço atingido - preço do produto chegou ao valor-alvo definido
     */
    ALERTA_PRECO_ATINGIDO,
    
    /**
     * Redução de preço detectada em produto favoritado
     */
    REDUCAO_PRECO_DETECTADA,
    
    /**
     * Nova oferta/promoção de fornecedor favorito
     */
    NOVA_OFERTA_FORNECEDOR_FAVORITO
}

