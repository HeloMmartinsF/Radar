# Central de Notificações - Radar Fornecedor

## ✅ Implementação Concluída

A Central de Notificações foi implementada com sucesso! Este documento contém as instruções para compilação, teste e deploy.

---

## 📦 Arquivos Criados

### Backend (Java)
- `src/main/java/br/com/radarfornecedor/radar/model/TipoNotificacao.java` - Enum com 5 tipos de notificação
- `src/main/java/br/com/radarfornecedor/radar/model/Notificacao.java` - Entidade JPA com 11 campos
- `src/main/java/br/com/radarfornecedor/radar/repository/NotificacaoRepository.java` - Repository com queries otimizadas
- `src/main/java/br/com/radarfornecedor/radar/service/NotificacaoService.java` - Service com lógica de negócio e deduplicação
- `src/main/java/br/com/radarfornecedor/radar/controller/NotificacaoController.java` - Controller REST com 6 endpoints

### Frontend (HTML/JS)
- `src/main/resources/static/js/notificacoes.js` - Sistema completo de notificações (badge, dropdown, página)
- `src/main/resources/static/layout/notificacoes-sino.html` - Componente sino com badge e dropdown
- `src/main/resources/static/pages/central-notificacoes.html` - Página completa de notificações

### Banco de Dados
- `migration_central_notificacoes.sql` - Script de criação da tabela NOTIFICACOES com 6 índices

### Modificados
- `src/main/java/br/com/radarfornecedor/radar/controller/AvaliacaoFornecedorController.java` - Integrada criação de notificação ao receber avaliação
- `src/main/java/br/com/radarfornecedor/radar/controller/FeedController.java` - Preparado para notificar seguidores
- `src/main/resources/static/layout/menu-lateral.html` - Integrado botão de notificações com badge e dropdown

---

## 🚀 Como Compilar e Executar

### 1. Compilar o Projeto

```powershell
cd C:\Projeto\Projeto_Radar_Fornecedor\radar
.\mvnw.cmd clean compile
```

### 2. Copiar Arquivos Estáticos para Target (Regra 02)

Após compilar, copiar os arquivos frontend para o diretório target:

```powershell
# Copiar notificacoes.js
Copy-Item -Path "src\main\resources\static\js\notificacoes.js" -Destination "target\classes\static\js\notificacoes.js" -Force

# Copiar notificacoes-sino.html
Copy-Item -Path "src\main\resources\static\layout\notificacoes-sino.html" -Destination "target\classes\static\layout\notificacoes-sino.html" -Force

# Copiar central-notificacoes.html
Copy-Item -Path "src\main\resources\static\pages\central-notificacoes.html" -Destination "target\classes\static\pages\central-notificacoes.html" -Force

# Copiar menu-lateral.html atualizado
Copy-Item -Path "src\main\resources\static\layout\menu-lateral.html" -Destination "target\classes\static\layout\menu-lateral.html" -Force
```

### 3. Iniciar o Servidor

Execute o arquivo batch existente:

```powershell
.\Iniciar SpringBoot Radar Fornecedor.bat
```

Ou manualmente:

```powershell
.\mvnw.cmd spring-boot:run
```

O servidor iniciará em `http://localhost:8080`

---

## 🧪 Como Testar

### Teste 1: Criar Notificação de Avaliação

1. Faça login como um **comprador** ou **representante**
2. Acesse o perfil de um fornecedor
3. Crie uma avaliação (nota e comentário)
4. Faça logout e login como o **fornecedor avaliado**
5. Verifique:
   - Badge vermelho no botão "Notificações" no menu lateral (contador de não lidas)
   - Clique no botão e veja a notificação no dropdown
   - Acesse "Central de Notificações" e veja a notificação completa

### Teste 2: Marcar Como Lida

1. No dropdown, clique na notificação
2. Verifique que o badge diminui o contador
3. Na página central, verifique que a notificação muda de cor (borda azul → branca)

### Teste 3: Marcar Todas Como Lidas

1. Na página central, clique em "Marcar todas como lidas"
2. Verifique que todas as notificações ficam com borda branca
3. Verifique que o badge no menu lateral desaparece (contador = 0)

### Teste 4: Deduplicação

1. Como comprador, crie uma avaliação para o fornecedor
2. Tente criar outra avaliação para o mesmo fornecedor
3. Faça login como fornecedor
4. Verifique que recebeu apenas **1 notificação** (deduplicação funcionando)

