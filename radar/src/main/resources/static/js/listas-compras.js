/**
 * Sistema de Lista de Compras - Radar Fornecedor
 * Gerenciamento completo de listas e itens com cálculos automáticos
 */

// Estado global
let listaAtual = null;
let listaIdAtual = null;
let abaAtual = 'ativas';
let editandoLista = false;

// Inicialização
document.addEventListener('DOMContentLoaded', async () => {
    await carregarMenuLateral();
    await exibirInfoUsuario();
    await aplicarControlesDeAcesso();
    await carregarEstatisticasResumo();
    await carregarListas();
    
    // Configurar busca de produtos
    const inputBusca = document.getElementById('input-busca-produto');
    if (inputBusca) {
        let timeoutBusca;
        inputBusca.addEventListener('input', (e) => {
            clearTimeout(timeoutBusca);
            const termo = e.target.value.trim();
            if (termo.length >= 2) {
                timeoutBusca = setTimeout(() => buscarProdutos(termo), 300);
            } else {
                document.getElementById('resultados-busca-produto').classList.add('hidden');
            }
        });
    }
});

// ==================== CARREGAMENTO DE DADOS ====================

async function carregarEstatisticasResumo() {
    try {
        const response = await fetch('/api/lista-compras');
        if (!response.ok) throw new Error('Erro ao carregar estatísticas');
        
        const listas = await response.json();
        const ativas = listas.filter(l => l.ativa);
        
        let totalItens = 0;
        let totalConcluidos = 0;
        
        for (const lista of ativas) {
            const stats = await fetch(`/api/lista-compras/${lista.id}/estatisticas`).then(r => r.json());
            totalItens += stats.totalItens || 0;
            totalConcluidos += stats.itensConcluidos || 0;
        }
        
        const percentualGeral = totalItens > 0 ? Math.round((totalConcluidos / totalItens) * 100) : 0;
        
        const html = `
            <div class="bg-white rounded-lg shadow p-6">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-gray-500 text-sm">Listas Ativas</p>
                        <p class="text-3xl font-bold text-gray-800">${ativas.length}</p>
                    </div>
                    <i class="fas fa-list text-4xl text-blue-500"></i>
                </div>
            </div>
            <div class="bg-white rounded-lg shadow p-6">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-gray-500 text-sm">Total de Itens</p>
                        <p class="text-3xl font-bold text-gray-800">${totalItens}</p>
                    </div>
                    <i class="fas fa-boxes text-4xl text-purple-500"></i>
                </div>
            </div>
            <div class="bg-white rounded-lg shadow p-6">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-gray-500 text-sm">Itens Concluídos</p>
                        <p class="text-3xl font-bold text-gray-800">${totalConcluidos}</p>
                    </div>
                    <i class="fas fa-check-circle text-4xl text-green-500"></i>
                </div>
            </div>
            <div class="bg-white rounded-lg shadow p-6">
                <div class="flex items-center justify-between">
                    <div>
                        <p class="text-gray-500 text-sm">Progresso Geral</p>
                        <p class="text-3xl font-bold text-gray-800">${percentualGeral}%</p>
                    </div>
                    <i class="fas fa-chart-line text-4xl text-[#d4af37]"></i>
                </div>
            </div>
        `;
        
        document.getElementById('estatisticas-resumo').innerHTML = html;
    } catch (error) {
        console.error('Erro ao carregar estatísticas:', error);
    }
}

async function carregarListas() {
    try {
        const response = await fetch('/api/lista-compras');
        if (!response.ok) throw new Error('Erro ao carregar listas');
        
        const listas = await response.json();
        
        const ativas = listas.filter(l => l.ativa);
        const arquivadas = listas.filter(l => !l.ativa);
        
        renderizarListas(ativas, 'container-ativas');
        renderizarListas(arquivadas, 'container-arquivadas');
        
    } catch (error) {
        console.error('Erro ao carregar listas:', error);
        mostrarNotificacao('Erro ao carregar listas', 'error');
        document.getElementById('container-ativas').innerHTML = `
            <div class="col-span-full text-center py-12">
                <i class="fas fa-exclamation-circle text-4xl text-red-400 mb-4"></i>
                <p class="text-gray-600">Erro ao carregar listas de compras</p>
            </div>
        `;
    }
}

