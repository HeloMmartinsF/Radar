/**
 * suporte.js
 * Sistema de Central de Ajuda e Suporte para usuários.
 * 
 * Funcionalidades:
 * - Criar chamados de suporte
 * - Listar meus chamados (abertos e encerrados)
 * - Visualizar detalhes e histórico de mensagens
 * - Enviar mensagens para o suporte
 * - Reabrir chamados encerrados
 */

// ============================================
// ESTADO GLOBAL
// ============================================
let chamadosAbertos = [];
let chamadosEncerrados = [];
let chamadoAtual = null;

// ============================================
// INICIALIZAÇÃO
// ============================================
function inicializarSuporte() {
    console.log('[Suporte] Inicializando sistema de suporte...');
    
    configurarTabs();
    configurarModais();
    carregarCategorias();
    carregarUrgencias();
    carregarChamadosAbertos();
    
    // Verificar se há um chamado específico na URL
    const params = new URLSearchParams(window.location.search);
    const numeroChamado = params.get('chamado');
    if (numeroChamado) {
        setTimeout(() => visualizarChamado(numeroChamado), 500);
    }
}

// ============================================
// CONFIGURAÇÃO DE TABS
// ============================================
function configurarTabs() {
    const tabs = {
        'tab-abertos': 'content-abertos',
        'tab-encerrados': 'content-encerrados'
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
            if (tabId === 'tab-abertos') {
                carregarChamadosAbertos();
            } else if (tabId === 'tab-encerrados') {
                carregarChamadosEncerrados();
            }
        });
    });
}

// ============================================
// CONFIGURAÇÃO DE MODAIS
// ============================================
function configurarModais() {
    // Modal Criar Chamado
    const modalCriar = document.getElementById('modal-criar-chamado');
    const btnAbrir = document.getElementById('btn-novo-chamado');
    const btnAbrirMobile = document.getElementById('btn-novo-chamado-mobile');
    const btnFechar = document.getElementById('btn-fechar-modal-criar');
    const btnCancelar = document.getElementById('btn-cancelar-modal-criar');

    [btnAbrir, btnAbrirMobile].forEach(btn => {
        btn?.addEventListener('click', () => abrirModalCriarChamado());
    });

    [btnFechar, btnCancelar].forEach(btn => {
        btn?.addEventListener('click', () => fecharModalCriarChamado());
    });

    // Fechar ao clicar fora
    modalCriar?.addEventListener('click', (e) => {
        if (e.target === modalCriar) {
            fecharModalCriarChamado();
        }
    });

    // Modal Visualizar Chamado
    const modalVer = document.getElementById('modal-ver-chamado');
    const btnFecharVer = document.getElementById('btn-fechar-modal-ver');

    btnFecharVer?.addEventListener('click', () => fecharModalVerChamado());

    modalVer?.addEventListener('click', (e) => {
        if (e.target === modalVer) {
            fecharModalVerChamado();
        }
    });

    // Formulário Criar Chamado
    document.getElementById('form-criar-chamado')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await criarChamado();
    });

    // Formulário Enviar Mensagem
    document.getElementById('form-enviar-mensagem')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        await enviarMensagem();
    });

    // Botão Reabrir
    document.getElementById('btn-reabrir-chamado')?.addEventListener('click', async () => {
        await reabrirChamado();
    });
}

function abrirModalCriarChamado() {
    document.getElementById('modal-criar-chamado').classList.remove('hidden');
    document.getElementById('form-criar-chamado').reset();
}

function fecharModalCriarChamado() {
    document.getElementById('modal-criar-chamado').classList.add('hidden');
}

function fecharModalVerChamado() {
    document.getElementById('modal-ver-chamado').classList.add('hidden');
    chamadoAtual = null;
}

// ============================================
// CARREGAR CATEGORIAS E URGÊNCIAS
// ============================================
async function carregarCategorias() {
    try {
        const response = await fetch('/api/suporte/categorias');
        if (!response.ok) throw new Error('Erro ao carregar categorias');

        const categorias = await response.json();
        const select = document.getElementById('input-categoria');

        Object.entries(categorias).forEach(([valor, nome]) => {
            const option = document.createElement('option');
            option.value = valor;
            option.textContent = nome;
            select.appendChild(option);
        });

    } catch (error) {
        console.error('[Suporte] Erro ao carregar categorias:', error);
    }
}

