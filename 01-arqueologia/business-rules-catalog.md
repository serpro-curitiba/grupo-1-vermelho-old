<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Catálogo de Regras de Negócio — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **business-rules-catalog**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).

> Registre aqui todas as regras de negócio extraídas do código Natural/Adabas.
> Cada regra precisa ter rastreabilidade até o código-fonte.
>
> **REGRA DURA:** linhas com `Programa Fonte` vazio são **inválidas** e não contam para o gate do Estágio 2. Use o formato `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>` sempre que possível. Mínimo aceito: nome do arquivo .NSN.

## Como pensar em "regra de negócio"

O que conta:

- Um `IF` que decide algo no domínio (ex.: _"se a UF é do Nordeste e o programa é Seca, valor base × 1.2"_)
- Uma constante numérica sem explicação (ex.: `0.075` num cálculo de imposto)
- Uma transição de status com regra (ex.: _"só de A para S, nunca de I para A"_)
- Um tratamento especial para um caso (ex.: _"se o CPF começa com 999, é teste"_)

O que NÃO conta: paginação de relatório, formatação de saída, manipulação de cursor Adabas, abertura de arquivo. Ignore esses detalhes de implementação.

## Níveis de Risco

| Nível       | Descrição                                                     |
| ----------- | ------------------------------------------------------------- |
| **CRÍTICO** | Regra financeira ou de segurança — erro causa prejuízo direto |
| **ALTO**    | Regra de negócio central — afeta fluxo principal              |
| **MÉDIO**   | Regra de validação ou formatação — afeta qualidade dos dados  |
| **BAIXO**   | Regra de apresentação ou conveniência — impacto limitado      |

## Regras Encontradas

> **Catálogo consolidado pós-merge (2026-05-27).** Reúne contribuições de TODOS os pares cobrindo os 15 programas Natural do legado SIFAP:
>
> | Faixa de IDs      | Programa(s)                | Par responsável | Origem                                                |
> | ----------------- | -------------------------- | --------------- | ----------------------------------------------------- |
> | BR-001..BR-021    | VALBENEF, VALDOCS, VALELEG | Pares 1 + 4     | Validação de beneficiário, documentos e elegibilidade |
> | BR-BEN-001..008   | CALCBENF                   | Par 3           | Cálculo do valor bruto do benefício                   |
> | BR-COR-001..006   | CALCCORR                   | Par 3           | Correção monetária (IPCA)                             |
> | BR-DSC-001..008   | CALCDSCT                   | Par 3           | Aplicação de descontos                                |
> | BR-PGT-001..016   | BATCHPGT                   | Par 2           | Orquestrador do ciclo de pagamento                    |
> | BR-CON-001..012   | BATCHCON                   | Par 2           | Conciliação de retorno bancário (CNAB 240)            |
> | BR-REL-001..007   | BATCHREL                   | Par 2           | Relatórios batch agregados                            |
> | BR-CAD-001..011   | CADBENEF                   | Par 1           | Cadastro de beneficiário                              |
> | BR-DEP-001..007   | CADDEPEND                  | Par 1           | Cadastro de dependentes                               |
> | BR-PRG-001..008   | CADPROG                    | Par 1           | Cadastro de programa social                           |
> | BR-CSL-001..006   | CONSBENF                   | Par 1           | Consulta interativa de beneficiário                   |
> | BR-AUD-001..009   | RELAUDIT                   | Par 5           | Relatório de auditoria                                |
> | BR-RPG-001..008   | RELPGT                     | Par 5           | Relatório de pagamentos                               |
> | BR-GERAL-001..004 | transversal                | Par 3 + Par 2   | Cascata, formato monetário, status                    |

### VALBENEF / VALDOCS / VALELEG — Validações (BR-001..BR-021)

> Pares 1 + 4. Validação de beneficiário, documentos e elegibilidade. Numeração legada preservada por compatibilidade com [`test-scenarios.md`](test-scenarios.md), [`mysteries-found.md`](mysteries-found.md) e specs do Estágio 2.

