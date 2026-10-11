# Recursos de Administração de Perfis - IMPLEMENTADO

**Data:** 06/10/2026
**Status:** ✅ Código criado, aguardando compilação e testes

---

## 📋 RESUMO DAS ALTERAÇÕES

Implementados recursos para que:
1. **Administradores** possam visualizar e editar TODOS os perfis
2. **Fornecedores/Clientes** criem perfis automaticamente (sem aprovação)
3. **Usuários** editem livremente seus próprios perfis a qualquer momento

---

## 🔧 ARQUIVOS MODIFICADOS

### 1. **PerfilSocialService.java**
**Localização:** `src/main/java/br/com/radarfornecedor/radar/service/PerfilSocialService.java`

**Novos Métodos:**

```java
// Lista todos os perfis (admin only)
public List<Map<String, Object>> listarTodosPerfis()

// Busca perfil de outro usuário (admin vê tudo, user só público)
public Map<String, Object> buscarPerfilDeOutroUsuario(Long usuarioId, String username, String tipoUsuario)

// Admin atualiza perfil de qualquer usuário
public PerfilSocial atualizarPerfilPorUsuarioId(Long usuarioId, Map<String, Object> dados)
```

**Lógica de Negócio:**
- `listarTodosPerfis()`: Retorna lista com id, nome, tipo, biografia resumida, privacidade
- `buscarPerfilDeOutroUsuario()`: 
  - Se admin → retorna perfil completo (ignora privacidade)
  - Se user → retorna apenas se perfil público
- `atualizarPerfilPorUsuarioId()`: Admin pode alterar biografia, exibirEmail, exibirTelefone, exibirProdutos

---

### 2. **PerfilSocialController.java**
**Localização:** `src/main/java/br/com/radarfornecedor/radar/controller/PerfilSocialController.java`

**Novos Endpoints:**

```java
// GET /api/perfil/todos - Lista todos os perfis (admin only)
@GetMapping("/todos")
public ResponseEntity<List<Map<String, Object>>> listarTodosPerfis(...)

// PUT /api/perfil/{usuarioId} - Admin edita perfil de outro usuário
@PutMapping("/{usuarioId}")
public ResponseEntity<?> atualizarPerfilPorAdmin(@PathVariable Long usuarioId, ...)
```

**Controle de Acesso:**
```java
if (!"ADMIN".equals(tipoUsuario)) {
    return ResponseEntity.status(403).body("Acesso negado");
}
```

---

### 3. **admin-perfis.html** (NOVO)
**Localização:** `src/main/resources/static/pages/admin-perfis.html`

**Funcionalidades:**
- ✅ Tabela listando todos os usuários com perfil
- ✅ Colunas: Nome, Tipo, Biografia (prévia), Privacidade, Ações
- ✅ Filtros por tipo: Todos | Fornecedor | Cliente | Representante
- ✅ Botão "Editar" → abre modal com formulário
- ✅ Modal permite editar:
  - Biografia
  - Exibir Email (checkbox)
  - Exibir Telefone (checkbox)
  - Exibir Produtos (checkbox)
- ✅ Botão "Ver Perfil" → abre perfil completo em nova aba
- ✅ Design responsivo com Tailwind CSS
- ✅ Integração com menu lateral padrão

**Screenshot Lógica:**
```javascript
// Carregar todos os perfis
fetch('/api/perfil/todos')

// Editar perfil de outro usuário
fetch(`/api/perfil/${usuarioId}`, {
    method: 'PUT',
    body: JSON.stringify({
        biografia,
        exibirEmail,
        exibirTelefone,
        exibirProdutos
    })
})
```

---

## 🔐 CONTROLE DE ACESSO

### Perfis de Usuário

| Tipo | Criar Perfil | Ver Próprio | Editar Próprio | Ver Outros | Editar Outros |
|------|--------------|-------------|----------------|------------|---------------|
| **Fornecedor** | Auto | ✅ | ✅ | Só públicos | ❌ |
| **Cliente** | Auto | ✅ | ✅ | Só públicos | ❌ |
| **Representante** | Auto | ✅ | ✅ | Só públicos | ❌ |
| **Admin** | Auto | ✅ | ✅ | **TODOS** | **✅ TODOS** |

### Fluxo de Auto-Criação

```java
// PerfilSocialService.buscarPerfilCompleto()
PerfilSocial perfil = perfilSocialRepository.findByUsuario(usuario)
    .orElseGet(() -> {
        PerfilSocial novo = criarPerfilPadrao(usuario);
        return perfilSocialRepository.save(novo);
    });
```

**Características:**
- Primeiro acesso a `/pages/perfil.html` → cria perfil automaticamente
- Sem necessidade de aprovação ou autorização
- Perfil padrão: biografia vazia, foto padrão, privacidade parcial

