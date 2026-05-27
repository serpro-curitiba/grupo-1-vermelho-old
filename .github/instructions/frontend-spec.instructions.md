---
description: "Convenções de frontend para Angular 18+ — standalone components, signals, TypeScript strict, Reactive Forms, Angular Material"
applyTo: '**/src/app/**,**/*.component.ts,**/*.service.ts,**/*.ts,**/*.html'
---

# Especificação de Frontend — Angular 18+ + TypeScript

Este arquivo é ativado quando você trabalha em arquivos do projeto Angular do SIFAP 2.0 (`portal-sifap/`). Reforça as convenções decididas em [ADR-0001](../../docs/adr/0001-frontend-angular.md) e no [C4 L2](../../specs/000-contexto/c4-l2-containers.md).

## Resumo da Stack

| Camada | Tecnologia | Versão |
|-------|-----------|---------|
| Framework | Angular (standalone) | 18+ |
| Linguagem | TypeScript (strict mode) | 5+ |
| State local | Signals | nativo |
| Async / streams | RxJS | 7+ (apenas onde necessário) |
| Forms | Reactive Forms (`@angular/forms`) | 18+ |
| UI Kit | Angular Material (`@angular/material`) | 18+ |
| HTTP | `HttpClient` + interceptors | 18+ |
| Auth | `angular-auth-oidc-client` | 18+ |
| Roteamento | `@angular/router` com lazy loading | 18+ |
| SSR (opcional) | Angular Universal (`@angular/ssr`) | 18+ |
| i18n | `@angular/localize` ou `transloco` | 18+ / latest |
| Testes | Jest + Testing Library Angular | latest |
| E2E | Playwright | latest |

## Estrutura de pastas (package-by-feature)

```text
portal-sifap/
  src/
    app/
      core/                       # singletons: auth, interceptors, guards, logger
        auth/
        interceptors/
        guards/
      shared/                     # componentes/pipes/diretivas reutilizáveis (standalone)
      features/                   # 1 pasta por bounded context
        beneficiarios/
          pages/                  # rotas (smart components)
          components/             # dumb components
          services/               # services + signals stores
          models/                 # types/interfaces
          beneficiarios.routes.ts # rotas lazy
        ciclos/
        fiscalizacao/
        pagamentos/
      app.config.ts               # providers globais (provideRouter, provideHttpClient, etc.)
      app.routes.ts               # rotas raiz com loadChildren
      app.component.ts            # shell mínimo
    environments/
    styles/
```

## Padrões Angular

### Standalone components (padrão obrigatório)

Sem `NgModule`. Todo componente, diretiva e pipe é standalone.

```ts
// features/pagamentos/pages/pagamentos-list.page.ts
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { MatTableModule } from '@angular/material/table';
import { CommonModule } from '@angular/common';
import { PagamentosService } from '../services/pagamentos.service';

@Component({
  selector: 'app-pagamentos-list',
  standalone: true,
  imports: [CommonModule, MatTableModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './pagamentos-list.page.html',
})
export class PagamentosListPage {
  private readonly service = inject(PagamentosService);

  protected readonly pagamentos = this.service.pagamentos; // signal
  protected readonly total = computed(() =>
    this.pagamentos().reduce((acc, p) => acc + p.valor, 0),
  );
}
```

Regras:

- **`ChangeDetectionStrategy.OnPush`** em todo componente.
- **`inject()`** em vez de injeção por construtor.
- **`signal`/`computed`** para state local; nunca mutar arrays/objetos — use cópia imutável (`update`).
- Roteamento em `*.routes.ts` separado por feature, importado via `loadChildren` na rota pai.

### Services com signals

```ts
// features/pagamentos/services/pagamentos.service.ts
import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { Pagamento } from '../models/pagamento';

@Injectable({ providedIn: 'root' })
export class PagamentosService {
  private readonly http = inject(HttpClient);
  private readonly _pagamentos = signal<readonly Pagamento[]>([]);
  readonly pagamentos = this._pagamentos.asReadonly();

  async load(cicloId: string): Promise<void> {
    const data = await firstValueFrom(
      this.http.get<Pagamento[]>(`/api/v1/ciclos/${cicloId}/pagamentos`),
    );
    this._pagamentos.set(data);
  }
}
```

Regras:

- Exponha signals como `readonly` via `asReadonly()`.
- Use `firstValueFrom` em vez de `.subscribe()` quando o consumo é one-shot.
- **Nunca** chame `HttpClient` direto de componentes — sempre via service.

