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

| ID           | Regra de Negócio                                                                                                         | Programa Fonte                                                                               | Campos DDM                                                             | Nível de Risco | Notas                                                                                |
| ------------ | ------------------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------- | -------------- | ------------------------------------------------------------------------------------ |
| BR-BEN-001   | Cálculo do valor bruto: VLR-BRUTO = VLR-BASE × FATOR-REGIONAL × FATOR-FAMILIAR × FATOR-RENDA                             | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L130-L200`                        | ARQ-150 (BENEFICIARIO), ARQ-155 (PROGRAMA-SOCIAL), ARQ-160 (PAGAMENTO) | CRÍTICO        | Regra financeira central. VLR-BASE vem de ARQ-155, fatores estão em tabelas internas |
| BR-BEN-002   | Fator regional tem 27 valores (1 para cada região/UF), variando de 1.0 a 1.4                                             | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L130-L157`                        | ARQ-150 (COD-REGIAO), ARQ-160 (VLR-BRUTO)                              | CRÍTICO        | Tabela #TAB-REG(1..27) hardcoded no programa. Valores refletem economia regional     |
| BR-BEN-003   | Fator familiar (progressivo): 1.0 + (NUM-DEPENDENTES × 0.15), máximo 5 dependentes                                       | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160-L180`                        | ARQ-150 (NUM-DEPENDENTES), ARQ-160 (VLR-BRUTO)                         | CRÍTICO        | Limite de 3–5 dependentes é variável (MYS-004: não confirmado em código)             |
| BR-BEN-004   | Fator de renda (5 faixas): <500=1.2, 500-1000=1.0, 1000-2000=0.9, 2000-3000=0.8, >3000=0.7                               | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L165-L185`                        | ARQ-150 (RENDA-FAMILIAR), ARQ-160 (VLR-BRUTO)                          | CRÍTICO        | Faixas estabelecem "foco" em população de baixa renda                                |
| BR-COR-001   | Correção retroativa usa juros compostos: VLR-CORRIGIDO = VLR-ORIGINAL × ∏(1 + IPCA[mês])                                 | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L60-L100`                         | ARQ-160 (VLR-BRUTO, VLR-CORRECAO, DT-CORRECAO)                         | CRÍTICO        | Cálculo de juros compostos preserva poder de compra histórico                        |
| BR-COR-002   | Tabela IPCA é carregada por ano, índices mensais (2010-2014), valor zero = sem correção                                  | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L40-L120`                         | ARQ-160 (VLR-BRUTO)                                                    | CRÍTICO        | MYS-003: tabela obsoleta após 2014; dados posteriores não documentados               |
| BR-COR-003   | Flag IND-CORRIGIDO previne correção duplicada (idempotência): se 'S', pula cálculo                                       | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L150-L170`                        | ARQ-160 (IND-CORRIGIDO, VLR-CORRECAO)                                  | CRÍTICO        | Proteção contra reprocessamento; essencial para auditoria                            |
| BR-COR-004   | Período de correção é definido por competência mês-a-mês, acumulação no campo VLR-CORRECAO                               | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L85-L95`                          | ARQ-160 (COMPETENCIA, VLR-CORRECAO)                                    | ALTO           | Cada mês processado independentemente, depois acumulado                              |
| BR-DSC-001   | Limite de desconto total é 30% do VLR-BRUTO, EXCETO tipo J (judicial) que não tem limite                                 | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148`                        | ARQ-150 (DESCONTOS PE), ARQ-160 (VLR-DESCONTO)                         | CRÍTICO        | MYS-005: judicial ilimitado cria risco de "desconto negativo" (aumento)              |
| BR-DSC-002   | Tipos de desconto: C=Contribuição (3-9%), I=Imposto, J=Judicial (ilimitado), P=Pensão, S=Sindical (1%), A=Administrativo | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L120`                        | ARQ-150 (DESCONTOS.TIPO-DSCT), ARQ-160 (VLR-DESCONTO)                  | CRÍTICO        | 6 tipos de desconto com alíquotas diferentes; S=Sindical é hardcoded 1%              |
| BR-DSC-003   | Cálculo desconto por alíquota: VLR-DSCT-ITEM = VLR-BRUTO × PCT-DSCT / 100, acumulado em VLR-TOTAL-DSCT                   | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L130-L140`                        | ARQ-150 (DESCONTOS.PCT-DSCT), ARQ-160 (VLR-DESCONTO)                   | ALTO           | Simples multiplicação; limite de 30% valida no final                                 |
| BR-DSC-004   | Desconto sindical é valor fixo 1% (não parametrizado), aplica sempre que beneficiário é sindicalizado                    | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L145-L150`                        | ARQ-150 (DESCONTOS.TIPO-DSCT=S)                                        | MÉDIO          | MYS-006: hardcoded 1% sem forma de alterar; recomendação: parametrizar em S3         |
| BR-DSC-005   | Descontos tipo J (judicial) são ilimitados e podem fazer pagamento ficar negativo (regra de negócio não-intuitiva)       | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148`                        | ARQ-150 (DESCONTOS.TIPO-DSCT=J), ARQ-160 (VLR-LIQUIDO)                 | CRÍTICO        | MYS-005: comportamento perigoso; recomendação: documentar política de negócio        |
| BR-DSC-006   | Ordem de processamento de descontos afeta resultado se houver limite; MYS-007: ordem ambígua no código                   | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L160`                        | ARQ-150 (DESCONTOS PE), ARQ-160 (VLR-DESCONTO)                         | ALTO           | Loop acumulativo; se ordem mudar, resultado muda. Docs não definem prioridade        |
| BR-GERAL-001 | Cascata de execução obrigatória: BATCHPGT → CALCBENF → CALCCORR → CALCDSCT → ARQ-160 (resultado final)                   | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN`                                  | ARQ-160 (PAGAMENTO)                                                    | CRÍTICO        | Fluxo não-alterável; cada etapa depende da anterior                                  |
| BR-GERAL-002 | Todos os valores monetários são armazenados em formato numérico com 2 casas decimais (N9.2)                              | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L1-L50` (definição de DATA LOCAL) | ARQ-160 (VLR-BRUTO, VLR-LIQUIDO, etc.)                                 | MÉDIO          | Convenção de precisão financeira; crítica para auditoria e reconciliação             |
| BR-GERAL-003 | Beneficiário ativo (STATUS='A') é pré-requisito; beneficiários excluídos (E) e suspensos (S) não recebem pagamento       | `01-arqueologia/legado-sifap/legacy-docs/REGRAS-NEGOCIO-2012.md#RN-002`                      | ARQ-150 (STATUS), ARQ-160 (VLR-LIQUIDO)                                | CRÍTICO        | Validação na entrada de CALCBENF; pula cálculo se não ativo                          |

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

## Resumo Estatístico

- Total de regras encontradas: \_\_\_
- Regras críticas: \_\_\_
- Regras com duplicação: \_\_\_
- Regras sem documentação (escondidas): \_\_\_

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
