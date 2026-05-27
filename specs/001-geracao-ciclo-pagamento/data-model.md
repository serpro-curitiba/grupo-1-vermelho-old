<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Data Model 001 — Geração de Ciclo de Pagamento

![ESTÁGIO 02](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-FFB900?style=for-the-badge) ![TIPO Data Model](https://img.shields.io/badge/TIPO-Data%20Model-1A1A1A?style=for-the-badge)

> Gerado a partir de [`plan.md`](plan.md) §3. Materializado pela migração Flyway `V1__init_pagamentos.sql` no protótipo (`backend/src/main/resources/db/migration/`).

## 1. Diagrama lógico (Mermaid)

```mermaid
erDiagram
    CICLO_PAGAMENTO ||--o{ PAGAMENTO : "contém"
    CICLO_PAGAMENTO ||--o{ BENEFICIARIO_SNAPSHOT : "fotografa"
    PAGAMENTO ||--|| BENEFICIARIO_SNAPSHOT : "valoriza"
    PAGAMENTO ||--o{ OUTBOX_EVENT : "emite"

    CICLO_PAGAMENTO {
        uuid id PK
        varchar(6) competencia "AAAAMM UNIQUE WHERE status<>CANCELADO"
        varchar(20) status "INICIADO|CALCULANDO|CALCULADO|EMITIDO|CANCELADO"
        int total_candidatos
        int total_pagamentos
        int total_ignorados
        numeric(15_2) valor_total
        timestamp iniciado_em
        timestamp calculado_em
        varchar(36) requisitante_id
    }
    PAGAMENTO {
        uuid id PK
        uuid ciclo_id FK
        bigint num_pagto "UNIQUE — sequência contínua a partir do legado"
        uuid beneficiario_id
        varchar(11) cpf "armazenado em claro; mascarado nos logs"
        varchar(20) status "PENDENTE|CALCULADO|EMITIDO|PAGO|REJEITADO|CANCELADO"
        numeric(15_2) valor_base
        numeric(8_4) fator_regional
        numeric(8_4) fator_familiar
        numeric(8_4) fator_renda
        numeric(8_4) fator_idade
        numeric(8_4) fator_reajuste
        numeric(15_2) valor_final
        timestamp calculado_em
    }
    BENEFICIARIO_SNAPSHOT {
        uuid ciclo_id PK_FK
        uuid beneficiario_id PK
        varchar(11) cpf
        varchar(100) nome
        int regiao
        int qtd_dependentes
        numeric(15_2) renda_mensal
        int idade
        varchar(20) status_origem
        varchar(36) programa_codigo
    }
    OUTBOX_EVENT {
        uuid id PK
        varchar(100) topico
        varchar(100) tipo
        text payload_json
        timestamp criado_em
        timestamp publicado_em
        varchar(20) status "PENDENTE|PUBLICADO|FALHA"
    }
```

## 2. Tabelas (DDL resumido)

### 2.1 `pagamentos.ciclo_pagamento`

| Coluna             | Tipo            | Restrição                       | REQ rastreado   |
| ------------------ | --------------- | ------------------------------- | --------------- |
| `id`               | `uuid`          | PK, default `gen_random_uuid()` | REQ-PAY-001     |
| `competencia`      | `varchar(6)`    | NOT NULL, regex `^[0-9]{6}$`    | REQ-PAY-001     |
| `status`           | `varchar(20)`   | NOT NULL, check `IN (...)`      | REQ-PAY-001     |
| `total_candidatos` | `int`           | NOT NULL DEFAULT 0              | REQ-PAY-010     |
| `total_pagamentos` | `int`           | NOT NULL DEFAULT 0              | REQ-PAY-020     |
| `total_ignorados`  | `int`           | NOT NULL DEFAULT 0              | REQ-PAY-011/012 |
| `valor_total`      | `numeric(15,2)` | NOT NULL DEFAULT 0              | REQ-PAY-020     |
| `iniciado_em`      | `timestamptz`   | NOT NULL                        | REQ-AUD-001     |
| `calculado_em`     | `timestamptz`   | NULLABLE                        | REQ-AUD-001     |
| `requisitante_id`  | `varchar(36)`   | NOT NULL                        | REQ-AUD-001     |

**Índices/constraints:**

- `UNIQUE INDEX ux_ciclo_competencia_ativa ON ciclo_pagamento(competencia) WHERE status <> 'CANCELADO'` → garante REQ-PAY-002.

### 2.2 `pagamentos.pagamento`

PK `id UUID`; FK lógica `ciclo_id → ciclo_pagamento.id` (sem cascata).
Sequence `pagamentos.seq_num_pagto` iniciada em `1000` (no protótipo) — em produção inicia em `MAX(NUM-PAGTO legado) + 1000` (ADR-002).
`CHECK (valor_final >= 0)`.
`UNIQUE(num_pagto)` → preserva REQ-PAY-030.

### 2.3 `pagamentos.beneficiario_snapshot`

PK composta `(ciclo_id, beneficiario_id)`. Imutável: cópia dos atributos no momento da geração; suporta REQ-AUD-001 e a futura conferência de equivalência com o legado.

### 2.4 `pagamentos.outbox_event`

Implementa [ADR-0003](../../docs/adr/0003-outbox-broker.md). Relay externo lê `status='PENDENTE'` em batch de 200.

## 3. Schemas isolados (ADR-0002)

| Schema PostgreSQL | Bounded context | Tabelas iniciais                                                |
| ----------------- | --------------- | --------------------------------------------------------------- |
| `pagamentos`      | pagamentos      | ciclo_pagamento, pagamento, beneficiario_snapshot, outbox_event |
| `beneficiarios`   | beneficiarios   | beneficiario, dependente, status_historico                      |
| `auditoria`       | auditoria       | evento, usuario_externo                                         |
| `programas`       | programas       | programa, fator_regional                                        |

Sem FKs cross-schema. Integração apenas via API do módulo ou eventos.

## 4. Mapeamento legacy → novo

| Adabas (DDM)                         | Tabela nova                                   | Transformação                        |
| ------------------------------------ | --------------------------------------------- | ------------------------------------ |
| `PAGAMENTO-V.NUM-PAGTO`              | `pagamento.num_pagto`                         | direto (BIGINT)                      |
| `PAGAMENTO-V.VLR-BENF`               | `pagamento.valor_final`                       | divisão por 100 (legado em centavos) |
| `BENEFICIARIO-V.DEPENDENTES[*]` (PE) | `beneficiarios.dependente` (1:N)              | desnormalização do Periodic Group    |
| `PAGAMENTO-V.CPF-BENEF`              | `pagamento.cpf` + `beneficiario_snapshot.cpf` | direto, com mascaramento em logs     |
| `#TAB-REG[1..25]`                    | `programas.fator_regional`                    | tabela paramétrica                   |
| `#TAB-REG[26..27]`                   | descartado                                    | mistério MYS-002 — sem uso real      |
