package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * PerfilSocialService - Monta o perfil completo a partir de dados existentes
 * 
 * NÃO DUPLICA DADOS! Busca informações de:
 * - PerfilSocial (foto, biografia, privacidade)
 * - Usuario (username, tipo, flags)
 * - Fornecedor (nome empresa, cnpj, endereço, email, telefone)
 * - Cliente (nome, cpf/cnpj)
 * - Produto (produtos cadastrados pelo fornecedor)
 * - Publicacao (posts do feed)
 */
@Service
public class PerfilSocialService {

    private final PerfilSocialRepository perfilSocialRepository;
    private final UsuarioRepository usuarioRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final PublicacaoRepository publicacaoRepository;

    public PerfilSocialService(
            PerfilSocialRepository perfilSocialRepository,
            UsuarioRepository usuarioRepository,
            FornecedorRepository fornecedorRepository,
            ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository,
            PublicacaoRepository publicacaoRepository) {
        this.perfilSocialRepository = perfilSocialRepository;
        this.usuarioRepository = usuarioRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.publicacaoRepository = publicacaoRepository;
    }

    /**
     * Monta o perfil completo do usuário autenticado
     * Busca dados de múltiplas tabelas SEM duplicar informações
     */
    @Transactional(readOnly = true)
    public Map<String, Object> montarPerfilCompleto(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Map<String, Object> perfilCompleto = new HashMap<>();

        // 1. Dados do PerfilSocial (foto, biografia, privacidade)
        PerfilSocial perfilSocial = perfilSocialRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPerfilPadrao(usuario));

        perfilCompleto.put("perfilSocial", montarDadosPerfilSocial(perfilSocial));

        // 2. Dados do Usuario (username, tipo, flags)
        perfilCompleto.put("usuario", montarDadosUsuario(usuario));

        // 3. Dados específicos por tipo de usuário
        if (usuario.getFornecedor() != null && usuario.getFornecedor()) {
            Map<String, Object> dadosFornecedor = buscarDadosFornecedor(usuario.getCnpjOuCpf());
            perfilCompleto.put("fornecedor", dadosFornecedor);

            // Produtos cadastrados (NÃO cria cópia, apenas referencia)
            Long fornecedorId = (Long) dadosFornecedor.get("id");
            if (fornecedorId != null) {
                perfilCompleto.put("produtos", buscarProdutosFornecedor(fornecedorId));
                perfilCompleto.put("publicacoes", buscarPublicacoesFornecedor(fornecedorId));
            }
        } else if (usuario.getCliente() != null && usuario.getCliente()) {
            perfilCompleto.put("cliente", buscarDadosCliente(usuario.getCnpjOuCpf()));
        }

        // 4. Estatísticas
        perfilCompleto.put("estatisticas", calcularEstatisticas(usuario));

