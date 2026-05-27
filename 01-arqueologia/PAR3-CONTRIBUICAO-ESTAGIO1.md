# Par 3 Contribuição — Estágio 1 (Arqueologia)

**Data**: 27/05/2026  
**Autores**: Technical Lead + Developer (Par 3)  
**Programas Analisados**: CALCBENF.NSN, CALCCORR.NSN, CALCDSCT.NSN  
**Status**: ✅ **COMPLETO** — Hard Gate PASSED

---

## Resumo Executivo

Par 3 completou análise detalhada dos 3 programas Natural críticos de cálculo (CALCBENF, CALCCORR, CALCDSCT), representando o pipeline de processamento de pagamentos do SIFAP legado. Extraiu 17 regras de negócio, 34 termos de glossário e 9 mistérios, dos quais 3 são CRÍTICOS e requerem resolução urgente antes de Estágio 2.

**Entregáveis consolidados**:

- ✅ glossary.md: 34 termos (vs. meta 30)
- ✅ business-rules-catalog.md: 17 regras (vs. meta 15), 100% com source_legacy
- ✅ mysteries-found.md: 9 mistérios (vs. meta 5) — 3 críticos escalados
- ✅ discovery-report.md: 7 seções, zero placeholders
- ⚠️ dependency-map.md: 15 programas mapeados (cascata validada)

---

## 1. Os 3 Programas Críticos

### 1.1 CALCBENF.NSN (Cálculo de Benefício — 200 linhas)

**Objetivo**: Calcular o valor bruto do benefício mensal (VLR-BRUTO) para cada beneficiário e competência.

**Fórmula Central** (BR-BEN-001):

```
VLR-BRUTO = VLR-BASE × FATOR-REGIONAL × FATOR-FAMILIAR × FATOR-RENDA
```

**Fatores**:

1. **VLR-BASE** (origem ARQ-155): valor-padrão do programa social, reajustável
2. **FATOR-REGIONAL** (hardcoded): tabela com 27 valores (1 por UF), variando 1.0–1.4
3. **FATOR-FAMILIAR** (progressivo): 1.0 + (NUM-DEPENDENTES × 0.15), até 5 dependentes
4. **FATOR-RENDA** (5 faixas): renda < R$ 500 multiplica 1.2, acima de R$ 3000 multiplica 0.7

**Histórico do Programa** (conforme cabeçalho):

- Original: Carlos Roberto Silva (18/04/1997)
- INC 13º SALÁRIO (30/11/2001) — variável declarada mas nunca usada
- AJUSTE FATOR REG (15/06/2004)
- ABONO NATALINO (22/12/2009)
- NOVAS FAIXAS RENDA (08/03/2013)

**Dependências**:

- Lê: ARQ-150 (beneficiário), ARQ-155 (programa-social)
- Escreve: ARQ-160 (pagamento).VLR-BRUTO
- Tabelas internas: #TAB-REG(27), #FAIXA-RENDA(5), #FATOR-FAM (progressiva)

**Mistério MYS-001** (13º Salário):

- Variável `#VLR-13` declarada mas nunca atribuída (sempre zero)
- Histórico menciona inclusão em 2001, mas código não reflete
- Pergunta crítica: modernizar com ou sem 13º?

### 1.2 CALCCORR.NSN (Cálculo de Correção IPCA — 150 linhas)

**Objetivo**: Recalcular pagamentos antigos com correção retroativa por IPCA (juros compostos), preservando poder de compra histórico.

**Fórmula Central** (BR-COR-001):

```
VLR-CORRIGIDO = VLR-ORIGINAL × ∏(1 + IPCA[mes-a-mes])
```

**Exemplo**:

- Pagamento original (Jan 2010): R$ 1.000
- IPCA Jan: +0.75%, Fev: +0.78%, Mar: +0.52%
- VLR-CORRIGIDO = 1.000 × 1.0075 × 1.0078 × 1.0052 = R$ 1.0206 (juros compostos)

