package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.TipoUsuario;
import br.com.radarfornecedor.radar.model.Usuario;
import br.com.radarfornecedor.radar.service.UsuarioService;
import javax.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    private boolean isNotAdmin(HttpSession session) {
        Usuario logado = (Usuario) session.getAttribute("usuario");
        return logado == null || logado.getTipo() != TipoUsuario.ADMIN;
    }

    @GetMapping
    public ResponseEntity<?> listarTodos(HttpSession session) {
        if (isNotAdmin(session)) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem consultar usuários.");
        }
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // Endpoint temporário para debug - listar fornecedores (sem autenticação)
    @GetMapping("/debug/fornecedores")
    public ResponseEntity<?> listarFornecedoresDebug() {
        List<Usuario> fornecedores = usuarioService.listarTodos().stream()
            .filter(u -> u.getFornecedor() != null && u.getFornecedor())
            .toList();
        
        return ResponseEntity.ok(fornecedores.stream().map(f -> Map.of(
            "id", f.getId(),
            "username", f.getUsername(),
            "tipo", f.getTipo().name(),
            "cnpjOuCpf", f.getCnpjOuCpf() != null ? f.getCnpjOuCpf() : "N/A",
            "ativo", f.getAtivo(),
            "fornecedor", f.getFornecedor()
        )).toList());
    }

    @PostMapping
    public ResponseEntity<?> cadastrar(@RequestBody Usuario novoUsuario, HttpSession session) {
        if (isNotAdmin(session)) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem cadastrar usuários.");
        }

        if (novoUsuario.getUsername() == null || novoUsuario.getUsername().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Nome de usuário é obrigatório.");
        }
        if (novoUsuario.getSenha() == null || novoUsuario.getSenha().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Senha é obrigatória.");
        }
        if (novoUsuario.getTipo() == null) {
            return ResponseEntity.badRequest().body("Tipo de usuário é obrigatório.");
        }

        // Permitir apenas cadastro de MANUTENCAO, EDICAO e ADMIN (conforme requisito)
        TipoUsuario tipo = novoUsuario.getTipo();
        if (tipo != TipoUsuario.MANUTENCAO && tipo != TipoUsuario.EDICAO && tipo != TipoUsuario.ADMIN) {
            return ResponseEntity.badRequest().body("Tipo de usuário inválido para cadastro por administrador.");
        }

        if (usuarioService.buscarPorUsername(novoUsuario.getUsername().trim()).isPresent()) {
            return ResponseEntity.badRequest().body("Este nome de usuário já está cadastrado.");
        }

        novoUsuario.setUsername(novoUsuario.getUsername().trim());
        novoUsuario.setAtivo(true); // Todo novo usuário começa ativo

        Usuario salvo = usuarioService.salvar(novoUsuario);
        return ResponseEntity.ok(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Usuario dadosAtualizados, HttpSession session) {
        if (isNotAdmin(session)) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem alterar dados de usuários.");
        }

        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    // Restrição: "Pode inativar usuários exeto o Administrador."
                    // Se o usuário a ser atualizado é um ADMIN e o dado atualizado tenta inativá-lo, rejeitamos.
                    if (usuario.getTipo() == TipoUsuario.ADMIN && dadosAtualizados.getAtivo() != null && !dadosAtualizados.getAtivo()) {
                        return ResponseEntity.badRequest().body("Não é permitido inativar um usuário Administrador.");
                    }

                    if (dadosAtualizados.getUsername() != null && !dadosAtualizados.getUsername().trim().isEmpty()) {
                        String novoUsername = dadosAtualizados.getUsername().trim();
                        // Se mudou o username, checa se já existe
                        if (!novoUsername.equalsIgnoreCase(usuario.getUsername())) {
                            if (usuarioService.buscarPorUsername(novoUsername).isPresent()) {
                                return ResponseEntity.badRequest().body("Este nome de usuário já está em uso.");
                            }
                        }
                        usuario.setUsername(novoUsername);
                    }

                    if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().trim().isEmpty()) {
                        usuario.setSenha(dadosAtualizados.getSenha().trim());
                    }

                    if (dadosAtualizados.getTipo() != null) {
                        usuario.setTipo(dadosAtualizados.getTipo());
                    }

                    if (dadosAtualizados.getAtivo() != null) {
                        usuario.setAtivo(dadosAtualizados.getAtivo());
                    }

                    if (dadosAtualizados.getFornecedor() != null) {
                        usuario.setFornecedor(dadosAtualizados.getFornecedor());
                    }
                    if (dadosAtualizados.getComprador() != null) {
                        usuario.setComprador(dadosAtualizados.getComprador());
                    }
                    if (dadosAtualizados.getRepresentante() != null) {
                        usuario.setRepresentante(dadosAtualizados.getRepresentante());
                    }
                    if (dadosAtualizados.getCliente() != null) {
                        usuario.setCliente(dadosAtualizados.getCliente());
                    }

                    Usuario salvo = usuarioService.salvar(usuario);
                    return ResponseEntity.ok(salvo);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(@PathVariable Long id, HttpSession session) {
        if (isNotAdmin(session)) {
            return ResponseEntity.status(403).body("Acesso negado. Apenas administradores podem excluir usuários.");
        }

        return usuarioService.buscarPorId(id)
                .map(usuario -> {
                    if (usuario.getTipo() == TipoUsuario.ADMIN) {
                        return ResponseEntity.badRequest().body("Não é permitido excluir um usuário Administrador.");
                    }
                    usuarioService.excluir(id);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
