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

> Tabela consolidada com contribuições de todos os pares. Termos extraídos dos programas Natural (CALC*, BATCH*, CAD*, REL*, CONS*, VAL*) e dos DDMs Adabas. Quando um termo apareceu em mais de uma contribuição, foi enriquecido com a informação mais completa (linhas de código, ambiguidades, mapeamento para modernização).

| #   | Termo                   | Expansão                                                            | Programa                                                   | Contexto                                                                                      |
| --- | ----------------------- | ------------------------------------------------------------------- | ---------------------------------------------------------- | --------------------------------------------------------------------------------------------- |
| 1   | `SIFAP`                 | Sistema de Fiscalização e Administração de Pagamentos               | Cabeçalhos dos programas                                   | Sigla do sistema legado em modernização.                                                      |
| 2   | `BENF`                  | Benefício / Beneficiário (ambíguo — ver Observações)                | `CALCBENF.NSN`, `BATCHPGT.NSN`, `BENEFICIARIO.ddm`         | Prefixo em variáveis (`#VLR-BENF`, `CPF-BENEF`). Refere-se ora ao valor, ora à pessoa física. |
| 3   | `PGT` / `PGTO`          | Pagamento                                                           | `BATCHPGT.NSN`, `BATCHCON.NSN`, `PAGAMENTO.ddm`            | Unidade de pagamento mensal. `NUM-PAGTO` é sequencial global.                                 |
| 4   | `CICLO`                 | Conjunto de pagamentos de uma COMPETENCIA                           | `BATCHPGT.NSN` (implícito)                                 | Não existe como entidade no legado; modernizado como aggregate `CicloPagamento`.              |
| 5   | `COMPETENCIA`           | Mês/ano de referência (AAAAMM)                                      | `BATCHPGT.NSN#L168`, `PAGAMENTO.ddm`, `PAGAMENTO.NSN`      | Identifica o ciclo (ex.: 202606).                                                             |
| 6   | `DSCT`                  | Desconto                                                            | `CALCDSCT.NSN`, `PAGAMENTO.ddm`                            | Descontos compulsórios (contribuição, imposto, judicial, etc.) deduzidos do bruto.            |
| 7   | `CORR`                  | Correção                                                            | `CALCCORR.NSN`                                             | Reajuste retroativo de pagamentos por variação IPCA.                                          |
| 8   | `CPF`                   | Cadastro de Pessoas Físicas                                         | `BENEFICIARIO.NSN`                                         | Número identificador do beneficiário (11 dígitos).                                            |
| 9   | `NIS`                   | Número de Identificação Social                                      | `CADBENEF.NSN`, `BATCHPGT.NSN`, `CONSBENF.NSN`             | Identificador social adicional ao CPF.                                                        |
| 10  | `DDM`                   | Data Definition Module                                              | DDM Adabas                                                 | Estrutura de dados (arquivos) no banco Adabas.                                                |
| 11  | `VLR`                   | Valor (prefixo)                                                     | `BATCHPGT.NSN`, `CALCBENF.NSN`                             | Prefixo de campos monetários.                                                                 |
| 12  | `VLR-BRUTO`             | Valor bruto (antes de descontos)                                    | `PAGAMENTO.ddm`, `BATCHPGT.NSN#L277`, `CALCBENF.NSN`       | Resultado dos 5 fatores aplicados ao valor base.                                              |
| 13  | `VLR-LIQUIDO` / `LIQ`   | Valor líquido (bruto − descontos)                                   | `PAGAMENTO.ddm`, `BATCHREL.NSN`, `RELPGT.NSN`              | Valor final creditado ao beneficiário.                                                        |
| 14  | `VLR-ABONO`             | Valor Abono                                                         | `CALCBENF.NSN#L200-220`                                    | Abono natalino (13º) quando aplicável.                                                        |
| 15  | `VLR-TEMP`              | Valor Temporário                                                    | Programas                                                  | Variável de trabalho para cálculos intermediários.                                            |
| 16  | `VLR-DIFF`              | Valor da Diferença                                                  | `CALCCORR.NSN`                                             | Diferença entre valor original e corrigido (para auditoria).                                  |
| 17  | `FATOR-REG`             | Fator Regional                                                      | `CALCBENF.NSN#L130-157`                                    | Multiplicador por região (1.0 a 1.4, 27 regiões).                                             |
| 18  | `FATOR-FAM`             | Fator Familiar                                                      | `CALCBENF.NSN#L160-180`, `BATCHPGT.NSN#L249-L262`          | Multiplicador por número de dependentes (faixas progressivas, 0 a 5+).                        |
| 19  | `FATOR-IDADE`           | Fator Etário                                                        | `BATCHPGT.NSN#L264-L275`                                   | ≥65 → 1.15; 60-64 → 1.10; <18 → 1.05.                                                         |
| 20  | `FATOR-RND`             | Fator de Renda                                                      | `CALCBENF.NSN`                                             | Multiplicador por faixa de renda familiar.                                                    |
| 21  | `FATOR-REAJ`            | Fator de Reajuste                                                   | `PROGRAMA-SOCIAL`                                          | Percentual de reajuste aplicado ao VLR-BASE.                                                  |
| 22  | `RENDA-FAM`             | Renda Familiar                                                      | `BENEFICIARIO.NSN`                                         | Renda total da família para cálculo de elegibilidade.                                         |
| 23  | `TAB-REG`               | Tabela de fatores regionais                                         | `BATCHPGT.NSN#L130-L151`                                   | 27 posições; 26-27 sem documentação (MYS-002).                                                |
| 24  | `COD-REGIAO`            | Código de região administrativa (1-25)                              | `BENEFICIARIO.ddm`, `BATCHREL.NSN#L117-L133`               | Mapeado em 5 macro-regiões.                                                                   |
| 25  | `UF`                    | Unidade da Federação                                                | `BENEFICIARIO.NSN`                                         | Estado onde beneficiário reside (sigla 2 letras).                                             |
| 26  | `ARQ-150`               | Arquivo 150 (Adabas)                                                | Adabas                                                     | Registro de beneficiário e dados cadastrais.                                                  |
| 27  | `ARQ-155`               | Arquivo 155 (Adabas)                                                | Adabas                                                     | Cadastro de programas sociais (VLR-BASE, FATOR-REAJUSTE).                                     |
| 28  | `ARQ-160`               | Arquivo 160 (Adabas)                                                | Adabas                                                     | Registro de pagamento (resultado de CALCBENF+CALCCORR+CALCDSCT).                              |
| 29  | `IPCA`                  | Índice Nacional de Preços ao Consumidor Amplo                       | `CALCCORR.NSN#L40-120`                                     | Índice oficial de inflação usado para correção retroativa de pagamentos.                      |
| 30  | `IND-CORRIGIDO`         | Indicador de Corrigido                                              | `CALCCORR.NSN`                                             | Flag (S/N) que indica se pagamento já foi corrigido (idempotência).                           |
| 31  | `TIPO-DSCT`             | Tipo de Desconto                                                    | `CALCDSCT.NSN#L100-120`                                    | Códigos: C=Contrib, I=Imposto, J=Judicial, P=Pensão, S=Sindical, A=Admin.                     |
| 32  | `TIPO-PGTO`             | Tipo de Pagamento                                                   | `CALCBENF.NSN#L45-55`                                      | N=Normal, D=Décimo, T=Terceiro (13º salário - variável).                                      |
| 33  | `STATUS-PGTO`           | Status do Pagamento (1-5)                                           | `PAGAMENTO.NSN`, `BATCHCON.NSN`, `BATCHREL.NSN#L82-L86`    | 1=GERADO, 2=PAGO, 3=CANCELADO, 4=DEVOLVIDO, 5=ESTORNADO.                                      |
| 34  | `STATUS` (beneficiário) | Status do beneficiário                                              | `BATCHPGT.NSN#L196`                                        | A=Ativo, S=Suspenso, C=Cancelado, I=Inativo, D=Desligado. Só `A` recebe pagamento.            |
| 35  | `NUM-DEPEND`            | Número de Dependentes                                               | `BENEFICIARIO.NSN`                                         | Quantidade de dependentes (0 a 3, limite configurável).                                       |
| 36  | `NUM-PROCESSO`          | Número de Processo                                                  | `CALCDSCT.NSN` (PE)                                        | Referência judicial para descontos tipo J.                                                    |
| 37  | `DT` / `DT-HOJE`        | Data (prefixo) / Data atual do sistema                              | Programas                                                  | Prefixo de campos de data. `DT-HOJE` = `*DATN` em Natural.                                    |
| 38  | `DT-GERACAO`            | Data de Geração                                                     | `PAGAMENTO.NSN`                                            | Data em que o pagamento foi processado.                                                       |
| 39  | `DT-CORRECAO`           | Data de Correção                                                    | `CALCCORR.NSN`                                             | Data em que a correção retroativa foi aplicada.                                               |
| 40  | `HR`                    | Hora (prefixo)                                                      | `BATCHCON.NSN`, `RELAUDIT.NSN`                             | Prefixo para campos de hora/evento.                                                           |
| 41  | `QTD`                   | Quantidade (prefixo)                                                | `BATCHPGT.NSN`, `RELAUDIT.NSN`                             | Contadores de processo e auditoria.                                                           |
| 42  | `TOT`                   | Total (prefixo)                                                     | `BATCHREL.NSN`, `RELPGT.NSN`                               | Acumuladores de totalização.                                                                  |
| 43  | `COD`                   | Código (prefixo)                                                    | `CADPROG.NSN`, `VALELEG.NSN`                               | Prefixo de identificadores de domínio.                                                        |
| 44  | `CALLNAT`               | Call Natural                                                        | `CALCBENF/CALCCORR/CALCDSCT`                               | Instrução Natural de chamada de subprograma.                                                  |
| 45  | `PE`                    | Periodic Group                                                      | Adabas DDM                                                 | Grupo de campos que se repete (ex: múltiplos descontos).                                      |
| 46  | `MU`                    | Multi-Valued                                                        | Adabas DDM                                                 | Campo que pode armazenar múltiplos valores.                                                   |
| 47  | `WORK FILE`             | Arquivo sequencial temporário (não-DDM)                             | `BATCHCON.NSN#L106`                                        | `WORK FILE 1` = retorno CNAB BB.                                                              |
| 48  | `JCL`                   | Job Control Language (script batch mainframe)                       | `BATCHREL.NSN` (param `#COMPETENCIA`)                      | Modernizado para scheduler (REQ-PAY-001).                                                     |
| 49  | `CNAB 240`              | Centro Nacional de Automação Bancária — layout FEBRABAN 240 colunas | `BATCHCON.NSN#L10`                                         | Remessa e retorno do Banco do Brasil.                                                         |
| 50  | `BB`                    | Banco do Brasil (código 001)                                        | `BATCHCON.NSN`                                             | Único banco integrado em produção. Banco Real (356) descontinuado.                            |
| 51  | `SIAFI`                 | Sistema Integrado de Administração Financeira (Gov. Federal)        | `BATCHPGT.NSN`, `legacy-docs/ARQUITETURA-ORIGINAL-1997.md` | Recebe empenhos via TXT batch. Modernizar para REST.                                          |
| 52  | `AUDITORIA`             | Trilha append-only de eventos                                       | `AUDITORIA.ddm`, `BATCHCON.NSN#L170-L205`                  | Eventos `PG-CONFIRMADO`, `PG-DEVOLVIDO`.                                                      |
| 53  | `CONCILIACAO`           | Bater PAGAMENTO emitido vs retorno CNAB                             | `BATCHCON.NSN`                                             | Processo diário de reconciliação.                                                             |

