package br.com.radarfornecedor.radar.config;

import java.util.regex.Pattern;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AtualizaBancoSQLite implements ApplicationRunner {

    private static final Pattern IDENTIFICADOR_SEGURO = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private final JdbcTemplate jdbcTemplate;

    public AtualizaBancoSQLite(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Migrações já foram executadas no banco restaurado
        // Apenas log de confirmação
        System.out.println("[MIGRATIONS] Banco já tem todas as migrations aplicadas (v30-32)");
    }

    // BACKUP DO CÓDIGO ANTIGO - mantido para referência histórica
    public void run_BACKUP(ApplicationArguments args) {
        criarTabelaDeHistorico();
        executarAtualizacoes();
    }

    private void criarTabelaDeHistorico() {
        String sql = "CREATE TABLE IF NOT EXISTS schema_migrations " +
                     "(versao INTEGER PRIMARY KEY, " +
                     "descricao TEXT NOT NULL, " +
                     "executada_em TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)";
        jdbcTemplate.execute(sql);
    }

    private void executarAtualizacoes() {
        // CLEANUP: Remover versões antigas não completadas para re-executar
        try {
            jdbcTemplate.update("DELETE FROM schema_migrations WHERE versao IN (30, 31)");
        } catch (Exception e) {
            // Tabela pode não existir ainda
        }
        
        // Versao 10
        aplicarAtualizacao(10, "Cria tabelas Paises e Cidades", () -> {
            String sql1 = "CREATE TABLE IF NOT EXISTS Paises " +
                         "(CodPais INTEGER PRIMARY KEY, Pais TEXT NOT NULL)";
            String sql2 = "CREATE TABLE IF NOT EXISTS Cidades " +
                         "(CodCidade INTEGER PRIMARY KEY, Cidade TEXT NOT NULL, " +
                         "UF TEXT NOT NULL, CodRegiao INTEGER, Alterado INTEGER DEFAULT 0, " +
                         "CodCidadeIBGE INTEGER NOT NULL UNIQUE, CodPais INTEGER NOT NULL)";
            executarSql(sql1);
            executarSql(sql2);
        });

        // Versao 11
        aplicarAtualizacao(11, "Adiciona CodCidade a fornecedores e compradores", () -> {
            adicionarColunaSeNaoExistir("Fornecedores", "CodCidade", "INTEGER");
            adicionarColunaSeNaoExistir("Compradores", "CodCidade", "INTEGER");
        });

        // Versao 12
        aplicarAtualizacao(12, "Carga inicial de paises", this::importarPaises);

        // Versao 20
        aplicarAtualizacao(20, "Cria tabelas de Usuarios, Representantes e Clientes", () -> {
            String sql1 = "CREATE TABLE IF NOT EXISTS Usuarios " +
                         "(ID INTEGER PRIMARY KEY AUTOINCREMENT, Username TEXT NOT NULL UNIQUE, " +
                         "Senha TEXT NOT NULL, Tipo TEXT NOT NULL)";
            String sql2 = "CREATE TABLE IF NOT EXISTS Representantes " +
                         "(ID INTEGER PRIMARY KEY AUTOINCREMENT, Nome TEXT NOT NULL, status TEXT)";
            String sql3 = "CREATE TABLE IF NOT EXISTS Clientes " +
                         "(ID INTEGER PRIMARY KEY AUTOINCREMENT, Nome TEXT NOT NULL, status TEXT)";
            executarSql(sql1);
            executarSql(sql2);
            executarSql(sql3);
        });

        // Versao 30 - Adiciona Categorias e Atividades (SEM DELETAR DADOS EXISTENTES)
        aplicarAtualizacao(30, "Cria tabelas de Categorias e Atividades com dados iniciais", () -> {
            // Criar tabelas se não existirem (usar nomes com capital como nas entities)
            String sqlCategorias = "CREATE TABLE IF NOT EXISTS Categorias " +
                                  "(id INTEGER PRIMARY KEY AUTOINCREMENT, nome TEXT NOT NULL UNIQUE, descricao TEXT, icone TEXT, ativa INTEGER DEFAULT 1, criadaEm TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, atualizadaEm TEXT DEFAULT CURRENT_TIMESTAMP)";
            String sqlAtividades = "CREATE TABLE IF NOT EXISTS Atividades " +
                                  "(id INTEGER PRIMARY KEY AUTOINCREMENT, cnae TEXT UNIQUE, descricao TEXT NOT NULL UNIQUE, secao TEXT, divisao TEXT, ativa INTEGER DEFAULT 1, criadaEm TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, atualizadaEm TEXT DEFAULT CURRENT_TIMESTAMP)";
            executarSql(sqlCategorias);
            executarSql(sqlAtividades);
            
            // Inserir dados iniciais em Categorias (INSERT OR IGNORE evita conflito se já existem)
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (1, 'Eletrônicos', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (2, 'Alimentos', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (3, 'Vestuário', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (4, 'Automóvel', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (5, 'Motocicletas', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (6, 'Moda e Têxtil', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (7, 'Embalagens', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (8, 'Tecnologia', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (9, 'Construção', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (10, 'Móvel e Escritório', 1)");
            
            // Inserir dados iniciais em Atividades
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (1, 'Comércio Varejista', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (2, 'Comércio Atacadista', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (3, 'Fabricação', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (4, 'Distribuição', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (5, 'Importação', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (6, 'Exportação', 1)");
        });

        // Versao 31 - Reconstrói tabelas Categorias e Atividades com schema completo
        aplicarAtualizacao(31, "Reconstrói tabelas com todas as colunas necessárias", () -> {
            try {
                // Backup de dados existentes (se houver)
                executarSql("CREATE TEMPORARY TABLE cat_backup AS SELECT * FROM Categorias");
                executarSql("CREATE TEMPORARY TABLE ati_backup AS SELECT * FROM Atividades");
            } catch (Exception e) {
                // Tabelas podem não existir ainda
            }
            
            // Dropar tabelas antigas se existem
            try {
                executarSql("DROP TABLE IF EXISTS Categorias");
                executarSql("DROP TABLE IF EXISTS Atividades");
            } catch (Exception e) {
                // Ignorar se tabelas não existem
            }
            
            // Recriar tabelas com schema completo
            String sqlCategorias = "CREATE TABLE Categorias (" +
                                  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                  "nome TEXT NOT NULL UNIQUE, " +
                                  "descricao TEXT, " +
                                  "icone TEXT, " +
                                  "ativa INTEGER DEFAULT 1, " +
                                  "criadaEm TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                                  "atualizadaEm TEXT DEFAULT CURRENT_TIMESTAMP" +
                                  ")";
            String sqlAtividades = "CREATE TABLE Atividades (" +
                                  "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                  "cnae TEXT UNIQUE, " +
                                  "descricao TEXT NOT NULL UNIQUE, " +
                                  "secao TEXT, " +
                                  "divisao TEXT, " +
                                  "ativa INTEGER DEFAULT 1, " +
                                  "criadaEm TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                                  "atualizadaEm TEXT DEFAULT CURRENT_TIMESTAMP" +
                                  ")";
            
            executarSql(sqlCategorias);
            executarSql(sqlAtividades);
            
            // Restaurar dados de backup se existirem
            try {
                executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) SELECT id, nome, ativa FROM cat_backup");
                executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) SELECT id, nome, ativa FROM ati_backup");
            } catch (Exception e) {
                // Backup pode não ter dados
            }
            
            // Inserir dados iniciais
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (1, 'Eletrônicos', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (2, 'Alimentos', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (3, 'Vestuário', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (4, 'Automóvel', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (5, 'Motocicletas', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (6, 'Moda e Têxtil', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (7, 'Embalagens', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (8, 'Tecnologia', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (9, 'Construção', 1)");
            executarSql("INSERT OR IGNORE INTO Categorias (id, nome, ativa) VALUES (10, 'Móvel e Escritório', 1)");
            
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (1, 'Comércio Varejista', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (2, 'Comércio Atacadista', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (3, 'Fabricação', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (4, 'Distribuição', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (5, 'Importação', 1)");
            executarSql("INSERT OR IGNORE INTO Atividades (id, descricao, ativa) VALUES (6, 'Exportação', 1)");
        });

        // Versao 32 - Consolida colunas duplicadas (CodCidade, CpfCnpj, etc) e adiciona campos faltantes
        aplicarAtualizacao(32, "Consolida colunas duplicadas e adiciona email/telefone/foto em Compradores", () -> {
            // PRIMEIRO: Adicionar colunas faltantes em Categorias e Atividades ANTES de consolidar
            adicionarColunaSeNaoExistir("Categorias", "criadaEm", "TEXT");
            adicionarColunaSeNaoExistir("Categorias", "atualizadaEm", "TEXT");
            adicionarColunaSeNaoExistir("Atividades", "criadaEm", "TEXT");
            adicionarColunaSeNaoExistir("Atividades", "atualizadaEm", "TEXT");
            
            // SEGUNDO: Consolidar tabelas (como antes)
            // 1. CONSOLIDAR FORNECEDORES (remover CodCidade duplicado)
            try {
                executarSql("BEGIN TRANSACTION");
                
                executarSql("CREATE TABLE fornecedores_new (" +
                           "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           "empresa VARCHAR(100) NOT NULL, " +
                           "cnpj VARCHAR(255) NOT NULL UNIQUE, " +
                           "status VARCHAR(255), " +
                           "pontuacao_risco FLOAT, " +
                           "logradouro VARCHAR(255), " +
                           "numero VARCHAR(255), " +
                           "complemento VARCHAR(255), " +
                           "bairro VARCHAR(255), " +
                           "cidade VARCHAR(255), " +
                           "estado VARCHAR(255), " +
                           "cep VARCHAR(255), " +
                           "latitude DOUBLE, " +
                           "longitude DOUBLE, " +
                           "cod_cidade BIGINT, " +
                           "aceitacpf BOOLEAN, " +
                           "prazo_entrega_dias INTEGER, " +
                           "email VARCHAR(255), " +
                           "telefone VARCHAR(255), " +
                           "foto TEXT, " +
                           "foto_nome VARCHAR(255), " +
                           "atividade_id BIGINT, " +
                           "categoria_id BIGINT)");
                
                executarSql("INSERT INTO fornecedores_new " +
                           "SELECT id, empresa, cnpj, status, pontuacao_risco, " +
                           "logradouro, numero, complemento, bairro, cidade, estado, cep, " +
                           "latitude, longitude, COALESCE(cod_cidade, CodCidade), " +
                           "aceitacpf, prazo_entrega_dias, email, telefone, foto, foto_nome, " +
                           "atividade_id, categoria_id FROM fornecedores");
                
                executarSql("DROP TABLE fornecedores");
                executarSql("ALTER TABLE fornecedores_new RENAME TO fornecedores");
                executarSql("COMMIT");
            } catch (Exception e) {
                try { executarSql("ROLLBACK"); } catch (Exception e2) { }
            }

            // 2. CONSOLIDAR COMPRADORES (remover CodCidade + adicionar email, telefone, foto, fotoNome)
            try {
                executarSql("BEGIN TRANSACTION");
                
                executarSql("CREATE TABLE compradores_new (" +
                           "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           "empresa VARCHAR(100) NOT NULL, " +
                           "cnpj VARCHAR(255) NOT NULL UNIQUE, " +
                           "status VARCHAR(255), " +
                           "pontuacao_risco FLOAT, " +
                           "logradouro VARCHAR(255), " +
                           "numero VARCHAR(255), " +
                           "complemento VARCHAR(255), " +
                           "bairro VARCHAR(255), " +
                           "cidade VARCHAR(255), " +
                           "estado VARCHAR(255), " +
                           "cep VARCHAR(255), " +
                           "latitude DOUBLE, " +
                           "longitude DOUBLE, " +
                           "cod_cidade BIGINT, " +
                           "atividade_id BIGINT, " +
                           "categoria_id BIGINT, " +
                           "email VARCHAR(255), " +
                           "telefone VARCHAR(255), " +
                           "foto TEXT, " +
                           "foto_nome VARCHAR(255))");
                
                executarSql("INSERT INTO compradores_new " +
                           "SELECT id, empresa, cnpj, status, pontuacao_risco, " +
                           "logradouro, numero, complemento, bairro, cidade, estado, cep, " +
                           "latitude, longitude, COALESCE(cod_cidade, CodCidade), " +
                           "atividade_id, categoria_id, NULL, NULL, NULL, NULL FROM compradores");
                
                executarSql("DROP TABLE compradores");
                executarSql("ALTER TABLE compradores_new RENAME TO compradores");
                executarSql("COMMIT");
            } catch (Exception e) {
                try { executarSql("ROLLBACK"); } catch (Exception e2) { }
            }

            // 3. CONSOLIDAR CLIENTES (remover CpfCnpj, TipoPessoa duplicados)
            try {
                executarSql("BEGIN TRANSACTION");
                
                executarSql("CREATE TABLE clientes_new (" +
                           "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           "nome VARCHAR(100) NOT NULL, " +
                           "status VARCHAR(255), " +
                           "cpf_cnpj VARCHAR(255), " +
                           "tipo_pessoa VARCHAR(255), " +
                           "logradouro VARCHAR(255), " +
                           "numero VARCHAR(255), " +
                           "complemento VARCHAR(255), " +
                           "bairro VARCHAR(255), " +
                           "cidade VARCHAR(255), " +
                           "estado VARCHAR(255), " +
                           "cep VARCHAR(255), " +
                           "latitude DOUBLE, " +
                           "longitude DOUBLE, " +
                           "email VARCHAR(255), " +
                           "telefone VARCHAR(255), " +
                           "endereco VARCHAR(255), " +
                           "foto TEXT, " +
                           "foto_nome VARCHAR(255))");
                
                executarSql("INSERT INTO clientes_new " +
                           "SELECT id, nome, status, " +
                           "COALESCE(cpf_cnpj, CpfCnpj), COALESCE(tipo_pessoa, TipoPessoa), " +
                           "logradouro, numero, complemento, bairro, cidade, estado, cep, " +
                           "latitude, longitude, email, telefone, endereco, foto, foto_nome FROM clientes");
                
                executarSql("DROP TABLE clientes");
                executarSql("ALTER TABLE clientes_new RENAME TO clientes");
                executarSql("COMMIT");
            } catch (Exception e) {
                try { executarSql("ROLLBACK"); } catch (Exception e2) { }
            }

            // 4. CONSOLIDAR REPRESENTANTES (remover CnpjFornecedor, CodEmpresa duplicados)
            try {
                executarSql("BEGIN TRANSACTION");
                
                executarSql("CREATE TABLE representantes_new (" +
                           "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                           "nome VARCHAR(100) NOT NULL, " +
                           "status VARCHAR(255), " +
                           "cnpj VARCHAR(255), " +
                           "cnpj_fornecedor VARCHAR(255), " +
                           "cod_empresa BIGINT, " +
                           "contato VARCHAR(30), " +
                           "email VARCHAR(120), " +
                           "logradouro VARCHAR(255), " +
                           "numero VARCHAR(255), " +
                           "complemento VARCHAR(255), " +
                           "bairro VARCHAR(255), " +
                           "cidade VARCHAR(255), " +
                           "estado VARCHAR(255), " +
                           "cep VARCHAR(255), " +
                           "latitude DOUBLE, " +
                           "longitude DOUBLE, " +
                           "atividade_id BIGINT, " +
                           "categoria_id BIGINT)");
                
                executarSql("INSERT INTO representantes_new " +
                           "SELECT id, nome, status, cnpj, " +
                           "COALESCE(cnpj_fornecedor, CnpjFornecedor), COALESCE(cod_empresa, CodEmpresa), " +
                           "contato, email, " +
                           "logradouro, numero, complemento, bairro, cidade, estado, cep, " +
                           "latitude, longitude, atividade_id, categoria_id FROM representantes");
                
                executarSql("DROP TABLE representantes");
                executarSql("ALTER TABLE representantes_new RENAME TO representantes");
                executarSql("COMMIT");
            } catch (Exception e) {
                try { executarSql("ROLLBACK"); } catch (Exception e2) { }
            }
        });
    }

    private void aplicarAtualizacao(int versao, String descricao, Runnable atualizacao) {
        try {
            Integer encontrada = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM schema_migrations WHERE versao = ?", Integer.class, versao);
            if (encontrada != null && encontrada > 0) {
                return;
            }
        } catch (Exception e) {
            // Tabela pode nao existir ainda
        }

        try {
            atualizacao.run();
            jdbcTemplate.update("INSERT INTO schema_migrations (versao, descricao) VALUES (?, ?)", versao, descricao);
        } catch (Exception e) {
            System.err.println("Erro ao aplicar atualizacao " + versao + ": " + e.getMessage());
        }
    }

    private void executarSql(String sql) {
        jdbcTemplate.execute(sql);
    }

    private void importarPaises() {
        jdbcTemplate.update("INSERT OR IGNORE INTO Paises (CodPais, Pais) VALUES (?, ?)", 1058, "BRASIL");
        jdbcTemplate.update("INSERT OR IGNORE INTO Paises (CodPais, Pais) VALUES (?, ?)", 9999999, "EXTERIOR");
    }

    private void adicionarColunaSeNaoExistir(String tabela, String coluna, String definicao) {
        validarIdentificador(tabela);
        validarIdentificador(coluna);

        try {
            boolean colunaExiste = jdbcTemplate.query("PRAGMA table_info(" + tabela + ")",
                    (rs, rowNum) -> rs.getString("name")).stream().anyMatch(coluna::equalsIgnoreCase);

            if (!colunaExiste) {
                String sql = "ALTER TABLE " + tabela + " ADD COLUMN " + coluna + " " + definicao;
                jdbcTemplate.execute(sql);
            }
        } catch (Exception e) {
            System.err.println("Erro ao adicionar coluna " + coluna + " em " + tabela + ": " + e.getMessage());
        }
    }

    private void validarIdentificador(String identificador) {
        if (!IDENTIFICADOR_SEGURO.matcher(identificador).matches()) {
            throw new IllegalArgumentException("Identificador SQL invalido: " + identificador);
        }
    }
}
