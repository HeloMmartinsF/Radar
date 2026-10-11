package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.PerfilSocial;
import br.com.radarfornecedor.radar.model.TipoUsuario;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.PerfilSocialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * PerfilSocialController - Endpoints para perfis sociais
 * 
 * Endpoints:
 * - GET /api/perfil/meu - Busca perfil do usuário autenticado
 * - GET /api/perfil/{id} - Busca perfil público de outro usuário
 * - PUT /api/perfil - Atualiza perfil social
 * - POST /api/perfil/foto - Upload de foto de perfil
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilSocialController {

    private final PerfilSocialService perfilSocialService;

    public PerfilSocialController(PerfilSocialService perfilSocialService) {
        this.perfilSocialService = perfilSocialService;
    }

    /**
     * GET /api/perfil/meu
     * Busca o perfil completo do usuário autenticado
     * 
     * Retorna:
     * - PerfilSocial (foto, biografia, privacidade)
     * - Usuario (username, tipo)
     * - Fornecedor/Cliente (dados da empresa/pessoa)
     * - Produtos cadastrados (lista existente)
     * - Publicações do feed (lista existente)
     * - Estatísticas
     */
    @GetMapping("/meu")
    public ResponseEntity<?> buscarMeuPerfil(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Map<String, Object> perfilCompleto = perfilSocialService.montarPerfilCompleto(usuario.getUsername());
            return ResponseEntity.ok(perfilCompleto);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao buscar perfil: " + e.getMessage()));
        }
    }

    /**
     * GET /api/perfil/{usuarioId}
     * Busca o perfil de outro usuário
     * - Administrador pode ver qualquer perfil
     * - Outros usuários veem apenas perfis públicos
     */
    @GetMapping("/{usuarioId}")
    public ResponseEntity<?> buscarPerfilDeOutroUsuario(
            @PathVariable Long usuarioId,
            HttpSession session) {
        try {
            Usuario usuarioLogado = (Usuario) session.getAttribute("usuario");
            String usernameVisitante = usuarioLogado != null ? usuarioLogado.getUsername() : null;
            String tipoUsuario = usuarioLogado != null ? usuarioLogado.getTipo().name() : null;
            
            Map<String, Object> perfil = perfilSocialService.buscarPerfilDeOutroUsuario(
                    usuarioId, usernameVisitante, tipoUsuario);
            return ResponseEntity.ok(perfil);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao buscar perfil: " + e.getMessage()));
        }
    }
    
    /**
     * GET /api/perfil/todos
     * Lista todos os perfis (apenas para administradores)
     */
    @GetMapping("/todos")
    public ResponseEntity<?> listarTodosPerfis(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
                return ResponseEntity.status(403)
                        .body(Map.of("erro", "Acesso negado. Apenas administradores."));
            }

            List<Map<String, Object>> perfis = perfilSocialService.listarTodosPerfis();
            return ResponseEntity.ok(perfis);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao listar perfis: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/perfil
     * Atualiza dados do perfil social
     * 
     * Body:
     * {
     *   "biografia": "Texto da biografia",
     *   "perfilPublico": true/false,
     *   "permiteContato": true/false,
     *   "exibeEmail": true/false,
     *   "exibeTelefone": true/false
     * }
     */
    @PutMapping
    public ResponseEntity<?> atualizarPerfil(
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            PerfilSocial perfilAtualizado = perfilSocialService.atualizarPerfilSocial(usuario.getUsername(), dados);
            
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Perfil atualizado com sucesso",
                    "perfil", perfilAtualizado
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao atualizar perfil: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/perfil/{usuarioId}
     * Atualiza perfil de outro usuário (apenas administradores)
     */
    @PutMapping("/{usuarioId}")
    public ResponseEntity<?> atualizarPerfilDeOutroUsuario(
            @PathVariable Long usuarioId,
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null || usuario.getTipo() != TipoUsuario.ADMIN) {
                return ResponseEntity.status(403)
                        .body(Map.of("erro", "Acesso negado. Apenas administradores podem editar perfis de outros usuários."));
            }

            PerfilSocial perfilAtualizado = perfilSocialService.atualizarPerfilPorUsuarioId(usuarioId, dados);
            
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Perfil atualizado com sucesso pelo administrador",
                    "perfil", perfilAtualizado
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao atualizar perfil: " + e.getMessage()));
        }
    }

    /**
     * POST /api/perfil/foto
     * Upload de foto de perfil
     * 
     * Salva em src/main/resources/static/uploads/perfis/ e target/classes/static/uploads/perfis/
     */
    @PostMapping("/foto")
    public ResponseEntity<?> uploadFotoPerfil(
            @RequestParam("foto") MultipartFile foto,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            String username = usuario.getUsername();

            // Validar arquivo
            if (foto.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Arquivo de imagem não fornecido"));
            }

            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Arquivo deve ser uma imagem"));
            }

            // Gerar nome único
            String extensao = obterExtensao(foto.getOriginalFilename());
            String nomeArquivo = "perfil_" + username + "_" + UUID.randomUUID().toString() + extensao;

            // Salvar em ambos os locais (src e target)
            String caminhoRelativo = "/uploads/perfis/" + nomeArquivo;
            
            // Diretório src (persistente)
            String pastaUploadSrc = "src/main/resources/static/uploads/perfis";
            salvarArquivo(foto, pastaUploadSrc, nomeArquivo);

            // Diretório target (runtime)
            String pastaUploadTarget = "target/classes/static/uploads/perfis";
            salvarArquivo(foto, pastaUploadTarget, nomeArquivo);

            // Atualizar perfil social com o caminho da foto
            Map<String, Object> dadosAtualizacao = new HashMap<>();
            dadosAtualizacao.put("fotoPerfil", caminhoRelativo);
            
            perfilSocialService.atualizarPerfilSocial(username, dadosAtualizacao);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Foto de perfil atualizada com sucesso",
                    "caminhoFoto", caminhoRelativo
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao fazer upload da foto: " + e.getMessage()));
        }
    }

    /**
     * DELETE /api/perfil/foto
     * Remove a foto de perfil (volta para foto padrão)
     */
    @DeleteMapping("/foto")
    public ResponseEntity<?> removerFotoPerfil(HttpSession session) {
        try {
            String username = (String) session.getAttribute("username");
            if (username == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            Map<String, Object> dadosAtualizacao = new HashMap<>();
            dadosAtualizacao.put("fotoPerfil", null);
            
            perfilSocialService.atualizarPerfilSocial(username, dadosAtualizacao);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", "Foto de perfil removida com sucesso"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao remover foto: " + e.getMessage()));
        }
    }

    // ============ MÉTODOS AUXILIARES ============

    private void salvarArquivo(MultipartFile arquivo, String pastaDestino, String nomeArquivo) throws IOException {
        File diretorio = new File(pastaDestino);
        if (!diretorio.exists()) {
            diretorio.mkdirs();
        }

        Path caminhoArquivo = Paths.get(pastaDestino, nomeArquivo);
        Files.write(caminhoArquivo, arquivo.getBytes());
    }

    private String obterExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return ".jpg";
        }
        return nomeArquivo.substring(nomeArquivo.lastIndexOf("."));
    }
}