### Reactive Forms tipados

```ts
import { FormBuilder, Validators } from '@angular/forms';

const fb = inject(FormBuilder);

protected readonly form = fb.nonNullable.group({
  cpf: ['', [Validators.required, Validators.pattern(/^\d{11}$/)]],
  valor: [0, [Validators.required, Validators.min(0.01)]],
});
```

- Sempre `fb.nonNullable.group(...)` — evita `null` implícito.
- Validators customizados em `core/validators/`.
- Template-driven forms são proibidos em produção.

### Roteamento + lazy loading

```ts
// app.routes.ts
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const APP_ROUTES: Routes = [
  {
    path: 'pagamentos',
    canActivate: [authGuard],
    loadChildren: () =>
      import('./features/pagamentos/pagamentos.routes').then(m => m.PAGAMENTOS_ROUTES),
  },
];
```

### HTTP + Interceptors

- JWT interceptor anexa `Authorization: Bearer …` a partir do `angular-auth-oidc-client`.
- Correlation interceptor injeta `X-Correlation-Id` (UUID por request).
- Error interceptor centraliza tratamento (Snackbar + log).

```ts
// app.config.ts
provideHttpClient(withInterceptors([authInterceptor, correlationInterceptor, errorInterceptor]))
```

### Auth (OIDC)

- `angular-auth-oidc-client` configurado contra Azure Entra ID.
- Portal cidadão (futuro): federação via gov.br.
- **Nunca** persista `access_token` em `localStorage` em texto plano; prefira `sessionStorage` com expiração curta ou o armazenamento da própria lib.

## Convenções TypeScript

- `strict: true` no `tsconfig.json` — sem `// @ts-ignore`, sem `any`.
- Use `unknown` + type guards quando o tipo é dinâmico.
- **Somente named exports** — proibido `export default` em componentes/services.
- `interface` para shapes públicos extensíveis; `type` para uniões e utilitários.
- Utility types: `Pick`, `Omit`, `Partial`, `Readonly`, `Required`.

## UI

- **Angular Material** é a primeira escolha (Button, Card, Table, Dialog, FormField, Select…).
- Tema customizado em `styles/_theme.scss` via `mat.define-theme`.
- Tailwind é opcional como complemento (utility classes para spacing/grid); não misture estilos inline.

## Acessibilidade (WCAG 2.1 AA)

- Todo `<img>` com `alt` significativo (`alt=""` para decorativas).
- Inputs sempre com `<mat-label>` ou `<label>` associado.
- Foco visível e navegação completa por teclado (`Tab`, `Esc` em dialogs).
- Contraste mínimo 4.5:1; cor não é o único canal de informação.
- Um único `<h1>` por página; headings em ordem lógica.
- `aria-live="polite"` para mensagens dinâmicas (validação, toasts).

## Testes (Jest + Testing Library Angular)

```ts
import { render, screen } from '@testing-library/angular';
import { PagamentoCardComponent } from './pagamento-card.component';

describe('PagamentoCardComponent', () => {
  it('exibe o valor formatado em BRL', async () => {
    await render(PagamentoCardComponent, {
      componentInputs: { pagamento: { id: '1', valor: 100.5 } },
    });
    expect(screen.getByText(/R\$\s*100,50/)).toBeInTheDocument();
  });
});
```

- 1 teste de unidade por regra de negócio relevante; E2E com Playwright para fluxos críticos.
- Mocks de `HttpClient` via `provideHttpClientTesting()`.

## Segurança

- Sanitização XSS: use bindings (`{{ }}`, `[innerText]`), nunca `[innerHTML]` sem `DomSanitizer.bypass*` justificado.
- CSP no servidor que serve o bundle; nada inline.
- Variáveis sensíveis ficam no backend — `environment.ts` carrega apenas URLs públicas.
- Nunca commitar tokens, client secrets ou IDs internos.

## O Que NÃO Fazer

- **Sem `NgModule`** — standalone obrigatório.
- **Sem `any`** — use `unknown` + type guards.
- **Sem `export default`** em componentes/services.
- **Sem `.subscribe()` aninhado** — prefira `firstValueFrom` ou `async` pipe.
- **Sem template-driven forms** em produção.
- **Sem chamadas HTTP em componentes** — passe por services.
- **Sem `localStorage` para tokens** sem cifragem/expiração.
- **Sem mutar signals** diretamente — use `set`/`update` com cópia imutável.
- **Sem Next.js, React, Vue, Svelte** — stack fixa em Angular (ver ADR-0001).