function renderizarListas(listas, containerId) {
    const container = document.getElementById(containerId);
    
    if (listas.length === 0) {
        container.innerHTML = `
            <div class="col-span-full text-center py-12">
                <i class="fas fa-shopping-cart text-4xl text-gray-300 mb-4"></i>
                <p class="text-gray-500">Nenhuma lista encontrada</p>
                ${containerId === 'container-ativas' ? '<button onclick="abrirModalNovaLista()" class="mt-4 text-[#d4af37] hover:underline font-semibold"><i class="fas fa-plus mr-2"></i>Criar primeira lista</button>' : ''}
            </div>
        `;
        return;
    }
    
    container.innerHTML = listas.map(lista => criarCardLista(lista)).join('');
}

function criarCardLista(lista) {
    const dataCriacao = new Date(lista.criadaEm).toLocaleDateString('pt-BR');
    const descricaoTruncada = lista.descricao && lista.descricao.length > 100 
        ? lista.descricao.substring(0, 100) + '...' 
        : lista.descricao || 'Sem descrição';
    
    return `
        <div class="lista-card bg-white rounded-lg shadow-md p-6 cursor-pointer hover:shadow-lg transition-all" 
             onclick="abrirDetalhesLista(${lista.id})">
            <div class="flex justify-between items-start mb-3">
                <h3 class="text-xl font-bold text-gray-800 flex-1">${lista.nome}</h3>
                ${lista.ativa ? '<span class="badge-novo bg-green-500 text-white text-xs px-2 py-1 rounded-full">Ativa</span>' : '<span class="bg-gray-400 text-white text-xs px-2 py-1 rounded-full">Arquivada</span>'}
            </div>
            <p class="text-gray-600 text-sm mb-4">${descricaoTruncada}</p>
            <div class="flex items-center text-sm text-gray-500 mb-4">
                <i class="far fa-calendar mr-2"></i>
                <span>${dataCriacao}</span>
            </div>
            <div class="flex gap-2">
                <button onclick="event.stopPropagation(); abrirDetalhesLista(${lista.id})" 
                        class="flex-1 bg-[#d4af37] hover:bg-yellow-600 text-white py-2 rounded-lg font-semibold transition-colors">
                    <i class="fas fa-eye mr-2"></i>Ver Detalhes
                </button>
            </div>
        </div>
    `;
}

// ==================== GERENCIAMENTO DE LISTAS ====================

function abrirModalNovaLista() {
    editandoLista = false;
    listaIdAtual = null;
    document.getElementById('modal-titulo').textContent = 'Nova Lista';
    document.getElementById('input-nome-lista').value = '';
    document.getElementById('input-descricao-lista').value = '';
    document.getElementById('modal-nova-lista').classList.remove('hidden');
}

function fecharModalNovaLista() {
    document.getElementById('modal-nova-lista').classList.add('hidden');
}

