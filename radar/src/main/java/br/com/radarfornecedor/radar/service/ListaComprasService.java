package br.com.radarfornecedor.radar.service;

import br.com.radarfornecedor.radar.model.*;
import br.com.radarfornecedor.radar.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service para gerenciar listas de compras.
 * 
 * RESPONSABILIDADES:
 * - CRUD de listas e itens
 * - Cálculo de valores estimados (com validação de unidades)
 * - Gerenciamento de quantidades
 * - Marcar itens como concluídos
 * - Enriquecimento com dados de produtos
 * 
 * SEGURANÇA:
 * - Todas operações validam usuarioId
 * - Não permite modificar listas de outros usuários
 */
@Service
public class ListaComprasService {

    private final ListaComprasRepository listaComprasRepository;
    private final ItemListaComprasRepository itemListaComprasRepository;
    private final ProdutoRepository produtoRepository;
    private final PublicacaoRepository publicacaoRepository;

    public ListaComprasService(
            ListaComprasRepository listaComprasRepository,
            ItemListaComprasRepository itemListaComprasRepository,
            ProdutoRepository produtoRepository,
            PublicacaoRepository publicacaoRepository) {
        this.listaComprasRepository = listaComprasRepository;
        this.itemListaComprasRepository = itemListaComprasRepository;
        this.produtoRepository = produtoRepository;
        this.publicacaoRepository = publicacaoRepository;
    }

    // ======================== LISTAS ========================

    /**
     * Cria uma nova lista de compras para um usuário.
     */
    @Transactional
    public ListaCompras criarLista(Long usuarioId, String nome, String descricao) {
        ListaCompras lista = new ListaCompras(usuarioId, nome);
        lista.setDescricao(descricao);
        return listaComprasRepository.save(lista);
    }

    /**
     * Lista todas as listas ativas de um usuário.
     */
    @Transactional(readOnly = true)
    public List<ListaCompras> listarListasAtivas(Long usuarioId) {
        return listaComprasRepository.findByUsuarioIdAndAtivaOrderByAtualizadaEmDesc(usuarioId, true);
    }

    /**
     * Busca uma lista específica de um usuário.
     * Retorna vazio se não existir ou não pertencer ao usuário.
     */
    @Transactional(readOnly = true)
    public Optional<ListaCompras> buscarLista(Long listaId, Long usuarioId) {
        return listaComprasRepository.findByIdAndUsuarioId(listaId, usuarioId);
    }

