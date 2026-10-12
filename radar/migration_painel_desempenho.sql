-- ========================================
-- MIGRATION: Painel de Desempenho Fornecedor
-- Data: 2026-10-06
-- Descrição: Cria tabelas para rastreamento de eventos
--            (visualizações, cliques em contato) para o
--            painel de desempenho exclusivo de fornecedores
-- ========================================

-- IMPORTANTE: Este script é incremental e não apaga dados existentes
-- Tabelas: EVENTOS_PUBLICACAO, EVENTOS_PRODUTO

-- ========================================
-- 1. TABELA EVENTOS_PUBLICACAO
-- ========================================
-- Rastreia visualizações e cliques em "entrar em contato" de publicações
-- Deduplicação: máximo 1 evento VIEW por usuário/IP a cada 24h
-- Cliques em CONTATO são sempre registrados (cada clique é intencional)

CREATE TABLE IF NOT EXISTS EVENTOS_PUBLICACAO (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    PUBLICACAO_ID BIGINT NOT NULL,
    FORNECEDOR_ID BIGINT NOT NULL,
    TIPO_EVENTO VARCHAR(20) NOT NULL,  -- 'VIEW' ou 'CONTATO'
    USUARIO_ID BIGINT,                 -- NULL para usuários não logados
    IP_USUARIO VARCHAR(45),            -- Para deduplicação de anônimos
    CRIADO_EM TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign keys (caso queira adicionar constraints)
    -- FOREIGN KEY (PUBLICACAO_ID) REFERENCES PUBLICACOES(ID) ON DELETE CASCADE,
    -- FOREIGN KEY (FORNECEDOR_ID) REFERENCES FORNECEDORES(ID) ON DELETE CASCADE,
    -- FOREIGN KEY (USUARIO_ID) REFERENCES USUARIOS(ID) ON DELETE SET NULL
    
    CONSTRAINT CHK_TIPO_EVENTO_PUB CHECK (TIPO_EVENTO IN ('VIEW', 'CONTATO'))
);

-- Índices para performance (queries por fornecedor, por publicação, por data)
CREATE INDEX IF NOT EXISTS idx_evento_publicacao_id ON EVENTOS_PUBLICACAO(PUBLICACAO_ID);
CREATE INDEX IF NOT EXISTS idx_evento_fornecedor_id ON EVENTOS_PUBLICACAO(FORNECEDOR_ID);
CREATE INDEX IF NOT EXISTS idx_evento_criado_em ON EVENTOS_PUBLICACAO(CRIADO_EM);
CREATE INDEX IF NOT EXISTS idx_evento_tipo ON EVENTOS_PUBLICACAO(TIPO_EVENTO);

-- Índice composto para deduplicação
CREATE INDEX IF NOT EXISTS idx_evento_dedup ON EVENTOS_PUBLICACAO(PUBLICACAO_ID, TIPO_EVENTO, USUARIO_ID, CRIADO_EM);

-- ========================================
-- 2. TABELA EVENTOS_PRODUTO
-- ========================================
-- Rastreia visualizações de produtos
-- Deduplicação: máximo 1 visualização por usuário/IP a cada 24h

CREATE TABLE IF NOT EXISTS EVENTOS_PRODUTO (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    PRODUTO_ID BIGINT NOT NULL,
    FORNECEDOR_ID BIGINT NOT NULL,
    USUARIO_ID BIGINT,                 -- NULL para usuários não logados
    IP_USUARIO VARCHAR(45),            -- Para deduplicação de anônimos
    CRIADO_EM TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    
    -- Foreign keys (caso queira adicionar constraints)
    -- FOREIGN KEY (PRODUTO_ID) REFERENCES PRODUTOS(ID) ON DELETE CASCADE,
    -- FOREIGN KEY (FORNECEDOR_ID) REFERENCES FORNECEDORES(ID) ON DELETE CASCADE,
    -- FOREIGN KEY (USUARIO_ID) REFERENCES USUARIOS(ID) ON DELETE SET NULL
);

-- Índices para performance
CREATE INDEX IF NOT EXISTS idx_evento_produto_id ON EVENTOS_PRODUTO(PRODUTO_ID);
CREATE INDEX IF NOT EXISTS idx_evento_prod_fornecedor_id ON EVENTOS_PRODUTO(FORNECEDOR_ID);
CREATE INDEX IF NOT EXISTS idx_evento_prod_criado_em ON EVENTOS_PRODUTO(CRIADO_EM);

-- Índice composto para deduplicação
CREATE INDEX IF NOT EXISTS idx_evento_prod_dedup ON EVENTOS_PRODUTO(PRODUTO_ID, USUARIO_ID, CRIADO_EM);

-- ========================================
-- 3. VERIFICAÇÃO DAS TABELAS CRIADAS
-- ========================================
-- Para verificar se as tabelas foram criadas corretamente:
-- SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('EVENTOS_PUBLICACAO', 'EVENTOS_PRODUTO');

-- ========================================
-- NOTAS IMPORTANTES:
-- ========================================
-- 1. Hibernate aplicará automaticamente com spring.jpa.hibernate.ddl-auto=update
-- 2. Não há DELETE ou DROP - dados existentes preservados
-- 3. Deduplicação implementada no Service (não na tabela) para flexibilidade
-- 4. IP_USUARIO suporta IPv6 (até 45 caracteres)
-- 5. TIPO_EVENTO tem constraint CHECK para garantir valores válidos
-- 6. Índices otimizados para queries do painel de desempenho:
--    - Por fornecedor (listar todos eventos)
--    - Por publicação/produto (contar visualizações específicas)
--    - Por data (evolução temporal, filtros de período)
--    - Deduplicação (verificar evento recente)
-- ========================================