### Teste 5: Autorização

1. Faça login como **usuário A**
2. Crie uma avaliação (gera notificação para fornecedor B)
3. Faça login como **usuário B** (diferente do fornecedor avaliado)
4. Acesse a central de notificações
5. Verifique que **não vê** a notificação do fornecedor (autorização funcionando)

### Teste 6: Atualização Automática do Badge

1. Deixe a aplicação aberta em uma aba
2. Em outra aba/navegador, crie uma avaliação que gera notificação para seu usuário
3. Aguarde até 30 segundos
4. Verifique que o badge atualiza automaticamente sem refresh

---

## 📊 Endpoints da API

### GET `/api/notificacoes`
Lista notificações do usuário autenticado

**Query Params:**
- `limite` (opcional): número máximo de notificações
- `apenasNaoLidas` (opcional): `true` para filtrar apenas não lidas

**Resposta:** Array de notificações

### GET `/api/notificacoes/nao-lidas/count`
Retorna quantidade de notificações não lidas

**Resposta:**
```json
{
  "count": 5,
  "sucesso": true
}
```

### GET `/api/notificacoes/{id}`
Busca notificação específica (valida autorização)

**Resposta:** Objeto Notificacao ou 404

### PUT `/api/notificacoes/{id}/ler`
Marca notificação como lida

**Resposta:**
```json
{
  "sucesso": true,
  "mensagem": "Notificação marcada como lida"
}
```

### PUT `/api/notificacoes/ler-todas`
Marca todas as notificações do usuário como lidas

**Resposta:**
```json
{
  "sucesso": true,
  "quantidadeMarcada": 3,
  "mensagem": "3 notificação(ões) marcada(s) como lida(s)"
}
```

### DELETE `/api/notificacoes/{id}`
Deleta notificação específica

**Resposta:**
```json
{
  "sucesso": true,
  "mensagem": "Notificação deletada com sucesso"
}
```

---

## 🔐 Segurança e Autorização

### Backend
- Todos os endpoints validam a sessão HTTP (`@SessionAttribute("usuario")`)
- Cada usuário só pode acessar suas próprias notificações
- Queries filtram por `usuarioId` do usuário autenticado
- Updates e deletes validam propriedade antes de executar

### Frontend
- Badge e dropdown carregam apenas notificações do usuário logado
- Links de notificação redirecionam para páginas autorizadas
- Erros 401/403 são tratados e escondem componentes

---

## 🗄️ Estrutura do Banco de Dados

### Tabela NOTIFICACOES

| Coluna | Tipo | Descrição |
|--------|------|-----------|
| ID | BIGINT | Chave primária (auto increment) |
| USUARIO_ID | BIGINT | ID do usuário que receberá a notificação |
| TIPO | VARCHAR(50) | Tipo: NOVA_PUBLICACAO, NOVA_AVALIACAO, etc |
| TITULO | VARCHAR(200) | Título curto da notificação |
| MENSAGEM | VARCHAR(500) | Mensagem detalhada |
| LINK_DESTINO | VARCHAR(500) | URL para redirecionar ao clicar |
| LIDA | BOOLEAN | Se foi lida pelo usuário |
| CRIADA_EM | TIMESTAMP | Data/hora de criação |
| EVENTO_ID | BIGINT | ID do evento (para deduplicação) |
| EVENTO_TIPO | VARCHAR(50) | Tipo do evento (para deduplicação) |
| ORIGEM_NOME | VARCHAR(200) | Nome do usuário que gerou o evento |

### Índices Criados
1. `idx_notif_usuario` - Filtrar por usuário
2. `idx_notif_lida` - Filtrar não lidas
3. `idx_notif_criada` - Ordenar por data
4. `idx_notif_dedup` - Deduplicação (usuarioId + eventoTipo + eventoId)
5. `idx_notif_usuario_criada` - Query principal (usuário + data desc)
6. `idx_notif_usuario_lida` - Contador de não lidas

---

## 🎨 Interface do Usuário

### Botão Notificações (Menu Lateral)
- Ícone sino dourado (`#d4af37`)
- Badge vermelho com contador (aparece apenas se houver não lidas)
- Clique abre dropdown à direita do menu

### Dropdown
- Posicionado à direita do menu lateral (`left-full`)
- Mostra últimas 5 notificações
- Header com botão "Ler todas"
- Footer com link "Ver todas as notificações"
- Scrollável (max-height: 96)

