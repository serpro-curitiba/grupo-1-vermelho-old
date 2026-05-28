# SIFAP 2.0 — Backend (Java 21 + Spring Boot 3.3 + Modulith)

> Estágio 3 do workshop. Implementa o MVP do bounded context **pagamentos** (REQ-PAY-001..024) + scaffold dos schemas `beneficiarios`, `programas`, `auditoria` (REQ-BEN-001, REQ-AUD-001).

## Build + testes

```powershell
cd backend
mvn -B test
```

Os testes de integração (`*IT`) usam Testcontainers — precisam de Docker rodando.

## Rodar local com Docker Compose

```powershell
cd ..
docker compose up -d --build backend postgres
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## Endpoint principal

```http
POST /api/v1/ciclos
Content-Type: application/json

{ "competencia": "202504" }
```

Respostas:

- `202 Accepted` — `{cicloId, status, totalPagamentos, valorTotal}` (REQ-PAY-001)
- `409 Conflict` — ciclo já existe para a competência (REQ-PAY-002)
- `422 Unprocessable Entity` — competência futura (REQ-PAY-003) ou formato inválido

## Endpoint de escopo ampliado

```http
GET /api/v1/ciclos/{cicloId}/resumo
```

Retorna o resumo operacional e financeiro consolidado do ciclo (totais processados,
gerados, rejeitados, ignorados, valor total, bruto, descontos, líquido, abono e 13º).

## Auditoria por ciclo

```http
GET /api/v1/auditoria/eventos?agregadoId={cicloId}
```

Consulta os eventos de auditoria relacionados ao ciclo informado.

## Rastreabilidade

Cada classe-chave referencia o REQ-ID em comentários:

- `Competencia.java` — REQ-PAY-001, REQ-PAY-003
- `CalculadoraBeneficio.java` — REQ-PAY-020..024
- `GerarCicloPagamentoUseCase.java` — REQ-PAY-001..012, REQ-PAY-020..024
- `V1__init_pagamentos.sql` — REQ-PAY-001..030, REQ-BEN-001, REQ-AUD-001

Veja a matriz completa em [`../specs/001-geracao-ciclo-pagamento/tasks.md`](../specs/001-geracao-ciclo-pagamento/tasks.md).
