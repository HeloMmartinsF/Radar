/**
 * perfil-comercial.js
 * Lógica do Perfil Comercial de Fornecedores
 * 
 * Funcionalidades:
 * - Carregar dados do fornecedor (via URL param ou usuário logado)
 * - Exibir produtos do catálogo
 * - Exibir publicações do feed
 * - Exibir e gerenciar avaliações
 * - Sistema de abas
 * - Formulário de avaliação
 */

let fornecedorAtual = null;
let usuarioLogado = null;
let fornecedorId = null;

// ========================================
// INICIALIZAÇÃO
// ========================================

document.addEventListener('DOMContentLoaded', async () => {
    console.log('[Perfil Comercial] Inicializando...');
    
    // Carregar menu lateral
    await carregarMenuLateral();
    
    // Buscar usuário logado
    usuarioLogado = await buscarUsuarioLogado();
    
    // Determinar qual fornecedor exibir
    const urlParams = new URLSearchParams(window.location.search);
    const fornecedorIdParam = urlParams.get('fornecedor');
    
    if (fornecedorIdParam) {
        // Visualizando perfil de outro fornecedor
        fornecedorId = parseInt(fornecedorIdParam);
    } else if (usuarioLogado && usuarioLogado.fornecedor) {
        // Visualizando próprio perfil (usuário é fornecedor)
        fornecedorId = await buscarIdFornecedorPorCnpj(usuarioLogado.cnpjOuCpf);
    } else {
        // Erro: usuário não é fornecedor e não especificou ID
        mostrarErro('Fornecedor não especificado');
        return;
    }
    
    if (!fornecedorId) {
        mostrarErro('Fornecedor não encontrado');
        return;
    }
    
    // Carregar dados do fornecedor
    await carregarDadosFornecedor(fornecedorId);
    
    // Inicializar sistema de abas
    inicializarAbas();
    
    // Carregar aba inicial (Sobre)
    await carregarAbaSobre();
    
    console.log('[Perfil Comercial] Inicializado com sucesso');
});

// ========================================
// BUSCAR DADOS INICIAIS
// ========================================

async function buscarUsuarioLogado() {
    try {
        const response = await fetch('/api/login/session');
        if (response.ok) {
            return await response.json();
        }
        return null;
    } catch (error) {
        console.error('Erro ao buscar usuário logado:', error);
        return null;
    }
}

async function buscarIdFornecedorPorCnpj(cnpj) {
    if (!cnpj) return null;
    
    try {
        const response = await fetch(`/api/fornecedores/cnpj/${cnpj}`);
        if (response.ok) {
            const fornecedor = await response.json();
            return fornecedor.id;
        }
        return null;
    } catch (error) {
        console.error('Erro ao buscar fornecedor por CNPJ:', error);
        return null;
    }
}

// ========================================
// CARREGAR DADOS DO FORNECEDOR
// ========================================

async function carregarDadosFornecedor(id) {
    try {
        const response = await fetch(`/api/fornecedores/${id}`);
        if (!response.ok) {
            throw new Error('Fornecedor não encontrado');
        }
        
        fornecedorAtual = await response.json();
        console.log('[Perfil Comercial] Fornecedor carregado:', fornecedorAtual);
        
        // Preencher dados no header
        preencherHeader();
        
        // Carregar resumo de avaliações
        await carregarResumoAvaliacoes();
        
    } catch (error) {
        console.error('Erro ao carregar fornecedor:', error);
        mostrarErro('Erro ao carregar dados do fornecedor');
    }
}

function preencherHeader() {
    // Nome da empresa
    document.getElementById('nomeEmpresa').textContent = fornecedorAtual.nome || 'Empresa';
    
    // CNPJ formatado
    document.getElementById('cnpjEmpresa').textContent = formatarCNPJ(fornecedorAtual.cnpj);
    
    // Logotipo
    if (fornecedorAtual.foto) {
        document.getElementById('logotipoEmpresa').src = `data:image/jpeg;base64,${fornecedorAtual.foto}`;
    }
    
    // Selo de verificado
    if (fornecedorAtual.verificado) {
        document.getElementById('seloVerificado').style.display = 'inline-flex';
    }
    
    // Localização
    const localizacao = [fornecedorAtual.cidade, fornecedorAtual.estado].filter(Boolean).join(', ');
    document.getElementById('localizacaoEmpresa').querySelector('span').textContent = localizacao || 'Localização não informada';
    
    // Ramo de atuação
    const ramo = fornecedorAtual.atividade ? fornecedorAtual.atividade.descricao : 'Não informado';
    const categoria = fornecedorAtual.categoria ? ` - ${fornecedorAtual.categoria.nome}` : '';
    document.getElementById('ramoAtuacao').querySelector('span').textContent = ramo + categoria;
    
    // Botão de contato
    document.getElementById('btnContato').addEventListener('click', () => abrirContato());
}

