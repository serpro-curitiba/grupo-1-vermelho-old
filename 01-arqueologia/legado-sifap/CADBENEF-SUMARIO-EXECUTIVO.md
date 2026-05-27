# 🔍 Análise Arqueológica: CADBENEF.NSN

**Data:** 2026-05-27 | **Arqueólogo:** Agente Archaeologist

---

## 📋 Metadados do Programa

| Campo | Valor |
|-------|-------|
| **Nome** | CADBENEF |
| **Sistema** | SIFAP (Sistema de Fiscalização e Administração de Pagamentos) |
| **Autor** | CARLOS ROBERTO DA SILVA |
| **Data Criação** | 15/03/1997 |
| **Última Alteração** | 10/01/2011 (JOSE FERREIRA) |
| **Estabilidade** | 15 anos sem mudanças |

### Histórico de Alterações
1. **22/08/2005** - MARCIA HELENA: Inclusão de validação CPF
2. **10/01/2011** - JOSE FERREIRA: Ajuste de status para idosos (>75 anos)

**Objetivo:** Cadastro de beneficiário com operações de inclusão/alteração, mantendo dados cadastrais no arquivo 150 (BENEFICIARIO).

---

## 📊 Estrutura de Dados

### VIEW BENEFICIARIO (17 campos)

```
CPF (N11)                    ← Chave Primária
├─ NOME (A60)
├─ DT-NASCIMENTO (N8)
├─ SEXO (A1)
├─ ENDERECO (A80)
├─ MUNICIPIO (A40)
├─ UF (A2)
├─ CEP (N8)
├─ TELEFONE (A15)
├─ RG (A15)
├─ STATUS (A1)              ← 'A'=Ativo, 'S'=Suspenso
├─ COD-PROGRAMA (N4)
├─ RENDA-FAMILIAR (N9.2)
├─ NUM-DEPENDENTES (N2)
├─ DT-CADASTRO (N8)
├─ DT-ATUALIZACAO (N8)
├─ COD-REGIAO (N2)
└─ NIS (N11)
```

---

## 📥 Inputs / Outputs

### Leitura (READ)
- **FIND BENEFICIARIO-V WITH CPF** → Busca beneficiário existente

### Escrita (WRITE)
| Operação | Campos | Linhas |
|----------|--------|--------|
| **STORE (INSERT)** | Todos os 17 campos | 196-209 |
| **UPDATE (MODIFY)** | 10 campos (exceto CPF, DT-NASC, SEXO, DT-CADASTRO) | 213-224 |

### Campos Imutáveis em Alteração
- ❌ CPF (chave primária)
- ❌ DT-NASCIMENTO
- ❌ SEXO
- ❌ DT-CADASTRO

---

## 🔗 Dependências

**Chamadas CALLNAT externas:** Nenhuma  
**Subroutinas internas:** 1

### Subroutina: VALIDA-CPF (L267-L318)
Implementa validação de CPF por **módulo 11** (algoritmo da Receita Federal Brasil).

---

## 💼 Regras de Negócio Extraídas

### 🔴 CRÍTICOS (BR-001 a BR-009)

| ID | Regra | Linha | Campo | Risco |
|----|-------|-------|-------|-------|
| **BR-001** | Operação deve ser I ou A | 119-124 | OPER | CRÍTICO |
| **BR-002** | CPF obrigatório | 126-131 | CPF | CRÍTICO |
| **BR-003** | Validação CPF - Módulo 11 | 267-318 | CPF | CRÍTICO |
| **BR-004** | Nome obrigatório | 133-138 | NOME | CRÍTICO |
| **BR-005** | Data nascimento obrigatória | 140-145 | DT-NASC | CRÍTICO |
| **BR-006** | Sexo deve ser M ou F | 147-152 | SEXO | CRÍTICO |
| **BR-007** | Em inclusão: CPF não pode existir | 156-164 | CPF | CRÍTICO |
| **BR-008** | Em alteração: CPF deve existir | 166-171 | CPF | CRÍTICO |
| **BR-009** | Cálculo de idade (ANO_ATUAL - ANO_NASC) | 173-177 | DT-NASC | MÉDIO ⚠️ |

### 🟠 ALTO IMPACTO (BR-010 a BR-013)