| ID     | Regra de Negócio                                                                                                         | Programa Fonte                                                                  | Campos DDM                                                                                                          | Nível de Risco | Notas                                     |
| ------ | ------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------- | -------------- | ----------------------------------------- |
| BR-001 | CPF deve ser válido pelo algoritmo mod-11 (dois dígitos verificadores)                                                   | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L165-L220`           | `BENEFICIARIO.CPF`                                                                                                  | CRÍTICO        | Mesma lógica replicada em VALDOCS.NSN     |
| BR-002 | CPF com todos os dígitos iguais é inválido, EXCETO quando começa com `000` (exceção "teste governo")                     | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L178-L188`           | `BENEFICIARIO.CPF`                                                                                                  | CRÍTICO        | Regra ESCONDIDA — back door               |
| BR-003 | CPF com prefixos especiais (`000`, `001`, `002`, `010`, `011`, `099`, `100`, `999`) bypassa toda validação de documentos | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L150-L165`            | `BENEFICIARIO.CPF`                                                                                                  | CRÍTICO        | Regra ESCONDIDA — back door governo/teste |
| BR-004 | CPF zero é inválido em validação de documentos                                                                           | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L92-L96`              | `BENEFICIARIO.CPF`                                                                                                  | ALTO           |                                           |
| BR-005 | Data de nascimento deve ter ano entre 1900 e ano atual                                                                   | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L233-L237`           | `BENEFICIARIO.DT-NASCIMENTO`                                                                                        | ALTO           |                                           |
| BR-006 | Data de nascimento deve ter mês 1–12 e dia válido por mês; fevereiro aceita até 29 sempre (não checa bissexto)           | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L88-L102, L237-L244` | `BENEFICIARIO.DT-NASCIMENTO`                                                                                        | ALTO           | BUG histórico — fev 29 sempre válido      |
| BR-007 | Nome deve ser não vazio e conter ao menos um espaço (nome + sobrenome)                                                   | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L249-L262`           | `BENEFICIARIO.NOME`                                                                                                 | MÉDIO          |                                           |
| BR-008 | UF deve ser uma das 27 siglas válidas quando preenchida                                                                  | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L57-L83, L132-L148`  | `BENEFICIARIO.UF`                                                                                                   | MÉDIO          |                                           |
| BR-009 | Status de beneficiário só pode ser A, S, C, I ou D                                                                       | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L150-L156`           | `BENEFICIARIO.STATUS`                                                                                               | ALTO           |                                           |
| BR-010 | RG deve ter ao menos 5 caracteres não vazios                                                                             | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L130-L143`            | `BENEFICIARIO.RG`                                                                                                   | MÉDIO          |                                           |
| BR-011 | Para elegibilidade, beneficiário deve estar com STATUS = `A`; S, C, D e I bloqueiam                                      | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L100-L120`            | `BENEFICIARIO.STATUS`                                                                                               | CRÍTICO        | Bloqueio explícito por status             |
| BR-012 | Programa deve existir e estar com STATUS-PROG = `A`; senão rejeita                                                       | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L76-L88`              | `PROGRAMA-SOCIAL.STATUS-PROG`                                                                                       | ALTO           |                                           |
| BR-013 | Região 99 (internacional/diplomático) bypassa todas as regras de elegibilidade                                           | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L90-L96`              | `BENEFICIARIO.COD-REGIAO`                                                                                           | CRÍTICO        | Regra ESCONDIDA — exceção total           |
| BR-014 | Idade do beneficiário deve respeitar IDADE-MIN e IDADE-MAX do programa quando > 0                                        | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L124-L138`            | `BENEFICIARIO.DT-NASCIMENTO`, `PROGRAMA-SOCIAL.IDADE-MIN/MAX`                                                       | ALTO           |                                           |
| BR-015 | Renda familiar não pode exceder RENDA-MAX do programa quando > 0                                                         | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L141-L148`            | `BENEFICIARIO.RENDA-FAMILIAR`, `PROGRAMA-SOCIAL.RENDA-MAX`                                                          | CRÍTICO        | Regra financeira                          |
| BR-016 | Programa tipo A (assistencial): renda > 600 sem dependentes rejeita; exige DOCUMENTOS-OK = `S`                           | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L152-L168`            | `PROGRAMA-SOCIAL.TIPO`, `BENEFICIARIO.RENDA-FAMILIAR`, `BENEFICIARIO.NUM-DEPENDENTES`, `BENEFICIARIO.DOCUMENTOS-OK` | ALTO           | Limite mágico `600.00`                    |
| BR-017 | Programa tipo P (previdenciário): idade < 60 rejeita                                                                     | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L170-L176`            | `PROGRAMA-SOCIAL.TIPO`, `BENEFICIARIO.DT-NASCIMENTO`                                                                | ALTO           |                                           |
| BR-018 | Programa tipo T (trabalho): idade fora da faixa 16–65 rejeita                                                            | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L178-L184`            | `PROGRAMA-SOCIAL.TIPO`, `BENEFICIARIO.DT-NASCIMENTO`                                                                | ALTO           |                                           |
| BR-019 | Tipo de programa desconhecido (≠ A, P, T) rejeita elegibilidade                                                          | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L186-L189`            | `PROGRAMA-SOCIAL.TIPO`                                                                                              | MÉDIO          |                                           |
| BR-020 | COD-ELEGIBILIDADE iniciando com `R` exige NIS válido (≠ 0)                                                               | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L210-L218`            | `PROGRAMA-SOCIAL.COD-ELEGIBILIDADE`, `BENEFICIARIO.NIS`                                                             | ALTO           |                                           |
| BR-021 | COD-ELEGIBILIDADE com `D` na posição 2 exige ao menos 1 dependente                                                       | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L220-L227`            | `PROGRAMA-SOCIAL.COD-ELEGIBILIDADE`, `BENEFICIARIO.NUM-DEPENDENTES`                                                 | ALTO           |                                           |

### CALCBENF — Cálculo do Valor Bruto do Benefício (BR-BEN-001..008)

> Par 3. Recuperado do branch `develop-implementacao` (commit `18403f0`). Cascata: `BATCHPGT → CALCBENF → CALCCORR → CALCDSCT → ARQ-160`.

| ID         | Regra de Negócio                                                                                              | Programa Fonte                                                        | Campos DDM                                        | Nível de Risco | Notas                                                   |
| ---------- | ------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ------------------------------------------------- | -------------- | ------------------------------------------------------- |
| BR-BEN-001 | Cálculo do valor bruto: `VLR-BRUTO = VLR-BASE × FATOR-REGIONAL × FATOR-FAMILIAR × FATOR-RENDA`                | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L100-L128` | `PROGRAMA-SOCIAL.VLR-BASE`, `PAGAMENTO.VLR-BRUTO` | CRÍTICO        | Fórmula central — regra financeira                      |
| BR-BEN-002 | Fator regional tem 27 valores (1 por UF), variando de 1.0 a 1.4                                               | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L130-L157` | `BENEFICIARIO.COD-REGIAO`                         | CRÍTICO        | Tabela parametrizada hardcoded no programa              |
| BR-BEN-003 | Fator familiar (progressivo): `1.0 + (NUM-DEPENDENTES × 0.15)`, máximo 5 dependentes                          | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L180-L200` | `BENEFICIARIO.NUM-DEPENDENTES`                    | CRÍTICO        | Acima de 5 dependentes, fator congelado em 1.75         |
| BR-BEN-004 | Fator de renda em 5 faixas: <500=1.2 · 500–1000=1.0 · 1000–2000=0.9 · 2000–3000=0.8 · >3000=0.7               | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160-L180` | `BENEFICIARIO.RENDA-FAMILIAR`                     | CRÍTICO        | Faixas reduzem o benefício para renda alta              |
| BR-BEN-005 | Valor bruto não pode ser negativo (proteção de fronteira)                                                     | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L210-L215` | `PAGAMENTO.VLR-BRUTO`                             | ALTO           | Se cálculo retorna negativo → erro, log e abort         |
| BR-BEN-006 | Variável de 13º salário declarada mas sem lógica de uso                                                       | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L45-L48`   | `PAGAMENTO.VLR-13`                                | CRÍTICO        | **MYS-001** — campo existe na FDT mas nunca é atribuído |
| BR-BEN-007 | TIPO-PGTO assume `N` (normal) por default; `D` e `T` declarados mas nunca atribuídos em CALCBENF              | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L220-L230` | `PAGAMENTO.TIPO-PGTO`                             | ALTO           | **MYS-002** — lógica de tipos confusa                   |
| BR-BEN-008 | Status do beneficiário deve ser `A` (ativo) — `E` (excluído) e `S` (suspenso) são rejeitados antes do cálculo | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L80-L90`   | `BENEFICIARIO.STATUS`                             | CRÍTICO        | Pré-requisito da cascata                                |

### CALCCORR — Correção Monetária IPCA (BR-COR-001..006)

> Par 3. Recuperado do `develop-implementacao`. Idempotente via `IND-CORRIGIDO`.

| ID         | Regra de Negócio                                                                           | Programa Fonte                                                        | Campos DDM                                        | Nível de Risco | Notas                                                                              |
| ---------- | ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------- | ------------------------------------------------- | -------------- | ---------------------------------------------------------------------------------- |
| BR-COR-001 | Correção retroativa usa juros compostos: `VLR-CORRIGIDO = VLR-ORIGINAL × ∏(1 + IPCA[mês])` | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L80-L120`  | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-CORRECAO`   | CRÍTICO        | Produtório mensal                                                                  |
| BR-COR-002 | Tabela IPCA é carregada por ano, índices mensais (2010–2014); valor zero = sem correção    | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L50-L78`   | — (tabela interna)                                | CRÍTICO        | **MYS-003** — sem dados pós-2014, correções recentes ficam zeradas silenciosamente |
| BR-COR-003 | Flag `IND-CORRIGIDO` previne correção duplicada (idempotência): se `S`, pula cálculo       | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L130-L138` | `PAGAMENTO.IND-CORRIGIDO`                         | CRÍTICO        | Garantia de idempotência batch                                                     |
| BR-COR-004 | Período de correção definido por competência mês-a-mês; acumulação em `VLR-CORRECAO`       | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L140-L160` | `PAGAMENTO.COMPETENCIA`, `PAGAMENTO.VLR-CORRECAO` | ALTO           | Granularidade mensal                                                               |
| BR-COR-005 | Tabela "Plano Verão" (1989–1991) ainda presente comentada; nunca foi removida              | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L165-L185` | —                                                 | MÉDIO          | **MYS-004** — código morto histórico                                               |
| BR-COR-006 | Se índice IPCA é zero para um mês, correção desse mês = 1.0 (não interrompe)               | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L105-L110` | —                                                 | ALTO           | Risco: meses sem dados não são distinguidos de "deflação zero"                     |

