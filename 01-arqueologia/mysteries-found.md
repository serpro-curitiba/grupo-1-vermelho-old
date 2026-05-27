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

| Nível       | Significado                                                                        |
| ----------- | ---------------------------------------------------------------------------------- |
| **CRÍTICA** | Problema confirmado com impacto financeiro/legal direto — exige decisão de negócio |
| **ALTA**    | Temos certeza de que há algo estranho aqui                                         |
| **MÉDIA**   | Parece suspeito, mas pode ter explicação                                           |
| **BAIXA**   | Pode ser intencional, mas não conseguimos confirmar                                |

## Mistérios Catalogados

<<<<<<< HEAD
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
=======
> Mistérios consolidados das três frentes de arqueologia: cálculo (CALCBENF / CALCCORR / CALCDSCT), batch/conciliação (BATCHCON / BATCHPGT) e validação + DDMs (VALBENEF / VALDOCS / VALELEG / DDMs).

| ID      | Descrição                                                                                                                                                                                                  | Onde Encontrado                                                                                              | Impacto Potencial                                                                                                                      | Confiança |
| ------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------ | -------------------------------------------------------------------------------------------------------------------------------------- | --------- |
| MYS-001 | 13º Salário (Abono Natalino): variável `#VLR-13` declarada em CALCBENF mas nunca usada no cálculo de `VLR-BRUTO` — sempre zerada. Histórico de commit "INC 13O SALARIO (30/11/2001)" não reflete no código | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L40-L60` (declaração); L200-L220 (sem atribuição) | Se modernizarmos sem 13º, perdemos compatibilidade com pagamentos legais. Se incluirmos sem entender a lógica, risco de cálculo errado | CRÍTICA   |
| MYS-002 | Valor hardcoded `0.0000` em IPCA: junho/2010 tem `MOVE 0.0000 TO #IPCA-ANO(1,6)` sem explicação                                                                                                            | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L75`                                              | Inflação zero não faz sentido economicamente. Se for erro, modernização deve preservar (compat) ou corrigir (decisão de negócio)       | ALTA      |
| MYS-003 | Tabela IPCA obsoleta pós-2014: loop só até 2014, mas sistema ainda processa competências 2015+ → fator zero aplicado silenciosamente. Histórico: último ajuste "(15/08/2014)"                              | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L40-L120`                                         | Subpagamento silencioso de correção IPCA em todos os pagamentos a partir de 2015                                                       | CRÍTICA   |
| MYS-004 | Plano Verão (1989-1991) ainda referenciado em comentário no CALCBENF (não deletado em ~30 anos)                                                                                                            | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L210-L220`                                        | Sinal de pouca confiança/falta de testes; arrastar para a modernização polui o domínio                                                 | MÉDIA     |
| MYS-005 | Desconto Judicial (tipo J) excluído explicitamente da verificação de limite de 30% → pode tornar `VLR-LIQUIDO` negativo. Comentário menciona "autorização legal" não documentada                           | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L142-L148`                                        | Risco de pagamento negativo (beneficiário devendo ao governo). Replicar ou corrigir requer decisão de negócio                          | CRÍTICA   |
| MYS-006 | Alíquota sindical 1% hardcoded (`MOVE 0.01 TO #ALIQ-DSCT...`) — mudança requer recompilação. Histórico mostra mudança 0.5% → 1% em 2015 sem rastreamento                                                   | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L145-L150`                                        | Sistema legado não responde a negociações sindicais; modernização precisa parametrizar                                                 | MÉDIA     |
| MYS-007 | Ordem de processamento de descontos ambígua: `FOR #IDX = 1 TO #NUM-DSCT` sobre PE do Adabas (que não garante ordem). Resultado financeiro depende da ordem por causa do teto de 30%                        | `01-arqueologia/legado-sifap/natural-programs/CALCDSCT.NSN#L100-L160`                                        | Mesma entrada pode produzir resultados diferentes; modernização precisa definir ordem canônica (FIFO? alíquota maior?)                 | ALTA      |
| MYS-008 | Flag `IND-CORRIGIDO` nunca é resetada após mudança de período. Beneficiário suspenso e reativado pula correções futuras                                                                                    | `01-arqueologia/legado-sifap/natural-programs/CALCCORR.NSN#L150-L170`                                        | Inconsistência entre beneficiários reativados; necessário definir política de reset                                                    | MÉDIA     |
| MYS-009 | Limite de dependentes ambíguo: RN-004 (2012) diz 3, mas variável `NUM-DEPENDENTES` é N2 (até 99) e tabelas internas definem até 5                                                                          | `01-arqueologia/legado-sifap/natural-programs/CALCBENF.NSN#L160`                                             | Fator familiar pode ser aplicado errado para famílias com 4–5 dependentes                                                              | MÉDIA     |
| MYS-010 | Código zumbi: integração CNAB com Banco Real (descontinuada em 2007) mantida como bloco comentado "para referência histórica" desde 2005                                                                   | `01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN#L207-L225`                                        | Confusão durante migração; falso positivo de integração multi-banco em auditorias                                                      | ALTA      |
| MYS-011 | Tabela `#TAB-REG` declarada com 27 posições mas só 25 regiões documentadas. Posições 26-27 default `1.0000` nunca referenciadas                                                                            | `01-arqueologia/legado-sifap/natural-programs/BATCHPGT.NSN#L130-L151`                                        | Resíduo provável de plano de expansão; pode confundir migração da tabela de fatores regionais                                          | MÉDIA     |
| MYS-012 | Campo `FATOR-K` existe sem semântica documentada no DDM de programa social                                                                                                                                 | `01-arqueologia/legado-sifap/adabas-ddms/PROGRAMA-SOCIAL.ddm`                                                | Cálculo de benefício pode divergir do legado por desconhecimento do fator                                                              | ALTA      |
| MYS-013 | Campo `COD-PROGRAMA` em BENEFICIARIO marcado como PE no remark, mas não há estrutura PE correspondente                                                                                                     | `01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm`                                                   | Modelagem errada do vínculo programa-beneficiário (1:1 vs histórico)                                                                   | ALTA      |
| MYS-014 | CPF de dependente aceita valor sentinela `00000000000`                                                                                                                                                     | `01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm`                                                   | Integridade de dados e joins por CPF podem quebrar na migração                                                                         | ALTA      |
| MYS-015 | Programa de relatório legado filtra ações `EX` da auditoria de forma implícita                                                                                                                             | `01-arqueologia/legado-sifap/adabas-ddms/AUDITORIA.ddm`                                                      | Trilha de exclusão pode sumir na migração se reproduzirmos o filtro sem perceber                                                       | ALTA      |
| MYS-016 | Datas numéricas usam `0` para "sem prazo" em alguns campos                                                                                                                                                 | `01-arqueologia/legado-sifap/adabas-ddms/BENEFICIARIO.ddm` e `PAGAMENTO.ddm`                                 | Parse de data inválido e perda de semântica no destino (precisa virar `NULL`/Optional)                                                 | MÉDIA     |
| MYS-017 | **Easter Egg 1:** CPFs com todos os dígitos iguais começando com `000` aceitos como válidos (exceção "teste governo")                                                                                      | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L178-L188`                                        | Back door: permite emissão e pagamento para CPFs sintéticos em produção                                                                | ALTA      |
| MYS-018 | **Easter Egg 2:** Lista de 8 prefixos especiais (`000,001,002,010,011,099,100,999`) bypassa toda validação de documentos e zera contadores de erro                                                         | `01-arqueologia/legado-sifap/natural-programs/VALDOCS.NSN#L41-L51, L150-L165`                                | Back door amplo: qualquer CPF com prefixo da lista é aceito mesmo com DV errado                                                        | ALTA      |
| MYS-019 | **Easter Egg 3:** Região `99` bypassa todas as regras de elegibilidade (status, idade, renda, docs, tipo)                                                                                                  | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L90-L96`                                           | Pagamentos podem fluir sem nenhum gate quando `COD-REGIAO=99`                                                                          | ALTA      |
| MYS-020 | Fevereiro aceita até dia 29 em qualquer ano (`#DIAS-MES(2)=29` fixo, sem cálculo de bissexto)                                                                                                              | `01-arqueologia/legado-sifap/natural-programs/VALBENEF.NSN#L88-L102`                                         | Datas inválidas (ex: 29/02/2023) passam pelo legado e serão rejeitadas no destino → divergência                                        | MÉDIA     |
| MYS-021 | Valor mágico `600.00` separa elegibilidade tipo A sem constante nomeada nem ADR                                                                                                                            | `01-arqueologia/legado-sifap/natural-programs/VALELEG.NSN#L152-L160`                                         | Limite financeiro escondido — não rastreável a política externa                                                                        | MÉDIA     |
>>>>>>> develop

## Detalhamento dos Mistérios

### MYS-010: Código zumbi — integração Banco Real descontinuada (2007)

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

### MYS-011: Tabela `#TAB-REG` com 27 posições, mas só 25 regiões documentadas

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

1. [x] Easter Egg 1 — **MYS-017**: CPF com todos iguais e prefixo `000` é aceito (`VALBENEF.NSN`)
2. [x] Easter Egg 2 — **MYS-018**: 8 prefixos especiais bypassam validação de docs (`VALDOCS.NSN`)
3. [x] Easter Egg 3 — **MYS-019**: Região 99 bypassa toda elegibilidade (`VALELEG.NSN`)

## Resumo

- Total de mistérios encontrados: 21
- Confiança crítica: 3
- Confiança alta: 10
- Confiança média: 8
- Confiança baixa: 0
- Easter eggs encontrados: 3 / 3 (MYS-017, MYS-018, MYS-019)

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
