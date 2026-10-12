# Sistema de Alertas de Preço - COMPLETO ✅

**Data**: 06/10/2026  
**Status**: Backend Completo | Frontend Pendente

---

## 📋 RESUMO

Sistema completo de monitoramento de preços com alertas automáticos. Permite que usuários criem alertas para serem notificados quando o preço de um produto atingir um valor-alvo específico.

---

## ✅ O QUE FOI IMPLEMENTADO

### 1. Entities (2)
- **`AlertaPreco.java`** - Armazena alertas configurados pelos usuários
  - Campos: id, usuarioId, produtoId, precoAlvo, criterio, ativo, criadoEm, ultimaVerificacao, disparadoEm, precoDisparado, notas
  - Enum: CriterioAlerta (MENOR_OU_IGUAL, MENOR, IGUAL)
  - 4 indexes para performance
  
- **`HistoricoPreco.java`** - Registra todas as mudanças de preço
  - Campos: id, produtoId, preco, precoPromocional, registradoEm, origem, publicacaoId, variacaoPercentual
  - Enum: OrigemPreco (MANUAL, PUBLICACAO, JOB_VERIFICACAO, API_EXTERNA)
  - 3 indexes otimizados

### 2. Repositories (2)
- **`AlertaPrecoRepository.java`** - 13 métodos de consulta
  - Busca por usuário, produto, status ativo
  - Atualização de última verificação
  - Marcação de alerta disparado
  - Deleção por usuário + produto
  
- **`HistoricoPrecoRepository.java`** - 10 métodos
  - Busca por produto e período
  - Último e penúltimo registro (para comparar variação)
  - Reduções de preço
  - Verificação de registro duplicado no mesmo dia

### 3. Service
- **`AlertaPrecoService.java`** - Lógica completa de negócio
  - CRUD de alertas (criar, listar, buscar, atualizar, deletar)
  - Validações: limite de 50 alertas por usuário, preço > 0
  - Histórico de preços (registrar, buscar, buscar reduções)
  - **JOB SCHEDULED @Scheduled(cron = "0 0 */1 * * *")** - Executa a cada 1 hora
  - Verificação automática de todos os alertas ativos
  - Disparo de notificações quando preço-alvo é atingido
  - Cálculo de variação percentual de preços
  - DTOs enriquecidos com dados do produto

### 4. Controller
- **`AlertaPrecoController.java`** - 8 endpoints REST
  - POST `/api/alertas-preco` - Criar alerta
  - GET `/api/alertas-preco` - Listar alertas (filtro: ativos)
  - GET `/api/alertas-preco/{id}` - Buscar alerta específico
  - PUT `/api/alertas-preco/{id}` - Atualizar alerta
  - DELETE `/api/alertas-preco/{id}` - Deletar alerta
  - GET `/api/alertas-preco/produto/{produtoId}` - Verificar se tem alerta
  - GET `/api/alertas-preco/historico/{produtoId}` - Histórico de preços
  - GET `/api/alertas-preco/historico/{produtoId}/reducoes` - Reduções de preço

### 5. Notificações
- **`TipoNotificacao.java`** - Adicionados 3 novos tipos:
  - `ALERTA_PRECO_ATINGIDO` - Preço-alvo foi atingido
  - `REDUCAO_PRECO_DETECTADA` - Produto favoritado teve redução de preço
  - `NOVA_OFERTA_FORNECEDOR_FAVORITO` - Fornecedor favorito publicou nova oferta

### 6. Configuração
- **`RadarApplication.java`** - Adicionado `@EnableScheduling`

---

## 🗄️ BANCO DE DADOS

### Tabelas Criadas (2)

#### ALERTAS_PRECO
```sql
CREATE TABLE alertas_preco (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    preco_alvo DECIMAL(10,2) NOT NULL,
    criterio VARCHAR(20) NOT NULL DEFAULT 'MENOR_OU_IGUAL',
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL,
    ultima_verificacao TIMESTAMP,
    disparado_em TIMESTAMP,
    preco_disparado DECIMAL(10,2),
    notas VARCHAR(1000),
    INDEX idx_alerta_usuario (usuario_id),
    INDEX idx_alerta_produto (produto_id),
    INDEX idx_alerta_ativo (ativo),
    INDEX idx_alerta_usuario_produto (usuario_id, produto_id)
);
```

