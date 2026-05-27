<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Decisões de Escopo — SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Worksheet](https://img.shields.io/badge/TIPO-Worksheet-1A1A1A?style=for-the-badge) ![PREENCHA Durante S2](https://img.shields.io/badge/PREENCHA-Durante%20S2-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **Scope Decisions**

> **Para quem é isto?** Este é um **artefato preenchido pelo time** durante o Estágio 2 (Spec Moderna).
>
> **O que você terá ao final do estágio:**
>
> 1. Este documento preenchido para sua feature
> 2. Rastreabilidade `source_legacy:` para cada REQ-ID
> 3. Sign-off do Product Owner antes da passagem H2
>
> 📘 **Guia passo a passo:** [`GUIDE.md`](GUIDE.md).


> Para cada funcionalidade encontrada no Estágio 1, decida: **Migrar**, **Descartar** ou **Evoluir**.
>
> - **Migrar**: trazer para o SIFAP 2.0 como está (mesma lógica, nova tecnologia)
> - **Descartar**: não trazer — funcionalidade obsoleta ou desnecessária
> - **Evoluir**: trazer E melhorar (nova UX, novo fluxo, nova capacidade)

**Time**: Grupo 1 · Vermelho
**Data**: 27/05/2026
**Edição**: Workshop SIFAP 2.0
**Par 1 (Product Owner) responsável**: [a preencher]
**Par 2 (Arquitetura) co-autor**: EA + SA

## Por que isso importa

O escopo é o que protege o time de chegar às 17h00 com 12 features pela metade. Se o Par 1 não cortar, o Estágio 3 não fecha. **Decisão difícil é tomada aqui, não no Estágio 3.**

## Como decidir

Pergunte de cada funcionalidade:

1. **Afeta o ciclo mensal de pagamento?** Sim → Migrar. Não → considere descartar.
2. **Tem uso documentado nos últimos 12 meses?** Não → descartar.
3. **Faz parte de um relatório regulatório obrigatório (TCU, CGU, BB)?** Sim → Migrar como está.
4. **Tem uma versão moderna mais barata de implementar?** Sim → Evoluir.

---

## Decisões por Funcionalidade

| #   | Funcionalidade            | Decisão  | Justificativa | Regra de Negócio (BR-XXX) | Prioridade |
| --- | ------------------------- | -------- | ------------- | ------------------------- | ---------- |
| 1   | Cadastro de Beneficiários (CADBENEF.NSN) | Migrar | Entidade core do domínio; CRUD direto em PostgreSQL preserva CPF/UF/dependentes. | BR-001, BR-002 | Alta |
| 2   | Cadastro de Dependentes (CADDEPEND.NSN) | Migrar | Insumo do fator familiar (BR-005). Pode ir como sub-recurso do beneficiário. | BR-005 (via FATOR-FAM) | Alta |
| 3   | Cadastro de Programa Social (CADPROG.NSN) | Migrar | Necessário para validação ativa/inativa e VLR-BASE. | BR-004 | Alta |
| 4   | Consulta de Beneficiários (CONSBENF.NSN) | Evoluir | Migra como REST + UI Angular com filtros. Substitui terminal 3270. | — | Alta |
| 5   | Geração mensal de pagamentos (BATCHPGT.NSN) | Migrar | **Caso de uso âncora** do Estágio 3 — REQ-PAY-001..052 da spec 001. Preservar comportamento bit-a-bit. | BR-001..BR-006 | Alta |
| 6   | Cálculo de benefícios (CALCBENF.NSN) | Migrar | Núcleo financeiro; 5 fatores (regional, familiar, renda, idade, reajuste). Testes de equivalência obrigatórios. | BR-005 | Alta |
| 7   | Cálculo de correção (CALCCORR.NSN) | Migrar | Atualização monetária de pagamentos atrasados. | a catalogar pelo Par 3 | Média |
| 8   | Cálculo de descontos (CALCDSCT.NSN) | Migrar | Regra do teto de 30% com exceção judicial (exemplo de referência do GUIDE). | BR-013 (Par 3) | Alta |
| 9   | Validação CPF (VALDOCS.NSN) | Evoluir | Substituir algoritmo manual por biblioteca validada + integração futura com Receita REST. | — | Média |
| 10  | Validação de elegibilidade (VALELEG.NSN) | Migrar | Regras de status e programa ativo (BR-001, BR-004). | BR-001, BR-004 | Alta |
| 11  | Conciliação CNAB 240 (BATCHCON.NSN) | Migrar | Integração crítica com Banco do Brasil. Layout CNAB preservado. | BR-007, BR-008 | Alta |
| 12  | Integração Banco Real (BATCHCON.NSN#L207-L225) | **Descartar** | Código zumbi desde 2007 (Santander adquiriu o Banco Real). Ver MYS-001 e BR-012. | BR-012 | — |
| 13  | Relatórios consolidados mensais (BATCHREL.NSN) | Migrar | Aceito pelo TCU há 23 anos; mudar layout requer aprovação externa. | BR-009, BR-010, BR-011 | Alta |
| 14  | Relatório de pagamentos por período (RELPGT.NSN) | Evoluir | UI Angular + export CSV/PDF. Migrar regras de agrupamento. | — | Média |
| 15  | Trilha de auditoria (RELAUDIT.NSN + AUDITORIA.ddm) | Migrar | Append-only obrigatório; base de evidência LGPD/TCU. | BR-008 | Alta |
| 16  | Tabela `#TAB-REG` posições 26-27 | **Descartar** | Sem documentação e sem uso. Ver MYS-002. | BR-006 | — |
| 17  | Spool de impressão mainframe | **Descartar** | Substituído por geração PDF/CSV sob demanda. | — | — |
| 18  | Autenticação por terminal 3270 | **Descartar** | Substituído por OAuth2/JWT + RBAC (REQ-PAY-051). | — | — |

> Adicione linhas para cada funcionalidade identificada no `discovery-report.md` do Estágio 1.

---

## Funcionalidades Novas (não existem no legado)

> Liste funcionalidades que o SIFAP 2.0 deveria ter e que não existem no sistema legado. Cada uma vira REQ-ID com `source_legacy: [GREENFIELD] <justificativa>`.

| #   | Funcionalidade Nova | Justificativa | Prioridade | Complexidade |
| --- | ------------------- | ------------- | ---------- | ------------ |
| N1  | Autenticação OAuth2/OIDC via Gov.br (REQ-PAY-051) | LGPD + substituição do auth de terminal 3270; integração com Entra ID já contemplada na ADR-0001. | Alta | Média |
| N2  | Mascaramento de CPF em logs (REQ-PAY-052) | Conformidade LGPD; não existia em 1997. | Alta | Baixa |
| N3  | Outbox + broker para eventos `CicloGerado`/`PagamentoCriado` (ADR-0003) | Substitui acoplamento por DDM compartilhado por mensageria assíncrona inter-módulos. | Alta | Média |
| N4  | Idempotency-Key em POST `/ciclos` (REQ-PAY-002) | API moderna exige guarda explícita; o batch confiava no operador. | Alta | Baixa |
| N5  | Métricas/observabilidade (Micrometer + Application Insights) | Não existia no mainframe; obrigatório para SRE. | Média | Baixa |
| N6  | Frontend Angular 18+ standalone (ADR-0001) | Substitui terminal 3270; UX moderna com RBAC. | Alta | Alta |

---

## Resumo de Escopo

| Decisão   | Quantidade | Percentual |
| --------- | ---------- | ---------- |
| Migrar    | 11         | 61%        |
| Descartar | 4          | 22%        |
| Evoluir   | 3          | 17%        |
| **Total** | 18         | 100%       |

> Greenfield (N1..N6): 6 funcionalidades novas adicionais aos 18 itens do legado.

## Riscos de Escopo

> Liste os riscos das decisões tomadas:

| Risco | Probabilidade | Impacto | Mitigação |
| ----- | ------------- | ------- | --------- |
| Migrar BATCHPGT bit-a-bit pode estourar a janela do Estágio 3 (3h). | Média | Alto | Cortar para apenas REQ-PAY-001..006 no MVP; demais REQs viram backlog. |
| Cálculo dos 5 fatores (BR-005) divergir entre Java e Natural. | Alta | Crítico | Testes de equivalência (golden-files) com 50+ casos reais antes do merge. |
| Conciliação CNAB 240 sem ambiente real do Banco do Brasil. | Alta | Médio | Mockar com arquivos CNAB de exemplo (`legacy-docs/`); integração real fica para fase pós-MVP. |
| Layout do relatório TCU mudar inadvertidamente em `BATCHREL` modernizado. | Baixa | Crítico | Teste de regressão visual + assinatura do PO antes do merge. |
| Descartar tabela `#TAB-REG[26-27]` (MYS-002) pode quebrar caso TCU pergunte. | Baixa | Baixo | Documentar a decisão em ADR-0004 (pendente). |

## Aprovação

- [ ] Par 1 (Product Owner) aprovou as decisões de escopo
- [ ] Par 2 (Enterprise Architect) validou a viabilidade técnica
- [ ] Par 3 (Technical Lead) confirmou que cabe nas 3 horas do Estágio 3
- [ ] Time concordou com as prioridades

> **Aprovação obrigatória na Passagem #2** (~16:00). Sem ela, o Estágio 3 não começa.

— Paula


---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template de ADR.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
