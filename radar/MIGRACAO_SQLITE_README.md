# Migração do Banco de Dados H2 → SQLite

## 📋 O que foi feito

### 1. Configuração do Banco de Dados
- **Antes:** Sistema usava H2 (`DBLRadar.mv.db`)
- **Agora:** Sistema usa SQLite (`DBLRadar.db`) - onde estão todos os dados reais

### 2. Arquivos Alterados

#### `application.properties`
```properties
# Antes (H2):
spring.datasource.url=jdbc:h2:./DBLRadar
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Agora (SQLite):
spring.datasource.url=jdbc:sqlite:DBLRadar.db
spring.datasource.driverClassName=org.sqlite.JDBC
spring.jpa.database-platform=org.hibernate.community.dialect.SQLiteDialect
```

#### `pom.xml`
Adicionadas dependências:
- `sqlite-jdbc` (driver JDBC para SQLite)
- `hibernate-community-dialects` (dialeto Hibernate para SQLite)

### 3. Sistema de Subcategorias Criado

#### Novos Arquivos:
1. **`Subcategoria.java`** (model) - Entidade JPA
2. **`SubcategoriaRepository.java`** (repository) - Acesso ao banco
3. **`SubcategoriaController.java`** (controller) - API REST

#### Endpoints Disponíveis:
- `GET /api/subcategorias` - Lista todas
- `GET /api/subcategorias/ativas` - Lista apenas ativas
- `GET /api/subcategorias/categoria/{categoria}` - Filtra por categoria
- `GET /api/subcategorias/estatisticas` - Estatísticas gerais
- `GET /api/subcategorias/{id}` - Busca por ID
- `POST /api/subcategorias` - Cria nova
- `PUT /api/subcategorias/{id}` - Atualiza existente
- `DELETE /api/subcategorias/{id}` - Desativa (soft delete)
- `DELETE /api/subcategorias/{id}/permanente` - Remove permanentemente

### 4. Estrutura da Tabela Subcategoria

```sql
CREATE TABLE subcategoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    categoria VARCHAR(100) NOT NULL,
    ativa BOOLEAN DEFAULT 1,
    criada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚀 Como Usar

### Opção 1: Executar o BAT
```
REINICIAR_COM_SQLITE.bat
```

### Opção 2: Manual
```powershell
# 1. Encerrar Java
taskkill /F /IM java.exe

# 2. Compilar com novas dependências
cd c:\Projeto\Projeto_Radar_Fornecedor\radar
.\mvnw.cmd clean install -DskipTests

# 3. Iniciar servidor
.\mvnw.cmd spring-boot:run
```

## 📊 Inserir Subcategorias

Execute o script Python ou PowerShell criado anteriormente:

```powershell
# PowerShell
powershell -ExecutionPolicy Bypass -File .\inserir_subcategorias.ps1

# OU Python
python inserir_sub.py
```

Isso vai inserir ~220 subcategorias nas 22 categorias.

## ✅ Verificar

Após iniciar o servidor, teste:

```
http://localhost:8080/api/subcategorias/estatisticas
```

Deve retornar:
```json
{
  "total": 220,
  "ativas": 220
}
```

## 📁 Arquivos de Banco

- **`DBLRadar.db`** (SQLite) → Banco PRINCIPAL (será usado agora)
- **`DBLRadar.mv.db`** (H2) → Banco antigo (pode ser removido após migração)

## ⚠️ Importante

1. **O Hibernate criará a tabela automaticamente** com `ddl-auto=update`
2. **Todos os dados existentes no SQLite serão preservados**
3. **As subcategorias serão salvas no mesmo banco dos outros dados**
4. **Faça backup antes de testar:** `copy DBLRadar.db DBLRadar.db.backup_antes_subcategorias`

## 🔧 Troubleshooting

### Erro de compilação
```
mvnw.cmd clean install -DskipTests
```

### Banco não está sendo atualizado
Verifique se o `ddl-auto` está como `update`:
```properties
spring.jpa.hibernate.ddl-auto=update
```

### Subcategorias não aparecem
1. Verifique se o servidor iniciou sem erros
2. Teste o endpoint: `http://localhost:8080/api/subcategorias`
3. Execute o script de inserção novamente