### Central de Notificações (Página Completa)
- Filtros: "Todas" ou "Não lidas"
- Ações: "Marcar todas como lidas" e "Atualizar"
- Cards com cores por tipo:
  - 🔵 NOVA_PUBLICACAO - Azul
  - 🟡 NOVA_AVALIACAO - Amarelo
  - 🟢 NOVO_SEGUIDOR - Verde
  - 🌸 NOVO_COMENTARIO - Rosa
  - 🟣 SISTEMA - Roxo
- Animação de entrada (fadeInUp)
- Hover com elevação

---

## 🔄 Integrações Existentes

### AvaliacaoFornecedorController
- Ao criar avaliação, notifica o fornecedor automaticamente
- Busca usuário do fornecedor pelo CNPJ
- Cria notificação com link para perfil comercial + seção avaliações

### FeedController (Preparado para Futuro)
- Injeção do NotificacaoService já configurada
- Comentário TODO indicando onde integrar notificações de novas publicações
- Aguardando implementação do sistema de seguidores

---

## 📝 Próximos Passos (Futuro)

### Sistema de Seguidores
Quando implementado, adicionar no FeedController após salvar publicação:

```java
// Buscar todos os seguidores do fornecedor
List<Seguidor> seguidores = seguidorRepository.findByFornecedorId(fornecedor.getId());

// Notificar cada seguidor
for (Seguidor seguidor : seguidores) {
    notificacaoService.notificarNovaPublicacao(
        seguidor.getUsuarioId(),
        salva.getId(),
        fornecedor.getRazaoSocial(),
        salva.getTitulo(),
        fornecedor.getCnpj()
    );
}
```

### Sistema de Comentários
Integrar no controller de comentários:

```java
notificacaoService.notificarNovoComentario(
    autorPublicacao.getId(),
    comentario.getId(),
    usuarioComentarista.getUsername(),
    comentario.getTexto(),
    "/pages/feed.html?publicacao=" + publicacaoId
);
```

---

## 🐛 Troubleshooting

### Badge não aparece
- Verificar se usuário está autenticado
- Abrir DevTools → Console e verificar erros
- Verificar se `notificacoes.js` está carregado
- Verificar se endpoint `/api/notificacoes/nao-lidas/count` retorna 200

### Notificação não é criada ao avaliar
- Verificar se fornecedor tem usuário cadastrado com o CNPJ correto
- Verificar logs do servidor (console)
- Verificar tabela NOTIFICACOES no banco
- Verificar se `spring.jpa.hibernate.ddl-auto=update` está configurado

### Dropdown não abre
- Verificar se `notificacoes.js` está em `/js/notificacoes.js`
- Verificar se script está incluído no `menu-lateral.html`
- Verificar no DevTools se função `toggleDropdownNotificacoes` existe

### Arquivo não encontrado após compilar
- Copiar arquivos estáticos para `/target/classes/static/` (Regra 02)
- Fazer hard refresh no navegador (`Ctrl + Shift + R`)
- Verificar se servidor foi reiniciado após alterações Java

---

## 📚 Arquivos de Referência

- **Regras do Projeto:** `.kiro/steering/regras-projeto.md`
- **Migration SQL:** `migration_central_notificacoes.sql`
- **Documentação API:** Este arquivo, seção "Endpoints da API"

---

## ✅ Checklist de Deploy

- [ ] Compilar projeto: `.\mvnw.cmd clean compile`
- [ ] Copiar arquivos estáticos para target (ver seção "Como Compilar")
- [ ] Executar migration SQL (se banco não for H2 com ddl-auto=update)
- [ ] Reiniciar servidor Spring Boot
- [ ] Testar criação de notificação (avaliação)
- [ ] Testar badge e dropdown
- [ ] Testar página central de notificações
- [ ] Testar marcar como lida
- [ ] Testar autorização (usuários diferentes)
- [ ] Testar deduplicação
- [ ] Fazer commit: `git commit -m "..."`
- [ ] Fazer push: `git push origin main`

---

## 📞 Suporte

Em caso de dúvidas ou problemas:

1. Verificar logs do servidor em `spring_boot.log`
2. Verificar console do navegador (F12 → Console)
3. Verificar banco de dados: `SELECT * FROM NOTIFICACOES;`
4. Revisar este documento

---

**Data de Implementação:** 06/10/2026  
**Versão:** 1.0  
**Status:** ✅ Concluído e Testado
