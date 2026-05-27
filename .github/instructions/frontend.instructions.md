<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

---
applyTo: "**/src/app/**,**/*.component.ts,**/*.service.ts"
---

# Convenções de Frontend (resumo)

> Detalhes completos em [`frontend-spec.instructions.md`](frontend-spec.instructions.md) e [ADR-0001](../../docs/adr/0001-frontend-angular.md).

- Stack: **Angular 18+ standalone + signals + TypeScript strict**.
- Componentes: standalone, `ChangeDetectionStrategy.OnPush`, injeção via `inject()`, testes colocalizados.
- State: `signal`/`computed` local; services com signals para state compartilhado; RxJS apenas para streams assíncronos.
- Forms: **Reactive Forms** (`fb.nonNullable.group`); template-driven proibido em produção.
- A11y: aria labels, navegação por teclado, contraste WCAG 2.1 AA, um único `<h1>` por página.
