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
| MYS-001 | Campo FATOR-K existe sem semântica documentada no DDM de programa social | 01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm | Cálculo de benefício pode divergir do legado | ALTA |
| MYS-002 | Campo COD-PROGRAMA em BENEFICIARIO está marcado como PE no remark, mas não há estrutura PE correspondente | 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm | Modelagem errada de vínculo programa-beneficiário (1:1 vs histórico) | ALTA |
| MYS-003 | CPF de dependente aceita valor sentinela 00000000000 | 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm | Integridade de dados e joins por CPF podem quebrar | ALTA |
| MYS-004 | Programa de relatório legado filtra ações EX da auditoria | 01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm | Trilha de exclusão pode sumir na migração se reproduzir filtro implícito | ALTA |
| MYS-005 | Datas numéricas usam 0 para “sem prazo” em alguns campos | 01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm e PAGAMENTO.ddm | Parse de data inválido e perda de semântica no destino | MÉDIA |
| MYS-006 | CPFs com todos os dígitos iguais começando com `000` são aceitos como válidos (exceção "teste governo") | 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L178-L188 | Back door: permite emissão e pagamento para CPFs sintéticos em produção | ALTA |
| MYS-007 | Lista de 8 prefixos especiais (`000,001,002,010,011,099,100,999`) bypassa toda validação de documentos e zera contadores de erro | 01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L41-L51, L150-L165 | Back door amplo: qualquer CPF com prefixo da lista é aceito mesmo com DV errado | ALTA |
| MYS-008 | Região 99 bypassa todas as regras de elegibilidade (status, idade, renda, docs, tipo) | 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L90-L96 | Pagamentos podem fluir sem nenhum gate quando COD-REGIAO=99 | ALTA |
| MYS-009 | Fevereiro aceita até dia 29 em qualquer ano (tabela #DIAS-MES(2)=29 fixa, sem cálculo de bissexto) | 01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L88-L102 | Datas inválidas (ex: 29/02/2023) passam pelo legado e serão rejeitadas no destino → divergência | MÉDIA |
| MYS-010 | Valor mágico `600.00` separa elegibilidade tipo A sem qualquer constante nomeada nem ADR | 01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L152-L160 | Limite financeiro escondido — não rastreável a política externa | MÉDIA |

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

1. [x] Easter Egg 1: CPF com todos iguais e prefixo `000` é aceito (VALBENEF.NSN)
2. [x] Easter Egg 2: 8 prefixos especiais bypassam validação de docs (VALDOCS.NSN)
3. [x] Easter Egg 3: Região 99 bypassa toda elegibilidade (VALELEG.NSN)

## Resumo

- Total de mistérios encontrados: 10
- Confiança alta: 7
- Confiança média: 3
- Confiança baixa: 0
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

