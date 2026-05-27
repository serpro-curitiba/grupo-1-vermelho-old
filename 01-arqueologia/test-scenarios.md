<!-- markdownlint-disable MD013 MD025 MD026 MD028 MD029 MD034 MD040 MD051 MD060 -->

# Cenarios de Teste por BR (Stage 1)

## Objetivo

Registrar, ainda no Stage 1, os cenarios candidatos para cada BR-XXX extraida dos programas `VAL*.NSN`.
Esses cenarios alimentam o `/create-tests` e o `/test-strategy` nos Stages 2 e 3.

## Convencao

Para cada BR registramos:
- happy path (caminho feliz);
- pelo menos 1 limite (boundary);
- pelo menos 1 negativo (rejeicao explicita);
- camada sugerida (unit, integration, contract, e2e).

Camada padrao: unit, exceto quando exige acesso a Beneficiario+Programa (integration).

## Mapeamento BR -> cenarios

### BR-001 - CPF valido por mod-11
- Fonte: VALBENEF.NSN, VALDOCS.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptValidCpf_whenChecksumMatches
  - shouldRejectCpf_whenFirstCheckDigitWrong (boundary do DV1)
  - shouldRejectCpf_whenSecondCheckDigitWrong (boundary do DV2)
  - shouldRejectCpf_whenLengthDifferentFrom11

### BR-002 - CPF repetido invalido, exceto prefixo 000
- Fonte: VALBENEF.NSN
- Camada: unit
- Cenarios:
  - shouldRejectCpf_whenAllDigitsEqualAndNotStartingWith000 (ex: 11111111111)
  - shouldAcceptCpf_whenAllDigitsEqualAndStartsWith000 (ex: 00000000000)
  - shouldRejectCpf_whenStartsWith000ButHasMixedDigits_butChecksumInvalid

### BR-003 - Prefixos especiais bypassam validacao de documentos
- Fonte: VALDOCS.NSN
- Camada: unit
- Cenarios:
  - shouldBypassDocumentValidation_whenCpfStartsWith_000_001_002_010_011_099_100_999 (parametrizado)
  - shouldNotBypass_whenPrefixNotInSpecialList (ex: 123)
  - shouldEmitWarning_whenBypassApplied (rastreabilidade obrigatoria)

### BR-004 - CPF zero invalido
- Fonte: VALDOCS.NSN
- Camada: unit
- Cenarios:
  - shouldRejectCpf_whenCpfIsZero
  - shouldRejectCpf_whenCpfIsBlank

### BR-005 - Ano de nascimento entre 1900 e ano atual
- Fonte: VALBENEF.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptBirthDate_whenYearIs1900 (limite inferior)
  - shouldAcceptBirthDate_whenYearIsCurrent (limite superior)
  - shouldRejectBirthDate_whenYearBefore1900
  - shouldRejectBirthDate_whenYearInFuture

### BR-006 - Validacao dia/mes da data de nascimento (sem checagem bissexto)
- Fonte: VALBENEF.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptDate_whenFeb29InLeapYear
  - shouldAcceptDate_whenFeb29InNonLeapYear_legacyBehavior (comportamento legado)
  - shouldRejectDate_whenFeb30
  - shouldRejectDate_whenMonth13
  - shouldRejectDate_whenDay0
  - shouldRejectDate_whenApril31

### BR-007 - Nome obrigatorio com sobrenome
- Fonte: VALBENEF.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptName_whenContainsFirstAndLastName
  - shouldRejectName_whenBlank
  - shouldRejectName_whenSingleWord
  - shouldRejectName_whenOnlySpaces

### BR-008 - UF deve ser uma das 27 validas
- Fonte: VALBENEF.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptUf_whenInValidList (parametrizado com as 27)
  - shouldAcceptUf_whenBlank (UF opcional quando vazia)
  - shouldRejectUf_whenLengthDifferentFrom2
  - shouldRejectUf_whenNotInValidList (ex: XX)
  - shouldRejectUf_whenLowercase (caso comportamento exato deva ser preservado)

### BR-009 - Status valido em A, S, C, I, D
- Fonte: VALBENEF.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptStatus_whenInDomain (parametrizado A,S,C,I,D)
  - shouldRejectStatus_whenOutsideDomain (ex: X, vazio)
  - shouldRejectStatus_whenLowercase (preservar comportamento legado)