#### HISTORICO_PRECOS
```sql
CREATE TABLE historico_precos (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    produto_id BIGINT NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    preco_promocional DECIMAL(10,2),
    registrado_em TIMESTAMP NOT NULL,
    origem VARCHAR(30),
    publicacao_id BIGINT,
    variacao_percentual DECIMAL(5,2),
    INDEX idx_historico_produto (produto_id),
    INDEX idx_historico_data (registrado_em),
    INDEX idx_historico_produto_data (produto_id, registrado_em)
);
```

---

## 🔌 ENDPOINTS REST

### 1. Criar Alerta
**POST** `/api/alertas-preco`

**Body:**
```json
{
  "produtoId": 123,
  "precoAlvo": 99.90,
  "criterio": "MENOR_OU_IGUAL",  // Opcional
  "notas": "Quero comprar quando estiver abaixo de R$ 100"  // Opcional
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "usuarioId": 5,
  "produtoId": 123,
  "precoAlvo": 99.90,
  "criterio": "MENOR_OU_IGUAL",
  "ativo": true,
  "criadoEm": "2026-10-06T14:30:00",
  "ultimaVerificacao": null,
  "disparadoEm": null,
  "precoDisparado": null,
  "notas": "Quero comprar quando estiver abaixo de R$ 100"
}
```

### 2. Listar Alertas
**GET** `/api/alertas-preco?ativos=true`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "produtoId": 123,
    "precoAlvo": 99.90,
    "criterio": "MENOR_OU_IGUAL",
    "ativo": true,
    "criadoEm": "2026-10-06T14:30:00",
    "produto": {
      "id": 123,
      "nome": "Notebook Dell",
      "preco": 120.00,
      "categoria": "TECNOLOGIA"
    },
    "diferencaPreco": -20.10,  // Faltam R$ 20,10 para atingir
    "precoAbaixoAlvo": false
  }
]
```

### 3. Buscar Alerta
**GET** `/api/alertas-preco/{id}`

**Response:** `200 OK` (retorna AlertaPreco)

### 4. Atualizar Alerta
**PUT** `/api/alertas-preco/{id}`

**Body (todos os campos opcionais):**
```json
{
  "precoAlvo": 89.90,
  "criterio": "MENOR",
  "ativo": true,
  "notas": "Atualizado"
}
```

**Response:** `200 OK` (retorna AlertaPreco atualizado)

### 5. Deletar Alerta
**DELETE** `/api/alertas-preco/{id}`

**Response:** `200 OK` - `"Alerta deletado com sucesso"`

### 6. Verificar Alerta por Produto
**GET** `/api/alertas-preco/produto/{produtoId}`

**Response:** `200 OK`
```json
{
  "temAlerta": true
}
```

### 7. Histórico de Preços
**GET** `/api/alertas-preco/historico/{produtoId}?limite=10`

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "produtoId": 123,
    "preco": 120.00,
    "precoPromocional": null,
    "registradoEm": "2026-10-06T14:00:00",
    "origem": "JOB_VERIFICACAO",
    "variacaoPercentual": -5.5  // Reduziu 5,5%
  }
]
```

### 8. Reduções de Preço
**GET** `/api/alertas-preco/historico/{produtoId}/reducoes`

**Response:** `200 OK` (array de HistoricoPreco com variação negativa)

---

## ⚙️ JOB SCHEDULED

### Configuração
```java
@Scheduled(cron = "0 0 */1 * * *")  // A cada 1 hora
public void verificarAlertasDePreco()
```

### Funcionamento
1. **Busca todos os alertas ativos** do banco
2. Para cada alerta:
   - Busca o produto correspondente
   - Verifica se preço atual atende ao critério
   - Se sim:
     - Marca alerta como disparado (ativo = false)
     - Registra preço e data do disparo
     - Envia notificação ao usuário
3. **Atualiza `ultimaVerificacao`** de todos os alertas
4. **Loga estatísticas** (quantos alertas verificados, quantos disparados)

### Critérios de Comparação
- **MENOR_OU_IGUAL**: Preço atual <= preço-alvo
- **MENOR**: Preço atual < preço-alvo
- **IGUAL**: |Preço atual - preço-alvo| <= 0.01

### Exemplo de Log
```
2026-10-06 15:00:00 INFO  - Iniciando verificação de alertas de preço...
2026-10-06 15:00:02 INFO  - Encontrados 15 alertas ativos para verificação
2026-10-06 15:00:05 INFO  - Alerta 3 disparado para usuário 5. Produto: Notebook Dell, Preço: 95.00
2026-10-06 15:00:06 INFO  - Verificação concluída. 1 alertas disparados
```

---

## 🔒 SEGURANÇA

