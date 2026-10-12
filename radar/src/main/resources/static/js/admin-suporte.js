/**
 * admin-suporte.js
 * Painel administrativo de gerenciamento de chamados de suporte.
 * 
 * ACESSO RESTRITO: Apenas username="admin" pode acessar.
 * 
 * Funcionalidades:
 * - Visualizar todos os chamados do sistema
 * - Filtrar por status, categoria, urgência
 * - Ver estatísticas do suporte
 * - Alterar status dos chamados
 * - Responder mensagens
 * - Adicionar observações internas (visível apenas para admin)
 */

// ============================================
// ESTADO GLOBAL
// ============================================
let todosChamados = [];
let chamadoAtual = null;
let filtroAtual = {
    status: '',
    categoria: '',
    urgencia: ''
};

// ============================================
// INICIALIZAÇÃO
// ============================================
function inicializarAdminSuporte() {
    console.log('[Admin Suporte] Inicializando painel administrativo...');
    
    // Verificar se usuário é admin
    verificarPermissaoAdmin();
    
    configurarModal();
    carregarFiltros();
    carregarEstatisticas();
    carregarTodosChamados();
    
    // Verificar se há um chamado específico na URL
    const params = new URLSearchParams(window.location.search);
    const numeroChamado = params.get('chamado');
    if (numeroChamado) {
        setTimeout(() => gerenciarChamado(numeroChamado), 500);
    }
    
    // Botão atualizar
    document.getElementById('btn-atualizar')?.addEventListener('click', () => {
        carregarEstatisticas();
        carregarTodosChamados();
    });
    
    // Botão aplicar filtros
    document.getElementById('btn-aplicar-filtros')?.addEventListener('click', () => {
        aplicarFiltros();
    });
}

// ============================================
// VERIFICAR PERMISSÃO ADMIN
// ============================================
function verificarPermissaoAdmin() {
    fetch('/api/login/session')
        .then(response => response.json())
        .then(usuario => {
            if (usuario.username !== 'admin') {
                alert('Acesso negado. Apenas administradores podem acessar esta página.');
                window.location.href = '/pages/ajuda-suporte.html';
            }
        })
        .catch(error => {
            console.error('[Admin Suporte] Erro ao verificar sessão:', error);
            window.location.href = '/login.html';
        });
}

// ============================================
// CONFIGURAÇÃO DE MODAL
// ============================================
function configurarModal() {
    const modal = document.getElementById('modal-gerenciar');
    const btnFechar = document.getElementById('btn-fechar-modal');

    btnFechar?.addEventListener('click', () => fecharModal());

    modal?.addEventListener('click', (e) => {
        if (e.target === modal) {
            fecharModal();
        }
    });

    // Formulário Responder
    document.getElementById('form-responder')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await responderChamado();
    });

    // Botão Alterar Status
    document.getElementById('btn-alterar-status')?.addEventListener('click', async () => {
        await alterarStatus();
    });

    // Botão Salvar Observações
    document.getElementById('btn-salvar-observacoes')?.addEventListener('click', async () => {
        await salvarObservacoes();
    });
}

function fecharModal() {
    document.getElementById('modal-gerenciar').classList.add('hidden');
    chamadoAtual = null;
}

