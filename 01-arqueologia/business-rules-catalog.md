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

> Contribuição Par 2 (EA + SA) — BRs extraídas dos 3 batches: BATCHPGT, BATCHCON, BATCHREL. Demais pares acrescentam a partir da BR-013.

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 |                  |                |            |                |       |
| BR-002 |                  |                |            |                |       |
| BR-003 |                  |                |            |                |       |
| BR-004 |                  |                |            |                |       |
| BR-005 |                  |                |            |                |       |
| BR-006 |                  |                |            |                |       |
| BR-007 |                  |                |            |                |       |
| BR-008 |                  |                |            |                |       |
| BR-009 |                  |                |            |                |       |
| BR-010 |                  |                |            |                |       |
| BR-011 |                  |                |            |                |       |
| BR-012 |                  |                |            |                |       |
| BR-013 |                  |                |            |                |       |
| BR-014 |                  |                |            |                |       |
| BR-015 |                  |                |            |                |       |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                                        | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | --------------------------------------------------------------------- | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
| BR-013 | Desconto total não pode exceder 30% do valor bruto, exceto descontos judiciais (tipo J) | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` | `PAGAMENTO.VLR-BRUTO`, `PAGAMENTO.VLR-TOTAL-DSCT`, `PAGAMENTO.TIPO-DSCT` | CRÍTICO        | Regra financeira. Tipo 'J' = exceção legal |

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

| Campo | BATCHPGT.NSN | BATCHCON.NSN | BATCHREL.NSN |
| --- | --- | --- | --- |
| Autor original | Carlos Roberto da Silva | Marcos Antonio Ribeiro | Patricia Gomes de Souza |
| Data inicial | 22/06/1997 | 05/03/2000 | 10/11/1999 |
| Última alteração | 10/07/2015 (Anderson Lima — INC AUDITORIA) | 30/01/2014 (Anderson Lima — INC AUDITORIA) | 14/02/2013 (Anderson Lima — AJUSTE FORMATO) |
| Inputs (DDMs lidos) | BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO | PAGAMENTO, AUDITORIA, WORK FILE 1 (CNAB BB) | PAGAMENTO, BENEFICIARIO |
| Outputs (DDMs escritos) | PAGAMENTO (STORE) | PAGAMENTO (UPDATE), AUDITORIA (STORE) | — (spool de impressão) |
| CALLNAT (chamadas externas) | **0** — header menciona "CHAMA CALCBENF E CALCDSCT", mas é comentário falso. Reuso só por `PERFORM` interno. | **0** — `PERFORM GRAVA-AUDITORIA-CONC` interno. | **0** — sem subrotinas relevantes. |
| BRs extraídas | BR-001 · BR-002 · BR-003 · BR-004 · BR-005 · BR-006 | BR-007 · BR-008 · BR-012 | BR-009 · BR-010 · BR-011 |
| Mistérios | MYS-002 | MYS-001 | — |
| Sistemas externos | SIAFI (TXT empenho), Banco do Brasil (CNAB remessa) | Banco do Brasil (CNAB 240 retorno) | — |

> **Achado transversal Par 2:** nenhum dos 3 programas usa `CALLNAT`. O acoplamento entre programas se dá exclusivamente via DDMs compartilhados (BENEFICIARIO, PROGRAMA-SOCIAL, PAGAMENTO, AUDITORIA). Confirmado por varredura completa dos 15 `.NSN`.

## Resumo Estatístico

- Total de regras encontradas: **12** (Par 2 contribuiu BR-001..BR-012; pares 1/3/4/5 acrescentam a partir da BR-013)
- Regras críticas: **5** (BR-002, BR-003, BR-005, BR-008, e BR-007 indireto)
- Regras com duplicação: 0
- Regras sem documentação prévia (escondidas no código): **12**

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
