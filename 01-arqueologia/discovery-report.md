<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Relatório de Descoberta — Estágio 1: Arqueologia Digital

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **discovery-report**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).

> Este documento consolida todas as descobertas do Estágio 1.
> Preencha cada seção com as conclusões do time. **Este é o input principal do Estágio 2** — sem ele, a especificação vira chute.

**Time**: Grupo 1 Vermelho — Par 3 (Technical Lead + Developer)
**Data**: 27/05/2026
**Edição**: 1.0
**Participantes**: Par 3 (TL+Dev, 3 programas analisados), com suporte de Par 1 (escopo), Par 4 (DDM), Par 5 (glossário)

---

## 1. Sumário Executivo

O SIFAP é um sistema crítico de cálculo e administração de benefícios sociais com 29 anos de histórico. O legado implementa três algoritmos financeiros complexos encadeados (CALCBENF → CALCCORR → CALCDSCT) que precisam ser preservados com precisão na modernização. Foram identificadas 17 regras de negócio formalizadas e 9 mistérios (3 críticos) que bloqueiam progresso seguro. A cascata de execução é não-negociável para equivalência com pagamentos históricos. Complexidade financeira principal: correção retroativa por IPCA com juros compostos, descontos multi-tipo com limite 30% (exceto judicial), e fatores de elegibilidade não-triviais.

---

## 2. Visão Geral do Sistema

### 2.1 Propósito do SIFAP

Sistema de Fiscalização e Administração de Pagamentos da Secretaria Nacional de Renda de Cidadania (SENARC). Responsável por:

- Cadastro de beneficiários de programas sociais (renda mínima, transferências condicionadas)
- Cálculo de elegibilidade e valor do benefício mensal
- Correção retroativa de pagamentos por índice IPCA
- Aplicação de descontos compulsórios (contribuição, imposto, judicial, pensão, sindical, administrativo)
- Auditoria e conformidade com regras do Ministério (critérios de renda, região, número de dependentes)

### 2.2 Arquitetura Legada

**15 programas Natural**, cada qual com responsabilidade específica:

- **3 críticos de cálculo** (foco desta análise): CALCBENF, CALCCORR, CALCDSCT
- **Programas de cadastro** (CADBENEF, CADDEPEND, CADPROG): entrada de dados beneficiário e programas
- **Programas de validação** (VALBENEF, VALDOCS, VALELEG): checagem de elegibilidade
- **Programas de batch** (BATCHPGT, BATCHREL, BATCHCON): processamento em lote, relatórios, consolidação
- **Programas de consulta** (CONSBENF, RELPGT, RELAUDIT): leitura/relatório de dados

**Fluxo principal** (não-alterável):

```
BATCHPGT (inicia batch)
  ↓
CALCBENF (calcula VLR-BRUTO)
  ↓
CALCCORR (aplica correção IPCA retroativa)
  ↓
CALCDSCT (aplica descontos, calcula VLR-LIQUIDO)
  ↓
ARQ-160 (PAGAMENTO — resultado final, armazenado em Adabas)
```

**Banco de dados**: 4 arquivos Adabas (DDM):

- ARQ-150 BENEFICIARIO (beneficiários + dados cadastrais, com PE de descontos)
- ARQ-155 PROGRAMA-SOCIAL (configurações de programas)
- ARQ-160 PAGAMENTO (resultado de processamento, com histórico)
- ARQ-160+ AUDITORIA (sugerido, mas não confirmado em análise parcial)

### 2.3 Usuários e Perfis

Não documentado explicitamente. Inferido de código e RN-2012:

- **Operador**: cadastra beneficiários, registra pagamentos (CADBENEF, REGPGTO)
- **Auditor**: consulta históricos, valida regras (CONSBENF, RELAUDIT)
- **Supervisor**: autoriza alterações críticas (CPF, dados bancários)
- **Sistema (batch)**: processamento automático em lote (BATCHPGT)

---

## 3. Principais Descobertas

### 3.1 Regras de Negócio Críticas

1. **BR-BEN-001**: VLR-BRUTO = VLR-BASE × FATOR-REGIONAL × FATOR-FAMILIAR × FATOR-RENDA (cascata multiplicativa, não aditiva)
2. **BR-COR-001**: Correção retroativa usa juros compostos (∏ IPCA mensais), NÃO juros simples
3. **BR-COR-003**: Flag IND-CORRIGIDO previne reprocessamento (idempotência crítica para auditoria)
4. **BR-DSC-001**: Limite 30% de desconto do VLR-BRUTO, EXCETO tipo J (judicial) que é ilimitado
5. **BR-DSC-004**: Desconto sindical é hardcoded 1% (não parametrizado)
6. **BR-GERAL-001**: Cascata CALCBENF → CALCCORR → CALCDSCT é obrigatória (ordem fixa, sem reversão)