// ============================================
// CARREGAR FILTROS
// ============================================
async function carregarFiltros() {
    try {
        // Carregar Status
        const responseStatus = await fetch('/api/suporte/status');
        const statusList = await responseStatus.json();
        const selectStatus = document.getElementById('filtro-status');
        const selectStatusModal = document.getElementById('select-status');
        
        Object.entries(statusList).forEach(([valor, nome]) => {
            const option = document.createElement('option');
            option.value = valor;
            option.textContent = nome;
            selectStatus.appendChild(option.cloneNode(true));
            selectStatusModal.appendChild(option);
        });

        // Carregar Categorias
        const responseCat = await fetch('/api/suporte/categorias');
        const categorias = await responseCat.json();
        const selectCat = document.getElementById('filtro-categoria');
        
        Object.entries(categorias).forEach(([valor, nome]) => {
            const option = document.createElement('option');
            option.value = valor;
            option.textContent = nome;
            selectCat.appendChild(option);
        });

        // Carregar Urgências
        const responseUrg = await fetch('/api/suporte/urgencias');
        const urgencias = await responseUrg.json();
        const selectUrg = document.getElementById('filtro-urgencia');
        
        Object.entries(urgencias).forEach(([valor, nome]) => {
            const option = document.createElement('option');
            option.value = valor;
            option.textContent = nome;
            selectUrg.appendChild(option);
        });

    } catch (error) {
        console.error('[Admin Suporte] Erro ao carregar filtros:', error);
    }
}

// ============================================
// CARREGAR ESTATÍSTICAS
// ============================================
async function carregarEstatisticas() {
    try {
        const response = await fetch('/api/suporte/admin/estatisticas');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar estatísticas');
        }

        const stats = await response.json();
        
        document.getElementById('estatisticas').innerHTML = `
            <div class="bg-yellow-900/20 border border-yellow-800/50 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                    <i class="fas fa-folder-open text-2xl text-yellow-400"></i>
                    <span class="text-2xl font-black text-white">${stats.totalAbertos || 0}</span>
                </div>
                <p class="text-xs text-slate-400">Abertos</p>
            </div>
            <div class="bg-green-900/20 border border-green-800/50 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                    <i class="fas fa-check-circle text-2xl text-green-400"></i>
                    <span class="text-2xl font-black text-white">${stats.totalEncerrados || 0}</span>
                </div>
                <p class="text-xs text-slate-400">Encerrados</p>
            </div>
            <div class="bg-orange-900/20 border border-orange-800/50 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                    <i class="fas fa-exclamation-triangle text-2xl text-orange-400"></i>
                    <span class="text-2xl font-black text-white">${stats.totalUrgenciaAlta || 0}</span>
                </div>
                <p class="text-xs text-slate-400">Alta Urgência</p>
            </div>
            <div class="bg-red-900/20 border border-red-800/50 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                    <i class="fas fa-fire text-2xl text-red-400"></i>
                    <span class="text-2xl font-black text-white">${stats.totalUrgenciaCritica || 0}</span>
                </div>
                <p class="text-xs text-slate-400">Críticos</p>
            </div>
            <div class="bg-blue-900/20 border border-blue-800/50 rounded-lg p-4">
                <div class="flex items-center justify-between mb-2">
                    <i class="fas fa-envelope text-2xl text-blue-400"></i>
                    <span class="text-2xl font-black text-white">${stats.totalComNovasMensagens || 0}</span>
                </div>
                <p class="text-xs text-slate-400">Novas Mensagens</p>
            </div>
        `;

    } catch (error) {
        console.error('[Admin Suporte] Erro ao carregar estatísticas:', error);
    }
}

// ============================================
// CARREGAR TODOS OS CHAMADOS
// ============================================
async function carregarTodosChamados() {
    const loading = document.getElementById('loading');
    const empty = document.getElementById('empty');
    const lista = document.getElementById('lista-chamados');

    loading.classList.remove('hidden');
    empty.classList.add('hidden');
    lista.classList.add('hidden');

    try {
        const response = await fetch('/api/suporte/admin/chamados');
        
        if (!response.ok) {
            if (response.status === 403) {
                alert('Acesso negado. Apenas administradores podem acessar.');
                window.location.href = '/pages/ajuda-suporte.html';
                return;
            }
            throw new Error('Erro ao carregar chamados');
        }

        todosChamados = await response.json();
        console.log('[Admin Suporte] Chamados carregados:', todosChamados.length);

        loading.classList.add('hidden');

        if (todosChamados.length === 0) {
            empty.classList.remove('hidden');
        } else {
            renderizarChamados();
            lista.classList.remove('hidden');
        }

    } catch (error) {
        console.error('[Admin Suporte] Erro ao carregar chamados:', error);
        loading.classList.add('hidden');
        lista.innerHTML = `
            <div class="bg-red-900/20 border border-red-800 rounded-xl p-6 text-center">
                <i class="fas fa-exclamation-triangle text-3xl text-red-500 mb-3"></i>
                <p class="text-red-400">Erro ao carregar chamados: ${error.message}</p>
            </div>
        `;
        lista.classList.remove('hidden');
    }
}

