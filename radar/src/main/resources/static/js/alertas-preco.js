/**
 * alertas-preco.js
 * Sistema completo de gerenciamento de Alertas de Preço
 * 
 * Funcionalidades:
 * - Criar, editar, excluir alertas
 * - Listar alertas ativos e disparados
 * - Buscar e visualizar histórico de preços
 * - Autocomplete de produtos
 */

// ============================================
// ESTADO GLOBAL
// ============================================
let alertasAtivos = [];
let alertasDisparados = [];
let produtoSelecionado = null;
let alertaEditando = null;

// ============================================
// INICIALIZAÇÃO
// ============================================
function inicializarAlertasPreco() {
    console.log('[Alertas] Inicializando sistema de alertas de preço...');
    
    configurarTabs();
    configurarModal();
    configurarBuscaProduto();
    configurarFormulario();
    
    carregarAlertasAtivos();
}

// ============================================
// CONFIGURAÇÃO DE TABS
// ============================================
function configurarTabs() {
    const tabs = {
        'tab-ativos': 'content-ativos',
        'tab-disparados': 'content-disparados',
        'tab-historico': 'content-historico'
    };

    Object.keys(tabs).forEach(tabId => {
        document.getElementById(tabId)?.addEventListener('click', () => {
            // Desativar todos os tabs
            document.querySelectorAll('.tab-button').forEach(btn => {
                btn.classList.remove('border-[#d4af37]', 'text-white');
                btn.classList.add('border-transparent', 'text-slate-400');
            });

            // Ativar tab clicado
            const tabBtn = document.getElementById(tabId);
            tabBtn.classList.add('border-[#d4af37]', 'text-white');
            tabBtn.classList.remove('border-transparent', 'text-slate-400');

            // Esconder todos os conteúdos
            document.querySelectorAll('.tab-content').forEach(content => {
                content.classList.add('hidden');
            });

            // Mostrar conteúdo do tab
            const contentId = tabs[tabId];
            document.getElementById(contentId).classList.remove('hidden');

            // Carregar dados específicos do tab
            if (tabId === 'tab-ativos') {
                carregarAlertasAtivos();
            } else if (tabId === 'tab-disparados') {
                carregarAlertasDisparados();
            }
        });
    });
}

// ============================================
// CONFIGURAÇÃO DE MODAL
// ============================================
function configurarModal() {
    const modal = document.getElementById('modal-alerta');
    const btnAbrir = document.getElementById('btn-novo-alerta');
    const btnAbrirMobile = document.getElementById('btn-novo-alerta-mobile');
    const btnFechar = document.getElementById('btn-fechar-modal');
    const btnCancelar = document.getElementById('btn-cancelar-modal');

    [btnAbrir, btnAbrirMobile].forEach(btn => {
        btn?.addEventListener('click', () => abrirModal());
    });

    [btnFechar, btnCancelar].forEach(btn => {
        btn?.addEventListener('click', () => fecharModal());
    });

    // Fechar ao clicar fora
    modal?.addEventListener('click', (e) => {
        if (e.target === modal) {
            fecharModal();
        }
    });
}

function abrirModal(alerta = null) {
    alertaEditando = alerta;
    const modal = document.getElementById('modal-alerta');
    const titulo = document.getElementById('modal-titulo');
    
    if (alerta) {
        titulo.innerHTML = '<i class="fas fa-edit text-[#d4af37] mr-2"></i>Editar Alerta';
        preencherFormulario(alerta);
    } else {
        titulo.innerHTML = '<i class="fas fa-bell text-[#d4af37] mr-2"></i>Novo Alerta de Preço';
        limparFormulario();
    }
    
    modal.classList.remove('hidden');
}

function fecharModal() {
    document.getElementById('modal-alerta').classList.add('hidden');
    limparFormulario();
    alertaEditando = null;
}

// ============================================
// CONFIGURAÇÃO DE BUSCA DE PRODUTO
// ============================================
function configurarBuscaProduto() {
    const input = document.getElementById('input-buscar-produto');
    const dropdown = document.getElementById('dropdown-produtos');
    let timeoutBusca = null;

    input?.addEventListener('input', (e) => {
        const termo = e.target.value.trim();
        
        clearTimeout(timeoutBusca);
        
        if (termo.length < 2) {
            dropdown.classList.add('hidden');
            return;
        }

        timeoutBusca = setTimeout(() => {
            buscarProdutos(termo);
        }, 300);
    });

    // Remover produto selecionado
    document.getElementById('btn-remover-produto')?.addEventListener('click', () => {
        produtoSelecionado = null;
        document.getElementById('produto-selecionado').classList.add('hidden');
        document.getElementById('input-buscar-produto').value = '';
        document.getElementById('produto-id').value = '';
    });
}