async function salvarLista(event) {
    event.preventDefault();
    
    const nome = document.getElementById('input-nome-lista').value.trim();
    const descricao = document.getElementById('input-descricao-lista').value.trim();
    
    if (!nome) {
        mostrarNotificacao('Nome da lista é obrigatório', 'error');
        return;
    }
    
    try {
        const payload = { nome, descricao };
        let response;
        
        if (editandoLista && listaIdAtual) {
            response = await fetch(`/api/lista-compras/${listaIdAtual}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        } else {
            response = await fetch('/api/lista-compras', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
        }
        
        if (!response.ok) throw new Error('Erro ao salvar lista');
        
        mostrarNotificacao(editandoLista ? 'Lista atualizada com sucesso!' : 'Lista criada com sucesso!', 'success');
        fecharModalNovaLista();
        await carregarListas();
        await carregarEstatisticasResumo();
        
        if (editandoLista && listaIdAtual) {
            await abrirDetalhesLista(listaIdAtual);
        }
        
    } catch (error) {
        console.error('Erro ao salvar lista:', error);
        mostrarNotificacao('Erro ao salvar lista', 'error');
    }
}

async function abrirDetalhesLista(listaId) {
    try {
        const [lista, itens, estatisticas, valorTotal] = await Promise.all([
            fetch(`/api/lista-compras/${listaId}`).then(r => r.json()),
            fetch(`/api/lista-compras/${listaId}/itens`).then(r => r.json()),
            fetch(`/api/lista-compras/${listaId}/estatisticas`).then(r => r.json()),
            fetch(`/api/lista-compras/${listaId}/total`).then(r => r.json())
        ]);
        
        listaAtual = lista;
        listaIdAtual = listaId;
        
        // Preencher header
        document.getElementById('detalhes-nome').textContent = lista.nome;
        document.getElementById('detalhes-descricao').textContent = lista.descricao || 'Sem descrição';
        document.getElementById('detalhes-data').textContent = new Date(lista.criadaEm).toLocaleDateString('pt-BR');
        document.getElementById('detalhes-total-itens').textContent = estatisticas.totalItens || 0;
        
        // Barra de progresso
        const percentual = estatisticas.percentualConcluido || 0;
        document.getElementById('detalhes-percentual').textContent = `${percentual}%`;
        document.getElementById('detalhes-barra-progresso').style.width = `${percentual}%`;
        
        // Valor total
        document.getElementById('detalhes-valor-total').textContent = valorTotal.valorTotalFormatado || 'R$ 0,00';
        document.getElementById('detalhes-itens-com-preco').textContent = valorTotal.itensComPreco || 0;
        document.getElementById('detalhes-total-itens-valor').textContent = valorTotal.totalItens || 0;
        
        // Botão arquivar/desarquivar
        document.getElementById('btn-arquivar-texto').textContent = lista.ativa ? 'Arquivar' : 'Desarquivar';
        
        // Renderizar itens
        renderizarItens(itens);
        
        document.getElementById('modal-detalhes-lista').classList.remove('hidden');
        
    } catch (error) {
        console.error('Erro ao carregar detalhes:', error);
        mostrarNotificacao('Erro ao carregar detalhes da lista', 'error');
    }
}

function fecharModalDetalhes() {
    document.getElementById('modal-detalhes-lista').classList.add('hidden');
    listaAtual = null;
    listaIdAtual = null;
}

function editarListaAtual() {
    if (!listaAtual) return;
    
    editandoLista = true;
    document.getElementById('modal-titulo').textContent = 'Editar Lista';
    document.getElementById('input-nome-lista').value = listaAtual.nome;
    document.getElementById('input-descricao-lista').value = listaAtual.descricao || '';
    document.getElementById('modal-nova-lista').classList.remove('hidden');
}

async function arquivarListaAtual() {
    if (!listaIdAtual) return;
    
    const acao = listaAtual.ativa ? 'arquivar' : 'desarquivar';
    if (!confirm(`Deseja realmente ${acao} esta lista?`)) return;
    
    try {
        const response = await fetch(`/api/lista-compras/${listaIdAtual}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nome: listaAtual.nome,
                descricao: listaAtual.descricao,
                ativa: !listaAtual.ativa
            })
        });
        
        if (!response.ok) throw new Error(`Erro ao ${acao} lista`);
        
        mostrarNotificacao(`Lista ${acao === 'arquivar' ? 'arquivada' : 'desarquivada'} com sucesso!`, 'success');
        fecharModalDetalhes();
        await carregarListas();
        await carregarEstatisticasResumo();
        
    } catch (error) {
        console.error(`Erro ao ${acao} lista:`, error);
        mostrarNotificacao(`Erro ao ${acao} lista`, 'error');
    }
}

