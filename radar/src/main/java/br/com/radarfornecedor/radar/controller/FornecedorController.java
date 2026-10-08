package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.service.FornecedorService;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.model.TipoUsuario;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.repository.ProdutoRepository;
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
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final PerfilFotoRepository perfilFotoRepository;

    public FornecedorController(FornecedorService fornecedorService,
                                 FornecedorRepository fornecedorRepository,
                                 ProdutoRepository produtoRepository,
                                 PerfilFotoRepository perfilFotoRepository) {
        this.fornecedorService    = fornecedorService;
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository    = produtoRepository;
        this.perfilFotoRepository = perfilFotoRepository;
    }

    @PostMapping
    public ResponseEntity<Fornecedor> cadastrar(@Valid @RequestBody Fornecedor fornecedor) {
        Fornecedor novoFornecedor = fornecedorService.cadastrar(fornecedor);
        return new ResponseEntity<>(novoFornecedor, HttpStatus.CREATED);
    }

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(@RequestParam String nome,
                                       @RequestParam String email,
                                       @RequestParam String telefone,
                                       @RequestParam String cnpj,
                                       @RequestParam String logradouro,
                                       @RequestParam String numero,
                                       @RequestParam String complemento,
                                       @RequestParam String bairro,
                                       @RequestParam String cidade,
                                       @RequestParam String estado,
                                       @RequestParam String cep,
                                       @RequestParam Integer prazoEntregaDias,
                                       @RequestParam(required = false) MultipartFile foto) {
        try {
            Fornecedor fornecedor = new Fornecedor();
            fornecedor.setNome(nome);
            fornecedor.setEmail(email);
            fornecedor.setTelefone(telefone);
            fornecedor.setCnpj(cnpj);
            fornecedor.setLogradouro(logradouro);
            fornecedor.setNumero(numero);
            fornecedor.setComplemento(complemento);
            fornecedor.setBairro(bairro);
            fornecedor.setCidade(cidade);
            fornecedor.setEstado(estado);
            fornecedor.setCep(cep);
            fornecedor.setPrazoEntregaDias(prazoEntregaDias);
            fornecedor.setStatus("ATIVO");

            if (foto != null && !foto.isEmpty()) {
                fornecedor.setFoto(java.util.Base64.getEncoder().encodeToString(foto.getBytes()));
                fornecedor.setFotoNome(foto.getOriginalFilename());
            }

            Fornecedor novoFornecedor = fornecedorService.cadastrar(fornecedor);
            return new ResponseEntity<>(novoFornecedor, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao processar foto: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Fornecedor>> listarTodos(javax.servlet.http.HttpSession session) {
        List<Fornecedor> lista = fornecedorService.listarTodos(session);
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Fornecedor> buscarPorId(@PathVariable Long id) {
        return fornecedorService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cnpj/{cnpj}")
    public ResponseEntity<Fornecedor> buscarPorCnpj(@PathVariable String cnpj) {
        return fornecedorService.buscarPorCnpj(cnpj)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Fornecedor> atualizar(@PathVariable Long id, @Valid @RequestBody Fornecedor fornecedor, javax.servlet.http.HttpSession session) {
        try {
            Fornecedor atualizado = fornecedorService.atualizar(id, fornecedor, session);
            return ResponseEntity.ok(atualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        try {
            fornecedorService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    //  ENDPOINTS DE PERFIL — adicionados para funcionalidade de perfil
    // ══════════════════════════════════════════════════════════════════════

    /**
     * GET /api/fornecedores/{id}/perfil
     * Retorna o perfil público de um fornecedor (sem dados sensíveis).
     * Inclui foto principal, bio, site, nome fantasia, localização e galeria de fotos.
     */
    @GetMapping("/{id}/perfil")
    public ResponseEntity<?> obterPerfil(@PathVariable Long id) {
        return fornecedorRepository.findById(id)
                .map(f -> {
                    List<br.com.radarfornecedor.radar.model.PerfilFoto> galeria =
                            perfilFotoRepository.findByDonoPerfilAndDonoIdOrderByOrdemAscCriadaEmAsc("FORNECEDOR", id);

                    // Remove base64 da galeria para economizar payload — frontend usa endpoint /imagem
                    galeria.forEach(foto -> foto.setFoto(null));

                    var produtos = produtoRepository.findAll().stream()
                            .filter(p -> id.equals(p.getFornecedorId()))
                            .map(p -> Map.of(
                                    "id",        p.getId(),
                                    "nome",      p.getNome() != null ? p.getNome() : "",
                                    "descricao", p.getDescricao() != null ? p.getDescricao() : "",
                                    "preco",     p.getPreco() != null ? p.getPreco() : 0,
                                    "categoria", p.getCategoria() != null ? p.getCategoria().getDescricao() : ""
                            ))
                            .collect(java.util.stream.Collectors.toList());

                    Map<String, Object> resposta = new HashMap<>();
                    resposta.put("id", f.getId());
                    resposta.put("nome", f.getNome() != null ? f.getNome() : "");
                    resposta.put("nomeFantasia", f.getNomeFantasia() != null ? f.getNomeFantasia() : "");
                    resposta.put("bio", f.getBio() != null ? f.getBio() : "");
                    resposta.put("site", f.getSite() != null ? f.getSite() : "");
                    resposta.put("cidade", f.getCidade() != null ? f.getCidade() : "");
                    resposta.put("estado", f.getEstado() != null ? f.getEstado() : "");
                    resposta.put("email", f.getEmail() != null ? f.getEmail() : "");
                    resposta.put("telefone", f.getTelefone() != null ? f.getTelefone() : "");
                    resposta.put("temFoto", f.getFoto() != null && !f.getFoto().isEmpty());
                    resposta.put("fotoNome", f.getFotoNome() != null ? f.getFotoNome() : "");
                    resposta.put("categoria", f.getCategoria() != null ? f.getCategoria().getNome() : "");
                    resposta.put("atividade", f.getAtividade() != null ? f.getAtividade().getDescricao() : "");
                    resposta.put("galeria", galeria);
                    resposta.put("produtos", produtos);

                    return ResponseEntity.ok(resposta);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/fornecedores/{id}/foto
     * Retorna a foto principal do fornecedor em base64.
     */
    @GetMapping("/{id}/foto")
    public ResponseEntity<?> obterFotoPrincipal(@PathVariable Long id) {
        return fornecedorRepository.findById(id)
                .filter(f -> f.getFoto() != null && !f.getFoto().isEmpty())
                .map(f -> ResponseEntity.ok(Map.of(
                        "foto",     f.getFoto(),
                        "fotoNome", f.getFotoNome() != null ? f.getFotoNome() : ""
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/fornecedores/{id}/perfil
     * Atualiza campos de perfil: bio, site, nomeFantasia e foto principal.
     * Aceita multipart/form-data OU JSON (sem foto).
     * Segurança: apenas o próprio fornecedor ou admin pode atualizar.
     */
    @PutMapping(value = "/{id}/perfil", consumes = {"multipart/form-data", "application/x-www-form-urlencoded"})
    public ResponseEntity<?> atualizarPerfilMultipart(
            @PathVariable Long id,
            @RequestParam(value = "bio",          required = false) String bio,
            @RequestParam(value = "site",          required = false) String site,
            @RequestParam(value = "nomeFantasia",  required = false) String nomeFantasia,
            @RequestParam(value = "foto",          required = false) MultipartFile foto,
            HttpSession session) {

        ResponseEntity<?> authErro = verificarDonoFornecedor(session, id);
        if (authErro != null) return authErro;

        return fornecedorRepository.findById(id).map(f -> {
            if (bio         != null) f.setBio(bio.length() > 1000 ? bio.substring(0, 1000) : bio);
            if (site        != null) f.setSite(site);
            if (nomeFantasia != null) f.setNomeFantasia(nomeFantasia);
            if (foto != null && !foto.isEmpty()) {
                try {
                    f.setFoto(Base64.getEncoder().encodeToString(foto.getBytes()));
                    f.setFotoNome(foto.getOriginalFilename());
                } catch (IOException e) {
                    // mantém foto anterior em caso de erro
                }
            }
            fornecedorRepository.save(f);
            return ResponseEntity.ok(Map.of("mensagem", "Perfil atualizado com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /api/fornecedores/{id}/perfil/json
     * Atualiza perfil via JSON (sem upload de foto).
     */
    @PutMapping("/{id}/perfil/json")
    public ResponseEntity<?> atualizarPerfilJson(
            @PathVariable Long id,
            @RequestBody Map<String, String> dados,
            HttpSession session) {

        ResponseEntity<?> authErro = verificarDonoFornecedor(session, id);
        if (authErro != null) return authErro;

        return fornecedorRepository.findById(id).map(f -> {
            if (dados.containsKey("bio")) {
                String bio = dados.get("bio");
                f.setBio(bio != null && bio.length() > 1000 ? bio.substring(0, 1000) : bio);
            }
            if (dados.containsKey("site"))         f.setSite(dados.get("site"));
            if (dados.containsKey("nomeFantasia")) f.setNomeFantasia(dados.get("nomeFantasia"));
            fornecedorRepository.save(f);
            return ResponseEntity.ok(Map.of("mensagem", "Perfil atualizado com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/fornecedores/{id}/foto
     * Remove a foto principal do fornecedor.
     */
    @DeleteMapping("/{id}/foto")
    public ResponseEntity<?> removerFotoPrincipal(@PathVariable Long id, HttpSession session) {
        ResponseEntity<?> authErro = verificarDonoFornecedor(session, id);
        if (authErro != null) return authErro;

        return fornecedorRepository.findById(id).map(f -> {
            f.setFoto(null);
            f.setFotoNome(null);
            fornecedorRepository.save(f);
            return ResponseEntity.ok(Map.of("mensagem", "Foto removida com sucesso"));
        }).orElse(ResponseEntity.notFound().build());
    }

    // ── Helper anti-IDOR para Fornecedor ─────────────────────────────────────

    private ResponseEntity<?> verificarDonoFornecedor(HttpSession session, Long fornecedorId) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }
        if (usuario.getTipo() == TipoUsuario.ADMIN
                || usuario.getTipo() == TipoUsuario.MANUTENCAO
                || usuario.getTipo() == TipoUsuario.EDICAO) {
            return null; // admins podem tudo
        }
        if (!Boolean.TRUE.equals(usuario.getFornecedor())) {
            return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão"));
        }
        String cnpjOuCpf = usuario.getCnpjOuCpf();
        if (cnpjOuCpf == null) {
            return ResponseEntity.status(403).body(Map.of("erro", "Usuário sem CNPJ vinculado"));
        }
        String docClean = cnpjOuCpf.replaceAll("\\D", "");
        boolean eDono = fornecedorRepository.findById(fornecedorId)
                .map(f -> f.getCnpj() != null && f.getCnpj().replaceAll("\\D", "").equals(docClean))
                .orElse(false);
        if (!eDono) {
            return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão para alterar este perfil"));
        }
        return null;
    }
}
