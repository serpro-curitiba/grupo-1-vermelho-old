<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-INDEX — Decisões Arquiteturais do SIFAP 2.0

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO Index](https://img.shields.io/badge/TIPO-Index-1A1A1A?style=for-the-badge)

> Índice consolidado das ADRs vigentes do SIFAP 2.0 (Estágio 2). Cumpre o requisito do [`GUIDE.md`](GUIDE.md) de **3 a 5 ADRs**.

## Inventário

| #    | ADR                                                               | Decisão                                                 | Status | Owner          | REQ-IDs ligados                                        |
| ---- | ----------------------------------------------------------------- | ------------------------------------------------------- | ------ | -------------- | ------------------------------------------------------ |
| 001  | [ADR-0001 Frontend Angular](../docs/adr/0001-frontend-angular.md) | Angular 18+ standalone + signals (substitui Next.js)    | aceito | Par 2 (SA)     | REQ-PAY-051 (HttpOnly cookie), todos os REQs do portal |
| 002a | [ADR-0002 Modular Monolith](../docs/adr/0002-modular-monolith.md) | Pacote por bounded context com Spring Modulith          | aceito | Par 2 (SA)     | todos                                                  |
| 002b | [ADR-002 Migração de Dados](ADR-002-migracao-dados.md)            | Strangler Fig com CDC (Adabas → PostgreSQL)             | aceito | Par 4 (DBA)    | REQ-PAY-030, REQ-PAY-031, REQ-CON-001, REQ-AUD-001     |
| 003a | [ADR-0003 Outbox + Broker](../docs/adr/0003-outbox-broker.md)     | Padrão Outbox + RabbitMQ (local) / Service Bus (prod)   | aceito | Par 2 (SA)     | REQ-PAY-040, REQ-PAY-041, REQ-PAY-052                  |
| 003b | [ADR-003 Auth](ADR-003-autenticacao-e-autorizacao.md)             | Entra ID + gov.br federados via OAuth2/OIDC + RBAC fino | aceito | Par 5 + Par 2  | REQ-PAY-050, REQ-PAY-051, REQ-PAY-052, REQ-AUD-002     |
| 005  | [ADR-005 Deploy + Infra](ADR-005-estrategia-deploy-e-infra.md)    | Docker Compose local + CI básico + Terraform draft      | aceito | Par 5 (DevOps) | REQ-OPS-001, REQ-OPS-002                               |

> **Nota de numeração:** o repositório carrega duas séries históricas — `docs/adr/0001-0003` (kit base) e `02-spec-moderna/ADR-002, ADR-003, ADR-005` (decisões deste workshop). Ambas vigentes. Próximas ADRs (`ADR-004`, `ADR-006`+) usarão a série `02-spec-moderna/` por padrão.

## Como navegar

- **Decisão de stack frontend?** → [ADR-0001](../docs/adr/0001-frontend-angular.md)
- **Decisão de estilo arquitetural (mono vs micro)?** → [ADR-0002](../docs/adr/0002-modular-monolith.md)
- **Como migrar os 4 DDMs Adabas?** → [ADR-002](ADR-002-migracao-dados.md)
- **Como publicar eventos sem 2PC?** → [ADR-0003](../docs/adr/0003-outbox-broker.md)
- **Como autenticar servidores + cidadãos + workers?** → [ADR-003](ADR-003-autenticacao-e-autorizacao.md)
- **Como rodar local e em CI?** → [ADR-005](ADR-005-estrategia-deploy-e-infra.md)

## Como criar uma nova ADR

1. Copie [`ADR-TEMPLATE.md`](ADR-TEMPLATE.md) para `ADR-NNN-titulo-curto.md` (use o próximo número disponível na série).
2. Preencha **contexto, opções consideradas (mínimo 2), decisão, justificativa, consequências (positivas + negativas + riscos)**.
3. Ligue a ADR a REQ-IDs em [`SPECIFICATION.md`](SPECIFICATION.md) §6.
4. Acrescente uma linha neste índice.
5. Abra PR e marque revisores: Par 2 (SA) + um par afetado pela decisão.

## Decisões que ainda não viraram ADR (backlog do Estágio 3+)

| Tema                                                | Quando virar ADR                               |
| --------------------------------------------------- | ---------------------------------------------- |
| UI kit (Angular Material vs PrimeNG)                | Antes da primeira tela em produção (Estágio 3) |
| Estratégia de cache (Redis vs Caffeine local)       | Quando p95 de leitura > 200 ms                 |
| Política de retenção de eventos no broker           | Antes do go-live em produção                   |
| Mecanismo de feature flag (Unleash vs LaunchDarkly) | Quando primeira feature precisar toggle        |

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="SPECIFICATION.md"><strong>SPECIFICATION</strong></a><br/>
<sub>Spec consolidada do Estágio 2.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="ADR-TEMPLATE.md"><strong>ADR-TEMPLATE</strong></a><br/>
<sub>Template para próximas decisões.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
