---
description: "Extrai regras de negócio de um programa Natural lendo blocos IF/THEN/ELSE e confirmando com documentação."
mode: ask
required: ['file']
inputs:
  file:
    description: "Caminho relativo ao arquivo .nat a ser analisado"
    type: string
  docs:
    description: "(opcional) Caminho(s) para pastas/arquivos de documentação para cross-reference"
    type: array
    items: string
# model removed to avoid hardcoding — orquestração decide
# Executor requirements: fornecer APIs para leitura de arquivos e busca por texto.
# Recommended minimal tools: `file_read`, `file_search`, `read_file` (bindings do executor).
---

# /extract-business-rules

## Objetivo

Leia um programa Natural escolhido e extraia toda regra de negócio candidata identificando lógica condicional (IF/THEN/ELSE, DECIDE, AT BREAK). Cada regra é declarada em linguagem clara, rastreada à fonte e classificada como confirmada ou mistério.

## Quando Invocar

Depois que a equipe completar o inventário inicial (`/archaeology-kickoff`) e escolher um programa para ler.

## Pré-condições

- `01-arqueologia/inventory.md` existe
- A equipe selecionou um arquivo específico de programa Natural para analisar (parâmetro `file` obrigatório)
- A pasta `01-arqueologia/legado-sifap/` está acessível

## Entradas que a Equipe Deve Fornecer

- O path completo para o programa Natural a analisar (por exemplo, `01-arqueologia/legado-sifap/programs/PGXXXXXX.nat`)
- Quaisquer paths de documentação disponíveis em `01-arqueologia/legado-sifap/docs/` (opcional — usados para confirmação)

## O Que Vou Fazer

- Ler o programa especificado de cima a baixo
- Identificar todo bloco condicional: `IF...THEN...ELSE...END-IF`, `DECIDE ON`, `AT BREAK OF` e operadores de comparação
- Para cada bloco condicional, formular uma regra de negócio candidata em linguagem clara
- Fazer cross-reference com documentação em `01-arqueologia/legado-sifap/docs/`, se disponível
- Classificar cada regra como **confirmed** (correspondência em documentação), **inferred** (somente código, sem suporte documental) ou **mistério** (lógica pouco clara)
- Rascunhar candidatos de notação EARS para regras confirmadas

## O Que NÃO Vou Fazer

- Inferir regras apenas a partir de nomes de programas ou variáveis — leio a lógica real
- Fabricar explicações para código pouco claro — mistérios continuam mistérios
- Resumir o programa inteiro em uma passada — trabalho bloco por bloco
- Referenciar conhecimento sobre qualquer sistema legado específico — leio apenas o que a equipe me mostra
- Promover automaticamente regras inferred para status confirmed

## Formato de Saída

Anexar a `01-arqueologia/business-rules-catalog.md` (idempotente: se uma regra já existir, atualizar/annotar em vez de duplicar):

```markdown
## Regras de [nome-do-arquivo]

| # | Declaração da Regra | Candidato EARS | Fonte | Classificação | Notas |
|---|---|---|---|---|---|
| 1 | Quando X ocorrer, o sistema deverá fazer Y | Event-driven | file.nat:L42-58 | Confirmada | Corresponde à seção 3.2 do documento |
| 2 | Se Z ocorrer, o sistema deverá rejeitar | Unwanted | file.nat:L73-81 | Mistério | <!-- mistério: não está claro o que aciona Z --> |
```

## Definição de Pronto

- [ ] Todo bloco IF/THEN/ELSE, DECIDE e AT BREAK no programa foi examinado
- [ ] Cada regra candidata tem file path e intervalo de linhas
- [ ] Regras confirmadas citam a seção de documentação que as apoia
- [ ] Regras inferred estão claramente marcadas e não são tratadas como fatos
- [ ] Mistérios têm marcadores `<!-- mistério: ... -->` com uma descrição do que é desconhecido
- [ ] Existe pelo menos um candidato de notação EARS por regra confirmada

## Corpo do Prompt

Você é o `@archaeologist-agent`. A equipe escolheu um programa Natural para analisar regras de negócio. Você o lerá sistematicamente e extrairá toda regra de negócio condicional.

**Passo 1 — Ler DEFINE DATA.**
Abra o arquivo especificado. Leia a seção `DEFINE DATA` primeiro. Liste toda variável com seu tipo, tamanho e qualquer comentário. Isso estabelece o vocabulário para entender condições depois.

**Passo 1.1 — Inclusões**
- Liste cada `INCLUDE`/`COPY` encontrado com o caminho e as linhas relevantes.
- Se uma variável usada em condições vem de um include, anote a origem ao lado da variável.

**Limite de profundidade para inclusões**
- Para evitar ciclos, limite a leitura recursiva de `INCLUDE`/`COPY` a 5 níveis por padrão e registre quando o limite for atingido.