async function deletarListaAtual() {
    if (!listaIdAtual) return;
    
    if (!confirm('⚠️ ATENÇÃO: Esta ação é IRREVERSÍVEL!\n\nTodos os itens desta lista serão PERMANENTEMENTE excluídos.\n\nDeseja realmente continuar?')) return;
    
    try {
        const response = await fetch(`/api/lista-compras/${listaIdAtual}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Erro ao deletar lista');
        
        mostrarNotificacao('Lista excluída com sucesso', 'success');
        fecharModalDetalhes();
        await carregarListas();
        await carregarEstatisticasResumo();
        
    } catch (error) {
        console.error('Erro ao deletar lista:', error);
        mostrarNotificacao('Erro ao deletar lista', 'error');
    }
}

// ==================== GERENCIAMENTO DE ITENS ====================

function renderizarItens(itens) {
    const container = document.getElementById('container-itens-lista');
    
    if (itens.length === 0) {
        container.innerHTML = `
            <div class="text-center py-8 text-gray-500">
                <i class="fas fa-box-open text-4xl mb-3"></i>
                <p>Nenhum item adicionado ainda</p>
                <button onclick="abrirModalAdicionarItem()" class="mt-4 text-[#d4af37] hover:underline font-semibold">
                    <i class="fas fa-plus mr-2"></i>Adicionar primeiro item
                </button>
            </div>
        `;
        return;
    }
    
    container.innerHTML = itens.map(item => criarCardItem(item)).join('');
}

function criarCardItem(item) {
    const produtoNome = item.produto?.nome || 'Produto não encontrado';
    const fornecedorNome = item.produto?.fornecedor?.nomeFantasia || 'Fornecedor desconhecido';
    const precoUnitarioFormatado = item.precoUnitario 
        ? `R$ ${item.precoUnitario.toFixed(2).replace('.', ',')}` 
        : 'Preço não informado';
    const valorTotal = item.precoUnitario 
        ? `R$ ${(item.precoUnitario * item.quantidade).toFixed(2).replace('.', ',')}` 
        : '-';
    const classConcluido = item.concluido ? 'item-concluido' : '';
    
    return `
        <div class="border rounded-lg p-4 mb-3 ${classConcluido} transition-all hover:shadow-md">
            <div class="flex justify-between items-start">
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                        <input type="checkbox" ${item.concluido ? 'checked' : ''} 
                               onchange="toggleConcluido(${item.id}, this.checked)"
                               class="w-5 h-5 text-green-600 rounded focus:ring-2 focus:ring-green-500 cursor-pointer">
                        <h4 class="text-lg font-semibold text-gray-800">${produtoNome}</h4>
                    </div>
                    <p class="text-sm text-gray-500 mb-2">
                        <i class="fas fa-store mr-1"></i>${fornecedorNome}
                    </p>
                    <div class="flex flex-wrap gap-4 text-sm">
                        <span class="text-gray-700">
                            <i class="fas fa-cubes mr-1 text-[#d4af37]"></i>
                            <strong>${item.quantidade}</strong> ${item.unidade || 'UNIDADE'}
                        </span>
                        <span class="text-gray-700">
                            <i class="fas fa-tag mr-1 text-green-500"></i>
                            ${precoUnitarioFormatado}
                        </span>
                        <span class="text-gray-700 font-semibold">
                            <i class="fas fa-calculator mr-1 text-blue-500"></i>
                            Total: ${valorTotal}
                        </span>
                    </div>
                    ${item.observacoes ? `<p class="text-sm text-gray-600 mt-2 italic"><i class="fas fa-comment mr-1"></i>${item.observacoes}</p>` : ''}
                    ${item.concluido ? `<p class="text-xs text-green-600 mt-2"><i class="fas fa-check mr-1"></i>Concluído em ${new Date(item.concluidoEm).toLocaleDateString('pt-BR')}</p>` : ''}
                </div>
                <div class="flex gap-2 ml-4">
                    <button onclick="editarQuantidade(${item.id}, ${item.quantidade}, '${item.unidade || 'UNIDADE'}')" 
                            class="text-blue-500 hover:text-blue-700 p-2" title="Editar quantidade">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="removerItem(${item.id})" 
                            class="text-red-500 hover:text-red-700 p-2" title="Remover item">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `;
}

function abrirModalAdicionarItem() {
    document.getElementById('input-busca-produto').value = '';
    document.getElementById('input-produto-id').value = '';
    document.getElementById('input-quantidade').value = '1';
    document.getElementById('input-unidade').value = 'UNIDADE';
    document.getElementById('input-observacoes').value = '';
    document.getElementById('produto-selecionado').classList.add('hidden');
    document.getElementById('resultados-busca-produto').classList.add('hidden');
    document.getElementById('modal-adicionar-item').classList.remove('hidden');
}

function fecharModalAdicionarItem() {
    document.getElementById('modal-adicionar-item').classList.add('hidden');
}

async function buscarProdutos(termo) {
    try {
        const response = await fetch(`/api/produtos/buscar?q=${encodeURIComponent(termo)}`);
        if (!response.ok) throw new Error('Erro ao buscar produtos');
        
        const produtos = await response.json();
        const container = document.getElementById('resultados-busca-produto');
        
        if (produtos.length === 0) {
            container.innerHTML = '<p class="p-4 text-gray-500 text-center">Nenhum produto encontrado</p>';
            container.classList.remove('hidden');
            return;
        }
        
        container.innerHTML = produtos.map(p => `
            <div onclick="selecionarProduto(${p.id}, '${p.nome.replace(/'/g, "\\'")}', ${p.preco || 0})" 
                 class="p-3 hover:bg-gray-100 cursor-pointer border-b last:border-b-0">
                <p class="font-semibold text-gray-800">${p.nome}</p>
                <p class="text-sm text-gray-500">
                    ${p.fornecedor?.nomeFantasia || 'Sem fornecedor'} 
                    ${p.preco ? `- R$ ${p.preco.toFixed(2).replace('.', ',')}` : ''}
                </p>
            </div>
        `).join('');
        
        container.classList.remove('hidden');
        
    } catch (error) {
        console.error('Erro ao buscar produtos:', error);
    }
}

function selecionarProduto(id, nome, preco) {
    document.getElementById('input-produto-id').value = id;
    document.getElementById('produto-selecionado-nome').textContent = nome;
    document.getElementById('produto-selecionado').classList.remove('hidden');
    document.getElementById('resultados-busca-produto').classList.add('hidden');
    document.getElementById('input-busca-produto').value = nome;
}

async function salvarItem(event) {
    event.preventDefault();
    
    const produtoId = document.getElementById('input-produto-id').value;
    const quantidade = parseFloat(document.getElementById('input-quantidade').value);
    const unidade = document.getElementById('input-unidade').value;
    const observacoes = document.getElementById('input-observacoes').value.trim();
    
    if (!produtoId || !quantidade || quantidade <= 0) {
        mostrarNotificacao('Selecione um produto e informe a quantidade', 'error');
        return;
    }
    
    try {
        const response = await fetch(`/api/lista-compras/${listaIdAtual}/itens`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                produtoId: parseInt(produtoId),
                quantidade,
                unidade,
                observacoes: observacoes || null
            })
        });
        
        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || 'Erro ao adicionar item');
        }
        
        mostrarNotificacao('Item adicionado com sucesso!', 'success');
        fecharModalAdicionarItem();
        await abrirDetalhesLista(listaIdAtual);
        await carregarEstatisticasResumo();
        
    } catch (error) {
        console.error('Erro ao adicionar item:', error);
        mostrarNotificacao(error.message || 'Erro ao adicionar item', 'error');
    }
}

async function toggleConcluido(itemId, concluido) {
    try {
        const response = await fetch(`/api/lista-compras/itens/${itemId}/concluir`, {
            method: 'PUT'
        });
        
        if (!response.ok) throw new Error('Erro ao atualizar status');
        
        await abrirDetalhesLista(listaIdAtual);
        await carregarEstatisticasResumo();
        
    } catch (error) {
        console.error('Erro ao atualizar status:', error);
        mostrarNotificacao('Erro ao atualizar status do item', 'error');
    }
}

async function editarQuantidade(itemId, quantidadeAtual, unidadeAtual) {
    const novaQuantidade = prompt(`Editar quantidade\nQuantidade atual: ${quantidadeAtual} ${unidadeAtual}\n\nNova quantidade:`, quantidadeAtual);
    
    if (!novaQuantidade || isNaN(novaQuantidade) || parseFloat(novaQuantidade) <= 0) return;
    
    try {
        const response = await fetch(`/api/lista-compras/itens/${itemId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                quantidade: parseFloat(novaQuantidade),
                unidade: unidadeAtual
            })
        });
        
        if (!response.ok) throw new Error('Erro ao atualizar quantidade');
        
        mostrarNotificacao('Quantidade atualizada com sucesso!', 'success');
        await abrirDetalhesLista(listaIdAtual);
        await carregarEstatisticasResumo();
        
    } catch (error) {
        console.error('Erro ao atualizar quantidade:', error);
        mostrarNotificacao('Erro ao atualizar quantidade', 'error');
    }
}

