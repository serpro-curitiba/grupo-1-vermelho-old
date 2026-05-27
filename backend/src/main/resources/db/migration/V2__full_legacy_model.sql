-- ============================================================================
-- V2 — Port fiel do legado SIFAP (15 programas Natural + 4 DDMs)
-- Cobre CADBENEF / CADDEPEND / CADPROG / CALCBENF / CALCCORR / CALCDSCT
--       VALBENEF / VALDOCS / VALELEG / CONSBENF
--       BATCHPGT (completo) / BATCHCON / BATCHREL / RELAUDIT / RELPGT
-- ============================================================================
-- ---------------------------------------------------------------------------
-- novos schemas
-- ---------------------------------------------------------------------------
CREATE SCHEMA IF NOT EXISTS conciliacao;

CREATE SCHEMA IF NOT EXISTS relatorios;

-- ---------------------------------------------------------------------------
-- BENEFICIARIO — colunas completas do DDM BENEFICIARIO (ARQ 150)
-- ---------------------------------------------------------------------------
ALTER TABLE beneficiarios.beneficiario
ADD COLUMN num_inscricao BIGINT UNIQUE,
ADD COLUMN nome_mae VARCHAR(60),
ADD COLUMN nome_pai VARCHAR(60),
ADD COLUMN dt_nascimento DATE,
ADD COLUMN sexo VARCHAR(1) CHECK (
  sexo IS NULL
  OR sexo IN ('M', 'F', 'I')
),
ADD COLUMN est_civil VARCHAR(1) CHECK (
  est_civil IS NULL
  OR est_civil IN ('S', 'C', 'D', 'V', 'U')
),
ADD COLUMN rg_numero VARCHAR(15),
ADD COLUMN rg_orgao VARCHAR(10),
ADD COLUMN rg_uf VARCHAR(2),
ADD COLUMN logradouro VARCHAR(60),
ADD COLUMN numero VARCHAR(10),
ADD COLUMN complemento VARCHAR(30),
ADD COLUMN bairro VARCHAR(40),
ADD COLUMN municipio VARCHAR(40),
ADD COLUMN uf VARCHAR(2),
ADD COLUMN cep VARCHAR(8),
ADD COLUMN tel_fixo VARCHAR(14),
ADD COLUMN tel_celular VARCHAR(15),
ADD COLUMN email VARCHAR(80),
ADD COLUMN nis VARCHAR(11),
ADD COLUMN dt_cadastro DATE,
ADD COLUMN dt_inicio_benef DATE,
ADD COLUMN dt_fim_benef DATE,
ADD COLUMN mot_situacao VARCHAR(3),
ADD COLUMN documentos_ok VARCHAR(1) NOT NULL DEFAULT 'N' CHECK (documentos_ok IN ('S', 'N')),
ADD COLUMN ind_biometria VARCHAR(1) NOT NULL DEFAULT 'N' CHECK (ind_biometria IN ('S', 'N', 'P')),
ADD COLUMN cod_elegibilidade VARCHAR(5);

CREATE INDEX ix_beneficiario_nis ON beneficiarios.beneficiario (nis);

CREATE INDEX ix_beneficiario_inscr ON beneficiarios.beneficiario (num_inscricao);

-- ampliar status para conjunto completo do legado (A/S/C/I/D)
ALTER TABLE beneficiarios.beneficiario
DROP CONSTRAINT IF EXISTS beneficiario_status_check;

ALTER TABLE beneficiarios.beneficiario ADD CONSTRAINT beneficiario_status_check CHECK (status IN ('ATIVO', 'SUSPENSO', 'CANCELADO', 'INATIVO', 'DESLIGADO'));

-- sequence de num_inscricao (CADBENEF gera no INSERT)
CREATE SEQUENCE beneficiarios.seq_num_inscricao START
WITH
  100000 INCREMENT BY 1;

