-- ========================================
-- MIGRATION: Central de Notificações
-- Data: 2026-10-06
-- Descrição: Cria tabela NOTIFICACOES para armazenar
--            notificações dos usuários (publicações,
--            avaliações, seguidores, comentários)
-- ========================================

-- IMPORTANTE: Este script é incremental e não apaga dados existentes
-- Tabela: NOTIFICACOES

-- ========================================
-- 1. TABELA NOTIFICACOES
-- ========================================
-- Armazena notificações de eventos relevantes para cada usuário
-- Cada usuário visualiza apenas suas próprias notificações (autorização backend)
-- Deduplicação: evita notificações duplicadas do mesmo evento (EVENTO_TIPO + EVENTO_ID)

CREATE TABLE IF NOT EXISTS NOTIFICACOES (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Usuário que receberá a notificação
    USUARIO_ID BIGINT NOT NULL,
    
    -- Tipo de notificação
    TIPO VARCHAR(50) NOT NULL,  -- 'NOVA_PUBLICACAO', 'NOVA_AVALIACAO', 'NOVO_SEGUIDOR', 'NOVO_COMENTARIO', 'SISTEMA'
    
    -- Conteúdo da notificação
    TITULO VARCHAR(200) NOT NULL,
    MENSAGEM VARCHAR(500),
    LINK_DESTINO VARCHAR(500),
    
    -- Status de leitura
    LIDA BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Data de criação
    CRIADA_EM TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Campos para deduplicação (evita notificações duplicadas)
    EVENTO_ID BIGINT,              -- ID do evento que originou a notificação
    EVENTO_TIPO VARCHAR(50),       -- Tipo do evento ('PUBLICACAO', 'AVALIACAO', 'SEGUIDOR', 'COMENTARIO')
    
    -- Nome do usuário/fornecedor que originou o evento (para exibição)
    ORIGEM_NOME VARCHAR(200),
    
    -- Foreign key (opcional, descomente se quiser constraint)
    -- FOREIGN KEY (USUARIO_ID) REFERENCES USUARIOS(ID) ON DELETE CASCADE
    
    -- Constraint para tipo de notificação
    CONSTRAINT CHK_TIPO_NOTIFICACAO CHECK (
        TIPO IN ('NOVA_PUBLICACAO', 'NOVA_AVALIACAO', 'NOVO_SEGUIDOR', 'NOVO_COMENTARIO', 'SISTEMA')
    )
);

-- ========================================
-- 2. ÍNDICES PARA PERFORMANCE
-- ========================================

-- Índice principal: listagem de notificações por usuário (ordenado por data)
CREATE INDEX IF NOT EXISTS idx_notif_usuario ON NOTIFICACOES(USUARIO_ID);

-- Índice para filtrar notificações não lidas
CREATE INDEX IF NOT EXISTS idx_notif_lida ON NOTIFICACOES(LIDA);

-- Índice para ordenação por data (mais recentes primeiro)
CREATE INDEX IF NOT EXISTS idx_notif_criada ON NOTIFICACOES(CRIADA_EM);

-- Índice composto para deduplicação
-- Verifica se já existe notificação para o mesmo usuário + evento
CREATE INDEX IF NOT EXISTS idx_notif_dedup ON NOTIFICACOES(USUARIO_ID, EVENTO_TIPO, EVENTO_ID);

-- Índice composto para query principal (listagem ordenada por usuário)
CREATE INDEX IF NOT EXISTS idx_notif_usuario_criada ON NOTIFICACOES(USUARIO_ID, CRIADA_EM DESC);

-- Índice composto para contagem de não lidas (badge)
CREATE INDEX IF NOT EXISTS idx_notif_usuario_lida ON NOTIFICACOES(USUARIO_ID, LIDA);

-- ========================================
-- 3. VERIFICAÇÃO DA TABELA CRIADA
-- ========================================
-- Para verificar se a tabela foi criada corretamente:
-- SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'NOTIFICACOES';

-- Para verificar índices criados:
-- SELECT INDEX_NAME FROM INFORMATION_SCHEMA.INDEXES WHERE TABLE_NAME = 'NOTIFICACOES';

-- ========================================
-- 4. EXEMPLOS DE QUERIES (TESTES)
-- ========================================

-- Listar todas as notificações de um usuário (ordenadas por data)
-- SELECT * FROM NOTIFICACOES WHERE USUARIO_ID = 1 ORDER BY CRIADA_EM DESC;

-- Contar notificações não lidas de um usuário (para badge)
-- SELECT COUNT(*) FROM NOTIFICACOES WHERE USUARIO_ID = 1 AND LIDA = FALSE;

-- Buscar últimas 5 notificações de um usuário (para dropdown)
-- SELECT * FROM NOTIFICACOES WHERE USUARIO_ID = 1 ORDER BY CRIADA_EM DESC LIMIT 5;

-- Marcar uma notificação como lida
-- UPDATE NOTIFICACOES SET LIDA = TRUE WHERE ID = 1 AND USUARIO_ID = 1;

-- Marcar todas as notificações de um usuário como lidas
-- UPDATE NOTIFICACOES SET LIDA = TRUE WHERE USUARIO_ID = 1 AND LIDA = FALSE;

-- Verificar se já existe notificação para um evento (deduplicação)
-- SELECT COUNT(*) FROM NOTIFICACOES 
-- WHERE USUARIO_ID = 1 AND EVENTO_TIPO = 'PUBLICACAO' AND EVENTO_ID = 123;

-- ========================================
-- NOTAS IMPORTANTES:
-- ========================================
-- 1. Hibernate aplicará automaticamente com spring.jpa.hibernate.ddl-auto=update
-- 2. Não há DELETE ou DROP - dados existentes preservados
-- 3. Deduplicação implementada no Service usando EVENTO_TIPO + EVENTO_ID
-- 4. Cada usuário só pode acessar suas próprias notificações (validação no Controller)
-- 5. Notificações são criadas automaticamente quando eventos ocorrem:
--    - Nova publicação de fornecedor seguido → NOVA_PUBLICACAO
--    - Nova avaliação recebida → NOVA_AVALIACAO
--    - Novo seguidor → NOVO_SEGUIDOR
--    - Novo comentário em publicação → NOVO_COMENTARIO
--    - Notificações do sistema → SISTEMA
-- 6. LINK_DESTINO contém URL para onde a notificação redireciona ao clicar
-- 7. ORIGEM_NOME armazena nome do usuário que gerou o evento (ex: "João Silva")
-- 8. Índices otimizados para:
--    - Listagem de notificações por usuário (query principal)
--    - Contagem de não lidas (badge no sino)
--    - Filtro de não lidas (dropdown inicial)
--    - Deduplicação (evita notificações duplicadas)
-- 9. BOOLEAN é compatível com H2 (banco atual do projeto)
-- 10. VARCHAR(500) para LINK_DESTINO suporta URLs longas com query params
-- ========================================

