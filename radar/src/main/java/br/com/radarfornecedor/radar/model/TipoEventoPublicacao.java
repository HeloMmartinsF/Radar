package br.com.radarfornecedor.radar.model;

/**
 * Tipos de eventos rastreados em publicações
 */
public enum TipoEventoPublicacao {
    /**
     * Visualização da publicação (contada uma vez por usuário/IP por dia)
     */
    VIEW,
    
    /**
     * Clique no botão "Entrar em contato" ou similar
     */
    CONTATO
}