async function carregarResumoAvaliacoes() {
    try {
        const response = await fetch(`/api/avaliacoes-fornecedor/${fornecedorId}/resumo`);
        if (response.ok) {
            const resumo = await response.json();
            
            // Média
            const media = resumo.media || 0;
            document.getElementById('mediaAvaliacoes').textContent = media.toFixed(1);
            
            // Total
            document.getElementById('totalAvaliacoes').querySelector('span').textContent = resumo.total || 0;
            
            // Estrelas
            exibirEstrelas('estrelasResumo', media);
        }
    } catch (error) {
        console.error('Erro ao carregar resumo de avaliações:', error);
    }
}

// ========================================
// SISTEMA DE ABAS
// ========================================

function inicializarAbas() {
    const botoes = document.querySelectorAll('.tab-btn');
    
    botoes.forEach(btn => {
        btn.addEventListener('click', async () => {
            const tabId = btn.dataset.tab;
            
            // Remover classe ativa de todos
            botoes.forEach(b => b.classList.remove('tab-ativa'));
            document.querySelectorAll('.tab-content').forEach(c => c.classList.add('hidden'));
            
            // Ativar selecionado
            btn.classList.add('tab-ativa');
            document.getElementById(`aba-${tabId}`).classList.remove('hidden');
            
            // Carregar conteúdo da aba
            await carregarConteudoAba(tabId);
        });
    });
}

async function carregarConteudoAba(tabId) {
    switch(tabId) {
        case 'sobre':
            await carregarAbaSobre();
            break;
        case 'produtos':
            await carregarAbaProdutos();
            break;
        case 'publicacoes':
            await carregarAbaPublicacoes();
            break;
        case 'avaliacoes':
            await carregarAbaAvaliacoes();
            break;
        case 'localizacao':
            await carregarAbaLocalizacao();
            break;
    }
}

// ========================================
// ABA: SOBRE
// ========================================

async function carregarAbaSobre() {
    // Descrição (usar biografia do PerfilSocial se existir, ou descrição padrão)
    const descricao = fornecedorAtual.biografia || 
                     `${fornecedorAtual.nome} é uma empresa fornecedora cadastrada no Radar Fornecedor.`;
    document.getElementById('descricaoEmpresa').textContent = descricao;
    
    // Email
    document.getElementById('emailEmpresa').textContent = fornecedorAtual.email || 'Não informado';
    
    // Telefone
    document.getElementById('telefoneEmpresa').textContent = formatarTelefone(fornecedorAtual.telefone) || 'Não informado';
    
    // Prazo de entrega
    const prazo = fornecedorAtual.prazoEntregaDias ? 
                 `${fornecedorAtual.prazoEntregaDias} dias úteis` : 
                 'Não informado';
    document.getElementById('prazoEntrega').textContent = prazo;
    
    // Aceita CPF
    document.getElementById('aceitaCpf').textContent = fornecedorAtual.aceitaCpf ? 'Sim' : 'Não';
}

// ========================================
// ABA: PRODUTOS
// ========================================

