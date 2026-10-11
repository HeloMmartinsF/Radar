package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "Clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @NotBlank
    @Column(name = "Nome", nullable = false, length = 100)
    private String nome;

    @Column(name = "status")
    private String status;

    @Column(name = "CpfCnpj")
    private String cpfCnpj;

    @Column(name = "TipoPessoa")
    private String tipoPessoa;

    // Endereco e geolocalizacao
    @Column(name = "Logradouro")
    private String logradouro;

    @Column(name = "Numero")
    private String numero;

    @Column(name = "Complemento")
    private String complemento;

    @Column(name = "Bairro")
    private String bairro;

    @Column(name = "Cidade")
    private String cidade;

    @Column(name = "Estado")
    private String estado;

    @Column(name = "Cep")
    private String cep;

    @Column(name = "Latitude")
    private Double latitude;

    @Column(name = "Longitude")
    private Double longitude;

    // Contato e foto
    @Column(name = "Email")
    private String email;

    @Column(name = "Telefone")
    private String telefone;

    @Column(name = "Endereco")
    private String endereco;

    @Column(name = "Foto", columnDefinition = "TEXT")
    private String foto;

    @Column(name = "FotoNome")
    private String fotoNome;

    public Cliente() {}

    public Cliente(Long id, String nome, String status) {
        this.id = id;
        this.nome = nome;
        this.status = status;
    }

    public Cliente(Long id, String nome, String status, String cpfCnpj, String tipoPessoa) {
        this.id = id;
        this.nome = nome;
        this.status = status;
        this.cpfCnpj = cpfCnpj;
        this.tipoPessoa = tipoPessoa;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCpfCnpj() { return cpfCnpj; }
    public void setCpfCnpj(String cpfCnpj) { this.cpfCnpj = cpfCnpj; }

    public String getTipoPessoa() { return tipoPessoa; }
    public void setTipoPessoa(String tipoPessoa) { this.tipoPessoa = tipoPessoa; }

    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }

    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getFoto() { return foto; }
    public void setFoto(String foto) { this.foto = foto; }

    public String getFotoNome() { return fotoNome; }
    public void setFotoNome(String fotoNome) { this.fotoNome = fotoNome; }
}