// ============================================
// APLICAR FILTROS
// ============================================
async function aplicarFiltros() {
    filtroAtual.status = document.getElementById('filtro-status').value;
    filtroAtual.categoria = document.getElementById('filtro-categoria').value;
    filtroAtual.urgencia = document.getElementById('filtro-urgencia').value;

    const loading = document.getElementById('loading');
    const empty = document.getElementById('empty');
    const lista = document.getElementById('lista-chamados');

    loading.classList.remove('hidden');
    empty.classList.add('hidden');
    lista.classList.add('hidden');

    try {
        const params = new URLSearchParams();
        if (filtroAtual.status) params.append('status', filtroAtual.status);
        if (filtroAtual.categoria) params.append('categoria', filtroAtual.categoria);
        if (filtroAtual.urgencia) params.append('urgencia', filtroAtual.urgencia);

        const url = '/api/suporte/admin/chamados' + (params.toString() ? '?' + params.toString() : '');
        const response = await fetch(url);
        
        if (!response.ok) {
            throw new Error('Erro ao aplicar filtros');
        }

        todosChamados = await response.json();
        console.log('[Admin Suporte] Filtros aplicados. Resultados:', todosChamados.length);

        loading.classList.add('hidden');

        if (todosChamados.length === 0) {
            empty.classList.remove('hidden');
        } else {
            renderizarChamados();
            lista.classList.remove('hidden');
        }

    } catch (error) {
        console.error('[Admin Suporte] Erro ao aplicar filtros:', error);
        alert('Erro ao aplicar filtros: ' + error.message);
        loading.classList.add('hidden');
    }
}

// ============================================
// RENDERIZAR CHAMADOS
// ============================================
function renderizarChamados() {
    const lista = document.getElementById('lista-chamados');
    
    lista.innerHTML = todosChamados.map(chamado => `
        <div class="bg-slate-900/50 backdrop-blur-sm border ${getBordaUrgencia(chamado.urgencia)} rounded-xl p-6 hover:border-[#d4af37] transition-colors cursor-pointer"
             onclick="gerenciarChamado('${chamado.numeroChamado}')">
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                        <h3 class="text-lg font-bold text-white">
                            ${chamado.assunto}
                        </h3>
                        ${chamado.temNovasMensagensAdmin ? '<span class="px-2 py-0.5 text-xs font-bold bg-red-500 text-white rounded-full animate-pulse">NOVA MENSAGEM</span>' : ''}
                    </div>
                    <p class="text-sm text-slate-400">
                        <i class="fas fa-ticket mr-1 text-[#d4af37]"></i>
                        ${chamado.numeroChamado} • ${chamado.usuarioNome} (${chamado.usuarioTipo})
                    </p>
                </div>
                <div class="text-right space-y-2">
                    ${getBadgeStatus(chamado.status)}
                    ${getBadgeUrgencia(chamado.urgencia)}
                </div>
            </div>

            <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Categoria</p>
                    <p class="text-sm font-bold text-white">${formatarCategoria(chamado.categoria)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Criado</p>
                    <p class="text-sm font-bold text-white">${formatarDataRelativa(chamado.criadoEm)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Atualizado</p>
                    <p class="text-sm font-bold text-white">${chamado.atualizadoEm ? formatarDataRelativa(chamado.atualizadoEm) : 'N/A'}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Mensagens</p>
                    <p class="text-sm font-bold text-white">
                        <i class="fas fa-comments mr-1"></i>${chamado.totalMensagens || 0}
                    </p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Responsável</p>
                    <p class="text-sm font-bold text-white">${chamado.adminResponsavelNome || 'Não atribuído'}</p>
                </div>
            </div>
        </div>
    `).join('');
}

