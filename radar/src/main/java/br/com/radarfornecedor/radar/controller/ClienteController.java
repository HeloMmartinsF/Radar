package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Cliente;
import br.com.radarfornecedor.radar.service.ClienteService;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.model.TipoUsuario;
import br.com.radarfornecedor.radar.repository.ClienteRepository;
import br.com.radarfornecedor.radar.repository.PerfilFotoRepository;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteRepository clienteRepository;
    private final PerfilFotoRepository perfilFotoRepository;

    public ClienteController(ClienteService clienteService,
                              ClienteRepository clienteRepository,
                              PerfilFotoRepository perfilFotoRepository) {
        this.clienteService       = clienteService;
        this.clienteRepository    = clienteRepository;
        this.perfilFotoRepository = perfilFotoRepository;
    }

    @PostMapping
    public ResponseEntity<Cliente> cadastrar(@Valid @RequestBody Cliente cliente) {
        Cliente novo = clienteService.cadastrar(cliente);
        return new ResponseEntity<>(novo, HttpStatus.CREATED);
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestParam String nome,
                                       @RequestParam String email,
                                       @RequestParam String telefone,
                                       @RequestParam String endereco,
                                       @RequestParam String cidade,
                                       @RequestParam String estado,
                                       @RequestParam String cep,
                                       @RequestParam String cpfCnpj,
                                       @RequestParam String tipoPessoa,
                                       @RequestParam(required = false) MultipartFile foto) {
        try {
            Cliente cliente = new Cliente();
            cliente.setNome(nome);
            cliente.setEmail(email);
            cliente.setTelefone(telefone);
            cliente.setEndereco(endereco);
            cliente.setCidade(cidade);
            cliente.setEstado(estado);
            cliente.setCep(cep);
            cliente.setCpfCnpj(cpfCnpj);
            cliente.setTipoPessoa(tipoPessoa);
            cliente.setStatus("ATIVO");

            if (foto != null && !foto.isEmpty()) {
                cliente.setFoto(java.util.Base64.getEncoder().encodeToString(foto.getBytes()));
                cliente.setFotoNome(foto.getOriginalFilename());
            }

            Cliente novo = clienteService.cadastrar(cliente);
            return new ResponseEntity<>(novo, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao processar foto: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos(HttpSession session) {
        List<Cliente> lista = clienteService.listarTodos(session);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Long id) {
        return clienteService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> atualizar(@PathVariable Long id, @Valid @RequestBody Cliente cliente, HttpSession session) {
        try {
            Cliente atualizado = clienteService.atualizar(id, cliente, session);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            clienteService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ENDPOINTS DE PERFIL — adicionados para funcionalidade de perfil
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GET /api/clientes/{id}/perfil
     * Retorna o perfil público de um cliente.
     * Informações sensíveis (CPF completo) são mascaradas.
     */
    @GetMapping("/{id}/perfil")
    public ResponseEntity<?> obterPerfil(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .map(c -> {
                    List<br.com.radarfornecedor.radar.model.PerfilFoto> galeria =
                            perfilFotoRepository.findByDonoPerfilAndDonoIdOrderByOrdemAscCriadaEmAsc("CLIENTE", id);
                    galeria.forEach(foto -> foto.setFoto(null));

                    // Máscara do CPF: exibe apenas os 3 primeiros e os 2 últimos dígitos
                    String cpfMascarado = "";
                    if (c.getCpfCnpj() != null && c.getCpfCnpj().length() >= 5) {
                        String doc = c.getCpfCnpj().replaceAll("\\D", "");
                        cpfMascarado = doc.substring(0, 3) + ".***.***-" + doc.substring(doc.length() - 2);
                    }

                    Map<String, Object> resposta = new HashMap<>();
                    resposta.put("id", c.getId());
                    resposta.put("nome", c.getNome() != null ? c.getNome() : "");
                    resposta.put("bio", c.getBio() != null ? c.getBio() : "");
                    resposta.put("segmento", c.getSegmento() != null ? c.getSegmento() : "");
                    resposta.put("cidade", c.getCidade() != null ? c.getCidade() : "");
                    resposta.put("estado", c.getEstado() != null ? c.getEstado() : "");
                    resposta.put("email", c.getEmail() != null ? c.getEmail() : "");
                    resposta.put("telefone", c.getTelefone() != null ? c.getTelefone() : "");
                    resposta.put("tipoPessoa", c.getTipoPessoa() != null ? c.getTipoPessoa() : "");
                    resposta.put("cpfMascarado", cpfMascarado);
                    resposta.put("temFoto", c.getFoto() != null && !c.getFoto().isEmpty());
                    resposta.put("fotoNome", c.getFotoNome() != null ? c.getFotoNome() : "");
                    resposta.put("galeria", galeria);

                    return ResponseEntity.ok(resposta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/clientes/{id}/foto
     * Retorna a foto principal do cliente em base64.
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<?> obterFotoPrincipal(@PathVariable Long id) {
        return clienteRepository.findById(id)
                .filter(c -> c.getFoto() != null && !c.getFoto().isEmpty())
                .map(c -> ResponseEntity.ok(Map.of(
                        "foto",     c.getFoto(),
                        "fotoNome", c.getFotoNome() != null ? c.getFotoNome() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/clientes/{id}/perfil
     * Atualiza bio, segmento e foto principal do cliente.
     */
    @PutMapping(value = "/{id}/perfil", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public ResponseEntity<?> atualizarPerfilMultipart(
            @PathVariable Long id,
            @RequestParam(value = "bio",      required = false) String bio,
            @RequestParam(value = "segmento", required = false) String segmento,
            @RequestParam(value = "foto",     required = false) MultipartFile foto,
            HttpSession session) {

        ResponseEntity<?> authErro = verificarDonoCliente(session, id);
        if (authErro != null) return authErro;

        return clienteRepository.findById(id).map(c -> {
            if (bio      != null) c.setBio(bio.length() > 1000 ? bio.substring(0, 1000) : bio);
            if (segmento != null) c.setSegmento(segmento);
            if (foto != null && !foto.isEmpty()) {
                try {
                    c.setFoto(Base64.getEncoder().encodeToString(foto.getBytes()));
                    c.setFotoNome(foto.getOriginalFilename());
                } catch (IOException e) {
                    // mantém foto anterior
                }
            }
            clienteRepository.save(c);
            return ResponseEntity.ok(Map.of("mensagem", "Perfil atualizado com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/clientes/{id}/perfil/json
     * Atualiza perfil via JSON (sem upload de foto).
     */
    @PutMapping("/{id}/perfil/json")
    public ResponseEntity<?> atualizarPerfilJson(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados,
            HttpSession session) {

        ResponseEntity<?> authErro = verificarDonoCliente(session, id);
        if (authErro != null) return authErro;

        return clienteRepository.findById(id).map(c -> {
            if (dados.containsKey("bio")) {
                String bio = dados.get("bio");
                c.setBio(bio != null && bio.length() > 1000 ? bio.substring(0, 1000) : bio);
            }
            if (dados.containsKey("segmento")) c.setSegmento(dados.get("segmento"));
            clienteRepository.save(c);
            return ResponseEntity.ok(Map.of("mensagem", "Perfil atualizado com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/clientes/{id}/foto
     * Remove a foto principal do cliente.
     */
    @DeleteMapping("/{id}/foto")
    public ResponseEntity<?> removerFotoPrincipal(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> authErro = verificarDonoCliente(session, id);
        if (authErro != null) return authErro;

        return clienteRepository.findById(id).map(c -> {
            c.setFoto(null);
            c.setFotoNome(null);
            clienteRepository.save(c);
            return ResponseEntity.ok(Map.of("mensagem", "Foto removida com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helper anti-IDOR para Cliente ─────────────────────────────────────────

    private ResponseEntity<?> verificarDonoCliente(HttpSession session, Long clienteId) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }
        if (usuario.getTipo() == TipoUsuario.ADMIN
                || usuario.getTipo() == TipoUsuario.MANUTENCAO
                || usuario.getTipo() == TipoUsuario.EDICAO) {
            return null;
        }
        if (!Boolean.TRUE.equals(usuario.getCliente())) {
            return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão"));
        }
        String cnpjOuCpf = usuario.getCnpjOuCpf();
        if (cnpjOuCpf == null) {
            return ResponseEntity.status(403).body(Map.of("erro", "Usuário sem CPF vinculado"));
        }
        String docClean = cnpjOuCpf.replaceAll("\\D", "");
        boolean eDono = clienteRepository.findById(clienteId)
                .map(c -> c.getCpfCnpj() != null && c.getCpfCnpj().replaceAll("\\D", "").equals(docClean))
                .orElse(false);
        if (!eDono) {
            return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão para alterar este perfil"));
        }
        return null;
    }
}
