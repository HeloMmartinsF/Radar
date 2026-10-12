package br.com.radarfornecedor.radar.controller;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller para o Feed Comercial
 * Gerencia publicações de produtos e promoções dos fornecedores
 */
@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final PublicacaoRepository publicacaoRepository;
    private final CurtidaPublicacaoRepository curtidaRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final UsuarioRepository usuarioRepository;
    private final br.com.radarfornecedor.radar.service.NotificacaoService notificacaoService;

    // Diretórios para upload de imagens (src + target para persistência)
    private static final String UPLOAD_DIR_SRC = "src/main/resources/static/uploads/feed/";
    private static final String UPLOAD_DIR_TARGET = "target/classes/static/uploads/feed/";

    public FeedController(
            PublicacaoRepository publicacaoRepository,
            CurtidaPublicacaoRepository curtidaRepository,
            FornecedorRepository fornecedorRepository,
            ProdutoRepository produtoRepository,
            UsuarioRepository usuarioRepository,
            br.com.radarfornecedor.radar.service.NotificacaoService notificacaoService) {
        this.publicacaoRepository = publicacaoRepository;
        this.curtidaRepository = curtidaRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.produtoRepository = produtoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacaoService = notificacaoService;

        // Criar diretórios de upload se não existirem
        criarDiretorioUpload();
    }

    private void criarDiretorioUpload() {
        try {
            // Criar em src (persistência permanente)
            Path uploadPathSrc = Paths.get(UPLOAD_DIR_SRC);
            if (!Files.exists(uploadPathSrc)) {
                Files.createDirectories(uploadPathSrc);
                System.out.println("[FEED] Diretório de upload criado: " + uploadPathSrc.toAbsolutePath());
            }
            
            // Criar em target (runtime do Spring Boot)
            Path uploadPathTarget = Paths.get(UPLOAD_DIR_TARGET);
            if (!Files.exists(uploadPathTarget)) {
                Files.createDirectories(uploadPathTarget);
                System.out.println("[FEED] Diretório de runtime criado: " + uploadPathTarget.toAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("[FEED] Erro ao criar diretórios de upload: " + e.getMessage());
        }
    }

    /**
     * GET /api/feed
     * Lista todas as publicações ativas ordenadas por data (mais recentes primeiro)
     */
    @GetMapping
    public ResponseEntity<?> listarPublicacoes(HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            List<Publicacao> publicacoes = publicacaoRepository.findByAtivaOrderByCriadaEmDesc(true);
            
            // Enriquecer com dados do fornecedor e verificar curtidas
            List<Map<String, Object>> publicacoesDTO = publicacoes.stream()
                    .map(pub -> montarPublicacaoDTO(pub, usuario))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(publicacoesDTO);
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar feed: " + e.getMessage());
        }
    }

    /**
     * GET /api/feed/{id}
     * Buscar uma publicação específica
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPublicacao(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            Optional<Publicacao> publicacaoOpt = publicacaoRepository.findById(id);
            if (!publicacaoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Publicação não encontrada");
            }
            
            Publicacao publicacao = publicacaoOpt.get();
            if (!publicacao.getAtiva()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Publicação não encontrada");
            }
            
            return ResponseEntity.ok(montarPublicacaoDTO(publicacao, usuario));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao buscar publicação: " + e.getMessage());
        }
    }

    /**
     * GET /api/feed/fornecedor/{fornecedorId}
     * Lista publicações de um fornecedor específico
     */
    @GetMapping("/fornecedor/{fornecedorId}")
    public ResponseEntity<?> listarPublicacoesFornecedor(
            @PathVariable Long fornecedorId,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            List<Publicacao> publicacoes = publicacaoRepository
                    .findByFornecedorIdAndAtivaOrderByCriadaEmDesc(fornecedorId, true);
            
            List<Map<String, Object>> publicacoesDTO = publicacoes.stream()
                    .map(pub -> montarPublicacaoDTO(pub, usuario))
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(publicacoesDTO);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao carregar publicações: " + e.getMessage());
        }
    }

    /**
     * POST /api/feed
     * Criar nova publicação (SOMENTE FORNECEDORES)
     */
    @PostMapping
    public ResponseEntity<?> criarPublicacao(
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Você precisa estar autenticado para criar publicações");
            }

            // Verificar se é fornecedor
            if (!Boolean.TRUE.equals(usuario.getFornecedor())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Apenas fornecedores podem criar publicações no feed");
            }

            // Buscar fornecedor pelo CNPJ do usuário
            Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(usuario.getCnpjOuCpf());
            if (!fornecedorOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Fornecedor não encontrado no sistema");
            }

            Fornecedor fornecedor = fornecedorOpt.get();

            // Criar publicação
            Publicacao publicacao = new Publicacao();
            publicacao.setFornecedorId(fornecedor.getId());
            publicacao.setTitulo((String) dados.get("titulo"));
            publicacao.setDescricao((String) dados.get("descricao"));
            
            if (dados.get("preco") != null) {
                publicacao.setPreco(Double.valueOf(dados.get("preco").toString()));
            }
            if (dados.get("precoPromocional") != null) {
                publicacao.setPrecoPromocional(Double.valueOf(dados.get("precoPromocional").toString()));
            }
            if (dados.get("unidadeVenda") != null) {
                publicacao.setUnidadeVenda((String) dados.get("unidadeVenda"));
            }
            if (dados.get("quantidadeMinima") != null) {
                publicacao.setQuantidadeMinima(Integer.valueOf(dados.get("quantidadeMinima").toString()));
            }
            if (dados.get("condicoesEspeciais") != null) {
                publicacao.setCondicoesEspeciais((String) dados.get("condicoesEspeciais"));
            }
            if (dados.get("produtoId") != null) {
                publicacao.setProdutoId(Long.valueOf(dados.get("produtoId").toString()));
            }
            if (dados.get("imagens") != null) {
                publicacao.setImagens((String) dados.get("imagens"));
            }

            Publicacao salva = publicacaoRepository.save(publicacao);
            
            // TODO: NOTIFICAÇÕES - Quando sistema de seguidores for implementado, adicionar aqui:
            // 1. Buscar todos os seguidores do fornecedor
            // 2. Para cada seguidor, criar notificação usando:
            //    notificacaoService.notificarNovaPublicacao(
            //        seguidorUsuarioId, 
            //        salva.getId(), 
            //        fornecedor.getRazaoSocial(), 
            //        salva.getTitulo(), 
            //        fornecedor.getCnpj()
            //    );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(montarPublicacaoDTO(salva, usuario));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao criar publicação: " + e.getMessage());
        }
    }

    /**
     * POST /api/feed/upload
     * Upload de imagens para publicações
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImagem(
            @RequestParam("imagem") MultipartFile arquivo,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado"));
            }

            if (!Boolean.TRUE.equals(usuario.getFornecedor())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("erro", "Apenas fornecedores podem fazer upload de imagens"));
            }

            if (arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Arquivo vazio"));
            }

            // Validar tipo de arquivo
            String contentType = arquivo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Apenas imagens são permitidas"));
            }

            // Gerar nome único para o arquivo
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String nomeOriginal = arquivo.getOriginalFilename();
            String extensao = nomeOriginal != null && nomeOriginal.contains(".") 
                    ? nomeOriginal.substring(nomeOriginal.lastIndexOf(".")) 
                    : ".jpg";
            String nomeArquivo = "feed_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extensao;

            // Salvar arquivo em AMBOS os diretórios (src para persistência, target para runtime)
            Path caminhoSrc = Paths.get(UPLOAD_DIR_SRC + nomeArquivo);
            Path caminhoTarget = Paths.get(UPLOAD_DIR_TARGET + nomeArquivo);
            
            byte[] bytesImagem = arquivo.getBytes();
            Files.write(caminhoSrc, bytesImagem);
            Files.write(caminhoTarget, bytesImagem);

            String urlImagem = "/uploads/feed/" + nomeArquivo;
            
            System.out.println("[FEED] Imagem salva: " + urlImagem);
            System.out.println("[FEED] - SRC: " + caminhoSrc.toAbsolutePath());
            System.out.println("[FEED] - TARGET: " + caminhoTarget.toAbsolutePath());
            
            return ResponseEntity.ok(Map.of(
                    "sucesso", true,
                    "url", urlImagem,
                    "nomeArquivo", nomeArquivo
            ));
            
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao salvar imagem: " + e.getMessage()));
        }
    }

    /**
     * PUT /api/feed/{id}
     * Editar publicação (apenas o próprio fornecedor)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> editarPublicacao(
            @PathVariable Long id,
            @RequestBody Map<String, Object> dados,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Você precisa estar autenticado");
            }

            Optional<Publicacao> publicacaoOpt = publicacaoRepository.findById(id);
            if (!publicacaoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Publicação não encontrada");
            }

            Publicacao publicacao = publicacaoOpt.get();

            // Verificar se o usuário é o dono da publicação
            Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(usuario.getCnpjOuCpf());
            if (!fornecedorOpt.isPresent() || 
                !publicacao.getFornecedorId().equals(fornecedorOpt.get().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Você só pode editar suas próprias publicações");
            }

            // Atualizar campos
            if (dados.containsKey("titulo")) {
                publicacao.setTitulo((String) dados.get("titulo"));
            }
            if (dados.containsKey("descricao")) {
                publicacao.setDescricao((String) dados.get("descricao"));
            }
            if (dados.containsKey("preco")) {
                publicacao.setPreco(dados.get("preco") != null ? 
                        Double.valueOf(dados.get("preco").toString()) : null);
            }
            if (dados.containsKey("precoPromocional")) {
                publicacao.setPrecoPromocional(dados.get("precoPromocional") != null ? 
                        Double.valueOf(dados.get("precoPromocional").toString()) : null);
            }
            if (dados.containsKey("unidadeVenda")) {
                publicacao.setUnidadeVenda((String) dados.get("unidadeVenda"));
            }
            if (dados.containsKey("quantidadeMinima")) {
                publicacao.setQuantidadeMinima(dados.get("quantidadeMinima") != null ? 
                        Integer.valueOf(dados.get("quantidadeMinima").toString()) : null);
            }
            if (dados.containsKey("condicoesEspeciais")) {
                publicacao.setCondicoesEspeciais((String) dados.get("condicoesEspeciais"));
            }
            if (dados.containsKey("imagens")) {
                publicacao.setImagens((String) dados.get("imagens"));
            }
            if (dados.containsKey("ativa")) {
                publicacao.setAtiva((Boolean) dados.get("ativa"));
            }

            Publicacao atualizada = publicacaoRepository.save(publicacao);
            
            return ResponseEntity.ok(montarPublicacaoDTO(atualizada, usuario));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao editar publicação: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/feed/{id}
     * Excluir publicação (apenas o próprio fornecedor ou admin)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluirPublicacao(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Você precisa estar autenticado");
            }

            Optional<Publicacao> publicacaoOpt = publicacaoRepository.findById(id);
            if (!publicacaoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Publicação não encontrada");
            }

            Publicacao publicacao = publicacaoOpt.get();

            // Verificar permissão (fornecedor dono ou admin)
            boolean isAdmin = usuario.getTipo() == TipoUsuario.ADMIN;
            boolean isDono = false;
            
            if (Boolean.TRUE.equals(usuario.getFornecedor())) {
                Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findByCnpj(usuario.getCnpjOuCpf());
                if (fornecedorOpt.isPresent()) {
                    isDono = publicacao.getFornecedorId().equals(fornecedorOpt.get().getId());
                }
            }

            if (!isAdmin && !isDono) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Você só pode excluir suas próprias publicações");
            }

            // Soft delete (marcar como inativa)
            publicacao.setAtiva(false);
            publicacaoRepository.save(publicacao);
            
            return ResponseEntity.ok(Map.of("sucesso", true, "mensagem", "Publicação excluída"));
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao excluir publicação: " + e.getMessage());
        }
    }

    /**
     * POST /api/feed/{id}/curtir
     * Curtir ou descurtir uma publicação
     */
    @PostMapping("/{id}/curtir")
    @Transactional
    public ResponseEntity<?> curtirPublicacao(
            @PathVariable Long id,
            HttpSession session) {
        try {
            Usuario usuario = (Usuario) session.getAttribute("usuario");
            
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("erro", "Você precisa estar autenticado para curtir"));
            }

            Optional<Publicacao> publicacaoOpt = publicacaoRepository.findById(id);
            if (!publicacaoOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("erro", "Publicação não encontrada"));
            }

            Publicacao publicacao = publicacaoOpt.get();

            // Verificar se já curtiu
            boolean jaCurtiu = curtidaRepository.existsByPublicacaoIdAndUsuarioId(id, usuario.getId());

            if (jaCurtiu) {
                // Descurtir
                curtidaRepository.deleteByPublicacaoIdAndUsuarioId(id, usuario.getId());
                publicacao.setTotalCurtidas(Math.max(0, publicacao.getTotalCurtidas() - 1));
                publicacaoRepository.save(publicacao);
                
                return ResponseEntity.ok(Map.of(
                        "curtido", false,
                        "totalCurtidas", publicacao.getTotalCurtidas()
                ));
            } else {
                // Curtir
                CurtidaPublicacao curtida = new CurtidaPublicacao();
                curtida.setPublicacaoId(id);
                curtida.setUsuarioId(usuario.getId());
                curtidaRepository.save(curtida);
                
                publicacao.setTotalCurtidas(publicacao.getTotalCurtidas() + 1);
                publicacaoRepository.save(publicacao);
                
                return ResponseEntity.ok(Map.of(
                        "curtido", true,
                        "totalCurtidas", publicacao.getTotalCurtidas()
                ));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao curtir publicação: " + e.getMessage()));
        }
    }

    /**
     * Monta DTO da publicação com dados enriquecidos
     */
    private Map<String, Object> montarPublicacaoDTO(Publicacao publicacao, Usuario usuarioLogado) {
        Map<String, Object> dto = new HashMap<>();
        
        dto.put("id", publicacao.getId());
        dto.put("titulo", publicacao.getTitulo());
        dto.put("descricao", publicacao.getDescricao());
        dto.put("preco", publicacao.getPreco());
        dto.put("precoPromocional", publicacao.getPrecoPromocional());
        dto.put("unidadeVenda", publicacao.getUnidadeVenda());
        dto.put("quantidadeMinima", publicacao.getQuantidadeMinima());
        dto.put("condicoesEspeciais", publicacao.getCondicoesEspeciais());
        dto.put("totalCurtidas", publicacao.getTotalCurtidas());
        dto.put("criadaEm", publicacao.getCriadaEm().toString());
        dto.put("atualizadaEm", publicacao.getAtualizadaEm().toString());
        
        // Imagens
        if (publicacao.getImagens() != null && !publicacao.getImagens().isEmpty()) {
            dto.put("imagens", Arrays.asList(publicacao.getImagens().split(",")));
        } else {
            dto.put("imagens", new ArrayList<>());
        }
        
        // Dados do fornecedor
        Optional<Fornecedor> fornecedorOpt = fornecedorRepository.findById(publicacao.getFornecedorId());
        if (fornecedorOpt.isPresent()) {
            Fornecedor fornecedor = fornecedorOpt.get();
            Map<String, Object> fornecedorDTO = new HashMap<>();
            fornecedorDTO.put("id", fornecedor.getId());
            fornecedorDTO.put("nome", fornecedor.getNome());
            fornecedorDTO.put("foto", fornecedor.getFoto());
            fornecedorDTO.put("telefone", fornecedor.getTelefone());
            fornecedorDTO.put("email", fornecedor.getEmail());
            dto.put("fornecedor", fornecedorDTO);
        }
        
        // Verificar se usuário logado curtiu
        if (usuarioLogado != null) {
            boolean curtido = curtidaRepository.existsByPublicacaoIdAndUsuarioId(
                    publicacao.getId(), usuarioLogado.getId());
            dto.put("curtidoPorMim", curtido);
            
            // Verificar se é o dono da publicação
            if (Boolean.TRUE.equals(usuarioLogado.getFornecedor())) {
                Optional<Fornecedor> meuFornecedor = fornecedorRepository.findByCnpj(usuarioLogado.getCnpjOuCpf());
                dto.put("minhaPublicacao", meuFornecedor.isPresent() && 
                        publicacao.getFornecedorId().equals(meuFornecedor.get().getId()));
            } else {
                dto.put("minhaPublicacao", false);
            }
        } else {
            dto.put("curtidoPorMim", false);
            dto.put("minhaPublicacao", false);
        }
        
        return dto;
    }
}
