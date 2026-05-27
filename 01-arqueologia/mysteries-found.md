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
| MYS-001 | Código zumbi: integração Banco Real (descontinuada em 2007) comentada em vez de removida | `BATCHCON.NSN#L207-L225` | Confusão durante migração; falso positivo de integração multi-banco | ALTA |
| MYS-002 | Tabela `#TAB-REG` declarada com 27 posições mas só 25 regiões documentadas | `BATCHPGT.NSN#L130-L151` | Posições 26-27 com fator 1.0000 nunca usadas; possível resíduo de plano de expansão | MÉDIA |
| MYS-003 |           |                 |                   |           |
| MYS-004 |           |                 |                   |           |
| MYS-005 |           |                 |                   |           |
| MYS-006 |           |                 |                   |           |
| MYS-007 |           |                 |                   |           |
| MYS-008 |           |                 |                   |           |
| MYS-009 |           |                 |                   |           |
| MYS-010 |           |                 |                   |           |

## Detalhamento dos Mistérios

### MYS-001: Código zumbi — integração Banco Real descontinuada (2007)

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L207-L225`
- **Trecho de código**:

```natural
* --------------------------------------------------------
* INTEGRACAO BANCO REAL - DESCONTINUADA
* BANCO REAL FOI ADQUIRIDO PELO SANTANDER EM 2007
* MANTER CODIGO PARA REFERENCIA HISTORICA
* RESPONSAVEL: MARCOS RIBEIRO - 18/09/2005
* --------------------------------------------------------
*  DEFINE WORK FILE 2 'RETORNO_REAL.DAT' TYPE 'ASCII'
*  READ WORK FILE 2 #REG-CNAB
*    MOVE SUBSTR(#REG-CNAB,1,3) TO #CNAB-BANCO
*    IF #CNAB-BANCO NE '356'   /* COD BANCO REAL */
*      ESCAPE TOP
*    END-IF
*    /* LAYOUT BANCO REAL DIFERENTE DO BB */
*    PERFORM CONCILIA-REAL
*  END-WORK
```

- **O que esperávamos**: conciliação CNAB apenas com Banco do Brasil (CNAB 240), conforme cabeçalho do programa (L10) e ADR original de 1997.
- **O que o código faz**: mantém um bloco inteiro comentado referenciando o código bancário `356` (Banco Real) com layout CNAB diferente. O comentário admite que o banco foi adquirido pelo Santander em 2007 e o código permanece "para referência histórica".
- **Hipótese do time**: migração para Santander nunca foi finalizada; o autor optou por comentar o bloco em vez de remover. Há também uma subrotina `CONCILIA-REAL` (não encontrada no fonte atual) referenciada — possível resíduo de copybook removido.
- **Risco se ignorarmos**: (1) ao reescrever a conciliação no SIFAP 2.0, alguém pode interpretar o código zumbi como requisito de integração multi-banco; (2) se houver auditoria sobre integrações ativas, isso aparece como falso positivo; (3) sinaliza débito técnico de processo — código morto persistido por 18+ anos.
- **Confiança**: **ALTA**

---

### MYS-002: Tabela `#TAB-REG` com 27 posições, mas só 25 regiões documentadas

- **Arquivo**: `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L130-L151`
- **Sintoma**: Array `#TAB-REG` declarado com 27 elementos. Os `MOVE`s subsequentes preenchem posições 1-25 com fatores regionais distintos (0.95 a 1.10). Posições 26 e 27 ficam com valor default `1.0000` e não são referenciadas em nenhum lugar do programa, nem em BATCHCON, nem em BATCHREL.
- **Hipóteses do time**:
  1. Reserva para estados/regiões criados após 1988 (Tocantins, ou novas regiões administrativas) que nunca foram cadastrados.
  2. Provisão para "região especial" (Brasília? exterior?) que ficou abandonada após reestruturação organizacional.
  3. Easter egg / erro de declaração nunca corrigido pelo autor original.
- **Impacto se ignorarmos**: BAIXO — basta migrar como tabela de 25 entradas. Mas convém registrar a decisão de remover as posições 26-27 caso o TCU questione em auditoria.
- **Investigação sugerida**: procurar `01-arqueologia/legado-sifap/legacy-docs/` por menção a "região 26"/"região 27"; entrevistar autor original (Carlos Roberto da Silva, autor desde 1997).
- **Confiança**: **MÉDIA**

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

