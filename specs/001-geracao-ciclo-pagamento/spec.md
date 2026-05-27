<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Spec 001 — Geração de Ciclo de Pagamento Mensal

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-FFB900?style=for-the-badge) ![TIPO Spec](https://img.shields.io/badge/TIPO-Spec-1A1A1A?style=for-the-badge) ![ENGINE Spec-Kit](https://img.shields.io/badge/ENGINE-Spec--Kit-00A4EF?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Specs](../README.md) → **001-geracao-ciclo-pagamento**
>
> **Status:** Rascunho v0.1 (Par 2 · Arquitetura) — aguardando revisão de Par 1 (PO + RE).
> **Bounded context:** `pagamentos`
> **Origem legado primária:** [`BATCHPGT.NSN`](../../01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN)

---

## 1. Visão geral

Substituir o job batch noturno do SIFAP legado (`BATCHPGT.NSN`) por um caso de uso transacional moderno que **gera um ciclo de pagamento para uma competência (AAAAMM)**, calcula o valor de cada benefício elegível segundo as regras herdadas e deixa o ciclo pronto para emissão de remessa CNAB ao Banco do Brasil e empenho ao SIAFI (estes dois últimos cobertos por specs subsequentes).

### 1.1 Personas envolvidas

| Persona | Como participa |
|---|---|
| Gestor (administrador) | Dispara a geração do ciclo via portal; aprova exceções. |
| Sistema (scheduler) | Pode disparar geração agendada (ex.: dia 25 de cada mês). |
| Beneficiário / Cidadão | Não interage diretamente; consulta o resultado pelo portal cidadão (spec separada). |
| Fiscal | Lê o ciclo gerado para conferência amostral. |

### 1.2 Escopo

| Dentro do escopo | Fora do escopo (specs separadas) |
|---|---|
| Criação do agregado `CicloPagamento` | Emissão de remessa CNAB 240 (`002-emissao-remessa-cnab`) |
| Cálculo do valor de cada `Pagamento` | Envio de empenho SIAFI (`003-empenho-siafi`) |
| Marcação de inelegíveis com motivo | Conciliação de retornos bancários (`004-conciliacao-cnab`) |
| Publicação de evento `CicloIniciado` e `PagamentoCalculado` | Notificação ao cidadão (`005-portal-cidadao`) |

---

## 2. Histórias de usuário

### US-001 — Gestor gera ciclo da competência

> **Como** gestor do programa,
> **quero** disparar a geração do ciclo de pagamento de uma competência específica,
> **para que** os beneficiários ativos recebam o valor correto naquele mês.

**Critério de aceite (cenário principal):**

```gherkin
Dado que existem 100 beneficiários cadastrados, sendo 80 ativos e 20 suspensos,
  E que nenhum ciclo foi gerado ainda para a competência 202606,
Quando o gestor dispara POST /api/v1/ciclos com {"competencia": "202606"},
Então o sistema cria um CicloPagamento com status "CALCULADO",
  E gera 80 entidades Pagamento (uma por beneficiário ativo),
  E não gera pagamentos para os 20 suspensos,
  E publica 1 evento CicloIniciado + 80 eventos PagamentoCalculado.
```

### US-002 — Gestor evita duplicidade

> **Como** gestor,
> **quero** que o sistema rejeite a geração duplicada de um ciclo já existente,
> **para que** nenhum beneficiário receba pagamento em duplicidade.

```gherkin
Dado que já existe um ciclo CALCULADO para 202606,
Quando o gestor dispara POST /api/v1/ciclos com {"competencia": "202606"},
Então o sistema retorna HTTP 409 Conflict,
  E não cria novos pagamentos,
  E registra evento de auditoria CicloDuplicadoBloqueado.
```

### US-003 — Fiscal consulta detalhes de um pagamento

> **Como** fiscal,
> **quero** ver a composição do valor de um pagamento específico,
> **para que** eu possa auditar a aplicação das regras de cálculo.

```gherkin
Dado um pagamento P-123 calculado a partir das regras vigentes,
Quando o fiscal acessa GET /api/v1/pagamentos/P-123,
Então a resposta contém: valorBase, fatorRegional, fatorFamiliar, fatorRenda, fatorIdade, valorFinal.
```

---

## 3. Requisitos EARS

> **Notação EARS:**
>
> - **Ubiquitous:** "The system shall …"
> - **Event-driven:** "When `<gatilho>`, the system shall …"
> - **State-driven:** "While `<estado>`, the system shall …"
> - **Optional:** "Where `<feature>`, the system shall …"
> - **Unwanted behavior:** "If `<condição>`, then the system shall …"
>
> Todos os REQs carregam `source_legacy:` (HARD GATE do CI).

### 3.1 Disparo e idempotência

#### REQ-PAY-001 · Disparo manual

When um usuário com papel `GESTOR_PAGAMENTOS` envia `POST /api/v1/ciclos` informando uma competência válida no formato AAAAMM, the system shall criar um aggregate `CicloPagamento` com `status=INICIADO` e retornar HTTP 202 com o `cicloId`.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L168-L172`
- **Prioridade:** must
- **Notas:** No legado o gatilho era 100% batch noturno; modernizar para REST + scheduler opcional.

#### REQ-PAY-002 · Bloqueio de duplicidade

If já existir um `CicloPagamento` para a mesma competência com `status ≠ CANCELADO`, then the system shall rejeitar a requisição com HTTP 409 e publicar evento `CicloDuplicadoBloqueado`.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L201-L213` (verificação "JA-GERADO" via `FIND PAGAMENTO-V WITH CPF-BENEF`)
- **Prioridade:** must

#### REQ-PAY-003 · Competência futura

If a competência informada for posterior à competência corrente do sistema (ex.: 202608 quando hoje é 202606), then the system shall rejeitar com HTTP 422 e mensagem `competencia.futura`.

- **source_legacy:** `[GREENFIELD]` — o legado batch confiava no operador; a API exige guarda explícita.
- **Prioridade:** should

### 3.2 Seleção de beneficiários elegíveis

#### REQ-PAY-010 · Apenas ativos

When o ciclo entra em fase de geração, the system shall considerar apenas beneficiários com `status=ATIVO` no momento do disparo.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L196-L200` (`IF BENEFICIARIO-V.STATUS NE 'A' … ESCAPE TOP`)
- **Prioridade:** must

#### REQ-PAY-011 · Deduplicação por CPF

If dois registros de beneficiário compartilharem o mesmo CPF, then the system shall processar apenas o primeiro (ordem determinística por `cpf` ascendente) e registrar `BeneficiarioDuplicadoIgnorado` com nível WARN.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L188-L195` (controle `#CPF-ANT`)
- **Prioridade:** must
- **Notas:** No legado essa lógica era cosmética (READ por CPF já ordenava); manter para paridade comportamental e migração de dados.

#### REQ-PAY-012 · Programa ativo

If o programa referenciado pelo beneficiário não existir OU seu `status=INATIVO`, then the system shall pular o beneficiário, registrar `BeneficiarioSemProgramaAtivo` e incrementar contador de ignorados.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L222-L235`
- **Prioridade:** must

### 3.3 Cálculo do valor do benefício

#### REQ-PAY-020 · Fórmula principal

When um beneficiário elegível for processado, the system shall calcular `valorFinal = valorBase × fatorRegional × fatorFamiliar × fatorRenda × fatorIdade × fatorReajuste`, arredondado a 2 casas decimais (HALF_UP).

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L277-L278` (`COMPUTE #VLR-BENF = #VLR-BASE * #FATOR-REG * #FATOR-FAM…`)
- **Prioridade:** must

#### REQ-PAY-021 · Fator regional

The system shall aplicar `fatorRegional` lido da tabela de 25 regiões cadastradas; se a região do beneficiário estiver fora do intervalo 1–25, the system shall usar `1.0000` como fallback.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L243-L247`
- **Prioridade:** must
- **Notas:** A tabela está hardcoded no Natural (27 posições; só 25 documentadas). Investigar posições 26–27 — possível Easter egg / mistério aberto.

#### REQ-PAY-022 · Fator familiar

The system shall calcular `fatorFamiliar` a partir do número de dependentes:

| Dependentes | Fórmula |
|---|---|
| 0 | `1.0000` |
| 1 a 2 | `1.0000 + (n × 0.0500)` |
| 3 a 4 | `1.1000 + ((n − 2) × 0.0300)` |
| 5+ | `1.1600 + ((n − 4) × 0.0200)` |

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L249-L262`
- **Prioridade:** must

#### REQ-PAY-023 · Fator renda

The system shall determinar `fatorRenda` por faixa: até 300 → 1.0000; até 600 → 0.8500; até 1000 → 0.7000; até 1500 → 0.5500; acima → 0.4000.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L153-L161`
- **Prioridade:** must

#### REQ-PAY-024 · Fator idade

The system shall aplicar `fatorIdade`: idade ≥ 65 → 1.1500; 60–64 → 1.1000; < 18 → 1.0500; demais → 1.0000.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L264-L275`
- **Prioridade:** must

### 3.4 Persistência e ordenação

#### REQ-PAY-030 · Sequencial de pagamento

When um `Pagamento` for persistido, the system shall atribuir `numPagto` único e monotônico em todo o sistema (sem reset por ciclo).

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L173-L177` (busca `MAX(NUM-PAGTO)` via `READ … DESCENDING ESCAPE BOTTOM`)
- **Prioridade:** must
- **Notas técnicas:** Modernizar para sequence do PostgreSQL ou identidade Snowflake; preservar continuidade do contador legado na migração de dados.

#### REQ-PAY-031 · Ordenação por CPF

The system shall persistir pagamentos em ordem ascendente por CPF do beneficiário **dentro do ciclo**, mantendo a invariante do legado.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L181-L184` (comentário: `LEITURA EM ORDEM ALFABETICA POR CPF (OTIMIZACAO 1999) — SISTEMAS DOWNSTREAM DEPENDEM DESTA ORDENACAO`)
- **Prioridade:** should
- **Notas:** Decisão pendente em ADR — confirmar com Par 1 se algum downstream externo (SIAFI/BB) ainda depende dessa ordem ou se podemos abandonar.

### 3.5 Eventos e observabilidade

#### REQ-PAY-040 · Evento de início de ciclo

When `CicloPagamento` for criado, the system shall publicar `CicloIniciado{cicloId, competencia, totalBeneficiariosCandidatos, iniciadoEm}` no broker via padrão Outbox.

- **source_legacy:** `[GREENFIELD]` — o legado apenas escrevia em `WRITE` para o spool de impressão (linhas 167-171 do BATCHPGT).
- **Prioridade:** must

#### REQ-PAY-041 · Evento por pagamento

When um `Pagamento` for calculado e persistido com sucesso, the system shall publicar `PagamentoCalculado{pagamentoId, cicloId, cpf, valorFinal, competencia}`.

- **source_legacy:** `[GREENFIELD]` (decorrente do desacoplamento monolito ↔ worker).
- **Prioridade:** must

#### REQ-PAY-042 · Contadores

The system shall expor as métricas `sifap_ciclo_processados_total`, `sifap_ciclo_ignorados_total` e `sifap_ciclo_erros_total` por competência (compatíveis com Prometheus/Azure Monitor).

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L335-L345` (contadores `#QTD-PROCESSADOS`, `#QTD-IGNORADOS`, `#QTD-ERROS` impressos no resumo).
- **Prioridade:** should

### 3.6 Segurança e auditoria

#### REQ-PAY-050 · Autorização

The system shall exigir token JWT com claim `roles=[GESTOR_PAGAMENTOS]` para todos os endpoints `POST /api/v1/ciclos*`.

- **source_legacy:** `[GREENFIELD]` — o legado autenticava por usuário do terminal 3270 (sem RBAC fino).
- **Prioridade:** must

#### REQ-PAY-051 · Mascaramento de CPF em logs

The system shall mascarar CPFs em logs estruturados (padrão `***.***.123-45`) e nunca persistir CPFs em campos de log indexáveis.

- **source_legacy:** `[GREENFIELD]` — exigência da LGPD (não vigente em 1997).
- **Prioridade:** must

#### REQ-PAY-052 · Trilha de auditoria

When qualquer evento de domínio do ciclo for publicado, the system shall garantir consumo pelo módulo `auditoria` (append-only), com timestamp UTC e correlationId.

- **source_legacy:** `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN` (escrita no spool) + DDM `AUDITORIA` (escritas em BATCHCON).
- **Prioridade:** must

---

## 4. Mapa de rastreabilidade

| REQ-ID | Origem legado | Tipo |
|---|---|---|
| REQ-PAY-001 | `BATCHPGT.NSN#L168-L172` | Adaptação |
| REQ-PAY-002 | `BATCHPGT.NSN#L201-L213` | Adaptação |
| REQ-PAY-003 | `[GREENFIELD]` | Novo |
| REQ-PAY-010 | `BATCHPGT.NSN#L196-L200` | Paridade |
| REQ-PAY-011 | `BATCHPGT.NSN#L188-L195` | Paridade |
| REQ-PAY-012 | `BATCHPGT.NSN#L222-L235` | Paridade |
| REQ-PAY-020 | `BATCHPGT.NSN#L277-L278` | Paridade |
| REQ-PAY-021 | `BATCHPGT.NSN#L243-L247` | Paridade + mistério aberto (posições 26-27 da tabela) |
| REQ-PAY-022 | `BATCHPGT.NSN#L249-L262` | Paridade |
| REQ-PAY-023 | `BATCHPGT.NSN#L153-L161` | Paridade |
| REQ-PAY-024 | `BATCHPGT.NSN#L264-L275` | Paridade |
| REQ-PAY-030 | `BATCHPGT.NSN#L173-L177` | Adaptação (sequence PG) |
| REQ-PAY-031 | `BATCHPGT.NSN#L181-L184` | Paridade (decisão pendente) |
| REQ-PAY-040 | `[GREENFIELD]` | Novo (Outbox) |
| REQ-PAY-041 | `[GREENFIELD]` | Novo (Outbox) |
| REQ-PAY-042 | `BATCHPGT.NSN#L335-L345` | Adaptação |
| REQ-PAY-050 | `[GREENFIELD]` | Novo (RBAC) |
| REQ-PAY-051 | `[GREENFIELD]` | Novo (LGPD) |
| REQ-PAY-052 | DDM `AUDITORIA` + escritas legacy | Adaptação |

---

## 5. Premissas e dependências

- **Bounded contexts envolvidos:** `pagamentos` (owner), `beneficiarios` (lê snapshot), `programas` (lê regras vigentes), `auditoria` (consome eventos).
- **Tecnologia:** Java 21 + Spring Boot 3.3 + Spring Modulith + PostgreSQL 16 + broker (RabbitMQ local / Azure Service Bus prod).
- **Pré-requisitos:** dados de beneficiários e programas migrados; ADR-0002 (Modular Monolith) aprovado; ADR-0003 (Outbox) aprovado.

## 6. Critérios de pronto da spec

- [x] Toda EARS tem `source_legacy:` (HARD GATE).
- [x] REQs cobrem cenários de erro (REQ-PAY-002, 003, 011, 012).
- [x] Cenários Gherkin nas histórias-chave.
- [ ] Revisão de Par 1 (PO + RE) registrada como comentário no PR.
- [ ] ADR-0002 e ADR-0003 abertos (Par 2).
- [ ] `plan.md`, `data-model.md`, `contracts/openapi.yaml` e `tasks.md` gerados via `/speckit.plan` + `/speckit.tasks`.

## 7. Mistérios e decisões abertas

- **Mistério aberto:** posições 26-27 da tabela `#TAB-REG` em [BATCHPGT.NSN#L150-L151](../../01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN). A documentação fala de 25 regiões; há 27 posições. Investigar antes do Estágio 3 ou abrir MYS-002.
- **Decisão pendente:** REQ-PAY-031 (ordenação por CPF) — manter ou abandonar? Depende de confirmação com sistemas downstream reais.
- **Decisão pendente:** estratégia para preservar `numPagto` contínuo na migração de dados (importar último valor do legado).

---

> **Próximo passo:** rodar `/speckit.plan` para transformar este spec em `plan.md` técnico e `/speckit.tasks` para quebrar em tarefas.
