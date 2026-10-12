/**
 * notificacoes.js
 * 
 * Gerencia a central de notificações do Radar Fornecedor:
 * - Ícone sino com badge de não lidas
 * - Dropdown com últimas 5 notificações
 * - Página completa com todas as notificações
 * - Marcação como lida
 * - Atualização automática do contador
 */

// ========================================
// CONSTANTES E CONFIGURAÇÃO
// ========================================

const NOTIFICACOES_API = '/api/notificacoes';
const INTERVALO_ATUALIZACAO = 30000; // 30 segundos
let intervaloBadge = null;

// ========================================
// INICIALIZAÇÃO
// ========================================

/**
 * Inicializa o sistema de notificações
 * Chamado automaticamente quando o DOM estiver pronto
 */
function inicializarNotificacoes() {
    // Atualizar badge inicial
    atualizarBadgeNotificacoes();
    
    // Atualizar badge periodicamente
    intervaloBadge = setInterval(atualizarBadgeNotificacoes, INTERVALO_ATUALIZACAO);
    
    // Event listeners para dropdown
    const sinoIcon = document.getElementById('notificacoes-icon');
    if (sinoIcon) {
        sinoIcon.addEventListener('click', toggleDropdownNotificacoes);
    }
    
    // Fechar dropdown ao clicar fora
    document.addEventListener('click', (e) => {
        const dropdown = document.getElementById('notificacoes-dropdown');
        const sino = document.getElementById('notificacoes-icon');
        
        if (dropdown && sino && !dropdown.contains(e.target) && !sino.contains(e.target)) {
            dropdown.classList.add('hidden');
        }
    });
}

// ========================================
// BADGE (CONTADOR DE NÃO LIDAS)
// ========================================

/**
 * Atualiza o badge com quantidade de notificações não lidas
 */
async function atualizarBadgeNotificacoes() {
    try {
        const response = await fetch(`${NOTIFICACOES_API}/nao-lidas/count`);
        
        if (!response.ok) {
            // Se não autenticado ou erro, não mostrar badge
            if (response.status === 401) {
                esconderBadge();
                return;
            }
            throw new Error('Erro ao buscar contagem de notificações');
        }
        
        const data = await response.json();
        const count = data.count || 0;
        
        const badge = document.getElementById('notificacoes-badge');
        if (badge) {
            if (count > 0) {
                badge.textContent = count > 99 ? '99+' : count;
                badge.classList.remove('hidden');
            } else {
                badge.classList.add('hidden');
            }
        }
        
    } catch (error) {
        console.error('Erro ao atualizar badge de notificações:', error);
        esconderBadge();
    }
}

/**
 * Esconde o badge de notificações
 */
function esconderBadge() {
    const badge = document.getElementById('notificacoes-badge');
    if (badge) {
        badge.classList.add('hidden');
    }
}

// ========================================
// DROPDOWN (ÚLTIMAS 5 NOTIFICAÇÕES)
// ========================================

/**
 * Abre/fecha o dropdown de notificações
 */
async function toggleDropdownNotificacoes(event) {
    event.stopPropagation();
    
    const dropdown = document.getElementById('notificacoes-dropdown');
    if (!dropdown) return;
    
    if (dropdown.classList.contains('hidden')) {
        // Abrir dropdown e carregar notificações
        await carregarDropdownNotificacoes();
        dropdown.classList.remove('hidden');
    } else {
        // Fechar dropdown
        dropdown.classList.add('hidden');
    }
}

/**
 * Carrega as últimas 5 notificações no dropdown
 */
async function carregarDropdownNotificacoes() {
    try {
        const response = await fetch(`${NOTIFICACOES_API}?limite=5`);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar notificações');
        }
        
        const notificacoes = await response.json();
        renderizarDropdownNotificacoes(notificacoes);
        
    } catch (error) {
        console.error('Erro ao carregar dropdown de notificações:', error);
        mostrarErroDropdown();
    }
}

/**
 * Renderiza notificações no dropdown
 */