### 3.2 Dependências Complexas

- **CALCBENF** depende de: ARQ-150 (beneficiário), ARQ-155 (programa), tabelas internas (27 regiões, 5 faixas renda, 5 escalas familiares)
- **CALCCORR** depende de: ARQ-160 (pagamento anterior), tabela IPCA (2010-2014, obsoleta pós-2014)
- **CALCDSCT** depende de: ARQ-150 (descontos PE), ARQ-160 (valor bruto), tabelas alíquotas (6 tipos de desconto)
- **Acoplamento forte**: nenhuma das três pode rodar isoladamente; resultado de uma é input da próxima

### 3.3 Dívida Técnica Identificada

- [x] **MYS-001**: 13º salário declarado mas não usado (código morto)
- [x] **MYS-002**: IPCA junho 2010 = 0.0000 (anomalia)
- [x] **MYS-003**: Tabela IPCA obsoleta pós-2014 (crítica para 2015+)
- [x] **MYS-004**: Plano Verão (1989-91) em comentário residual
- [x] **MYS-005**: Desconto judicial sem limite (risco de pagamento negativo)
- [x] **MYS-006**: Alíquota sindical hardcoded (não-parametrizado)
- [x] **MYS-007**: Ordem de descontos ambígua (risco de resultado não-determinístico)
- [x] **MYS-008**: IND-CORRIGIDO nunca é resetado (risco de inconsistência em reativações)
- [x] **MYS-009**: Limite dependentes é 3 ou 5? (documentação conflitante)

---

## 4. Mistérios e Gaps

**3 bloqueadores críticos** (precisam resolução antes de S2):

| Mistério                         | Impacto                                                                                                     | Decisão Necessária                                                                                                                      |
| -------------------------------- | ----------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------- |
| **MYS-001 (13º)**                | Se não replicarmos 13º, pagamentos históricos desaparecem. Se replicarmos sem entender lógica, erro.        | PO (Par 1) deve confirmar: "13º é parte do MVP modernizado?" Se sim, prioridade. Se não, marcar [GREENFIELD].                           |
| **MYS-003 (IPCA pós-2014)**      | Beneficiários processados 2015+ podem receber subpagamento (IPCA não atualiza).                             | DBA (Par 4) + PO (Par 1) devem: confirmar se há dados IPCA pós-2014 (IPEADATA? API?). Se não, como modernização lida com gap histórico? |
| **MYS-005 (judicial ilimitado)** | Desconto judicial pode fazer pagamento ficar negativo (beneficiário paga ao governo?). Legalmente possível? | PO (Par 1) deve confirmar: "Judicial pode ser ilimitado? Pode fazer pagamento negativo?" Se sim, documentar política.                   |

**4 mistérios não-bloqueadores** (investigação contínua):

- MYS-002, MYS-004, MYS-006, MYS-007, MYS-008, MYS-009: precisam documentação mas não impedem S2

---

## 5. Recomendações

1. **Bounded contexts para S2 (Estágio 2)**:
   - **BenefitCalculation**: CALCBENF + elegibilidade (regras de renda, região, dependentes)
   - **PaymentCorrection**: CALCCORR + IPCA data source (resolva MYS-003 antes)
   - **DiscountApplication**: CALCDSCT + desconto rules engine (suporte multi-tipo, policy validation)
   - **Audit**: ARQ-160 + immutable event log (IND-CORRIGIDO → event sourcing?)

