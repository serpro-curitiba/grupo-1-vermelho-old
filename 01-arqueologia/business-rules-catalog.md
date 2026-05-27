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

> Contribuição consolidada — BR-001..BR-010 extraídas de VALBENEF/VALDOCS (validações de beneficiário e documentos); BR-011..BR-021 extraídas de VALELEG (elegibilidade). Demais pares acrescentam a partir da BR-022.

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

- Total de regras encontradas: **21** (BR-001..BR-021; demais pares acrescentam a partir da BR-022)
- Regras críticas: **6** (BR-001, BR-002, BR-003, BR-011, BR-013, BR-015)
- Regras com duplicação: **1** (lógica de validação de CPF replicada entre VALBENEF e VALDOCS — ver BR-001)
- Regras escondidas explicitamente marcadas no código: **3** (BR-002, BR-003, BR-013) — ainda restam outras a descobrir

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