async function carregarAbaProdutos() {
    const container = document.getElementById('listaProdutos');
    container.innerHTML = '<div class="col-span-full text-center py-8"><i class="fas fa-spinner fa-spin text-2xl"></i></div>';
    
    try {
        const response = await fetch(`/api/produtos/fornecedor/${fornecedorId}`);
        if (!response.ok) throw new Error('Erro ao carregar produtos');
        
        const produtos = await response.json();
        
        // Atualizar contador
        document.getElementById('contadorProdutos').textContent = produtos.length;
        
        if (produtos.length === 0) {
            container.innerHTML = `
                <div class="col-span-full text-center py-12 text-gray-500">
                    <i class="fas fa-box-open text-5xl mb-4"></i>
                    <p class="text-lg">Nenhum produto cadastrado ainda</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = produtos.map(produto => `
            <div class="produto-card bg-white rounded-lg shadow-md overflow-hidden transition duration-300 cursor-pointer"
                 onclick="window.location.href='/pages/produto-detalhes.html?id=${produto.id}'">
                <div class="h-48 bg-gray-200 flex items-center justify-center overflow-hidden">
                    ${produto.foto ? 
                        `<img src="data:image/jpeg;base64,${produto.foto}" alt="${produto.nome}" class="w-full h-full object-cover">` :
                        `<i class="fas fa-image text-gray-400 text-4xl"></i>`
                    }
                </div>
                <div class="p-4">
                    <h3 class="font-semibold text-gray-800 mb-2 truncate">${produto.nome}</h3>
                    <p class="text-gray-600 text-sm mb-2 line-clamp-2">${produto.descricao || 'Sem descrição'}</p>
                    <p class="text-[#d4af37] font-bold text-lg">R$ ${produto.preco ? produto.preco.toFixed(2) : '0.00'}</p>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Erro ao carregar produtos:', error);
        container.innerHTML = `
            <div class="col-span-full text-center py-12 text-red-500">
                <i class="fas fa-exclamation-circle text-4xl mb-4"></i>
                <p>Erro ao carregar produtos</p>
            </div>
        `;
    }
}

// ========================================
// ABA: PUBLICAÇÕES
// ========================================

async function carregarAbaPublicacoes() {
    const container = document.getElementById('listaPublicacoes');
    container.innerHTML = '<div class="text-center py-8"><i class="fas fa-spinner fa-spin text-2xl"></i></div>';
    
    try {
        const response = await fetch(`/api/publicacoes/fornecedor/${fornecedorId}`);
        if (!response.ok) throw new Error('Erro ao carregar publicações');
        
        const publicacoes = await response.json();
        
        // Atualizar contador
        document.getElementById('contadorPublicacoes').textContent = publicacoes.length;
        
        if (publicacoes.length === 0) {
            container.innerHTML = `
                <div class="text-center py-12 text-gray-500">
                    <i class="fas fa-newspaper text-5xl mb-4"></i>
                    <p class="text-lg">Nenhuma publicação ainda</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = publicacoes.map(pub => `
            <div class="bg-white rounded-lg shadow-md p-6">
                <div class="flex items-start justify-between mb-4">
                    <div class="flex items-center gap-3">
                        <img src="${pub.foto ? `data:image/jpeg;base64,${pub.foto}` : '/img/empresa-default.png'}" 
                             alt="${pub.fornecedorNome}" 
                             class="w-12 h-12 rounded-full object-cover">
                        <div>
                            <p class="font-semibold">${pub.fornecedorNome || fornecedorAtual.nome}</p>
                            <p class="text-sm text-gray-500">${formatarData(pub.criadoEm)}</p>
                        </div>
                    </div>
                </div>
                <p class="text-gray-700 mb-4">${pub.texto}</p>
                ${pub.imagem ? `
                    <img src="data:image/jpeg;base64,${pub.imagem}" 
                         alt="Publicação" 
                         class="w-full rounded-lg mb-4">
                ` : ''}
                <div class="flex items-center gap-4 text-gray-500">
                    <span><i class="far fa-heart mr-1"></i> ${pub.curtidas || 0}</span>
                    <span><i class="far fa-comment mr-1"></i> ${pub.comentarios || 0}</span>
                </div>
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Erro ao carregar publicações:', error);
        container.innerHTML = `
            <div class="text-center py-12 text-red-500">
                <i class="fas fa-exclamation-circle text-4xl mb-4"></i>
                <p>Erro ao carregar publicações</p>
            </div>
        `;
    }
}

// ========================================
// ABA: AVALIAÇÕES
// ========================================

async function carregarAbaAvaliacoes() {
    await Promise.all([
        carregarEstatisticasAvaliacoes(),
        carregarFormularioAvaliacao(),
        carregarListaAvaliacoes()
    ]);
}

async function carregarEstatisticasAvaliacoes() {
    const container = document.getElementById('estatisticasAvaliacoes');
    
    try {
        const response = await fetch(`/api/avaliacoes-fornecedor/${fornecedorId}/estatisticas`);
        if (!response.ok) throw new Error('Erro ao carregar estatísticas');
        
        const stats = await response.json();
        const media = stats.mediaNotas || 0;
        const total = stats.totalAvaliacoes || 0;
        const distribuicao = stats.distribuicao || {};
        
        container.innerHTML = `
            <div class="grid md:grid-cols-3 gap-6">
                <div class="text-center">
                    <p class="text-5xl font-bold text-[#d4af37] mb-2">${media.toFixed(1)}</p>
                    <div class="text-2xl mb-2">${gerarEstrelas(media)}</div>
                    <p class="text-gray-600">${total} ${total === 1 ? 'avaliação' : 'avaliações'}</p>
                </div>
                <div class="md:col-span-2">
                    ${[5,4,3,2,1].map(nota => {
                        const count = distribuicao[nota] || 0;
                        const percentual = total > 0 ? (count / total) * 100 : 0;
                        return `
                            <div class="flex items-center gap-3 mb-2">
                                <span class="w-8 text-sm text-gray-600">${nota} ★</span>
                                <div class="flex-1 h-4 bg-gray-200 rounded-full overflow-hidden">
                                    <div class="h-full bg-[#d4af37]" style="width: ${percentual}%"></div>
                                </div>
                                <span class="w-12 text-sm text-gray-600 text-right">${count}</span>
                            </div>
                        `;
                    }).join('')}
                </div>
            </div>
        `;
        
    } catch (error) {
        console.error('Erro ao carregar estatísticas:', error);
        container.innerHTML = '<p class="text-gray-500 text-center">Nenhuma avaliação ainda</p>';
    }
}

async function carregarFormularioAvaliacao() {
    const container = document.getElementById('formNovaAvaliacao');
    
    // Não exibir formulário se não estiver logado
    if (!usuarioLogado) {
        container.style.display = 'none';
        return;
    }
    
    // Verificar se usuário é o próprio fornecedor
    const eProprioFornecedor = usuarioLogado.cnpjOuCpf === fornecedorAtual.cnpj;
    if (eProprioFornecedor) {
        container.style.display = 'none';
        return;
    }
    
    // Verificar se já avaliou
    try {
        const response = await fetch(`/api/avaliacoes-fornecedor/${fornecedorId}/ja-avaliou`);
        if (!response.ok) throw new Error('Erro ao verificar avaliação');
        
        const resultado = await response.json();
        if (resultado.jaAvaliou) {
            container.style.display = 'none';
            return;
        }
        
        // Exibir formulário
        container.style.display = 'block';
        container.innerHTML = `
            <h3 class="text-xl font-bold text-gray-800 mb-4">Avaliar esta empresa</h3>
            <form id="formAvaliar" class="space-y-4">
                <div>
                    <label class="block text-gray-700 mb-2">Sua nota:</label>
                    <div class="flex gap-2">
                        ${[1,2,3,4,5].map(n => `
                            <button type="button" class="estrela-selecao text-3xl text-gray-300 hover:text-[#d4af37]" data-nota="${n}">
                                <i class="fas fa-star"></i>
                            </button>
                        `).join('')}
                    </div>
                    <input type="hidden" id="notaSelecionada" required>
                </div>
                <div>
                    <label for="comentarioAvaliacao" class="block text-gray-700 mb-2">Comentário (opcional):</label>
                    <textarea id="comentarioAvaliacao" rows="4" maxlength="500" 
                              class="w-full border rounded-lg px-4 py-2 focus:outline-none focus:ring-2 focus:ring-[#d4af37]"
                              placeholder="Compartilhe sua experiência com esta empresa..."></textarea>
                    <p class="text-sm text-gray-500 mt-1"><span id="contadorComentario">0</span>/500 caracteres</p>
                </div>
                <button type="submit" class="bg-[#d4af37] hover:bg-[#c4a037] text-white px-6 py-3 rounded-lg font-semibold">
                    Enviar Avaliação
                </button>
            </form>
        `;
        
        // Eventos do formulário
        inicializarFormularioAvaliacao();
        
    } catch (error) {
        console.error('Erro ao carregar formulário:', error);
        container.style.display = 'none';
    }
}

function inicializarFormularioAvaliacao() {
    // Seleção de estrelas
    const estrelas = document.querySelectorAll('.estrela-selecao');
    estrelas.forEach(estrela => {
        estrela.addEventListener('click', () => {
            const nota = parseInt(estrela.dataset.nota);
            document.getElementById('notaSelecionada').value = nota;
            
            // Atualizar visual
            estrelas.forEach((e, i) => {
                if (i < nota) {
                    e.classList.remove('text-gray-300');
                    e.classList.add('text-[#d4af37]');
                } else {
                    e.classList.add('text-gray-300');
                    e.classList.remove('text-[#d4af37]');
                }
            });
        });
    });
    
    // Contador de caracteres
    const textarea = document.getElementById('comentarioAvaliacao');
    const contador = document.getElementById('contadorComentario');
    textarea.addEventListener('input', () => {
        contador.textContent = textarea.value.length;
    });
    
    // Submit do formulário
    document.getElementById('formAvaliar').addEventListener('submit', async (e) => {
        e.preventDefault();
        await enviarAvaliacao();
    });
}

async function enviarAvaliacao() {
    const nota = document.getElementById('notaSelecionada').value;
    const comentario = document.getElementById('comentarioAvaliacao').value;
    
    if (!nota) {
        alert('Por favor, selecione uma nota de 1 a 5 estrelas');
        return;
    }
    
    try {
        const response = await fetch('/api/avaliacoes-fornecedor', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fornecedorId: fornecedorId,
                nota: parseInt(nota),
                comentario: comentario || null
            })
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao enviar avaliação');
        }
        
        alert('Avaliação enviada com sucesso!');
        
        // Recarregar aba de avaliações
        await carregarAbaAvaliacoes();
        
    } catch (error) {
        console.error('Erro ao enviar avaliação:', error);
        alert(error.message);
    }
}

