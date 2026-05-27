---
description: "Inicia o Estágio 1 — orienta a equipe sobre a pasta de legado e produz um inventário inicial."
mode: ask
model: claude-sonnet-4-20250514
tools: ['codebase', 'search', 'findFiles', 'grep_search']
---

# /archaeology-kickoff

## Objetivo

Oriente a equipe sobre a codebase legada com um inventário top-down antes de ler qualquer programa individual. Esta é a primeira coisa que o Estágio 1 faz — mapear o terreno antes de cavar.

## Quando Invocar

Logo no início do Estágio 1, imediatamente depois que a equipe recebe acesso à pasta `01-arqueologia/legado-sifap/`.

## Pré-condições

- ✅ A pasta `01-arqueologia/legado-sifap/` está disponível no workspace (com symlink criado por `11-scripts/setup.sh` ou posicionada manualmente)
- ✅ A equipe **ainda não invocou** `/extract-business-rules` ou `/map-dependencies`
- ✅ `00-SETUP.md` foi completado (symlink criado)
- ⚠️ Se alguma pré-condição falhar, este prompt aborta — redirecione para `/setup-verify`

## Entradas que a Equipe Deve Fornecer

- O path para a pasta de legado (normalmente `01-arqueologia/legado-sifap/`)
- Confirmação de que a equipe ainda não começou a ler arquivos individuais (este prompt é para orientação, não leitura profunda)

## O Que Vou Fazer

- Escanear a pasta `01-arqueologia/legado-sifap/` recursivamente e listar todos os diretórios
- Contar arquivos por extensão (`.nat`, `.cpy`, `.ddm`, `.map` e quaisquer outras)
- Classificar programas por prefixos de padrão de nomes (por exemplo, `BN-*` para batch, `PG-*` para online)
- Sinalizar os 3 principais itens que parecem incomuns com base no tamanho do nome, tamanho do arquivo ou localização
- Propor uma ordem de leitura com base na classificação

## O Que NÃO Vou Fazer

- Abrir ou ler arquivos de programa individuais (isso vem nos prompts seguintes)
- Dizer à equipe o que os programas fazem — a equipe descobre isso por conta própria
- Fabricar explicações para convenções de nomes — se um prefixo não estiver claro, marco como desconhecido
- Referenciar qualquer interno específico do sistema — trabalho apenas com o que a estrutura de pastas revela

## Formato de Saída

Um arquivo Markdown em `01-arqueologia/inventory.md` com:

```markdown
# Inventário Legado — [Nome da Equipe]
**Data:** [data] | **Status:** Primeira Passada

## Estrutura de Pastas
## Contagem de Arquivos por Tipo
## Padrões de Convenção de Nomes
## Itens Incomuns (Top 3)
## Ordem de Leitura Proposta
```

### Exemplo de Inventário Preenchido

```markdown
# Inventário Legado — Grupo 1 Vermelho
**Data:** 27/05/2026 | **Status:** Primeira Passada

## Estrutura de Pastas
01-arqueologia/legado-sifap/
├── natural-programs/   (15 arquivos .NSN)
├── adabas-ddms/        (4 arquivos .ddm)
├── legacy-docs/        (7 arquivos .md)
└── demo/               (2 arquivos HTML)

## Contagem de Arquivos por Tipo
| Extensão | Contagem | Finalidade provável |
|----------|----------|---------------------|
| .nsn     | 15       | Programas Natural   |
| .ddm     | 4        | Estruturas Adabas   |
| .md      | 7        | Documentação        |
| .html    | 2        | Demos legadas       |

## Padrões de Convenção de Nomes
| Prefixo | Contagem | Hipótese                       |
|---------|----------|--------------------------------|
| CAD     | 3        | Cadastro (entry points online) |
| BATCH   | 3        | Lotes em batch                 |
| CALC    | 3        | Cálculos                       |
| VAL     | 3        | Validações                     |
| CONS    | 1        | Consultas (read-only)          |
| REL     | 2        | Relatórios                     |

## Itens Incomuns (Top 3)
1. **BATCHPGT.NSN** — maior arquivo do lote (4.2KB) com prefixo BATCH
   → Ação: provável entry point de batch — priorize leitura
2. **legacy-docs/SIFAP-Historia.md** — único .md em pasta legada
   → Ação: pode conter contexto histórico — leia antes dos programas
3. **demo/sifap-terminal.html** — único HTML, aninhamento profundo
   → Ação: pode ser UI legada — use para entender fluxos de usuário

## Ordem de Leitura Proposta
1. legacy-docs/SIFAP-Historia.md — contexto histórico primeiro
2. adabas-ddms/*.ddm — entender dados antes do código (4 arquivos)
3. BATCH*.NSN — entry points batch
4. CAD*.NSN — cadastros (lógica de entrada)
5. CALC*.NSN — cálculos (core business logic)
6. VAL*.NSN — validações
7. CONS*.NSN + REL*.NSN — consultas e relatórios (após core)
```