// ============================================
// GERENCIAR CHAMADO
// ============================================
async function gerenciarChamado(numeroChamado) {
    try {
        const response = await fetch(`/api/suporte/admin/chamados/${numeroChamado}`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar chamado');
        }

        chamadoAtual = await response.json();
        
        // Preencher modal
        document.getElementById('modal-titulo').textContent = chamadoAtual.assunto;
        document.getElementById('modal-numero').textContent = `Chamado: ${chamadoAtual.numeroChamado}`;
        document.getElementById('chamado-descricao').textContent = chamadoAtual.descricao;
        document.getElementById('chamado-numero-resposta').value = numeroChamado;
        document.getElementById('select-status').value = chamadoAtual.status;
        document.getElementById('textarea-observacoes').value = chamadoAtual.observacoesInternas || '';
        
        // Informações
        document.getElementById('chamado-info').innerHTML = `
            <div class="space-y-2">
                <div>
                    <p class="text-xs text-slate-500">Usuário</p>
                    <p class="text-sm font-bold text-white">${chamadoAtual.usuarioNome}</p>
                </div>
                <div>
                    <p class="text-xs text-slate-500">Tipo</p>
                    <p class="text-sm font-bold text-white">${chamadoAtual.usuarioTipo}</p>
                </div>
                ${chamadoAtual.cnpjCpf ? `
                <div>
                    <p class="text-xs text-slate-500">CNPJ/CPF</p>
                    <p class="text-sm font-bold text-white">${chamadoAtual.cnpjCpf}</p>
                </div>
                ` : ''}
                <div>
                    <p class="text-xs text-slate-500">Categoria</p>
                    <p class="text-sm font-bold text-white">${formatarCategoria(chamadoAtual.categoria)}</p>
                </div>
                <div>
                    <p class="text-xs text-slate-500">Criado em</p>
                    <p class="text-sm font-bold text-white">${formatarData(chamadoAtual.criadoEm)}</p>
                </div>
            </div>
        `;
        
        // Informações técnicas
        if (chamadoAtual.infoTecnicas) {
            document.getElementById('info-tecnicas-container').classList.remove('hidden');
            document.getElementById('chamado-info-tecnicas').textContent = chamadoAtual.infoTecnicas;
        } else {
            document.getElementById('info-tecnicas-container').classList.add('hidden');
        }
        
        // Carregar mensagens
        await carregarMensagensAdmin(numeroChamado);
        
        // Abrir modal
        document.getElementById('modal-gerenciar').classList.remove('hidden');
        
    } catch (error) {
        console.error('[Admin Suporte] Erro ao gerenciar chamado:', error);
        alert('Erro ao carregar chamado: ' + error.message);
    }
}