async function removerItem(itemId) {
    if (!confirm('Deseja realmente remover este item da lista?')) return;
    
    try {
        const response = await fetch(`/api/lista-compras/itens/${itemId}`, {
            method: 'DELETE'
        });
        
        if (!response.ok) throw new Error('Erro ao remover item');
        
        mostrarNotificacao('Item removido com sucesso!', 'success');
        await abrirDetalhesLista(listaIdAtual);
        await carregarEstatisticasResumo();
        
    } catch (error) {
        console.error('Erro ao remover item:', error);
        mostrarNotificacao('Erro ao remover item', 'error');
    }
}

// ==================== NAVEGAÇÃO ====================

function trocarAba(aba) {
    abaAtual = aba;
    
    // Atualizar estilo das abas
    document.getElementById('aba-ativas').className = aba === 'ativas' 
        ? 'px-6 py-3 font-semibold text-[#d4af37] border-b-2 border-[#d4af37]'
        : 'px-6 py-3 font-semibold text-gray-500 hover:text-gray-700';
    
    document.getElementById('aba-arquivadas').className = aba === 'arquivadas'
        ? 'px-6 py-3 font-semibold text-[#d4af37] border-b-2 border-[#d4af37]'
        : 'px-6 py-3 font-semibold text-gray-500 hover:text-gray-700';
    
    // Mostrar/ocultar containers
    document.getElementById('container-ativas').classList.toggle('hidden', aba !== 'ativas');
    document.getElementById('container-arquivadas').classList.toggle('hidden', aba !== 'arquivadas');
}

