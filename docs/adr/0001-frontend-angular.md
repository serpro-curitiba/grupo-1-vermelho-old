<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-0001: Adotar Angular como framework de frontend (substituindo Next.js)

> Resumo: o portal SIFAP 2.0 será construído em Angular 18+ (standalone + signals + SSR opcional via Angular Universal). Decisão substitui a referência anterior a Next.js 15 em `.github/copilot-instructions.md` e nas instruções de frontend.

| Campo | Valor |
| --- | --- |
| Status | aceito |
| Data | 2026-05-27 |
| Autores | Par 2 · Arquitetura (Enterprise Architect + Software Architect) |
| Substitui | N/A (primeira decisão de stack de frontend) |

## Contexto

A versão preliminar do kit de instruções (`.github/copilot-instructions.md`, `.github/instructions/frontend-spec.instructions.md`) declarava **Next.js 15 App Router** como framework de frontend. Durante o planejamento do C4 L2 do SIFAP 2.0 ([c4-l2-containers.md](../../specs/000-contexto/c4-l2-containers.md)), o time decidiu alinhar o portal à stack corporativa preferida (Angular + Java/Spring), com os seguintes drivers:

- Os 3 atores (fiscal, gestor, beneficiário) consomem majoritariamente formulários transacionais, listas com filtros e dashboards — sweet-spot de Angular com Reactive Forms + Material.
- O backend é Spring Boot 3.3; manter "Java + Angular" reduz o número de stacks que a equipe precisa dominar.
- A equipe possui mais experiência prévia com Angular do que com React/Next.js.
- SSR/SEO não é requisito do portal interno; quando necessário (portal cidadão), Angular Universal cobre.

## Decisão

Vamos adotar **Angular 18+** como framework de frontend para o container `web` (Portal SIFAP), com as seguintes diretrizes não-negociáveis:

- **Standalone components** (sem `NgModule`) por padrão.
- **Signals** para state local; RxJS apenas para fluxos assíncronos.
- **Angular Router** com lazy loading por bounded context.
- **TypeScript strict** (`strict: true`, sem `any`).
- **SSR via Angular Universal** opcional — habilitar apenas no portal cidadão se houver requisito de SEO ou first-paint.
- **Auth** via `@angular/oauth2-oidc` (ou `angular-auth-oidc-client`) integrado ao Azure Entra ID + gov.br.
- **HTTP** sempre via `HttpClient` com interceptors para JWT e correlação.
- **Testes** com Jest + Testing Library Angular (substituem Karma+Jasmine).
- **UI kit:** Angular Material (primeira escolha) ou PrimeNG; decisão deixada para próximo ADR.

Não vamos usar Next.js, React, Vite-SPA, Vue ou Svelte. Não vamos misturar frameworks no portal.

## Alternativas consideradas

| Alternativa | Por que foi rejeitada |
| ----------- | --------------------- |
| Manter Next.js 15 App Router | Equipe tem menos experiência; mistura React + Java aumenta carga cognitiva; server components/actions agregam complexidade sem ganho claro para um portal majoritariamente autenticado e transacional. |
| React SPA puro (Vite + React Router) | Falta opinião pronta para forms, i18n, lazy-loading e DI — teríamos que costurar bibliotecas. Angular já entrega tudo isso. |
| Blazor (Server ou WASM) | Reduziria a 1 linguagem (C#), mas o backend é Java; introduzir .NET no front é mais custoso que o ganho. |

## Consequências

- **Mais fácil:**
  - Padronização de stack (Java + Angular) e onboarding.
  - Estrutura opinionada (CLI, DI, schematics) acelera scaffold.
  - Forms complexos (fiscalização, cadastro) ficam mais simples com Reactive Forms.
- **Mais difícil:**
  - SSR exige Angular Universal e cuidado com hidratação — só ativar quando preciso.
  - Bundle inicial maior que Next.js; mitigar com lazy loading agressivo.
  - Pesquisar talento Angular sênior no mercado é um pouco mais raro que React.
- **Riscos:**
  - O protótipo em `prototype/frontend/` (se existir) precisa ser reescrito ou descartado.
  - Instruções `.github/instructions/frontend*.instructions.md` ficam inconsistentes até serem reescritas.
- **Mitigações:**
  - Reescrever [`.github/instructions/frontend-spec.instructions.md`](../../.github/instructions/frontend-spec.instructions.md) na mesma PR desta ADR.
  - Atualizar `.github/copilot-instructions.md` (seção Stack-Alvo e Regras de Geração de Código).
  - Atualizar [`docker-compose.yml`](../../docker-compose.yml) (serviço `frontend` passa a apontar para build Angular).
  - Manter o protótipo Next.js em branch `legacy/prototype-nextjs` apenas para referência visual.

## Relacionado

- C4 L2: [specs/000-contexto/c4-l2-containers.md](../../specs/000-contexto/c4-l2-containers.md)
- Instruções: [.github/copilot-instructions.md](../../.github/copilot-instructions.md) · [.github/instructions/frontend-spec.instructions.md](../../.github/instructions/frontend-spec.instructions.md)
- Compose: [docker-compose.yml](../../docker-compose.yml)
- REQ-IDs: a serem ligados em `specs/000-contexto/` quando as primeiras specs do portal forem criadas.

## Referências

- [Angular 18 — Release notes](https://blog.angular.io/)
- [Angular Universal (SSR)](https://angular.dev/guide/ssr)
- [Signals](https://angular.dev/guide/signals)
- [angular-auth-oidc-client](https://github.com/damienbod/angular-auth-oidc-client)