// ============================================
// CARREGAR MENSAGENS (ADMIN)
// ============================================
async function carregarMensagensAdmin(numeroChamado) {
    try {
        const response = await fetch(`/api/suporte/chamados/${numeroChamado}/mensagens`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar mensagens');
        }

        const mensagens = await response.json();
        const lista = document.getElementById('lista-mensagens-admin');
        
        if (mensagens.length === 0) {
            lista.innerHTML = `
                <div class="text-center py-8 text-slate-500">
                    <i class="fas fa-comments text-4xl mb-2"></i>
                    <p>Nenhuma mensagem ainda</p>
                </div>
            `;
            return;
        }
        
        lista.innerHTML = mensagens.map(msg => {
            const isAdmin = msg.autorTipo === 'ADMIN';
            const align = isAdmin ? 'items-end' : 'items-start';
            const bg = isAdmin ? 'bg-green-900/30 border-green-800' : 'bg-blue-900/30 border-blue-800';
            
            return `
                <div class="flex flex-col ${align}">
                    <div class="max-w-[80%] ${bg} border rounded-lg p-4">
                        <div class="flex items-center gap-2 mb-2">
                            <i class="fas ${isAdmin ? 'fa-user-shield' : 'fa-user'} text-[#d4af37]"></i>
                            <span class="text-sm font-bold text-white">${msg.autorNome || (isAdmin ? 'Admin' : 'Usuário')}</span>
                            <span class="text-xs text-slate-500">${formatarDataHora(msg.criadaEm)}</span>
                        </div>
                        <p class="text-sm text-slate-300 whitespace-pre-wrap">${msg.mensagem}</p>
                    </div>
                </div>
            `;
        }).join('');
        
        // Scroll para última mensagem
        lista.scrollTop = lista.scrollHeight;
        
    } catch (error) {
        console.error('[Admin Suporte] Erro ao carregar mensagens:', error);
    }
}

// ============================================
// RESPONDER CHAMADO
// ============================================
async function responderChamado() {
    const numeroChamado = document.getElementById('chamado-numero-resposta').value;
    const mensagemTexto = document.getElementById('input-resposta').value.trim();
    
    if (!mensagemTexto) {
        alert('Digite uma resposta');
        return;
    }
    
    try {
        const response = await fetch(`/api/suporte/admin/chamados/${numeroChamado}/mensagens`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensagem: mensagemTexto })
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao enviar resposta');
        }
        
        document.getElementById('input-resposta').value = '';
        await carregarMensagensAdmin(numeroChamado);
        
        mostrarNotificacao('Resposta enviada!', 'success');
        
    } catch (error) {
        console.error('[Admin Suporte] Erro ao enviar resposta:', error);
        alert('Erro ao enviar resposta: ' + error.message);
    }
}

// ============================================
// ALTERAR STATUS
// ============================================
async function alterarStatus() {
    if (!chamadoAtual) return;
    
    const novoStatus = document.getElementById('select-status').value;
    
    if (!novoStatus) {
        alert('Selecione um status');
        return;
    }
    
    try {
        const response = await fetch(`/api/suporte/admin/chamados/${chamadoAtual.numeroChamado}/status`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ status: novoStatus })
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao alterar status');
        }
        
        mostrarNotificacao('Status alterado com sucesso!', 'success');
        
        // Atualizar chamado atual
        chamadoAtual.status = novoStatus;
        
        // Recarregar listas
        carregarEstatisticas();
        carregarTodosChamados();
        
    } catch (error) {
        console.error('[Admin Suporte] Erro ao alterar status:', error);
        alert('Erro ao alterar status: ' + error.message);
    }
}

// ============================================
// SALVAR OBSERVAÇÕES
// ============================================
async function salvarObservacoes() {
    if (!chamadoAtual) return;
    
    const observacoes = document.getElementById('textarea-observacoes').value;
    
    try {
        const response = await fetch(`/api/suporte/admin/chamados/${chamadoAtual.numeroChamado}/observacoes`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ observacoes })
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao salvar observações');
        }
        
        mostrarNotificacao('Observações salvas com sucesso!', 'success');
        
    } catch (error) {
        console.error('[Admin Suporte] Erro ao salvar observações:', error);
        alert('Erro ao salvar observações: ' + error.message);
    }
}

// ============================================
// FUNÇÕES UTILITÁRIAS
// ============================================
function formatarData(dataString) {
    if (!dataString) return 'N/A';
    const data = new Date(dataString);
    return data.toLocaleDateString('pt-BR');
}

function formatarDataHora(dataString) {
    if (!dataString) return 'N/A';
    const data = new Date(dataString);
    return data.toLocaleString('pt-BR');
}