async function buscarProdutos(termo) {
    const dropdown = document.getElementById('dropdown-produtos');
    
    try {
        const response = await fetch(`/api/produtos/buscar?termo=${encodeURIComponent(termo)}`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar produtos');
        }

        const produtos = await response.json();
        renderizarDropdownProdutos(produtos);
        
    } catch (error) {
        console.error('[Alertas] Erro ao buscar produtos:', error);
        dropdown.innerHTML = `
            <div class="p-4 text-center text-red-400">
                <i class="fas fa-exclamation-triangle mr-2"></i>
                Erro ao buscar produtos
            </div>
        `;
        dropdown.classList.remove('hidden');
    }
}

function renderizarDropdownProdutos(produtos) {
    const dropdown = document.getElementById('dropdown-produtos');
    
    if (produtos.length === 0) {
        dropdown.innerHTML = `
            <div class="p-4 text-center text-slate-400">
                <i class="fas fa-search mr-2"></i>
                Nenhum produto encontrado
            </div>
        `;
        dropdown.classList.remove('hidden');
        return;
    }

    dropdown.innerHTML = produtos.map(produto => `
        <div class="produto-item p-4 hover:bg-slate-700 cursor-pointer border-b border-slate-700 last:border-b-0"
             onclick="selecionarProduto(${produto.id}, '${produto.nome.replace(/'/g, "\\'")}', ${produto.preco || 0})">
            <div class="flex items-center justify-between">
                <div class="flex-1">
                    <p class="font-bold text-white">${produto.nome}</p>
                    <p class="text-sm text-slate-400">${produto.categoria || 'Sem categoria'}</p>
                </div>
                <div class="text-right">
                    <p class="font-bold text-[#d4af37]">R$ ${(produto.preco || 0).toFixed(2)}</p>
                    ${produto.precoPromocional ? `<p class="text-xs text-green-400">Promoção: R$ ${produto.precoPromocional.toFixed(2)}</p>` : ''}
                </div>
            </div>
        </div>
    `).join('');
    
    dropdown.classList.remove('hidden');
}

function selecionarProduto(id, nome, preco) {
    produtoSelecionado = { id, nome, preco };
    
    document.getElementById('produto-id').value = id;
    document.getElementById('input-buscar-produto').value = '';
    document.getElementById('dropdown-produtos').classList.add('hidden');
    
    // Mostrar produto selecionado
    document.getElementById('produto-nome-selecionado').textContent = nome;
    document.getElementById('produto-preco-atual').textContent = `Preço atual: R$ ${preco.toFixed(2)}`;
    document.getElementById('produto-selecionado').classList.remove('hidden');
    
    // Sugerir preço alvo (10% abaixo do preço atual)
    const precoSugerido = (preco * 0.9).toFixed(2);
    document.getElementById('input-preco-alvo').value = precoSugerido;
}

// ============================================
// CONFIGURAÇÃO DE FORMULÁRIO
// ============================================
function configurarFormulario() {
    const form = document.getElementById('form-alerta');
    
    form?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await salvarAlerta();
    });
}

function preencherFormulario(alerta) {
    document.getElementById('alerta-id').value = alerta.id;
    document.getElementById('input-preco-alvo').value = alerta.precoAlvo;
    document.getElementById('input-criterio').value = alerta.criterio;
    document.getElementById('input-notas').value = alerta.notas || '';
    document.getElementById('input-ativo').checked = alerta.ativo;
    
    // Produto selecionado (simulado - buscar dados reais do produto)
    produtoSelecionado = {
        id: alerta.produtoId,
        nome: 'Produto #' + alerta.produtoId,
        preco: alerta.precoAlvo
    };
    
    document.getElementById('produto-id').value = alerta.produtoId;
    document.getElementById('produto-nome-selecionado').textContent = produtoSelecionado.nome;
    document.getElementById('produto-preco-atual').textContent = `ID do produto: ${alerta.produtoId}`;
    document.getElementById('produto-selecionado').classList.remove('hidden');
}