function renderizarDropdownNotificacoes(notificacoes) {
    const lista = document.getElementById('notificacoes-lista');
    if (!lista) return;
    
    if (notificacoes.length === 0) {
        lista.innerHTML = `
            <div class="px-4 py-8 text-center text-gray-500">
                <i class="fas fa-bell-slash text-3xl mb-2"></i>
                <p>Nenhuma notificação</p>
            </div>
        `;
        return;
    }
    
    lista.innerHTML = notificacoes.map(notif => `
        <div class="notificacao-item ${notif.lida ? 'lida' : 'nao-lida'} 
                    px-4 py-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100"
             onclick="abrirNotificacao(${notif.id}, '${notif.linkDestino || ''}')">
            <div class="flex items-start gap-3">
                ${!notif.lida ? '<div class="w-2 h-2 bg-blue-500 rounded-full mt-2 flex-shrink-0"></div>' : '<div class="w-2"></div>'}
                <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-gray-900 truncate">${escapeHtml(notif.titulo)}</p>
                    ${notif.mensagem ? `<p class="text-xs text-gray-600 truncate mt-1">${escapeHtml(notif.mensagem)}</p>` : ''}
                    <p class="text-xs text-gray-400 mt-1">${formatarTempo(notif.criadaEm)}</p>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Mostra erro no dropdown
 */
function mostrarErroDropdown() {
    const lista = document.getElementById('notificacoes-lista');
    if (lista) {
        lista.innerHTML = `
            <div class="px-4 py-8 text-center text-red-500">
                <i class="fas fa-exclamation-circle text-3xl mb-2"></i>
                <p>Erro ao carregar notificações</p>
            </div>
        `;
    }
}

// ========================================
// AÇÕES COM NOTIFICAÇÕES
// ========================================

/**
 * Abre uma notificação (marca como lida e redireciona)
 */
async function abrirNotificacao(notificacaoId, linkDestino) {
    try {
        // Marcar como lida
        await marcarNotificacaoComoLida(notificacaoId);
        
        // Atualizar badge
        await atualizarBadgeNotificacoes();
        
        // Redirecionar se houver link
        if (linkDestino && linkDestino !== 'null' && linkDestino !== '') {
            window.location.href = linkDestino;
        }
        
    } catch (error) {
        console.error('Erro ao abrir notificação:', error);
    }
}

/**
 * Marca uma notificação como lida
 */
async function marcarNotificacaoComoLida(notificacaoId) {
    try {
        const response = await fetch(`${NOTIFICACOES_API}/${notificacaoId}/ler`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao marcar notificação como lida');
        }
        
        return true;
        
    } catch (error) {
        console.error('Erro ao marcar notificação como lida:', error);
        return false;
    }
}

/**
 * Marca todas as notificações como lidas
 */
async function marcarTodasComoLidas() {
    try {
        const response = await fetch(`${NOTIFICACOES_API}/ler-todas`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao marcar todas como lidas');
        }
        
        // Atualizar badge e recarregar lista se estiver na página
        await atualizarBadgeNotificacoes();
        
        if (window.location.pathname.includes('central-notificacoes')) {
            await carregarTodasNotificacoes();
        } else {
            await carregarDropdownNotificacoes();
        }
        
        return true;
        
    } catch (error) {
        console.error('Erro ao marcar todas como lidas:', error);
        return false;
    }
}

/**
 * Deleta uma notificação
 */
async function deletarNotificacao(notificacaoId) {
    if (!confirm('Deseja realmente deletar esta notificação?')) {
        return false;
    }
    
    try {
        const response = await fetch(`${NOTIFICACOES_API}/${notificacaoId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error('Erro ao deletar notificação');
        }
        
        // Atualizar badge e recarregar lista
        await atualizarBadgeNotificacoes();
        
        if (window.location.pathname.includes('central-notificacoes')) {
            await carregarTodasNotificacoes();
        } else {
            await carregarDropdownNotificacoes();
        }
        
        return true;
        
    } catch (error) {
        console.error('Erro ao deletar notificação:', error);
        alert('Erro ao deletar notificação');
        return false;
    }
}

// ========================================
// PÁGINA COMPLETA (central-notificacoes.html)
// ========================================

/**
 * Carrega todas as notificações na página completa
 */
async function carregarTodasNotificacoes(filtro = 'todas') {
    try {
        let url = NOTIFICACOES_API;
        
        if (filtro === 'nao-lidas') {
            url += '?apenasNaoLidas=true';
        }
        
        const response = await fetch(url);
        
        if (!response.ok) {
            throw new Error('Erro ao carregar notificações');
        }
        
        const notificacoes = await response.json();
        renderizarTodasNotificacoes(notificacoes);
        
    } catch (error) {
        console.error('Erro ao carregar todas as notificações:', error);
        mostrarErroListaCompleta();
    }
}