function formatarDataRelativa(dataString) {
    if (!dataString) return 'Agora';
    
    const agora = new Date();
    const data = new Date(dataString);
    const diffMs = agora - data;
    const diffMinutos = Math.floor(diffMs / 60000);
    
    if (diffMinutos < 1) return 'Agora';
    if (diffMinutos < 60) return `Há ${diffMinutos} min`;
    
    const diffHoras = Math.floor(diffMinutos / 60);
    if (diffHoras < 24) return `Há ${diffHoras}h`;
    
    const diffDias = Math.floor(diffHoras / 24);
    if (diffDias < 7) return `Há ${diffDias} dia${diffDias > 1 ? 's' : ''}`;
    
    return formatarData(dataString);
}

function formatarCategoria(categoria) {
    const categorias = {
        'ACESSO_CONTA': 'Acesso à conta',
        'CADASTRO': 'Cadastro',
        'PRODUTOS': 'Produtos',
        'PRECOS': 'Preços',
        'PERFIL': 'Perfil',
        'MENSAGENS': 'Mensagens',
        'ORCAMENTOS': 'Orçamentos',
        'DENUNCIAS': 'Denúncias',
        'ERRO_TECNICO': 'Erro técnico',
        'NOTIFICACOES': 'Notificações',
        'FAVORITOS_LISTAS': 'Favoritos/Listas',
        'BUSCA_FILTROS': 'Busca/Filtros',
        'PAGAMENTOS': 'Pagamentos',
        'OUTROS': 'Outros'
    };
    return categorias[categoria] || categoria;
}

function getBadgeStatus(status) {
    const badges = {
        'ABERTO': '<span class="px-2 py-1 text-xs font-bold bg-yellow-600 text-white rounded">Aberto</span>',
        'EM_ATENDIMENTO': '<span class="px-2 py-1 text-xs font-bold bg-blue-600 text-white rounded">Em atendimento</span>',
        'AGUARDANDO_USUARIO': '<span class="px-2 py-1 text-xs font-bold bg-orange-600 text-white rounded">Aguardando usuário</span>',
        'AGUARDANDO_ADMIN': '<span class="px-2 py-1 text-xs font-bold bg-purple-600 text-white rounded">Aguardando admin</span>',
        'RESOLVIDO': '<span class="px-2 py-1 text-xs font-bold bg-green-600 text-white rounded">Resolvido</span>',
        'ENCERRADO': '<span class="px-2 py-1 text-xs font-bold bg-gray-600 text-white rounded">Encerrado</span>'
    };
    return badges[status] || status;
}

function getBadgeUrgencia(urgencia) {
    const badges = {
        'BAIXA': '<span class="px-2 py-1 text-xs font-bold bg-slate-700 text-white rounded">Baixa</span>',
        'NORMAL': '<span class="px-2 py-1 text-xs font-bold bg-blue-700 text-white rounded">Normal</span>',
        'ALTA': '<span class="px-2 py-1 text-xs font-bold bg-orange-600 text-white rounded">Alta</span>',
        'CRITICA': '<span class="px-2 py-1 text-xs font-bold bg-red-600 text-white rounded animate-pulse">Crítica</span>'
    };
    return badges[urgencia] || urgencia;
}

function getBordaUrgencia(urgencia) {
    const bordas = {
        'BAIXA': 'border-slate-800',
        'NORMAL': 'border-blue-800',
        'ALTA': 'border-orange-800',
        'CRITICA': 'border-red-800'
    };
    return bordas[urgencia] || 'border-slate-800';
}

function mostrarNotificacao(mensagem, tipo = 'info') {
    const classe = tipo === 'success' ? 'bg-green-500' : tipo === 'error' ? 'bg-red-500' : 'bg-blue-500';
    
    const notif = document.createElement('div');
    notif.className = `fixed top-4 right-4 ${classe} text-white px-6 py-3 rounded-lg shadow-xl z-50`;
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
window.inicializarAdminSuporte = inicializarAdminSuporte;
window.gerenciarChamado = gerenciarChamado;

console.log('[Admin Suporte] Módulo admin-suporte.js carregado');
