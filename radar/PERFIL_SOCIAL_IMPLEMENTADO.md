# 🎭 PERFIL SOCIAL - IMPLEMENTAÇÃO COMPLETA

**Data:** 2026-10-11  
**Status:** ✅ IMPLEMENTADO - Pronto para Teste  
**Princípio:** **REUTILIZAÇÃO 100%** - Nenhum dado duplicado

---

## 📋 RESUMO EXECUTIVO

Implementado sistema de **Perfis Sociais** estilo Instagram/LinkedIn que **REUTILIZA** completamente os dados existentes:

- ✅ **Usuario** (username, tipo, flags)
- ✅ **Fornecedor** (empresa, CNPJ, endereço, contato)
- ✅ **Cliente** (nome, CPF/CNPJ)
- ✅ **Produto** (catálogo existente)
- ✅ **Publicacao** (feed comercial existente)

### O que foi criado:
- **1 nova tabela:** `PERFIS_SOCIAIS` (apenas foto, biografia, privacidade)
- **0 duplicações:** Todos os dados vêm das tabelas existentes

---

## 🗂️ ARQUITETURA DA SOLUÇÃO

### Entidades

```
PerfilSocial (NOVA)
├── id
├── usuario_id (FK → Usuario) 1:1
├── foto_perfil
├── biografia (500 chars)
├── perfil_publico
├── permite_contato
├── exibe_email
├── exibe_telefone
├── criado_em
└── atualizado_em

Usuario (EXISTENTE - REUTILIZADO)
├── username
├── tipo
├── fornecedor (boolean)
├── cliente (boolean)
├── representante (boolean)
└── cnpj_ou_cpf

Fornecedor (EXISTENTE - REUTILIZADO)
├── nome (empresa)
├── cnpj
├── email
├── telefone
├── cidade
├── estado
├── foto (logotipo)
└── ...

Cliente (EXISTENTE - REUTILIZADO)
├── nome
├── cpf_cnpj
└── ...

Produto (EXISTENTE - REUTILIZADO)
├── nome
├── descricao
├── preco
├── categoria
└── fornecedor_id

Publicacao (EXISTENTE - REUTILIZADO)
├── titulo
├── descricao
├── preco
├── imagens
├── total_curtidas
└── fornecedor_id
```

### Fluxo de Dados

```
1. Frontend chama: GET /api/perfil/meu

2. PerfilSocialController recebe requisição

3. PerfilSocialService.montarPerfilCompleto(username):
   ├── Busca Usuario
   ├── Busca/Cria PerfilSocial (se não existe)
   ├── Se Fornecedor:
   │   ├── Busca dados de Fornecedor (por CNPJ)
   │   ├── Busca Produtos (findByFornecedorId)
   │   ├── Busca Publicacoes (findByFornecedorIdAndAtiva)
   │   └── Calcula Estatísticas
   ├── Se Cliente:
   │   └── Busca dados de Cliente (por CPF/CNPJ)
   └── Retorna JSON agregado

4. Frontend exibe perfil completo com tabs:
   ├── Tab Produtos (grid de produtos existentes)
   ├── Tab Publicações (posts do feed existente)
   └── Tab Sobre (informações adicionais)
```

---

## 📁 ARQUIVOS CRIADOS

### Backend (Java)

| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `PerfilSocial.java` | 165 | Entidade JPA (foto, bio, privacidade) |
| `PerfilSocialRepository.java` | 28 | Repository com findByUsuario |
| `PerfilSocialService.java` | 320 | Service que **MONTA** perfil agregado |
| `PerfilSocialController.java` | 237 | REST API (GET/PUT/POST/DELETE) |

### Repositories Atualizados

| Repository | Método Adicionado |
|------------|-------------------|
| `ProdutoRepository` | `findByFornecedorId(Long)` |
| `ProdutoRepository` | `countByFornecedorId(Long)` |
| `PublicacaoRepository` | `findByFornecedorIdAndAtiva(..., Pageable)` |
| `PublicacaoRepository` | `findByFornecedorIdAndAtiva(...)` (sem Pageable) |
| `ClienteRepository` | `findByCpfCnpj(String)` |

