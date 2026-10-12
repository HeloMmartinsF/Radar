/**
 * Painel de Desempenho - Radar Fornecedor
 * Carrega e exibe indicadores de desempenho exclusivos para fornecedores
 */

// Variáveis globais
let graficoVisualizacoes = null;
let graficoInteracoes = null;

// Inicialização
document.addEventListener('DOMContentLoaded', async () => {
    try {
        // Carregar menu lateral
        await carregarMenuLateral();
        await exibirInfoUsuario();
        await aplicarControlesDeAcesso();

        // Carregar indicadores
        await carregarIndicadores();

        // Event listeners
        document.getElementById('btn-atualizar').addEventListener('click', carregarIndicadores);
        document.getElementById('filtro-periodo').addEventListener('change', carregarIndicadores);

    } catch (erro) {
        console.error('Erro na inicialização:', erro);
        mostrarErro('Erro ao inicializar página: ' + erro.message);
    }
});

/**
 * Carrega indicadores de desempenho do backend
 */
async function carregarIndicadores() {
    const loading = document.getElementById('loading');
    const dashboard = document.getElementById('dashboard');
    const semDados = document.getElementById('sem-dados');
    const erro = document.getElementById('erro');

    try {
        // Mostrar loading
        loading.classList.remove('hidden');
        dashboard.classList.add('hidden');
        semDados.classList.add('hidden');
        erro.classList.add('hidden');

        // Obter período selecionado
        const diasHistorico = document.getElementById('filtro-periodo').value;

        // Buscar dados da API
        const response = await fetch(`/api/desempenho/indicadores?diasHistorico=${diasHistorico}`);

        if (response.status === 401) {
            window.location.href = '/login.html';
            return;
        }

        if (response.status === 403) {
            mostrarErro('Acesso negado. Esta página é exclusiva para fornecedores.');
            return;
        }

        if (!response.ok) {
            throw new Error('Erro ao carregar indicadores');
        }

        const indicadores = await response.json();

        // Verificar se há dados
        if (temDados(indicadores)) {
            exibirIndicadores(indicadores);
            dashboard.classList.remove('hidden');
        } else {
            semDados.classList.remove('hidden');
        }

    } catch (error) {
        console.error('Erro ao carregar indicadores:', error);
        mostrarErro(error.message);
    } finally {
        loading.classList.add('hidden');
    }
}

/**
 * Verifica se há dados significativos para exibir
 */
function temDados(indicadores) {
    return indicadores.totalPublicacoes > 0 ||
           indicadores.totalVisualizacoesPublicacoes > 0 ||
           indicadores.totalCurtidas > 0 ||
           indicadores.totalCliquesContato > 0 ||
           indicadores.totalVisualizacoesProdutos > 0;
}

/**
 * Exibe os indicadores na interface
 */
function exibirIndicadores(indicadores) {
    // Atualizar cards de totais
    document.getElementById('total-publicacoes').textContent = formatarNumero(indicadores.totalPublicacoes || 0);
    document.getElementById('total-visualizacoes').textContent = formatarNumero(indicadores.totalVisualizacoesPublicacoes || 0);
    document.getElementById('total-curtidas').textContent = formatarNumero(indicadores.totalCurtidas || 0);
    document.getElementById('total-contatos').textContent = formatarNumero(indicadores.totalCliquesContato || 0);
    document.getElementById('total-visualizacoes-produtos').textContent = formatarNumero(indicadores.totalVisualizacoesProdutos || 0);

    // Renderizar gráficos
    renderizarGraficoVisualizacoes(indicadores.evolucaoVisualizacoes || []);
    renderizarGraficoInteracoes(
        indicadores.evolucaoCurtidas || [],
        indicadores.evolucaoContatos || []
    );

    // Renderizar top performers
    renderizarPublicacoesMaisVisualizadas(indicadores.publicacoesMaisVisualizadas || []);
    renderizarPublicacoesMaisInteragidas(indicadores.publicacoesMaisInteragidas || []);
    renderizarProdutosDestaque(indicadores.produtosMaisVisualizados || []);
}