### Validações
- ✅ Todos os endpoints validam sessão (HttpSession)
- ✅ Apenas o proprietário pode acessar/modificar seus alertas
- ✅ Limite de 50 alertas ativos por usuário
- ✅ Preço-alvo deve ser > 0
- ✅ Produto deve existir no banco

### Isolamento de Dados
- Cada usuário só vê seus próprios alertas
- Consultas sempre incluem `usuarioId` no WHERE
- Job scheduled não expõe dados de usuários

---

## 📊 ESTATÍSTICAS

| Métrica | Quantidade |
|---------|-----------|
| **Arquivos Criados** | 6 |
| **Arquivos Modificados** | 2 |
| **Entities** | 2 |
| **Repositories** | 2 |
| **Services** | 1 |
| **Controllers** | 1 |
| **Endpoints REST** | 8 |
| **Tabelas de Banco** | 2 |
| **Indexes** | 7 |
| **Jobs Scheduled** | 1 |
| **Linhas de Código** | ~1.200 |

---

## 🚀 COMO TESTAR

### 1. Reiniciar Spring Boot
As tabelas serão criadas automaticamente. O job scheduled iniciará automaticamente.

### 2. Criar Alerta via API
```javascript
fetch('/api/alertas-preco', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    produtoId: 1,
    precoAlvo: 50.00,
    criterio: 'MENOR_OU_IGUAL',
    notas: 'Teste de alerta'
  })
}).then(r => r.json()).then(console.log);
```

### 3. Listar Alertas
```javascript
fetch('/api/alertas-preco?ativos=true')
  .then(r => r.json())
  .then(console.log);
```

### 4. Simular Disparo
- Alterar preço de um produto no banco para valor abaixo do alerta
- Aguardar próxima execução do job (a cada 1 hora)
- OU reiniciar Spring Boot e chamar o método manualmente

### 5. Verificar Notificação
```
GET /api/notificacoes
```
Deve aparecer notificação do tipo `ALERTA_PRECO_ATINGIDO`

---

## ⏭️ PRÓXIMOS PASSOS

### Frontend (Pendente)
- [ ] Página `meus-alertas.html`:
  - Lista de alertas ativos e disparados
  - Formulário de criação de alerta
  - Edição inline de preço-alvo
  - Gráfico de histórico de preços
  - Badge de status (ativo, disparado, inativo)
  
- [ ] Integração em Produtos:
  - Botão "Criar Alerta" nos cards de produtos
  - Modal rápido de criação
  - Indicador visual se produto já tem alerta
  
- [ ] Dashboard de Alertas:
  - Resumo: alertas ativos, disparados hoje, economia potencial
  - Gráfico de tendência de preços
  - Top produtos com maior redução

### Melhorias (Opcional)
- [ ] Alerta de "% de redução" ao invés de preço fixo
- [ ] Notificação por email (além da in-app)
- [ ] Histórico de alertas disparados
- [ ] Alerta recorrente (reativa após disparo)
- [ ] Alerta de aumento de preço
- [ ] Comparação de preços entre fornecedores

---

## 🐛 TROUBLESHOOTING

### Job não está executando
1. Verificar se `@EnableScheduling` está na classe principal
2. Verificar logs: `grep "Iniciando verificação de alertas" spring_boot.log`
3. Aguardar 1 hora após iniciar o servidor

### Alerta não dispara
1. Verificar se preço do produto está configurado
2. Verificar se critério está correto
3. Verificar se alerta está ativo
4. Checar logs de erro no job

### Notificação não aparece
1. Verificar se NotificacaoService está funcionando
2. Consultar tabela `notificacoes` diretamente
3. Verificar se usuário está autenticado

---

## 📝 NOTAS TÉCNICAS

### Performance
- Indexes otimizados para queries frequentes
- Batch loading de produtos (evita N+1)
- Job executa em transação separada
- Limite de 50 alertas por usuário

### Escalabilidade
- Job scheduled pode ser substituído por fila (RabbitMQ, Kafka)
- Histórico de preços pode ser particionado por data
- Cache de produtos pode ser implementado (Redis)

### Manutenção
- Logs detalhados para debug
- Try-catch em cada alerta (um erro não para o job)
- Soft delete (ativo=false) preserva histórico

---

## ✍️ AUTORIA

**Implementado por**: Kiro AI  
**Data**: 06/10/2026  
**Projeto**: Radar Fornecedor - Sistema de Inteligência Comercial  
**Sprint**: Sistema de Alertas de Preço  
**Tecnologias**: Spring Boot, Spring Scheduling, H2 Database, JPA/Hibernate
