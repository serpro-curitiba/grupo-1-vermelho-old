<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Mistérios Encontrados — SIFAP Legado

![ESTÁGIO 01 Arqueologia](https://img.shields.io/badge/ESTÁGIO-01%20Arqueologia-F25022?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S1](https://img.shields.io/badge/PREENCHA-Durante%20S1-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 1](README.md) → **mysteries-found**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 1 (Arqueologia).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento totalmente preenchido com os dados reais do legado SIFAP
> 2. Rastreabilidade para `01-arqueologia/legado-sifap/` (programas `.NSN` e DDMs)
> 3. Base de evidência usada nas EARS do Estágio 2 (`source_legacy:`)
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Registre aqui toda lógica, comportamento ou código que o time não conseguiu explicar.
> "Mistérios" são trechos de código sem documentação, com lógica não-óbvia ou que parecem workarounds.
>
> **Cota mínima para passar pelo portão do Estágio 2:** 5 mistérios documentados.

## O que conta como "mistério"?

- Código que faz algo inesperado sem comentário explicando por quê
- Valores hardcoded sem explicação (números mágicos)
- Lógica condicional que parece um workaround ou gambiarra
- Campos no DDM que não são usados por nenhum programa
- Programas que existem mas não são chamados por ninguém
- Comportamento diferente entre o que a documentação diz e o que o código faz
- Easter eggs deixados pelos desenvolvedores originais

## Níveis de Confiança

| Nível     | Significado                                         |
| --------- | --------------------------------------------------- |
| **ALTA**  | Temos certeza de que há algo estranho aqui          |
| **MÉDIA** | Parece suspeito, mas pode ter explicação            |
| **BAIXA** | Pode ser intencional, mas não conseguimos confirmar |

## Mistérios Catalogados

| ID      | Descrição | Onde Encontrado | Impacto Potencial | Confiança |
| ------- | --------- | --------------- | ----------------- | --------- |
| MYS-001 | Status = 'S' atribuído para beneficiários > 75 anos, mas significado nunca é documentado | CADBENEF.NSN#L160-L162 | Risco: status 'S' pode significar aposentado, inválido ou elegibilidade especial sem clareza | aberto |
| MYS-002 | Campo RENDA-FAMILIAR coletado e armazenado, mas NUNCA é validado ou usado em comparações | CADBENEF.NSN#L52,171,223 | Risco: dados fantasma; possível bug silencioso se elegibilidade depende de renda | aberto |
| MYS-003 | Constante mágica 0.347215 hardcoded em fórmula de cálculo, sem comentário explicativo | CADPROG.NSN#L88 | Risco: cálculos financeiros; constante pode ser inflação, índice perdido em 23 anos | aberto |
| MYS-004 | DT-FIM do programa pode ser 0 (indeterminado), mas sistema não trata em comparações de validade | CADPROG.NSN#L70 | Risco: queries futuras compareando datas podem falhar silenciosamente | aberto |
| MYS-005 | Limite máximo 5 dependentes por beneficiário sem explicação da origem ou justificativa | CADDEPEND.NSN#L35 | Risco: por quê 5? Limite legal, técnico ou histórico? Afeta elegibilidade | aberto |
| MYS-006 | Campos CPF, DT-NASCIMENTO, SEXO, COD-PROGRAMA imutáveis em atualização; regra nunca documentada | CADBENEF.NSN#L187-L230 | Risco: migrations viram armadilhas; imutabilidade deve ser tratada em modernização | aberto |
| MYS-007 | DEPENDENTES podem ter parentesco 'OU' (outro), sem validação contra tabela de valores válido | CADDEPEND.NSN#L58-L59 | Risco: 'OU' é indefinido; quem são? Tutores? Regras de elegibilidade viram ambíguas | aberto |
| MYS-008 |                  |                 |                   |           |
| MYS-009 |                  |                 |                   |           |
| MYS-010 |                  |                 |                   |           |

## Detalhamento dos Mistérios

### MYS-001: [Título do Mistério]

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/ARQUIVO.NSN#L<inicio>-L<fim>`
- **Trecho de código**:

```natural
* Cole aqui o trecho relevante
```

- **O que esperávamos**: [comportamento esperado]
- **O que o código faz**: [comportamento real]
- **Hipótese do time**: [melhor palpite]
- **Risco se ignorarmos**: [o que pode dar errado na migração]

---

> Copie o bloco acima para cada mistério encontrado.

## Easter Eggs

> Dica: existem **3 easter eggs** escondidos no código legado. Registre aqui os que encontrar:

1. [ ] Easter Egg 1: \_\_\_
2. [ ] Easter Egg 2: \_\_\_
3. [ ] Easter Egg 3: \_\_\_

## Resumo

- Total de mistérios encontrados: \_\_\_
- Confiança alta: \_\_\_
- Confiança média: \_\_\_
- Confiança baixa: \_\_\_
- Easter eggs encontrados: \_\_\_ / 3

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="mysteries-checklist.md"><strong>mysteries-checklist.md</strong></a><br/>
<sub>Lista do que procurar.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="discovery-report.md"><strong>discovery-report.md</strong></a><br/>
<sub>Síntese final.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="README.md">Voltar ao Kit PT-BR</a></sub>

