<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Ambiguidades de Dados (DDM) - Stage 1

## Objetivo

Registrar ambiguidades encontradas nos DDMs Adabas que podem gerar risco de modelagem,
query errada ou regressao funcional na migracao.

## Escala de severidade

- Alta: pode quebrar regra de negocio critica
- Media: pode gerar divergencia funcional relevante
- Baixa: ponto de nomenclatura/documentacao

## Ambiguidades catalogadas

| ID | DDM | Campo(s) | Evidencia | Ambiguidade | Risco | Severidade | Acao sugerida |
| --- | --- | --- | --- | --- | --- | --- | --- |
| DDM-AMB-001 | PROGRAMA-SOCIAL | BG FATOR-K | PROGRAMA-SOCIAL.ddm (comentario de campo BG) | Campo inserido por solicitacao SENARC sem semantica formal documentada | Calculo incorreto de beneficio no novo sistema | Alta | Validar com SME e registrar regra no catalogo BR antes de codificar |
| DDM-AMB-002 | BENEFICIARIO | CA COD-PROGRAMA | BENEFICIARIO.ddm (remark marca campo como PE, mas sem grupo PE definido para CA) | Marcacao PE inconsistente com estrutura declarada | Mapeamento errado (1:1 vs historico 1:N por programa) | Alta | Definir com negocio se beneficiario pode ter historico multi-programa |
| DDM-AMB-003 | BENEFICIARIO | DB CPF-DEPENDENTE | BENEFICIARIO.ddm (remark: CPF ou 00000000000) | Uso de CPF sentinela nao representa CPF valido | Violacao de qualidade de dados e joins incorretos | Alta | Definir regra de normalizacao para sentinela no ETL |
| DDM-AMB-004 | AUDITORIA | BA COD-ACAO = EX | AUDITORIA.ddm NOTA2 (programa RELAUDIT filtra EX) | Exclusoes existem no dado bruto, mas podem nao aparecer em relatorio legado | Perda de trilha de auditoria ao reproduzir relatorios | Alta | Garantir visao completa em audit_event sem filtro implicito |
| DDM-AMB-005 | PAGAMENTO | DA SIT-PAGAMENTO | PAGAMENTO.ddm (status em codigos de 1 char) | Nao ha tabela de dominio oficial no DDM para ciclo de vida e transicoes | Regras de transicao inconsistentes entre modulos | Media | Criar tabela de dominio e mapear transicoes permitidas |
| DDM-AMB-006 | BENEFICIARIO/PAGAMENTO | Datas N(8) e horas N(6) | BENEFICIARIO.ddm e PAGAMENTO.ddm | Valores 0 sao usados como "sem prazo" em alguns campos de data | Parse invalido e perda de semantica no PostgreSQL date/time | Media | Definir regra unica: 0 -> NULL + coluna de semantica quando necessario |
| DDM-AMB-007 | AUDITORIA | DA/DB/DC/DD/DE/DF (MU pareados) | AUDITORIA.ddm (MU max 20 em pares antes/depois) | Nao esta explicito se ocorrencias sao sempre alinhadas por indice | Reconstrucao incorreta de diff de alteracao | Media | Validar comportamento no Natural e adicionar teste de equivalencia |
| DDM-AMB-008 | PROGRAMA-SOCIAL | EA TIPO-DSCT-APLIC (MU) | PROGRAMA-SOCIAL.ddm (lista codigos no comentario) | Nao ha tabela oficial de codigos/descricao e vigencia | Inconsistencia de codigos no novo schema | Media | Criar tabela de dominio de tipo desconto no Stage 2 |
| DDM-AMB-009 | PAGAMENTO | AB NUM-CPF e AC NUM-INSCRICAO | PAGAMENTO.ddm | DDM nao esclarece precedencia para reconciliar chave de beneficiario | Duplicidade de vinculo ou quebra de FK logica | Media | Definir chave canonica para relacionamento payment->beneficiary |
| DDM-AMB-010 | AUDITORIA | Retencao 10 anos vs sem purge | AUDITORIA.ddm (retencao minima e sem purge historico) | Politica de retencao legal nao define estrategia de arquivamento no destino | Crescimento sem controle e custo alto de storage/index | Baixa | Definir politica de particionamento e arquivamento no Stage 2 |

---

## Perguntas para handoff (Stage 1 -> Stage 2)

1. O campo `COD-PROGRAMA` em beneficiary representa estado atual ou historico de vinculos?
2. O `FATOR-K` participa de quais formulas e em quais condicoes?
3. Quais status de pagamento podem transitar entre si (matriz de transicao)?
4. Campos de data com valor 0 devem virar NULL em todos os casos?
5. Em auditoria, pares MU before/after sao sempre sincronizados por `occurrence_no`?

---

## Dependencias para Stage 3

- Sem resposta para DDM-AMB-001 e DDM-AMB-002 nao deve fechar migration final de calculo e vinculo programa-beneficiario.
- Sem resposta para DDM-AMB-004 nao deve fechar relatorio de auditoria equivalente.
- Sem resposta para DDM-AMB-006 e DDM-AMB-007 ha risco de erro de ETL e diff historico.