async function carregarListaAvaliacoes() {
    const container = document.getElementById('listaAvaliacoes');
    container.innerHTML = '<div class="text-center py-8"><i class="fas fa-spinner fa-spin text-2xl"></i></div>';
    
    try {
        const response = await fetch(`/api/avaliacoes-fornecedor/${fornecedorId}`);
        if (!response.ok) throw new Error('Erro ao carregar avaliações');
        
        const avaliacoes = await response.json();
        
        if (avaliacoes.length === 0) {
            container.innerHTML = `
                <div class="text-center py-12 text-gray-500">
                    <i class="fas fa-star text-5xl mb-4"></i>
                    <p class="text-lg">Nenhuma avaliação ainda</p>
                    <p class="text-sm">Seja o primeiro a avaliar esta empresa!</p>
                </div>
            `;
            return;
        }
        
        container.innerHTML = avaliacoes.map(av => `
            <div class="avaliacao-card bg-white rounded-lg shadow-md p-6">
                <div class="flex items-start justify-between mb-4">
                    <div class="flex items-center gap-3">
                        <div class="w-12 h-12 rounded-full bg-gray-300 flex items-center justify-center">
                            <i class="fas fa-user text-gray-600"></i>
                        </div>
                        <div>
                            <p class="font-semibold">${av.nomeAvaliador}</p>
                            <p class="text-sm text-gray-500">${av.tipoAvaliador}</p>
                        </div>
                    </div>
                    <div class="text-right">
                        <div class="text-[#d4af37] mb-1">${gerarEstrelas(av.nota)}</div>
                        <p class="text-sm text-gray-500">${formatarData(av.criadoEm)}</p>
                    </div>
                </div>
                ${av.comentario ? `<p class="text-gray-700 mb-4">${av.comentario}</p>` : ''}
                ${av.respostaFornecedor ? `
                    <div class="bg-gray-50 rounded-lg p-4 mt-4 border-l-4 border-[#d4af37]">
                        <p class="font-semibold text-gray-800 mb-2">
                            <i class="fas fa-reply text-[#d4af37] mr-2"></i>
                            Resposta do fornecedor:
                        </p>
                        <p class="text-gray-700">${av.respostaFornecedor}</p>
                        <p class="text-sm text-gray-500 mt-2">${formatarData(av.dataResposta)}</p>
                    </div>
                ` : ''}
                ${podeResponderAvaliacao(av) ? `
                    <button onclick="responderAvaliacao(${av.id})" 
                            class="mt-4 text-[#d4af37] hover:text-[#c4a037] font-semibold">
                        <i class="fas fa-reply mr-2"></i>Responder
                    </button>
                ` : ''}
            </div>
        `).join('');
        
    } catch (error) {
        console.error('Erro ao carregar avaliações:', error);
        container.innerHTML = `
            <div class="text-center py-12 text-red-500">
                <i class="fas fa-exclamation-circle text-4xl mb-4"></i>
                <p>Erro ao carregar avaliações</p>
            </div>
        `;
    }
}