### CALCDSCT — Aplicação de Descontos (BR-DSC-001..008)

> Par 3. Recuperado do `develop-implementacao`. Tem regras explosivas (judicial sem teto pode estourar o líquido).

| ID         | Regra de Negócio                                                                                                                     | Programa Fonte                                                        | Campos DDM                                                               | Nível de Risco | Notas                                                      |
| ---------- | ------------------------------------------------------------------------------------------------------------------------------------ | --------------------------------------------------------------------- | ------------------------------------------------------------------------ | -------------- | ---------------------------------------------------------- |
| BR-DSC-001 | Limite de desconto total = 30% do `VLR-BRUTO`, EXCETO tipo `J` (judicial) que NÃO tem limite                                         | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira central; exceção legal                    |
| BR-DSC-002 | Tipos de desconto: `C`=Contribuição (3–9%), `I`=Imposto, `J`=Judicial (ilimitado), `P`=Pensão, `S`=Sindical (1%), `A`=Administrativo | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L140` | `PAGAMENTO.TIPO-DSCT`                                                    | CRÍTICO        | 6 tipos                                                    |
| BR-DSC-003 | Cálculo por alíquota: `VLR-DSCT-ITEM = VLR-BRUTO × PCT-DSCT / 100`, acumulado em `VLR-TOTAL-DSCT`                                    | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L150-L165` | `PAGAMENTO.VLR-DSCT-ITEM`, `PAGAMENTO.VLR-TOTAL-DSCT`                    | CRÍTICO        | Aritmética padrão                                          |
| BR-DSC-004 | Desconto sindical é fixo 1% (hardcoded), aplica sempre que beneficiário é sindicalizado                                              | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L170-L178` | —                                                                        | ALTO           | **MYS-006** — não parametrizado                            |
| BR-DSC-005 | Descontos tipo `J` são ilimitados; pagamento líquido pode ficar negativo                                                             | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L180-L188` | `PAGAMENTO.VLR-LIQUIDO`                                                  | CRÍTICO        | **MYS-005** — sem política de tratamento                   |
| BR-DSC-006 | Ordem de processamento de descontos afeta resultado quando há teto; ordem é ambígua                                                  | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L150` | `PAGAMENTO.TIPO-DSCT`                                                    | ALTO           | **MYS-007** — ordem implícita não documentada              |
| BR-DSC-007 | Desconto tipo `I` (imposto) usa tabela progressiva externa (referência sem implementação)                                            | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L195-L200` | —                                                                        | MÉDIO          | Possível regra escondida — tabela imposto fora do programa |
| BR-DSC-008 | Cálculo do líquido: `VLR-LIQUIDO = VLR-BRUTO + VLR-CORRECAO - VLR-TOTAL-DSCT`                                                        | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L190-L195` | `PAGAMENTO.VLR-LIQUIDO`                                                  | CRÍTICO        | Saída final da cascata                                     |

### BATCHPGT — Orquestrador do Ciclo de Pagamento (BR-PGT-001..016)

> Par 2. Sem `CALLNAT` — acoplamento via DDMs compartilhados.

| ID         | Regra de Negócio                                                                                                     | Programa Fonte                                                        | Campos DDM                                    | Nível de Risco | Notas                                                                                                        |
| ---------- | -------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | --------------------------------------------- | -------------- | ------------------------------------------------------------------------------------------------------------ |
| BR-PGT-001 | Competência mensal calculada a partir da data de execução (`YYYYMM`)                                                 | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L140-L145` | `PAGAMENTO.COMPETENCIA`                       | CRÍTICO        | Base para ordenação e processamento downstream                                                               |
| BR-PGT-002 | Tabela de fatores regionais: 27 regiões (1.0000–1.4000); regiões 26–27 sempre 1.0000                                 | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L115-L140` | `BENEFICIARIO.COD-REGIAO`                     | CRÍTICO        | Duplicação aparente com tabela de CALCBENF (BR-BEN-002)                                                      |
| BR-PGT-003 | 5 faixas de renda com limites R$ 300/600/1000/1500/9999.99 e fatores 1.0/0.85/0.70/0.55/0.40                         | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L142-L148` | `BENEFICIARIO.RENDA-FAMILIAR`                 | CRÍTICO        | Divergente das faixas de CALCBENF (BR-BEN-004) — possível conflito                                           |
| BR-PGT-004 | Processamento obrigatoriamente ordenado por CPF                                                                      | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L214-L217` | `BENEFICIARIO.CPF`                            | CRÍTICO        | **REGRA ESCONDIDA** — comentário cita "compatibilidade com sistemas downstream"; downstream não identificado |
| BR-PGT-005 | Filtro de duplicatas: rejeita se CPF já foi processado neste lote                                                    | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L220-L225` | — (variável `#CPF-ANT`)                       | ALTO           | Depende da ordenação BR-PGT-004                                                                              |
| BR-PGT-006 | Status do beneficiário deve ser `A` (ativo); descarta se diferente                                                   | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L227-L231` | `BENEFICIARIO.STATUS`                         | CRÍTICO        | Coerente com BR-011, BR-BEN-008                                                                              |
| BR-PGT-007 | Não gera pagamento se já existe registro na mesma competência                                                        | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L233-L241` | `PAGAMENTO.COMPETENCIA`                       | CRÍTICO        | Idempotência do batch                                                                                        |
| BR-PGT-008 | Programa deve estar ativo (`STATUS-PROG = 'A'`); descarta se inativo ou inexistente                                  | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L249-L264` | `PROGRAMA-SOCIAL.STATUS-PROG`                 | CRÍTICO        | Coerente com BR-012                                                                                          |
| BR-PGT-009 | Fator familiar: 1.0 (0 dep), +0.05 até 2, +0.03 de 3–4, +0.02 acima de 5                                             | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L278-L289` | `BENEFICIARIO.NUM-DEPENDENTES`                | CRÍTICO        | Divergente de BR-BEN-003 (`+0.15` linear) — conflito real entre programas                                    |
| BR-PGT-010 | Fator idade: 1.15 (≥65), 1.10 (60–64), 1.05 (<18), 1.0 (18–59)                                                       | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L291-L300` | `BENEFICIARIO.DT-NASCIMENTO`                  | CRÍTICO        | Fator não existe em CALCBENF                                                                                 |
| BR-PGT-011 | 13º salário aplicado apenas em dezembro (`MES=12`) com `TIPO-PGTO='D'`; valor = `VLR-BASE × FATOR-REG × FATOR-IDADE` | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L302-L310` | `PAGAMENTO.VLR-13`, `PAGAMENTO.TIPO-PGTO`     | CRÍTICO        | Resolve parte de **MYS-001** — lógica está aqui, não em CALCBENF                                             |
| BR-PGT-012 | Abono de 15% para programas tipo `A` em dezembro: `VLR-ABONO = VLR-BENF × 0.15`                                      | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L310-L314` | `PAGAMENTO.VLR-ABONO`, `PROGRAMA-SOCIAL.TIPO` | ALTO           | Hardcoded 15%                                                                                                |
| BR-PGT-013 | Desconto de 3% só aplica se valor bruto > R$ 500.00                                                                  | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L316-L321` | —                                             | ALTO           | Conflita com cascata de CALCDSCT (BR-DSC-001..006)                                                           |
| BR-PGT-014 | Truncagem monetária: multiplica por 100 e divide por 100 (sem +0.005)                                                | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L323-L325` | todos os valores monetários                   | CRÍTICO        | **REGRA ESCONDIDA** — divergência com BR-REL-003 (arredondamento)                                            |
| BR-PGT-015 | Valor líquido negativo é forçado para zero                                                                           | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L318-L321` | `PAGAMENTO.VLR-LIQUIDO`                       | MÉDIO          | Contradiz BR-DSC-005 (judicial pode ficar negativo)                                                          |
| BR-PGT-016 | Status inicial do pagamento é `G` (gerado)                                                                           | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L408`      | `PAGAMENTO.STATUS-PGTO`                       | ALTO           | Transições: G → P (pago) · C (cancelado) · D (devolvido) · E (estornado)                                     |

### BATCHCON — Conciliação de Retorno Bancário CNAB 240 (BR-CON-001..012)

> Par 2. Integra com Banco do Brasil. Banco Real ainda comentado no código (banco adquirido em 2007).

| ID         | Regra de Negócio                                                                                          | Programa Fonte                                                        | Campos DDM                                                      | Nível de Risco | Notas                                                   |
| ---------- | --------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | --------------------------------------------------------------- | -------------- | ------------------------------------------------------- |
| BR-CON-001 | Formato CNAB 240 (BB): Banco(3) · Lote(4) · TipoReg(1) · CPF(44) · Valor(120) · DtPgto(140) · CodRet(231) | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L50-L90`   | — (parsing posicional)                                          | CRÍTICO        | Layout fechado                                          |
| BR-CON-002 | Processa apenas registros detalhe tipo `3`; ignora headers/footers                                        | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L92-L95`   | —                                                               | CRÍTICO        | Filtro obrigatório                                      |
| BR-CON-003 | Conversão centavos→reais: divide por 100                                                                  | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L100-L102` | —                                                               | CRÍTICO        | CNAB envia em centavos inteiros                         |
| BR-CON-004 | Match exato CPF + NUM-PAGTO + COMPETENCIA para identificar pagamento                                      | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L107-L121` | `PAGAMENTO.CPF`, `PAGAMENTO.NUM-PAGTO`, `PAGAMENTO.COMPETENCIA` | CRÍTICO        | Chave composta                                          |
| BR-CON-005 | Tolerância de conciliação: R$ 0.01 (`abs(diff) ≤ 0.01`)                                                   | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L123-L128` | —                                                               | ALTO           | **REGRA ESCONDIDA** — valor hardcoded sem justificativa |
| BR-CON-006 | Código retorno `00` → status `P` (pago); código de banco fixo = 1 (BB)                                    | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L138-L147` | `PAGAMENTO.STATUS-PGTO`                                         | CRÍTICO        | Apenas BB suportado                                     |
| BR-CON-007 | Código retorno `01` → status `D` (devolvido); motivo em `COD-RETORNO`                                     | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L148-L154` | `PAGAMENTO.STATUS-PGTO`                                         | ALTO           |                                                         |
| BR-CON-008 | Código retorno `02` → status `E` (estornado); causa não detalhada                                         | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L155-L161` | `PAGAMENTO.STATUS-PGTO`                                         | ALTO           |                                                         |
| BR-CON-009 | Data de pagamento capturada do CNAB no formato `YYYYMMDD`                                                 | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L139-L147` | `PAGAMENTO.DT-PAGAMENTO`                                        | ALTO           |                                                         |
| BR-CON-010 | Conciliação bem-sucedida registra auditoria com ação `CO` (usuário `BATCH`)                               | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L195-L210` | `AUDITORIA.ACAO`, `AUDITORIA.USUARIO`                           | CRÍTICO        | Trilha obrigatória                                      |
| BR-CON-011 | Divergência registra auditoria com ação `DV`; grava `VLR-ANTERIOR` (SIFAP) e `VLR-NOVO` (Banco)           | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L211-L225` | `AUDITORIA.VLR-ANTERIOR`, `AUDITORIA.VLR-NOVO`                  | CRÍTICO        | Rastreabilidade financeira                              |
| BR-CON-012 | Integração Banco Real (descontinuada em 2007) ainda presente como código comentado                        | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L168-L200` | —                                                               | MÉDIO          | **REGRA ESCONDIDA** — código morto há 17+ anos          |

### BATCHREL — Relatórios Batch Agregados (BR-REL-001..007)

> Par 2. Agrega por região e status.

| ID         | Regra de Negócio                                                               | Programa Fonte                                                        | Campos DDM                | Nível de Risco | Notas                                                                                         |
| ---------- | ------------------------------------------------------------------------------ | --------------------------------------------------------------------- | ------------------------- | -------------- | --------------------------------------------------------------------------------------------- |
| BR-REL-001 | Cinco macro-regiões: NORTE, NORDESTE, SUDESTE, SUL, CENTRO-OESTE               | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L71-L76`   | —                         | MÉDIO          | Codinome de display                                                                           |
| BR-REL-002 | Mapeamento `COD-REGIAO → macro`: 1–5→1 · 6–10→2 · 11–15→3 · 16–20→4 · 21–27→5  | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L104-L120` | `BENEFICIARIO.COD-REGIAO` | CRÍTICO        | Quebra ranges fixos                                                                           |
| BR-REL-003 | Arredondamento (ROUND `+0.005`) — divergente da truncagem em BATCHPGT          | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L130-L135` | valores monetários        | ALTO           | **REGRA ESCONDIDA** — totais do relatório podem divergir dos pagamentos efetivos por centavos |
| BR-REL-004 | Status válidos para agregação: `G`, `P`, `C`, `D`, `E` mapeados em índices 1–5 | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L137-L147` | `PAGAMENTO.STATUS-PGTO`   | CRÍTICO        | Coerente com BR-PGT-016                                                                       |
| BR-REL-005 | Paginação padrão mainframe: 66 linhas por página                               | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L50`       | —                         | BAIXO          | Apresentação                                                                                  |
| BR-REL-006 | Filtro por competência (`READ BY COMPETENCIA = entrada`); escape ao mudar      | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L88-L92`   | `PAGAMENTO.COMPETENCIA`   | CRÍTICO        | Index-based range read                                                                        |
| BR-REL-007 | Múltiplos acumuladores cruzados: total por região e por status                 | `01-arqueologia/legado-sifap/natural-programs/BATCHREL.NSN#L63-L70`   | —                         | CRÍTICO        | Agregação multidimensional                                                                    |

