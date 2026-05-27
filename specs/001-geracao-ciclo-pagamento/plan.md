<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Plan 001 — Geração de Ciclo de Pagamento Mensal

![ESTÁGIO 02 Spec Moderna](https://img.shields.io/badge/ESTÁGIO-02%20Spec%20Moderna-FFB900?style=for-the-badge) ![TIPO Plan](https://img.shields.io/badge/TIPO-Plan-1A1A1A?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Specs](../README.md) → [001](README.md) → **plan**
>
> Plano técnico para implementar [`spec.md`](spec.md). Gerado pelo Software Architect (Par 2), pronto para Technical Lead (Par 3) quebrar em tarefas.

## 1. Arquitetura alvo

- **Pattern:** Modular Monolith ([ADR-0002](../../docs/adr/0002-modular-monolith.md))
- **Comunicação:** Outbox + broker ([ADR-0003](../../docs/adr/0003-outbox-broker.md))
- **Stack:** Java 21 + Spring Boot 3.3 + Spring Modulith 1.2 + PostgreSQL 16 + RabbitMQ (local) / Azure Service Bus (prod)

## 2. Módulo `pagamentos` — estrutura proposta

```
backend/src/main/java/br/gov/sifap/pagamentos/
├── package-info.java                    # @ApplicationModule(allowedDependencies = {"shared", "beneficiarios::api", "programas::api"})
├── domain/
│   ├── CicloPagamento.java              # Aggregate root
│   ├── Pagamento.java                   # Entity dentro do aggregate
│   ├── Competencia.java                 # VO (AAAAMM)
│   ├── StatusCiclo.java                 # enum: INICIADO, CALCULANDO, CALCULADO, EMITIDO, CANCELADO
│   ├── ComposicaoValor.java             # VO: fatores + valorBase + valorFinal
│   ├── CalculadoraBeneficio.java        # Domain service (regras REQ-PAY-020..024)
│   └── eventos/
│       ├── CicloIniciado.java
│       ├── PagamentoCalculado.java
│       └── CicloDuplicadoBloqueado.java
├── application/
│   ├── GerarCicloPagamentoUseCase.java
│   ├── ConsultarCicloUseCase.java
│   └── ConsultarPagamentoUseCase.java
├── infrastructure/
│   ├── persistence/
│   │   ├── CicloPagamentoJpaEntity.java
│   │   ├── PagamentoJpaEntity.java
│   │   ├── CicloPagamentoRepository.java
│   │   └── OutboxEventEntity.java
│   ├── beneficiarios/
│   │   └── BeneficiariosLocalAdapter.java   # adapter para o módulo beneficiarios::api
│   └── programas/
│       └── ProgramasLocalAdapter.java
└── api/
    ├── CicloPagamentoController.java
    ├── PagamentoController.java
    └── dto/
```

## 3. Modelo de dados (resumido — detalhe em [`data-model.md`](data-model.md))

| Tabela | Schema | PK | Observações |
|---|---|---|---|
| `ciclo_pagamento` | `pagamentos` | `id UUID` | `competencia VARCHAR(6) UNIQUE WHERE status<>'CANCELADO'` (REQ-PAY-002) |
| `pagamento` | `pagamentos` | `id UUID` | `num_pagto BIGSERIAL UNIQUE` (REQ-PAY-030); FK lógica para `ciclo_pagamento.id` |
| `outbox_event` | `pagamentos` | `id UUID` | Padrão ADR-0003 |
| `beneficiario_snapshot` | `pagamentos` | `(ciclo_id, beneficiario_id)` | Imutável por ciclo (suporta REQ-PAY-031) |

> Schema `pagamentos` é isolado; sem FK física para outros schemas (ADR-0002).

## 4. Contratos REST (resumido — detalhe em [`contracts/openapi.yaml`](contracts/openapi.yaml))

| Verbo | Path | REQ | Resposta sucesso |
|---|---|---|---|
| POST | `/api/v1/ciclos` | REQ-PAY-001 | `202 Accepted` `{cicloId, status}` |
| GET | `/api/v1/ciclos/{id}` | US-003 | `200 OK` `{ciclo + agregados}` |
| GET | `/api/v1/ciclos?competencia=AAAAMM` | — | `200 OK` lista |
| GET | `/api/v1/pagamentos/{id}` | US-003 | `200 OK` com `ComposicaoValor` |

Erros padronizados: `application/problem+json` (RFC 7807). Códigos:

- `409 ciclo.duplicado` (REQ-PAY-002)
- `422 competencia.futura` (REQ-PAY-003)
- `422 competencia.formato-invalido`
- `403 sem-permissao` (REQ-PAY-050)

## 5. Eventos publicados

| Tópico | Evento | Schema (v1) |
|---|---|---|
| `sifap.pagamentos.ciclo.v1` | `CicloIniciado` | `{cicloId, competencia, totalCandidatos, iniciadoEm}` |
| `sifap.pagamentos.pagamento.v1` | `PagamentoCalculado` | `{pagamentoId, cicloId, cpfMascarado, valorFinal, competencia, calculadoEm}` |
| `sifap.pagamentos.ciclo.v1` | `CicloDuplicadoBloqueado` | `{competencia, cicloExistenteId, tentadoEm, requisitanteId}` |

Consumidores conhecidos: `auditoria` (todos os eventos), `relatorios` (CalculadoEvent), `portal` (CalculadoEvent — atualiza read-model).

## 6. Estratégia de testes

| Camada | Ferramenta | Cobertura mínima |
|---|---|---|
| Unidade (domínio) | JUnit 5 + AssertJ | 100% de `CalculadoraBeneficio` (REQ-PAY-020..024) |
| Unidade (use case) | JUnit 5 + Mockito | Cenários US-001/002 |
| Módulo (Modulith) | `ApplicationModules.verify()` | Sem dependências fora do permitido |
| Integração | Spring Boot Test + Testcontainers (PG + RabbitMQ) | Cenários Gherkin do `spec.md` |
| Contrato | Spring Cloud Contract / Pact | Eventos `v1` |
| Equivalência legacy | Teste comparativo com fixture dos 80 ativos | Diff zero vs. saída esperada de `BATCHPGT.NSN` |

> **Equivalência legacy** é específica desta feature por ser conversão de regras 1:1.

## 7. Decisões abertas para Par 3

- [ ] Estratégia para preservar `numPagto` contínuo na migração (sequence começa em `MAX(legado.NUM-PAGTO)+1`).
- [ ] Confirmar REQ-PAY-031 com sistemas downstream — se irrelevante, abandonar ordenação determinística.
- [ ] Investigar `#TAB-REG[26..27]` antes de implementar REQ-PAY-021 (abrir MYS-002 se confirmado).
- [ ] Definir TTL de `beneficiario_snapshot` (sugestão: manter para sempre — base de auditoria).

## 8. Riscos

| Risco | Impacto | Mitigação |
|---|---|---|
| Cálculo divergir do legado em casos de borda | Alto | Teste de equivalência com 1.000+ fixtures reais antes do go-live |
| Outbox lag em pico de geração mensal | Médio | Bulk insert + relay com batch size 200; alerta de lag |
| Snapshot de beneficiário ficar desatualizado | Baixo | Documentar: snapshot é o "como estava na geração"; auditoria garante rastreabilidade |
| `num_pagto` colidir na migração | Alto | Script de migração reserva range; sequence inicia acima do max legado |

## 9. Cronograma (estimado — 3 dias de Par 3)

1. **Dia 1:** estrutura de pacotes + JPA entities + repositories + testes de módulo verde.
2. **Dia 2:** `CalculadoraBeneficio` 100% testada + `GerarCicloPagamentoUseCase` + integração com Outbox.
3. **Dia 3:** REST controllers + OpenAPI + testes de integração Testcontainers + teste de equivalência.

## 10. Definição de pronto

- [ ] Todos os REQ-PAY-* têm pelo menos 1 teste rastreado por comentário `// REQ-PAY-XXX`.
- [ ] `ApplicationModules.verify()` passa.
- [ ] `mvn verify` verde com Testcontainers.
- [ ] OpenAPI gerado bate com `contracts/openapi.yaml`.
- [ ] CI `legacy-traceability` aprova (todos os EARS têm `source_legacy:`).
- [ ] Documentação `quickstart.md` permite QA validar US-001.
