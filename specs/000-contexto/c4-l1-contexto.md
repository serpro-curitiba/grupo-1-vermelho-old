<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# C4 Nível 1 — Diagrama de Contexto do SIFAP 2.0

| Campo | Valor |
| --- | --- |
| Status | proposto |
| Autores | Par 2 · Arquitetura (Enterprise Architect + Software Architect) |
| Relacionado | [c4-l2-containers.md](c4-l2-containers.md) · [ADR-0001 frontend Angular](../../docs/adr/0001-frontend-angular.md) |
| Origem legado | sistema completo SIFAP (15 programas `.NSN` + 4 DDMs) |

> Diagrama de contexto agnóstico de tecnologia: mostra atores, o sistema SIFAP 2.0 e os 4 sistemas externos integrados. A decisão de stack frontend (Angular) só aparece no L2.

## Rastreabilidade legado → atores e externos

| Elemento | Tipo | `source_legacy:` |
| --- | --- | --- |
| Fiscal / Auditor | Person | 01-arqueologia/legado-sifap/natural-programs/RELAUDIT.NSN · VALBENEF.NSN · VALDOCS.NSN |
| Gestor de Benefícios | Person | 01-arqueologia/legado-sifap/natural-programs/CADBENEF.NSN · CADPROG.NSN · BATCHPGT.NSN |
| Beneficiário / Cidadão | Person | 01-arqueologia/legado-sifap/natural-programs/CONSBENF.NSN · `[GREENFIELD]` portal de autoatendimento (não existia no legado terminal) |
| SIAFI | System_Ext | 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN · 01-arqueologia/legado-sifap/legacy-docs/ARQUITETURA-ORIGINAL-1997.md (§5) · README.md (§integrações) |
| Receita Federal | System_Ext | 01-arqueologia/legado-sifap/README.md (§integrações — consulta CPF online via transação Natural / terminal 3270) · VALDOCS.NSN · VALBENEF.NSN |
| Banco do Brasil | System_Ext | 01-arqueologia/legado-sifap/natural-programs/BATCHCON.NSN (CNAB 240) · BATCHPGT.NSN (emissão de remessa) |
| CadÚnico | System_Ext | `[GREENFIELD]` — sem ocorrência no legado SIFAP; integração pretendida para enriquecer elegibilidade socioeconômica no SIFAP 2.0 |

> O portal do beneficiário e a integração com CadÚnico são marcados como **GREENFIELD** — o SIFAP legado roda apenas em terminal verde para servidores e não consulta CadÚnico. Justificativa: REQ moderno de autoatendimento cidadão + cruzamento socioeconômico, ambos sem equivalente no legado.

> **Atualização pós-arqueologia (Par 2):** a Receita Federal era consultada **online por terminal 3270** (timeout 30s), não por REST. O contrato moderno (API REST) substitui essa integração.

## Diagrama

```mermaid
C4Context
    title Diagrama de Contexto (C4 Nível 1) — SIFAP 2.0

    Person(fiscal, "Fiscal / Auditor", "Servidor que analisa pagamentos, abre apurações e homologa ciclos de benefícios.")
    Person(gestor, "Gestor de Benefícios", "Coordena a geração de ciclos de pagamento, aprova exceções e acompanha indicadores.")
    Person(beneficiario, "Beneficiário / Cidadão", "Consulta status do benefício, comprovantes e abre contestações via portal/app.")

    System(sifap, "SIFAP 2.0", "Sistema de Fiscalização e Administração de Pagamentos — cadastro de beneficiários, geração de ciclos, fiscalização, auditoria e emissão de ordens bancárias.")

    System_Ext(siafi, "SIAFI", "Sistema Integrado de Administração Financeira do Governo Federal. Recebe empenhos e ordens de pagamento.")
    System_Ext(receita, "Receita Federal", "Validação de CPF/CNPJ, situação cadastral e óbitos.")
    System_Ext(bb, "Banco do Brasil", "Processamento de ordens bancárias, retornos de crédito e devoluções.")
    System_Ext(cadunico, "CadÚnico [GREENFIELD]", "Base de dados socioeconômica usada para elegibilidade e cruzamentos. Integração nova no SIFAP 2.0.")

    Rel(fiscal, sifap, "Investiga, homologa e audita", "HTTPS / SSO")
    Rel(gestor, sifap, "Gera ciclos e aprova exceções", "HTTPS / SSO")
    Rel(beneficiario, sifap, "Consulta benefício e contesta", "HTTPS (Portal/App)")

    Rel(sifap, siafi, "Envia empenhos e ordens de pagamento", "API / arquivo batch")
    Rel(sifap, receita, "Valida CPF e situação cadastral", "API REST")
    Rel(sifap, bb, "Emite ordens bancárias e recebe retornos", "API / CNAB")
    Rel(sifap, cadunico, "Consulta elegibilidade socioeconômica", "API REST")

    UpdateLayoutConfig($c4ShapeInRow="3", $c4BoundaryInRow="1")
```

## Decisões

- **Notação:** `C4Context` nativo do Mermaid — renderiza em GitHub, VS Code preview e Spec-Kit sem plugin.
- **3 atores escolhidos:** cobrem todos os fluxos do legado (fiscalização, administração) + 1 ator novo (cidadão).
- **4 sistemas externos:** SIAFI, Receita Federal, Banco do Brasil — confirmados na arqueologia (Par 2). CadÚnico marcado como `[GREENFIELD]` (sem ocorrência no legado). Dataprev/SERPRO/TCU não foram identificados; se aparecerem na síntese, promover para externos.
- **Protocolos:** o legado usava terminal 3270 (Receita) e arquivo batch TXT (SIAFI/CNAB). O SIFAP 2.0 padroniza REST/CNAB modernos — diferença a registrar em ADR de integração.
- **Stack:** **omitida** propositalmente no L1 — escolhas técnicas vivem no L2 + ADRs.

## Pendências

- [ ] Par 1 (PO + RE) confirmar que o portal cidadão e a integração CadÚnico são REQs aprovados (não suposições arquiteturais).
- [ ] Par 2 fechar análise completa de `01-arqueologia/legado-sifap/` para garantir que não há quinto sistema externo.
- [ ] Atualizar `01-arqueologia/dependency-map.md` com os mesmos elementos para consistência.
