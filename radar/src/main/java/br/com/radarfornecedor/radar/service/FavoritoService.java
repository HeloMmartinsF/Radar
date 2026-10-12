package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gerenciar favoritos de produtos e fornecedores.
 * 
 * RESPONSABILIDADES:
 * - Criar e remover favoritos com validações
 * - Verificar duplicatas (unique constraint)
 * - Validar existência de produto/fornecedor
 * - Listar favoritos com dados enriquecidos
 * - Estatísticas (contadores)
 * 
 * SEGURANÇA:
 * - Todas operações validam usuarioId
 * - Não permite modificar favoritos de outros usuários
 */
@Service
public class FavoritoService {

    private final ProdutoFavoritoRepository produtoFavoritoRepository;
    private final FornecedorFavoritoRepository fornecedorFavoritoRepository;
    private final ProdutoRepository produtoRepository;
    private final FornecedorRepository fornecedorRepository;

    public FavoritoService(
            ProdutoFavoritoRepository produtoFavoritoRepository,
            FornecedorFavoritoRepository fornecedorFavoritoRepository,
            ProdutoRepository produtoRepository,
            FornecedorRepository fornecedorRepository) {
        this.produtoFavoritoRepository = produtoFavoritoRepository;
        this.fornecedorFavoritoRepository = fornecedorFavoritoRepository;
        this.produtoRepository = produtoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    // ======================== PRODUTOS FAVORITOS ========================

    /**
     * Favorita um produto para um usuário.
     * Valida se produto existe e previne duplicatas.
     *
     * @param usuarioId ID do usuário
     * @param produtoId ID do produto
     * @return ProdutoFavorito criado
     * @throws IllegalArgumentException se produto não existe ou já está favoritado
     */
    @Transactional
    public ProdutoFavorito favoritarProduto(Long usuarioId, Long produtoId) {
        // Validar se produto existe
        if (!produtoRepository.existsById(produtoId)) {
            throw new IllegalArgumentException("Produto não encontrado (ID: " + produtoId + ")");
        }

        // Verificar se já está favoritado
        if (produtoFavoritoRepository.existsByUsuarioIdAndProdutoId(usuarioId, produtoId)) {
            throw new IllegalArgumentException("Produto já está na lista de favoritos");
        }

        // Criar favorito
        ProdutoFavorito favorito = new ProdutoFavorito(usuarioId, produtoId);
        
        try {
            return produtoFavoritoRepository.save(favorito);
        } catch (DataIntegrityViolationException e) {
            // Caso raro: outro processo criou favorito enquanto validávamos
            throw new IllegalArgumentException("Produto já está na lista de favoritos");
        }
    }

    /**
     * Remove um produto dos favoritos.
     * Valida que o favorito pertence ao usuário.
     *
     * @param usuarioId ID do usuário
     * @param produtoId ID do produto
     * @return true se removeu, false se não estava favoritado
     */
    @Transactional
    public boolean desfavoritarProduto(Long usuarioId, Long produtoId) {
        int deleted = produtoFavoritoRepository.deleteByUsuarioIdAndProdutoId(usuarioId, produtoId);
        return deleted > 0;
    }

    /**
     * Lista todos os produtos favoritos de um usuário.
     * Retorna dados ENRIQUECIDOS com informações do produto.
     *
     * @param usuarioId ID do usuário
     * @return Lista de Maps com dados do favorito + produto
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarProdutosFavoritos(Long usuarioId) {
        List<ProdutoFavorito> favoritos = produtoFavoritoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
        
        // Buscar IDs dos produtos
        Set<Long> produtoIds = favoritos.stream()
                .map(ProdutoFavorito::getProdutoId)
                .collect(Collectors.toSet());
        
        if (produtoIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Buscar produtos em batch (performance)
        Map<Long, Produto> produtosMap = produtoRepository.findAllById(produtoIds).stream()
                .collect(Collectors.toMap(Produto::getId, p -> p));
        
        // Buscar fornecedores dos produtos
        Set<Long> fornecedorIds = produtosMap.values().stream()
                .map(Produto::getFornecedorId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Map<Long, Fornecedor> fornecedoresMap = Collections.emptyMap();
        if (!fornecedorIds.isEmpty()) {
            fornecedoresMap = fornecedorRepository.findAllById(fornecedorIds).stream()
                    .collect(Collectors.toMap(Fornecedor::getId, f -> f));
        }
        
        // Montar DTOs enriquecidos
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (ProdutoFavorito favorito : favoritos) {
            Produto produto = produtosMap.get(favorito.getProdutoId());
            
            if (produto == null) {
                // Produto foi deletado - pular
                continue;
            }
            
            Map<String, Object> dto = new HashMap<>();
            dto.put("favoritoId", favorito.getId());
            dto.put("criadoEm", favorito.getCriadoEm());
            dto.put("notas", favorito.getNotas());
            
            // Dados do produto
            dto.put("produtoId", produto.getId());
            dto.put("produtoNome", produto.getNome());
            dto.put("produtoDescricao", produto.getDescricao());
            dto.put("produtoCategoria", produto.getCategoria());
            dto.put("produtoPreco", produto.getPreco());
            
            // Dados do fornecedor (se existir)
            if (produto.getFornecedorId() != null) {
                Fornecedor fornecedor = fornecedoresMap.get(produto.getFornecedorId());
                if (fornecedor != null) {
                    dto.put("fornecedorId", fornecedor.getId());
                    dto.put("fornecedorNome", fornecedor.getNome());
                    dto.put("fornecedorCnpj", fornecedor.getCnpj());
                    dto.put("fornecedorCidade", fornecedor.getCidade());
                    dto.put("fornecedorEstado", fornecedor.getEstado());
                }
            }
            
            resultado.add(dto);
        }
        
        return resultado;
    }

    /**
     * Verifica se um produto está favoritado por um usuário.
     * Útil para renderizar estado do botão de favorito.
     *
     * @param usuarioId ID do usuário
     * @param produtoId ID do produto
     * @return true se está favoritado, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean isProdutoFavoritado(Long usuarioId, Long produtoId) {
        return produtoFavoritoRepository.existsByUsuarioIdAndProdutoId(usuarioId, produtoId);
    }

    /**
     * Conta quantos produtos um usuário favoritou.
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de produtos favoritos
     */
    @Transactional(readOnly = true)
    public long contarProdutosFavoritos(Long usuarioId) {
        return produtoFavoritoRepository.countByUsuarioId(usuarioId);
    }

    // ======================== FORNECEDORES FAVORITOS ========================

    /**
     * Favorita/segue um fornecedor.
     * Valida se fornecedor existe e previne duplicatas.
     *
     * @param usuarioId ID do usuário
     * @param fornecedorId ID do fornecedor
     * @return FornecedorFavorito criado
     * @throws IllegalArgumentException se fornecedor não existe ou já está favoritado
     */
    @Transactional
    public FornecedorFavorito favoritarFornecedor(Long usuarioId, Long fornecedorId) {
        // Validar se fornecedor existe
        if (!fornecedorRepository.existsById(fornecedorId)) {
            throw new IllegalArgumentException("Fornecedor não encontrado (ID: " + fornecedorId + ")");
        }

        // Verificar se já está favoritado
        if (fornecedorFavoritoRepository.existsByUsuarioIdAndFornecedorId(usuarioId, fornecedorId)) {
            throw new IllegalArgumentException("Fornecedor já está na lista de favoritos");
        }

        // Criar favorito
        FornecedorFavorito favorito = new FornecedorFavorito(usuarioId, fornecedorId);
        
        try {
            return fornecedorFavoritoRepository.save(favorito);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Fornecedor já está na lista de favoritos");
        }
    }

    /**
     * Remove um fornecedor dos favoritos (deixa de seguir).
     *
     * @param usuarioId ID do usuário
     * @param fornecedorId ID do fornecedor
     * @return true se removeu, false se não estava favoritado
     */
    @Transactional
    public boolean desfavoritarFornecedor(Long usuarioId, Long fornecedorId) {
        int deleted = fornecedorFavoritoRepository.deleteByUsuarioIdAndFornecedorId(usuarioId, fornecedorId);
        return deleted > 0;
    }

    /**
     * Lista todos os fornecedores favoritos de um usuário.
     * Retorna dados ENRIQUECIDOS com informações do fornecedor.
     *
     * @param usuarioId ID do usuário
     * @return Lista de Maps com dados do favorito + fornecedor
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarFornecedoresFavoritos(Long usuarioId) {
        List<FornecedorFavorito> favoritos = fornecedorFavoritoRepository.findByUsuarioIdOrderByCriadoEmDesc(usuarioId);
        
        // Buscar IDs dos fornecedores
        Set<Long> fornecedorIds = favoritos.stream()
                .map(FornecedorFavorito::getFornecedorId)
                .collect(Collectors.toSet());
        
        if (fornecedorIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Buscar fornecedores em batch (performance)
        Map<Long, Fornecedor> fornecedoresMap = fornecedorRepository.findAllById(fornecedorIds).stream()
                .collect(Collectors.toMap(Fornecedor::getId, f -> f));
        
        // Montar DTOs enriquecidos
        List<Map<String, Object>> resultado = new ArrayList<>();
        
        for (FornecedorFavorito favorito : favoritos) {
            Fornecedor fornecedor = fornecedoresMap.get(favorito.getFornecedorId());
            
            if (fornecedor == null) {
                // Fornecedor foi deletado - pular
                continue;
            }
            
            Map<String, Object> dto = new HashMap<>();
            dto.put("favoritoId", favorito.getId());
            dto.put("criadoEm", favorito.getCriadoEm());
            dto.put("notificarNovasOfertas", favorito.getNotificarNovasOfertas());
            dto.put("notas", favorito.getNotas());
            
            // Dados do fornecedor
            dto.put("fornecedorId", fornecedor.getId());
            dto.put("fornecedorNome", fornecedor.getNome());
            dto.put("fornecedorCnpj", fornecedor.getCnpj());
            dto.put("fornecedorEmail", fornecedor.getEmail());
            dto.put("fornecedorTelefone", fornecedor.getTelefone());
            dto.put("fornecedorCidade", fornecedor.getCidade());
            dto.put("fornecedorEstado", fornecedor.getEstado());
            dto.put("fornecedorFoto", fornecedor.getFoto());
            dto.put("fornecedorStatus", fornecedor.getStatus());
            dto.put("fornecedorPontuacaoRisco", fornecedor.getPontuacaoRisco());
            
            resultado.add(dto);
        }
        
        return resultado;
    }

    /**
     * Verifica se um fornecedor está favoritado/seguido por um usuário.
     *
     * @param usuarioId ID do usuário
     * @param fornecedorId ID do fornecedor
     * @return true se está favoritado, false caso contrário
     */
    @Transactional(readOnly = true)
    public boolean isFornecedorFavoritado(Long usuarioId, Long fornecedorId) {
        return fornecedorFavoritoRepository.existsByUsuarioIdAndFornecedorId(usuarioId, fornecedorId);
    }

    /**
     * Conta quantos fornecedores um usuário está seguindo.
     *
     * @param usuarioId ID do usuário
     * @return Quantidade de fornecedores favoritos
     */
    @Transactional(readOnly = true)
    public long contarFornecedoresFavoritos(Long usuarioId) {
        return fornecedorFavoritoRepository.countByUsuarioId(usuarioId);
    }

    /**
     * Conta quantos seguidores um fornecedor tem.
     * Útil para estatísticas do fornecedor.
     *
     * @param fornecedorId ID do fornecedor
     * @return Quantidade de seguidores
     */
    @Transactional(readOnly = true)
    public long contarSeguidoresFornecedor(Long fornecedorId) {
        return fornecedorFavoritoRepository.countByFornecedorId(fornecedorId);
    }

    /**
     * Atualiza configuração de notificações para um fornecedor favoritado.
     * Permite "silenciar" notificações sem desfavoritar.
     *
     * @param usuarioId ID do usuário
     * @param fornecedorId ID do fornecedor
     * @param notificar true para ativar, false para desativar
     * @return true se atualizou, false se não estava favoritado
     */
    @Transactional
    public boolean atualizarNotificacoesFornecedor(Long usuarioId, Long fornecedorId, Boolean notificar) {
        int updated = fornecedorFavoritoRepository.updateNotificacoes(usuarioId, fornecedorId, notificar);
        return updated > 0;
    }

    /**
     * Lista IDs dos usuários que seguem um fornecedor COM notificações ativas.
     * Usado para enviar alertas de novas publicações/ofertas.
     *
     * @param fornecedorId ID do fornecedor
     * @return Lista de IDs dos usuários a serem notificados
     */
    @Transactional(readOnly = true)
    public List<Long> buscarUsuariosParaNotificar(Long fornecedorId) {
        return fornecedorFavoritoRepository.findUsuarioIdsComNotificacoesAtivasByFornecedorId(fornecedorId);
    }
}
