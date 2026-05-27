# SIFAP 2.0 — Frontend (Angular 18 standalone)

> Estágio 3 — MVP do portal de operações. Implementa REQ-PAY-001 (formulário "Gerar ciclo").

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

Abra http://localhost:4200 e gere um ciclo para a competência `202504`.

## Build de produção

```powershell
npm run build
```

## Docker

O `Dockerfile` faz build de produção e serve o resultado via `nginx` na porta 4200 — alinhado com `docker-compose.yml` do root.
