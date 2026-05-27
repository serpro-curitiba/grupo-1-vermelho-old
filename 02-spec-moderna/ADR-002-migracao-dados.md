<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-002: Estratégia de migração de dados Adabas → PostgreSQL (Strangler Fig)

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![Status Aceito](https://img.shields.io/badge/Status-Aceito-7FBA00?style=for-the-badge)

**Data:** 2026-05-27
**Status:** Aceito
**Decisores:** Par 2 (EA + SA), Par 3 (TL + Developer), Par 4 (DBA + QA)

> Decisão sobre **como** migrar os dados dos 4 DDMs Adabas (`BENEFICIARIO`, `PROGRAMA-SOCIAL`, `PAGAMENTO`, `AUDITORIA`) para o PostgreSQL 16 do SIFAP 2.0, **sem** janela de downtime maior que 1 hora e **preservando** a continuidade de `NUM-PAGTO` (REQ-PAY-030).

## Contexto

A arqueologia (Estágio 1) confirmou:

- **4 DDMs Adabas** com tipos não-relacionais (Periodic Groups `PE` em `BENEFICIARIO.DEPENDENTES`, MUs implícitos, campos `A1` codificados).
- **Sem `CALLNAT`** entre programas — todo o acoplamento é por dados compartilhados ([`01-arqueologia/dependency-map.md`](../01-arqueologia/dependency-map.md)).
- **Volumes esperados (projeção):** 2,1 M beneficiários, 380 mil dependentes, 78 M pagamentos históricos (1997–2026), 240 M registros de auditoria.
- **Janelas críticas:** ciclo mensal roda dia 25; o legado processa por 6h. Não podemos migrar entre os dias 23 e 28.
- **Restrições legais:** auditoria não pode perder nem 1 evento (TCU exige rastreabilidade total desde 1997).
- **Restrição operacional:** equipe de 5 pessoas no workshop e ~15 em produção — não suporta dual-write manual prolongado.

Três abordagens foram avaliadas:

### Opção 1 — Big-Bang ETL

- **Descrição:** parar o SIFAP legado num fim de semana, exportar todos os DDMs com `ADAREP`/`ADAULD`, transformar via Spark, carregar no PostgreSQL, religar o sistema moderno.
- **Vantagens:** simples; sem código de sincronização.
- **Desvantagens:** janela de >24h indisponível; rollback complexo (precisaria reexportar do legado); todo o risco concentrado em uma data; impossível validar dados em produção antes do go-live.

### Opção 2 — Strangler Fig com CDC (Change Data Capture)

- **Descrição:** o moderno sobe **lendo** PostgreSQL replicado em tempo real do Adabas via Event Replicator for Adabas (ERA) → Kafka → consumer Java que aplica em PG. As escritas continuam no legado por algumas semanas (read-modernization); depois invertemos para escrita no moderno e read-back para legado (write-modernization); legado é desligado por bounded context.
- **Vantagens:** janela de corte por bounded context (cada um < 1h); valida dados em produção antes do go-live total; rollback por contexto.
- **Desvantagens:** requer ERA (licença Software AG) ou ferramenta equivalente; código de sincronização não-trivial; janela de coexistência ~4 semanas por contexto.

### Opção 3 — Dual-write em camada de aplicação

- **Descrição:** a nova API escreve simultaneamente no Adabas (via gateway Natural/RPC) e no PostgreSQL. Quando paridade estiver provada, desliga o Adabas.
- **Vantagens:** controle fino do que é replicado; não exige ERA.
- **Desvantagens:** complexidade do gateway Natural; escritas distribuídas sem 2PC = risco de divergência permanente; performance penalizada (2 writes); precisa manter expertise Natural ativa.

## Decisão

**Adotamos a Opção 2 — Strangler Fig com CDC**, na seguinte sequência por bounded context:

| Ordem | Bounded context | Fase Strangler                                       | Janela alvo  |
| :---: | --------------- | ---------------------------------------------------- | ------------ |
|   1   | `auditoria`     | append-only — read+write moderno direto              | semana 1     |
|   2   | `programas`     | low-volume, baixo risco                              | semana 2     |
|   3   | `beneficiarios` | read moderno (CDC) → write moderno                   | semanas 3-5  |
|   4   | `pagamentos`    | read moderno → write moderno após 2 ciclos validados | semanas 6-10 |
|   5   | `conciliacao`   | move-com-pagamentos                                  | semana 10    |

Princípios técnicos:

1. **CDC unidirecional** com ERA (ou alternativa Qlik Replicate) durante a fase de read-modernization. **Sem dual-write**.
2. **Sequence PG `pagamento.num_pagto` inicia em `MAX(legado.NUM-PAGTO) + 1000`** (gap de segurança) — atende REQ-PAY-030.
3. **Snapshot de migração** persistido em `migracao.snapshot_<contexto>` antes de cada virada — permite rollback de leitura.
4. **Validação contínua** via job Spark que compara totais (`COUNT`, `SUM(VLR-BENF)`, `MAX(timestamp)`) entre Adabas e PG a cada 5 minutos. Divergência > 0.01 % dispara alerta P1.
5. **Periodic Groups `PE`** (dependentes) viram tabela relacional `beneficiario_dependente` com FK lógica (sem FK física cross-schema, conforme [ADR-0002](../docs/adr/0002-modular-monolith.md)).
6. **Trilhas de auditoria históricas** (1997–2026) migram em batch noturno separado, fora do caminho crítico — append-only no schema `auditoria`.
7. **`#TAB-REG` posições 26-27** **não** migram (decisão de escopo §16 — descartar).

## Justificativa

A Opção 2 é a única que:

- mantém o sistema legado de pé enquanto o moderno é validado **com dados reais de produção**;
- permite **rollback por bounded context** (não tudo-ou-nada);
- respeita as **janelas regulatórias** (TCU + ciclo mensal);
- viabiliza testes de equivalência (Par 4) com dataset real, atendendo o item REQ-PAY-020..024.

O custo de ERA é justificado pelo de-risk; a Software AG cobra ~R$ 180 k/ano que cabe no budget de transição (12 meses).

## Consequências

### Positivas

- Migração incremental, rollback granular.
- Equivalência comportamental validada com dados reais antes do switch-over.
- `auditoria` migra primeiro — destrava o item `REQ-AUD-001` (append-only) cedo.

### Negativas

- Coexistência Adabas + PG por ~3 meses → custo dobrado de infra (mitigação: contratar ERA por 6 meses apenas).
- Equipe precisa entender CDC + consumer Java idempotente.
- Janela de inconsistência eventual (lag CDC ≤ 5 s) — comunicar a fiscais que relatórios "instantâneos" no moderno podem mostrar dado até 5 s atrás.

### Riscos

| Risco                                                                 | Mitigação                                                                          |
| --------------------------------------------------------------------- | ---------------------------------------------------------------------------------- |
| Lag CDC > 5 s em pico de ciclo                                        | Particionar consumer por hash de CPF; alerta de lag em Application Insights        |
| Periodic Group `DEPENDENTES(PE)` perder ordem na transformação        | Capturar índice `1..5` como coluna `ordem`; teste de equivalência por (cpf, ordem) |
| `NUM-PAGTO` colidir após failover                                     | Gap de 1000 + reserva de range antes da virada; teste de fail-over com fixture     |
| ERA falhar e backlog crescer indefinidamente                          | Circuit breaker: se backlog > 30 min, congela writes no legado e alerta P1         |
| Descoberta tardia de regra escondida não catalogada (ex.: BR-CON-005) | QA roda diff diário por bounded context; encontros semanais com Par 3 (RE)         |

## Referências

- [ADR-0002 Modular Monolith](../docs/adr/0002-modular-monolith.md) — schema por bounded context
- [ADR-0003 Outbox + Broker](../docs/adr/0003-outbox-broker.md) — eventos pós-migração
- [`SPECIFICATION.md` §4 REQ-PAY-030](SPECIFICATION.md) — preservação de `NUM-PAGTO`
- [`01-arqueologia/discovery-report.md`](../01-arqueologia/discovery-report.md) — volumes e dependências
- [`01-arqueologia/business-rules-catalog.md`](../01-arqueologia/business-rules-catalog.md) — 116 regras a preservar
- Software AG — [Event Replicator for Adabas](https://documentation.softwareag.com/adabas/era/v3-5/era-overview/era-overview.htm)
- Martin Fowler — [Strangler Fig Application](https://martinfowler.com/bliki/StranglerFigApplication.html)
- REQ relacionados: **REQ-PAY-030, REQ-PAY-031, REQ-CON-001, REQ-AUD-001**