## Tratamento de Erros

**Se a pasta estiver vazia ou inacessível:**
- Reporte: "Pasta não encontrada em `[path]`. Sugerindo validar com `find` (bash) ou `Get-ChildItem` (PowerShell)."
- Não falhe — gere inventário vazio com sugestão de ação.

**Se houver arquivos com extensões desconhecidas:**
- Liste-os com `Extensão desconhecida — investigar manualmente`.
- Não adivinhe a finalidade.

**Se o symlink/pasta de setup estiver faltando:**
- Aborte com: "Pasta `01-arqueologia/legado-sifap/` não encontrada. Rode `11-scripts/setup.sh` antes."

## Definição de Pronto

- [ ] O arquivo de inventário existe com a estrutura de pastas documentada
- [ ] As contagens de arquivos estão corretas (verificáveis por uma segunda pessoa da equipe rodando `find`)
- [ ] Pelo menos 3 padrões de convenção de nomes identificados com contagens
- [ ] Três itens "parece incomum" sinalizados com file paths e motivos
- [ ] A ordem de leitura proposta é justificada por padrões de nomes ou posição estrutural

## Corpo do Prompt

Você é o `@archaeologist-agent`, iniciando uma orientação do Estágio 1 com a equipe. A equipe acabou de receber sua codebase legada e ainda não abriu nenhum arquivo.

Execute os seguintes passos em ordem. Não pule nenhum passo.

**Passo 1 — Mapear a árvore de pastas.**
Liste todos os diretórios e subdiretórios sob o path de legado fornecido. Exiba a estrutura em árvore. Conte o número total de diretórios.

**Passo 2 — Contar arquivos por extensão.**
Para cada extensão de arquivo encontrada (`.nat`, `.cpy`, `.ddm`, `.map`, `.txt`, `.md` ou qualquer outra), reporte a contagem. Apresente como tabela: `| Extensão | Contagem | Finalidade provável |`. Para "Finalidade provável", use apenas conhecimento genérico de Natural/Adabas (por exemplo, `.nat` = programa-fonte Natural, `.cpy` = copycode, `.ddm` = Data Definition Module). Não adivinhe o conteúdo de nenhum arquivo específico.

**Passo 3 — Identificar padrões de convenção de nomes.**
Escaneie todos os nomes de arquivos (sem abrir os arquivos). Agrupe arquivos pelo padrão de prefixo (primeiros 2-3 caracteres antes de um delimitador como `-`, `_` ou um dígito). Para cada padrão com 2+ arquivos, reporte: `| Prefixo | Contagem | Hipótese |`. A hipótese se baseia apenas em conhecimento genérico de convenções Natural. Se um prefixo não tiver padrão claro, marque a hipótese como `Desconhecido — investigar no próximo passo`.

**Passo 4 — Sinalizar itens incomuns.**
Identifique os 3 itens mais incomuns na pasta. "Incomum" significa qualquer um destes: maior arquivo por tamanho, aninhamento mais profundo, padrão de nome que ocorre apenas uma vez ou extensão que aparece apenas uma vez. Para cada um, forneça: file path, o que o torna incomum e uma ação de investigação sugerida.

**Passo 5 — Propor uma ordem de leitura.**
Com base nos padrões identificados, proponha quais arquivos ler primeiro. Priorize: (a) entry points batch (normalmente identificáveis por padrões de prefixo), (b) arquivos DDM (para entender dados antes do código), (c) os programas mais conectados (arquivos cujos nomes aparecem como argumentos em outros nomes de arquivos, sugerindo relacionamentos CALLNAT). Declare claramente que isso é uma hipótese — a ordem real de leitura mudará quando a equipe começar a rastrear dependências.

**Passo 6 — Gerar o inventário.**
Escreva o inventário completo em `01-arqueologia/inventory.md` seguindo o formato de saída acima. Inclua a data, um placeholder para nome da equipe e uma nota de que esta é a primeira passada — a ser revisada conforme a equipe lê arquivos individuais.

Não abra nenhum arquivo para ler seu conteúdo. Este prompt opera somente sobre nomes de arquivos e estrutura de pastas. Se a equipe pedir para você ler um arquivo específico, redirecione para `/extract-business-rules` ou `/map-dependencies`.

## Próximos Passos

Após este inventário:
1. Cada par escolhe seus 3 programas conforme `01-arqueologia/LEGACY-EXPLORATION-CHECKLIST.md`
2. Invocar `/extract-business-rules` para cada programa atribuído
3. Invocar `/map-dependencies` para mapear cadeias CALLNAT
4. Invocar `/catalog-mysteries` para registrar perguntas em aberto
5. Consolidar tudo em `01-arqueologia/discovery-report.md` via `/discovery-report`

## Exemplo de Invocação

```
/archaeology-kickoff path=01-arqueologia/legado-sifap/
```
