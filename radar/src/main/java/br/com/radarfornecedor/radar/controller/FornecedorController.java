package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Fornecedor;
import br.com.radarfornecedor.radar.service.FornecedorService;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorService fornecedorService;

    public FornecedorController(FornecedorService fornecedorService) {
        this.fornecedorService = fornecedorService;
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

    /**
     * PUT /api/fornecedores/{id}/verificar
     * Marca/desmarca um fornecedor como verificado (selo de empresa verificada)
     * Apenas ADMIN pode executar esta operação
     * 
     * Body: { "verificado": true/false }
     * 
     * @param id ID do fornecedor
     * @param request Dados da verificação
     * @param session Sessão HTTP
     * @return Fornecedor atualizado
     */
    @PutMapping("/{id}/verificar")
    public ResponseEntity<?> verificarFornecedor(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request,
            javax.servlet.http.HttpSession session) {
        try {
            // Validação 1: Usuário autenticado?
            br.com.radarfornecedor.radar.model.Usuario usuario = 
                (br.com.radarfornecedor.radar.model.Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("erro", "Usuário não autenticado"));
            }

            // Validação 2: Usuário é ADMIN?
            if (usuario.getTipo() != br.com.radarfornecedor.radar.model.TipoUsuario.ADMIN) {
                return ResponseEntity.status(403)
                        .body(Map.of("erro", "Apenas administradores podem verificar fornecedores"));
            }

            // Validação 3: Fornecedor existe?
            java.util.Optional<Fornecedor> fornecedorOpt = fornecedorService.buscarPorId(id);
            if (fornecedorOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("erro", "Fornecedor não encontrado"));
            }

            Fornecedor fornecedor = fornecedorOpt.get();
            Boolean verificado = request.getOrDefault("verificado", true);

            // Atualizar status de verificação
            fornecedor.setVerificado(verificado);
            
            if (verificado) {
                fornecedor.setVerificadoEm(java.time.LocalDateTime.now());
                fornecedor.setVerificadoPor(usuario.getId());
            } else {
                // Se desmarcar verificação, limpar dados
                fornecedor.setVerificadoEm(null);
                fornecedor.setVerificadoPor(null);
            }

            // Salvar (usar o service para manter consistência)
            Fornecedor atualizado = fornecedorService.atualizar(id, fornecedor, session);

            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "mensagem", verificado ? "Fornecedor verificado com sucesso!" : "Verificação removida",
                    "fornecedor", atualizado
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao verificar fornecedor: " + e.getMessage()));
        }
    }

    /**
     * GET /api/fornecedores/{id}/status-verificacao
     * Retorna o status de verificação de um fornecedor
     * 
     * @param id ID do fornecedor
     * @return Status de verificação
     */
    @GetMapping("/{id}/status-verificacao")
    public ResponseEntity<?> obterStatusVerificacao(@PathVariable Long id) {
        try {
            java.util.Optional<Fornecedor> fornecedorOpt = fornecedorService.buscarPorId(id);
            if (fornecedorOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("erro", "Fornecedor não encontrado"));
            }

            Fornecedor fornecedor = fornecedorOpt.get();
            
            Map<String, Object> status = new java.util.HashMap<>();
            status.put("verificado", fornecedor.getVerificado() != null && fornecedor.getVerificado());
            status.put("verificadoEm", fornecedor.getVerificadoEm());
            status.put("verificadoPor", fornecedor.getVerificadoPor());

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("erro", "Erro ao obter status: " + e.getMessage()));
        }
    }
}