-- ---------------------------------------------------------------------------
-- DEPENDENTE — DDM BENEFICIARIO.GRP-DEPENDENTE (PE máx 10)
-- ---------------------------------------------------------------------------
ALTER TABLE beneficiarios.dependente
ADD COLUMN cpf VARCHAR(11),
ADD COLUMN dt_nascimento DATE,
ADD COLUMN parentesco VARCHAR(2) CHECK (
  parentesco IS NULL
  OR parentesco IN ('FI', 'CO', 'CJ', 'IR', 'NT', 'TU', 'OU')
),
ADD COLUMN documento VARCHAR(15),
ADD COLUMN sexo VARCHAR(1) CHECK (
  sexo IS NULL
  OR sexo IN ('M', 'F', 'I')
),
ADD COLUMN ind_deficiencia VARCHAR(1) NOT NULL DEFAULT 'N' CHECK (ind_deficiencia IN ('S', 'N')),
ADD COLUMN situacao VARCHAR(20) NOT NULL DEFAULT 'ATIVO' CHECK (situacao IN ('ATIVO', 'INATIVO', 'DESLIGADO'));

-- ---------------------------------------------------------------------------
-- DESCONTOS CADASTRAIS — origem CALCDSCT.NSN (DESCONTOS-V[*])
-- ---------------------------------------------------------------------------
CREATE TABLE
  beneficiarios.desconto_cadastro (
    id UUID PRIMARY KEY,
    beneficiario_id UUID NOT NULL REFERENCES beneficiarios.beneficiario (id) ON DELETE CASCADE,
    tipo VARCHAR(1) NOT NULL CHECK (tipo IN ('C', 'I', 'J', 'S', 'P', 'A')),
    valor_fixo NUMERIC(9, 2),
    percentual NUMERIC(5, 2),
    dt_inicio DATE NOT NULL,
    dt_fim DATE,
    num_processo VARCHAR(20),
    criado_em TIMESTAMPTZ NOT NULL DEFAULT now ()
  );

CREATE INDEX ix_desconto_cadastro_benef ON beneficiarios.desconto_cadastro (beneficiario_id);

-- ---------------------------------------------------------------------------
-- PROGRAMA — colunas do DDM PROGRAMA-SOCIAL (ARQ 151)
-- ---------------------------------------------------------------------------
ALTER TABLE programas.programa
ADD COLUMN sigla VARCHAR(10),
ADD COLUMN tipo VARCHAR(1) NOT NULL DEFAULT 'A' CHECK (tipo IN ('A', 'P', 'T')),
ADD COLUMN cod_elegibilidade VARCHAR(5),
ADD COLUMN dt_inicio DATE,
ADD COLUMN dt_fim DATE,
ADD COLUMN renda_max NUMERIC(9, 2),
ADD COLUMN idade_min INT,
ADD COLUMN idade_max INT,
ADD COLUMN pct_reajuste_anual NUMERIC(7, 4) NOT NULL DEFAULT 0,
ADD COLUMN fator_k NUMERIC(7, 4) NOT NULL DEFAULT 1.0000,
ADD COLUMN vlr_calc_ajustado NUMERIC(9, 2);

CREATE TABLE
  programas.faixa_calculo (
    id UUID PRIMARY KEY,
    programa_codigo VARCHAR(36) NOT NULL REFERENCES programas.programa (codigo) ON DELETE CASCADE,
    ordem INT NOT NULL CHECK (ordem BETWEEN 1 AND 5),
    renda_ate NUMERIC(9, 2) NOT NULL,
    fator NUMERIC(7, 4) NOT NULL,
    UNIQUE (programa_codigo, ordem)
  );

CREATE TABLE
  programas.parametro_regional (
    id UUID PRIMARY KEY,
    programa_codigo VARCHAR(36) NOT NULL REFERENCES programas.programa (codigo) ON DELETE CASCADE,
    regiao INT NOT NULL CHECK (regiao BETWEEN 1 AND 99),
    fator_extra NUMERIC(7, 4) NOT NULL DEFAULT 1.0000,
    UNIQUE (programa_codigo, regiao)
  );

