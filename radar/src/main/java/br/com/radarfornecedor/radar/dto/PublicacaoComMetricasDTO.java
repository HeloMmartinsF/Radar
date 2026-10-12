package br.com.radarfornecedor.radar.dto;

/**
 * DTO para publicação com suas métricas de desempenho
 */
public class PublicacaoComMetricasDTO {

    private Long id;
    private String titulo;
    private String descricao;
    private String primeiraImagem;
    private Long visualizacoes;
    private Long curtidas;
    private Long cliquesContato;
    private Long totalInteracoes; // curtidas + cliquesContato

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getPrimeiraImagem() {
        return primeiraImagem;
    }

    public void setPrimeiraImagem(String primeiraImagem) {
        this.primeiraImagem = primeiraImagem;
    }

    public Long getVisualizacoes() {
        return visualizacoes;
    }

    public void setVisualizacoes(Long visualizacoes) {
        this.visualizacoes = visualizacoes;
    }

    public Long getCurtidas() {
        return curtidas;
    }

    public void setCurtidas(Long curtidas) {
        this.curtidas = curtidas;
    }

    public Long getCliquesContato() {
        return cliquesContato;
    }

    public void setCliquesContato(Long cliquesContato) {
        this.cliquesContato = cliquesContato;
    }

    public Long getTotalInteracoes() {
        return totalInteracoes;
    }

    public void setTotalInteracoes(Long totalInteracoes) {
        this.totalInteracoes = totalInteracoes;
    }
}