/**
 * Renderiza gráfico de evolução de visualizações
 */
function renderizarGraficoVisualizacoes(dados) {
    const ctx = document.getElementById('grafico-visualizacoes');
    
    // Destruir gráfico anterior se existir
    if (graficoVisualizacoes) {
        graficoVisualizacoes.destroy();
    }

    // Preparar dados
    const labels = dados.map(d => formatarData(d.data));
    const values = dados.map(d => d.total || 0);

    // Criar gráfico
    graficoVisualizacoes = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Visualizações',
                data: values,
                borderColor: '#10b981',
                backgroundColor: 'rgba(16, 185, 129, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: {
                    display: false
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });
}

/**
 * Renderiza gráfico de curtidas e contatos
 */
function renderizarGraficoInteracoes(curtidas, contatos) {
    const ctx = document.getElementById('grafico-interacoes');
    
    // Destruir gráfico anterior se existir
    if (graficoInteracoes) {
        graficoInteracoes.destroy();
    }

    // Combinar labels únicos
    const todasDatas = new Set([
        ...curtidas.map(d => d.data),
        ...contatos.map(d => d.data)
    ]);
    const labels = Array.from(todasDatas).sort().map(d => formatarData(d));

    // Preparar dados
    const datasCurtidas = curtidas.map(d => d.data);
    const datasContatos = contatos.map(d => d.data);

    const valuesCurtidas = Array.from(todasDatas).sort().map(data => {
        const item = curtidas.find(c => c.data === data);
        return item ? item.total : 0;
    });

    const valuesContatos = Array.from(todasDatas).sort().map(data => {
        const item = contatos.find(c => c.data === data);
        return item ? item.total : 0;
    });

    // Criar gráfico
    graficoInteracoes = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Curtidas',
                    data: valuesCurtidas,
                    backgroundColor: '#ef4444',
                },
                {
                    label: 'Contatos',
                    data: valuesContatos,
                    backgroundColor: '#8b5cf6',
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    }
                }
            }
        }
    });
}

/**
 * Renderiza lista de publicações mais visualizadas
 */
function renderizarPublicacoesMaisVisualizadas(publicacoes) {
    const container = document.getElementById('lista-mais-visualizadas');
    
    if (!publicacoes || publicacoes.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-sm">Nenhuma visualização ainda</p>';
        return;
    }

    container.innerHTML = publicacoes.map(pub => `
        <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition">
            ${pub.primeiraImagem ? `
                <img src="${pub.primeiraImagem}" alt="${pub.titulo}" 
                     class="w-12 h-12 rounded object-cover flex-shrink-0">
            ` : `
                <div class="w-12 h-12 rounded bg-gray-300 flex items-center justify-center flex-shrink-0">
                    <i class="fas fa-image text-gray-500"></i>
                </div>
            `}
            <div class="flex-1 min-w-0">
                <p class="font-medium text-sm text-gray-800 truncate">${pub.titulo}</p>
                <p class="text-xs text-gray-600">
                    <i class="fas fa-eye text-green-500 mr-1"></i>
                    ${formatarNumero(pub.visualizacoes || 0)} visualizações
                </p>
            </div>
        </div>
    `).join('');
}

/**
 * Renderiza lista de publicações mais interagidas
 */
