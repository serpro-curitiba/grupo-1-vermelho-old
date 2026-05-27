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

| #   | Termo | Expansão | Programa | Contexto |
| --- | ----- | -------- | -------- | -------- |
| 1 | CPF | Cadastro de Pessoas Físicas | CADBENEF; CADDEPEND | Identificador único do beneficiário |
| 2 | NIS | Número de Inscrição Social | CADBENEF | Número de inscrição no sistema social |
| 3 | RG | Registro Geral | CADBENEF | Documento de identidade complementar |
| 4 | BENEFICIARIO | Beneficiário | CADBENEF; CADDEPEND | Pessoa inscrita em programa social |
| 5 | DEPENDENTES | Dependentes | CADDEPEND | Grupo de pessoas vinculadas ao beneficiário |
| 6 | NOME | Nome | CADBENEF; CADDEPEND | Nome completo da pessoa física |
| 7 | DT-NASCIMENTO | Data de Nascimento | CADBENEF | Data de nascimento (formato AAAAMMDD) |
| 8 | SEXO | Sexo | CADBENEF; CADDEPEND | Sexo (M=Masculino, F=Feminino) |
| 9 | IDADE | Idade | CADBENEF | Idade calculada em anos; critério de elegibilidade |
| 10 | ENDERECO | Endereço | CADBENEF | Endereço residencial completo |
| 11 | MUNICIPIO | Município | CADBENEF | Cidade de residência |
| 12 | UF | Unidade Federativa | CADBENEF | Estado brasileiro (sigla 2 caracteres) |
| 13 | CEP | Código de Endereçamento Postal | CADBENEF | Código postal brasileiro (8 dígitos) |
| 14 | COD-REGIAO | Código da Região | CADBENEF | Código geográfico-administrativo da região |
| 15 | TELEFONE | Telefone | CADBENEF | Telefone de contato do beneficiário |
| 16 | RENDA-FAMILIAR | Renda Familiar | CADBENEF | Renda total mensal do núcleo familiar |
| 17 | NUM-DEPENDENTES | Número de Dependentes | CADBENEF; CADDEPEND | Quantidade de dependentes (máximo 5) |
| 18 | PROGRAMA-SOCIAL | Programa Social | CADPROG | Programa de benefícios governamental |
| 19 | COD-PROGRAMA | Código do Programa | CADBENEF; CADPROG | Código numérico único do programa |
| 20 | NOME-PROGRAMA | Nome do Programa | CADPROG | Nome descritivo do programa social |
| 21 | TIPO | Tipo de Programa | CADPROG | Classificação: A(Assistencial), P(Previdenciário), T(Trabalho) |
| 22 | VLR-BASE | Valor Base | CADPROG | Valor mensal base do benefício antes de ajustes |
| 23 | FATOR-REAJUSTE | Fator de Reajuste | CADPROG | Índice percentual de ajuste para inflação |
| 24 | FATOR-K | Fator de Correção | CADPROG | Fator multiplicador: 1.00 + (FATOR-REAJUSTE × 0.347215) |
| 25 | RENDA-MAX | Renda Máxima | CADPROG | Limite máximo de renda para elegibilidade |
| 26 | IDADE-MIN | Idade Mínima | CADPROG | Limite mínimo de idade para elegibilidade |
| 27 | IDADE-MAX | Idade Máxima | CADPROG | Limite máximo de idade para elegibilidade |
| 28 | STATUS | Status do Beneficiário | CADBENEF; CADDEPEND | Estado: A(Ativo), S(Idoso >75 anos), C(Cancelado), D(Desligado) |
| 29 | DT-CADASTRO | Data de Cadastro | CADBENEF | Data de inclusão inicial no sistema |
| 30 | DT-ATUALIZACAO | Data de Atualização | CADBENEF | Data da última alteração de dados |

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