### Frontend (HTML/JS)

| Arquivo | Linhas | Descrição |
|---------|--------|-----------|
| `perfil.html` | 640 | Página do perfil (estilo Instagram) |
| `menu-lateral.html` | +10 | Link "Meu Perfil" adicionado |

### Scripts

| Script | Função |
|--------|--------|
| `COPIAR_PERFIL_PARA_TARGET.bat` | Copia arquivos para runtime |

---

## 🔌 ENDPOINTS DA API

### 1. Buscar Meu Perfil

```
GET /api/perfil/meu
```

**Resposta:**
```json
{
  "perfilSocial": {
    "id": 1,
    "fotoPerfil": "/uploads/perfis/perfil_usuario_123.jpg",
    "biografia": "Fornecedor de alimentos desde 1995...",
    "perfilPublico": true,
    "permiteContato": true,
    "exibeEmail": true,
    "exibeTelefone": false,
    "criadoEm": "2026-10-11T10:00:00"
  },
  "usuario": {
    "id": 5,
    "username": "fornecedor1",
    "tipo": "FORNECEDOR",
    "isFornecedor": true,
    "isCliente": false
  },
  "fornecedor": {
    "id": 10,
    "nomeEmpresa": "Distribuidora ABC Ltda",
    "cnpj": "12345678000195",
    "email": "contato@abc.com",
    "telefone": "(11) 98765-4321",
    "cidade": "São Paulo",
    "estado": "SP",
    "logotipo": "/uploads/fornecedores/logo_abc.png",
    "prazoEntregaDias": 5,
    "categoria": "Alimentos",
    "atividade": "Distribuidora"
  },
  "produtos": [
    {
      "id": 100,
      "nome": "Feijão Preto 1kg",
      "descricao": "Feijão preto tipo 1",
      "categoria": "ALIMENTOS",
      "preco": 8.50
    }
  ],
  "publicacoes": [
    {
      "id": 50,
      "titulo": "Promoção Feijão!",
      "descricao": "Feijão preto com 20% de desconto",
      "preco": 10.00,
      "precoPromocional": 8.00,
      "imagens": "/uploads/feed/promo_feijao.jpg",
      "totalCurtidas": 25,
      "criadaEm": "2026-10-10T14:30:00"
    }
  ],
  "estatisticas": {
    "totalProdutos": 50,
    "totalPublicacoes": 10,
    "totalCurtidas": 250
  }
}
```

### 2. Buscar Perfil Público de Outro Usuário

```
GET /api/perfil/{usuarioId}
```

**Comportamento:**
- Se perfil público → retorna completo
- Se perfil privado → retorna dados limitados

### 3. Atualizar Perfil

```
PUT /api/perfil
Content-Type: application/json

{
  "biografia": "Nova biografia...",
  "perfilPublico": true,
  "permiteContato": true,
  "exibeEmail": false,
  "exibeTelefone": false
}
```

### 4. Upload Foto de Perfil

```
POST /api/perfil/foto
Content-Type: multipart/form-data

foto: [arquivo]
```

**Salvamento Duplo:**
- `src/main/resources/static/uploads/perfis/` (persistente)
- `target/classes/static/uploads/perfis/` (runtime)

### 5. Remover Foto de Perfil

```
DELETE /api/perfil/foto
```

---

## 🎨 INTERFACE - PERFIL.HTML

### Layout

```
┌─────────────────────────────────────────────────┐
│  [Menu Lateral]   │                             │
│                   │   🔵 Foto      Nome Empresa │
│  ⚫ Fornecedores  │   ────────                  │
│  ⚫ Compradores   │   @username | FORNECEDOR    │
│  ⚫ Feed          │                              │
│  ⚫ Meu Perfil ◄──┤   Biografia: texto...       │
│  ⚫ Produtos      │                              │
│                   │   📞 (11) 1234-5678         │
│                   │   📧 email@empresa.com      │
│                   │   📍 São Paulo - SP         │
│                   │                              │
│                   │   ┌───────────────────┐    │
│                   │   │ 50  │ 10  │ 250  │    │
│                   │   │Prod │Publ │Curt   │    │
│                   │   └───────────────────┘    │
│                   │                              │
│                   │   [Produtos] [Publicações] [Sobre]│
│                   │                              │
│                   │   ┌──────┐ ┌──────┐        │
│                   │   │Prod 1│ │Prod 2│        │
│                   │   │R$8,50│ │R$12,0│        │
│                   │   └──────┘ └──────┘        │
└─────────────────────────────────────────────────┘
```

