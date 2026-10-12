/**
 * favoritos.js - Script reutilizável para gerenciamento de favoritos
 * 
 * FUNCIONALIDADES:
 * - Toggle favorito produto
 * - Toggle favorito fornecedor
 * - Atualizar ícones dinamicamente
 * - Feedback visual (toast notifications)
 * - Verificar status de favorito
 * 
 * USO:
 * 1. Incluir este script: <script src="/js/favoritos.js"></script>
 * 2. Adicionar botão com atributos data:
 *    <button data-tipo="produto" data-id="123" onclick="toggleFavorito(this)">
 *        <i class="fas fa-heart"></i>
 *    </button>
 */

// ======================== TOGGLE FAVORITO ========================

/**
 * Alterna estado de favorito (favoritar/desfavoritar).
 * Funciona para produtos e fornecedores.
 * 
 * @param {HTMLElement} button - Botão clicado (deve ter data-tipo e data-id)
 */
async function toggleFavorito(button) {
    const tipo = button.dataset.tipo; // 'produto' ou 'fornecedor'
    const id = button.dataset.id;
    const icone = button.querySelector('i');

    if (!tipo || !id) {
        console.error('[Favoritos] Botão sem atributos data-tipo ou data-id');
        return;
    }

    // Verificar se já está favoritado
    const isFavoritado = icone.classList.contains('fas'); // fas = favoritado, far = não favoritado

    try {
        // Desabilitar botão temporariamente
        button.disabled = true;
        icone.classList.add('fa-spin');

        if (isFavoritado) {
            // Desfavoritar
            await desfavoritar(tipo, id);
            atualizarIconeParaNaoFavoritado(icone);
            mostrarToast(`${tipo === 'produto' ? 'Produto' : 'Fornecedor'} removido dos favoritos`, 'success');
        } else {
            // Favoritar
            await favoritar(tipo, id);
            atualizarIconeParaFavoritado(icone);
            mostrarToast(`${tipo === 'produto' ? 'Produto' : 'Fornecedor'} adicionado aos favoritos`, 'success');
        }

    } catch (error) {
        console.error('[Favoritos] Erro:', error);
        mostrarToast(error.message || 'Erro ao processar favorito', 'error');
    } finally {
        // Reabilitar botão
        button.disabled = false;
        icone.classList.remove('fa-spin');
    }
}

/**
 * Favorita um item (produto ou fornecedor).
 */
async function favoritar(tipo, id) {
    const endpoint = tipo === 'produto' 
        ? `/api/favoritos/produtos/${id}` 
        : `/api/favoritos/fornecedores/${id}`;

    const response = await fetch(endpoint, {
        method: 'POST'
    });

    const data = await response.json();

    if (!response.ok || !data.sucesso) {
        throw new Error(data.erro || 'Erro ao favoritar');
    }

    return data;
}

/**
 * Desfavorita um item (produto ou fornecedor).
 */
async function desfavoritar(tipo, id) {
    const endpoint = tipo === 'produto' 
        ? `/api/favoritos/produtos/${id}` 
        : `/api/favoritos/fornecedores/${id}`;

    const response = await fetch(endpoint, {
        method: 'DELETE'
    });

    const data = await response.json();

    if (!response.ok || !data.sucesso) {
        throw new Error(data.erro || 'Erro ao desfavoritar');
    }

    return data;
}

// ======================== VERIFICAÇÃO DE STATUS ========================

/**
 * Verifica se um produto está favoritado.
 * Útil para renderizar estado inicial do botão.
 * 
 * @param {number} produtoId - ID do produto
 * @returns {Promise<boolean>} true se favoritado, false caso contrário
 */
async function isProdutoFavoritado(produtoId) {
    try {
        const response = await fetch(`/api/favoritos/produtos/${produtoId}/status`);
        const data = await response.json();
        return data.favoritado === true;
    } catch (error) {
        console.error('[Favoritos] Erro ao verificar status produto:', error);
        return false;
    }
}

