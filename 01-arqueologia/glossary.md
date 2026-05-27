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

| #   | Termo         | Expansão                             | Programa                   | Contexto                                                                 |
| --- | ------------- | ------------------------------------ | -------------------------- | ------------------------------------------------------------------------ |
| 1   | BENF          | Benefício                            | CALCBENF.NSN               | Valor do benefício mensal calculado para o beneficiário                  |
| 2   | DSCT          | Desconto                             | CALCDSCT.NSN               | Descontos compulsórios (contribuição, imposto, judicial, etc.)           |
| 3   | CORR          | Correção                             | CALCCORR.NSN               | Reajuste retroativo de pagamentos por variação IPCA                      |
| 4   | CPF           | Cadastro de Pessoas Físicas          | BENEFICIARIO.NSN           | Número identificador do beneficiário (11 dígitos)                        |
| 5   | NIS           | Número de Inscrição Social           | CADBENEF.NSN               | Identificador único do beneficiário no programa social                   |
| 6   | DDM           | Data Definition Module               | DDM Adabas                 | Estrutura de dados (arquivos) no banco Adabas                            |
| 7   | VLR-BRUTO     | Valor Bruto                          | CALCBENF/CALCDSCT          | Valor do benefício sem descontos                                         |
| 8   | VLR-LIQUIDO   | Valor Líquido                        | CALCDSCT                   | Valor do benefício após todos os descontos                               |
| 9   | FATOR-REG     | Fator Regional                       | CALCBENF.NSN#L130-157      | Multiplicador por região (1.0 a 1.4, 27 regiões)                         |
| 10  | FATOR-FAM     | Fator Familiar                       | CALCBENF.NSN#L160-180      | Multiplicador por número de dependentes (0 a 5+)                         |
| 11  | FATOR-RND     | Fator de Renda                       | CALCBENF.NSN               | Multiplicador por faixa de renda familiar                                |
| 12  | RENDA-FAM     | Renda Familiar                       | BENEFICIARIO.NSN           | Renda total da família para cálculo de elegibilidade                     |
| 13  | COMPETENCIA   | Competência                          | PAGAMENTO.NSN              | Período de referência (AAAAMM) do pagamento                              |
| 14  | ARQ-150       | Arquivo 150                          | Adabas                     | Registro de beneficiário e dados cadastrais                              |
| 15  | ARQ-160       | Arquivo 160                          | Adabas                     | Registro de pagamento (resultado de CALCBENF+CALCCORR+CALCDSCT)          |
| 16  | ARQ-155       | Arquivo 155                          | Adabas                     | Cadastro de programas sociais (VLR-BASE, FATOR-REAJUSTE)                 |
| 17  | IPCA          | Índice de Preços ao Consumidor Amplo | CALCCORR.NSN#L40-120       | Índice de inflação utilizado para correção de pagamentos                 |
| 18  | IND-CORRIGIDO | Indicador de Corrigido               | CALCCORR.NSN               | Flag (S/N) que indica se pagamento já foi corrigido (idempotência)       |
| 19  | TIPO-DSCT     | Tipo de Desconto                     | CALCDSCT.NSN#L100-120      | Códigos: C=Contrib, I=Imposto, J=Judicial, P=Pensão, S=Sindical, A=Admin |
| 20  | TIPO-PGTO     | Tipo de Pagamento                    | CALCBENF.NSN#L45-55        | N=Normal, D=Décimo, T=Terceiro (13º salário - variável)                  |
| 21  | STATUS-PGTO   | Status do Pagamento                  | PAGAMENTO.NSN              | A=Ativo, S=Suspenso, P=Processado, C=Cancelado                           |
| 22  | VLR-ABONO     | Valor Abono                          | CALCBENF.NSN#L200-220      | Abono natalino (13º) quando aplicável                                    |
| 23  | UF            | Unidade da Federação                 | BENEFICIARIO.NSN           | Estado onde beneficiário reside (sigla 2 letras)                         |
| 24  | CALLNAT       | Call Natural                         | CALCBENF/CALCCORR/CALCDSCT | Instrução Natural de chamada de subprograma                              |
| 25  | PE            | Periodic Group                       | Adabas DDM                 | Grupo de campos que se repete (ex: múltiplos descontos)                  |
| 26  | MU            | Multi-Valued                         | Adabas DDM                 | Campo que pode armazenar múltiplos valores                               |
| 27  | VLR-TEMP      | Valor Temporário                     | Programas                  | Variável de trabalho para cálculos intermediários                        |
| 28  | DT-HOJE       | Data de Hoje                         | Programas                  | Data atual do sistema (\*DATN em Natural)                                |
| 29  | FATOR-REAJ    | Fator de Reajuste                    | PROGRAMA-SOCIAL            | Percentual de reajuste aplicado ao VLR-BASE                              |
| 30  | NUM-DEPEND    | Número de Dependentes                | BENEFICIARIO.NSN           | Quantidade de dependentes (0 a 3, limite configurável)                   |
| 31  | NUM-PROCESSO  | Número de Processo                   | CALCDSCT.NSN (PE)          | Referência judicial para descontos tipo J                                |
| 32  | DT-GERACAO    | Data de Geração                      | PAGAMENTO.NSN              | Data que o pagamento foi processado                                      |
| 33  | DT-CORRECAO   | Data de Correção                     | CALCCORR.NSN               | Data que a correção retroativa foi aplicada                              |
| 34  | VLR-DIFF      | Valor da Diferença                   | CALCCORR.NSN               | Diferença entre valor original e corrigido (para auditoria)              |

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
