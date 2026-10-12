/**
 * conversa.js
 * Gerenciamento de uma conversa individual (chat)
 */

let conversaIdAtual = null;
let ultimaMensagemId = 0;
let pollingInterval = null;

/**
 * Carrega detalhes da conversa e suas mensagens
 */
async function carregarConversa(conversaId) {
    conversaIdAtual = conversaId;

    const loading = document.getElementById('mensagens-loading');
    const erro = document.getElementById('mensagens-erro');

    loading.classList.remove('hidden');
    erro.classList.add('hidden');

    try {
        // Buscar detalhes da conversa
        const respConv = await fetch(`/api/chat/conversas/${conversaId}`);
        if (!respConv.ok) {
            throw new Error('Conversa não encontrada');
        }
        const conversa = await respConv.json();

        // Atualizar cabeçalho
        atualizarCabecalho(conversa);

        // Buscar mensagens
        const respMsgs = await fetch(`/api/chat/conversas/${conversaId}/mensagens`);
        if (!respMsgs.ok) {
            throw new Error('Erro ao carregar mensagens');
        }
        const mensagens = await respMsgs.json();

        loading.classList.add('hidden');

        // Renderizar mensagens
        renderizarMensagens(mensagens);

        // Marcar como lidas
        await marcarComoLidas(conversaId);

        // Scroll para o final
        scrollParaFinal();

    } catch (error) {
        console.error('Erro:', error);
        loading.classList.add('hidden');
        erro.classList.remove('hidden');
    }
}

/**
 * Atualiza cabeçalho com informações da conversa
 */
function atualizarCabecalho(conversa) {
    const nomeEl = document.getElementById('nome-participante');
    const contextoEl = document.getElementById('contexto-conversa');
    const avatarEl = document.getElementById('avatar-inicial');

    nomeEl.textContent = conversa.outroParticipanteNome || 'Usuário';
    avatarEl.textContent = conversa.outroParticipanteNome ? conversa.outroParticipanteNome.charAt(0).toUpperCase() : 'U';

    // Contexto
    let contextoTexto = '';
    if (conversa.contexto === 'PRODUTO' && conversa.produtoNome) {
        contextoTexto = `<i class="fas fa-box text-blue-500"></i> ${conversa.produtoNome}`;
    } else if (conversa.contexto === 'ORCAMENTO') {
        contextoTexto = '<i class="fas fa-file-invoice text-green-500"></i> Orçamento';
    } else if (conversa.fornecedorNome) {
        contextoTexto = `<i class="fas fa-building text-gray-500"></i> ${conversa.fornecedorNome}`;
    }
    contextoEl.innerHTML = contextoTexto;
}

/**
 * Renderiza lista de mensagens
 */
function renderizarMensagens(mensagens, append = false) {
    const lista = document.getElementById('lista-mensagens');
    
    if (!append) {
        lista.innerHTML = '';
    }

    if (mensagens.length === 0 && !append) {
        const vazio = document.createElement('div');
        vazio.className = 'text-center py-12 text-gray-500';
        vazio.innerHTML = `
            <i class="fas fa-comment-slash text-4xl mb-3"></i>
            <p>Nenhuma mensagem ainda. Inicie a conversa!</p>
        `;
        lista.appendChild(vazio);
        return;
    }

    mensagens.forEach(mensagem => {
        const msgEl = criarElementoMensagem(mensagem);
        lista.appendChild(msgEl);

        // Atualizar última mensagem ID
        if (mensagem.id > ultimaMensagemId) {
            ultimaMensagemId = mensagem.id;
        }
    });
}

/**
 * Cria elemento HTML de uma mensagem
 */
function criarElementoMensagem(mensagem) {
    const div = document.createElement('div');
    div.className = `flex ${mensagem.ehMinha ? 'justify-end' : 'justify-start'}`;

    const bubble = document.createElement('div');
    bubble.className = `max-w-md px-4 py-3 rounded-lg shadow-sm ${mensagem.ehMinha ? 'mensagem-minha' : 'mensagem-outro'}`;

    const texto = document.createElement('p');
    texto.className = 'text-sm whitespace-pre-wrap break-words';
    texto.textContent = mensagem.texto;

    const rodape = document.createElement('div');
    rodape.className = `flex items-center gap-2 mt-2 text-xs ${mensagem.ehMinha ? 'text-blue-100' : 'text-gray-500'}`;

    const hora = document.createElement('span');
    hora.textContent = formatarHora(mensagem.enviadaEm);

    rodape.appendChild(hora);

    if (mensagem.editada) {
        const editadoSpan = document.createElement('span');
        editadoSpan.textContent = '• editada';
        rodape.appendChild(editadoSpan);
    }

    if (mensagem.ehMinha && mensagem.lida) {
        const lidaIcon = document.createElement('i');
        lidaIcon.className = 'fas fa-check-double';
        lidaIcon.title = 'Lida';
        rodape.appendChild(lidaIcon);
    }

    bubble.appendChild(texto);
    bubble.appendChild(rodape);
    div.appendChild(bubble);

    return div;
}