### CADBENEF — Cadastro de Beneficiário (BR-CAD-001..011)

> Par 1. Inclusão/alteração interativa. Reusa MOD-11 implementado também em VALBENEF (BR-001).

| ID         | Regra de Negócio                                                                                              | Programa Fonte                                                        | Campos DDM                   | Nível de Risco | Notas                                                                          |
| ---------- | ------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ---------------------------- | -------------- | ------------------------------------------------------------------------------ |
| BR-CAD-001 | CPF obrigatório validado por MOD-11 com 2 dígitos verificadores                                               | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L130-L140` | `BENEFICIARIO.CPF`           | CRÍTICO        | Duplicação de BR-001                                                           |
| BR-CAD-002 | CPF único: rejeita inclusão se já existe                                                                      | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L160-L166` | `BENEFICIARIO.CPF`           | CRÍTICO        | FIND com chave primária                                                        |
| BR-CAD-003 | Nome obrigatório (60 chars), não vazio                                                                        | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L145-L148` | `BENEFICIARIO.NOME`          | ALTO           |                                                                                |
| BR-CAD-004 | Data de nascimento obrigatória no formato `YYYYMMDD`                                                          | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L150-L152` | `BENEFICIARIO.DT-NASCIMENTO` | CRÍTICO        | Insumo para cálculo de idade                                                   |
| BR-CAD-005 | Sexo aceita apenas `M` ou `F`                                                                                 | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L154-L157` | `BENEFICIARIO.SEXO`          | MÉDIO          |                                                                                |
| BR-CAD-006 | Status inicial sempre `A` (ativo) ao incluir                                                                  | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L170-L172` | `BENEFICIARIO.STATUS`        | CRÍTICO        | Default                                                                        |
| BR-CAD-007 | Maiores de 75 anos recebem automaticamente status `S` (suspenso) na inclusão                                  | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L174-L176` | `BENEFICIARIO.STATUS`        | ALTO           | **REGRA ESCONDIDA** — discriminação por idade hardcoded; pode ferir legislação |
| BR-CAD-008 | Operações suportadas: apenas `I` (inclusão) e `A` (alteração) — sem delete                                    | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L105-L109` | —                            | CRÍTICO        | Constraint de design                                                           |
| BR-CAD-009 | Primeiro dígito MOD-11: pesos 10..2 sobre 9 primeiros; resto < 2 → DV=0; senão DV=11-resto                    | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L283-L300` | —                            | CRÍTICO        | Algoritmo central                                                              |
| BR-CAD-010 | Segundo dígito MOD-11: pesos 11..2 sobre 10 primeiros (incluindo DV1)                                         | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L302-L315` | —                            | CRÍTICO        | Mesma lógica                                                                   |
| BR-CAD-011 | Alteração NÃO modifica CPF, DT-NASCIMENTO ou tipo de programa; atualiza apenas dados pessoais e RENDA/NUM-DEP | `01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L189-L195` | `BENEFICIARIO.*`             | CRÍTICO        | Lista explícita de campos editáveis                                            |