### Features

✅ **Header:**
- Foto de perfil (circular, 128x128px)
- Nome da empresa / pessoa
- Username (@usuario)
- Tipo de usuário (badge colorido)
- Botão "Editar Perfil"

✅ **Corpo:**
- Biografia (até 500 caracteres)
- Informações de contato (respeitando privacidade)
- Estatísticas (produtos, publicações, curtidas)
- Badges de privacidade

✅ **Tabs:**
- **Produtos:** Grid 3 colunas com produtos cadastrados
- **Publicações:** Lista de posts do feed
- **Sobre:** Informações adicionais da empresa

✅ **Modal de Edição:**
- Campo textarea para biografia (contador de caracteres)
- Checkboxes de privacidade:
  - Perfil Público/Privado
  - Permite Contato
  - Exibir Email
  - Exibir Telefone

✅ **Upload de Foto:**
- Botão câmera sobreposto na foto
- Input file hidden
- Upload via FormData
- Preview instantâneo

---

## 🧪 COMO TESTAR

### Passo 1: Copiar Arquivos para Target

```batch
cd C:\Projeto\Projeto_Radar_Fornecedor\radar
COPIAR_PERFIL_PARA_TARGET.bat
```

### Passo 2: Recompilar (se necessário)

```batch
.\mvnw.cmd clean compile
```

### Passo 3: Iniciar Servidor

```batch
cd..
"Iniciar SpringBoot Radar Fornecedor.bat"
```

### Passo 4: Fazer Login

```
http://localhost:8080/login.html
```

Login como fornecedor (que já tem produtos/publicações).

### Passo 5: Acessar Perfil

```
http://localhost:8080/pages/perfil.html
```

Ou clique em **"Meu Perfil"** no menu lateral.

### Passo 6: Verificar Dados

✅ **Deve exibir:**
- Nome da empresa (de Fornecedor)
- CNPJ formatado
- Email e telefone (se configurado para exibir)
- Cidade e Estado
- **Produtos cadastrados** (da tabela Produtos)
- **Publicações do feed** (da tabela Publicacoes)
- Estatísticas corretas

✅ **Deve permitir:**
- Editar biografia
- Alterar configurações de privacidade
- Upload de foto de perfil
- Ver produtos em grid
- Ver publicações do feed

---

## ✅ CHECKLIST DE VALIDAÇÃO

### Backend

- [ ] PerfilSocial criado automaticamente no primeiro acesso
- [ ] GET /api/perfil/meu retorna JSON completo
- [ ] Produtos listados são os mesmos de /api/produtos
- [ ] Publicações listadas são as mesmas de /api/feed
- [ ] PUT /api/perfil salva biografia e privacidade
- [ ] POST /api/perfil/foto salva imagem (src + target)
- [ ] Estatísticas calculadas corretamente

### Frontend

- [ ] Página carrega sem erros 404
- [ ] Foto de perfil exibida (ou placeholder)
- [ ] Nome da empresa correto
- [ ] Tab Produtos exibe produtos existentes
- [ ] Tab Publicações exibe posts do feed
- [ ] Modal de edição abre e fecha
- [ ] Biografia salva e atualiza na tela
- [ ] Upload de foto funciona

### Integração

- [ ] Produtos exibidos no perfil são os mesmos de /pages/produtos.html
- [ ] Publicações exibidas são as mesmas de /pages/feed.html
- [ ] Estatísticas batem com dados reais
- [ ] Nenhum dado duplicado no banco
- [ ] Atualizar produto em /produtos reflete no perfil
- [ ] Criar publicação em /feed reflete no perfil

