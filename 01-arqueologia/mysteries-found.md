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

| ID      | Descrição                                                                                                                                 | Onde Encontrado                                                                                                                              | Impacto Potencial                                                                                                                                       | Confiança |
| ------- | ----------------------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------- | --------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| MYS-001 | 13º Salário (Abono Natalino): variável #VLR-13 existe em CALCBENF mas nunca é usada no cálculo de VLR-BRUTO (sempre fica zerada)          | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L40-L60` (declaração), L200-L220 (nunca atribuição)                               | Se modernizarmos sem 13º, perderemos compatibilidade com pagamentos legais. Se incluirmos sem entender a lógica, risco de cálculo errado                | CRÍTICA   | Variável declarada mas não usada. Histórico de commit: "INC 13O SALARIO (30/11/2001)" mas código não reflete essa mudança                                                          |
| MYS-002 | Valores hardcoded 0.0000 em IPCA: junho 2010 tem IPCA=0.0000 (linha MOVE 0.0000 TO #IPCA-ANO(1,6))                                        | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L75`                                                                              | Junho 2010 teve inflação real? Se foi zero, por que? Se foi não-zero, o cálculo legado está errado, e modernização deve preservar o erro ou corrigi-lo? | ALTA      | Inflação zero não faz sentido economicamente. Verificar se é ignorância de dados ou gambiarra proposital                                                                           |
| MYS-003 | IPCA tabela obsoleta pós-2014: último ano na tabela é 2014, mas sistema ainda processa competências 2015+                                 | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L40-L120` (loop apenas até 2014)                                                  | Se processarmos pagamentos 2015+, a correção IPCA aplica fator zero (não atualiza), causando subpagamento silencioso                                    | CRÍTICA   | Tabela foi hardcoded, nunca atualizada. Histórico: "NOVOS INDICES IPCA (20/01/2006)" e último "AJUSTE PERIODO (15/08/2014)"                                                        |
| MYS-004 | Plano Verão (1989-1991) comentado no código CALCBENF mas nunca deletado                                                                   | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L210-L220` (comentário residual)                                                  | Gambiarra histórica? Artefato de versão anterior? Reflete falta de limpeza de código ou medo de deletar (falta de testes)?                              | MÉDIA     | Comentário menciona "plano de reajuste 1989-1991" como se ainda houvesse condição para aplicar, mas comentado. Pode indicar pouca confiança do desenvolvedor em seu próprio código |
| MYS-005 | Desconto Judicial (tipo J) sem limite superior: pode fazer VLR-LIQUIDO negativo                                                           | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148` (tratamento especial para tipo J)                                      | Risco de pagamentos negativos (o beneficiário deveria pagar ao governo?) é real. Se modernizar, replicar ou corrigir? Requer decisão de negócio         | CRÍTICA   | Código explicitamente exclui tipo J da verificação de limite 30%. Comentário em RN-2012 menciona "autorização legal" mas não documentada                                           |
| MYS-006 | Sindical hardcoded 1%: não parametrizado em tabela, alteração requer recompilação                                                         | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L145-L150` (MOVE 0.01 to #ALIQ-DSCT...)                                           | Se sindicatos negociarem mudança de alíquota, sistema legado não consegue responder (recompilação necessária). Modernização deve parametrizar           | MÉDIA     | Alíquota sindical foi ajustada em histórico 2015 mas aumento de 0.5% para 1% está hardcoded sem rastreamento                                                                       |
| MYS-007 | Ordem de processamento de descontos é ambígua: loop não-explicitamente-documentado em qual ordem aplica múltiplos descontos               | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L160` (FOR #IDX = 1 TO #NUM-DSCT ... mas ordem de DESCONTOS PE é indefinida) | Se ordem mudar (ex: FIFO vs. alíquota maior primeiro), resultado monetário muda. Qual ordem é legal?                                                    | ALTA      | PE (periodic group) em Adabas não garante ordem. Código Natural não ordena explicitamente. Risco: acúmulo atinge limite 30% em ordem A mas não em ordem B                          |
| MYS-008 | Flag IND-CORRIGIDO não é resetada após novo período: se beneficiário nunca recebe CALCCORR novamente (ex: suspenso), fica marcado forever | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L150-L170`                                                                        | Se beneficiário reativa, IND-CORRIGIDO precisa ser zerado? Se não for, correções futuras são puladas. Se for, quando e por quem?                        | MÉDIA     | Idempotência é boa, mas sem política de reset, pode gerar inconsistências entre beneficiários reativados                                                                           |
| MYS-009 | Número máximo de dependentes (NUM-DEPENDENTES): limite é 3 ou 5? Documentação 2012 diz 3, mas código e comentários falam em até 5         | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160` (variável declarada como N2, pode ir até 99)                                | Cálculo de fator familiar depende do limite. Se alguém tiver 4 ou 5 dependentes, como a fator é aplicado?                                               | MÉDIA     | RN-004 (2012) diz limite 3, mas comentário "Marcos Antônio mencionou" sugere historicamente pode ter sido 5. Tabelas internas definem até 5                                        |

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
