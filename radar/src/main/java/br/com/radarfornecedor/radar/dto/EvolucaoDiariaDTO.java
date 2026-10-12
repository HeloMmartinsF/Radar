package br.com.radarfornecedor.radar.dto;

/**
 * DTO para evolução diária de uma métrica
 */
public class EvolucaoDiariaDTO {

    private String data; // formato: yyyy-MM-dd
    private Long total;

    public EvolucaoDiariaDTO() {
    }

    public EvolucaoDiariaDTO(String data, Long total) {
        this.data = data;
        this.total = total;
    }

    // Getters e Setters
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }
}