### BR-010 - RG com pelo menos 5 caracteres
- Fonte: VALDOCS.NSN
- Camada: unit
- Cenarios:
  - shouldAcceptRg_whenLength5 (limite)
  - shouldRejectRg_whenLength4
  - shouldRejectRg_whenBlank

### BR-011 - Elegibilidade exige status A
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldBeEligible_whenBeneficiaryStatusActive
  - shouldNotBeEligible_whenStatusSuspended
  - shouldNotBeEligible_whenStatusCancelled
  - shouldNotBeEligible_whenStatusInactive
  - shouldNotBeEligible_whenStatusDisaffiliated
  - shouldReturnSpecificReason_perStatus (rastreabilidade da causa)

### BR-012 - Programa existente e ativo
- Fonte: VALELEG.NSN
- Camada: integration (carrega `PROGRAMA-SOCIAL`)
- Cenarios:
  - shouldRejectEligibility_whenProgramNotFound
  - shouldRejectEligibility_whenProgramInactive
  - shouldProceed_whenProgramActive

### BR-013 - Regiao 99 bypassa elegibilidade
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldBypassAllChecks_whenRegionIs99
  - shouldNotBypass_whenRegionDifferentFrom99
  - shouldEmitAudit_whenBypassApplied (rastreabilidade)

### BR-014 - Faixa etaria do programa
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldAccept_whenAgeEqualsIdadeMin
  - shouldAccept_whenAgeEqualsIdadeMax
  - shouldReject_whenAgeBelowIdadeMin
  - shouldReject_whenAgeAboveIdadeMax
  - shouldSkipCheck_whenIdadeMinAndIdadeMaxAreZero

### BR-015 - Renda nao excede RENDA-MAX
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldAccept_whenIncomeEqualsRendaMax (limite)
  - shouldReject_whenIncomeAboveRendaMax (ex: 1 centavo acima)
  - shouldSkipCheck_whenRendaMaxIsZero

### BR-016 - Programa A: renda > 600 sem dependentes rejeita e exige docs OK
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldReject_whenTypeA_andIncomeAbove600_andNoDependents
  - shouldAccept_whenTypeA_andIncomeAbove600_andHasDependents
  - shouldAccept_whenTypeA_andIncomeBelowOrEqual600 (boundary 600.00)
  - shouldReject_whenTypeA_andDocumentsNotOk
  - shouldAccept_whenTypeA_andDocumentsOk

### BR-017 - Programa P exige idade >= 60
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldAccept_whenTypeP_andAge60 (boundary)
  - shouldReject_whenTypeP_andAge59
  - shouldAccept_whenTypeP_andAge75

### BR-018 - Programa T exige idade 16..65
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldAccept_whenTypeT_andAge16 (boundary inferior)
  - shouldAccept_whenTypeT_andAge65 (boundary superior)
  - shouldReject_whenTypeT_andAge15
  - shouldReject_whenTypeT_andAge66

### BR-019 - Tipo de programa desconhecido rejeita
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldReject_whenProgramTypeIsUnknown (ex: X)
  - shouldReject_whenProgramTypeIsBlank

### BR-020 - COD-ELEG iniciando com R exige NIS valido
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldReject_whenCodElegStartsWithR_andNisIsZero
  - shouldAccept_whenCodElegStartsWithR_andNisIsValid
  - shouldSkipCheck_whenCodElegDoesNotStartWithR

### BR-021 - COD-ELEG posicao 2 = D exige dependentes
- Fonte: VALELEG.NSN
- Camada: unit
- Cenarios:
  - shouldReject_whenCodElegSecondCharIsD_andNumDepIsZero
  - shouldAccept_whenCodElegSecondCharIsD_andNumDepGreaterThanZero
  - shouldSkipCheck_whenCodElegSecondCharIsNotD

## Resumo

- BRs cobertas: 21
- BRs criticas com cenario explicito de regra escondida: BR-002, BR-003, BR-013
- Casos de equivalencia com legado recomendados: BR-001, BR-002, BR-003, BR-006 (fev 29), BR-013, BR-016

## Proximos passos

- Validar com PO/RE as exclusoes (BR-002, BR-003, BR-013) antes de virarem teste positivo no Stage 3.
- Promover BRs `CRITICO` para a coluna principal do `TEST-STRATEGY.md` no Stage 2.
- Materializar os testes em JUnit 5/AssertJ ou Vitest no Stage 3 via `/create-tests`.
