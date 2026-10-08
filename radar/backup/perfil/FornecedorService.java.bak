package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.model.Representante;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.repository.RepresentanteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;
    private final RepresentanteRepository representanteRepository;

    public FornecedorService(FornecedorRepository fornecedorRepository, RepresentanteRepository representanteRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.representanteRepository = representanteRepository;
    }

    public Fornecedor cadastrar(Fornecedor fornecedor) {
        Optional<Fornecedor> existente = fornecedorRepository.findByCnpj(fornecedor.getCnpj());
        if (existente.isPresent()) {
            throw new RuntimeException("Já existe um fornecedor cadastrado com este CNPJ.");
        }

        if (fornecedor.getStatus() == null) {
            fornecedor.setStatus("EM_ANALISE");
        }
        if (fornecedor.getPontuacaoRisco() == null) {
            fornecedor.setPontuacaoRisco(0.0);
        }

        return fornecedorRepository.save(fornecedor);
    }

    public List<Fornecedor> listarTodos() {
        return fornecedorRepository.findAll();
    }

    public List<Fornecedor> listarTodos(javax.servlet.http.HttpSession session) {
        br.com.radarfornecedor.radar.model.Usuario usuario = (br.com.radarfornecedor.radar.model.Usuario) session.getAttribute("usuario");
        if (usuario != null) {
            // ✅ ADMIN, MANUTENCAO, EDICAO: Vê tudo
            if (usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.ADMIN ||
                usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.MANUTENCAO ||
                usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.EDICAO) {
                return fornecedorRepository.findAll();
            }
            
            // ✅ PADRAO ou RESTRITO com perfil FORNECEDOR: Vê apenas sua própria empresa
            if (Boolean.TRUE.equals(usuario.getFornecedor())) {
                String cnpjOuCpf = usuario.getCnpjOuCpf();
                if (cnpjOuCpf != null) {
                    String cnpjClean = cnpjOuCpf.replaceAll("\\D", "");
                    Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(cnpjClean);
                    
                    if (fornecedorOpt.isPresent()) {
                        return List.of(fornecedorOpt.get());
                    }
                }
                return List.of();
            }
            
            // ✅ PADRAO ou RESTRITO com perfil REPRESENTANTE: Vê fornecedor vinculado
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
                                return List.of(fornecedorOpt.get());
                            }
                        }
                    }
                }
                return List.of();
            }
            
            // ✅ PADRAO ou RESTRITO com perfil CLIENTE: Vê fornecedores que aceitam CPF
            if (Boolean.TRUE.equals(usuario.getCliente())) {
                return fornecedorRepository.findAll().stream()
                        .filter(f -> Boolean.TRUE.equals(f.getAceitaCpf()))
                        .collect(Collectors.toList());
            }
        }
        // Sem sessão: não vê nada
        return List.of();
    }

    public Optional<Fornecedor> buscarPorId(Long id) {
        return fornecedorRepository.findById(id);
    }

    public Optional<Fornecedor> buscarPorCnpj(String cnpj) {
        return fornecedorRepository.findByCnpj(cnpj);
    }

    public Fornecedor atualizar(Long id, Fornecedor dadosNovos, javax.servlet.http.HttpSession session) {
        Fornecedor existente = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado com o ID: " + id));

        br.com.radarfornecedor.radar.model.Usuario usuario = (br.com.radarfornecedor.radar.model.Usuario) session.getAttribute("usuario");
        
        if (usuario != null) {
            // ✅ ADMIN, MANUTENCAO, EDICAO: Podem editar qualquer fornecedor
            if (usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.ADMIN ||
                usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.MANUTENCAO ||
                usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.EDICAO) {
                // Sem restrições
            }
            // ✅ RESTRITO: Não pode editar nada
            else if (usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.RESTRITO) {
                throw new RuntimeException("Você não tem permissão para editar cadastros.");
            }
            // ✅ PADRAO com perfil FORNECEDOR: Pode editar apenas seu próprio cadastro
            else if (Boolean.TRUE.equals(usuario.getFornecedor())) {
                String cnpjOuCpf = usuario.getCnpjOuCpf();
                if (cnpjOuCpf != null) {
                    String cnpjClean = cnpjOuCpf.replaceAll("\\D", "");
                    Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(cnpjClean);

                    if (fornecedorOpt.isPresent()) {
                        Fornecedor fornecedor = fornecedorOpt.get();
                        if (!fornecedor.getId().equals(id)) {
                            throw new RuntimeException("Você só tem permissão para editar o seu próprio cadastro.");
                        }
                        // ✅ PADRAO não pode alterar o STATUS
                        if (usuario.getTipo() == br.com.radarfornecedor.radar.model.TipoUsuario.PADRAO) {
                            dadosNovos.setStatus(existente.getStatus()); // Manter status original
                        }
                    } else {
                        throw new RuntimeException("Você não tem permissão para editar este cadastro.");
                    }
                } else {
                    throw new RuntimeException("Usuário sem CNPJ vinculado.");
                }
            } else {
                throw new RuntimeException("Você não tem permissão para editar fornecedores.");
            }
        }

        Optional<Fornecedor> comMesmoCnpj = fornecedorRepository.findByCnpj(dadosNovos.getCnpj());
        if (comMesmoCnpj.isPresent() && !comMesmoCnpj.get().getId().equals(id)) {
            throw new RuntimeException("Já existe outro fornecedor cadastrado com este CNPJ.");
        }

        existente.setNome(dadosNovos.getNome());
        existente.setCnpj(dadosNovos.getCnpj());
        existente.setStatus(dadosNovos.getStatus());
        existente.setPontuacaoRisco(dadosNovos.getPontuacaoRisco());
        
        // Atualizar campos de endereço
        existente.setCep(dadosNovos.getCep());
        existente.setLogradouro(dadosNovos.getLogradouro());
        existente.setNumero(dadosNovos.getNumero());
        existente.setComplemento(dadosNovos.getComplemento());
        existente.setBairro(dadosNovos.getBairro());
        existente.setCidade(dadosNovos.getCidade());
        existente.setEstado(dadosNovos.getEstado());
        existente.setLatitude(dadosNovos.getLatitude());
        existente.setLongitude(dadosNovos.getLongitude());
        existente.setCodCidade(dadosNovos.getCodCidade());
        
        // Atualizar categoria e atividade
        if (dadosNovos.getCategoria() != null) {
            existente.setCategoria(dadosNovos.getCategoria());
        }
        if (dadosNovos.getAtividade() != null) {
            existente.setAtividade(dadosNovos.getAtividade());
        }

        return fornecedorRepository.save(existente);
    }

    public void excluir(Long id) {
        Fornecedor existente = fornecedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fornecedor não encontrado com o ID: " + id));
        existente.setStatus("INATIVO");
        fornecedorRepository.save(existente);
    }
}
