<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD033 MD034 MD040 MD051 MD060 -->

# ADR-005: Estratégia de Deploy e Infraestrutura como Código

![ESTÁGIO 02 Spec](https://img.shields.io/badge/ESTÁGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge) ![STATUS Aceita](https://img.shields.io/badge/STATUS-Aceita-7FBA00?style=for-the-badge)

> 🗺 **Você está aqui:** [Kit PT-BR](../README.md) → [Estágio 2](README.md) → **ADR-005**

> 📘 Esta ADR registra a decisão operacional do Stage 2 sobre ambiente local, CI e draft de infraestrutura como código. Ela orienta a implementação do Stage 3 e a evolução do Stage 4.

**Data**: 27/05/2026
**Status**: Aceita
**Decisores**: Par 2 (EA + SA), Par 5 (DevOps + Tech Writer), apoio Par 3

## Contexto

No Estágio 2 precisamos definir um caminho de deploy reproduzível para o SIFAP 2.0, com baixo custo de operação no workshop e transição segura para o Estágio 3.

Restrições e requisitos principais:

- O time precisa subir ambiente local com Docker Compose de forma previsível.
- O pipeline deve validar qualidade mínima com lint, testes e build de imagem.
- A topologia alvo em nuvem deve ser descrita por Terraform, mesmo sem aplicar no workshop.
- O fluxo deve evitar segredos versionados e permitir evolução para CI/CD contínuo.

## Opções Consideradas

### Opção 1: Deploy manual sem IaC

- **Descrição**: configurar recursos e publicar a aplicação manualmente.
- **Vantagens**: início rápido para demo local.
- **Desvantagens**: baixo controle, pouca repetibilidade e alto risco operacional.

### Opção 2: Docker Compose local + CI básico + Terraform draft

- **Descrição**: padronizar a execução local com Compose, validar via GitHub Actions e manter Terraform em modo draft para a topologia alvo.
- **Vantagens**: equilíbrio entre velocidade e governança; reduz divergência entre máquinas; prepara a migração para cloud.
- **Desvantagens**: ainda não entrega deploy automático em nuvem no workshop.

### Opção 3: Pipeline completo com deploy automático em Azure no Estágio 2

- **Descrição**: implementar CI/CD completo com provisionamento e deploy automático imediato.
- **Vantagens**: maturidade operacional mais alta desde cedo.
- **Desvantagens**: custo e complexidade acima do necessário para o tempo do workshop.

## Decisão

**Decidimos adotar a Opção 2: Docker Compose local + CI básico + Terraform draft.**

## Justificativa

A Opção 2 atende o objetivo do workshop com menor risco porque:

- garante ambiente local padrão para o time;
- estabelece gate de qualidade com workflow de CI;
- formaliza a direção de infraestrutura via Terraform sem bloquear o progresso por credenciais cloud no Estágio 2.

## Consequências

### Positivas

- Build e execução local ficam reproduzíveis para a equipe.
- O pipeline passa a validar mudanças críticas antes da demo.
- A infraestrutura alvo fica documentada e versionada desde o início.

### Negativas

- O deploy em nuvem ainda depende de evolução no Estágio 4.
- O draft Terraform exige refinamento de módulos, variáveis e providers reais.

### Riscos

- **Risco**: diferença entre ambiente local e nuvem.
  **Mitigação**: manter health checks, portas e variáveis alinhados; evoluir para pipeline com ambiente de homologação.

## Referências

- [GUIDE do Estagio 2](GUIDE.md)
- [Template ADR](ADR-TEMPLATE.md)
- [ADR-0001 Frontend Angular](../docs/adr/0001-frontend-angular.md)
- [ADR-0003 Outbox + broker](../docs/adr/0003-outbox-broker.md)
- [Pipeline CI](../.github/workflows/ci.yml)
- [Compose local](../docker-compose.yml)
- [Draft Terraform](../infra/README.md)
- REQ relacionado: REQ-OPS-001 (pipeline mínimo), REQ-OPS-002 (infra como código)

---

### Continuar a leitura

<table width="100%">
<tr>
<td width="50%" valign="top" align="left">
<sub><strong>← ANTERIOR</strong></sub><br/>
<a href="GUIDE.md"><strong>GUIDE do Estágio 2</strong></a><br/>
<sub>Passo a passo do estágio.</sub>
</td>
<td width="50%" valign="top" align="right">
<sub><strong>PRÓXIMO →</strong></sub><br/>
<a href="../docs/adr/0001-frontend-angular.md"><strong>ADR-0001 Frontend Angular</strong></a><br/>
<sub>Decisão relacionada de stack de frontend.</sub>
</td>
</tr>
</table>

<sub>↑ <a href="../README.md">Voltar ao Kit PT-BR</a></sub>