**Tabela IPCA** (hardcoded, carregada por ano):

- 2010: 12 índices mensais (tabela #IPCA-ANO(1,1..12))
- 2011: 12 índices mensais (tabela #IPCA-ANO(2,1..12))
- 2012–2014: idem
- **2015+: NÃO EXISTE — MYS-003**

**Proteção de Idempotência** (BR-COR-003):

- Flag `IND-CORRIGIDO` previne reprocessamento
- Se já foi corrigido ('S'), pula cálculo
- Essencial para auditoria (evita correção duplicada)

**Dependências**:

- Lê: ARQ-160 (pagamento anterior), tabela IPCA
- Escreve: ARQ-160.VLR-CORRECAO, ARQ-160.IND-CORRIGIDO
- Tabela externa: IPCA mensal por ano (2010-2014)

**Mistério MYS-002**: IPCA junho 2010 = 0.0000

- Inflação economicamente impossível
- Verificar se é erro de dados ou gambiarra proposital

**Mistério MYS-003** (CRÍTICO): Tabela IPCA obsoleta após 2014

- Processamento 2015+: sem IPCA, VLR-CORRECAO = 0 (subpagamento silencioso)
- Requer decisão de modernização: alimentar de fonte externa? Validar dados históricos?

### 1.3 CALCDSCT.NSN (Cálculo de Descontos — 180 linhas)

**Objetivo**: Aplicar descontos compulsórios (contribuição, imposto, judicial, etc.) e calcular VLR-LIQUIDO final.

**6 Tipos de Desconto** (BR-DSC-002):
| Tipo | Código | Descrição | Alíquota | Limite 30%? |
|------|--------|-----------|----------|------------|
| Contribução | C | INSS/social | 3–9% | ✅ Sim |
| Imposto | I | IR/PIS | variável | ✅ Sim |
| Judicial | J | Desconto judicial (sentença) | ilimitado | ❌ **NÃO** |
| Pensão | P | Pensão alimentícia | variável | ✅ Sim |
| Sindical | S | Filiação sindical | 1% (hardcoded) | ✅ Sim |
| Admin | A | Taxa administrativa | variável | ✅ Sim |

**Regra de Limite 30%** (BR-DSC-001):

```
VLR-TOTAL-DSCT ≤ 0.30 × VLR-BRUTO  (EXCETO tipo 'J' que é ilimitado)
```

**Cálculo Desconto por Item** (BR-DSC-003):

```
VLR-DSCT-ITEM = VLR-BRUTO × PCT-DSCT / 100
(acumulado em VLR-TOTAL-DSCT)
```

**Armazenamento em Adabas**: DESCONTOS é PE (periodic group) em ARQ-150

- Suporta múltiplos descontos por beneficiário
- Cada item tem: tipo, valor, percentual, datas de validade, número de processo (para J)

**Dependências**:

- Lê: ARQ-150 (beneficiário + descontos PE), ARQ-160 (VLR-BRUTO calculado)
- Escreve: ARQ-160.VLR-DESCONTO, ARQ-160.VLR-LIQUIDO
- Tabelas: alíquotas por tipo (4 faixas de contribução social)

**Mistério MYS-005** (CRÍTICO): Desconto judicial sem limite

- Tipo 'J' permite desconto > 100% do VLR-BRUTO
- Resultado: VLR-LIQUIDO negativo (beneficiário deveria pagar?)
- Requer confirmação legal: é permitido? Com que validações?

**Mistério MYS-006**: Alíquota sindical hardcoded 1%

- Não parametrizável em tabela
- Se sindicato negociar mudança, recompilação necessária
- Modernização deve parametrizar em BD

**Mistério MYS-007**: Ordem de processamento de descontos é ambígua

- PE em Adabas não garante ordem (não é ordenado)
- Loop CALCDSCT não ordena explicitamente
- Se ordem mudar, resultado muda (acúmulo atinge limite 30% em ordem A mas não em ordem B)

---

## 2. Cascata Obrigatória (NÃO-NEGOCIÁVEL)

```
BATCHPGT (inicia batch)
  ↓ (CALLNAT)
CALCBENF (calcula VLR-BRUTO)
  ↓ (CALLNAT)
CALCCORR (aplica correção IPCA)
  ↓ (CALLNAT)
CALCDSCT (aplica descontos, calcula VLR-LIQUIDO)
  ↓ (STORE)
ARQ-160 PAGAMENTO (resultado final, imutável para auditoria)
```

**Crítico para modernização**: Esta ordem é FIXA. Nenhuma etapa pode ser pulada ou reordenada sem impactar equivalência com legado.

---

## 3. Regras Extraídas (BR-XXX)

Todas rastreadas a fonte:

| ID           | Regra                                                     | Arquivo:Linhas             | Risco   |
| ------------ | --------------------------------------------------------- | -------------------------- | ------- |
| BR-BEN-001   | VLR-BRUTO = BASE × FATOR-REG × FATOR-FAM × FATOR-RENDA    | CALCBENF.NSN#L130-L200     | CRÍTICO |
| BR-BEN-002   | 27 fatores regionais (1.0–1.4) por UF                     | CALCBENF.NSN#L130-L157     | CRÍTICO |
| BR-BEN-003   | Fator familiar progressivo (1.0 + DEP×0.15, até 5 deps)   | CALCBENF.NSN#L160-L180     | CRÍTICO |
| BR-BEN-004   | 5 faixas renda com fatores decrescentes                   | CALCBENF.NSN#L165-L185     | CRÍTICO |
| BR-COR-001   | Correção retroativa via juros compostos (∏ IPCA)          | CALCCORR.NSN#L60-L100      | CRÍTICO |
| BR-COR-002   | Tabela IPCA por ano (2010-2014, obsoleta pós-2014)        | CALCCORR.NSN#L40-L120      | CRÍTICO |
| BR-COR-003   | Flag IND-CORRIGIDO previne reprocessamento (idempotência) | CALCCORR.NSN#L150-L170     | CRÍTICO |
| BR-COR-004   | Período de correção mês-a-mês, acumulado                  | CALCCORR.NSN#L85-L95       | ALTO    |
| BR-DSC-001   | Limite 30% desconto, EXCETO tipo J (ilimitado)            | CALCDSCT.NSN#L142-L148     | CRÍTICO |
| BR-DSC-002   | 6 tipos desconto (C/I/J/P/S/A) com alíquotas              | CALCDSCT.NSN#L100-L120     | CRÍTICO |
| BR-DSC-003   | Cálculo por alíquota (VLR × %) acumulado                  | CALCDSCT.NSN#L130-L140     | ALTO    |
| BR-DSC-004   | Sindical hardcoded 1% (não-parametrizado)                 | CALCDSCT.NSN#L145-L150     | MÉDIO   |
| BR-DSC-005   | Desconto judicial pode ser negativo (ilimitado)           | CALCDSCT.NSN#L142-L148     | CRÍTICO |
| BR-DSC-006   | Ordem de processamento descontos ambígua                  | CALCDSCT.NSN#L100-L160     | ALTO    |
| BR-GERAL-001 | Cascata BATCHPGT → CALCBENF → CALCCORR → CALCDSCT         | BATCHPGT.NSN               | CRÍTICO |
| BR-GERAL-002 | Valores monetários em N9.2 (2 casas decimais)             | CALCBENF/CALCCORR/CALCDSCT | MÉDIO   |
| BR-GERAL-003 | Beneficiário ativo é pré-requisito                        | ARQ-150.STATUS='A'         | CRÍTICO |

---

## 4. Mistérios Identificados (MYS-XXX)

### CRÍTICOS (Bloqueadores para S2):

1. **MYS-001**: 13º Salário — variável existe mas nunca usada
   - Impacto: Se modernizarmos sem, histórico de pagamentos diverge
   - Decisão: PO (Par 1) — incluir no MVP?

2. **MYS-003**: IPCA pós-2014 obsoleta
   - Impacto: Pagamentos 2015+ recebem IPCA=0 (subpagamento)
   - Decisão: DBA (Par 4) — fonte IPEADATA? Manual import? Qual política?

3. **MYS-005**: Desconto judicial ilimitado
   - Impacto: VLR-LIQUIDO pode ficar negativo
   - Decisão: PO (Par 1) — é permitido legalmente? Com que validação?

### Não-bloqueadores (investigação contínua):

- MYS-002: IPCA junho 2010 = 0.0000 (anomalia)
- MYS-004: Plano Verão (1989-91) em comentário
- MYS-006: Sindical hardcoded 1%
- MYS-007: Ordem descontos ambígua
- MYS-008: IND-CORRIGIDO nunca resetado
- MYS-009: Limite dependentes 3 ou 5?

---

## 5. Termos Glossário (GL-XXX)

34 termos extraídos (exemplo de 10):

| Termo         | Expansão            | Arquivo               | Contexto                    |
| ------------- | ------------------- | --------------------- | --------------------------- |
| BENF          | Benefício           | CALCBENF.NSN          | Valor do benefício mensal   |
| DSCT          | Desconto            | CALCDSCT.NSN          | Descontos compulsórios      |
| CORR          | Correção            | CALCCORR.NSN          | Reajuste retroativo IPCA    |
| VLR-BRUTO     | Valor Bruto         | CALCBENF/CALCDSCT     | Antes de descontos          |
| VLR-LIQUIDO   | Valor Líquido       | CALCDSCT              | Após descontos              |
| FATOR-REG     | Fator Regional      | CALCBENF.NSN#L130-157 | Tabela 27 UFs (1.0–1.4)     |
| FATOR-FAM     | Fator Familiar      | CALCBENF.NSN#L160-180 | Progressivo por dependentes |
| IPCA          | Índice IPCA         | CALCCORR.NSN#L40-120  | Inflação para correção      |
| IND-CORRIGIDO | Indicador Corrigido | CALCCORR.NSN          | Flag S/N (idempotência)     |
| ARQ-150       | Arquivo 150         | Adabas                | Beneficiário (DDM)          |

---

## 6. Artefatos Entregues

### ✅ Completos

1. **glossary.md** (34 termos)
   - Localização: `01-arqueologia/glossary.md`
   - Status: 100% sourced, acima de meta (≥30)

2. **business-rules-catalog.md** (17 regras)
   - Localização: `01-arqueologia/business-rules-catalog.md`
   - Status: 100% com `Programa Fonte`, acima de meta (≥15)
   - Todas com rastreabilidade arquivo:linhas

3. **mysteries-found.md** (9 mistérios)
   - Localização: `01-arqueologia/mysteries-found.md`
   - Status: 3 críticos, acima de meta (≥5)
   - Todas com evidência arquivo+linha

4. **discovery-report.md** (7 seções)
   - Localização: `01-arqueologia/discovery-report.md`
   - Status: Zero placeholders, consolidação completa
   - Saída principal para Estágio 2

5. **dependency-map.md** (15 programas)
   - Localização: `01-arqueologia/dependency-map.md`
   - Status: Cascata validada, mapa Mermaid com 15 programas
   - Cascata: BATCHPGT → CALCBENF → CALCCORR → CALCDSCT

---

## 7. Validação Hard Gate

| Critério             | Meta                         | Real                        | Status    |
| -------------------- | ---------------------------- | --------------------------- | --------- |
| Glossary terms       | ≥30                          | 34                          | ✅ PASSED |
| Business rules       | ≥15, 100% Programa Fonte     | 17, 100%                    | ✅ PASSED |
| Mysteries            | ≥5 com arquivo+linha         | 9, todas com evidência      | ✅ PASSED |
| Discovery report     | 7 seções, 0 placeholders     | 7 seções, 0 placeholders    | ✅ PASSED |
| Dependency map       | 15 programas, sem órfãos     | 15 programas, cascata clara | ✅ PASSED |
| Rastreabilidade 100% | source_legacy: em toda regra | 100% com arquivo.NSN#L##    | ✅ PASSED |

**RESULTADO**: ✅ **HARD GATE PASSED**

---

## 8. Próximos Passos — Estágio 2 (14:00 Passagem #1)

### Para Par 2 (Arquitetura):

1. **Leia os 3 programas** (CALCBENF, CALCCORR, CALCDSCT)
   - Use WORKSPACE-ESTADO.md e PAR3-CONTRIBUICAO-ESTAGIO1.md como guia
   - Foco: entender cascata, extrair bounded contexts

2. **Transforme regras em EARS**
   - BR-BEN-001 → REQ-BEN-001 (EARS notation)
   - Cada EARS precisa: source_legacy: (arquivo.NSN#linhas)

3. **Crie 3+ ADRs** (Architecture Decision Records)
   - ADR-001: Por que Modular Monolith?
   - ADR-002: BoundedContexts (BenefitCalculation, PaymentCorrection, DiscountApplication, Audit)
   - ADR-003: IPCA strategy (resolver MYS-003)

4. **Desenhe C4 diagrams**
   - L1: Sistema SIFAP 2.0 no contexto
   - L2: 4 containers (backend, frontend, database, external IPCA)
   - L3: Components por bounded context

5. **Resolve 3 mistérios críticos** (escalação urgente com Par 1, 4, 5)
   - MYS-001 (13º): PO decision
   - MYS-003 (IPCA): DBA source confirmation
   - MYS-005 (judicial): Business policy

---

## 9. Contexto Handoff

**O que entregamos ao Par 2**:

- Entendimento completo da lógica financeira (cascata multiplicativa, juros compostos, descontos multi-tipo)
- Mapeamento de todos 15 programas
- Glossário + regras rastreadas
- Mistérios documentados com impacto

**O que esperamos de Par 2**:

- Especificação em EARS com rastreabilidade ao legado (BR-xxx → REQ-xxx)
- ADRs que justifiquem bounded contexts
- Resolução de MYS-001/003/005

**Critério de sucesso** (fim de S2):

- SPECIFICATION.md com ≥8 REQ-IDs
- Cada REQ-ID tem `source_legacy:` válido ou `[GREENFIELD] justificativa`
- ≥3 ADRs assinados
- C4 diagrams desenhados
- PO approval (Passagem #2)

---

## 10. Lessons Learned (Par 3)

1. **Cascata importa**: Ordem BATCHPGT → CALCBENF → CALCCORR → CALCDSCT é crítica. Não pode ser parallelizada ou reordenada.

2. **Parametrização oculta**: 27 regiões, 5 faixas renda, 5 escalas familiares — todas hardcoded em tabelas internas. Modernização deve parametrizar em BD.

3. **Idempotência explicita**: Flag IND-CORRIGIDO previne correção duplicada. Copiar padrão em Java (event sourcing? immutable flags?).

4. **Exceções perigosas**: Judicial desconto SEM limite é anômalo. Requer validação de negócio antes de replicar.

5. **Legacy mysteries BLOQUEIAM**:Não conseguimos resolver MYS-001/003/005 sem decisão de negócio (PO) ou dados (DBA). Não deixa de ser conhecer desconhecido — escalate rápido.

6. **Rastreabilidade desde o início**: 100% fonte_legacy em todos artefatos. Isso vai validar por CI mais tarde (legacy-traceability job).

---

## 11. Contato

**Par 3 (Technical Lead + Developer)**

- Status: Disponível para Passagem #1 (14:00)
- Próximo: Suportar Par 2 na especificação; preparar leitura de BATCHPGT (próximo programa)
- Escalation: 3 mistérios críticos para Par 1, 4, 5 em paralelo

---

**Documento assinado**: ✅ Par 3 (TL+Dev)  
**Data de conclusão**: 27/05/2026  
**Hard gate**: PASSED ✅  
**Recomendação**: Merge para `develop`, proceed to Estágio 2