### CADDEPEND — Cadastro de Dependentes (BR-DEP-001..007)

> Par 1. Usa estrutura `PE` (Periodic Group) do Adabas.

| ID         | Regra de Negócio                                                                                        | Programa Fonte                                                        | Campos DDM                     | Nível de Risco | Notas                                        |
| ---------- | ------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ------------------------------ | -------------- | -------------------------------------------- |
| BR-DEP-001 | Máximo de 5 dependentes por beneficiário                                                                | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L65`      | `BENEFICIARIO.NUM-DEPENDENTES` | CRÍTICO        | Coerente com fator familiar BR-BEN-003       |
| BR-DEP-002 | Parentesco aceita apenas: `FI` (filho), `CO` (cônjuge), `IR` (irmão), `OU` (outro)                      | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L87-L90`  | `BENEFICIARIO.PARENTESCO`      | CRÍTICO        | Domínio fechado                              |
| BR-DEP-003 | CPF de dependente único: rejeita se já cadastrado em qualquer beneficiário e ≠ 0                        | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L97-L105` | `BENEFICIARIO.CPF-DEP`         | ALTO           | Loop de verificação                          |
| BR-DEP-004 | Não permite incluir dependente se status do beneficiário é `C` (cancelado) ou `D` (desligado)           | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L54-L57`  | `BENEFICIARIO.STATUS`          | CRÍTICO        |                                              |
| BR-DEP-005 | Nome do dependente obrigatório (60 chars)                                                               | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L74-L76`  | `BENEFICIARIO.NOME-DEP`        | MÉDIO          |                                              |
| BR-DEP-006 | Estrutura `DEPENDENTES(PE)` é Periodic Group; dados não sobrevivem a restart sem persistência explícita | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L16-L22`  | `BENEFICIARIO.DEPENDENTES(PE)` | MÉDIO          | **REGRA ESCONDIDA** — semântica Adabas opaca |
| BR-DEP-007 | Loop de inclusão permite múltiplos dependentes em sequência até resposta `N`                            | `01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L60-L117` | —                              | BAIXO          | UX interativa                                |

