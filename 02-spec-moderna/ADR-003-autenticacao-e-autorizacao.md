<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-003: Autenticação e Autorização — Entra ID + gov.br + RBAC

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![Status Aceito](https://img.shields.io/badge/Status-Aceito-7FBA00?style=for-the-badge)

**Data:** 2026-05-27
**Status:** Aceito
**Decisores:** Par 2 (EA + SA), Par 5 (DevOps + Tech Writer), apoio Par 1 (PO)

> Define como o SIFAP 2.0 autentica e autoriza usuários internos (servidores), externos (cidadãos) e processos (workers/scheduler), substituindo a autenticação por terminal 3270 do legado.

## Contexto

O SIFAP legado roda exclusivamente em terminal 3270. A autenticação é **por usuário do mainframe** (RACF), sem RBAC fino: qualquer servidor logado pode rodar `BATCHPGT`, consultar pagamentos e ler auditoria. A única separação é por "perfil" mainframe (TSO/ISPF).

Requisitos do SIFAP 2.0:

- **Servidores internos (Gestor, Fiscal, Auditor):** SSO corporativo Microsoft Entra ID (já adotado pela organização).
- **Cidadãos (Beneficiários):** SSO **gov.br** (selo Prata mínimo para consulta, Ouro para contestações).
- **Processos batch (worker, scheduler):** identidade gerenciada Azure (Managed Identity) — sem segredo em arquivo.
- **RBAC fino:** REQ-PAY-050 exige claim `roles=[GESTOR_PAGAMENTOS]` para `POST /api/v1/ciclos*`; REQ-AUD-002 exige `SUPER_AUDITOR` para CPF não-mascarado.
- **LGPD:** REQ-PAY-051 — CPF mascarado em logs; tokens nunca em `localStorage` plano.
- **Auditoria:** todo login/logout e mudança de role registra evento (REQ-AUD-001).

Três opções avaliadas:

### Opção 1 — Apenas Entra ID

- **Descrição:** todo usuário (incluindo cidadãos) cria conta corporativa.
- **Vantagens:** simples; um único IdP.
- **Desvantagens:** **inviável** para cidadãos (não-servidores não têm conta corporativa); fere requisito do portal cidadão.

### Opção 2 — Entra ID (interno) + gov.br (cidadão), federados via OAuth2/OIDC

- **Descrição:** dois IdPs externos; o backend confia em ambos via JWKS; claims normalizados em um `UserPrincipal` interno. Workers usam Managed Identity Azure.
- **Vantagens:** atende ambos os públicos com IdPs reconhecidos; sem invenção de roda; alinhado a [ADR-0001 (Angular + `angular-auth-oidc-client`)](../docs/adr/0001-frontend-angular.md).
- **Desvantagens:** duas integrações para manter; mapeamento gov.br ↔ roles exige tabela `usuario_externo` no schema `auditoria`.

### Opção 3 — IdP próprio (Keycloak self-hosted)

- **Descrição:** subir Keycloak federado a Entra ID e gov.br.
- **Vantagens:** controle total; um único `issuer`.
- **Desvantagens:** mais um componente para operar (DB, HA, patches); custo de SRE não justificado para 15 pessoas de equipe.

## Decisão

**Adotamos a Opção 2 — Entra ID + gov.br federados via OAuth2/OIDC**, com os seguintes pontos não-negociáveis:

1. **Frontend Angular** usa `angular-auth-oidc-client` com dois `configIds` (`entra`, `govbr`); o usuário escolhe o IdP no login. Tokens em **sessionStorage cifrado** (nunca `localStorage` puro — atende REQ-PAY-051).
2. **Backend Spring Security 6** com `JwtDecoder` multi-issuer: valida JWKS de ambos os IdPs; expira `lastSeenJwks` em 1h.
3. **Mapeamento de claims → roles internas** ocorre em `SecurityFilterChain` via `JwtAuthenticationConverter`:
   - Entra ID: claim `roles` (App Role) → `ROLE_GESTOR_PAGAMENTOS`, `ROLE_FISCAL`, `ROLE_SUPER_AUDITOR`.
   - gov.br: claim `amr` (`urn:govbr:loa:bronze|silver|gold`) + `cpf` → `ROLE_BENEFICIARIO` (Silver+) ou `ROLE_BENEFICIARIO_GOLD` (Gold).
4. **Roles internas (RBAC):**

   | Role                     | Capacidades                                                        |
   | ------------------------ | ------------------------------------------------------------------ |
   | `ROLE_BENEFICIARIO`      | Consulta próprio benefício; abre contestação (Gold).               |
   | `ROLE_BENEFICIARIO_GOLD` | Inclui Beneficiário + ações com assinatura jurídica (gov.br Ouro). |
   | `ROLE_FISCAL`            | Read-only em pagamentos + auditoria (CPF mascarado).               |
   | `ROLE_SUPER_AUDITOR`     | Igual a Fiscal + CPF em claro (REQ-AUD-002).                       |
   | `ROLE_GESTOR_PAGAMENTOS` | CRUD em beneficiário/programa; dispara ciclos (REQ-PAY-001..052).  |
   | `ROLE_ADMIN`             | Gerencia roles e parametriza programas.                            |

5. **Workers (batch / scheduler):** **Managed Identity Azure** com role `ROLE_WORKER` injetada via app role do Entra ID. Tokens obtidos via `DefaultAzureCredential`; nunca persistidos em disco.
6. **Segredos** (client secrets do Entra ID, chave de assinatura de cookies, JDBC URL, etc.) **sempre** em **Azure Key Vault** lidos por Managed Identity (atende `security.instructions.md` §segredos).
7. **Auditoria de autenticação:** todo `LoginSuccess`, `LoginFailure`, `RoleChanged`, `TokenRevoked` publica evento via Outbox → consumer `auditoria` grava append-only (atende REQ-AUD-001).
8. **Tokens:**
   - `access_token` JWT, TTL 15 min;
   - `refresh_token` rotativo, TTL 8 h (internos) / 30 min (cidadãos);
   - `id_token` apenas para login (não autoriza chamadas REST).
9. **CORS** declarado explicitamente por origem (`portal.sifap.gov.br`, `portal-hml.sifap.gov.br`) — sem `*` (atende `security.instructions.md` §OWASP-A05).

## Justificativa

Entra ID + gov.br atende **todos os públicos** com IdPs já reconhecidos pelas auditorias (TCU, CGU). A federação direta evita o custo operacional de um IdP próprio. O alinhamento com Managed Identity remove a necessidade de chaves rotativas em pipelines (atende [ADR-005](ADR-005-estrategia-deploy-e-infra.md)).

## Consequências

### Positivas

- Zero secret de senha gerenciado pela aplicação.
- Servidores reaproveitam SSO corporativo (UX já familiar).
- Cidadãos usam gov.br (atende exigência GovTech BR de uso preferencial).
- RBAC fino preserva regras escondidas do legado (ex.: mascaramento de CPF varia por perfil — BR-RPG-003).

### Negativas

- Duas integrações OIDC para configurar (Entra ID Tenant + gov.br Sandbox).
- Mapeamento gov.br requer tabela `usuario_externo (cpf, role, data_concessao)` — nova entidade no domínio `auditoria`.
- Tokens curtos exigem `refresh_token` rotativo no Angular (`angular-auth-oidc-client` já cobre).

### Riscos

| Risco                                                            | Mitigação                                                                                         |
| ---------------------------------------------------------------- | ------------------------------------------------------------------------------------------------- |
| Sandbox do gov.br ficar indisponível em homologação              | Mock OIDC local (Keycloak em devcontainer) para dev; CI usa fixtures de JWT assinados             |
| Servidor com role removida continuar com token válido por 15 min | Lista de revogação distribuída via Redis; consulta em cada `JwtAuthenticationFilter`              |
| Vazamento de token via XSS no portal cidadão                     | CSP estrita + `HttpOnly` cookie para `refresh_token` + revisão de dependências NPM via Dependabot |
| Beneficiário consultar dados de outro CPF                        | Authorization PreFilter compara `subject.cpf` com `path.cpf`; teste de integração obrigatório     |
| Worker batch rodar com permissão excessiva                       | Role `ROLE_WORKER` só tem `POST /ciclos` + `PUT /pagamentos/*/status`; revisão trimestral         |

## Referências

- [ADR-0001 Angular](../docs/adr/0001-frontend-angular.md) — biblioteca OIDC do front
- [ADR-0002 Modular Monolith](../docs/adr/0002-modular-monolith.md) — segurança no kernel `shared`
- [ADR-005 Deploy / Infra](ADR-005-estrategia-deploy-e-infra.md) — Key Vault e Managed Identity
- [`SPECIFICATION.md` §4 REQ-PAY-050..052, REQ-AUD-001..002](SPECIFICATION.md)
- [`.github/instructions/security.instructions.md`](../.github/instructions/security.instructions.md)
- [gov.br — Documentação técnica OIDC](https://acesso.gov.br/roteiro-tecnico/)
- [Microsoft Entra ID — App roles + RBAC](https://learn.microsoft.com/entra/identity-platform/howto-add-app-roles-in-apps)
- [Spring Security 6 — Multi-tenant Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/multitenancy.html)
- REQ relacionados: **REQ-PAY-050, REQ-PAY-051, REQ-PAY-052, REQ-AUD-001, REQ-AUD-002**
