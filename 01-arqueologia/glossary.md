<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Glossário do SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **glossary**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Preencha esta tabela com todos os termos, abreviações e siglas encontrados no código Natural/Adabas.
> **Meta: no mínimo 30 termos.**

## Por que isso importa

Sistemas legados têm vocabulário próprio que ninguém documenta em lugar nenhum — só está no nome das variáveis. Se o time do Estágio 2 não souber o que `DSCT`, `BENF`, `PE` ou `CTC` significam, vai escrever uma spec sobre o que ele _acha_ que isso significa. Glossário é o que evita esse desencontro.

## Como preencher

- **Termo**: a abreviação ou sigla exatamente como aparece no código
- **Expansão**: o significado completo do termo
- **Programa**: em qual arquivo `.NSN` ou `.ddm` o termo foi encontrado
- **Contexto**: breve explicação de como/onde o termo é usado

## Dica de extração

Prompt útil no Copilot Chat (cole o conteúdo de 2–3 arquivos `.NSN` no chat antes):

> _"Liste todas as abreviações e siglas usadas neste código Natural. Para cada uma, sugira a expansão e marque com 'CONFIRMADO' ou 'HIPÓTESE'."_

## Termos encontrados

> Contribuição Par 2 (EA + SA) — termos extraídos dos 3 batches (BATCHPGT, BATCHCON, BATCHREL). Demais pares acrescentam a partir do termo 20.

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1   | `BENF` | Beneficiário | `BATCHPGT.NSN`, `BENEFICIARIO.ddm` | Pessoa física receptora do pagamento. Prefixo em variáveis (`#VLR-BENF`, `CPF-BENEF`). |
| 2   | `PGT` / `PGTO` | Pagamento | `BATCHPGT.NSN`, `BATCHCON.NSN` | Unidade de pagamento mensal. `NUM-PAGTO` é sequencial global. |
| 3   | `COMPETENCIA` | Mês/ano de referência (AAAAMM) | `BATCHPGT.NSN#L168`, `PAGAMENTO.ddm` | Identifica o ciclo (ex.: 202606). |
| 4   | `CICLO` | Conjunto de pagamentos de uma COMPETENCIA | `BATCHPGT.NSN` (implícito) | Não existe como entidade no legado; modernizado como aggregate `CicloPagamento`. |
| 5   | `CNAB 240` | Centro Nacional de Automação Bancária — layout FEBRABAN 240 colunas | `BATCHCON.NSN#L10` | Remessa e retorno do Banco do Brasil. |
| 6   | `BB` | Banco do Brasil (código 001) | `BATCHCON.NSN` | Único banco integrado em produção. Banco Real (356) descontinuado. |
| 7   | `SIAFI` | Sist. Integ. Admin. Financeira (Gov. Federal) | `BATCHPGT.NSN`, `legacy-docs/ARQUITETURA-ORIGINAL-1997.md` | Recebe empenhos via TXT batch. Modernizar p/ REST. |
| 8   | `VLR-BRUTO` | Valor bruto (antes de descontos) | `PAGAMENTO.ddm`, `BATCHPGT.NSN#L277` | Resultado dos 5 fatores. |
| 9   | `VLR-LIQUIDO` | Valor líquido (bruto − descontos) | `PAGAMENTO.ddm`, `BATCHREL.NSN` | Valor creditado ao beneficiário. |
| 10  | `STATUS-PGTO` | Status do pagamento (1-5) | `BATCHCON.NSN`, `BATCHREL.NSN#L82-L86` | 1=GERADO, 2=PAGO, 3=CANCELADO, 4=DEVOLVIDO, 5=ESTORNADO. |
| 11  | `STATUS` (beneficiário) | Status A/S/C/I/D | `BATCHPGT.NSN#L196` | Só `A` recebe pagamento. |
| 12  | `COD-REGIAO` | Código de região administrativa (1-25) | `BENEFICIARIO.ddm`, `BATCHREL.NSN#L117-L133` | Mapeado em 5 macro-regiões. |
| 13  | `TAB-REG` | Tabela de fatores regionais | `BATCHPGT.NSN#L130-L151` | 27 posições; 26-27 sem documentação (MYS-002). |
| 14  | `FATOR-FAM` | Fator familiar (por dependentes) | `BATCHPGT.NSN#L249-L262` | Faixas progressivas. |
| 15  | `FATOR-IDADE` | Fator etário | `BATCHPGT.NSN#L264-L275` | ≥65→1.15; 60-64→1.10; <18→1.05. |
| 16  | `AUDITORIA` | Trilha append-only de eventos | `AUDITORIA.ddm`, `BATCHCON.NSN#L170-L205` | Eventos `PG-CONFIRMADO`, `PG-DEVOLVIDO`. |
| 17  | `CONCILIACAO` | Bater PAGAMENTO emitido vs retorno CNAB | `BATCHCON.NSN` | Processo diário. |
| 18  | `WORK FILE` | Arquivo sequencial temporário (não-DDM) | `BATCHCON.NSN#L106` | `WORK FILE 1` = retorno CNAB BB. |
| 19  | `JCL` | Job Control Language (script batch mainframe) | `BATCHREL.NSN` (param `#COMPETENCIA`) | Modernizado p/ scheduler (REQ-PAY-001). |
| 20  |       |          |          |          |
| 21  |       |          |          |          |
| 22  |       |          |          |          |
| 23  |       |          |          |          |
| 24  |       |          |          |          |
| 25  |       |          |          |          |
| 26  |       |          |          |          |
| 27  |       |          |          |          |
| 28  |       |          |          |          |
| 29  |       |          |          |          |
| 30  |       |          |          |          |

> Adicione mais linhas conforme necessário. Não se limite a 30!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- Anote aqui qualquer padrão de nomenclatura que o time identificou:
- Convenções de prefixo/sufixo encontradas:
- Termos ambíguos que precisam de validação com especialista:

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
<a href="business-rules-catalog.md"><strong>business-rules-catalog.md</strong></a><br/>
<sub>Catálogo de regras.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

