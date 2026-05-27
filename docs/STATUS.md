<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# 📊 STATUS do Dia — Dashboard de Progresso

![DASHBOARD Status](https://img.shields.io/badge/DASHBOARD-Status%20do%20dia-7FBA00?style=for-the-badge) ![ATUALIZE A cada 30 min](https://img.shields.io/badge/ATUALIZE-A%20cada%2030%20min-1A1A1A?style=for-the-badge) ![DONO Technical Lead](https://img.shields.io/badge/DONO-Technical%20Lead-737373?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Docs](README.md) → **STATUS**

> **Para quem é isto?** Para o líder do time atualizar e o facilitador ler de longe.
>
> **O que você terá ao final desta leitura:** visão de 1 página do que está verde, amarelo e vermelho agora.

> **Última reconciliação:** 2026-05-27 após merge de `develop-implementacao`, `develop-dbaqa` e `develop-arquitetura` em `develop`. Nenhum arquivo foi perdido — o commit `787f8b9 "Ajuste merge"` apenas normalizou conflitos em 3 arquivos de arqueologia.

---

## 🎯 Status global

| Indicador                  | Estado | Observação                                    |
| -------------------------- | ------ | --------------------------------------------- |
| Time inteiro presente      | ✅     | 4 de 5 pares com commits em `develop`         |
| Branch `develop` protegida | ✅     | Merges via PR (#1 + branches `develop-*`)     |
| CI verde em `develop`      | ⚪     | Verificar workflow `ci.yml` após último merge |
| Demo ensaiada              | ⚪     | Pendente — depende do Estágio 3               |

---

## 🏰 Progresso dos 4 mundos

| Estágio                  | Status          | Owner                                                          | DoD verde? | Notas                                                                                                    |
| ------------------------ | --------------- | -------------------------------------------------------------- | ---------- | -------------------------------------------------------------------------------------------------------- |
| 🟦 **1 — Arqueologia**   | ✅ pronto       | Pares 3 + 4 (entregaram); 1, 2, 5 contribuíram tangencialmente | ☑          | HARD GATE passado. Ver [`01-arqueologia/CONCLUSAO-ESTAGIO1.md`](../01-arqueologia/CONCLUSAO-ESTAGIO1.md) |
| 🟫 **2 — Spec Moderna**  | 🔄 em progresso | Par 2 (entregou esboço); Par 5 entregou ADR-005                | ☐          | 3 ADRs + C4 L1/L2 + 1 spec (`001-geracao-ciclo-pagamento`) commitados                                    |
| 🟧 **3 — Implementação** | ⏸ não começou   | Pares 3 + 4                                                    | ☐          | Sem código backend/frontend ainda                                                                        |
| 🏰 **4 — Evolução**      | 🔄 fundação     | Par 5 (DevOps)                                                 | ☐          | `docker-compose.yml`, `ci.yml`, draft Terraform já em pé                                                 |

**Legenda:** ⚪ não começou · 🔄 em progresso · ✅ pronto · ⚠️ atrasado · 🔴 bloqueado

---

## 📦 O que cada par já entregou (snapshot pós-merge)

### Par 1 · Visão (PO + RE)

- ⚪ **Sem commits próprios em `develop`** — entregas a recuperar/escalar.
- Pendência: validar mistérios CRÍTICOS (MYS-001, MYS-003, MYS-005) e produzir specs EARS com `source_legacy:`.

### Par 2 · Arquitetura (EA + SA) — branch `develop-arquitetura`

- ADRs: [`docs/adr/0001-frontend-angular.md`](../docs/adr/0001-frontend-angular.md) · [`0002-modular-monolith.md`](../docs/adr/0002-modular-monolith.md) · [`0003-outbox-broker.md`](../docs/adr/0003-outbox-broker.md)
- C4: [`specs/000-contexto/c4-l1-contexto.md`](../specs/000-contexto/c4-l1-contexto.md) · [`c4-l2-containers.md`](../specs/000-contexto/c4-l2-containers.md)
- Spec piloto: [`specs/001-geracao-ciclo-pagamento/`](../specs/001-geracao-ciclo-pagamento/) (`spec.md`, `plan.md`, `contracts/openapi.yaml`)
- Kit Copilot instalado: `enterprise-architect.agent.md`, `software-architect.agent.md` + 6 prompts + 2 skills + instructions (`backend`, `frontend`, `infrastructure`, `security`)
- Atualizou [`02-spec-moderna/scope-decisions.md`](../02-spec-moderna/scope-decisions.md)

### Par 3 · Implementação (TL + Dev) — branch `develop-implementacao`

- Arqueologia: [`01-arqueologia/PAR3-CONTRIBUICAO-ESTAGIO1.md`](../01-arqueologia/PAR3-CONTRIBUICAO-ESTAGIO1.md)
- Atualizou: `business-rules-catalog.md`, `discovery-report.md`, `glossary.md`, `mysteries-found.md`
- Programas Natural analisados: `CALCBENF.NSN`, `CALCCORR.NSN`, `CALCDSCT.NSN` (24 regras BR-BEN/BR-COR/BR-DSC)
- ⏸ Sem entregas de S3 ainda (código Java/Angular)

### Par 4 · Qualidade (DBA + QA) — branch `develop-dbaqa`

- DDM/dados: [`01-arqueologia/ddm-relational-mapping.md`](../01-arqueologia/ddm-relational-mapping.md) · [`ddm-ambiguities.md`](../01-arqueologia/ddm-ambiguities.md)
- Testes: [`01-arqueologia/test-scenarios.md`](../01-arqueologia/test-scenarios.md) (cenários por regra de negócio)
- Atualizou `business-rules-catalog.md` com regras de validação de CPF e `mysteries-found.md`

### Par 5 · Operações (DevOps + TW) — branch `develop-arquitetura` (último commit `8b6b04f`)

- ADR: [`02-spec-moderna/ADR-005-estrategia-deploy-e-infra.md`](../02-spec-moderna/ADR-005-estrategia-deploy-e-infra.md)
- Infra: estabilizou [`docker-compose.yml`](../docker-compose.yml), [`.github/workflows/ci.yml`](../.github/workflows/ci.yml), [`11-scripts/setup.sh`](../11-scripts/setup.sh), draft Terraform
- Kit Copilot instalado: `devops-engineer.agent.md`, `tech-writer.agent.md` + 6 prompts (`pipeline`, `iac-module`, `incident-rca`, `doc-drift`, `generate-docs`, `update-codemap`) + 3 skills (`iac-review`, `pipeline-hardening`, `doc-style-lint`) + `cicd.instructions.md`

---

## 🟢 Passagens (canos verdes entre mundos)

| Passagem | De → Para         | Quando           | Status                                           |
| -------- | ----------------- | ---------------- | ------------------------------------------------ |
| **H1**   | Par 1 → Par 2     | fim do Estágio 1 | ✅ implícita (Par 2 já em S2)                    |
| **H2**   | Par 2 → Pares 3+4 | fim do Estágio 2 | ⚪ pendente (spec piloto pronta, falta sign-off) |
| **H3**   | Pares 3+4 → Par 5 | fim do Estágio 3 | ⚪ não iniciada                                  |

---

## 🪙 Métricas do dia (preencher conforme avança)

| Métrica                        | Meta                  | Atual                                                                                           |
| ------------------------------ | --------------------- | ----------------------------------------------------------------------------------------------- |
| Termos no glossário            | ≥ 30                  | 30 ✅                                                                                           |
| Regras BR-NNN documentadas     | ≥ 15                  | **116 ✅** (catálogo unificado pós-merge — cobre 15/15 programas Natural)                       |
| Mistérios catalogados          | ≥ 5                   | 7 + 14 regras escondidas marcadas no código ✅                                                  |
| Cenários de teste documentados | ≥ 1 por regra crítica | ✅ ver `test-scenarios.md`                                                                      |
| REQ-IDs em EARS                | ≥ 12                  | parcial (spec `001-geracao-ciclo-pagamento`)                                                    |
| ADRs aprovadas                 | ≥ 3                   | 4 ✅ (`0001-frontend-angular`, `0002-modular-monolith`, `0003-outbox-broker`, `ADR-005-deploy`) |
| Diagramas C4                   | ≥ L1+L2               | 2 ✅                                                                                            |
| Endpoints REST funcionais      | ≥ 3                   | 0 ⏸                                                                                             |
| Cobertura backend              | ≥ 70%                 | —%                                                                                              |
| Cobertura frontend             | ≥ 60%                 | —%                                                                                              |
| Issues criadas para Agent      | ≥ 1                   | 0 ⏸                                                                                             |
| PRs mergeados                  | —                     | 4 (PR #1 + 3 merges diretos)                                                                    |

---

## 🚨 Sinais de alerta

> Preencha quando aparecer. Líder lê em voz alta no próximo stand-up.

```
- [ ] (vazio)
```

---

## 🏆 Achievements desbloqueadas

Marque conforme conquistar:

- [x] 🍄 **Primeira regra BR-NNN com `Programa Fonte`** — bem-vindo à arqueologia!
- [x] ⭐ **Primeira EARS escrita** com `source_legacy:` — `specs/001-geracao-ciclo-pagamento/spec.md`
- [x] 📜 **3 ADRs aprovadas** pelo time — `docs/adr/0001`, `0002`, `0003` (+ ADR-005)
- [ ] 🚩 **CI verde no primeiro try**
- [ ] 🦖 **Primeiro endpoint REST funcionando** via Swagger
- [ ] 🧪 **Cobertura ≥70% backend**
- [ ] 🎭 **Primeiro PR do Agent revisado e mergeado**
- [ ] 🏰 **Terraform plan sem erro** (draft existe; aplicar `validate`)
- [ ] 👸 **DEMO RODOU** — Princesa salva!

---

## 📝 Stand-ups (registre 1 frase por par a cada transição)

### H1 (fim Estágio 1)

- Par 1 · Visão: ******\*\*******\_******\*\*******
- Par 2 · Arquitetura: ******\*\*******\_******\*\*******
- Par 3 · Implementação: ******\*\*******\_******\*\*******
- Par 4 · Qualidade: ******\*\*******\_******\*\*******
- Par 5 · Operações: ******\*\*******\_******\*\*******

### H2 (fim Estágio 2)

- Par 1: ******\*\*******\_******\*\*******
- Par 2: ******\*\*******\_******\*\*******
- Par 3: ******\*\*******\_******\*\*******
- Par 4: ******\*\*******\_******\*\*******
- Par 5: ******\*\*******\_******\*\*******

### H3 (fim Estágio 3)

- Par 1: ******\*\*******\_******\*\*******
- Par 2: ******\*\*******\_******\*\*******
- Par 3: ******\*\*******\_******\*\*******
- Par 4: ******\*\*******\_******\*\*******
- Par 5: ******\*\*******\_******\*\*******

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="demo-script.md"><strong>Script da Demo</strong></a><br/>
<sub>3 minutos finais.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="CHECKLIST-LIDER.md"><strong>Checklist do Líder</strong></a><br/>
<sub>Hora a hora.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