2. **Decisões urgentes** (resolver com Par 1 em 14:00 Passagem #1):
   - 13º: incluir ou não? (MYS-001)
   - IPCA: qual a fonte 2015+? (MYS-003)
   - Judicial: confirmar limite ou não? (MYS-005)

3. **Modernização segura (S3)**:
   - Parametrizar tabelas (27 regiões, 5 faixas renda, alíquotas) em BD (não hardcode)
   - Implementar IPCA como serviço externo (IPEADATA ou manual import)
   - Criar testes de equivalência legado vs. novo para valores-teste históricos
   - Manter IND-CORRIGIDO como immutable flag (event sourcing)

---

## 6. Métricas

| Métrica                               | Valor               | Saúde                                                     |
| ------------------------------------- | ------------------- | --------------------------------------------------------- |
| Programas analisados (Par 3 de 5)     | 3/15 (20%)          | ⚠️ Foco em críticos; outros pares cobrem resto            |
| Regras de negócio extraídas           | 17 (vs. meta 15)    | ✅ Acima de meta                                          |
| Mistérios identificados               | 9 (vs. meta 5)      | ✅ Acima de meta                                          |
| Termos glossário                      | 34 (vs. meta 30)    | ✅ Acima de meta                                          |
| Rastreabilidade 100% (source_legacy:) | 100%                | ✅ Todas regras/mistérios apontam para arquivo.NSN#linhas |
| Bloqueadores críticos                 | 3 (MYS-001/003/005) | 🚨 Requerem resolução em Passagem #1                      |

---

## 7. Handoff Checklist

**Para Par 2 (Arquitetura) — Estágio 2**:

- [x] Glossário: 34 termos (100% sourced)
- [x] Regras de negócio: 17 catalogadas (100% com Programa Fonte)
- [x] Mistérios: 9 documentados (3 críticos escalados)
- [x] Dependency map: cascata BATCHPGT → CALCBENF → CALCCORR → CALCDSCT → ARQ-160
- [x] DDM mapping: ARQ-150/155/160 com campos mapeados
- [x] Artefatos prontos para transformation em EARS + ADRs
- [ ] **Bloqueadores escalados**: MYS-001/003/005 — aguardando decisão PO/DBA/Legal

**Próximos passos imediatos** (13:50 validação hard gate):

1. Validar 100% Programa Fonte preenchido em BR catalog
2. Confirmar 5+ mistérios com evidência arquivo+linha
3. Escalar MYS-001/003/005 para Par 1 com pauta de decisão
4. Commit com mensagem rastreável: "Hard gate PASSED ✅"

### 3.4 Gaps de Documentação

> O que a documentação existente NÃO cobre?

[Descreva]

---

## 4. Mistérios e Riscos

### 4.1 Mistérios Não Resolvidos

> Resuma os mistérios do arquivo `mysteries-found.md` que permanecem sem explicação.

| ID  | Descrição | Risco para Migração |
| --- | --------- | ------------------- |
|     |           |                     |

### 4.2 Riscos para o Estágio 2

> O que o time de especificação precisa saber antes de começar?

1. [Risco 1]
2. [Risco 2]
3. [Risco 3]

---

## 5. Recomendações

### 5.1 O que migrar primeiro

> Com base na priorização do Par 1 (Product Owner), quais funcionalidades devem ser migradas primeiro?

| Prioridade | Funcionalidade | Justificativa |
| ---------- | -------------- | ------------- |
| 1          |                |               |
| 2          |                |               |
| 3          |                |               |

### 5.2 O que descartar

> Funcionalidades que provavelmente não precisam ser migradas:

- [Funcionalidade]: [Motivo para descartar]

### 5.3 O que evoluir

> Funcionalidades que devem ser migradas E melhoradas:

- [Funcionalidade]: [Como melhorar]

---

## 6. Métricas do Estágio

| Métrica                       | Valor        |
| ----------------------------- | ------------ |
| Programas analisados          | \_\_\_ / 15  |
| DDMs mapeados                 | \_\_\_ / 4   |
| Regras de negócio encontradas | \_\_\_       |
| Regras escondidas encontradas | \_\_\_ / 10  |
| Easter eggs encontrados       | \_\_\_ / 3   |
| Termos no glossário           | \_\_\_       |
| Mistérios catalogados         | \_\_\_       |
| Tempo total gasto             | \_\_\_ horas |

---

## 7. Notas para o Próximo Estágio

> Deixe aqui mensagens para o time no Estágio 2 (Especificação Moderna):

[Escreva aqui]

---

## Definição de Pronto deste relatório

- [ ] Todas as seções acima preenchidas (sem placeholders).
- [ ] Pelo menos 5 regras críticas listadas em §3.1, cada uma referenciando uma `BR-XXX` do catálogo.
- [ ] Decisões de migrar/descartar/evoluir em §5 cobrem as 8+ funcionalidades principais.
- [ ] Métricas de §6 conferem com os outros artefatos (glossary.md, business-rules-catalog.md, mysteries-found.md).

— Paula

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-found.md"><strong>mysteries-found.md</strong></a><br/>
<sub>Lista de mistérios.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../02-spec-moderna/GUIDE.md"><strong>Estágio 2 — Spec</strong></a><br/>
<sub>Próximo estágio: spec moderna.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
