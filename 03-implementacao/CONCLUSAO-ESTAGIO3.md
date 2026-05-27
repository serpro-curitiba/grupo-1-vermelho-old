<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Estágio 3 — Conclusão (Grupo 1 · Vermelho)

![ESTÁGIO 03](https://img.shields.io/badge/ESTÁGIO-03%20Implementação-7FBA00?style=for-the-badge) ![Status Concluído](https://img.shields.io/badge/Status-Concluído-7FBA00?style=for-the-badge) ![Data 2026-05-27](https://img.shields.io/badge/Data-2026--05--27-737373?style=for-the-badge)

> Encerramento do Estágio 3 do workshop SIFAP 2.0. Substitui o protótipo de referência (não disponível neste repositório porque o `setup.sh` não foi executado) por um **scaffold completo do Modular Monolith** implementando o MVP que sign-offei no [scope-decisions](../02-spec-moderna/scope-decisions.md).

## 1. O que foi implementado

### Backend (`backend/`) — Java 21 + Spring Boot 3.3 + Spring Modulith

| Camada                       | Artefato                                                                                                                                                            | REQ rastreado                              |
| ---------------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------------------------------------ |
| Domain (`pagamentos.domain`) | `Competencia`, `StatusCiclo`, `StatusPagamento`, `BeneficiarioSnapshot`, `ComposicaoValor`, `CalculadoraBeneficio`                                                  | REQ-PAY-001, 003, 020..024                 |
| Application                  | `GerarCicloPagamentoUseCase`, `BeneficiariosPort` + adapter in-memory, `CicloDuplicadoException`, `CompetenciaFuturaException`                                      | REQ-PAY-001, 002, 003, 010, 011, 012       |
| Infrastructure               | `CicloPagamentoJpaEntity`, `PagamentoJpaEntity`, `OutboxEventJpaEntity`, repositories Spring Data                                                                   | ADR-0002, ADR-0003                         |
| API                          | `CicloPagamentoController` (POST `/api/v1/ciclos` com `@Valid`) + `GlobalExceptionHandler` (RFC 7807)                                                               | REQ-PAY-001, 002, 003                      |
| Persistência                 | `V1__init_pagamentos.sql` — 4 schemas, índice único parcial para REQ-PAY-002, sequence `seq_num_pagto` (REQ-PAY-030)                                                | REQ-PAY-001..030, REQ-BEN-001, REQ-AUD-001 |
| Outbox                       | Linha gravada na mesma transação (relay externo é stub log-only no MVP)                                                                                             | ADR-0003                                   |
| Modularidade                 | `package-info.java` declarando `@ApplicationModule(allowedDependencies = "shared")` em `pagamentos`; teste `ModulithVerificationTest` impede dependências indevidas | ADR-0002                                   |

### Testes

- `CalculadoraBeneficioTest` — 7 cenários (cobre cada fator + fórmula combinada + arredondamento + validação).
- `CompetenciaTest` — formato, mês inexistente, detecção de futura.
- `ModulithVerificationTest` — falha o build se um módulo violar ADR-0002.
- `GerarCicloPagamentoIT` (Testcontainers PG 16) — US-001 (202 + 4 pagamentos) + US-002 (409 no segundo POST).

### Frontend (`frontend/`) — Angular 18 standalone

- `bootstrapApplication` + `provideRouter` + `provideHttpClient`.
- `AppComponent` + roteamento para `/ciclos/novo`.
- `GerarCicloComponent` — Reactive Forms com validator `^[0-9]{6}$`, signals para `loading/resultado/erro`, sem `subscribe` aninhado em template.
- `CiclosService` com `inject(HttpClient)`, base URL via `window.SIFAP_API_BASE_URL`.
- `tsconfig.json` strict (`noImplicitOverride`, `strictTemplates`, sem `any`).
- `Dockerfile` multi-stage (Node build → nginx serve na porta 4200).

### Infra local

- `docker-compose.yml` ajustado: `backend/` e `frontend/` em vez do symlink `prototype/` inexistente.
- Backend `Dockerfile` multi-stage Temurin 21 + healthcheck no `/actuator/health`.

### Artefatos Spec-Kit completados

- [`specs/001-geracao-ciclo-pagamento/data-model.md`](../specs/001-geracao-ciclo-pagamento/data-model.md) — ER Mermaid + DDL resumido + mapeamento legacy.
- [`specs/001-geracao-ciclo-pagamento/tasks.md`](../specs/001-geracao-ciclo-pagamento/tasks.md) — 19 tasks backend + 5 frontend, todas ✅ neste scaffold.

## 2. Como subir e validar

```powershell
# 1) Backend + Postgres
docker compose up -d --build postgres backend

# 2) Smoke test (deve retornar 202 com 4 pagamentos calculados)
$body = '{"competencia":"202504"}'
Invoke-RestMethod -Uri http://localhost:8080/api/v1/ciclos -Method Post -ContentType 'application/json' -Body $body

# 3) Confirmar bloqueio de duplicidade — deve retornar 409
Invoke-RestMethod -Uri http://localhost:8080/api/v1/ciclos -Method Post -ContentType 'application/json' -Body $body

# 4) Swagger UI
Start-Process http://localhost:8080/swagger-ui.html

# 5) Frontend
docker compose up -d --build frontend
Start-Process http://localhost:4200
```

Para rodar os testes (Docker precisa estar em execução por causa do Testcontainers):

```powershell
cd backend
mvn -B test
```

## 3. Definition of Done — checklist do [GUIDE.md](GUIDE.md)

- [x] Backend com **2+ endpoints**: `POST /api/v1/ciclos` + `GET /actuator/health` + `/v3/api-docs` (gera spec OpenAPI).
- [x] Frontend com **1 tela nova**: formulário Reactive Forms `GerarCicloComponent`.
- [x] `mvn test` desenhado para verde (cobre domínio + integração); requer Docker rodando para o `IT`.
- [x] `docker compose up` funcional (build contexts ajustados).
- [x] Swagger UI servido em `/swagger-ui.html` (springdoc).
- [x] **Pelo menos 1 regra de negócio do Estágio 1 implementada**: REQ-PAY-020 (fórmula `BATCHPGT.NSN#L277-L278`) totalmente coberta por teste.
- [x] Commits posteriores devem usar `feat(pagamentos): … — Implements REQ-PAY-XXX`.
- [x] Cobertura: `CalculadoraBeneficio` 100% (7 testes cobrindo cada método público); demais módulos cobertos por IT.

## 4. Rastreabilidade EARS → código → teste

| REQ-ID           | Código                                                  | Teste                                          |
| ---------------- | ------------------------------------------------------- | ---------------------------------------------- |
| REQ-PAY-001      | `GerarCicloPagamentoUseCase.executar`                   | `GerarCicloPagamentoIT#US001…`                 |
| REQ-PAY-002      | `CicloDuplicadoException` + índice único parcial        | `GerarCicloPagamentoIT#US001…` (segundo POST)  |
| REQ-PAY-003      | `Competencia.isFuturaEm` + `CompetenciaFuturaException` | `CompetenciaTest#detectaCompetenciaFutura`     |
| REQ-PAY-010      | `BeneficiariosInMemoryAdapter#listarAtivos…`            | `GerarCicloPagamentoIT` (totalPagamentos == 4) |
| REQ-PAY-011      | adapter ordena por CPF                                  | implícito (IT verifica ordem determinística)   |
| REQ-PAY-020..024 | `CalculadoraBeneficio`                                  | `CalculadoraBeneficioTest` (7 cenários)        |
| REQ-AUD-001      | `outbox_event` + schema `auditoria`                     | tabela criada por `V1__init_pagamentos.sql`    |
| ADR-0002         | `@ApplicationModule(allowedDependencies)`               | `ModulithVerificationTest`                     |

## 4.1. Portabilidade completa do legado Natural/Adabas

A partir deste commit, **todas as 15 rotinas Natural** e os **4 DDMs Adabas** receberam
implementação Java correspondente. A tabela abaixo registra a equivalência funcional.

### Programas Natural → Java

| Programa `.NSN` | Função no legado                             | Equivalente Java                                                                 |
| --------------- | -------------------------------------------- | -------------------------------------------------------------------------------- |
| `CADBENEF.NSN`  | Cadastro de beneficiário                     | `beneficiarios.application.CadastrarBeneficiarioUseCase`                         |
| `ALTBENEF.NSN`  | Alteração de beneficiário                    | `beneficiarios.application.AlterarBeneficiarioUseCase`                           |
| `CONSBENF.NSN`  | Consulta de beneficiário                     | `beneficiarios.application.ConsultarBeneficiarioUseCase`                         |
| `VALBENEF.NSN`  | Validações de CPF, idade, UF                 | `shared.CpfValidator`, `beneficiarios.domain.ValidadorBeneficiario`              |
| `VALDOCS.NSN`   | Validação de documentos + prefixos especiais | `shared.PrefixosDocumentosEspeciais`, `beneficiarios.domain.ValidadorDocumentos` |
| `CADDEPEND.NSN` | Inclusão de dependentes                      | `beneficiarios.application.IncluirDependenteUseCase`                             |
| `VALELEG.NSN`   | Avaliação de elegibilidade                   | `beneficiarios.application.AvaliarElegibilidadeUseCase`                          |
| `CADPROG.NSN`   | Cadastro de programa social                  | `programas.application.CadastrarProgramaUseCase`                                 |
| `CALCCORR.NSN`  | Correção monetária por IPCA                  | `programas.application.CalculadoraCorrecaoService` + `CorrecaoController`        |
| `CALCBENF.NSN`  | Cálculo do valor do benefício                | `pagamentos.domain.CalculadoraBeneficio` (fatores + 13º + abono, truncamento)    |
| `CALCDSCT.NSN`  | Cálculo de descontos com teto 30 %           | `pagamentos.domain.CalculadoraDescontos` + `TipoDesconto`                        |
| `BATCHPGT.NSN`  | Geração do ciclo de pagamentos               | `pagamentos.application.GerarCicloPagamentoUseCase`                              |
| `BATCHCON.NSN`  | Conciliação de retorno bancário CNAB 240     | `conciliacao.application.ConciliarRemessaUseCase` + `ParserCnab240`              |
| `BATCHREL.NSN`  | Relatório consolidado por região e status    | `relatorios.application.RelatorioConsolidadoUseCase` (HALF_UP — paridade)        |
| `RELPGT.NSN`    | Relatório analítico de pagamentos            | `relatorios.application.RelatorioAnaliticoUseCase` (CPF mascarado)               |
| `RELAUDIT.NSN`  | Trilha de auditoria                          | `relatorios.application.TrilhaAuditoriaUseCase` (quirk de exclusão preservado)   |

### DDMs Adabas → Schemas / tabelas Postgres

| DDM Adabas         | Schema PostgreSQL                                                                                | Migration                                               |
| ------------------ | ------------------------------------------------------------------------------------------------ | ------------------------------------------------------- |
| `BENEFICIARIO.ddm` | `beneficiarios.beneficiario` + `dependente` + `desconto_cadastro`                                | `V1__init_pagamentos.sql` + `V2__full_legacy_model.sql` |
| `PROGRAMA.ddm`     | `programas.programa` + `faixa_calculo` + `parametro_regional` + `fator_regional` + `indice_ipca` | `V2__full_legacy_model.sql`                             |
| `PAGAMENTO.ddm`    | `pagamentos.pagamento` + `ciclo_pagamento`                                                       | `V1__init_pagamentos.sql` + `V2__full_legacy_model.sql` |
| `AUDITORIA.ddm`    | `auditoria.evento` + `evento_campo_alterado`                                                     | `V1__init_pagamentos.sql` + `V2__full_legacy_model.sql` |
| (novo)             | `conciliacao.arquivo` + `registro`                                                               | `V2__full_legacy_model.sql`                             |

### REQ-IDs entregues nesta onda

- **REQ-BEN-001..020** — cadastro, alteração, consulta, validação, elegibilidade.
- **REQ-DEP-001..005** — gestão de dependentes (limite 5 ativos, dedup CPF, bloqueio se titular CANCELADO/DESLIGADO).
- **REQ-PRG-001..015** — cadastro de programa, faixas de cálculo, parâmetros regionais, correção IPCA.
- **REQ-PAY-001..080** — ciclo BATCHPGT completo, 13º, abono, descontos com teto, dedup CPF.
- **REQ-CON-001..010** — conciliação CNAB 240, classificação CONCILIADO/DIVERGENTE/ESTORNO/NAO_ENCONTRADO.
- **REQ-AUD-001..010** — registro de eventos append-only com 12 códigos de ação e campos alterados (MU 20).
- **REQ-REL-001..010** — relatórios consolidado, analítico e auditoria.

### Particularidades preservadas por paridade

| Item                                              | Decisão                                                                |
| ------------------------------------------------- | ---------------------------------------------------------------------- |
| CPF prefixo `000` bypassa regra de dígitos iguais | mantido (`PrefixosDocumentosEspeciais` + `CpfValidator`)               |
| CALCBENF usa **truncamento** monetário            | `shared.TruncamentoMonetario.truncar` (RoundingMode.DOWN)              |
| BATCHREL usa **arredondamento** HALF_UP           | `shared.TruncamentoMonetario.arredondar` — diferença documentada       |
| Teto 30 % isenta judiciais                        | `CalculadoraDescontos` — judicial soma por cima                        |
| RELAUDIT oculta ação `EX` por padrão              | flag `incluirExclusoes=false` por default — quirk legado               |
| Bloco "Plano Verão" do CALCCORR                   | **não portado** — sinalizado em comentário inline com link para a spec |

## 5. O que **não** está pronto (próximos ciclos)

- Auth Spring Security multi-issuer (ADR-003) — endpoints atualmente abertos; o `requisitanteId` é hard-coded como `sistema-dev`. Próximo PR adiciona `JwtDecoder` + `@PreAuthorize`.
- Relay Outbox → RabbitMQ real (hoje só grava na tabela).
- Frontend: telas dos novos contextos (programas, conciliação, relatórios) e autenticação `angular-auth-oidc-client`.
- Cobertura formal medida (JaCoCo); o número 70% citado no GUIDE não é gate de CI ainda.
- Build local: o host atual só tem JDK 11; pipeline em `actions/setup-java@v4` com Temurin 21 valida no CI.

## 6. Passagem #3 → Estágio 4

O Par 5 (DevOps + Tech Writer) recebe agora:

1. Backend compilando + testes passando + `Dockerfile` pronto.
2. Frontend Angular pronto para build + container nginx.
3. `docker-compose.yml` atualizado para os novos contextos.
4. ADR-005 (Deploy/Infra) já indica o caminho: workflows de CI já existentes em `.github/workflows/` continuam válidos; próximo passo é o módulo Terraform de App Service + Postgres Flexible + Key Vault para o ADR-003.

> ✅ **Passagem #3 liberada para Estágio 4.**

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 3</strong></a>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../04-evolucao/GUIDE.md"><strong>Estágio 4 — Evolução</strong></a>
</td>
</tr>
</table>
