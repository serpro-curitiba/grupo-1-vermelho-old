-- ============================================================================
-- V1 — Inicialização dos schemas dos bounded contexts (ADR-0002 Modular Monolith)
-- Cobre: REQ-PAY-001..030, REQ-BEN-001, REQ-AUD-001
-- Origem legado: BATCHPGT.NSN, CADBENEF.NSN, ATUFCAD.NSN + DDMs PAGAMENTO-V, BENEFICIARIO-V
-- ============================================================================

-- Os schemas são criados pelo Flyway (create-schemas: true). Aqui declaramos
-- as tabelas iniciais do MVP do Estágio 3.

-- ---------------------------------------------------------------------------
-- SCHEMA: beneficiarios
-- ---------------------------------------------------------------------------
CREATE TABLE beneficiarios.beneficiario (
    id                UUID         PRIMARY KEY,
    cpf               VARCHAR(11)  NOT NULL UNIQUE,
    nome              VARCHAR(100) NOT NULL,
    regiao            INT          NOT NULL CHECK (regiao BETWEEN 1 AND 27),
    renda_mensal      NUMERIC(15,2) NOT NULL DEFAULT 0,
    idade             INT          NOT NULL CHECK (idade >= 0),
    status            VARCHAR(20)  NOT NULL CHECK (status IN ('ATIVO','SUSPENSO','INATIVO')),
    programa_codigo   VARCHAR(36)  NOT NULL,
    criado_em         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    atualizado_em     TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_beneficiario_status ON beneficiarios.beneficiario(status);

CREATE TABLE beneficiarios.dependente (
    id              UUID         PRIMARY KEY,
    beneficiario_id UUID         NOT NULL REFERENCES beneficiarios.beneficiario(id) ON DELETE CASCADE,
    nome            VARCHAR(100) NOT NULL,
    idade           INT          NOT NULL CHECK (idade >= 0)
);
CREATE INDEX ix_dependente_benef ON beneficiarios.dependente(beneficiario_id);

-- ---------------------------------------------------------------------------
-- SCHEMA: programas
-- ---------------------------------------------------------------------------
CREATE TABLE programas.programa (
    codigo     VARCHAR(36) PRIMARY KEY,
    nome       VARCHAR(100) NOT NULL,
    status     VARCHAR(20)  NOT NULL CHECK (status IN ('ATIVO','INATIVO')),
    valor_base NUMERIC(15,2) NOT NULL
);

CREATE TABLE programas.fator_regional (
    regiao INT PRIMARY KEY CHECK (regiao BETWEEN 1 AND 27),
    fator  NUMERIC(8,4) NOT NULL DEFAULT 1.0000
);

-- Seed mínimo de 25 regiões com fator neutro (REQ-PAY-021).
INSERT INTO programas.fator_regional(regiao, fator)
SELECT generate_series(1, 25), 1.0000;

-- ---------------------------------------------------------------------------
-- SCHEMA: pagamentos
-- ---------------------------------------------------------------------------
CREATE SEQUENCE pagamentos.seq_num_pagto START WITH 1000 INCREMENT BY 1;

CREATE TABLE pagamentos.ciclo_pagamento (
    id                UUID         PRIMARY KEY,
    competencia       VARCHAR(6)   NOT NULL CHECK (competencia ~ '^[0-9]{6}$'),
    status            VARCHAR(20)  NOT NULL CHECK (status IN ('INICIADO','CALCULANDO','CALCULADO','EMITIDO','CANCELADO')),
    total_candidatos  INT          NOT NULL DEFAULT 0,
    total_pagamentos  INT          NOT NULL DEFAULT 0,
    total_ignorados   INT          NOT NULL DEFAULT 0,
    valor_total       NUMERIC(15,2) NOT NULL DEFAULT 0,
    iniciado_em       TIMESTAMPTZ  NOT NULL,
    calculado_em      TIMESTAMPTZ,
    requisitante_id   VARCHAR(36)  NOT NULL
);

-- REQ-PAY-002: bloqueio de duplicidade em competências não-canceladas.
CREATE UNIQUE INDEX ux_ciclo_competencia_ativa
    ON pagamentos.ciclo_pagamento(competencia)
    WHERE status <> 'CANCELADO';

CREATE TABLE pagamentos.pagamento (
    id              UUID          PRIMARY KEY,
    ciclo_id        UUID          NOT NULL,
    num_pagto       BIGINT        NOT NULL UNIQUE DEFAULT nextval('pagamentos.seq_num_pagto'),
    beneficiario_id UUID          NOT NULL,
    cpf             VARCHAR(11)   NOT NULL,
    status          VARCHAR(20)   NOT NULL CHECK (status IN ('PENDENTE','CALCULADO','EMITIDO','PAGO','REJEITADO','CANCELADO')),
    valor_base      NUMERIC(15,2) NOT NULL,
    fator_regional  NUMERIC(8,4)  NOT NULL DEFAULT 1.0000,
    fator_familiar  NUMERIC(8,4)  NOT NULL DEFAULT 1.0000,
    fator_renda     NUMERIC(8,4)  NOT NULL DEFAULT 1.0000,
    fator_idade     NUMERIC(8,4)  NOT NULL DEFAULT 1.0000,
    fator_reajuste  NUMERIC(8,4)  NOT NULL DEFAULT 1.0000,
    valor_final     NUMERIC(15,2) NOT NULL CHECK (valor_final >= 0),
    calculado_em    TIMESTAMPTZ
);
CREATE INDEX ix_pagamento_ciclo ON pagamentos.pagamento(ciclo_id);

-- Outbox (ADR-0003) — relay externo publica para o broker.
CREATE TABLE pagamentos.outbox_event (
    id            UUID         PRIMARY KEY,
    topico        VARCHAR(100) NOT NULL,
    tipo          VARCHAR(100) NOT NULL,
    payload_json  TEXT         NOT NULL,
    criado_em     TIMESTAMPTZ  NOT NULL DEFAULT now(),
    publicado_em  TIMESTAMPTZ,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE'
                  CHECK (status IN ('PENDENTE','PUBLICADO','FALHA'))
);
CREATE INDEX ix_outbox_status_criado ON pagamentos.outbox_event(status, criado_em);

-- ---------------------------------------------------------------------------
-- SCHEMA: auditoria (REQ-AUD-001) — append-only.
-- ---------------------------------------------------------------------------
CREATE TABLE auditoria.evento (
    id            UUID         PRIMARY KEY,
    tipo          VARCHAR(100) NOT NULL,
    agregado      VARCHAR(100) NOT NULL,
    agregado_id   VARCHAR(64)  NOT NULL,
    usuario_id    VARCHAR(64)  NOT NULL,
    payload_json  TEXT         NOT NULL,
    ocorrido_em   TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX ix_evento_agregado ON auditoria.evento(agregado, agregado_id);
CREATE INDEX ix_evento_ocorrido ON auditoria.evento(ocorrido_em);
