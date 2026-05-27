<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# ADR-005: Estrategia de Deploy e Infraestrutura como Codigo

![ESTAGIO 02 Spec](https://img.shields.io/badge/ESTAGIO-02%20Spec-00A4EF?style=for-the-badge) ![TIPO ADR](https://img.shields.io/badge/TIPO-ADR-1A1A1A?style=for-the-badge)

**Data**: 27/05/2026
**Status**: Aceita
**Decisores**: Par 2 (EA + SA), Par 5 (DevOps + Tech Writer), apoio Par 3

## Contexto

No Estagio 2 precisamos definir um caminho de deploy reproduzivel para o SIFAP 2.0, com baixo custo de operacao no workshop e transicao segura para o Estagio 3.

Requisitos e restricoes:

- O time precisa subir ambiente local com Docker Compose de forma previsivel.
- O pipeline deve validar qualidade minima (lint, test e build de imagem).
- A topologia alvo em nuvem deve ser descrita por Terraform, mesmo sem aplicar no dia.
- O fluxo deve evitar segredos em codigo e permitir evolucao para CI/CD continuo.

## Opcoes Consideradas

### Opcao 1: Deploy manual sem IaC

- **Descricao**: configurar recursos e publicar aplicacao manualmente.
- **Vantagens**: inicio rapido para demo local.
- **Desvantagens**: baixo controle, pouca repetibilidade, alto risco operacional.

### Opcao 2: Docker Compose local + CI basico + Terraform draft

- **Descricao**: padronizar execucao local com compose, validar via GitHub Actions e manter Terraform em modo draft para topologia alvo.
- **Vantagens**: equilibrio entre velocidade e governanca; reduz divergencia entre maquinas; prepara migracao para cloud.
- **Desvantagens**: ainda sem deploy automatico em nuvem no workshop.

### Opcao 3: Pipeline completo com deploy automatico em Azure no Estagio 2

- **Descricao**: implementar CI/CD full com provisionamento e deploy automatico imediato.
- **Vantagens**: maturidade operacional mais alta desde cedo.
- **Desvantagens**: custo e complexidade acima do necessario para o tempo do workshop.

## Decisao

**Decidimos adotar a Opcao 2: Docker Compose local + CI basico + Terraform draft.**

## Justificativa

A Opcao 2 atende o objetivo do workshop com menor risco:

- garante ambiente local padrao para o time;
- estabelece gate de qualidade com workflow de CI;
- formaliza a direcao de infraestrutura via Terraform sem bloquear o progresso por credenciais/cloud no Estagio 2.

## Consequencias

### Positivas

- Build e execucao local ficam reproduziveis para a equipe.
- Pipeline passa a validar mudancas criticas antes da demo.
- Infraestrutura alvo fica documentada e versionada desde o inicio.

### Negativas

- O deploy em nuvem ainda depende de evolucao no Estagio 4.
- O draft Terraform exige refinamento de modulos e variaveis de ambiente.

### Riscos

- **Risco**: diferenca entre ambiente local e nuvem.
  **Mitigacao**: manter health checks e variaveis alinhados, e evoluir para pipeline com ambiente de homologacao.

## Referencias

- [GUIDE do Estagio 2](GUIDE.md)
- [Template ADR](ADR-TEMPLATE.md)
- [Pipeline CI](../.github/workflows/ci.yml)
- [Compose local](../docker-compose.yml)
- REQ relacionado: REQ-OPS-001 (pipeline minimo), REQ-OPS-002 (infra como codigo)
