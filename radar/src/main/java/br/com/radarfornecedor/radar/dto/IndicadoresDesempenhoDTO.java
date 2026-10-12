package br.com.radarfornecedor.radar.dto;

import java.util.List;

/**
 * DTO com todos os indicadores de desempenho do fornecedor
 */
public class IndicadoresDesempenhoDTO {

    // Totais gerais
    private Long totalPublicacoes;
    private Long totalVisualizacoesPublicacoes;
    private Long totalCurtidas;
    private Long totalCliquesContato;
    private Long totalVisualizacoesProdutos;

    // Top performers
    private List<PublicacaoComMetricasDTO> publicacoesMaisVisualizadas;
    private List<PublicacaoComMetricasDTO> publicacoesMaisInteragidas;
    private List<ProdutoComMetricasDTO> produtosMaisVisualizados;

    // Evolução temporal
    private List<EvolucaoDiariaDTO> evolucaoVisualizacoes;
    private List<EvolucaoDiariaDTO> evolucaoCurtidas;
    private List<EvolucaoDiariaDTO> evolucaoContatos;

    // Getters e Setters
    public Long getTotalPublicacoes() {
        return totalPublicacoes;
    }

    public void setTotalPublicacoes(Long totalPublicacoes) {
        this.totalPublicacoes = totalPublicacoes;
    }

    public Long getTotalVisualizacoesPublicacoes() {
        return totalVisualizacoesPublicacoes;
    }

    public void setTotalVisualizacoesPublicacoes(Long totalVisualizacoesPublicacoes) {
        this.totalVisualizacoesPublicacoes = totalVisualizacoesPublicacoes;
    }

    public Long getTotalCurtidas() {
        return totalCurtidas;
    }

    public void setTotalCurtidas(Long totalCurtidas) {
        this.totalCurtidas = totalCurtidas;
    }

    public Long getTotalCliquesContato() {
        return totalCliquesContato;
    }

    public void setTotalCliquesContato(Long totalCliquesContato) {
        this.totalCliquesContato = totalCliquesContato;
    }

    public Long getTotalVisualizacoesProdutos() {
        return totalVisualizacoesProdutos;
    }

    public void setTotalVisualizacoesProdutos(Long totalVisualizacoesProdutos) {
        this.totalVisualizacoesProdutos = totalVisualizacoesProdutos;
    }

    public List<PublicacaoComMetricasDTO> getPublicacoesMaisVisualizadas() {
        return publicacoesMaisVisualizadas;
    }

    public void setPublicacoesMaisVisualizadas(List<PublicacaoComMetricasDTO> publicacoesMaisVisualizadas) {
        this.publicacoesMaisVisualizadas = publicacoesMaisVisualizadas;
    }

    public List<PublicacaoComMetricasDTO> getPublicacoesMaisInteragidas() {
        return publicacoesMaisInteragidas;
    }

    public void setPublicacoesMaisInteragidas(List<PublicacaoComMetricasDTO> publicacoesMaisInteragidas) {
        this.publicacoesMaisInteragidas = publicacoesMaisInteragidas;
    }

    public List<ProdutoComMetricasDTO> getProdutosMaisVisualizados() {
        return produtosMaisVisualizados;
    }

    public void setProdutosMaisVisualizados(List<ProdutoComMetricasDTO> produtosMaisVisualizados) {
        this.produtosMaisVisualizados = produtosMaisVisualizados;
    }

    public List<EvolucaoDiariaDTO> getEvolucaoVisualizacoes() {
        return evolucaoVisualizacoes;
    }

    public void setEvolucaoVisualizacoes(List<EvolucaoDiariaDTO> evolucaoVisualizacoes) {
        this.evolucaoVisualizacoes = evolucaoVisualizacoes;
    }

    public List<EvolucaoDiariaDTO> getEvolucaoCurtidas() {
        return evolucaoCurtidas;
    }

    public void setEvolucaoCurtidas(List<EvolucaoDiariaDTO> evolucaoCurtidas) {
        this.evolucaoCurtidas = evolucaoCurtidas;
    }

    public List<EvolucaoDiariaDTO> getEvolucaoContatos() {
        return evolucaoContatos;
    }

    public void setEvolucaoContatos(List<EvolucaoDiariaDTO> evolucaoContatos) {
        this.evolucaoContatos = evolucaoContatos;
    }
}
