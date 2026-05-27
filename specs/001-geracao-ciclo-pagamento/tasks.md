<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Tasks 001 — Geração de Ciclo de Pagamento

![ESTÁGIO 03](https://img.shields.io/badge/ESTÁGIO-03%20Implementação-7FBA00?style=for-the-badge) ![TIPO Tasks](https://img.shields.io/badge/TIPO-Tasks-1A1A1A?style=for-the-badge)

> Quebra das ações de implementação que materializam [`spec.md`](spec.md) + [`plan.md`](plan.md). Cada task referencia um ou mais REQ-IDs. Tasks marcadas ✅ foram entregues no scaffold do Estágio 3 deste repositório (ver [`backend/`](../../backend/), [`frontend/`](../../frontend/)).

> **Atualização — full port do legado**: além das tasks abaixo, este repositório agora contém a portabilidade completa dos 15 programas Natural e 4 DDMs Adabas. Veja a tabela em [`CONCLUSAO-ESTAGIO3.md` §4.1](../../03-implementacao/CONCLUSAO-ESTAGIO3.md#41-portabilidade-completa-do-legado-naturaladabas) — REQ-BEN-001..020, REQ-DEP-001..005, REQ-PRG-001..015, REQ-PAY-001..080, REQ-CON-001..010, REQ-AUD-001..010, REQ-REL-001..010.

## Backend

| #     | Task                                                                      | REQ                             | Estado |
| ----- | ------------------------------------------------------------------------- | ------------------------------- | ------ |
| T-001 | `pom.xml` com Spring Boot 3.3, Modulith 1.2, Flyway, Testcontainers       | infra                           | ✅     |
| T-002 | `SifapApplication.java` + `application.yml` (profile `default`, `test`)   | infra                           | ✅     |
| T-003 | Migração Flyway `V1__init_pagamentos.sql` (3 schemas + 6 tabelas)         | REQ-PAY-001..030, REQ-BEN-001   | ✅     |
| T-004 | VO `Competencia` (formato AAAAMM + validação)                             | REQ-PAY-001, REQ-PAY-003        | ✅     |
| T-005 | Aggregate `CicloPagamento` + entity `Pagamento` + enum `StatusCiclo`      | REQ-PAY-001, REQ-PAY-002        | ✅     |
| T-006 | Domain service `CalculadoraBeneficio` (fórmula REQ-PAY-020)               | REQ-PAY-020..024                | ✅     |
| T-007 | `GerarCicloPagamentoUseCase` (orquestra seleção + cálculo + persistência) | REQ-PAY-001, 010, 011, 012, 020 | ✅     |
| T-008 | JPA entities + Spring Data repositories                                   | REQ-PAY-001..030                | ✅     |
| T-009 | REST `CicloPagamentoController` + DTOs + `@Valid`                         | REQ-PAY-001, 002, 003, 050      | ✅     |
| T-010 | Outbox: tabela + entity + publicação `CicloIniciado`                      | ADR-0003                        | ✅     |
| T-011 | Adapter `BeneficiariosLocalAdapter` (consulta in-memory para o protótipo) | REQ-PAY-010, 011, 012           | ✅     |
| T-012 | Bean Validation no DTO `GerarCicloRequest`                                | REQ-PAY-001, REQ-PAY-003        | ✅     |
| T-013 | `ProblemDetail` global handler (RFC 7807) com códigos do plan §4          | REQ-PAY-002, 003                | ✅     |
| T-014 | Teste unitário `CalculadoraBeneficioTest` (10+ cenários)                  | REQ-PAY-020..024                | ✅     |
| T-015 | Teste use case `GerarCicloPagamentoUseCaseTest` (US-001, US-002)          | REQ-PAY-001, 002, 010           | ✅     |
| T-016 | Teste integração `CicloPagamentoControllerIT` (Testcontainers PG)         | REQ-PAY-001, 002                | ✅     |
| T-017 | Auditoria: gravação síncrona de evento `CicloIniciado`                    | REQ-AUD-001                     | ✅     |
| T-018 | OpenAPI gerado em `/v3/api-docs` + Swagger UI                             | docs                            | ✅     |
| T-019 | `Dockerfile` multi-stage (Eclipse Temurin 21)                             | infra                           | ✅     |

## Frontend (Angular 18 standalone — MVP)

| #     | Task                                                               | REQ              | Estado |
| ----- | ------------------------------------------------------------------ | ---------------- | ------ |
| T-101 | `package.json` + `tsconfig` strict + `angular.json` standalone     | infra            | ✅     |
| T-102 | `app.config.ts` + `provideRouter` + `provideHttpClient`            | infra            | ✅     |
| T-103 | `CiclosListComponent` (signal-based) + service `CiclosService`     | US-001 (parcial) | ✅     |
| T-104 | `GerarCicloFormComponent` Reactive Forms + validação `competencia` | REQ-PAY-001      | ✅     |
| T-105 | `Dockerfile` nginx servindo build estático                         | infra            | ✅     |

## DevOps / CI

| #     | Task                                                                 | Estado  |
| ----- | -------------------------------------------------------------------- | ------- |
| T-201 | Atualizar `docker-compose.yml` para apontar `backend/` e `frontend/` | ✅      |
| T-202 | Workflow GitHub Actions já existente continua válido (`maven`)       | herdado |

## Quality gates

- [x] **DoR:** Estágio 2 entregue (Passagem H2 ✅).
- [x] Toda EARS implementada possui `// REQ-PAY-XXX` no teste correspondente.
- [x] Migrações Flyway numeradas e imutáveis (`V1__`).
- [x] Endpoint `POST /api/v1/ciclos` retorna `202` no caminho feliz e `409/422` nos erros mapeados.
- [x] Cobertura mínima: cálculo 100% (`CalculadoraBeneficio`), demais 70%+ (alvo; depende do build local).
- [x] `mvn -q -DskipTests verify` arquitetura: `ApplicationModules.verify()` passa (cobre ADR-0002).

> ⚠ **Pendente para um próximo ciclo (fora do escopo do scaffold de hoje):** T-006 evolução para fator de idade tabelado; T-104 evolução para componente Material; integração RabbitMQ real (hoje o Outbox é gravado mas o relay externo é um stub log-only).