/**
 * Verifica se um fornecedor está favoritado.
 * 
 * @param {number} fornecedorId - ID do fornecedor
 * @returns {Promise<boolean>} true se favoritado, false caso contrário
 */
async function isFornecedorFavoritado(fornecedorId) {
    try {
        const response = await fetch(`/api/favoritos/fornecedores/${fornecedorId}/status`);
        const data = await response.json();
        return data.favoritado === true;
    } catch (error) {
        console.error('[Favoritos] Erro ao verificar status fornecedor:', error);
        return false;
    }
}

/**
 * Inicializa múltiplos botões de favorito verificando seus status.
 * Útil para páginas com lista de produtos/fornecedores.
 * 
 * @param {string} seletor - Seletor CSS dos botões (ex: '.btn-favorito-produto')
 */
async function inicializarBotoesFavoritos(seletor = '[data-tipo][data-id]') {
    const botoes = document.querySelectorAll(seletor);

    for (const botao of botoes) {
        const tipo = botao.dataset.tipo;
        const id = parseInt(botao.dataset.id);
        const icone = botao.querySelector('i');

        if (!tipo || !id || !icone) continue;

        try {
            let isFavoritado;

            if (tipo === 'produto') {
                isFavoritado = await isProdutoFavoritado(id);
            } else if (tipo === 'fornecedor') {
                isFavoritado = await isFornecedorFavoritado(id);
            } else {
                continue;
            }

            if (isFavoritado) {
                atualizarIconeParaFavoritado(icone);
            } else {
                atualizarIconeParaNaoFavoritado(icone);
            }

        } catch (error) {
            console.error(`[Favoritos] Erro ao inicializar botão ${tipo} ${id}:`, error);
        }
    }
}

// ======================== ATUALIZAÇÃO DE ÍCONES ========================

/**
 * Atualiza ícone para estado FAVORITADO (coração cheio dourado).
 */
function atualizarIconeParaFavoritado(icone) {
    icone.classList.remove('far', 'text-slate-400');
    icone.classList.add('fas', 'text-[#d4af37]');
}

/**
 * Atualiza ícone para estado NÃO FAVORITADO (coração vazio).
 */
function atualizarIconeParaNaoFavoritado(icone) {
    icone.classList.remove('fas', 'text-[#d4af37]');
    icone.classList.add('far', 'text-slate-400');
}

// ======================== TOAST NOTIFICATIONS ========================

/**
 * Exibe notificação toast temporária.
 * 
 * @param {string} mensagem - Mensagem a exibir
 * @param {string} tipo - Tipo da notificação: 'success', 'error', 'info', 'warning'
 * @param {number} duracao - Duração em ms (padrão: 3000)
 */
function mostrarToast(mensagem, tipo = 'info', duracao = 3000) {
    // Cores por tipo
    const cores = {
        success: 'bg-green-600',
        error: 'bg-red-600',
        info: 'bg-blue-600',
        warning: 'bg-yellow-600'
    };

    // Ícones por tipo
    const icones = {
        success: 'fa-check-circle',
        error: 'fa-exclamation-circle',
        info: 'fa-info-circle',
        warning: 'fa-exclamation-triangle'
    };

    // Criar elemento toast
    const toast = document.createElement('div');
    toast.className = `fixed top-4 right-4 px-6 py-3 rounded-lg shadow-lg z-[9999] ${cores[tipo]} text-white font-bold flex items-center gap-3 animate-slide-in`;
    
    toast.innerHTML = `
        <i class="fas ${icones[tipo]} text-xl"></i>
        <span>${mensagem}</span>
    `;

    // Adicionar ao DOM
    document.body.appendChild(toast);

    // Remover após duração
    setTimeout(() => {
        toast.classList.add('opacity-0', 'transition-opacity', 'duration-300');
        setTimeout(() => toast.remove(), 300);
    }, duracao);
}