function podeResponderAvaliacao(avaliacao) {
    // Apenas o fornecedor dono do perfil pode responder
    if (!usuarioLogado) return false;
    return usuarioLogado.cnpjOuCpf === fornecedorAtual.cnpj;
}

window.responderAvaliacao = async function(avaliacaoId) {
    const resposta = prompt('Digite sua resposta:');
    if (!resposta) return;
    
    try {
        const response = await fetch(`/api/avaliacoes-fornecedor/${avaliacaoId}/resposta`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ resposta })
        });
        
        if (!response.ok) {
            const erro = await response.json();
            throw new Error(erro.erro || 'Erro ao responder');
        }
        
        alert('Resposta publicada com sucesso!');
        await carregarListaAvaliacoes();
        
    } catch (error) {
        console.error('Erro ao responder:', error);
        alert(error.message);
    }
};

// ========================================
// ABA: LOCALIZAÇÃO
// ========================================

async function carregarAbaLocalizacao() {
    const container = document.getElementById('enderecoCompleto');
    
    const endereco = [
        fornecedorAtual.logradouro,
        fornecedorAtual.numero,
        fornecedorAtual.complemento,
        fornecedorAtual.bairro,
        fornecedorAtual.cidade,
        fornecedorAtual.estado,
        fornecedorAtual.cep
    ].filter(Boolean).join(', ');
    
    container.innerHTML = `
        <p class="text-gray-700 text-lg">
            <i class="fas fa-map-marker-alt text-[#d4af37] mr-2"></i>
            ${endereco || 'Endereço não informado'}
        </p>
    `;
    
    // TODO: Integrar mapa se latitude/longitude disponíveis
}