| ID | Regra | Detalhe | Risco |
|----|-------|---------|-------|
| **BR-010** | Status inicial = 'A' (Ativo) | Apenas para INCLUSÃO | MÉDIO |
| **BR-011** | **Se IDADE > 75 → Status = 'S' (Suspenso)** | Adicionado em 2011, justificativa desconhecida | ALTO ⚠️ |
| **BR-012** | DT-CADASTRO e DT-ATUALIZACAO = *DATN (hoje) | Apenas para INCLUSÃO | MÉDIO |
| **BR-013** | Em alteração: alguns campos são imutáveis | CPF, DT-NASC, SEXO, DT-CADASTRO | ALTO |

---

## ⚠️ Mistérios Descobertos (12)

### 🔴 CRÍTICOS

#### M-003: Limite de 75 anos - Justificativa?
- A regra BR-011 suspende beneficiários > 75 anos
- **Pergunta:** Por que 75 e não 70, 80 ou outra idade?
- **Data:** Adicionada 10/01/2011 por JOSE FERREIRA
- **Sugestão:** Consultar negócio (era 29 anos atrás!)
- **Risco:** Regra pode estar desatualizada

#### M-002: Cálculo de idade é impreciso
```
Problema: Idade = ANO_ATUAL - ANO_NASC (trunca mês e dia)

Exemplo:
- Alguém nascido em 31/12/1950
- Em 01/01/2026: teria 76 anos ✓ (correto)
- Em 30/12/2025: teria 75 anos ✗ (deveria ser 74)

Impacto: Beneficiários podem ser suspensos NO DIA ERRADO!
```

#### M-006: COD-PROGRAMA não validado
- Campo aceita qualquer N4 (0-9999)
- Sem validação se programa existe em tabela
- **Risco:** Dados órfãos (referências para programas inexistentes)

#### M-007: NIS sem validação
- Aceita qualquer N11 (11 dígitos)
- Sem checksum (como CPF teria)
- Sem validação de unicidade
- Sem integração com INSS
- **Risco:** Duplicatas, inconsistência com cadastro nacional

#### M-010: Transações sem garantia ACID
- Usa `END TRANSACTION` em Adabas
- **Pergunta:** Há garantia de atomicidade?
- Se falha entre UPDATE e END TRANSACTION, fica inconsistente?

### 🟠 ALTO IMPACTO

#### M-001: Validação de data incompleta
- ✅ Valida se DT-NASC = 0
- ❌ Não valida se é data real (32/13/2000 é aceita!)
- ❌ Não valida se data está no futuro
- ❌ Não valida limite de idade mínima

#### M-004: CPF=0 é redundante
- Linha 156 rejeita CPF=0
- Mas FIND com CPF=0 também não acharia nada
- **Pergunta:** Há beneficiários com CPF=0 no banco (bug de dados)?

#### M-005: Endereço pode ser incompleto
- Campos ENDERECO, MUNICIPIO, UF, CEP, TELEFONE, RG podem ser nulos
- **Pergunta:** É intencional ou oversight?
- Endereços incompletos dificultam correspondência física

### 🟡 MÉDIO IMPACTO

#### M-008: Renda familiar e dependentes sem validação
- ❌ Sem validar se RENDA-FAM >= 0
- ❌ Sem validar se NUM-DEP está em 0-99
- ❌ Sem validação cruzada: se NUM-DEP > 0, RENDA-FAM deveria ser > 0?

#### M-009: Sem validações sexo-específicas
- **Pergunta:** Há benefícios específicos por sexo?
  - Licença maternidade (F) vs. paternidade (M)?
  - Programa deveria validar isto?

#### M-011: Timestamps sem hora
- Usa apenas *DATN (data, sem hora)
- Se múltiplas operações no mesmo dia, perde sequência
- Auditoria fica imprecisa

#### M-012: Mensagens em português
- Hardcoded em português (não suporta i18n)
- Se migrar para cloud multilíngue, refatoring necessário

---

## 🔗 Fluxo de Operação (Sequência)

