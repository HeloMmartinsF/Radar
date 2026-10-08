package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.Cliente;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.model.TipoUsuario;
import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.model.Representante;
import br.com.radarfornecedor.radar.repository.ClienteRepository;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.repository.RepresentanteRepository;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final RepresentanteRepository representanteRepository;

    public ClienteService(ClienteRepository clienteRepository, FornecedorRepository fornecedorRepository, RepresentanteRepository representanteRepository) {
        this.clienteRepository = clienteRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.representanteRepository = representanteRepository;
    }

    public Cliente cadastrar(Cliente cliente) {
        if (cliente.getCpfCnpj() == null) {
            throw new RuntimeException("CPF é obrigatório.");
        }
        String doc = cliente.getCpfCnpj().replaceAll("\\D", "");
        if (doc.length() != 11) {
            throw new RuntimeException("Clientes somente com CPF (11 dígitos). CNPJ não é permitido.");
        }
        cliente.setCpfCnpj(doc);
        cliente.setTipoPessoa("PF");
        return clienteRepository.save(cliente);
    }

    public List<Cliente> listarTodos(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            // ✅ ADMIN, MANUTENCAO, EDICAO: Vê tudo
            if (usuario.getTipo() == TipoUsuario.ADMIN ||
                usuario.getTipo() == TipoUsuario.MANUTENCAO ||
                usuario.getTipo() == TipoUsuario.EDICAO) {
                return clienteRepository.findAll();
            }
            
            // ✅ Comprador não vê clientes
            if (Boolean.TRUE.equals(usuario.getComprador())) {
                return java.util.Collections.emptyList();
            }
            
            // ✅ PADRAO ou RESTRITO com perfil CLIENTE: Vê apenas seu próprio cadastro
            if (Boolean.TRUE.equals(usuario.getCliente())) {
                String cpfOuCnpj = usuario.getCnpjOuCpf();
                if (cpfOuCnpj != null) {
                    String cpfClean = cpfOuCnpj.replaceAll("\\D", "");
                    Optional<Cliente> clienteOpt = clienteRepository.findAll().stream()
                            .filter(c -> c.getCpfCnpj() != null && c.getCpfCnpj().replaceAll("\\D", "").equals(cpfClean))
                            .findFirst();

                    if (clienteOpt.isPresent()) {
                        return List.of(clienteOpt.get());
                    }
                }
                return List.of();
            }
            
            // ✅ PADRAO ou RESTRITO com perfil FORNECEDOR: Vê clientes conforme aceitaCpf
            if (Boolean.TRUE.equals(usuario.getFornecedor())) {
                String cnpjOuCpf = usuario.getCnpjOuCpf();
                if (cnpjOuCpf != null) {
                    String cnpjClean = cnpjOuCpf.replaceAll("\\D", "");
                    Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(cnpjClean);

                    if (fornecedorOpt.isPresent()) {
                        Fornecedor fornecedor = fornecedorOpt.get();
                        if (Boolean.TRUE.equals(fornecedor.getAceitaCpf())) {
                            return clienteRepository.findAll();
                        } else {
                            return clienteRepository.findByTipoPessoa("PJ");
                        }
                    }
                }
                return List.of();
            }
            
            // ✅ PADRAO ou RESTRITO com perfil REPRESENTANTE: Vê clientes do fornecedor vinculado
            if (Boolean.TRUE.equals(usuario.getRepresentante())) {
                String cnpjOuCpf = usuario.getCnpjOuCpf();
                if (cnpjOuCpf != null) {
                    final String cnpjClean = cnpjOuCpf.replaceAll("\\D", "");
                    Optional<Representante> repOpt = representanteRepository.findAll().stream()
                            .filter(r -> r.getCnpj() != null && r.getCnpj().replaceAll("\\D", "").equals(cnpjClean))
                            .findFirst();

                    if (repOpt.isPresent()) {
                        Representante rep = repOpt.get();
                        String cnpjFornecedor = rep.getCnpjFornecedor();
                        if (cnpjFornecedor != null) {
                            Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(cnpjFornecedor.replaceAll("\\D", ""));

                            if (fornecedorOpt.isPresent()) {
                                Fornecedor fornecedor = fornecedorOpt.get();
                                if (Boolean.TRUE.equals(fornecedor.getAceitaCpf())) {
                                    return clienteRepository.findAll();
                                } else {
                                    return clienteRepository.findByTipoPessoa("PJ");
                                }
                            }
                        }
                    }
                }
                return clienteRepository.findByTipoPessoa("PJ");
            }
        }
        return List.of();
    }

    public Optional<Cliente> buscarPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente atualizar(Long id, Cliente dadosNovos, HttpSession session) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + id));

        Usuario usuario = (Usuario) session.getAttribute("usuario");
        
        if (usuario != null) {
            // ✅ ADMIN, MANUTENCAO, EDICAO: Podem editar qualquer cliente
            if (usuario.getTipo() == TipoUsuario.ADMIN ||
                usuario.getTipo() == TipoUsuario.MANUTENCAO ||
                usuario.getTipo() == TipoUsuario.EDICAO) {
                // Sem restrições
            }
            // ✅ RESTRITO: Não pode editar nada
            else if (usuario.getTipo() == TipoUsuario.RESTRITO) {
                throw new RuntimeException("Você não tem permissão para editar cadastros.");
            }
            // ✅ PADRAO com perfil CLIENTE: Pode editar apenas seu próprio cadastro
            else if (Boolean.TRUE.equals(usuario.getCliente())) {
                String cpfOuCnpj = usuario.getCnpjOuCpf();
                if (cpfOuCnpj != null) {
                    String cpfClean = cpfOuCnpj.replaceAll("\\D", "");
                    Optional<Cliente> clienteOpt = clienteRepository.findAll().stream()
                            .filter(c -> c.getCpfCnpj() != null && c.getCpfCnpj().replaceAll("\\D", "").equals(cpfClean))
                            .findFirst();

                    if (clienteOpt.isPresent()) {
                        Cliente cliente = clienteOpt.get();
                        if (!cliente.getId().equals(id)) {
                            throw new RuntimeException("Você só tem permissão para editar o seu próprio cadastro.");
                        }
                        // ✅ PADRAO não pode alterar o STATUS
                        if (usuario.getTipo() == TipoUsuario.PADRAO) {
                            dadosNovos.setStatus(existente.getStatus()); // Manter status original
                        }
                    } else {
                        throw new RuntimeException("Você não tem permissão para editar este cadastro.");
                    }
                } else {
                    throw new RuntimeException("Usuário sem CPF vinculado.");
                }
            } else {
                throw new RuntimeException("Você não tem permissão para editar clientes.");
            }
        }

        existente.setNome(dadosNovos.getNome());
        existente.setStatus(dadosNovos.getStatus());

        // CPF obrigatório — CNPJ não permitido
        if (dadosNovos.getCpfCnpj() != null) {
            String doc = dadosNovos.getCpfCnpj().replaceAll("\\D", "");
            if (doc.length() != 11) {
                throw new RuntimeException("Clientes somente com CPF (11 dígitos). CNPJ não é permitido.");
            }
            existente.setCpfCnpj(doc);
        }
        existente.setTipoPessoa("PF");

        // Endereço e geolocalização
        existente.setCep(dadosNovos.getCep());
        existente.setLogradouro(dadosNovos.getLogradouro());
        existente.setNumero(dadosNovos.getNumero());
        existente.setComplemento(dadosNovos.getComplemento());
        existente.setBairro(dadosNovos.getBairro());
        existente.setCidade(dadosNovos.getCidade());
        existente.setEstado(dadosNovos.getEstado());
        existente.setLatitude(dadosNovos.getLatitude());
        existente.setLongitude(dadosNovos.getLongitude());

        return clienteRepository.save(existente);
    }

    public void excluir(Long id) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente não encontrado com o ID: " + id));
        existente.setStatus("INATIVO");
        clienteRepository.save(existente);
    }
}
