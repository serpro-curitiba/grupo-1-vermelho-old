<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-0002: Modular Monolith com pacote por bounded context

![ADR 0002](https://img.shields.io/badge/ADR-0002-00A4EF?style=for-the-badge) ![Status Aceito](https://img.shields.io/badge/Status-Aceito-7FBA00?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../../README.md) → [Docs](../README.md) → [ADRs](README.md) → **0002**

> Adotamos um **monolito modular Spring Boot** com pacote por bounded context, em vez de microsserviços, para preservar consistência transacional e simplificar operação no Estágio 3.

| Campo      | Valor |
| ---------- | ----- |
| Status     | aceito |
| Data       | 2026-05-27 |
| Autores    | Software Architect (Par 2) |
| Substitui  | N/A |

## Contexto

O SIFAP legado roda como um conjunto de programas Natural que **compartilham 4 DDMs Adabas** (BENEFICIARIO, PAGAMENTO, PROGRAMA-SOCIAL, AUDITORIA) — sem `CALLNAT` entre eles, conforme apurado em [`01-arqueologia/dependency-map.md`](../../01-arqueologia/dependency-map.md). O acoplamento real é por **dados compartilhados**, não por API.

A modernização precisa:

- Preservar **consistência transacional** dentro de um ciclo de pagamento (`pagamentos` + `auditoria` no mesmo commit).
- Permitir **evolução por bounded context** sem que cada release pague o custo de coordenação multi-repositório.
- Rodar em equipe pequena (5 pessoas no workshop, ~15 em produção projetada).
- Suportar **Strangler Fig** — substituir lotes do legado um a um sem reescrever tudo.

Microsserviços imporiam latência de rede entre `pagamentos` e `auditoria`, exigiriam saga distribuída para REQ-PAY-052 e multiplicariam custos operacionais (15 pipelines, 15 dashboards, 15 KeyVault entries).

## Decisão

Vamos adotar **monolito modular Spring Boot 3.3 + Spring Modulith**, com a seguinte estrutura:

```
backend/
  src/main/java/br/gov/sifap/
    SifapApplication.java
    shared/                  # kernel comum: Id, Cpf, Money, eventos base
    beneficiarios/           # bounded context (módulo)
      domain/
      application/
      infrastructure/
      api/
      package-info.java      # @ApplicationModule(allowedDependencies = {"shared"})
    programas/
    fiscalizacao/
    pagamentos/
    conciliacao/
    auditoria/
    relatorios/
    portal/                  # BFF do portal-cidadao
```

Regras invioláveis:

1. **Um pacote = um bounded context.** Cross-package só por: (a) eventos de domínio publicados via Outbox, ou (b) interfaces públicas declaradas em `api/`.
2. **`@ApplicationModule(allowedDependencies = …)`** restringe quem pode importar quem — validado em testes via `ApplicationModules.verify()`.
3. **Banco PostgreSQL único**, com **schema por bounded context** (`beneficiarios.beneficiario`, `pagamentos.ciclo` etc.). Sem foreign keys entre schemas.
4. **Worker em processo separado** (mesma JVM ou container irmão) consumindo a mesma codebase com perfil `worker` — para batch (REQ-PAY-001 disparado por scheduler) e processamento de eventos.
5. **Strangler Fig:** novos REQs são implementados no módulo correspondente; programas Natural são desativados conforme o módulo cobre seus casos de uso.

## Alternativas consideradas

| Alternativa | Por que foi rejeitada |
| --- | --- |
| **Microsserviços por contexto** | Latência REST/broker dentro de fluxos transacionais (US-001, REQ-PAY-052); custo operacional desproporcional ao tamanho da equipe; saga distribuída para auditoria; observabilidade fragmentada. |
| **Monolito tradicional (uma pasta)** | Reproduz o problema do legado (acoplamento por dados); impossível impor fronteiras; cada PR vira merge conflict. |
| **Hexagonal puro sem Modulith** | Sem validação automática de fronteiras; depende de revisão humana — quebra em equipes >5. |
| **Reuso do legado via wrapper REST** | Mantém Natural/Adabas em produção indefinidamente; não cumpre objetivo do workshop. |

## Consequências

- **Mais fácil:** transações ACID dentro de um contexto; deploy único; debug end-to-end; testes de integração rápidos com Testcontainers.
- **Mais difícil:** disciplinar a equipe a respeitar fronteiras (compensado por `ApplicationModules.verify()` no CI); escalar contextos individualmente (mitigação: extrair worker separado quando um contexto crescer demais).
- **Riscos:**
  - Crescimento descontrolado → "monolito grande de novo". **Mitigação:** revisão arquitetural a cada nova feature; métrica de LOC por módulo.
  - Falha no servidor derruba todos os contextos. **Mitigação:** instâncias múltiplas atrás de load balancer; worker isolado para batch.
  - Time pode subverter regras com `@Autowired` cross-package. **Mitigação:** teste `ModulithVerification` falha o build.
- **Pronto para extração:** se um contexto crescer ou precisar de escala independente (ex.: `relatorios` com alto fan-out), o desacoplamento por eventos permite extração para serviço separado sem reescrita.

## Relacionado

- REQ-IDs: REQ-PAY-001, REQ-PAY-040, REQ-PAY-041, REQ-PAY-052
- ADRs: [ADR-0001](0001-frontend-angular.md), ADR-0003 (Outbox)
- Arquivos-fonte: [`01-arqueologia/dependency-map.md`](../../01-arqueologia/dependency-map.md) · [`specs/000-contexto/c4-l2-containers.md`](../../specs/000-contexto/c4-l2-containers.md) · [`.github/instructions/modular-monolith.instructions.md`](../../.github/instructions/modular-monolith.instructions.md)
- Referência externa: [Spring Modulith](https://spring.io/projects/spring-modulith)
