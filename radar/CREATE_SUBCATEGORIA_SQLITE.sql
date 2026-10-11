-- Criação da tabela subcategoria no SQLite
-- Execute este script no DBLRadar.db

CREATE TABLE IF NOT EXISTS subcategoria (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    categoria VARCHAR(100) NOT NULL,
    ativa BOOLEAN DEFAULT 1,
    criada_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice para busca por categoria
CREATE INDEX IF NOT EXISTS idx_subcategoria_categoria ON subcategoria(categoria);

-- Índice para busca por nome
CREATE INDEX IF NOT EXISTS idx_subcategoria_nome ON subcategoria(nome);