**Passo 2 — Identificar blocos condicionais.**
Escaneie o programa para cada instância de:
- `IF ... THEN ... [ELSE ...] END-IF`
- `DECIDE ON FIRST/EVERY VALUE OF`
- `AT BREAK OF`
- Operadores de comparação usados com literais (valores numéricos, constantes string, valores de data)

Para cada bloco, registre: linha inicial, linha final, expressão de condição, ação tomada em cada branch.

Adicionalmente, capture chamadas e fluxo:
- Detecte `CALLNAT`, `PERFORM` e registre arestas "chama" para alimentar o grafo de dependências.

**Fluxo e limitações**
- Se chamadas externas referenciam programas fora do workspace, marque como `<!-- mistério: external reference: <path> -->`.

**Passo 3 — Formular regras candidatas.**
Para cada bloco condicional, escreva uma declaração de regra de negócio em linguagem clara. Siga este padrão:
- Comece com a condição: "Quando [condição]..." ou "Se [condição]..."
- Declare a ação: "...o sistema deve [ação]"
- Inclua o branch else se existir: "Caso contrário, o sistema deve [ação alternativa]"

**Passo 4 — Tentar classificação EARS.**
Para cada regra, proponha qual padrão EARS ela corresponde:
- **Ubiquitous**: Sempre verdadeiro, sem trigger → "O sistema deverá..."
- **Event-driven**: Acionado por um evento → "Quando [evento], o sistema deverá..."
- **State-driven**: Ativo enquanto está em um estado → "Enquanto [estado], o sistema deverá..."
- **Optional**: Condicional a uma feature/config → "Onde [condição], o sistema deverá..."
- **Unwanted**: Tratamento de erro ou rejeição → "Se [condição indesejada], então o sistema deverá..."

**Passo 5 — Fazer cross-reference com documentação.**
Se a equipe forneceu paths de documentação, pesquise nesses arquivos por palavras-chave correspondentes aos nomes de variáveis ou valores literais nas condições. Para cada correspondência encontrada, promova a regra para "confirmed" e cite a seção de documentação. Para cada regra sem suporte documental, classifique como "inferred".

**Passo 6 — Sinalizar mistérios.**
Para qualquer bloco condicional em que:
- Os nomes de variáveis são crípticos e a intenção da condição não está clara
- Os valores literais não têm significado óbvio (magic numbers)
- A lógica parece contraditória ou redundante

Marque como `<!-- mistério: [descrição do que não está claro] -->` e adicione ao catálogo com classificação "mistério".

**Nota sobre magic numbers**
- Ao encontrar literais sem documentação (ex.: `IF X = 30`), adicione uma nota explicando que se trata de um `magic number` e proponha hipóteses de significado; não promova a hipótese para confirmed sem evidência documental.

**Passo 7 — Gerar resultados.**
Anexe os resultados a `01-arqueologia/business-rules-catalog.md`. Se o arquivo não existir, crie-o com um cabeçalho. Cada entrada de regra deve ter: número da regra, declaração em linguagem clara, candidato EARS, arquivo-fonte e intervalo de linhas, classificação e notas. Mantenha idempotência: se uma regra com mesma declaração e fonte já existir, atualize suas notas e classificação em vez de duplicar.

Não infira regras a partir de nomes de programas ou organização de arquivos. Leia o código real. Se o propósito de um bloco for genuinamente pouco claro após leitura cuidadosa, ele é um mistério — não uma regra.

## Exemplo de Invocação

```
/extract-business-rules file=01-arqueologia/legado-sifap/programs/PGMAIN01.nat docs=01-arqueologia/legado-sifap/docs/
```

## Exemplos de saída (estrutura)

Exemplo JSON (útil para parsing automático):

```json
{
  "file": "01-arqueologia/legado-sifap/programs/PGMAIN01.nat",
  "rules": [
    {
      "id": 1,
      "statement": "Quando X ocorrer, o sistema deverá fazer Y",
      "ears": "Event-driven",
      "source": "PGMAIN01.nat:L42-L58",
      "classification": "confirmed",
      "notes": "Corresponde à seção 3.2 do documento"
    }
  ]
}
```

Exemplo YAML (análogo):

```yaml
file: 01-arqueologia/legado-sifap/programs/PGMAIN01.nat
rules:
  - id: 1
    statement: "Quando X ocorrer, o sistema deverá fazer Y"
    ears: Event-driven
    source: PGMAIN01.nat:L42-L58
    classification: confirmed
    notes: "Corresponde à seção 3.2 do documento"
```

## Mensagens de erro previsíveis
- `file not found`: arquivo `file` não existe ou caminho incorreto.
- `DEFINE DATA not found`: arquivo não contém seção `DEFINE DATA` reconhecível.
- `no conditional blocks found`: nenhum bloco condicional detectado.
- `include depth limit reached`: limite de leitura de includes atingido.

Adote mensagens legíveis e códigos de erro simples para automação.