async function carregarUrgencias() {
    try {
        const response = await fetch('/api/suporte/urgencias');
        if (!response.ok) throw new Error('Erro ao carregar urgências');

        const urgencias = await response.json();
        const select = document.getElementById('input-urgencia');

        Object.entries(urgencias).forEach(([valor, nome]) => {
            const option = document.createElement('option');
            option.value = valor;
            option.textContent = nome;
            select.appendChild(option);
        });

    } catch (error) {
        console.error('[Suporte] Erro ao carregar urgências:', error);
    }
}

// ============================================
// CRIAR CHAMADO
// ============================================
async function criarChamado() {
    const assunto = document.getElementById('input-assunto').value.trim();
    const categoria = document.getElementById('input-categoria').value;
    const urgencia = document.getElementById('input-urgencia').value;
    const descricao = document.getElementById('input-descricao').value.trim();

    if (!assunto || !categoria || !urgencia || !descricao) {
        alert('Por favor, preencha todos os campos obrigatórios');
        return;
    }

    // Capturar informações técnicas
    const infoTecnicas = `Navegador: ${navigator.userAgent}, Tela: ${window.screen.width}x${window.screen.height}`;

    const dados = {
        assunto,
        categoria,
        urgencia,
        descricao,
        infoTecnicas
    };

    const btnSalvar = document.getElementById('btn-salvar-chamado');
    const textoOriginal = btnSalvar.innerHTML;
    btnSalvar.disabled = true;
    btnSalvar.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Enviando...';

    try {
        const response = await fetch('/api/suporte/chamados', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dados)
        });

        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao criar chamado');
        }

        const chamado = await response.json();
        console.log('[Suporte] Chamado criado:', chamado.numeroChamado);

        fecharModalCriarChamado();
        carregarChamadosAbertos();
        
        mostrarNotificacao(`Chamado ${chamado.numeroChamado} criado com sucesso!`, 'success');

    } catch (error) {
        console.error('[Suporte] Erro ao criar chamado:', error);
        alert('Erro ao criar chamado: ' + error.message);
    } finally {
        btnSalvar.disabled = false;
        btnSalvar.innerHTML = textoOriginal;
    }
}

