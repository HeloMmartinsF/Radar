/**
 * mensagens.js
 * Gerenciamento da lista de conversas comerciais
 */

let conversasGlobal = [];

/**
 * Carrega e exibe todas as conversas do usuário
 */
async function carregarConversas() {
    const loading = document.getElementById('conversas-loading');
    const erro = document.getElementById('conversas-erro');
    const vazio = document.getElementById('conversas-vazio');
    const lista = document.getElementById('lista-conversas');

    loading.classList.remove('hidden');
    erro.classList.add('hidden');
    vazio.classList.add('hidden');

    try {
        const response = await fetch('/api/chat/conversas', {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!response.ok) {
            throw new Error('Erro ao carregar conversas');
        }

        const conversas = await response.json();
        conversasGlobal = conversas;

        loading.classList.add('hidden');

        if (conversas.length === 0) {
            vazio.classList.remove('hidden');
            return;
        }

        // Renderizar conversas
        renderizarConversas(conversas);

        // Atualizar badge de total não lidas
        atualizarBadgeTotal(conversas);

    } catch (error) {
        console.error('Erro:', error);
        loading.classList.add('hidden');
        erro.classList.remove('hidden');
    }
}

/**
 * Renderiza lista de conversas no DOM
 */
function renderizarConversas(conversas) {
    const lista = document.getElementById('lista-conversas');
    
    // Limpar conteúdo anterior (exceto loading, erro, vazio)
    const items = lista.querySelectorAll('.conversa-item');
    items.forEach(item => item.remove());

    conversas.forEach(conversa => {
        const item = criarItemConversa(conversa);
        lista.appendChild(item);
    });
}

/**
 * Cria elemento HTML de uma conversa
 */
function criarItemConversa(conversa) {
    const div = document.createElement('div');
    div.className = 'conversa-item p-4 flex items-center gap-4 transition-colors';
    div.dataset.conversaId = conversa.id;

    if (conversa.naoLidas > 0) {
        div.classList.add('conversa-nao-lida');
    }

    // Avatar
    const avatar = document.createElement('div');
    avatar.className = 'flex-shrink-0 w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-lg';
    avatar.textContent = conversa.outroParticipanteNome ? conversa.outroParticipanteNome.charAt(0).toUpperCase() : 'U';
    
    // Conteúdo
    const conteudo = document.createElement('div');
    conteudo.className = 'flex-1 min-w-0';

    // Cabeçalho (nome + data)
    const cabecalho = document.createElement('div');
    cabecalho.className = 'flex items-center justify-between mb-1';

    const nome = document.createElement('div');
    nome.className = 'font-semibold text-gray-900 truncate';
    nome.textContent = conversa.outroParticipanteNome || 'Usuário';

    // Contexto (ícone + texto)
    if (conversa.contexto) {
        const contextoSpan = document.createElement('span');
        contextoSpan.className = 'ml-2 text-xs text-gray-500';
        
        if (conversa.contexto === 'PRODUTO') {
            contextoSpan.innerHTML = '<i class="fas fa-box text-blue-500"></i> ' + (conversa.produtoNome || 'Produto');
        } else if (conversa.contexto === 'ORCAMENTO') {
            contextoSpan.innerHTML = '<i class="fas fa-file-invoice text-green-500"></i> Orçamento';
        } else {
            contextoSpan.innerHTML = '<i class="fas fa-comment text-gray-400"></i> Geral';
        }
        
        nome.appendChild(contextoSpan);
    }

    const dataHora = document.createElement('span');
    dataHora.className = 'text-xs text-gray-500 flex-shrink-0';
    dataHora.textContent = formatarDataRelativa(conversa.ultimaMensagemEm);

    cabecalho.appendChild(nome);
    cabecalho.appendChild(dataHora);

    // Última mensagem
    const ultimaMensagem = document.createElement('p');
    ultimaMensagem.className = 'text-sm text-gray-600 truncate';
    ultimaMensagem.textContent = conversa.ultimaMensagemTexto || 'Nenhuma mensagem ainda';

    // Badge de não lidas
    if (conversa.naoLidas > 0) {
        const badge = document.createElement('div');
        badge.className = 'badge-nao-lidas bg-red-500 text-white text-xs font-bold rounded-full px-2 py-1 ml-2';
        badge.textContent = conversa.naoLidas;
        ultimaMensagem.appendChild(badge);
    }

    conteudo.appendChild(cabecalho);
    conteudo.appendChild(ultimaMensagem);

    div.appendChild(avatar);
    div.appendChild(conteudo);

    // Click para abrir conversa
    div.addEventListener('click', () => {
        window.location.href = `/pages/conversa.html?id=${conversa.id}`;
    });

    return div;
}

/**
 * Atualiza badge de total de mensagens não lidas
 */
function atualizarBadgeTotal(conversas) {
    const totalNaoLidas = conversas.reduce((sum, conv) => sum + (conv.naoLidas || 0), 0);
    
    const badge = document.getElementById('total-nao-lidas-badge');
    const texto = document.getElementById('total-nao-lidas-texto');

    if (totalNaoLidas > 0) {
        badge.classList.remove('hidden');
        texto.textContent = totalNaoLidas;
    } else {
        badge.classList.add('hidden');
    }

    // Atualizar badge no menu lateral também
    atualizarBadgeMenuLateral(totalNaoLidas);
}

/**
 * Atualiza badge no menu lateral
 */
function atualizarBadgeMenuLateral(total) {
    const chatBadge = document.getElementById('chat-badge');
    if (chatBadge) {
        if (total > 0) {
            chatBadge.classList.remove('hidden');
            chatBadge.textContent = total > 99 ? '99+' : total;
        } else {
            chatBadge.classList.add('hidden');
        }
    }
}

/**
 * Configura pesquisa de conversas
 */
function configurarPesquisa() {
    const inputPesquisa = document.getElementById('pesquisa-conversas');
    
    inputPesquisa.addEventListener('input', (e) => {
        const termo = e.target.value.toLowerCase().trim();
        
        if (!termo) {
            renderizarConversas(conversasGlobal);
            return;
        }

        const filtradas = conversasGlobal.filter(conversa => {
            const nome = (conversa.outroParticipanteNome || '').toLowerCase();
            const fornecedor = (conversa.fornecedorNome || '').toLowerCase();
            const produto = (conversa.produtoNome || '').toLowerCase();
            const mensagem = (conversa.ultimaMensagemTexto || '').toLowerCase();

            return nome.includes(termo) || 
                   fornecedor.includes(termo) || 
                   produto.includes(termo) ||
                   mensagem.includes(termo);
        });

        renderizarConversas(filtradas);
    });
}

/**
 * Formata data de forma relativa (ex: "há 5 minutos", "ontem", "2 dias atrás")
 */
function formatarDataRelativa(dataStr) {
    if (!dataStr) return '';

    const data = new Date(dataStr);
    const agora = new Date();
    const diffMs = agora - data;
    const diffMinutos = Math.floor(diffMs / 60000);
    const diffHoras = Math.floor(diffMs / 3600000);
    const diffDias = Math.floor(diffMs / 86400000);

    if (diffMinutos < 1) {
        return 'agora';
    } else if (diffMinutos < 60) {
        return `há ${diffMinutos} min`;
    } else if (diffHoras < 24) {
        return `há ${diffHoras}h`;
    } else if (diffDias === 1) {
        return 'ontem';
    } else if (diffDias < 7) {
        return `${diffDias} dias atrás`;
    } else {
        return data.toLocaleDateString('pt-BR', { day: '2-digit', month: '2-digit', year: 'numeric' });
    }
}

/**
 * Polling: Atualiza conversas a cada 30 segundos
 */
setInterval(async () => {
    try {
        const response = await fetch('/api/chat/conversas');
        if (response.ok) {
            const conversas = await response.json();
            conversasGlobal = conversas;
            renderizarConversas(conversas);
            atualizarBadgeTotal(conversas);
        }
    } catch (error) {
        console.error('Erro no polling:', error);
    }
}, 30000);