```
1. Obter data/hora do sistema (*DATN)
2. INPUT: Ler 14 campos (operação, CPF, Nome, etc.)
3. VALIDAÇÕES BÁSICAS:
   ├─ OPER ∈ {I, A} ?
   ├─ CPF ≠ 0 ?
   ├─ CPF é válido (módulo 11) ? → PERFORM VALIDA-CPF
   ├─ NOME ≠ branco ?
   ├─ DT-NASC ≠ 0 ?
   └─ SEXO ∈ {M, F} ?
4. FIND beneficiário existente com este CPF
5. LÓGICA DE OPERAÇÃO:
   ├─ Se INCLUSÃO (I):
   │  └─ Beneficiário já existe? → ERRO
   └─ Se ALTERAÇÃO (A):
      └─ Beneficiário não existe? → ERRO
6. Calcular IDADE
7. Aplicar STATUS:
   ├─ Se INCLUSÃO: STATUS = 'A' (padrão)
   └─ Se IDADE > 75: STATUS = 'S' (sobrescreve padrão)
8. STORE ou UPDATE:
   ├─ INCLUSÃO: STORE 17 campos + DT-CADASTRO + DT-ATUALIZACAO
   └─ ALTERAÇÃO: UPDATE 10 campos + DT-ATUALIZACAO
9. Mensagem de sucesso
```

---

## 🔐 Algoritmo de Validação CPF (Módulo 11)

### Estrutura CPF: 11 dígitos

```
D1 D2 D3 D4 D5 D6 D7 D8 D9 DV1 DV2
                         ↓    ↓
                    Dígitos verificadores
```

### Cálculo DV1 (1º dígito verificador)

```
SOMA = D1×10 + D2×9 + D3×8 + ... + D9×2
RESTO = SOMA MOD 11

Se RESTO < 2:
  DV1 = 0
Senão:
  DV1 = 11 - RESTO

Validação: DV1 ≟ D10 (10º dígito)
```

### Cálculo DV2 (2º dígito verificador)

```
SOMA = D1×11 + D2×10 + D3×9 + ... + D9×3 + DV1×2
RESTO = SOMA MOD 11

Se RESTO < 2:
  DV2 = 0
Senão:
  DV2 = 11 - RESTO

Validação: DV2 ≟ D11 (11º dígito)
```

---

## 💡 Insights de Modernização

### Curto Prazo (Imediato)
- ✅ Externalizar constantes (75, 'A', 'S')
- ✅ Documentar origem da regra de 75 anos
- ✅ Adicionar validação de data real

### Médio Prazo (1-3 meses)
- ✅ Validar chaves estrangeiras (COD-PROGRAMA, COD-REGIAO)
- ✅ Implementar validação NIS com INSS
- ✅ Cálculo preciso de idade (com mês e dia)

### Longo Prazo (Spring Boot)
```java
// Migração esperada
@RestController
@RequestMapping("/api/v1/beneficiarios")
public class BeneficiarioController {
    @PostMapping
    public ResponseEntity<Beneficiario> include(@Valid @RequestBody BeneficiarioDTO dto) { ... }
    
    @PutMapping("/{cpf}")
    public ResponseEntity<Beneficiario> alter(@PathVariable String cpf, ...) { ... }
}
```

---

## 📈 Risco Geral: MÉDIO-ALTO ⚠️

### Justificativa
✅ **Estável:** 15 anos sem alterações críticas  
✅ **Validações Core:** CPF e unicidade implementadas  
❌ **Múltiplas Lacunas:** Datas, referências externas, NIS  
❌ **Justificativas Ausentes:** Limite de 75 anos, cálculo de idade impreciso  
⚠️ **Dependência de Negócio:** Regra pode estar desatualizada (2011)

---

## 📝 Próximos Passos Recomendados

1. **Entrevistar JOSE FERREIRA** (ou sucessor)
   - Por que limite de 75 anos?
   - Ainda válido em 2026?

2. **Auditoria de Dados**
   - Há beneficiários com CPF=0?
   - Há CPFs inválidos (falsos positivos de validação)?
   - Há programa/região órfãos?

3. **Integração INSS**
   - Validar NIS em cadastro nacional?
   - Protocolo de sincronização?

4. **Planejamento de Migração**
   - Converter para Java/Spring Boot
   - Implementar validações faltantes
   - Adicionar auditoria completa

---

**Arquivo de Análise Detalhada:** `CADBENEF-ANALISE-ARQUEOLOGICA.yaml`