### CADPROG — Cadastro de Programa Social (BR-PRG-001..008)

> Par 1. Aplica um "Fator K" mágico nunca documentado.

| ID         | Regra de Negócio                                                                      | Programa Fonte                                                       | Campos DDM                          | Nível de Risco | Notas                                                                                                    |
| ---------- | ------------------------------------------------------------------------------------- | -------------------------------------------------------------------- | ----------------------------------- | -------------- | -------------------------------------------------------------------------------------------------------- |
| BR-PRG-001 | Tipo de programa aceita apenas: `A` (assistencial), `P` (previdência), `T` (trabalho) | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L40`       | `PROGRAMA-SOCIAL.TIPO`              | CRÍTICO        | Coerente com BR-016..BR-019                                                                              |
| BR-PRG-002 | Fator K de correção: `1.00 + (FATOR-REAJ × 0.347215)`                                 | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L100-L102` | `PROGRAMA-SOCIAL.FATOR-REAJUSTE`    | CRÍTICO        | **REGRA ESCONDIDA** — constante `0.347215` sem qualquer comentário; possível índice inflacionário antigo |
| BR-PRG-003 | Valor base ajustado e armazenado: `VLR-CALC = VLR-BASE × FATOR-K`                     | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L103`      | `PROGRAMA-SOCIAL.VLR-BASE`          | CRÍTICO        | Valor persistido NUNCA é o bruto informado                                                               |
| BR-PRG-004 | Código de elegibilidade (`A5`) referência tabela externa, sem validação               | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L40`       | `PROGRAMA-SOCIAL.COD-ELEGIBILIDADE` | MÉDIO          | Acoplamento solto                                                                                        |
| BR-PRG-005 | `DT-FIM = 0` significa programa indeterminado; sem validação `DT-FIM ≥ DT-INICIO`     | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L60-L62`   | `PROGRAMA-SOCIAL.DT-FIM`            | MÉDIO          | Possível inconsistência                                                                                  |
| BR-PRG-006 | Status inicial sempre `A` (ativo)                                                     | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L104`      | `PROGRAMA-SOCIAL.STATUS-PROG`       | CRÍTICO        | Nenhum programa pode iniciar inativo                                                                     |
| BR-PRG-007 | `COD-PROGRAMA` único: rejeita duplicidade                                             | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L94-L99`   | `PROGRAMA-SOCIAL.COD-PROGRAMA`      | CRÍTICO        |                                                                                                          |
| BR-PRG-008 | Operações suportadas: `I` (inclusão) e `C` (consulta) — sem alterar nem deletar       | `01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L76-L77`   | —                                   | MÉDIO          | Constraint forte                                                                                         |

### CONSBENF — Consulta Interativa de Beneficiário (BR-CSL-001..006)

> Par 1. Mascara CPF de forma inconsistente — documentado como intencional.

| ID         | Regra de Negócio                                                                         | Programa Fonte                                                        | Campos DDM                             | Nível de Risco | Notas                                                                                                                                          |
| ---------- | ---------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | -------------------------------------- | -------------- | ---------------------------------------------------------------------------------------------------------------------------------------------- |
| BR-CSL-001 | Dois modos de busca: `C` (por CPF) ou `N` (por NIS); default `C` se em branco            | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L115-L128` | `BENEFICIARIO.CPF`, `BENEFICIARIO.NIS` | CRÍTICO        |                                                                                                                                                |
| BR-CSL-002 | Máscara de CPF com regra inconsistente declarada no comentário                           | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L186-L210` | `BENEFICIARIO.CPF`                     | ALTO           | **REGRA ESCONDIDA** — comentário literal: _"Às vezes mostra primeiros 3 dígitos ao invés de últimos. Não corrigir sem aprovação da auditoria"_ |
| BR-CSL-003 | Decode de status: `A`=ATIVO · `S`=SUSPENSO · `C`=CANCELADO · `I`=INATIVO · `D`=DESLIGADO | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L130-L147` | `BENEFICIARIO.STATUS`                  | CRÍTICO        | Coerente com BR-009                                                                                                                            |
| BR-CSL-004 | Histórico mostra últimos 12 pagamentos em ordem descendente                              | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L150-L170` | `PAGAMENTO.*`                          | ALTO           | Limite fixo                                                                                                                                    |
| BR-CSL-005 | Tela primária via MAP `CONSBENF-M01`                                                     | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L92-L96`   | —                                      | MÉDIO          |                                                                                                                                                |
| BR-CSL-006 | Fallback para input textual se MAP retorna erro                                          | `01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN#L97-L105`  | —                                      | MÉDIO          | Compatibilidade com modo batch                                                                                                                 |