> Adicione mais linhas conforme necessário. Não se limite a 30!

## Exemplo de linha bem preenchida

| #   | Termo  | Expansão | Programa                        | Contexto                                                                                                         |
| --- | ------ | -------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------------- |
| 1   | `DSCT` | Desconto | `CALCDSCT.NSN`, `PAGAMENTO.ddm` | Tipo de dedução aplicada sobre valor bruto do pagamento. Tipos: 'J' (judicial), 'I' (imposto), 'T' (trabalhista) |

## Observações

- Anote aqui qualquer padrão de nomenclatura que o time identificou: prefixos funcionais por domínio foram recorrentes (CAD = cadastro, CALC = cálculo, VAL = validação, REL = relatório, CONS = consulta, BATCH = processamento em lote). Em variáveis, prefixos de campo também são consistentes (DT, HR, COD, NUM, VLR, QTD, TOT).
- Convenções de prefixo/sufixo encontradas: variáveis locais de trabalho usam # no Natural (ex.: #VLR-BASE, #QTD-ERROS); sufixos de contexto aparecem com frequência (BENEF, PROG, PGTO, DSCT, LIQ); views de arquivos Adabas seguem padrão com -V (ex.: BENEFICIARIO-V, PAGAMENTO-V).
- Termos ambíguos que precisam de validação com especialista: PAG (página x pagamento), REG (região x registro), DESC (descrição x desconto), SUB (subtotal x subrotina), CONT (controle x continuação), RND (arredondamento, provável), BENF (benefício x beneficiário dependendo do programa).

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
