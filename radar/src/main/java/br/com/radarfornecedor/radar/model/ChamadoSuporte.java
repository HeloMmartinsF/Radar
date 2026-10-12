package br.com.radarfornecedor.radar.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Entidade que representa um chamado de suporte aberto por um usuário.
 * 
 * SEGURANÇA:
 * - Cada usuário só pode visualizar seus próprios chamados
 * - Administradores podem visualizar e gerenciar todos os chamados
 * - observacoesInternas são EXCLUSIVAS dos administradores
 * - Validação de propriedade SEMPRE no backend
 * 
 * FLUXO:
 * 1. Usuário abre chamado → status=ABERTO
 * 2. Admin visualiza → status=EM_ATENDIMENTO
 * 3. Admin responde → status=AGUARDANDO_USUARIO
 * 4. Usuário responde → status=AGUARDANDO_ADMIN
 * 5. Admin resolve → status=RESOLVIDO
 * 6. Admin ou usuário encerra → status=ENCERRADO
 */
@Entity
@Table(
    name = "CHAMADOS_SUPORTE",
    indexes = {
        @Index(name = "idx_chamado_numero", columnList = "NUMERO_CHAMADO"),
        @Index(name = "idx_chamado_usuario", columnList = "USUARIO_ID"),
        @Index(name = "idx_chamado_status", columnList = "STATUS"),
        @Index(name = "idx_chamado_categoria", columnList = "CATEGORIA"),
        @Index(name = "idx_chamado_urgencia", columnList = "URGENCIA"),
        @Index(name = "idx_chamado_criado", columnList = "CRIADO_EM"),
        @Index(name = "idx_chamado_usuario_status", columnList = "USUARIO_ID, STATUS")
    }
)
public class ChamadoSuporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /**
     * Número único do chamado (formato: SUP-2026-00001)
     * Gerado automaticamente na criação
     */
    @NotBlank
    @Column(name = "NUMERO_CHAMADO", unique = true, nullable = false, length = 20)
    private String numeroChamado;

    /**
     * ID do usuário que abriu o chamado
     */
    @NotNull
    @Column(name = "USUARIO_ID", nullable = false)
    private Long usuarioId;

    /**
     * Nome do usuário (cache para exibição, pode ficar desatualizado)
     */
    @Column(name = "USUARIO_NOME", length = 200)
    private String usuarioNome;

    /**
     * Tipo do usuário (FORNECEDOR, CLIENTE, REPRESENTANTE)
     */
    @Column(name = "USUARIO_TIPO", length = 50)
    private String usuarioTipo;

    /**
     * CNPJ/CPF vinculado ao usuário (se houver)
     */
    @Column(name = "CNPJ_CPF", length = 20)
    private String cnpjCpf;

    /**
     * Assunto breve do problema
     */
    @NotBlank
    @Column(name = "ASSUNTO", nullable = false, length = 200)
    private String assunto;

    /**
     * Categoria do atendimento
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORIA", nullable = false, length = 50)
    private CategoriaSuporte categoria;

    /**
     * Descrição detalhada do problema
     */
    @NotBlank
    @Column(name = "DESCRICAO", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    /**
     * Nível de urgência
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "URGENCIA", nullable = false, length = 20)
    private NivelUrgencia urgencia;

    /**
     * Status atual do chamado
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 30)
    private StatusChamado status;

    /**
     * Data/hora de criação do chamado
     */
    @NotNull
    @Column(name = "CRIADO_EM", nullable = false)
    private LocalDateTime criadoEm;

    /**
     * Data/hora da última atualização
     */
    @Column(name = "ATUALIZADO_EM")
    private LocalDateTime atualizadoEm;

    /**
     * Data/hora do encerramento
     */
    @Column(name = "ENCERRADO_EM")
    private LocalDateTime encerradoEm;

    /**
     * ID do admin que está atendendo (opcional)
     */
    @Column(name = "ADMIN_RESPONSAVEL_ID")
    private Long adminResponsavelId;

    /**
     * Nome do admin responsável (cache)
     */
    @Column(name = "ADMIN_RESPONSAVEL_NOME", length = 200)
    private String adminResponsavelNome;

    /**
     * Observações internas dos administradores
     * NUNCA deve ser exposta ao usuário que abriu o chamado
     * Usado para notas internas, decisões administrativas, etc.
     */
    @Column(name = "OBSERVACOES_INTERNAS", columnDefinition = "TEXT")
    private String observacoesInternas;

    /**
     * Informações técnicas capturadas (navegador, SO, IP, etc)
     */
    @Column(name = "INFO_TECNICAS", columnDefinition = "TEXT")
    private String infoTecnicas;

    /**
     * Contador de mensagens no chamado
     */
    @Column(name = "TOTAL_MENSAGENS")
    private Integer totalMensagens = 0;

    /**
     * Indica se há mensagens não lidas pelo admin
     */
    @Column(name = "TEM_NOVAS_MENSAGENS_ADMIN")
    private Boolean temNovasMensagensAdmin = false;

    /**
     * Indica se há mensagens não lidas pelo usuário
     */
    @Column(name = "TEM_NOVAS_MENSAGENS_USUARIO")
    private Boolean temNovasMensagensUsuario = false;

    // Construtores

    public ChamadoSuporte() {
        this.criadoEm = LocalDateTime.now();
        this.status = StatusChamado.ABERTO;
    }

    public ChamadoSuporte(Long usuarioId, String assunto, CategoriaSuporte categoria, String descricao, NivelUrgencia urgencia) {
        this();
        this.usuarioId = usuarioId;
        this.assunto = assunto;
        this.categoria = categoria;
        this.descricao = descricao;
        this.urgencia = urgencia;
    }

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumeroChamado() {
        return numeroChamado;
    }

    public void setNumeroChamado(String numeroChamado) {
        this.numeroChamado = numeroChamado;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNome() {
        return usuarioNome;
    }

    public void setUsuarioNome(String usuarioNome) {
        this.usuarioNome = usuarioNome;
    }

    public String getUsuarioTipo() {
        return usuarioTipo;
    }

    public void setUsuarioTipo(String usuarioTipo) {
        this.usuarioTipo = usuarioTipo;
    }

    public String getCnpjCpf() {
        return cnpjCpf;
    }

    public void setCnpjCpf(String cnpjCpf) {
        this.cnpjCpf = cnpjCpf;
    }

    public String getAssunto() {
        return assunto;
    }

    public void setAssunto(String assunto) {
        this.assunto = assunto;
    }

    public CategoriaSuporte getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaSuporte categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public NivelUrgencia getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(NivelUrgencia urgencia) {
        this.urgencia = urgencia;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
        this.atualizadoEm = LocalDateTime.now();
        
        if (status == StatusChamado.ENCERRADO && this.encerradoEm == null) {
            this.encerradoEm = LocalDateTime.now();
        }
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public LocalDateTime getEncerradoEm() {
        return encerradoEm;
    }

    public void setEncerradoEm(LocalDateTime encerradoEm) {
        this.encerradoEm = encerradoEm;
    }

    public Long getAdminResponsavelId() {
        return adminResponsavelId;
    }

    public void setAdminResponsavelId(Long adminResponsavelId) {
        this.adminResponsavelId = adminResponsavelId;
    }

    public String getAdminResponsavelNome() {
        return adminResponsavelNome;
    }

    public void setAdminResponsavelNome(String adminResponsavelNome) {
        this.adminResponsavelNome = adminResponsavelNome;
    }

    public String getObservacoesInternas() {
        return observacoesInternas;
    }

    public void setObservacoesInternas(String observacoesInternas) {
        this.observacoesInternas = observacoesInternas;
    }

    public String getInfoTecnicas() {
        return infoTecnicas;
    }

    public void setInfoTecnicas(String infoTecnicas) {
        this.infoTecnicas = infoTecnicas;
    }

    public Integer getTotalMensagens() {
        return totalMensagens;
    }

    public void setTotalMensagens(Integer totalMensagens) {
        this.totalMensagens = totalMensagens;
    }

    public Boolean getTemNovasMensagensAdmin() {
        return temNovasMensagensAdmin;
    }

    public void setTemNovasMensagensAdmin(Boolean temNovasMensagensAdmin) {
        this.temNovasMensagensAdmin = temNovasMensagensAdmin;
    }

    public Boolean getTemNovasMensagensUsuario() {
        return temNovasMensagensUsuario;
    }

    public void setTemNovasMensagensUsuario(Boolean temNovasMensagensUsuario) {
        this.temNovasMensagensUsuario = temNovasMensagensUsuario;
    }
}