### RELAUDIT — Relatório de Auditoria (BR-AUD-001..009)

> Par 5. Filtra ações de exclusão silenciosamente.

| ID         | Regra de Negócio                                                                                                                       | Programa Fonte                                                        | Campos DDM          | Nível de Risco | Notas                                                 |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ------------------- | -------------- | ----------------------------------------------------- |
| BR-AUD-001 | Ações auditadas (2 chars): `IN` (inclusão), `AL` (alteração), `CO` (conciliação), `CN` (consulta), `DV` (divergência), `EX` (exclusão) | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L145-L165` | `AUDITORIA.ACAO`    | CRÍTICO        | Domínio fechado                                       |
| BR-AUD-002 | Ações `EX` (exclusão) são gravadas, mas NUNCA exibidas no relatório                                                                    | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L155-L160` | `AUDITORIA.ACAO`    | ALTO           | **REGRA ESCONDIDA** — possível back door de auditoria |
| BR-AUD-003 | Período padrão `19970101` (data de início do SIFAP) quando filtro não informado                                                        | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L120-L125` | —                   | MÉDIO          | Constante histórica                                   |
| BR-AUD-004 | Jobs batch gravam auditoria com usuário `BATCH` (sem rastreamento individual)                                                          | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L200`      | `AUDITORIA.USUARIO` | MÉDIO          | Limitação de rastreabilidade                          |
| BR-AUD-005 | Formatação de hora: `HHMMSS` → `HH:MM:SS`                                                                                              | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L168-L171` | —                   | BAIXO          | Apresentação                                          |
| BR-AUD-006 | Paginação mainframe: 66 linhas por página                                                                                              | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L50`       | —                   | BAIXO          | Apresentação                                          |
| BR-AUD-007 | Dois modos de saída: `T` (tela/WRITE) ou `I` (impressora/PRINT); default `T`                                                           | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L173-L180` | —                   | MÉDIO          |                                                       |
| BR-AUD-008 | Contadores agregados por tipo de ação: `IN`, `AL`, `CO`, `CN`, `DV`, OUTROS                                                            | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L165-L185` | —                   | CRÍTICO        | Estatística para auditor                              |
| BR-AUD-009 | Filtro opcional de ação por igualdade exata com `ACAO-FILTRO`; branco = sem filtro                                                     | `01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN#L162-L167` | `AUDITORIA.ACAO`    | MÉDIO          |                                                       |

### RELPGT — Relatório de Pagamentos (BR-RPG-001..008)

> Par 5. Quebras por programa com subtotal.

| ID         | Regra de Negócio                                                                               | Programa Fonte                                                      | Campos DDM               | Nível de Risco | Notas                                                                                              |
| ---------- | ---------------------------------------------------------------------------------------------- | ------------------------------------------------------------------- | ------------------------ | -------------- | -------------------------------------------------------------------------------------------------- |
| BR-RPG-001 | Filtro por programa: `0` = todos os programas; qualquer outro valor filtra por igualdade       | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L85-L90`   | `PAGAMENTO.COD-PROGRAMA` | CRÍTICO        | Sentinela 0                                                                                        |
| BR-RPG-002 | Quebra/subtotal ao mudar `COD-PROGRAMA` (compara com `#PROG-ANT`)                              | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L110-L117` | `PAGAMENTO.COD-PROGRAMA` | CRÍTICO        | Depende de ordenação por programa                                                                  |
| BR-RPG-003 | Máscara CPF: `***.***.*XX-XX` (mostra últimos 2 dígitos, oculta 7 centrais)                    | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L120-L125` | `BENEFICIARIO.CPF`       | MÉDIO          | Divergente de BR-CSL-002                                                                           |
| BR-RPG-004 | Decode tipo pagamento: `N`=NORMAL · `D`=DECIMO/13º · `T`=TERCEIRO                              | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L127-L137` | `PAGAMENTO.TIPO-PGTO`    | MÉDIO          | **Possível mistério** — `T` declarado aqui mas nunca atribuído em BATCHPGT (relacionado a MYS-002) |
| BR-RPG-005 | Decode status pagamento: `G`=GERADO · `P`=PAGO · `C`=CANCELADO · `D`=DEVOLVIDO · `E`=ESTORNADO | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L139-L151` | `PAGAMENTO.STATUS-PGTO`  | CRÍTICO        | Coerente com BR-PGT-016, BR-REL-004                                                                |
| BR-RPG-006 | Filtro de competência exige `COMP-INI` e `COMP-FIM`; range inclusive                           | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L79-L82`   | `PAGAMENTO.COMPETENCIA`  | CRÍTICO        |                                                                                                    |
| BR-RPG-007 | Paginação mainframe: 66 linhas por página                                                      | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L53`       | —                        | BAIXO          | Apresentação                                                                                       |
| BR-RPG-008 | Subtotais por programa acumulam `BRUTO` e `LIQUIDO`; reset após break                          | `01-arqueologia/legado-sifap/natural-programs/RELPGT.NSN#L230-L235` | —                        | CRÍTICO        | Agregação                                                                                          |

### BR-GERAL — Regras Transversais (BR-GERAL-001..004)

> Recuperadas do `develop-implementacao` (Par 3) e do achado do Par 2 sobre acoplamento.