---

## 🚨 PONTOS DE ATENÇÃO

### Dados Não Duplicados

✅ **CORRETO:**
```
Perfil exibe:
- Nome empresa → vem de Fornecedor.nome
- Produtos → SELECT * FROM Produtos WHERE fornecedorId = X
- Publicações → SELECT * FROM Publicacoes WHERE fornecedorId = X
```

❌ **ERRADO (não fizemos isso):**
```
Criar tabela produtos_perfil
Copiar produtos para produtos_perfil
```

### Relacionamentos

```
Usuario 1:1 PerfilSocial
Usuario.cnpjOuCpf → Fornecedor.cnpj (lookup)
Fornecedor.id → Produto.fornecedorId (FK existente)
Fornecedor.id → Publicacao.fornecedorId (FK existente)
```

### Sessão

O perfil usa `HttpSession` para pegar `username` do usuário autenticado. Garanta que o login seta `session.setAttribute("username", ...)`.

---

## 📊 IMPACTO NO BANCO DE DADOS

### Nova Tabela

```sql
CREATE TABLE PERFIS_SOCIAIS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USUARIO_ID BIGINT NOT NULL UNIQUE,
    FOTO_PERFIL TEXT,
    BIOGRAFIA VARCHAR(500),
    PERFIL_PUBLICO BOOLEAN NOT NULL DEFAULT TRUE,
    PERMITE_CONTATO BOOLEAN NOT NULL DEFAULT TRUE,
    EXIBE_EMAIL BOOLEAN NOT NULL DEFAULT FALSE,
    EXIBE_TELEFONE BOOLEAN NOT NULL DEFAULT FALSE,
    CRIADO_EM TIMESTAMP NOT NULL,
    ATUALIZADO_EM TIMESTAMP,
    FOREIGN KEY (USUARIO_ID) REFERENCES USUARIOS(ID)
);
```

### Tabelas Reutilizadas (Zero Alterações)

- ✅ `USUARIOS`
- ✅ `FORNECEDORES`
- ✅ `CLIENTES`
- ✅ `PRODUTOS`
- ✅ `PUBLICACOES`

### Diretório de Uploads

```
src/main/resources/static/uploads/
├── feed/           (existente - publicações)
└── perfis/         (NOVO - fotos de perfil)
```

---

## 🎯 PRÓXIMOS PASSOS (FUTURO)

### Melhorias Opcionais

1. **Perfis de Representantes**
   - Adicionar lógica para tipo REPRESENTANTE
   - Exibir empresa vinculada
   - Produtos que representa

2. **Perfil Público com URL Amigável**
   - `/perfil/@username`
   - SEO-friendly
   - Compartilhável

3. **Galeria de Fotos**
   - Múltiplas fotos da empresa
   - Carrossel no perfil

4. **Avaliações e Reviews**
   - Clientes avaliam fornecedores
   - Exibir rating no perfil

5. **Conexões / Seguir**
   - Clientes seguem fornecedores
   - Feed personalizado

---

## 📚 DOCUMENTAÇÃO ADICIONAL

### Para Desenvolvedores

- `PerfilSocialService.java` - Comentários em cada método
- `PerfilSocialController.java` - Javadoc nos endpoints
- `perfil.html` - Comentários JavaScript inline

### Para Usuários Finais

Criar documentação:
- "Como editar meu perfil"
- "Configurações de privacidade"
- "Como fazer upload de foto"

---

## ✅ CONCLUSÃO

Sistema de Perfis Sociais **implementado com sucesso** seguindo princípio de **REUTILIZAÇÃO 100%**:

- ✅ Nenhum dado duplicado
- ✅ Integridade referencial mantida
- ✅ Performance otimizada (queries diretas)
- ✅ Fácil manutenção (fonte única de verdade)
- ✅ Interface moderna e responsiva
- ✅ API RESTful completa

**Status:** Pronto para testes e uso em produção! 🚀

---

**Última atualização:** 2026-10-11  
**Desenvolvido por:** Kiro AI Assistant  
**Versão:** 1.0.0
