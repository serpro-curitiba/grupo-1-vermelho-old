# SIFAP 2.0 — Frontend

> Angular 18 standalone.
> Estágio 3 — MVP do portal de operações.
> Implementa REQ-PAY-001 (formulário "Gerar ciclo").

## Stack

- Angular 18.2 — standalone components + signals
- TypeScript 5.5 strict
- Reactive Forms + validators tipados (sem template-driven)
- HttpClient com base URL configurável (`window.SIFAP_API_BASE_URL`)

## Rodar local

```powershell
cd frontend
npm install
npm start
```

Abra <http://localhost:4200> e gere um ciclo para a competência `202504`.

Após a geração com sucesso, o frontend navega automaticamente para
`/ciclos/{cicloId}` e exibe o resumo operacional completo do ciclo.

A tela de resumo também consulta `/api/v1/auditoria/eventos?agregadoId={cicloId}`
e mostra os eventos de auditoria com filtro textual por ID,
tipo, ação e usuário.

## Build de produção

```powershell
npm run build
```

## Docker

O `Dockerfile` faz build de produção e serve o resultado via `nginx`
na porta 4200, alinhado com `docker-compose.yml` do root.
