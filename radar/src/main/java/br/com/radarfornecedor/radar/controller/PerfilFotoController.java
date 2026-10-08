package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.PerfilFoto;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.model.TipoUsuario;
import br.com.radarfornecedor.radar.repository.FornecedorRepository;
import br.com.radarfornecedor.radar.repository.ClienteRepository;
import br.com.radarfornecedor.radar.repository.PerfilFotoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Controller de Galeria de Fotos do Perfil.
 *
 * Endpoints:
 *   GET    /api/perfil/fotos/{donoPerfil}/{donoId}          — lista fotos da galeria
 *   POST   /api/perfil/fotos/{donoPerfil}/{donoId}          — adiciona foto
 *   DELETE /api/perfil/fotos/{donoPerfil}/{donoId}/{fotoId} — remove foto (apenas dono)
 *
 * donoPerfil: "FORNECEDOR" ou "CLIENTE"
 * donoId:     ID do Fornecedor ou Cliente
 */
@RestController
@RequestMapping("/api/perfil/fotos")
@CrossOrigin(origins = "*")
public class PerfilFotoController {

    private static final int MAX_FOTOS_GALERIA = 20;
    private static final long MAX_FOTO_BYTES   = 5 * 1024 * 1024; // 5 MB

    private final PerfilFotoRepository perfilFotoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ClienteRepository    clienteRepository;

    public PerfilFotoController(PerfilFotoRepository perfilFotoRepository,
                                FornecedorRepository fornecedorRepository,
                                ClienteRepository clienteRepository) {
        this.perfilFotoRepository = perfilFotoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.clienteRepository    = clienteRepository;
    }

    // ── Listar fotos da galeria (público) ────────────────────────────────────

    @GetMapping("/{donoPerfil}/{donoId}")
    public ResponseEntity<List<PerfilFoto>> listar(@PathVariable String donoPerfil,
                                                    @PathVariable Long donoId) {
        String tipo = donoPerfil.toUpperCase();
        if (!tipo.equals("FORNECEDOR") && !tipo.equals("CLIENTE")) {
            return ResponseEntity.badRequest().build();
        }
        List<PerfilFoto> fotos = perfilFotoRepository
                .findByDonoPerfilAndDonoIdOrderByOrdemAscCriadaEmAsc(tipo, donoId);

        // Não expõe o base64 completo na listagem — retorna apenas metadados + miniatura
        // Para exibir no frontend, usa GET /api/perfil/fotos/{donoPerfil}/{donoId}/{fotoId}/imagem
        return ResponseEntity.ok(fotos);
    }

    // ── Buscar imagem individual (retorna base64) ─────────────────────────────

    @GetMapping("/{donoPerfil}/{donoId}/{fotoId}/imagem")
    public ResponseEntity<?> obterImagem(@PathVariable String donoPerfil,
                                          @PathVariable Long donoId,
                                          @PathVariable Long fotoId) {
        return perfilFotoRepository.findById(fotoId)
                .filter(f -> f.getDonoPerfil().equalsIgnoreCase(donoPerfil)
                          && f.getDonoId().equals(donoId))
                .map(f -> ResponseEntity.ok(Map.of(
                        "id",       f.getId(),
                        "foto",     f.getFoto(),
                        "fotoNome", f.getFotoNome() != null ? f.getFotoNome() : "",
                        "legenda",  f.getLegenda()  != null ? f.getLegenda()  : "",
                        "ordem",    f.getOrdem()    != null ? f.getOrdem()    : 0
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Adicionar foto à galeria ──────────────────────────────────────────────

    @PostMapping("/{donoPerfil}/{donoId}")
    public ResponseEntity<?> adicionar(@PathVariable String donoPerfil,
                                        @PathVariable Long donoId,
                                        @RequestParam("foto") MultipartFile foto,
                                        @RequestParam(value = "legenda", required = false) String legenda,
                                        @RequestParam(value = "ordem", required = false, defaultValue = "0") Integer ordem,
                                        HttpSession session) {
        String tipo = donoPerfil.toUpperCase();

        // Validações básicas
        if (!tipo.equals("FORNECEDOR") && !tipo.equals("CLIENTE")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "donoPerfil deve ser FORNECEDOR ou CLIENTE"));
        }
        if (foto == null || foto.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Arquivo de foto é obrigatório"));
        }
        if (foto.getSize() > MAX_FOTO_BYTES) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Foto maior que 5 MB não é permitida"));
        }

        // Segurança: verifica se o usuário logado é dono do perfil
        ResponseEntity<?> authErro = verificarPropriedade(session, tipo, donoId);
        if (authErro != null) return authErro;

        // Limite de fotos
        long qtd = perfilFotoRepository.countByDonoPerfilAndDonoId(tipo, donoId);
        if (qtd >= MAX_FOTOS_GALERIA) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Limite de " + MAX_FOTOS_GALERIA + " fotos na galeria atingido"));
        }

        try {
            String base64 = Base64.getEncoder().encodeToString(foto.getBytes());
            PerfilFoto perfilFoto = new PerfilFoto(tipo, donoId, base64, foto.getOriginalFilename());
            perfilFoto.setLegenda(legenda);
            perfilFoto.setOrdem(ordem != null ? ordem : 0);

            PerfilFoto salva = perfilFotoRepository.save(perfilFoto);
            // Retorna sem o base64 para economizar tráfego
            salva.setFoto(null);
            return ResponseEntity.ok(salva);

        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Erro ao processar foto: " + e.getMessage()));
        }
    }