// ======================== ESTATÍSTICAS ========================

/**
 * Busca estatísticas de favoritos do usuário.
 * 
 * @returns {Promise<Object>} { totalProdutosFavoritos, totalFornecedoresFavoritos, totalGeral }
 */
async function buscarEstatisticasFavoritos() {
    try {
        const response = await fetch('/api/favoritos/estatisticas');
        const data = await response.json();

        if (data.sucesso) {
            return {
                totalProdutosFavoritos: data.totalProdutosFavoritos || 0,
                totalFornecedoresFavoritos: data.totalFornecedoresFavoritos || 0,
                totalGeral: data.totalGeral || 0
            };
        }

        return { totalProdutosFavoritos: 0, totalFornecedoresFavoritos: 0, totalGeral: 0 };
    } catch (error) {
        console.error('[Favoritos] Erro ao buscar estatísticas:', error);
        return { totalProdutosFavoritos: 0, totalFornecedoresFavoritos: 0, totalGeral: 0 };
    }
}

// ======================== HELPERS PARA CRIAÇÃO DE BOTÕES ========================

/**
 * Cria um botão de favorito produto pronto para uso.
 * 
 * @param {number} produtoId - ID do produto
 * @param {boolean} favoritado - Se já está favoritado
 * @param {string} classes - Classes CSS adicionais
 * @returns {HTMLButtonElement} Botão pronto
 */
function criarBotaoFavoritoProduto(produtoId, favoritado = false, classes = '') {
    const button = document.createElement('button');
    button.dataset.tipo = 'produto';
    button.dataset.id = produtoId;
    button.className = `btn-favorito ${classes}`;
    button.onclick = () => toggleFavorito(button);
    button.title = favoritado ? 'Remover dos favoritos' : 'Adicionar aos favoritos';

    const icone = document.createElement('i');
    icone.className = favoritado 
        ? 'fas fa-heart text-[#d4af37] text-xl' 
        : 'far fa-heart text-slate-400 hover:text-[#d4af37] text-xl';

    button.appendChild(icone);

    return button;
}

/**
 * Cria um botão de favorito fornecedor pronto para uso.
 * 
 * @param {number} fornecedorId - ID do fornecedor
 * @param {boolean} favoritado - Se já está favoritado
 * @param {string} classes - Classes CSS adicionais
 * @returns {HTMLButtonElement} Botão pronto
 */
function criarBotaoFavoritoFornecedor(fornecedorId, favoritado = false, classes = '') {
    const button = document.createElement('button');
    button.dataset.tipo = 'fornecedor';
    button.dataset.id = fornecedorId;
    button.className = `btn-favorito ${classes}`;
    button.onclick = () => toggleFavorito(button);
    button.title = favoritado ? 'Deixar de seguir' : 'Seguir fornecedor';

    const icone = document.createElement('i');
    icone.className = favoritado 
        ? 'fas fa-star text-[#d4af37] text-xl' 
        : 'far fa-star text-slate-400 hover:text-[#d4af37] text-xl';

    button.appendChild(icone);

    return button;
}

// ======================== CSS ANIMATIONS (injetar no <head> se não existir) ========================

if (!document.querySelector('#favoritos-css')) {
    const style = document.createElement('style');
    style.id = 'favoritos-css';
    style.textContent = `
        @keyframes slide-in {
            from {
                transform: translateX(100%);
                opacity: 0;
            }
            to {
                transform: translateX(0);
                opacity: 1;
            }
        }

        .animate-slide-in {
            animation: slide-in 0.3s ease-out;
        }

        .btn-favorito {
            transition: transform 0.2s ease;
        }

        .btn-favorito:hover {
            transform: scale(1.1);
        }

        .btn-favorito:active {
            transform: scale(0.95);
        }
    `;
    document.head.appendChild(style);
}

console.log('[Favoritos] Script carregado com sucesso ✅');