    /**
     * Atualiza nome/descrição de uma lista.
     */
    @Transactional
    public ListaCompras atualizarLista(Long listaId, Long usuarioId, String nome, String descricao) {
        ListaCompras lista = buscarLista(listaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Lista não encontrada ou não pertence ao usuário"));

        if (nome != null && !nome.isBlank()) {
            lista.setNome(nome);
        }
        if (descricao != null) {
            lista.setDescricao(descricao);
        }

        return listaComprasRepository.save(lista);
    }

    /**
     * Arquiva uma lista (marca como inativa).
     * Não deleta do banco, apenas oculta da visualização padrão.
     */
    @Transactional
    public boolean arquivarLista(Long listaId, Long usuarioId) {
        Optional<ListaCompras> listaOpt = buscarLista(listaId, usuarioId);
        if (listaOpt.isEmpty()) {
            return false;
        }

        ListaCompras lista = listaOpt.get();
        lista.setAtiva(false);
        listaComprasRepository.save(lista);
        return true;
    }

    /**
     * Deleta permanentemente uma lista e todos seus itens.
     */
    @Transactional
    public boolean deletarLista(Long listaId, Long usuarioId) {
        Optional<ListaCompras> listaOpt = buscarLista(listaId, usuarioId);
        if (listaOpt.isEmpty()) {
            return false;
        }

        listaComprasRepository.delete(listaOpt.get());
        return true;
    }

    // ======================== ITENS ========================

    /**
     * Adiciona um produto à lista de compras.
     * Captura preço do produto no momento da adição.
     */
    @Transactional
    public ItemListaCompras adicionarItem(
            Long listaId,
            Long usuarioId,
            Long produtoId,
            Double quantidade,
            String observacoes) {

        // Validar que lista pertence ao usuário
        ListaCompras lista = buscarLista(listaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Lista não encontrada"));

        // Validar que produto existe
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        // Verificar se produto já está na lista
        Optional<ItemListaCompras> itemExistente = itemListaComprasRepository
                .findByListaIdAndProdutoId(listaId, produtoId);

        if (itemExistente.isPresent()) {
            throw new IllegalArgumentException("Produto já está na lista. Use atualizar quantidade.");
        }

        // Criar item
        ItemListaCompras item = new ItemListaCompras(lista, produtoId, quantidade, "unidade");
        item.setPrecoUnitario(produto.getPreco());
        item.setObservacoes(observacoes);

        return itemListaComprasRepository.save(item);
    }

    /**
     * Adiciona item à lista a partir de uma publicação do feed.
     * Usa preço e unidade da publicação.
     */
    @Transactional
    public ItemListaCompras adicionarItemDePublicacao(
            Long listaId,
            Long usuarioId,
            Long publicacaoId,
            Double quantidade) {

        // Validar lista
        ListaCompras lista = buscarLista(listaId, usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Lista não encontrada"));

        // Buscar publicação
        Publicacao publicacao = publicacaoRepository.findById(publicacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Publicação não encontrada"));

        // Se publicação tem produto vinculado, usar produto; senão lançar erro
        if (publicacao.getProdutoId() == null) {
            throw new IllegalArgumentException("Esta publicação não está vinculada a um produto cadastrado");
        }

        // Verificar duplicata
        Optional<ItemListaCompras> itemExistente = itemListaComprasRepository
                .findByListaIdAndProdutoId(listaId, publicacao.getProdutoId());

        if (itemExistente.isPresent()) {
            throw new IllegalArgumentException("Produto já está na lista");
        }

        // Criar item usando dados da publicação
        ItemListaCompras item = new ItemListaCompras(
                lista,
                publicacao.getProdutoId(),
                quantidade,
                publicacao.getUnidadeVenda() != null ? publicacao.getUnidadeVenda() : "unidade"
        );

        // Usar preço promocional se existir, senão preço normal
        BigDecimal preco = publicacao.getPrecoPromocional() != null
                ? BigDecimal.valueOf(publicacao.getPrecoPromocional())
                : (publicacao.getPreco() != null ? BigDecimal.valueOf(publicacao.getPreco()) : null);

        item.setPrecoUnitario(preco);
        item.setPublicacaoId(publicacaoId);

        return itemListaComprasRepository.save(item);
    }

    /**
     * Atualiza quantidade de um item.
     */
    @Transactional
    public ItemListaCompras atualizarQuantidade(
            Long itemId,
            Long listaId,
            Long usuarioId,
            Double novaQuantidade) {

        // Validar que lista pertence ao usuário
        if (!listaComprasRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            throw new IllegalArgumentException("Lista não encontrada");
        }

        // Buscar item
        ItemListaCompras item = itemListaComprasRepository.findByIdAndListaId(itemId, listaId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));

        // Validar quantidade
        if (novaQuantidade == null || novaQuantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        item.setQuantidade(novaQuantidade);
        return itemListaComprasRepository.save(item);
    }

    /**
     * Marca um item como concluído.
     */
    @Transactional
    public ItemListaCompras marcarComoConcluido(
            Long itemId,
            Long listaId,
            Long usuarioId,
            Boolean concluido) {

        // Validar autorização
        if (!listaComprasRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            throw new IllegalArgumentException("Lista não encontrada");
        }

        // Buscar item
        ItemListaCompras item = itemListaComprasRepository.findByIdAndListaId(itemId, listaId)
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado"));

        item.setConcluido(concluido);
        return itemListaComprasRepository.save(item);
    }

    /**
     * Remove um item da lista.
     */
    @Transactional
    public boolean removerItem(Long itemId, Long listaId, Long usuarioId) {
        // Validar autorização
        if (!listaComprasRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            return false;
        }

        int deleted = itemListaComprasRepository.deleteByIdAndListaId(itemId, listaId);
        return deleted > 0;
    }

    /**
     * Lista itens de uma lista com dados enriquecidos (produto completo).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listarItensEnriquecidos(Long listaId, Long usuarioId) {
        // Validar autorização
        if (!listaComprasRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            throw new IllegalArgumentException("Lista não encontrada");
        }

        // Buscar itens
        List<ItemListaCompras> itens = itemListaComprasRepository.findByListaIdOrderByAdicionadoEmDesc(listaId);

        if (itens.isEmpty()) {
            return Collections.emptyList();
        }

        // Buscar produtos em batch
        Set<Long> produtoIds = itens.stream()
                .map(ItemListaCompras::getProdutoId)
                .collect(Collectors.toSet());

        Map<Long, Produto> produtosMap = produtoRepository.findAllById(produtoIds).stream()
                .collect(Collectors.toMap(Produto::getId, p -> p));

        // Montar DTOs enriquecidos
        List<Map<String, Object>> resultado = new ArrayList<>();

        for (ItemListaCompras item : itens) {
            Produto produto = produtosMap.get(item.getProdutoId());

            if (produto == null) {
                // Produto foi deletado - pular ou marcar como indisponível
                continue;
            }

            Map<String, Object> dto = new HashMap<>();
            dto.put("itemId", item.getId());
            dto.put("quantidade", item.getQuantidade());
            dto.put("unidade", item.getUnidade());
            dto.put("precoUnitario", item.getPrecoUnitario());
            dto.put("valorTotal", item.calcularValorTotal());
            dto.put("concluido", item.getConcluido());
            dto.put("observacoes", item.getObservacoes());
            dto.put("adicionadoEm", item.getAdicionadoEm());

            // Dados do produto
            dto.put("produtoId", produto.getId());
            dto.put("produtoNome", produto.getNome());
            dto.put("produtoDescricao", produto.getDescricao());
            dto.put("produtoCategoria", produto.getCategoria());
            dto.put("produtoPrecoAtual", produto.getPreco());

            resultado.add(dto);
        }

        return resultado;
    }

    /**
     * Calcula valor total estimado de uma lista.
     * Soma apenas itens com preço disponível e NÃO concluídos.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calcularValorEstimado(Long listaId, Long usuarioId) {
        // Validar autorização
        if (!listaComprasRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            throw new IllegalArgumentException("Lista não encontrada");
        }

        // Buscar itens não concluídos
        List<ItemListaCompras> itens = itemListaComprasRepository.findByListaIdAndConcluidoFalse(listaId);

        BigDecimal totalComPreco = BigDecimal.ZERO;
        int itensComPreco = 0;
        int itensSemPreco = 0;

        for (ItemListaCompras item : itens) {
            BigDecimal valorItem = item.calcularValorTotal();
            if (valorItem != null) {
                totalComPreco = totalComPreco.add(valorItem);
                itensComPreco++;
            } else {
                itensSemPreco++;
            }
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("valorTotalEstimado", totalComPreco);
        resultado.put("itensComPreco", itensComPreco);
        resultado.put("itensSemPreco", itensSemPreco);
        resultado.put("totalItens", itens.size());
        resultado.put("temItensSemPreco", itensSemPreco > 0);

        return resultado;
    }

    /**
     * Conta estatísticas de uma lista.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obterEstatisticasLista(Long listaId, Long usuarioId) {
        // Validar autorização
        if (!listaComprasRepository.existsByIdAndUsuarioId(listaId, usuarioId)) {
            throw new IllegalArgumentException("Lista não encontrada");
        }

        long totalItens = itemListaComprasRepository.countByListaId(listaId);
        long itensPendentes = itemListaComprasRepository.countByListaIdAndConcluidoFalse(listaId);
        long itensConcluidos = totalItens - itensPendentes;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItens", totalItens);
        stats.put("itensPendentes", itensPendentes);
        stats.put("itensConcluidos", itensConcluidos);
        stats.put("percentualConcluido", totalItens > 0 ? (itensConcluidos * 100.0 / totalItens) : 0);

        return stats;
    }
}
