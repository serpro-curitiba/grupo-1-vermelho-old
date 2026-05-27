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

| ID     | Regra de Negócio | Programa Fonte | Campos DDM | Nível de Risco | Notas |
| ------ | ---------------- | -------------- | ---------- | -------------- | ----- |
| BR-001 | Operação de beneficiário deve ser I (Inclusão) ou A (Alteração) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L119-L124 | #OPER | CRÍTICO | Validação de entrada obrigatória |
| BR-002 | CPF é obrigatório para qualquer operação | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L126-L131 | CPF | CRÍTICO | Chave única do beneficiário |
| BR-003 | CPF deve ser válido conforme algoritmo módulo-11 com 2 dígitos verificadores | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L267-L318 | CPF | CRÍTICO | Algoritmo específico do SIFAP |
| BR-004 | Nome é obrigatório | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L133-L138 | NOME | CRÍTICO | Identificação do beneficiário |
| BR-005 | Data de nascimento é obrigatória (formato AAAAMMDD) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L140-L145 | DT-NASCIMENTO | CRÍTICO | Base para cálculo de elegibilidade |
| BR-006 | Sexo deve ser M (Masculino) ou F (Feminino) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L147-L152 | SEXO | CRÍTICO | Validação de domínio |
| BR-007 | Em INCLUSÃO (I): beneficiário não deve existir previamente | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L154-L164 | CPF | CRÍTICO | Impede duplicação; rejeita com erro |
| BR-008 | Em ALTERAÇÃO (A): beneficiário deve existir na base | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L166-L171 | CPF | CRÍTICO | Impede alteração de registro fantasma |
| BR-009 | Cálculo da idade: (ano-atual - ano-nascimento), com verificação de mês/dia | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L173-L177 | DT-NASCIMENTO, #IDADE | MÉDIO | Usado para determinação de status |
| BR-010 | Status inicial padrão para novo beneficiário é 'A' (Ativo) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L179-L182 | STATUS | MÉDIO | Transição de estado ao incluir |
| BR-011 | Beneficiários com idade > 75 anos recebem status 'S' (Suspenso) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L184-L187 | #IDADE, STATUS | ALTO | **Regra com limite sem justificação desde 2011** |
| BR-012 | Em INCLUSÃO: DT-CADASTRO e DT-ATUALIZACAO recebem data do sistema (#DT-HOJE) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L194-L208 | DT-CADASTRO, DT-ATUALIZACAO | MÉDIO | Auditoria de mudanças |
| BR-013 | Em ALTERAÇÃO: CPF, DT-NASCIMENTO, SEXO, DT-CADASTRO são imutáveis (read-only) | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN#L210-L228 | CPF, DT-NASCIMENTO, SEXO | ALTO | Garante integridade histórica |
| BR-014 | Máximo de 5 dependentes por beneficiário titular | 01-arqueologia/legado-sifap/natural-programs/CADDEPEND.NSN#L65-L68 | NUM-DEPENDENTES | CRÍTICO | Limite de elegibilidade para dependentes |
| BR-015 | Cálculo de valor base de programa ajustado: VLR-CALC = VLR-BASE * (1.00 + FATOR-REAJ * 0.347215) | 01-arqueologia/legado-sifap/natural-programs/CADPROG.NSN#L81-L82 | VLR-BASE, FATOR-REAJ, VLR-CALC | CRÍTICO | Constante mágica 0.347215 sem comentário explicativo |

> Adicione mais linhas conforme necessário. Lembre-se: existem **10 regras escondidas** no código!

## Exemplo de linha bem preenchida

| ID     | Regra de Negócio                                                                        | Programa Fonte                                   | Campos DDM                                                               | Nível de Risco | Notas                                      |
| ------ | --------------------------------------------------------------------------------------- | ------------------------------------------------ | ------------------------------------------------------------------------ | -------------- | ------------------------------------------ |
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

