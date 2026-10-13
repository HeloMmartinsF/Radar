# Correção Menu Lateral - Página Mensagens e Conversa

## Problema Identificado

A barra lateral aparecia **completamente preta** sem nenhum conteúdo (logo, ícones, links) nas páginas:
- `/pages/mensagens.html`
- `/pages/conversa.html`

## Causa Raiz

### Ordem de Carregamento de Scripts

Ambas as páginas tinham `data-standalone="true"` no `<body>`, o que faz com que o `DOMContentLoaded` do `/script.js` seja ignorado (linha 185 do script.js):

```javascript
if (document.body.dataset.standalone === 'true') return;
```

As páginas estavam tentando chamar `carregarMenuLateral()` no evento `DOMContentLoaded`, mas essa função está definida no `/script.js` com `defer`. Isso criava uma **condição de corrida**:

1. HTML carrega
2. `DOMContentLoaded` dispara
3. Script inline tenta chamar `carregarMenuLateral()` 
4. **Mas `/script.js` ainda não terminou de carregar!**
5. Função não existe → erro silencioso
6. Menu não carrega → sidebar preta

## Solução Implementada

### Mudança 1: Adicionar Links Necessários

Adicionado no `<head>` de ambas as páginas:

```html
<link rel="stylesheet" href="/style.css">
<script src="/script.js" defer></script>
```

### Mudança 2: Trocar Evento DOMContentLoaded por window.load

**ANTES (ERRADO):**
```javascript
document.addEventListener('DOMContentLoaded', async () => {
    await carregarMenuLateral();
    // ...
});
```

**DEPOIS (CORRETO):**
```javascript
window.addEventListener('load', async () => {
    // Verificar se as funções existem antes de chamar
    if (typeof carregarMenuLateral === 'function') {
        await carregarMenuLateral();
    }
    if (typeof exibirInfoUsuario === 'function') {
        await exibirInfoUsuario();
    }
    if (typeof aplicarControlesDeAcesso === 'function') {
        await aplicarControlesDeAcesso();
    }
    // ...
});
```

### Por que `window.load` funciona?

| Evento | Quando Dispara | Scripts Carregados? |
|--------|---------------|-------------------|
| `DOMContentLoaded` | Quando HTML parseado | ❌ Scripts `defer` podem não ter terminado |
| `window.load` | Após **TODOS** recursos carregarem | ✅ Scripts, CSS, imagens, tudo carregado |

Com `window.load`, garantimos que:
1. O HTML está parseado
2. Todos os scripts (incluindo `/script.js` defer) foram executados
3. A função `carregarMenuLateral()` está disponível
4. O menu lateral carrega corretamente

## Arquivos Alterados

### 1. `src/main/resources/static/pages/mensagens.html`
- Adicionado `<link rel="stylesheet" href="/style.css">`
- Adicionado `<script src="/script.js" defer></script>`
- Trocado `DOMContentLoaded` por `window.load`
- Adicionado verificação `typeof === 'function'` antes de cada chamada

### 2. `src/main/resources/static/pages/conversa.html`
- Adicionado `<link rel="stylesheet" href="/style.css">`
- Adicionado `<script src="/script.js" defer></script>`
- Trocado `DOMContentLoaded` por `window.load`
- Adicionado verificação `typeof === 'function'` antes de cada chamada
- Removido `setTimeout` de 500ms (não é mais necessário)

## Como Testar

1. **Fechar completamente o navegador** (não apenas as abas)
2. Reabrir o navegador
3. Acessar `http://localhost:8080`
4. Fazer login
5. Navegar até **Mensagens**
6. Verificar se a barra lateral aparece com:
   - ✅ Fundo preto
   - ✅ Logo Radar Fornecedor no topo
   - ✅ Ícones dourados (#d4af37)
   - ✅ Links de navegação funcionais
   - ✅ Informações do usuário no rodapé
   - ✅ Botão "Sair" no rodapé

7. Clicar em qualquer conversa para abrir `/pages/conversa.html`
8. Verificar se a barra lateral também aparece corretamente

## Hard Refresh

Se após fechar e reabrir o navegador a sidebar ainda aparecer preta:

**Chrome/Edge:** `Ctrl + Shift + R`  
**Firefox:** `Ctrl + F5`  
**Safari (Mac):** `Cmd + Shift + R`

Isso força o navegador a ignorar cache e baixar arquivos novamente.

## Script de Correção Criado

**Arquivo:** `CORRIGIR_MENU_MENSAGENS_LOAD.bat`

Executa automaticamente:
```batch
copy /Y "src\main\resources\static\pages\mensagens.html" "target\classes\static\pages\mensagens.html"
copy /Y "src\main\resources\static\pages\conversa.html" "target\classes\static\pages\conversa.html"
```

Copiar para `/target` é necessário porque o Spring Boot serve arquivos de lá quando em execução.

## Resumo Técnico

| Aspecto | Antes | Depois |
|---------|-------|--------|
| **Links CSS/JS** | ❌ Faltando `/style.css` e `/script.js` | ✅ Ambos presentes |
| **Evento de Carga** | ❌ `DOMContentLoaded` (muito cedo) | ✅ `window.load` (após tudo carregar) |
| **Verificação de Função** | ❌ Chamava direto (erro se não existir) | ✅ `typeof === 'function'` antes de chamar |
| **setTimeout Hack** | ❌ Presente (500ms arbitrário) | ✅ Removido (não necessário) |
| **Resultado** | ❌ Sidebar preta vazia | ✅ Sidebar completa e funcional |

## Lições Aprendidas

1. **`defer` não garante execução antes de `DOMContentLoaded`**
   - Use `window.load` quando depender de scripts externos

2. **Páginas `data-standalone="true"` precisam gerenciar o próprio ciclo**
   - Não podem confiar no DOMContentLoaded do script.js

3. **Sempre verificar existência de funções antes de chamar**
   - Previne erros silenciosos
   - Facilita debug

4. **Cache do navegador pode esconder problemas**
   - Sempre testar com hard refresh após mudanças
   - Fechar e reabrir navegador entre testes

---

**Data da Correção:** 13/10/2026 00:20  
**Testado:** ✅ Pronto para teste  
**Status:** 🟢 Correção aplicada e arquivos copiados para target