function renderizarPublicacoesMaisInteragidas(publicacoes) {
    const container = document.getElementById('lista-mais-interagidas');
    
    if (!publicacoes || publicacoes.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-sm">Nenhuma interação ainda</p>';
        return;
    }

    container.innerHTML = publicacoes.map(pub => `
        <div class="flex items-center gap-3 p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition">
            ${pub.primeiraImagem ? `
                <img src="${pub.primeiraImagem}" alt="${pub.titulo}" 
                     class="w-12 h-12 rounded object-cover flex-shrink-0">
            ` : `
                <div class="w-12 h-12 rounded bg-gray-300 flex items-center justify-center flex-shrink-0">
                    <i class="fas fa-image text-gray-500"></i>
                </div>
            `}
            <div class="flex-1 min-w-0">
                <p class="font-medium text-sm text-gray-800 truncate">${pub.titulo}</p>
                <p class="text-xs text-gray-600">
                    <i class="fas fa-heart text-red-500 mr-1"></i>${pub.curtidas || 0}
                    <i class="fas fa-envelope text-purple-500 ml-2 mr-1"></i>${pub.cliquesContato || 0}
                </p>
            </div>
        </div>
    `).join('');
}

/**
 * Renderiza lista de produtos mais visualizados
 */
function renderizarProdutosDestaque(produtos) {
    const container = document.getElementById('lista-produtos-destaque');
    
    if (!produtos || produtos.length === 0) {
        container.innerHTML = '<p class="text-gray-500 text-sm">Nenhuma visualização de produto ainda</p>';
        return;
    }

    container.innerHTML = produtos.map(prod => `
        <div class="p-3 bg-gray-50 rounded-lg hover:bg-gray-100 transition">
            <p class="font-medium text-sm text-gray-800 mb-1">${prod.nome}</p>
            <p class="text-xs text-gray-600 mb-2">${prod.categoria || 'Sem categoria'}</p>
            <p class="text-xs text-orange-600">
                <i class="fas fa-eye mr-1"></i>
                ${formatarNumero(prod.visualizacoes || 0)} visualizações
            </p>
        </div>
    `).join('');
}

/**
 * Formata número com separador de milhares
 */
function formatarNumero(num) {
    if (num === null || num === undefined) return '0';
    return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
}

/**
 * Formata data para exibição (dd/MM)
 */
function formatarData(dataStr) {
    if (!dataStr) return '';
    
    try {
        const partes = dataStr.split('-');
        if (partes.length === 3) {
            return `${partes[2]}/${partes[1]}`;
        }
        return dataStr;
    } catch (e) {
        return dataStr;
    }
}

/**
 * Mostra mensagem de erro
 */
function mostrarErro(mensagem) {
    const erro = document.getElementById('erro');
    const erroMensagem = document.getElementById('erro-mensagem');
    
    erroMensagem.textContent = mensagem;
    erro.classList.remove('hidden');
    
    document.getElementById('loading').classList.add('hidden');
    document.getElementById('dashboard').classList.add('hidden');
    document.getElementById('sem-dados').classList.add('hidden');
}

/**
 * Carrega menu lateral
 */
async function carregarMenuLateral() {
    try {
        const response = await fetch('/layout/menu-lateral.html');
        if (response.ok) {
            const html = await response.text();
            document.getElementById('menu-lateral').innerHTML = html;
        }
    } catch (error) {
        console.error('Erro ao carregar menu:', error);
    }
}

/**
 * Exibe informações do usuário
 */
async function exibirInfoUsuario() {
    try {
        const response = await fetch('/api/usuarios/info');
        if (response.ok) {
            const usuario = await response.json();
            const nomeUsuarioEl = document.getElementById('nome-usuario');
            if (nomeUsuarioEl && usuario.nome) {
                nomeUsuarioEl.textContent = usuario.nome;
            }
        }
    } catch (error) {
        console.error('Erro ao buscar info do usuário:', error);
    }
}

/**
 * Aplica controles de acesso baseados no tipo de usuário
 */
async function aplicarControlesDeAcesso() {
    try {
        const response = await fetch('/api/usuarios/info');
        if (response.ok) {
            const usuario = await response.json();
            
            // Se não for fornecedor, redirecionar
            if (!usuario.fornecedor) {
                window.location.href = '/pages/dashboard.html';
            }
        }
    } catch (error) {
        console.error('Erro ao verificar acesso:', error);
    }
}