// ============================================
// CARREGAR CHAMADOS ABERTOS
// ============================================
async function carregarChamadosAbertos() {
    const loading = document.getElementById('loading-abertos');
    const empty = document.getElementById('empty-abertos');
    const lista = document.getElementById('lista-chamados-abertos');
    const badge = document.getElementById('badge-abertos');

    loading.classList.remove('hidden');
    empty.classList.add('hidden');
    lista.classList.add('hidden');

    try {
        const response = await fetch('/api/suporte/chamados');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar chamados');
        }

        const todosChamados = await response.json();
        
        // Filtrar apenas não encerrados
        chamadosAbertos = todosChamados.filter(c => 
            c.status !== 'ENCERRADO' && c.status !== 'RESOLVIDO'
        );

        console.log('[Suporte] Chamados abertos carregados:', chamadosAbertos.length);

        loading.classList.add('hidden');

        if (chamadosAbertos.length === 0) {
            empty.classList.remove('hidden');
            badge.textContent = '0';
        } else {
            renderizarChamadosAbertos();
            lista.classList.remove('hidden');
            badge.textContent = chamadosAbertos.length;
        }

    } catch (error) {
        console.error('[Suporte] Erro ao carregar chamados:', error);
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

function renderizarChamadosAbertos() {
    const lista = document.getElementById('lista-chamados-abertos');
    
    lista.innerHTML = chamadosAbertos.map(chamado => `
        <div class="bg-slate-900/50 backdrop-blur-sm border ${getBordaStatus(chamado.status)} rounded-xl p-6 hover:border-[#d4af37] transition-colors cursor-pointer"
             onclick="visualizarChamado('${chamado.numeroChamado}')">
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                        <h3 class="text-lg font-bold text-white">
                            ${chamado.assunto}
                        </h3>
                        ${chamado.temNovasMensagensUsuario ? '<span class="px-2 py-0.5 text-xs font-bold bg-red-500 text-white rounded-full">NOVA</span>' : ''}
                    </div>
                    <p class="text-sm text-slate-400">
                        <i class="fas fa-ticket mr-1 text-[#d4af37]"></i>
                        ${chamado.numeroChamado}
                    </p>
                </div>
                <div class="text-right">
                    ${getBadgeStatus(chamado.status)}
                    ${getBadgeUrgencia(chamado.urgencia)}
                </div>
            </div>

            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Categoria</p>
                    <p class="text-sm font-bold text-white">${formatarCategoria(chamado.categoria)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Criado em</p>
                    <p class="text-sm font-bold text-white">${formatarData(chamado.criadoEm)}</p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Mensagens</p>
                    <p class="text-sm font-bold text-white">
                        <i class="fas fa-comments mr-1"></i>${chamado.totalMensagens || 0}
                    </p>
                </div>
                <div class="bg-slate-800/50 rounded-lg p-3">
                    <p class="text-xs text-slate-500 mb-1">Atualizado</p>
                    <p class="text-sm font-bold text-white">${chamado.atualizadoEm ? formatarDataRelativa(chamado.atualizadoEm) : 'Agora'}</p>
                </div>
            </div>

            <p class="text-sm text-slate-400 line-clamp-2">
                ${chamado.descricao}
            </p>
        </div>
    `).join('');
}

// ============================================
// CARREGAR CHAMADOS ENCERRADOS
// ============================================
async function carregarChamadosEncerrados() {
    const loading = document.getElementById('loading-encerrados');
    const empty = document.getElementById('empty-encerrados');
    const lista = document.getElementById('lista-chamados-encerrados');
    const badge = document.getElementById('badge-encerrados');

    loading.classList.remove('hidden');
    empty.classList.add('hidden');
    lista.classList.add('hidden');

    try {
        const response = await fetch('/api/suporte/chamados?status=ENCERRADO');
        
        if (!response.ok) {
            throw new Error('Erro ao carregar chamados encerrados');
        }

        chamadosEncerrados = await response.json();
        console.log('[Suporte] Chamados encerrados carregados:', chamadosEncerrados.length);

        loading.classList.add('hidden');

        if (chamadosEncerrados.length === 0) {
            empty.classList.remove('hidden');
            badge.textContent = '0';
        } else {
            renderizarChamadosEncerrados();
            lista.classList.remove('hidden');
            badge.textContent = chamadosEncerrados.length;
        }

    } catch (error) {
        console.error('[Suporte] Erro ao carregar chamados encerrados:', error);
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

function renderizarChamadosEncerrados() {
    const lista = document.getElementById('lista-chamados-encerrados');
    
    lista.innerHTML = chamadosEncerrados.map(chamado => `
        <div class="bg-green-900/10 backdrop-blur-sm border border-green-800/50 rounded-xl p-6 cursor-pointer hover:border-green-600 transition-colors"
             onclick="visualizarChamado('${chamado.numeroChamado}')">
            <div class="flex items-start justify-between mb-4">
                <div class="flex-1">
                    <div class="flex items-center gap-2 mb-2">
                        <i class="fas fa-check-circle text-green-400 text-xl"></i>
                        <h3 class="text-lg font-bold text-white">
                            ${chamado.assunto}
                        </h3>
                    </div>
                    <p class="text-sm text-slate-400">
                        <i class="fas fa-ticket mr-1 text-[#d4af37]"></i>
                        ${chamado.numeroChamado} • Encerrado em ${formatarData(chamado.encerradoEm)}
                    </p>
                </div>
            </div>

            <p class="text-sm text-slate-400 line-clamp-2 mb-3">
                ${chamado.descricao}
            </p>

            <button onclick="event.stopPropagation(); visualizarChamado('${chamado.numeroChamado}')" 
                    class="text-sm text-[#d4af37] hover:text-[#c4a137] font-bold">
                Ver detalhes <i class="fas fa-arrow-right ml-1"></i>
            </button>
        </div>
    `).join('');
}

// ============================================
// VISUALIZAR CHAMADO
// ============================================
async function visualizarChamado(numeroChamado) {
    try {
        const response = await fetch(`/api/suporte/chamados/${numeroChamado}`);
        
        if (!response.ok) {
            throw new Error('Erro ao buscar chamado');
        }

        chamadoAtual = await response.json();
        
        // Preencher modal
        document.getElementById('modal-ver-titulo').textContent = chamadoAtual.assunto;
        document.getElementById('modal-ver-numero').textContent = `Chamado: ${chamadoAtual.numeroChamado}`;
        document.getElementById('chamado-descricao').textContent = chamadoAtual.descricao;
        document.getElementById('chamado-numero-mensagem').value = numeroChamado;
        
        // Informações
        document.getElementById('chamado-info').innerHTML = `
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                    <p class="text-xs text-slate-500">Status</p>
                    ${getBadgeStatus(chamadoAtual.status)}
                </div>
                <div>
                    <p class="text-xs text-slate-500">Categoria</p>
                    <p class="text-sm font-bold text-white">${formatarCategoria(chamadoAtual.categoria)}</p>
                </div>
                <div>
                    <p class="text-xs text-slate-500">Urgência</p>
                    ${getBadgeUrgencia(chamadoAtual.urgencia)}
                </div>
                <div>
                    <p class="text-xs text-slate-500">Criado em</p>
                    <p class="text-sm font-bold text-white">${formatarData(chamadoAtual.criadoEm)}</p>
                </div>
            </div>
        `;
        
        // Carregar mensagens
        await carregarMensagens(numeroChamado);
        
        // Mostrar/esconder áreas baseado no status
        const areaReabrir = document.getElementById('area-reabrir');
        const formMensagem = document.getElementById('form-enviar-mensagem');
        
        if (chamadoAtual.status === 'ENCERRADO' || chamadoAtual.status === 'RESOLVIDO') {
            areaReabrir.classList.remove('hidden');
            formMensagem.classList.add('hidden');
        } else {
            areaReabrir.classList.add('hidden');
            formMensagem.classList.remove('hidden');
        }
        
        // Abrir modal
        document.getElementById('modal-ver-chamado').classList.remove('hidden');
        
    } catch (error) {
        console.error('[Suporte] Erro ao visualizar chamado:', error);
        alert('Erro ao carregar chamado: ' + error.message);
    }
}

// ============================================
// CARREGAR MENSAGENS
// ============================================
async function carregarMensagens(numeroChamado) {
    try {
        const response = await fetch(`/api/suporte/chamados/${numeroChamado}/mensagens`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar mensagens');
        }

        const mensagens = await response.json();
        const lista = document.getElementById('lista-mensagens');
        
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
            const align = isAdmin ? 'items-start' : 'items-end';
            const bg = isAdmin ? 'bg-blue-900/30 border-blue-800' : 'bg-slate-800 border-slate-700';
            
            return `
                <div class="flex flex-col ${align}">
                    <div class="max-w-[80%] ${bg} border rounded-lg p-4">
                        <div class="flex items-center gap-2 mb-2">
                            <i class="fas ${isAdmin ? 'fa-headset' : 'fa-user'} text-[#d4af37]"></i>
                            <span class="text-sm font-bold text-white">${msg.autorNome || (isAdmin ? 'Suporte' : 'Você')}</span>
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
        console.error('[Suporte] Erro ao carregar mensagens:', error);
    }
}

// ============================================
// ENVIAR MENSAGEM
// ============================================
async function enviarMensagem() {
    const numeroChamado = document.getElementById('chamado-numero-mensagem').value;
    const mensagemTexto = document.getElementById('input-nova-mensagem').value.trim();
    
    if (!mensagemTexto) {
        alert('Digite uma mensagem');
        return;
    }
    
    try {
        const response = await fetch(`/api/suporte/chamados/${numeroChamado}/mensagens`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ mensagem: mensagemTexto })
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao enviar mensagem');
        }
        
        document.getElementById('input-nova-mensagem').value = '';
        await carregarMensagens(numeroChamado);
        
        mostrarNotificacao('Mensagem enviada!', 'success');
        
    } catch (error) {
        console.error('[Suporte] Erro ao enviar mensagem:', error);
        alert('Erro ao enviar mensagem: ' + error.message);
    }
}

// ============================================
// REABRIR CHAMADO
// ============================================
async function reabrirChamado() {
    if (!chamadoAtual) return;
    
    if (!confirm('Deseja reabrir este chamado?')) return;
    
    try {
        const response = await fetch(`/api/suporte/chamados/${chamadoAtual.numeroChamado}/reabrir`, {
            method: 'PUT'
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao reabrir chamado');
        }
        
        mostrarNotificacao('Chamado reaberto com sucesso!', 'success');
        fecharModalVerChamado();
        carregarChamadosAbertos();
        
    } catch (error) {
        console.error('[Suporte] Erro ao reabrir chamado:', error);
        alert('Erro ao reabrir chamado: ' + error.message);
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
        'AGUARDANDO_USUARIO': '<span class="px-2 py-1 text-xs font-bold bg-orange-600 text-white rounded">Aguardando você</span>',
        'AGUARDANDO_ADMIN': '<span class="px-2 py-1 text-xs font-bold bg-purple-600 text-white rounded">Aguardando suporte</span>',
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

function getBordaStatus(status) {
    const bordas = {
        'ABERTO': 'border-yellow-800',
        'EM_ATENDIMENTO': 'border-blue-800',
        'AGUARDANDO_USUARIO': 'border-orange-800',
        'AGUARDANDO_ADMIN': 'border-purple-800',
        'RESOLVIDO': 'border-green-800',
        'ENCERRADO': 'border-gray-800'
    };
    return bordas[status] || 'border-slate-800';
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
window.inicializarSuporte = inicializarSuporte;
window.visualizarChamado = visualizarChamado;

console.log('[Suporte] Módulo suporte.js carregado');