---

## 🎯 ENDPOINTS DA API

### Usuário Normal

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/perfil/meu` | Busca próprio perfil (auto-cria) | User |
| PUT | `/api/perfil` | Atualiza próprio perfil | User |
| POST | `/api/perfil/foto` | Upload foto de perfil | User |
| GET | `/api/perfil/{id}` | Ver perfil de outro (se público) | User |

### Admin

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| GET | `/api/perfil/todos` | Lista TODOS os perfis | **Admin** |
| GET | `/api/perfil/{id}` | Ver qualquer perfil (ignora privacidade) | **Admin** |
| PUT | `/api/perfil/{usuarioId}` | Edita perfil de qualquer usuário | **Admin** |

---

## 🧪 CASOS DE TESTE

### Teste 1: Auto-Criação (Fornecedor)
```
1. Login como fornecedor (não admin)
2. Acesse /pages/perfil.html
3. ✅ ESPERADO: Perfil criado automaticamente
4. Edite biografia, salve
5. ✅ ESPERADO: Salvo sem aprovação
```

### Teste 2: Admin Lista Todos
```
1. Login como ADMIN
2. Acesse /pages/admin-perfis.html
3. ✅ ESPERADO: Tabela com todos os usuários
4. Clique filtro "Fornecedor"
5. ✅ ESPERADO: Só fornecedores aparecem
```

### Teste 3: Admin Edita Outro Perfil
```
1. Como admin em /pages/admin-perfis.html
2. Clique "Editar" no perfil de um fornecedor
3. Mude biografia para "Editado pelo admin"
4. Marque checkbox "Exibir Email"
5. Clique "Salvar Alterações"
6. ✅ ESPERADO: Perfil do fornecedor atualizado
7. Logout, login como fornecedor
8. Acesse próprio perfil
9. ✅ ESPERADO: Biografia mostra "Editado pelo admin"
```

### Teste 4: Usuário Normal Não Vê Admin Page
```
1. Login como fornecedor (não admin)
2. Tente acessar /pages/admin-perfis.html
3. ✅ ESPERADO: Menu lateral não mostra link para admin
4. Se forçar URL: GET /api/perfil/todos
5. ✅ ESPERADO: HTTP 403 Forbidden
```

---

## 📊 DADOS NO BANCO

### Tabela: PERFIS_SOCIAIS

```sql
CREATE TABLE PERFIS_SOCIAIS (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL UNIQUE,
    foto_perfil VARCHAR(500),
    biografia TEXT,
    exibir_email BOOLEAN DEFAULT FALSE,
    exibir_telefone BOOLEAN DEFAULT FALSE,
    exibir_produtos BOOLEAN DEFAULT TRUE,
    data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES USUARIOS(id)
);
```

**Observação:** Produtos e Publicações NÃO são duplicados. São buscados via JOIN das tabelas originais.

---

## 🚀 PRÓXIMOS PASSOS

1. ✅ Código implementado
2. ⏳ **COMPILAR:** Execute `mvnw.cmd clean compile`
3. ⏳ **REINICIAR:** Rode `Iniciar SpringBoot Radar Fornecedor.bat`
4. ⏳ **TESTAR:** Siga casos de teste acima
5. ⏳ **VALIDAR:** Zero duplicação de dados
6. ⏳ **COMMIT GIT:** Se tudo funcionar

---

## 🐛 TROUBLESHOOTING

### Erro: "Acesso negado" ao acessar /api/perfil/todos
**Causa:** Usuário não é admin
**Solução:** Verificar campo `tipo_usuario` na tabela USUARIOS. Deve ser exatamente "ADMIN" (case-sensitive)

### Perfil não abre
**Causa:** JavaScript com erro
**Solução:** Abrir console (F12), copiar erro, reportar

### Admin-perfis.html retorna 404
**Causa:** Arquivo não copiado para target/
**Solução:** Execute `xcopy /Y src\main\resources\static\pages\admin-perfis.html target\classes\static\pages\`

### Produtos não aparecem no perfil
**Causa:** Fornecedor sem produtos cadastrados
**Solução:** Vá em /produtos e cadastre um produto antes de testar

---

## 📝 CONFORMIDADE COM REGRAS DO PROJETO

✅ **Regra 01:** Banco DBLRadar.db mantido no caminho padrão  
✅ **Regra 02:** admin-perfis.html copiado para src/ e target/  
✅ **Regra 03:** admin-perfis.html segue padrão (menu-lateral, DOMContentLoaded)  
✅ **Regra 06:** Zero queries destrutivas, apenas INSERTs e UPDATEs  
✅ **Regra 09:** Sistema de cadastro não afetado

---

**FIM DO RELATÓRIO**