/**
 * Configura formulário de envio
 */
function configurarFormEnvio() {
    const form = document.getElementById('form-enviar-mensagem');
    const input = document.getElementById('input-mensagem');
    const btnEnviar = document.getElementById('btn-enviar');

    // Habilitar botão apenas se há texto
    input.addEventListener('input', () => {
        btnEnviar.disabled = input.value.trim().length === 0;
    });

    // Enter para enviar (Shift+Enter para nova linha)
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' && !e.shiftKey) {
            e.preventDefault();
            form.dispatchEvent(new Event('submit'));
        }
    });

    // Enviar mensagem
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        await enviarMensagem();
    });
}

/**
 * Envia uma nova mensagem
 */
async function enviarMensagem() {
    const input = document.getElementById('input-mensagem');
    const btnEnviar = document.getElementById('btn-enviar');
    const texto = input.value.trim();

    if (!texto || !conversaIdAtual) return;

    // Desabilitar input durante envio
    input.disabled = true;
    btnEnviar.disabled = true;

    try {
        const response = await fetch(`/api/chat/conversas/${conversaIdAtual}/mensagens`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ texto })
        });

        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao enviar mensagem');
        }

        const mensagem = await response.json();

        // Limpar input
        input.value = '';
        input.style.height = 'auto';

        // Adicionar mensagem à lista
        renderizarMensagens([{
            id: mensagem.id,
            remetenteId: mensagem.remetenteId,
            texto: mensagem.texto,
            enviadaEm: mensagem.enviadaEm,
            lida: false,
            editada: false,
            ehMinha: true
        }], true);

        // Scroll para o final
        scrollParaFinal();

    } catch (error) {
        console.error('Erro ao enviar:', error);
        alert('Erro ao enviar mensagem: ' + error.message);
    } finally {
        input.disabled = false;
        btnEnviar.disabled = texto.length === 0;
        input.focus();
    }
}

/**
 * Marca todas as mensagens da conversa como lidas
 */
async function marcarComoLidas(conversaId) {
    try {
        await fetch(`/api/chat/conversas/${conversaId}/marcar-lidas`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' }
        });
    } catch (error) {
        console.error('Erro ao marcar como lidas:', error);
    }
}

/**
 * Polling: busca novas mensagens a cada 5 segundos
 */
function iniciarPolling(conversaId) {
    // Limpar polling anterior se existir
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }

    pollingInterval = setInterval(async () => {
        try {
            const response = await fetch(`/api/chat/conversas/${conversaId}/mensagens`);
            if (!response.ok) return;

            const mensagens = await response.json();

            // Filtrar apenas mensagens novas (ID maior que última conhecida)
            const novas = mensagens.filter(m => m.id > ultimaMensagemId);

            if (novas.length > 0) {
                renderizarMensagens(novas, true);
                scrollParaFinal();
                await marcarComoLidas(conversaId);
            }

        } catch (error) {
            console.error('Erro no polling:', error);
        }
    }, 5000);
}

/**
 * Scroll automático para o final da área de mensagens
 */
function scrollParaFinal() {
    const area = document.getElementById('area-mensagens');
    setTimeout(() => {
        area.scrollTop = area.scrollHeight;
    }, 100);
}

/**
 * Auto-resize do textarea
 */
function configurarAutoResize() {
    const input = document.getElementById('input-mensagem');
    
    input.addEventListener('input', () => {
        input.style.height = 'auto';
        input.style.height = Math.min(input.scrollHeight, 120) + 'px';
    });
}

/**
 * Contador de caracteres
 */
function configurarContadorCaracteres() {
    const input = document.getElementById('input-mensagem');
    const contador = document.getElementById('contador-caracteres');
    
    input.addEventListener('input', () => {
        contador.textContent = input.value.length;
        
        if (input.value.length > 4500) {
            contador.classList.add('text-red-600', 'font-bold');
        } else {
            contador.classList.remove('text-red-600', 'font-bold');
        }
    });
}

/**
 * Formata hora no formato "HH:MM"
 */
function formatarHora(dataStr) {
    if (!dataStr) return '';
    
    const data = new Date(dataStr);
    const horas = String(data.getHours()).padStart(2, '0');
    const minutos = String(data.getMinutes()).padStart(2, '0');
    
    return `${horas}:${minutos}`;
}

/**
 * Cleanup ao sair da página
 */
window.addEventListener('beforeunload', () => {
    if (pollingInterval) {
        clearInterval(pollingInterval);
    }
});
