-- ========================================
-- MIGRATION: Perfil Comercial do Fornecedor
-- Data: 11/10/2026
-- Descrição: Adiciona campos de verificação e sistema de avaliações de empresas
-- ========================================

-- ========================================
-- 1. ADICIONAR CAMPOS DE VERIFICAÇÃO EM FORNECEDORES
-- ========================================

-- Campo para indicar se a empresa foi verificada/validada por um administrador
ALTER TABLE Fornecedores ADD COLUMN IF NOT EXISTS verificado BOOLEAN DEFAULT FALSE;

-- Data em que a empresa foi verificada
ALTER TABLE Fornecedores ADD COLUMN IF NOT EXISTS verificado_em TIMESTAMP NULL;

-- ID do administrador que verificou a empresa
ALTER TABLE Fornecedores ADD COLUMN IF NOT EXISTS verificado_por BIGINT NULL;

-- Foreign key para rastrear qual admin verificou
-- ALTER TABLE Fornecedores ADD CONSTRAINT fk_verificado_por 
--     FOREIGN KEY (verificado_por) REFERENCES Usuarios(ID);

-- ========================================
-- 2. CRIAR TABELA DE AVALIAÇÕES DE FORNECEDOR
-- ========================================

-- Diferente de AvaliacoesProdutos (que avalia produtos específicos),
-- esta tabela avalia a EMPRESA como um todo (atendimento, qualidade geral, confiabilidade)

CREATE TABLE IF NOT EXISTS AVALIACOES_FORNECEDOR (
    -- Identificador único da avaliação
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    
    -- Fornecedor sendo avaliado
    fornecedor_id BIGINT NOT NULL,
    
    -- Usuário que está avaliando (NULL se permitir avaliações anônimas no futuro)
    avaliador_usuario_id BIGINT NOT NULL,
    
    -- Nota de 1 a 5 estrelas
    nota INT NOT NULL CHECK (nota >= 1 AND nota <= 5),
    
    -- Comentário/review (opcional)
    comentario TEXT,
    
    -- Resposta pública do fornecedor à avaliação
    resposta_fornecedor TEXT,
    
    -- Data em que o fornecedor respondeu
    data_resposta TIMESTAMP NULL,
    
    -- Data de criação da avaliação
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    
    -- Chaves estrangeiras
    CONSTRAINT fk_avaliacao_fornecedor FOREIGN KEY (fornecedor_id) REFERENCES Fornecedores(ID) ON DELETE CASCADE,
    CONSTRAINT fk_avaliacao_usuario FOREIGN KEY (avaliador_usuario_id) REFERENCES Usuarios(ID) ON DELETE CASCADE,
    
    -- Impedir que um usuário avalie o mesmo fornecedor múltiplas vezes
    CONSTRAINT unique_avaliacao_por_usuario UNIQUE (fornecedor_id, avaliador_usuario_id)
);

-- ========================================
-- 3. ÍNDICES PARA PERFORMANCE
-- ========================================

-- Índice para buscar todas as avaliações de um fornecedor rapidamente
CREATE INDEX IF NOT EXISTS idx_avaliacoes_fornecedor_id ON AVALIACOES_FORNECEDOR(fornecedor_id);

-- Índice para buscar avaliações por data (útil para ordenação)
CREATE INDEX IF NOT EXISTS idx_avaliacoes_criado_em ON AVALIACOES_FORNECEDOR(criado_em DESC);

-- Índice para verificar se usuário já avaliou
CREATE INDEX IF NOT EXISTS idx_avaliacoes_usuario ON AVALIACOES_FORNECEDOR(avaliador_usuario_id);

-- ========================================
-- 4. COMENTÁRIOS EXPLICATIVOS
-- ========================================

-- COMMENT ON TABLE AVALIACOES_FORNECEDOR IS 'Avaliações globais de fornecedores (empresas), diferente de avaliações de produtos específicos';
-- COMMENT ON COLUMN AVALIACOES_FORNECEDOR.nota IS 'Nota de 1 a 5 estrelas para a empresa como um todo';
-- COMMENT ON COLUMN AVALIACOES_FORNECEDOR.resposta_fornecedor IS 'Resposta pública do fornecedor, visível para todos';
-- COMMENT ON COLUMN Fornecedores.verificado IS 'Indica se a empresa passou por processo de verificação/validação administrativa';

-- ========================================
-- 5. DADOS INICIAIS (OPCIONAL)
-- ========================================

-- Nenhum fornecedor é verificado por padrão
UPDATE Fornecedores SET verificado = FALSE WHERE verificado IS NULL;

-- ========================================
-- FIM DA MIGRATION
-- ========================================

-- COMO APLICAR:
-- 1. Parar o servidor Spring Boot
-- 2. Executar este arquivo no banco de dados:
--    sqlite3 DBLRadar.mv.db < migration_perfil_comercial.sql
-- 3. OU deixar o Hibernate aplicar automaticamente (ddl-auto=update)
-- 4. Reiniciar o servidor

-- ROLLBACK (se necessário):
-- DROP TABLE IF EXISTS AVALIACOES_FORNECEDOR;
-- ALTER TABLE Fornecedores DROP COLUMN IF EXISTS verificado;
-- ALTER TABLE Fornecedores DROP COLUMN IF EXISTS verificado_em;
-- ALTER TABLE Fornecedores DROP COLUMN IF EXISTS verificado_por;