function limparFormulario() {
    document.getElementById('form-alerta').reset();
    document.getElementById('alerta-id').value = '';
    document.getElementById('produto-id').value = '';
    document.getElementById('produto-selecionado').classList.add('hidden');
    document.getElementById('input-ativo').checked = true;
    produtoSelecionado = null;
}

// ============================================
// SALVAR ALERTA
// ============================================
async function salvarAlerta() {
    const alertaId = document.getElementById('alerta-id').value;
    const produtoId = document.getElementById('produto-id').value;
    const precoAlvo = document.getElementById('input-preco-alvo').value;
    const criterio = document.getElementById('input-criterio').value;
    const notas = document.getElementById('input-notas').value;
    const ativo = document.getElementById('input-ativo').checked;

    // Validações
    if (!produtoId) {
        alert('Por favor, selecione um produto');
        return;
    }

    if (!precoAlvo || parseFloat(precoAlvo) <= 0) {
        alert('Por favor, informe um preço-alvo válido');
        return;
    }

    const dados = {
        produtoId: parseInt(produtoId),
        precoAlvo: parseFloat(precoAlvo),
        criterio,
        notas: notas || null,
        ativo
    };

    const btnSalvar = document.getElementById('btn-salvar-alerta');
    const textoOriginal = btnSalvar.innerHTML;
    btnSalvar.disabled = true;
    btnSalvar.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Salvando...';

    try {
        let response;
        
        if (alertaId) {
            // Editar alerta existente
            response = await fetch(`/api/alertas-preco/${alertaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dados)
            });
        } else {
            // Criar novo alerta
            response = await fetch('/api/alertas-preco', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dados)
            });
        }

        if (!response.ok) {
            const erro = await response.text();
            throw new Error(erro || 'Erro ao salvar alerta');
        }

        const resultado = await response.json();
        console.log('[Alertas] Alerta salvo com sucesso:', resultado);

        fecharModal();
        carregarAlertasAtivos();
        
        // Mostrar mensagem de sucesso
        mostrarNotificacao(alertaId ? 'Alerta atualizado com sucesso!' : 'Alerta criado com sucesso!', 'success');

    } catch (error) {
        console.error('[Alertas] Erro ao salvar alerta:', error);
        alert('Erro ao salvar alerta: ' + error.message);
    } finally {
        btnSalvar.disabled = false;
        btnSalvar.innerHTML = textoOriginal;
    }
}

// ============================================
// CARREGAR ALERTAS ATIVOS
// ============================================
async function carregarAlertasAtivos() {
    const loading = document.getElementById('loading-ativos');
    const empty = document.getElementById('empty-ativos');
    const lista = document.getElementById('lista-alertas-ativos');
    const badge = document.getElementById('badge-ativos');

    loading.classList.remove('hidden');
    empty.classList.add('hidden');
    lista.classList.add('hidden');

    try {
        const response = await fetch('/api/alertas-preco?ativos=true');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar alertas ativos');
        }

        alertasAtivos = await response.json();
        console.log('[Alertas] Alertas ativos carregados:', alertasAtivos.length);

        loading.classList.add('hidden');

        if (alertasAtivos.length === 0) {
            empty.classList.remove('hidden');
            badge.textContent = '0';
        } else {
            renderizarAlertasAtivos();
            lista.classList.remove('hidden');
            badge.textContent = alertasAtivos.length;
        }

    } catch (error) {
        console.error('[Alertas] Erro ao carregar alertas ativos:', error);
        loading.classList.add('hidden');
        lista.innerHTML = `
            <div class="bg-red-900/20 border border-red-800 rounded-xl p-6 text-center">
                <i class="fas fa-exclamation-triangle text-3xl text-red-500 mb-3"></i>
                <p class="text-red-400">Erro ao carregar alertas: ${error.message}</p>
            </div>
        `;
        lista.classList.remove('hidden');
    }
}

function renderizarAlertasAtivos() {
    const lista = document.getElementById('lista-alertas-ativos');
    
    lista.innerHTML = alertasAtivos.map(alerta => `
        <div class="bg-slate-900/50 backdrop-blur-sm border border-slate-800 rounded-xl p-6 hover:border-[#d4af37] transition-colors">
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <h3 class="text-lg font-bold text-white mb-1">
                        Produto ID: ${alerta.produtoId}
                    </h3>
                    <p class="text-sm text-slate-400">
                        <i class="fas fa-tag mr-1 text-[#d4af37]"></i>
                        Alerta criado em ${formatarDataHora(alerta.criadoEm)}
                    </p>
                </div>
                <div class="flex gap-2">
                    <button onclick="editarAlerta(${alerta.id})" 
                            class="px-3 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg transition-colors"
                            title="Editar alerta">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button onclick="excluirAlerta(${alerta.id})" 
                            class="px-3 py-2 bg-red-900/30 hover:bg-red-900/50 text-red-400 rounded-lg transition-colors"
                            title="Excluir alerta">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
                <div class="bg-slate-800/50 rounded-lg p-4">
                    <p class="text-xs text-slate-500 mb-1">Preço Alvo</p>
                    <p class="text-xl font-bold text-[#d4af37]">R$ ${parseFloat(alerta.precoAlvo).toFixed(2)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-4">
                    <p class="text-xs text-slate-500 mb-1">Condição</p>
                    <p class="text-sm font-bold text-white">${formatarCriterio(alerta.criterio)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-4">
                    <p class="text-xs text-slate-500 mb-1">Status</p>
                    <p class="text-sm font-bold ${alerta.ativo ? 'text-green-400' : 'text-slate-500'}">
                        <i class="fas ${alerta.ativo ? 'fa-bell-on' : 'fa-bell-slash'} mr-1"></i>
                        ${alerta.ativo ? 'Ativo' : 'Inativo'}
                    </p>
                </div>
            </div>

            ${alerta.notas ? `
                <div class="bg-slate-800/30 rounded-lg p-3 mb-4">
                    <p class="text-xs text-slate-500 mb-1">
                        <i class="fas fa-sticky-note mr-1"></i>
                        Notas
                    </p>
                    <p class="text-sm text-slate-300">${alerta.notas}</p>
                </div>
            ` : ''}

            ${alerta.ultimaVerificacao ? `
                <p class="text-xs text-slate-500">
                    <i class="fas fa-clock mr-1"></i>
                    Última verificação: ${formatarDataHora(alerta.ultimaVerificacao)}
                </p>
            ` : ''}
        </div>
    `).join('');
}

// ============================================
// CARREGAR ALERTAS DISPARADOS
// ============================================
async function carregarAlertasDisparados() {
    const loading = document.getElementById('loading-disparados');
    const empty = document.getElementById('empty-disparados');
    const lista = document.getElementById('lista-alertas-disparados');
    const badge = document.getElementById('badge-disparados');

    loading.classList.remove('hidden');
    empty.classList.add('hidden');
    lista.classList.add('hidden');

    try {
        const response = await fetch('/api/alertas-preco?disparados=true');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar alertas disparados');
        }

        alertasDisparados = await response.json();
        console.log('[Alertas] Alertas disparados carregados:', alertasDisparados.length);

        loading.classList.add('hidden');

        if (alertasDisparados.length === 0) {
            empty.classList.remove('hidden');
            badge.textContent = '0';
        } else {
            renderizarAlertasDisparados();
            lista.classList.remove('hidden');
            badge.textContent = alertasDisparados.length;
        }

    } catch (error) {
        console.error('[Alertas] Erro ao carregar alertas disparados:', error);
        loading.classList.add('hidden');
        lista.innerHTML = `
            <div class="bg-red-900/20 border border-red-800 rounded-xl p-6 text-center">
                <i class="fas fa-exclamation-triangle text-3xl text-red-500 mb-3"></i>
                <p class="text-red-400">Erro ao carregar alertas: ${error.message}</p>
            </div>
        `;
        lista.classList.remove('hidden');
    }
}

function renderizarAlertasDisparados() {
    const lista = document.getElementById('lista-alertas-disparados');
    
    lista.innerHTML = alertasDisparados.map(alerta => `
        <div class="bg-green-900/10 backdrop-blur-sm border border-green-800/50 rounded-xl p-6">
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                        <i class="fas fa-check-circle text-green-400 text-xl"></i>
                        <h3 class="text-lg font-bold text-white">
                            Produto ID: ${alerta.produtoId}
                        </h3>
                    </div>
                    <p class="text-sm text-slate-400">
                        <i class="fas fa-bell mr-1 text-green-400"></i>
                        Alerta disparado em ${formatarDataHora(alerta.disparadoEm)}
                    </p>
                </div>
                <button onclick="excluirAlerta(${alerta.id})" 
                        class="px-3 py-2 bg-red-900/30 hover:bg-red-900/50 text-red-400 rounded-lg transition-colors"
                        title="Excluir alerta">
                    <i class="fas fa-trash"></i>
                </button>
            </div>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div class="bg-slate-800/50 rounded-lg p-4">
                    <p class="text-xs text-slate-500 mb-1">Preço Alvo</p>
                    <p class="text-xl font-bold text-[#d4af37]">R$ ${parseFloat(alerta.precoAlvo).toFixed(2)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-4">
                    <p class="text-xs text-slate-500 mb-1">Preço Atingido</p>
                    <p class="text-xl font-bold text-green-400">R$ ${parseFloat(alerta.precoDisparado).toFixed(2)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-4">
                    <p class="text-xs text-slate-500 mb-1">Economia</p>
                    <p class="text-xl font-bold text-green-400">
                        R$ ${(parseFloat(alerta.precoAlvo) - parseFloat(alerta.precoDisparado)).toFixed(2)}
                    </p>
                </div>
            </div>

            ${alerta.notas ? `
                <div class="bg-slate-800/30 rounded-lg p-3 mt-4">
                    <p class="text-xs text-slate-500 mb-1">
                        <i class="fas fa-sticky-note mr-1"></i>
                        Notas
                    </p>
                    <p class="text-sm text-slate-300">${alerta.notas}</p>
                </div>
            ` : ''}
        </div>
    `).join('');
}

// ============================================
// EDITAR E EXCLUIR ALERTA
// ============================================
async function editarAlerta(id) {
    const alerta = alertasAtivos.find(a => a.id === id) || alertasDisparados.find(a => a.id === id);
    
    if (!alerta) {
        alert('Alerta não encontrado');
        return;
    }
    
    abrirModal(alerta);
}

async function excluirAlerta(id) {
    if (!confirm('Tem certeza que deseja excluir este alerta?')) {
        return;
    }

    try {
        const response = await fetch(`/api/alertas-preco/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) {
            throw new Error('Erro ao excluir alerta');
        }

        console.log('[Alertas] Alerta excluído com sucesso:', id);
        
        // Recarregar listas
        carregarAlertasAtivos();
        carregarAlertasDisparados();
        
        mostrarNotificacao('Alerta excluído com sucesso!', 'success');

    } catch (error) {
        console.error('[Alertas] Erro ao excluir alerta:', error);
        alert('Erro ao excluir alerta: ' + error.message);
    }
}

// ============================================
// HISTÓRICO DE PREÇOS
// ============================================
document.getElementById('btn-buscar-historico')?.addEventListener('click', () => {
    const termo = document.getElementById('input-produto-historico').value.trim();
    
    if (!termo) {
        alert('Digite o nome de um produto para buscar o histórico');
        return;
    }
    
    // Por enquanto, apenas mostrar mensagem
    alert('Funcionalidade de histórico de preços será implementada em breve.\nBuscar por: ' + termo);
});

// ============================================
// FUNÇÕES UTILITÁRIAS
// ============================================
function formatarDataHora(dataString) {
    if (!dataString) return 'N/A';
    
    const data = new Date(dataString);
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    const hora = String(data.getHours()).padStart(2, '0');
    const minuto = String(data.getMinutes()).padStart(2, '0');
    
    return `${dia}/${mes}/${ano} às ${hora}:${minuto}`;
}

function formatarCriterio(criterio) {
    const criterios = {
        'MENOR_OU_IGUAL': '≤ Menor ou igual',
        'MENOR': '< Menor que',
        'IGUAL': '= Igual a'
    };
    
    return criterios[criterio] || criterio;
}

function mostrarNotificacao(mensagem, tipo = 'info') {
    // Implementação simples - pode ser melhorada com biblioteca de toast
    const classe = tipo === 'success' ? 'bg-green-500' : tipo === 'error' ? 'bg-red-500' : 'bg-blue-500';
    
    const notif = document.createElement('div');
    notif.className = `fixed top-4 right-4 ${classe} text-white px-6 py-3 rounded-lg shadow-xl z-50 animate-fade-in`;
    notif.innerHTML = `
        <i class="fas ${tipo === 'success' ? 'fa-check-circle' : 'fa-info-circle'} mr-2"></i>
        ${mensagem}
    `;
    
    document.body.appendChild(notif);
    
    setTimeout(() => {
        notif.remove();
    }, 3000);
}

// Expor funções globalmente
window.inicializarAlertasPreco = inicializarAlertasPreco;
window.selecionarProduto = selecionarProduto;
window.editarAlerta = editarAlerta;
window.excluirAlerta = excluirAlerta;

console.log('[Alertas] Módulo alertas-preco.js carregado');