// ========================================
// UTILIDADES
// ========================================

function formatarCNPJ(cnpj) {
    if (!cnpj) return '-';
    return cnpj.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})$/, '$1.$2.$3/$4-$5');
}

function formatarTelefone(telefone) {
    if (!telefone) return null;
    return telefone.replace(/^(\d{2})(\d{5})(\d{4})$/, '($1) $2-$3');
}

function formatarData(dataISO) {
    if (!dataISO) return '';
    const data = new Date(dataISO);
    return data.toLocaleDateString('pt-BR') + ' às ' + data.toLocaleTimeString('pt-BR', {hour: '2-digit', minute: '2-digit'});
}

function gerarEstrelas(nota) {
    const cheias = Math.floor(nota);
    const meia = nota % 1 >= 0.5 ? 1 : 0;
    const vazias = 5 - cheias - meia;
    
    return '★'.repeat(cheias) + 
           (meia ? '½' : '') + 
           '☆'.repeat(vazias);
}

function exibirEstrelas(elementId, nota) {
    const container = document.getElementById(elementId);
    const estrelas = container.querySelectorAll('i');
    
    estrelas.forEach((estrela, index) => {
        if (index < Math.floor(nota)) {
            estrela.classList.remove('estrela-inativa');
            estrela.classList.add('estrela-ativa');
        } else {
            estrela.classList.add('estrela-inativa');
            estrela.classList.remove('estrela-ativa');
        }
    });
}

function abrirContato() {
    if (fornecedorAtual.telefone) {
        window.location.href = `tel:${fornecedorAtual.telefone}`;
    } else if (fornecedorAtual.email) {
        window.location.href = `mailto:${fornecedorAtual.email}`;
    } else {
        alert('Informações de contato não disponíveis');
    }
}

function mostrarErro(mensagem) {
    document.body.innerHTML = `
        <div class="flex items-center justify-center min-h-screen bg-gray-100">
            <div class="text-center">
                <i class="fas fa-exclamation-triangle text-6xl text-red-500 mb-4"></i>
                <h1 class="text-2xl font-bold text-gray-800 mb-2">Erro</h1>
                <p class="text-gray-600">${mensagem}</p>
                <a href="/" class="mt-4 inline-block bg-[#d4af37] text-white px-6 py-2 rounded-lg hover:bg-[#c4a037]">
                    Voltar para Home
                </a>
            </div>
        </div>
    `;
}