| ID           | Regra de Negócio                                                                                           | Programa Fonte                                                             | Campos DDM                                                       | Nível de Risco | Notas                                       |
| ------------ | ---------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------- | ---------------------------------------------------------------- | -------------- | ------------------------------------------- |
| BR-GERAL-001 | Cascata obrigatória do ciclo de pagamento: `BATCHPGT → CALCBENF → CALCCORR → CALCDSCT → ARQ 160`           | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN` (orquestrador) | `PAGAMENTO.*`                                                    | CRÍTICO        | Ordem afeta resultado                       |
| BR-GERAL-002 | Todos os valores monetários em formato numérico com 2 casas decimais (N9.2)                                | `01-arqueologia/legado-sifap/adabas-ddms/PAGAMENTO.ddm`                    | `PAGAMENTO.VLR-*`                                                | ALTO           | Convenção do legado                         |
| BR-GERAL-003 | Beneficiário ativo (`STATUS='A'`) é pré-requisito; excluídos (`E`) e suspensos (`S`) não recebem pagamento | múltiplos (`BATCHPGT`, `CALCBENF`, `VALELEG`)                              | `BENEFICIARIO.STATUS`                                            | CRÍTICO        | Coerente com BR-011, BR-CAD-006, BR-PGT-006 |
| BR-GERAL-004 | Acoplamento entre programas é exclusivamente via DDMs compartilhados (sem `CALLNAT`); coesão indireta      | varredura completa dos 15 `.NSN`                                           | DDMs `BENEFICIARIO`, `PROGRAMA-SOCIAL`, `PAGAMENTO`, `AUDITORIA` | ALTO           | Achado do Par 2                             |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                                        | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-NNN | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

## Regras por Categoria

### Cálculos Financeiros

<!-- Liste aqui as regras relacionadas a cálculos de valores, benefícios, etc. -->

### Validações de Status

<!-- Liste aqui as regras de transição de status (A, S, C, I, D) -->

### Regras de Autorização

<!-- Liste aqui as regras de quem pode fazer o quê -->

### Regras de Negócio Temporais

<!-- Liste aqui regras com prazos, datas-limite, períodos -->

## Fichas por programa

### Par 2 · Arquitetura (BATCHPGT, BATCHCON, BATCHREL)

| Campo                       | BATCHPGT.NSN                                                                                                                                                   | BATCHCON.NSN                                    | BATCHREL.NSN                                |
| --------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------------------------------------- | ------------------------------------------- |
| Autor original              | Carlos Roberto da Silva                                                                                                                                        | Marcos Antonio Ribeiro                          | Patricia Gomes de Souza                     |
| Data inicial                | 22/06/1997                                                                                                                                                     | 05/03/2000                                      | 10/11/1999                                  |
| Última alteração            | 10/07/2015 (Anderson Lima — INC AUDITORIA)                                                                                                                     | 30/01/2014 (Anderson Lima — INC AUDITORIA)      | 14/02/2013 (Anderson Lima — AJUSTE FORMATO) |
| Inputs (DDMs lidos)         | BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO                                                                                                                       | PAGAMENTO, AUDITORIA, WORK FILE 1 (CNAB BB)     | PAGAMENTO, BENEFICIARIO                     |
| Outputs (DDMs escritos)     | PAGAMENTO (STORE)                                                                                                                                              | PAGAMENTO (UPDATE), AUDITORIA (STORE)           | — (spool de impressão)                      |
| CALLNAT (chamadas externas) | **0** — header menciona "CHAMA CALCBENF E CALCDSCT", mas é comentário falso. Reuso só por `PERFORM` interno.                                                   | **0** — `PERFORM GRAVA-AUDITORIA-CONC` interno. | **0** — sem subrutinas relevantes.          |
| BRs extraídas               | _pendente de re-mapeamento_ (IDs antigos colidem com BR-001..BR-012 já alocadas a VALBENEF/VALDOCS/VALELEG após merge — atribuir novos IDs a partir de BR-022) | _pendente de re-mapeamento_                     | _pendente de re-mapeamento_                 |
| Mistérios                   | MYS-002                                                                                                                                                        | MYS-001                                         | —                                           |
| Sistemas externos           | SIAFI (TXT empenho), Banco do Brasil (CNAB remessa)                                                                                                            | Banco do Brasil (CNAB 240 retorno)              | —                                           |

> **Achado transversal Par 2:** nenhum dos 3 programas usa `CALLNAT`. O acoplamento entre programas se dá exclusivamente via DDMs compartilhados (BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO, AUDITORIA). Confirmado por varredura completa dos 15 `.NSN`.

## Resumo Estatístico

- **Total de regras encontradas: 116** (catálogo consolidado pós-merge, cobrindo os 15 programas Natural)
  - VAL\* (BR-001..BR-021): 21
  - CALC\* (BR-BEN + BR-COR + BR-DSC): 22
  - BATCH\* (BR-PGT + BR-CON + BR-REL): 35
  - CAD\* (BR-CAD + BR-DEP + BR-PRG): 26
  - CONSBENF (BR-CSL): 6
  - REL\* (BR-AUD + BR-RPG): 17
  - Transversais (BR-GERAL): 4
- **Regras críticas:** 60+ (financeiras, status, validações de chave)
- **Regras escondidas / back doors:** 14 marcadas explicitamente — `BR-002`, `BR-003`, `BR-013`, `BR-PGT-004`, `BR-PGT-014`, `BR-CON-005`, `BR-CON-012`, `BR-REL-003`, `BR-CAD-007`, `BR-PRG-002`, `BR-CSL-002`, `BR-AUD-002`, `BR-DEP-006`, mais o conjunto de mistérios MYS-001..MYS-007
- **Conflitos detectados entre programas:**
  - BR-BEN-003 (`+0.15` linear) vs BR-PGT-009 (escalonado): fator familiar divergente
  - BR-BEN-004 (faixas <500/500-1000/...) vs BR-PGT-003 (faixas 300/600/1000/1500): renda divergente
  - BR-PGT-014 (truncagem) vs BR-REL-003 (arredondamento): totais de relatório ≠ pagamentos efetivos
  - BR-DSC-005 (judicial pode negativar) vs BR-PGT-015 (zera negativos): regra perdida ou redundante?
- **Duplicações estruturais:** validação CPF MOD-11 implementada três vezes (`VALBENEF`, `VALDOCS`, `CADBENEF` — BR-001, BR-CAD-001, BR-CAD-009)
- **Cobertura de programas:** 15/15 (100%) — todos os `.NSN` de `01-arqueologia/legado-sifap/natural-programs/` mapeados

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 1</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="dependency-map.md"><strong>dependency-map.md</strong></a><br/>
<sub>Mapa de quem chama quem.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>
