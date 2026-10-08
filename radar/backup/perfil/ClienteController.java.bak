package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.Cliente;
import br.com.radarfornecedor.radar.service.ClienteService;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
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
}