    // ── Excluir foto da galeria ───────────────────────────────────────────────

    @DeleteMapping("/{donoPerfil}/{donoId}/{fotoId}")
    public ResponseEntity<?> excluir(@PathVariable String donoPerfil,
                                      @PathVariable Long donoId,
                                      @PathVariable Long fotoId,
                                      HttpSession session) {
        String tipo = donoPerfil.toUpperCase();

        // Segurança: só o dono pode excluir suas fotos
        ResponseEntity<?> authErro = verificarPropriedade(session, tipo, donoId);
        if (authErro != null) return authErro;

        return perfilFotoRepository.findById(fotoId)
                .filter(f -> f.getDonoPerfil().equalsIgnoreCase(tipo)
                          && f.getDonoId().equals(donoId))
                .map(f -> {
                    perfilFotoRepository.delete(f);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Atualizar legenda de uma foto ─────────────────────────────────────────

    @PutMapping("/{donoPerfil}/{donoId}/{fotoId}")
    public ResponseEntity<?> atualizarLegenda(@PathVariable String donoPerfil,
                                               @PathVariable Long donoId,
                                               @PathVariable Long fotoId,
                                               @RequestBody Map<String, String> body,
                                               HttpSession session) {
        String tipo = donoPerfil.toUpperCase();

        ResponseEntity<?> authErro = verificarPropriedade(session, tipo, donoId);
        if (authErro != null) return authErro;

        return perfilFotoRepository.findById(fotoId)
                .filter(f -> f.getDonoPerfil().equalsIgnoreCase(tipo)
                          && f.getDonoId().equals(donoId))
                .map(f -> {
                    if (body.containsKey("legenda")) f.setLegenda(body.get("legenda"));
                    if (body.containsKey("ordem"))   f.setOrdem(Integer.parseInt(body.get("ordem")));
                    PerfilFoto atualizada = perfilFotoRepository.save(f);
                    atualizada.setFoto(null); // não retorna base64
                    return ResponseEntity.ok(atualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Verificação de propriedade (anti-IDOR) ────────────────────────────────

    /**
     * Garante que o usuário logado é o dono do perfil que está tentando modificar.
     * ADMIN, MANUTENCAO e EDICAO podem operar em qualquer perfil.
     * Retorna null se autorizado, ou ResponseEntity de erro caso não seja.
     */
    private ResponseEntity<?> verificarPropriedade(HttpSession session,
                                                    String tipo,
                                                    Long donoId) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("erro", "Não autenticado"));
        }

        // Admins podem tudo
        if (usuario.getTipo() == TipoUsuario.ADMIN
                || usuario.getTipo() == TipoUsuario.MANUTENCAO
                || usuario.getTipo() == TipoUsuario.EDICAO) {
            return null; // autorizado
        }

        String cnpjOuCpf = usuario.getCnpjOuCpf();
        if (cnpjOuCpf == null) {
            return ResponseEntity.status(403).body(Map.of("erro", "Usuário sem documento vinculado"));
        }
        String docClean = cnpjOuCpf.replaceAll("\\D", "");

        if ("FORNECEDOR".equals(tipo) && Boolean.TRUE.equals(usuario.getFornecedor())) {
            boolean eDono = fornecedorRepository.findById(donoId)
                    .map(f -> f.getCnpj() != null && f.getCnpj().replaceAll("\\D", "").equals(docClean))
                    .orElse(false);
            if (!eDono) {
                return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão para alterar este perfil"));
            }
            return null; // autorizado
        }

        if ("CLIENTE".equals(tipo) && Boolean.TRUE.equals(usuario.getCliente())) {
            boolean eDono = clienteRepository.findById(donoId)
                    .map(c -> c.getCpfCnpj() != null && c.getCpfCnpj().replaceAll("\\D", "").equals(docClean))
                    .orElse(false);
            if (!eDono) {
                return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão para alterar este perfil"));
            }
            return null; // autorizado
        }

        return ResponseEntity.status(403).body(Map.of("erro", "Sem permissão para este tipo de perfil"));
    }
}