        return perfilCompleto;
    }

    /**
     * Busca perfil de outro usuário (com controle de acesso)
     * - Administrador: vê qualquer perfil completo
     * - Usuário comum: vê apenas perfis públicos
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buscarPerfilDeOutroUsuario(Long usuarioId, String usernameVisitante, String tipoVisitante) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PerfilSocial perfil = perfilSocialRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPerfilPadrao(usuario));

        // Administrador vê tudo
        if ("ADMIN".equals(tipoVisitante)) {
            return montarPerfilCompleto(usuario.getUsername());
        }

        // Se perfil é privado e não é o próprio usuário, retorna dados limitados
        if (!perfil.getPerfilPublico() && !usuario.getUsername().equals(usernameVisitante)) {
            return montarPerfilPrivado(usuario);
        }

        // Retorna perfil completo público
        return montarPerfilCompleto(usuario.getUsername());
    }

    /**
     * Lista todos os perfis (apenas para administradores)
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarTodosPerfis() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        
        return usuarios.stream()
                .map(usuario -> {
                    Map<String, Object> resumo = new HashMap<>();
                    resumo.put("id", usuario.getId());
                    resumo.put("username", usuario.getUsername());
                    resumo.put("tipo", usuario.getTipo().name());
                    
                    // Buscar perfil social
                    PerfilSocial perfil = perfilSocialRepository.findByUsuario(usuario).orElse(null);
                    if (perfil != null) {
                        resumo.put("fotoPerfil", perfil.getFotoPerfil());
                        resumo.put("perfilPublico", perfil.getPerfilPublico());
                    }
                    
                    // Nome de exibição
                    String nomeExibicao = usuario.getUsername();
                    if (usuario.getFornecedor() != null && usuario.getFornecedor()) {
                        fornecedorRepository.findByCnpj(usuario.getCnpjOuCpf()).ifPresent(f -> {
                            resumo.put("nomeExibicao", f.getNome());
                        });
                    } else if (usuario.getCliente() != null && usuario.getCliente()) {
                        clienteRepository.findByCpfCnpj(usuario.getCnpjOuCpf()).ifPresent(c -> {
                            resumo.put("nomeExibicao", c.getNome());
                        });
                    }
                    
                    if (!resumo.containsKey("nomeExibicao")) {
                        resumo.put("nomeExibicao", nomeExibicao);
                    }
                    
                    return resumo;
                })
                .collect(Collectors.toList());
    }

    /**
     * Atualiza perfil de qualquer usuário por ID (para administradores)
     */
    @Transactional
    public PerfilSocial atualizarPerfilPorUsuarioId(Long usuarioId, Map<String, Object> dados) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PerfilSocial perfil = perfilSocialRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPerfilPadrao(usuario));

        // Atualiza campos
        if (dados.containsKey("fotoPerfil")) {
            perfil.setFotoPerfil((String) dados.get("fotoPerfil"));
        }
        if (dados.containsKey("biografia")) {
            perfil.setBiografia((String) dados.get("biografia"));
        }
        if (dados.containsKey("perfilPublico")) {
            perfil.setPerfilPublico((Boolean) dados.get("perfilPublico"));
        }
        if (dados.containsKey("permiteContato")) {
            perfil.setPermiteContato((Boolean) dados.get("permiteContato"));
        }
        if (dados.containsKey("exibeEmail")) {
            perfil.setExibeEmail((Boolean) dados.get("exibeEmail"));
        }
        if (dados.containsKey("exibeTelefone")) {
            perfil.setExibeTelefone((Boolean) dados.get("exibeTelefone"));
        }

        return perfilSocialRepository.save(perfil);
    }

    /**
     * Busca perfil público de outro usuário (método legado, mantido para compatibilidade)
     */
    @Transactional(readOnly = true)
    public Map<String, Object> buscarPerfilPublico(Long usuarioId, String usernameVisitante) {
        return buscarPerfilDeOutroUsuario(usuarioId, usernameVisitante, "USER");
    }

    /**
     * Atualiza dados do perfil social (foto, biografia, privacidade)
     */
    @Transactional
    public PerfilSocial atualizarPerfilSocial(String username, Map<String, Object> dados) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PerfilSocial perfil = perfilSocialRepository.findByUsuario(usuario)
                .orElseGet(() -> criarPerfilPadrao(usuario));

        // Atualiza apenas campos do PerfilSocial
        if (dados.containsKey("fotoPerfil")) {
            perfil.setFotoPerfil((String) dados.get("fotoPerfil"));
        }
        if (dados.containsKey("biografia")) {
            perfil.setBiografia((String) dados.get("biografia"));
        }
        if (dados.containsKey("perfilPublico")) {
            perfil.setPerfilPublico((Boolean) dados.get("perfilPublico"));
        }
        if (dados.containsKey("permiteContato")) {
            perfil.setPermiteContato((Boolean) dados.get("permiteContato"));
        }
        if (dados.containsKey("exibeEmail")) {
            perfil.setExibeEmail((Boolean) dados.get("exibeEmail"));
        }
        if (dados.containsKey("exibeTelefone")) {
            perfil.setExibeTelefone((Boolean) dados.get("exibeTelefone"));
        }

        return perfilSocialRepository.save(perfil);
    }

    // ============ MÉTODOS PRIVADOS DE MONTAGEM ============

    private PerfilSocial criarPerfilPadrao(Usuario usuario) {
        PerfilSocial perfil = new PerfilSocial();
        perfil.setUsuario(usuario);
        perfil.setPerfilPublico(true);
        perfil.setPermiteContato(true);
        return perfilSocialRepository.save(perfil);
    }

    private Map<String, Object> montarDadosPerfilSocial(PerfilSocial perfil) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("id", perfil.getId());
        dados.put("fotoPerfil", perfil.getFotoPerfil());
        dados.put("biografia", perfil.getBiografia());
        dados.put("perfilPublico", perfil.getPerfilPublico());
        dados.put("permiteContato", perfil.getPermiteContato());
        dados.put("exibeEmail", perfil.getExibeEmail());
        dados.put("exibeTelefone", perfil.getExibeTelefone());
        dados.put("criadoEm", perfil.getCriadoEm());
        return dados;
    }

    private Map<String, Object> montarDadosUsuario(Usuario usuario) {
        Map<String, Object> dados = new HashMap<>();
        dados.put("id", usuario.getId());
        dados.put("username", usuario.getUsername());
        dados.put("tipo", usuario.getTipo().name());
        dados.put("isFornecedor", usuario.getFornecedor());
        dados.put("isCliente", usuario.getCliente());
        dados.put("isRepresentante", usuario.getRepresentante());
        dados.put("isComprador", usuario.getComprador());
        return dados;
    }

    private Map<String, Object> buscarDadosFornecedor(String cnpj) {
        Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(cnpj);
        if (!fornecedorOpt.isPresent()) {
            return new HashMap<>();
        }

        Fornecedor fornecedor = fornecedorOpt.get();
        Map<String, Object> dados = new HashMap<>();
        dados.put("id", fornecedor.getId());
        dados.put("nomeEmpresa", fornecedor.getNome());
        dados.put("cnpj", fornecedor.getCnpj());
        dados.put("email", fornecedor.getEmail());
        dados.put("telefone", fornecedor.getTelefone());
        dados.put("cidade", fornecedor.getCidade());
        dados.put("estado", fornecedor.getEstado());
        dados.put("logotipo", fornecedor.getFoto()); // Foto da empresa (diferente de foto do perfil)
        dados.put("prazoEntregaDias", fornecedor.getPrazoEntregaDias());
        
        if (fornecedor.getCategoria() != null) {
            dados.put("categoria", fornecedor.getCategoria().getNome());
        }
        if (fornecedor.getAtividade() != null) {
            dados.put("atividade", fornecedor.getAtividade().getDescricao());
        }

        return dados;
    }

    private Map<String, Object> buscarDadosCliente(String cpfCnpj) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCpfCnpj(cpfCnpj);
        if (!clienteOpt.isPresent()) {
            return new HashMap<>();
        }

        Cliente cliente = clienteOpt.get();
        Map<String, Object> dados = new HashMap<>();
        dados.put("id", cliente.getId());
        dados.put("nome", cliente.getNome());
        dados.put("cpfCnpj", cliente.getCpfCnpj());
        dados.put("tipoPessoa", cliente.getTipoPessoa());
        dados.put("cidade", cliente.getCidade());
        dados.put("estado", cliente.getEstado());

        return dados;
    }

    private List<Map<String, Object>> buscarProdutosFornecedor(Long fornecedorId) {
        // Busca produtos EXISTENTES (não cria cópia)
        List<Produto> produtos = produtoRepository.findByFornecedorId(fornecedorId);
        
        return produtos.stream()
                .limit(20) // Limita a 20 produtos no perfil
                .map(p -> {
                    Map<String, Object> prod = new HashMap<>();
                    prod.put("id", p.getId());
                    prod.put("nome", p.getNome());
                    prod.put("descricao", p.getDescricao());
                    prod.put("categoria", p.getCategoria().name());
                    prod.put("preco", p.getPreco());
                    return prod;
                })
                .collect(Collectors.toList());
    }

    private List<Map<String, Object>> buscarPublicacoesFornecedor(Long fornecedorId) {
        // Busca publicações EXISTENTES do feed
        Pageable pageable = PageRequest.of(0, 10); // Últimas 10 publicações
        List<Publicacao> publicacoes = publicacaoRepository.findByFornecedorIdAndAtivaOrderByCriadaEmDesc(fornecedorId, true, pageable);
        
        return publicacoes.stream()
                .map(pub -> {
                    Map<String, Object> p = new HashMap<>();
                    p.put("id", pub.getId());
                    p.put("titulo", pub.getTitulo());
                    p.put("descricao", pub.getDescricao());
                    p.put("preco", pub.getPreco());
                    p.put("precoPromocional", pub.getPrecoPromocional());
                    p.put("imagens", pub.getImagens());
                    p.put("totalCurtidas", pub.getTotalCurtidas());
                    p.put("criadaEm", pub.getCriadaEm());
                    return p;
                })
                .collect(Collectors.toList());
    }

    private Map<String, Object> calcularEstatisticas(Usuario usuario) {
        Map<String, Object> stats = new HashMap<>();

        if (usuario.getFornecedor() != null && usuario.getFornecedor()) {
            Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(usuario.getCnpjOuCpf());
            if (fornecedorOpt.isPresent()) {
                Long fornecedorId = fornecedorOpt.get().getId();
                
                // Total de produtos cadastrados
                long totalProdutos = produtoRepository.countByFornecedorId(fornecedorId);
                stats.put("totalProdutos", totalProdutos);

                // Total de publicações ativas
                long totalPublicacoes = publicacaoRepository.countByFornecedorIdAndAtiva(fornecedorId, true);
                stats.put("totalPublicacoes", totalPublicacoes);

                // Total de curtidas recebidas
                long totalCurtidas = publicacaoRepository.findByFornecedorIdAndAtiva(fornecedorId, true)
                        .stream()
                        .mapToInt(Publicacao::getTotalCurtidas)
                        .sum();
                stats.put("totalCurtidas", totalCurtidas);
            }
        }

        return stats;
    }

    private Map<String, Object> montarPerfilPrivado(Usuario usuario) {
        Map<String, Object> perfilLimitado = new HashMap<>();
        perfilLimitado.put("username", usuario.getUsername());
        perfilLimitado.put("tipo", usuario.getTipo().name());
        perfilLimitado.put("perfilPrivado", true);
        perfilLimitado.put("mensagem", "Este perfil é privado");
        return perfilLimitado;
    }
}