// ==================== UTILIDADES ====================

function mostrarNotificacao(mensagem, tipo = 'info') {
    const cores = {
        success: 'bg-green-500',
        error: 'bg-red-500',
        warning: 'bg-yellow-500',
        info: 'bg-blue-500'
    };
    
    const icones = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        warning: 'fa-exclamation-triangle',
        info: 'fa-info-circle'
    };
    
    const notificacao = document.createElement('div');
    notificacao.className = `fixed top-4 right-4 ${cores[tipo]} text-white px-6 py-4 rounded-lg shadow-lg z-50 flex items-center gap-3 animate-slide-in`;
    notificacao.innerHTML = `
        <i class="fas ${icones[tipo]} text-xl"></i>
        <span class="font-semibold">${mensagem}</span>
    `;
    
    document.body.appendChild(notificacao);
    
    setTimeout(() => {
        notificacao.style.animation = 'slide-out 0.3s ease-in';
        setTimeout(() => notificacao.remove(), 300);
    }, 3000);
}

// CSS animations inline
const style = document.createElement('style');
style.textContent = `
    @keyframes slide-in {
        from { transform: translateX(100%); opacity: 0; }
        to { transform: translateX(0); opacity: 1; }
    }
    @keyframes slide-out {
        from { transform: translateX(0); opacity: 1; }
        to { transform: translateX(100%); opacity: 0; }
    }
    .animate-slide-in {
        animation: slide-in 0.3s ease-out;
    }
`;
document.head.appendChild(style);