/**
 * Renderiza todas as notificações na página completa
 */
function renderizarTodasNotificacoes(notificacoes) {
    const container = document.getElementById('todas-notificacoes');
    if (!container) return;
    
    if (notificacoes.length === 0) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-bell-slash text-6xl text-gray-300 mb-4"></i>
                <p class="text-gray-500 text-lg">Nenhuma notificação encontrada</p>
            </div>
        `;
        return;
    }
    
    container.innerHTML = notificacoes.map(notif => `
        <div class="notificacao-card ${notif.lida ? 'lida' : 'nao-lida'} 
                    bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow"
             data-id="${notif.id}">
            <div class="flex items-start gap-4">
                ${!notif.lida ? '<div class="w-3 h-3 bg-blue-500 rounded-full mt-1 flex-shrink-0"></div>' : '<div class="w-3"></div>'}
                <div class="flex-1 min-w-0">
                    <h3 class="text-base font-semibold text-gray-900">${escapeHtml(notif.titulo)}</h3>
                    ${notif.mensagem ? `<p class="text-sm text-gray-600 mt-1">${escapeHtml(notif.mensagem)}</p>` : ''}
                    ${notif.origemNome ? `<p class="text-xs text-gray-500 mt-2"><i class="fas fa-user text-gray-400"></i> ${escapeHtml(notif.origemNome)}</p>` : ''}
                    <p class="text-xs text-gray-400 mt-1"><i class="fas fa-clock text-gray-400"></i> ${formatarDataCompleta(notif.criadaEm)}</p>
                </div>
                <div class="flex gap-2 flex-shrink-0">
                    ${notif.linkDestino && notif.linkDestino !== 'null' ? 
                        `<button onclick="abrirNotificacao(${notif.id}, '${notif.linkDestino}')" 
                                class="text-blue-600 hover:text-blue-800" title="Abrir">
                            <i class="fas fa-external-link-alt"></i>
                        </button>` : ''}
                    ${!notif.lida ? 
                        `<button onclick="marcarNotificacaoComoLida(${notif.id}).then(() => carregarTodasNotificacoes())" 
                                class="text-green-600 hover:text-green-800" title="Marcar como lida">
                            <i class="fas fa-check"></i>
                        </button>` : ''}
                    <button onclick="deletarNotificacao(${notif.id})" 
                            class="text-red-600 hover:text-red-800" title="Deletar">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Mostra erro na lista completa
 */
function mostrarErroListaCompleta() {
    const container = document.getElementById('todas-notificacoes');
    if (container) {
        container.innerHTML = `
            <div class="text-center py-12">
                <i class="fas fa-exclamation-circle text-6xl text-red-300 mb-4"></i>
                <p class="text-red-500 text-lg">Erro ao carregar notificações</p>
                <button onclick="carregarTodasNotificacoes()" 
                        class="mt-4 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700">
                    Tentar novamente
                </button>
            </div>
        `;
    }
}

// ========================================
// UTILITÁRIOS
// ========================================

/**
 * Escapa HTML para prevenir XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Formata tempo relativo (ex: "há 5 minutos", "há 2 horas")
 */
function formatarTempo(dataISO) {
    if (!dataISO) return '';
    
    const data = new Date(dataISO);
    const agora = new Date();
    const diffMs = agora - data;
    const diffMinutos = Math.floor(diffMs / 60000);
    const diffHoras = Math.floor(diffMs / 3600000);
    const diffDias = Math.floor(diffMs / 86400000);
    
    if (diffMinutos < 1) return 'Agora';
    if (diffMinutos < 60) return `Há ${diffMinutos} min`;
    if (diffHoras < 24) return `Há ${diffHoras}h`;
    if (diffDias < 7) return `Há ${diffDias} dia${diffDias > 1 ? 's' : ''}`;
    
    return data.toLocaleDateString('pt-BR');
}

/**
 * Formata data completa (ex: "06/10/2026 às 15:30")
 */
function formatarDataCompleta(dataISO) {
    if (!dataISO) return '';
    
    const data = new Date(dataISO);
    return data.toLocaleString('pt-BR', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

// ========================================
// AUTO-INICIALIZAÇÃO
// ========================================

// Inicializar quando DOM estiver pronto
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', inicializarNotificacoes);
} else {
    inicializarNotificacoes();
}