-- ---------------------------------------------------------------------------
-- IPCA — usada por CALCCORR (#IPCA-ANO/#ANO-TAB)
-- ---------------------------------------------------------------------------
CREATE TABLE
  programas.indice_ipca (
    ano INT NOT NULL,
    mes INT NOT NULL CHECK (mes BETWEEN 1 AND 12),
    fator NUMERIC(8, 6) NOT NULL,
    PRIMARY KEY (ano, mes)
  );

INSERT INTO
  programas.indice_ipca (ano, mes, fator)
VALUES
  (2010, 1, 0.0075),
  (2010, 2, 0.0078),
  (2010, 3, 0.0052),
  (2010, 4, 0.0057),
  (2010, 5, 0.0043),
  (2010, 6, 0.0000),
  (2010, 7, 0.0001),
  (2010, 8, 0.0004),
  (2010, 9, 0.0045),
  (2010, 10, 0.0075),
  (2010, 11, 0.0083),
  (2010, 12, 0.0063),
  (2011, 1, 0.0083),
  (2011, 2, 0.0080),
  (2011, 3, 0.0079),
  (2011, 4, 0.0077),
  (2011, 5, 0.0047),
  (2011, 6, 0.0015),
  (2011, 7, 0.0016),
  (2011, 8, 0.0037),
  (2011, 9, 0.0053),
  (2011, 10, 0.0043),
  (2011, 11, 0.0052),
  (2011, 12, 0.0050),
  (2012, 1, 0.0056),
  (2012, 2, 0.0045),
  (2012, 3, 0.0021),
  (2012, 4, 0.0064),
  (2012, 5, 0.0036),
  (2012, 6, 0.0008),
  (2012, 7, 0.0043),
  (2012, 8, 0.0041),
  (2012, 9, 0.0054),
  (2012, 10, 0.0059),
  (2012, 11, 0.0060),
  (2012, 12, 0.0079);

-- ---------------------------------------------------------------------------
-- FATOR REGIONAL — substituir seed neutro por tabela real do CALCBENF.NSN
-- ---------------------------------------------------------------------------
DELETE FROM programas.fator_regional;

INSERT INTO
  programas.fator_regional (regiao, fator)
VALUES
  (1, 1.3500),
  (2, 1.3200),
  (3, 1.3000),
  (4, 1.2800),
  (5, 1.3100),
  (6, 1.4000),
  (7, 1.3800),
  (8, 1.3500),
  (9, 1.3200),
  (10, 1.3600),
  (11, 1.1000),
  (12, 1.1200),
  (13, 1.0800),
  (14, 1.0500),
  (15, 1.0000),
  (16, 1.0500),
  (17, 1.0700),
  (18, 1.0300),
  (19, 1.1500),
  (20, 1.2000),
  (21, 1.1800),
  (22, 1.2500),
  (23, 1.1000),
  (24, 1.2200),
  (25, 1.3300),
  (26, 1.0000),
  (27, 1.0000);

-- ---------------------------------------------------------------------------
-- PAGAMENTO — colunas adicionais do DDM PAGAMENTO (ARQ 152)
-- ---------------------------------------------------------------------------
ALTER TABLE pagamentos.pagamento
ADD COLUMN cod_programa VARCHAR(36),
ADD COLUMN competencia VARCHAR(6),
ADD COLUMN vlr_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN vlr_liquido NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN vlr_abono NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN vlr_13 NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN tipo_pgto VARCHAR(1) NOT NULL DEFAULT 'N' CHECK (tipo_pgto IN ('N', 'D', 'T')),
ADD COLUMN dt_geracao DATE,
ADD COLUMN dt_pagamento DATE,
ADD COLUMN cod_banco VARCHAR(3),
ADD COLUMN cod_agencia VARCHAR(6),
ADD COLUMN num_conta VARCHAR(13),
ADD COLUMN tipo_conta VARCHAR(1) CHECK (
  tipo_conta IS NULL
  OR tipo_conta IN ('C', 'P')
),
ADD COLUMN cod_retorno VARCHAR(2),
ADD COLUMN des_retorno VARCHAR(40),
ADD COLUMN dt_conciliacao DATE,
ADD COLUMN sit_conciliacao VARCHAR(1) NOT NULL DEFAULT 'N' CHECK (sit_conciliacao IN ('C', 'D', 'P', 'N')),
ADD COLUMN vlr_conciliado NUMERIC(15, 2),
ADD COLUMN vlr_correcao NUMERIC(15, 2),
ADD COLUMN ind_corrigido VARCHAR(1) NOT NULL DEFAULT 'N' CHECK (ind_corrigido IN ('S', 'N'));

ALTER TABLE pagamentos.pagamento
DROP CONSTRAINT IF EXISTS pagamento_status_check;

ALTER TABLE pagamentos.pagamento ADD CONSTRAINT pagamento_status_check CHECK (
  status IN (
    'PENDENTE',
    'CALCULADO',
    'GERADO',
    'EMITIDO',
    'PAGO',
    'REJEITADO',
    'DEVOLVIDO',
    'ESTORNADO',
    'CANCELADO'
  )
);

CREATE INDEX ix_pagamento_cpf_comp ON pagamentos.pagamento (cpf, competencia);

CREATE INDEX ix_pagamento_competencia ON pagamentos.pagamento (competencia);

CREATE INDEX ix_pagamento_status ON pagamentos.pagamento (status);

-- novos totalizadores no ciclo (BATCHREL/BATCHPGT)
ALTER TABLE pagamentos.ciclo_pagamento
ADD COLUMN total_bruto NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN total_desconto NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN total_liquido NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN total_abono NUMERIC(15, 2) NOT NULL DEFAULT 0,
ADD COLUMN total_13 NUMERIC(15, 2) NOT NULL DEFAULT 0;

-- ---------------------------------------------------------------------------
-- CONCILIAÇÃO — espelho de BATCHCON (arquivo CNAB 240)
-- ---------------------------------------------------------------------------
CREATE TABLE
  conciliacao.arquivo (
    id UUID PRIMARY KEY,
    nome_arquivo VARCHAR(120) NOT NULL,
    competencia VARCHAR(6) NOT NULL,
    importado_em TIMESTAMPTZ NOT NULL DEFAULT now (),
    qtd_lidos INT NOT NULL DEFAULT 0,
    qtd_conciliados INT NOT NULL DEFAULT 0,
    qtd_divergentes INT NOT NULL DEFAULT 0,
    qtd_nao_encontrados INT NOT NULL DEFAULT 0,
    sha256 VARCHAR(64)
  );

CREATE TABLE
  conciliacao.registro (
    id UUID PRIMARY KEY,
    arquivo_id UUID NOT NULL REFERENCES conciliacao.arquivo (id) ON DELETE CASCADE,
    cpf VARCHAR(11) NOT NULL,
    num_pagto BIGINT,
    vlr_retorno NUMERIC(15, 2) NOT NULL,
    dt_pagamento DATE,
    cod_retorno VARCHAR(2),
    resultado VARCHAR(20) NOT NULL CHECK (resultado IN ('CONCILIADO', 'DIVERGENTE', 'NAO_ENCONTRADO', 'ESTORNO')),
    diferenca NUMERIC(15, 2)
  );

CREATE INDEX ix_conciliacao_registro_arq ON conciliacao.registro (arquivo_id);

CREATE INDEX ix_conciliacao_registro_cpf ON conciliacao.registro (cpf);

-- ---------------------------------------------------------------------------
-- AUDITORIA — ampliar para o que o DDM AUDITORIA (ARQ 153) registra
-- ---------------------------------------------------------------------------
ALTER TABLE auditoria.evento
ADD COLUMN acao VARCHAR(2) CHECK (
  acao IS NULL
  OR acao IN ('IN', 'AL', 'EX', 'CO', 'CN', 'LG', 'LO', 'BT', 'ER', 'AU', 'RE', 'DV')
),
ADD COLUMN ip_origem VARCHAR(45),
ADD COLUMN id_correlacao VARCHAR(36),
ADD COLUMN sucesso VARCHAR(1) NOT NULL DEFAULT 'S' CHECK (sucesso IN ('S', 'N'));

CREATE TABLE
  auditoria.evento_campo_alterado (
    id UUID PRIMARY KEY,
    evento_id UUID NOT NULL REFERENCES auditoria.evento (id) ON DELETE CASCADE,
    ordem INT NOT NULL CHECK (ordem BETWEEN 1 AND 20),
    campo VARCHAR(50) NOT NULL,
    valor_ant VARCHAR(200),
    valor_pos VARCHAR(200),
    UNIQUE (evento_id, ordem)
  );

CREATE INDEX ix_auditoria_acao ON auditoria.evento (acao);